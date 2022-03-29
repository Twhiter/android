package com.example.mobilepay.ui.register

import android.graphics.Bitmap
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import com.example.mobilepay.R
import com.example.mobilepay.databinding.FragmentRegisterPasswordSetBinding
import com.example.mobilepay.entity.ResponseData
import com.example.mobilepay.network.UserApi
import com.lzj.pass.dialog.PayPassDialog
import com.lzj.pass.dialog.PayPassView
import kotlinx.coroutines.*
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import java.io.File
import java.util.*


typealias onPayFinishHandler = (pass:String?) -> Unit


class PasswordSetFragment : Fragment() {

    private lateinit var _binding: FragmentRegisterPasswordSetBinding

    val binding get() = _binding

    private val viewModel:RegisterViewModel by activityViewModels()



    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentRegisterPasswordSetBinding.inflate(inflater, container, false)
        return _binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        //check password and repeat password the same
        binding.confirmPassword.setOnFocusChangeListener {_,isFocused ->
            if (!isFocused)
            checkPassword()
        }

        binding.paymentPassword.setOnClickListener {
            payDialog("Please set password") { it1 ->
                val first:String? = it1

                payDialog("Please confirm password") {
                    val second:String? = it
                    if (first.equals(second)) {
                        Toast.makeText(requireContext(),"setting password successfully!"
                            ,Toast.LENGTH_LONG).show()
                        viewModel.setPaymentPassword(it!!)

                    } else {
                        Toast.makeText(requireContext(),"Password not the same",
                            Toast.LENGTH_LONG).show()
                        viewModel.setPaymentPassword("")
                    }

                    Log.d("Mainss",viewModel.paymentPassword.value.toString())

                }

            }
        }

        binding.finish.setOnClickListener { finish() }



    }


    private fun checkPassword():Boolean {

        val confirmPwd = binding.confirmPassword.text.toString()
        val password = binding.password.text.toString()
        if (confirmPwd != password) {
            binding.confirmPasswordLayout.error = getString(R.string.password_not_same_prompt)
            return false
        }
        else {
            binding.confirmPasswordLayout.error = null
            return true
        }
    }

    private fun checkPaymentPassword():Boolean {
        return if (viewModel.paymentPassword.value.isNullOrEmpty()) {
            Toast.makeText(requireContext(),"Payment password not set",Toast.LENGTH_LONG).show()
            false
        }else
            true
    }



    private fun checkAll():Boolean {
        return checkPassword() && checkPaymentPassword()
    }


    private fun finish() {

        if (!checkAll())
            return
        viewModel.setPassword(binding.password.text.toString())

        CoroutineScope(Dispatchers.Default).launch {

            val resp = submitData()

            if (resp.status != ResponseData.OK) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(requireContext(),resp.errorPrompt,Toast.LENGTH_LONG).show()
                }
                return@launch
            }

            withContext(Dispatchers.Main) {
                toFinalPage()
            }
        }
    }


    private suspend fun submitData():ResponseData<*> {

        val country = MultipartBody.Part.createFormData("country",viewModel.nationality.value!!)
        val email = MultipartBody.Part.createFormData("email",viewModel.email.value!!)
        val firstName = MultipartBody.Part.createFormData("firstName",viewModel.firstName.value!!)
        val lastName = MultipartBody.Part.createFormData("lastName",viewModel.familyName.value!!)
        val passportNumber = MultipartBody.Part.createFormData("passportNumber",viewModel.IdNumber.value!!)
        val password = MultipartBody.Part.createFormData("password",viewModel.password.value!!)
        val paymentPassword = MultipartBody.Part.createFormData("paymentPassword"
            ,viewModel.paymentPassword.value!!)
        val phoneNumber = MultipartBody.Part.createFormData("phoneNumber"
            ,viewModel.phoneCode.value!! + viewModel.phone.value!!)

        return withContext(Dispatchers.IO) {
            val f = File(requireContext().filesDir,UUID.randomUUID().toString() + ".")

            viewModel.IdPhoto.value!!.compress(Bitmap.CompressFormat.JPEG,100,f.outputStream())

            val passportPhoto = MultipartBody.Part.createFormData("passportPhoto",f.name,
                RequestBody.create(MediaType.parse("image/*"),f))


            UserApi.service.register(country, email, firstName, lastName, passportNumber
                , passportPhoto, password, paymentPassword, phoneNumber)
        }
    }

    private fun toFinalPage() {
        val action = PasswordSetFragmentDirections.actionPasswordSetFragmentToFinalFragment()
        findNavController().navigate(action)
    }






    private fun payDialog(title:String,handler: onPayFinishHandler) {

        val dialog = PayPassDialog(requireContext(),R.style.dialog_pay_theme)

        dialog.setAlertDialog(false)
            .setWindowSize(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT,0.4f)
            .setOutColse(false)
            .setGravity(R.style.dialogOpenAnimation, Gravity.BOTTOM)

        dialog.payViewPass
            .setRandomNumber(true)
            .setHintText(title)
            .setForgetText("")
            .setPayClickListener(object :PayPassView.OnPayClickListener {
                override fun onPassFinish(password: String?) {
                    handler(password)
                    dialog.dismiss()
                }
                override fun onPayClose() {
                    viewModel.setPaymentPassword("")
                    Log.d("Mainss",viewModel.paymentPassword.value.toString())
                    dialog.dismiss()
                }
                override fun onPayForget() {}
            })
    }
}
