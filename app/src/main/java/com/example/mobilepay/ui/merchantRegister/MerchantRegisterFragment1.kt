package com.example.mobilepay.ui.merchantRegister

import android.content.Intent
import android.graphics.BitmapFactory
import android.media.MediaScannerConnection
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.FileProvider
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModel
import androidx.navigation.fragment.findNavController
import com.example.mobilepay.Processor
import com.example.mobilepay.R
import com.example.mobilepay.Util.Companion.createImageFile
import com.example.mobilepay.databinding.FragmentMerchantRegister1Binding
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import java.io.File
import java.io.IOException

class MerchantRegisterFragment1 : Fragment() {

    private lateinit var binding:FragmentMerchantRegister1Binding
    private val viewModel:ViewModel1 by viewModels()
    private val infoViewModel:MerchantRegisterViewModel by activityViewModels()


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentMerchantRegister1Binding.inflate(inflater,container,false)
        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        binding.lifecycleOwner = this
        binding.infoViewModel = infoViewModel
        binding.viewModel = viewModel

        binding.name.addTextChangedListener {
            checkCompanyName(it.toString())
        }

        binding.licenseNumber.addTextChangedListener {
            checkCompanyLicenseNumber(it.toString())
        }

        binding.upload.setOnClickListener {
            uploadAction()
        }

        binding.next.setOnClickListener {

            val isOk = checkCompanyLicenseNumber(binding.licenseNumber.text.toString())
                    && checkCompanyName(binding.name.text.toString()) && checkPhoto()
            if (isOk) {

                infoViewModel.merchantRegisterInfo.companyName = binding.name.text.toString()
                infoViewModel.merchantRegisterInfo.licenseNumber = binding.licenseNumber.text
                    .toString()

                val action = MerchantRegisterFragment1Directions
                    .actionMerchantRegisterFragment1ToMerchantRegisterFragment2()
                findNavController().navigate(action)
            }
        }
    }


    private fun uploadAction() {
        val items = arrayOf("Camera","Select Image File")

        MaterialAlertDialogBuilder(requireContext())
            .setTitle(resources.getString(R.string.upload_license))
            .setItems(items) { _, which ->

                when(which) {
                    0 -> openCamera()
                    1 -> selectFile()
                }
            }
            .show()
    }

    private inner class CaptureImageLauncherCallBack :
        ActivityResultCallback<ActivityResult> {

        lateinit var file:File

        override fun onActivityResult(result: ActivityResult?) {
            this@MerchantRegisterFragment1.infoViewModel.merchantRegisterInfo.licensePhoto =
                BitmapFactory.decodeFile(file.path)
        }
    }

    private val captureImageLauncherCallBack = CaptureImageLauncherCallBack()

    private val takeImageLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult(),captureImageLauncherCallBack)

    private val selectImageLauncher = registerForActivityResult(
        ActivityResultContracts.OpenDocument()) {result ->
        if (result != null) {

            val resolver = requireContext().contentResolver
            var byteArray:ByteArray? = null

            resolver.openInputStream(result).use { stream->
                stream?.let {
                    byteArray = it.readBytes()
                }
            }

            byteArray?.apply {
                val bitMap = BitmapFactory.decodeByteArray(this,0, this.size)
                infoViewModel.merchantRegisterInfo.licensePhoto = bitMap
            }
        } else {
            Log.d("Mainss", "error")
        }
    }

    private fun openCamera() {

        var photoFile: File? = null;
        try {
            photoFile = createImageFile();
        } catch (e: IOException) {
            e.printStackTrace()
        }


        photoFile?.apply {
            val photoURI = FileProvider.getUriForFile(
                requireContext(),
                requireContext().applicationContext.packageName + ".FileProvider",
                this
            )

            val takeImageIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            takeImageIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI)
            captureImageLauncherCallBack.file = this
            takeImageLauncher.launch(takeImageIntent)

            MediaScannerConnection.scanFile(requireContext(), arrayOf(this.absolutePath),
                null,null)
        }
    }

    private fun selectFile() {
        selectImageLauncher.launch(arrayOf("image/*"))
    }


    private fun checkCompanyName(text:String):Boolean {


        val p = Processor<String>()
        val r = p.addHandler {
            if (text.isBlank())
                "Please Input Name"
            else null
        }.process()

        return if (r != null){
            binding.companyNameLayout.error = r
            false
        }else {
            binding.companyNameLayout.error = null
            true
        }
    }

    private fun checkCompanyLicenseNumber(text: String):Boolean {


        val p = Processor<String>()
        val r = p.addHandler {
            if (text.isBlank())
                "Please Input LicenseNumber"
            else null
        }.process()

        return if (r != null) {
            binding.companyLicenseNumber.error = r
            false
        } else {
            binding.companyLicenseNumber.error = null
            true
        }
    }

    private fun checkPhoto():Boolean {

        if (infoViewModel.merchantRegisterInfo.licensePhoto == null) {
            Toast.makeText(requireContext(),"Please Upload Your License Photo",
                Toast.LENGTH_SHORT).show()
            return false
        }
        return true
    }
}

class ViewModel1:ViewModel() {

}