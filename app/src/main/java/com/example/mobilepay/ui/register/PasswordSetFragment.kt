package com.example.mobilepay.ui.register

import android.graphics.Bitmap
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.mobilepay.R
import com.example.mobilepay.databinding.FragmentRegisterPasswordSetBinding
import com.example.mobilepay.entity.ResponseData
import com.example.mobilepay.network.UserApi
import com.example.mobilepay.ui.lib.PayHandler
import com.example.mobilepay.ui.lib.PaymentDialog
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import java.io.File
import java.util.*


typealias onPayFinishHandler = (pass: String?) -> Unit


class PasswordSetFragment : Fragment() {

    private lateinit var _binding: FragmentRegisterPasswordSetBinding

    val binding get() = _binding

    private val viewModel: RegisterViewModel by activityViewModels()


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {

        _binding = FragmentRegisterPasswordSetBinding.inflate(inflater, container, false)
        return _binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        //check password and repeat password the same
        binding.confirmPassword.setOnFocusChangeListener { _, isFocused ->
            if (!isFocused)
                checkPassword()
        }

        binding.paymentPassword.setOnClickListener {

            viewModel.setPassword("")
            val paymentDialog = PaymentDialog(requireContext(), layoutInflater)
            paymentDialog.setTitle("Please set password")
            paymentDialog.setForgetText("")
            paymentDialog.setHandler(object : PayHandler {
                override fun onFinish(password: String) {

                    val first: String = password

                    val paymentDialog2 = PaymentDialog(requireContext(), layoutInflater)
                    paymentDialog2.setTitle("Please confirm password")
                    paymentDialog2.setForgetText("")

                    paymentDialog2.setHandler(object : PayHandler {
                        override fun onFinish(password: String) {
                            val second: String = password
                            if (first == second) {
                                Toast.makeText(requireContext(),
                                    "setting password successfully!",
                                    Toast.LENGTH_SHORT).show()
                                viewModel.setPaymentPassword(second)
                            } else
                                Toast.makeText(requireContext(), "Password not the same",
                                    Toast.LENGTH_SHORT).show()
                        }

                        override fun onClose() {}
                        override fun onForgetPassword() {}
                    }).show()

                }

                override fun onClose() {}
                override fun onForgetPassword() {}

            }).show()
        }

        binding.finish.setOnClickListener { finish() }


    }


    private fun checkPassword(): Boolean {

        val confirmPwd = binding.confirmPassword.text.toString()
        val password = binding.password.text.toString()
        if (confirmPwd != password) {
            binding.confirmPasswordLayout.error = getString(R.string.password_not_same_prompt)
            return false
        } else {
            binding.confirmPasswordLayout.error = null
            return true
        }
    }

    private fun checkPaymentPassword(): Boolean {
        return if (viewModel.paymentPassword.value.isNullOrEmpty()) {
            Toast.makeText(requireContext(), "Payment password not set", Toast.LENGTH_LONG).show()
            false
        } else
            true
    }


    private fun checkAll(): Boolean {
        return checkPassword() && checkPaymentPassword()
    }


    private fun finish() {

        if (!checkAll())
            return
        viewModel.setPassword(binding.password.text.toString())

        lifecycleScope.launch(Dispatchers.Default) {

            val resp = submitData()

            if (resp.status != ResponseData.OK) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(requireContext(), resp.errorPrompt, Toast.LENGTH_LONG).show()
                }
                return@launch
            }

            withContext(Dispatchers.Main) {
                toFinalPage()
            }
        }
    }


    private suspend fun submitData(): ResponseData<*> {

        val country = MultipartBody.Part.createFormData("country", viewModel.nationality.value!!)
        val email = MultipartBody.Part.createFormData("email", viewModel.email.value!!)
        val firstName = MultipartBody.Part.createFormData("firstName", viewModel.firstName.value!!)
        val lastName = MultipartBody.Part.createFormData("lastName", viewModel.familyName.value!!)
        val passportNumber =
            MultipartBody.Part.createFormData("passportNumber", viewModel.IdNumber.value!!)
        val password = MultipartBody.Part.createFormData("password", viewModel.password.value!!)
        val paymentPassword =
            MultipartBody.Part.createFormData("paymentPassword", viewModel.paymentPassword.value!!)
        val phoneNumber = MultipartBody.Part.createFormData("phoneNumber",
            viewModel.phoneCode.value!! + viewModel.phone.value!!)

        return withContext(Dispatchers.IO) {
            val f = File(requireContext().filesDir, UUID.randomUUID().toString() + ".jpeg")

            viewModel.IdPhoto.value!!.compress(Bitmap.CompressFormat.JPEG, 100, f.outputStream())

            val passportPhoto = MultipartBody.Part.createFormData("passportPhoto", f.name,
                RequestBody.create(MediaType.parse("image/*"), f))


            val resp = UserApi.service.register(country,
                email,
                firstName,
                lastName,
                passportNumber,
                passportPhoto,
                password,
                paymentPassword,
                phoneNumber)
            f.delete()
            resp
        }
    }

    private fun toFinalPage() {
        val action = PasswordSetFragmentDirections.actionPasswordSetFragmentToFinalFragment(
            viewModel.phoneNumber, viewModel.email.value!!
        )
        findNavController().navigate(action)
    }


}
