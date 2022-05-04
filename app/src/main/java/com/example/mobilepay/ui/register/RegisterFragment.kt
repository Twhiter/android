package com.example.mobilepay.ui.register

import android.content.Intent
import android.graphics.BitmapFactory
import android.media.MediaScannerConnection
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.FileProvider
import androidx.core.graphics.drawable.toBitmap
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.findNavController
import com.example.mobilepay.R
import com.example.mobilepay.Util
import com.example.mobilepay.databinding.FragmentRegisterBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textfield.TextInputLayout
import java.io.File
import java.io.IOException


class RegisterFragment : Fragment() {

    private var _binding: FragmentRegisterBinding? = null

    val binding get() = _binding!!

    private val viewModel: RegisterViewModel by activityViewModels()


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentRegisterBinding.inflate(inflater, container, false)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.lifecycleOwner = this
        binding.viewmodel = viewModel

        binding.country.listSelection


        //set listen on first name and last name textInput
        binding.familyName.addTextChangedListener {
            checkFamilyName()
        }

        binding.firstName.addTextChangedListener {
            checkFirstName()
        }

        //set adaptor for countries
        val countries = requireContext().resources.getStringArray(R.array.country_list)
        val adaptor = ArrayAdapter(requireContext(), R.layout.list_country_item, countries)
        binding.country.setAdapter(adaptor)


        //set click action for next
        binding.next.setOnClickListener {

            if (checkAll()) {

                viewModel.setFamilyName(binding.familyName.text.toString())
                viewModel.setFirstName(binding.firstName.text.toString())
                viewModel.setIdNumber(binding.IDNumber.text.toString())
                viewModel.setNationality(binding.country.text.toString())
                viewModel.setIdPhoto(binding.imageView.drawable.toBitmap())


                val action =
                    RegisterFragmentDirections.actionRegisterFragmentToEmailAndPhoneVerify()
                view.findNavController().navigate(action)
            }
        }

        binding.upload.setOnClickListener {
            uploadAction()
        }
    }


    private fun checkFirstName(): Boolean {
        return checkName(binding.firstName.text!!, binding.firstNameLayout,
            getString(R.string.first_name_empty_prompt),
            getString(R.string.first_name_whiterspace_prompt))
    }

    private fun checkFamilyName(): Boolean {
        return checkName(binding.familyName.text!!, binding.familyNameLayout,
            getString(R.string.family_name_empty_prompt),
            getString(R.string.family_name_whiterspace_prompt))
    }

    private fun checkName(
        editable: CharSequence,
        textLayoutInput: TextInputLayout,
        vararg errorStrs: String,
    ): Boolean {

        if (editable.isBlank())
            textLayoutInput.error = errorStrs[0]
        else if (editable.contains(' '))
            textLayoutInput.error = errorStrs[1]
        else {
            textLayoutInput.error = null
            return true
        }
        return false
    }

    private fun checkNationality(): Boolean {
        if (binding.country.text.isBlank())
            binding.nationalityInputLayout.error = getString(R.string.select_nationality_prompt)
        else {
            binding.nationalityInputLayout.error = null
            return true
        }
        return false
    }

    private fun checkIDNumber(): Boolean {

        binding.IDNumber.text?.apply {

            if (this.isBlank()) {
                binding.IDNumberTextInput.error = getString(R.string.id_number_prompt)
                return false
            } else {
                binding.IDNumberTextInput.error = null
                return true
            }
        }
        return true
    }

    private fun checkIDDocument(): Boolean {

        if (binding.imageView.drawable == null) {
            Toast.makeText(requireContext(), R.string.upload_document_ID_prompt, Toast.LENGTH_LONG)
                .show()
            return false
        }
        return true
    }


    private fun checkAll(): Boolean {
        return checkFirstName()
                && checkFamilyName()
                && checkNationality()
                && checkIDNumber()
                && checkIDDocument()
    }


    private fun uploadAction() {
        val items = arrayOf("Camera", "Select Image File")

        MaterialAlertDialogBuilder(requireContext())
            .setTitle(resources.getString(R.string.upload_text))
            .setItems(items) { _, which ->

                when (which) {
                    0 -> openCamera()
                    1 -> selectFile()
                }
            }
            .show()
    }


    private inner class CaptureImageLauncherCallBack :
        ActivityResultCallback<ActivityResult> {

        lateinit var file: File

        override fun onActivityResult(result: ActivityResult?) {
            viewModel.setIdPhoto(BitmapFactory.decodeFile(file.path))
        }
    }

    private val captureImageLauncherCallBack = CaptureImageLauncherCallBack()

    private val takeImageLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult(), captureImageLauncherCallBack)


    private val selectImageLauncher = registerForActivityResult(
        ActivityResultContracts.OpenDocument()) { result ->
        if (result != null) {

            val resolver = requireContext().contentResolver
            var byteArray: ByteArray? = null

            resolver.openInputStream(result).use { stream ->
                stream?.let {
                    byteArray = it.readBytes()
                }
            }

            byteArray?.apply {
                viewModel.setIdPhoto(BitmapFactory.decodeByteArray(this, 0, this.size))
            }
        } else {
            Log.d("Mainss", "error")
        }
    }


    private fun openCamera() {

        var photoFile: File? = null
        try {
            photoFile = Util.createImageFile()
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
                null, null)
        }
    }

    private fun selectFile() {
        selectImageLauncher.launch(arrayOf("image/*"))
    }
}






