package com.example.mobilepay.ui.mainPage

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.mobilepay.Util
import com.example.mobilepay.databinding.FragmentScanBinding
import com.example.mobilepay.entity.QrCodeContent
import com.google.android.material.snackbar.Snackbar
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancelChildren
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class ScanFragment : Fragment() {

    private lateinit var binding: FragmentScanBinding
    private lateinit var cameraExecutor: ExecutorService

    override fun onResume() {
        super.onResume()
        MainPageActivity.getInstance()?.setFullScreenVisibility(View.GONE)
    }


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        binding = FragmentScanBinding.inflate(layoutInflater)
        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        if (allPermissionsGranted()) {
            startCamera()
        } else {
            tryRequestPermissions()
        }
        cameraExecutor = Executors.newSingleThreadExecutor()
    }


    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray,
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_CODE_PERMISSIONS)
            if (allPermissionsGranted())
                startCamera()
            else {
                Toast.makeText(requireContext(),
                    "Permissions not granted by the user.",
                    Toast.LENGTH_SHORT).show()
            }
    }

    override fun onDestroy() {
        super.onDestroy()
        cameraExecutor.shutdown()
    }


    private fun startCamera() {

        val cameraProviderFuture = ProcessCameraProvider.getInstance(requireContext())

        cameraProviderFuture.addListener({

            //used to bind the lifecycle of cameras
            val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()

            //preview
            val preview = Preview.Builder().build()
                .also { it.setSurfaceProvider(binding.barScanner.surfaceProvider) }

            val qrCodeImageAnalyzer = ImageAnalysis.Builder().build().also {
                it.setAnalyzer(cameraExecutor, QRCodeImageAnalyzer())
            }

            //select back camera as default
            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

            try {
                //unbind use cases before rebinding
                cameraProvider.unbindAll()

                //bind the use case to the camera
                cameraProvider.bindToLifecycle(this, cameraSelector, preview, qrCodeImageAnalyzer)

            } catch (e: Exception) {
                Log.d("Mainss", "error")
            }
        }, ContextCompat.getMainExecutor(requireContext()))
    }

    private fun allPermissionsGranted() = REQUIRED_PERMISSIONS.all {
        ContextCompat.checkSelfPermission(requireContext(), it) == PackageManager.PERMISSION_GRANTED
    }


    private fun tryRequestPermissions() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(requireActivity(),
                Manifest.permission.CAMERA)
        ) {

            Snackbar.make(binding.root, "Need this permission", Snackbar.LENGTH_INDEFINITE)
                .setAction("OK") {
                    ActivityCompat.requestPermissions(
                        requireActivity(),
                        REQUIRED_PERMISSIONS,
                        REQUEST_CODE_PERMISSIONS
                    )
                }.show()
        } else
            ActivityCompat.requestPermissions(
                requireActivity(),
                REQUIRED_PERMISSIONS,
                REQUEST_CODE_PERMISSIONS
            )
    }


    companion object {

        private const val REQUEST_CODE_PERMISSIONS = 10
        private val REQUIRED_PERMISSIONS =
            mutableListOf(
                Manifest.permission.CAMERA,
            ).apply {
                if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.P) {
                    add(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                }
            }.toTypedArray()


        private val options = BarcodeScannerOptions.Builder()
            .setBarcodeFormats(
                Barcode.FORMAT_ALL_FORMATS)
            .build()

        private val scanner = BarcodeScanning.getClient(options)
    }


    private inner class QRCodeImageAnalyzer : ImageAnalysis.Analyzer {

        val mutex = Mutex()

        @SuppressLint("UnsafeOptInUsageError")
        override fun analyze(imageProxy: ImageProxy) {


            val mediaImage = imageProxy.image ?: return
            val image = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)

            scanner.process(image).addOnSuccessListener { barcodes ->

                if (barcodes.size == 0)
                    return@addOnSuccessListener
                else if (barcodes.size != 1) {
                    Toast.makeText(
                        requireContext(), "Try to make camera scan only one qr code",
                        Toast.LENGTH_SHORT).show()
                    return@addOnSuccessListener
                }

                val barcode = barcodes[0]

                if (barcode.valueType != Barcode.TYPE_TEXT)
                    return@addOnSuccessListener

                val text = barcode.rawValue!!

                val qrCodeContent = Util.fromJsonToObject<QrCodeContent>(text)

                lifecycleScope.launch(Dispatchers.Main) {
                    qrCodeContent?.let {
                        mutex.lock()
                        try {
                            val action = ScanFragmentDirections.actionScanFragmentToPayFragment(it)
                            findNavController().navigate(action)
                            lifecycleScope.coroutineContext.cancelChildren()
                        } catch (e: Exception) {
                            mutex.unlock()
                        }
                    }
                }

            }.addOnFailureListener {
                imageProxy.close()
            }.addOnCompleteListener {
                imageProxy.close()
            }
        }
    }
}
