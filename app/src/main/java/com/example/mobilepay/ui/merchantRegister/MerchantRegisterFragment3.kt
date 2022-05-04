package com.example.mobilepay.ui.merchantRegister

import android.graphics.Bitmap
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.mobilepay.MainApplication
import com.example.mobilepay.Processor
import com.example.mobilepay.Util
import com.example.mobilepay.databinding.FragmentMerchantRegister3Binding
import com.example.mobilepay.entity.ResponseData
import com.example.mobilepay.network.MerchantApi
import com.example.mobilepay.network.VerifyApi
import com.example.mobilepay.network.VerifyApiService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.Request
import okhttp3.RequestBody
import java.io.File
import java.util.*


//todo test needed
class MerchantRegisterFinal : Fragment() {

    private lateinit var binding: FragmentMerchantRegister3Binding
    private val infoViewModel:MerchantRegisterViewModel by activityViewModels()
    private val viewModel:ViewModel3 by viewModels()



    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentMerchantRegister3Binding.inflate(inflater,container,false)
        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding.viewModel = viewModel
        binding.infoViewModel = infoViewModel
        binding.lifecycleOwner = this

        binding.yes.performClick()

        //set check change for toggle button
        binding.toggleButton.addOnButtonCheckedListener { _, checkedId, isChecked ->

            if (isChecked) {
                viewModel.useIndividualEmail.value =
                    (requireView().findViewById<Button>(checkedId) == binding.yes)
            }
        }

        //set change for email input
        binding.email.addTextChangedListener {
            it?.let { checkEmail() }
        }

        //set send action
        binding.sendEmail.setOnClickListener { sendEmailVerifyCode() }

        //set finish action
        binding.finish.setOnClickListener { finish() }

    }



    private fun checkEmail():Boolean {

        val processor = Processor<String?>()

        val str =  processor.addHandler{
            if (binding.email.text.isNullOrBlank())
                "Please Input"
            else
                null
        }.addHandler{
            if (Util.checkEmail(binding.email.text.toString()))
                null
            else
                "Incorrect Email Address"
        }.process()

        binding.emailInputLayout.error = str

        return str == null
    }

    private  fun sendEmailVerifyCode() {

        if (!checkEmail())
            return

        lifecycleScope.launch(Dispatchers.IO) {

            val resp = VerifyApiService.sendVerifyCode("email",binding.email.text.toString())

            val isOkay = resp.handleDefault(requireContext())
            if (!isOkay)
                return@launch
            Util.suspendSend(binding.sendEmail)
        }
    }

    private suspend fun checkVerifyCode():String? {

        if (binding.emailVerifyCode.text.isNullOrBlank())
            return "Please Input"

        val resp = withContext(Dispatchers.IO) {
            VerifyApi.service.checkVerifyCode("email",binding.email.text.toString()
                ,binding.emailVerifyCode.text.toString())
        }

        val isOkay = resp.handleDefault(requireContext())

        return if (!isOkay) "Incorrect Verify Code" else null
    }



    private fun checkWithNewEmail() {
        if (!checkEmail())
            return

        lifecycleScope.launch(Dispatchers.Default) {
            val str = checkVerifyCode()

            withContext(Dispatchers.Main) {
                binding.emailVerifyCodeInputLayout.error = str

                if (str != null)
                    return@withContext

                infoViewModel.merchantRegisterInfo.email = binding.email.text.toString()
                register()

            }
        }
    }

    private fun checkWithIndividualEmail() {
        infoViewModel.merchantRegisterInfo.email = null
        register()
    }


    private fun finish() {

        if (viewModel.useIndividualEmail.value == true)
            checkWithIndividualEmail()
        else
            checkWithNewEmail()
    }


    private suspend fun submitData():ResponseData<String?> {

        val token = MainApplication.db().kvDao().get("token")


        val companyName = MultipartBody.Part.createFormData("companyName"
            ,infoViewModel.merchantRegisterInfo.companyName)

        val licenseNumber = MultipartBody.Part.createFormData("licenseNumber"
            ,infoViewModel.merchantRegisterInfo.licenseNumber)

        val phoneNumber = infoViewModel.merchantRegisterInfo.phoneNumber?.let {
            MultipartBody.Part.createFormData("phoneNumber",it)
        }

        val email = infoViewModel.merchantRegisterInfo.email?.let {
            MultipartBody.Part.createFormData("email",it)
        }

        return withContext(Dispatchers.IO) {

            val f = File(requireContext().filesDir,UUID.randomUUID().toString() + ".jpeg")

            infoViewModel.merchantRegisterInfo.licensePhoto!!
                .compress(Bitmap.CompressFormat.JPEG,100,f.outputStream())

            val licensePhoto = MultipartBody.Part.createFormData("licensePhoto",f.name
                , RequestBody.create(MediaType.parse("image/*"),f))

            val resp = MerchantApi.service.register(token!!,companyName, licenseNumber, licensePhoto,
                phoneNumber,email)
            f.delete()
            resp
        }
    }

    private fun register() {

        //first submit data
        lifecycleScope.launch(Dispatchers.Default) {
            val resp = submitData()
            var errorPrompt:String? = null

            resp.handleOneWithDefault(requireContext()) {
                errorPrompt = it.data
                true
            }

            // if there is error , show the error and exit
            if (errorPrompt != null) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(requireContext(),errorPrompt,Toast.LENGTH_SHORT).show()
                }
                return@launch
            }

            //last step, go to the final page
            withContext(Dispatchers.Main) {

                val action = MerchantRegisterFinalDirections
                    .actionMerchantRegisterFinalToFinalFragment3(
                        infoViewModel.merchantRegisterInfo.phoneNumber?:
                        "Your individual account's phone number",
                        infoViewModel.merchantRegisterInfo.email?:
                        "Your individual account's email"
                    )
                findNavController().navigate(action)
            }
        }
    }



}
class ViewModel3:ViewModel() {
    val useIndividualEmail = MutableLiveData(true)
}