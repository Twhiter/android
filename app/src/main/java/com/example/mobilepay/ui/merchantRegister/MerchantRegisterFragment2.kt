package com.example.mobilepay.ui.merchantRegister

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.mobilepay.PhoneCode
import com.example.mobilepay.R
import com.example.mobilepay.Util
import com.example.mobilepay.databinding.FragmentMerchantRegister2Binding
import com.example.mobilepay.entity.ResponseData
import com.example.mobilepay.network.VerifyApi
import com.example.mobilepay.network.VerifyApiService
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


class MerchantRegisterFragment2 : Fragment() {

    private val viewModel:ViewModel2 by viewModels()
    private val infoViewModel:MerchantRegisterViewModel by activityViewModels()
    private lateinit var binding: FragmentMerchantRegister2Binding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentMerchantRegister2Binding.inflate(inflater,container,false)
        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        binding.yes.performClick()

        binding.viewModel = viewModel
        binding.infoViewModel = infoViewModel
        binding.lifecycleOwner = this

        //set use individual or not
        binding.toggleButton.addOnButtonCheckedListener { _, checkedId, isChecked ->

            if (isChecked) {
                viewModel.useIndividualPhone.value =
                    (requireView().findViewById<Button>(checkedId) == binding.yes)
            }
        }


        //select phone country code
        binding.codeSelect.setOnClickListener {
            val items = PhoneCode.COUNTRY_CODES.map { it.countryName + " " + it.code }

            MaterialAlertDialogBuilder(requireContext())
                .setTitle(getString(R.string.country_region_prompt))
                .setItems(items.toTypedArray()) {_,which ->
                    binding.codeSelect.setText(PhoneCode.COUNTRY_CODES[which].code)
                }
                .show()
        }

        binding.phone.addTextChangedListener {
            it?.let { checkPhone() }
        }

        binding.next.setOnClickListener {

            if (viewModel.useIndividualPhone.value == true)
                checkWithDefaultPhone()
            else
                checkWithNewPhone()

        }

        binding.send.setOnClickListener {
            sendPhoneVerify()
        }



    }

    private fun checkWithNewPhone() {
        lifecycleScope.launch(Dispatchers.Main) {

            if (!(checkCode() && checkPhone()))
                return@launch

            val verifyCodeErrStr = checkPhoneVerifyCode()
            if (verifyCodeErrStr != null) {
                binding.phoneVerifyCodeLayout.error = verifyCodeErrStr
                return@launch
            }


            infoViewModel.merchantRegisterInfo.phoneCode = binding.codeSelect.text.toString()
            infoViewModel.merchantRegisterInfo.phoneDigits = binding.phone.text.toString()


            val action = MerchantRegisterFragment2Directions
                .actionMerchantRegisterFragment2ToMerchantRegisterFinal()
            findNavController().navigate(action)
        }
    }

    private fun checkWithDefaultPhone() {
        infoViewModel.merchantRegisterInfo.phoneCode = null
        infoViewModel.merchantRegisterInfo.phoneDigits = null

        val action = MerchantRegisterFragment2Directions
            .actionMerchantRegisterFragment2ToMerchantRegisterFinal()
        findNavController().navigate(action)
    }


    private fun checkPhone():Boolean {

        //do not check phone number if the phone code is not selected
        if (binding.codeSelect.text.isBlank())
            return true


        val isOkay = Util
            .checkPhone(
                binding.codeSelect.text.toString() + binding.phone.text.toString())

        return if (isOkay) {
            binding.phoneLayout.error = null
            true
        } else {
            binding.phoneLayout.error = getString(R.string.incorrect_phone_number_prompt)
            false
        }
    }

    private fun checkCode():Boolean {
        return if (binding.codeSelect.text.isBlank()) {
            binding.codeSelectLayout.error = "Please Select Code"
            false
        }else {
            binding.codeSelectLayout.error = null
            true
        }
    }

    private suspend fun checkPhoneVerifyCode():String? {


        if (binding.phoneVerifyCode.text.isNullOrBlank())
            return "Please Input verify code"

        val resp = VerifyApi.service.checkVerifyCode("phone",
            binding.codeSelect.text.toString() + binding.phone.text.toString()
            ,binding.phoneVerifyCode.text.toString())

        val isCorrect = resp.handleOneWithDefault(requireContext()) {
            it.data ?: false
        }

        return if (isCorrect)
            null
        else
            getString(R.string.incorrect_verify_code_prompt)
    }

    private  fun sendPhoneVerify() {

        if (!(checkCode() && checkPhone()))
            return

        lifecycleScope.launch(Dispatchers.IO) {

           val resp = VerifyApiService.sendVerifyCode("phone",binding.codeSelect.text.toString()
                   + binding.phone.text.toString())

            val isOkay = resp.handleDefault(requireContext())
            if (!isOkay)
                return@launch
            Util.suspendSend(binding.send)
        }
    }





}

class ViewModel2:ViewModel() {

    val useIndividualPhone = MutableLiveData(true)

}