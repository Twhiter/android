package com.example.mobilepay.ui.register

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.findNavController
import com.example.mobilepay.PhoneCode
import com.example.mobilepay.R
import com.example.mobilepay.Util
import com.example.mobilepay.Util.Companion.suspendSend
import com.example.mobilepay.databinding.FragmentEmailAndPhoneVerifyBinding
import com.example.mobilepay.entity.ResponseData
import com.example.mobilepay.network.VerifyApi
import com.example.mobilepay.network.VerifyApiService.Companion.sendVerifyCode
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


class EmailAndPhoneVerify : Fragment() {

    private lateinit var _binding: FragmentEmailAndPhoneVerifyBinding

    val binding get() = _binding

    private val viewModel: RegisterViewModel by activityViewModels()


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {

        _binding = FragmentEmailAndPhoneVerifyBinding.inflate(inflater, container, false)
        return _binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.viewModel = viewModel
        binding.lifecycleOwner = this

        binding.codeSelect.setOnClickListener {
            val items = PhoneCode.COUNTRY_CODES.map { it.countryName + " " + it.code }

            MaterialAlertDialogBuilder(requireContext())
                .setTitle(getString(R.string.country_region_prompt))
                .setItems(items.toTypedArray()) { _, which ->
                    binding.codeSelect.setText(PhoneCode.COUNTRY_CODES[which].code)
                }
                .show()
        }

        binding.send.setOnClickListener {
            sendPhoneVerifyCode()
        }

        binding.sendEmail.setOnClickListener {
            sendEmailVerifyCode()
        }

        binding.next.setOnClickListener {
            next()
        }

    }

    private fun checkPhoneCode(): Boolean {
        return if (binding.codeSelect.text.toString() == "") {
            binding.codeSelectLayout.error = getString(R.string.select_code_prompt)
            false
        } else {
            binding.codeSelectLayout.error = null
            true
        }
    }

    private fun checkPhone(): Boolean {
        val isOkay: Boolean = Util
            .checkPhone(binding.codeSelect.text.toString() + binding.phone.text.toString())

        return if (isOkay) {
            true
        } else {
            binding.phoneLayout.error = getString(R.string.incorrect_phone_number_prompt)
            false
        }

    }

    private suspend fun checkPhoneVerifyCode(): Boolean {

        if (binding.phoneVerifyCode.text?.isBlank() == true) {
            lifecycleScope.launch {
                binding.phoneVerifyCodeLayout.error = "Please Input Verify Code"
            }
            return false
        }

        val resp = withContext(Dispatchers.IO) {
            checkVerifyCode("phone",
                binding.codeSelect.text.toString() + binding.phone.text.toString(),
                binding.phoneVerifyCode.text.toString())
        }

        if (resp.status != ResponseData.OK) {
            CoroutineScope(Dispatchers.Main).launch {
                Toast.makeText(requireContext(), resp.errorPrompt, Toast.LENGTH_LONG).show()
            }
            return false
        } else {
            if (resp.data == false) {

                CoroutineScope(Dispatchers.Main).launch {
                    binding.phoneVerifyCodeLayout.error =
                        getString(R.string.incorrect_verify_code_prompt)
                }
                return false
            }
        }
        CoroutineScope(Dispatchers.Main).launch {
            binding.phoneVerifyCodeLayout.error = null
        }
        return true
    }

    private fun sendPhoneVerifyCode() {

        if (!(checkPhoneCode() && checkPhone()))
            return

        lifecycleScope.launch(Dispatchers.IO) {
            val resp = sendVerifyCode("phone",
                binding.codeSelect.text.toString() + binding.phone.text.toString())

            if (resp.status != ResponseData.OK) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(requireContext(), resp.errorPrompt, Toast.LENGTH_LONG).show()
                }
                return@launch
            }
            suspendSend(binding.send)
        }
    }

    private suspend fun checkVerifyCode(
        type: String,
        target: String,
        code: String,
    ): ResponseData<Boolean> {
        return VerifyApi.service.checkVerifyCode(type, target, code)
    }


    private fun checkEmail(): Boolean {
        binding.email.text?.apply {
            val isOkay = Util.checkEmail(this.toString())
            if (isOkay) {
                binding.emailInputLayout.error = null
                return true
            }
        }

        binding.emailInputLayout.error = getString(R.string.incorrect_email_prompt)
        return false
    }


    private fun sendEmailVerifyCode() {
        if (!checkEmail())
            return
        CoroutineScope(Dispatchers.IO).launch {
            val resp = sendVerifyCode("email", binding.email.text.toString())

            if (resp.status != ResponseData.OK) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(requireContext(), resp.errorPrompt, Toast.LENGTH_LONG).show()
                }
                return@launch
            }
            suspendSend(binding.sendEmail)
        }
    }

    private suspend fun checkEmailVerifyCode(): Boolean {

        if (binding.email.text?.isBlank() == true) {
            CoroutineScope(Dispatchers.Main).launch {
                binding.phoneVerifyCodeLayout.error = "Please Input Verify Code"
            }
            return false
        }

        val resp = withContext(Dispatchers.IO) {
            checkVerifyCode("email",
                binding.email.text.toString(),
                binding.emailVerifyCode.text.toString())
        }

        if (resp.status != ResponseData.OK) {
            lifecycleScope.launch(Dispatchers.Main) {
                Toast.makeText(requireContext(), resp.errorPrompt, Toast.LENGTH_LONG).show()
            }

            return false
        } else {
            if (resp.data == false) {

                lifecycleScope.launch(Dispatchers.Main) {
                    binding.emailInputLayout.error =
                        getString(R.string.incorrect_verify_code_prompt)
                }
                return false
            }
        }
        lifecycleScope.launch(Dispatchers.Main) {
            binding.phoneVerifyCodeLayout.error = null
        }
        return true
    }


    private fun next() {

        lifecycleScope.launch(Dispatchers.Default) {

            if (!(checkEmailVerifyCode() && checkPhoneVerifyCode()))
                return@launch

            withContext(Dispatchers.Main) {
                viewModel.setPhoneCode(binding.codeSelect.text.toString())
                viewModel.setPhone(binding.phone.text.toString())
                viewModel.setEmail(binding.email.text.toString())
                val action =
                    EmailAndPhoneVerifyDirections.actionEmailAndPhoneVerifyToPasswordSetFragment()
                requireView().findNavController().navigate(action)
            }

        }
    }


}