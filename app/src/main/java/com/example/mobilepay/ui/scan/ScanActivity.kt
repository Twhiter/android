package com.example.mobilepay.ui.scan

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.mobilepay.databinding.ActivityScanBinding
import com.google.android.material.snackbar.Snackbar
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage
import java.lang.Exception
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class ScanActivity : AppCompatActivity() {


    private lateinit var binding: ActivityScanBinding
    private lateinit var cameraExecutor: ExecutorService





    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding =  ActivityScanBinding.inflate(layoutInflater)
        setContentView(binding.root)

        if (allPermissionsGranted()) {
            startCamera()
        }else {
            tryRequestPermissions()
        }

        cameraExecutor = Executors.newSingleThreadExecutor()
        supportActionBar?.hide()

    }


    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_CODE_PERMISSIONS)
                if (allPermissionsGranted())
                    startCamera()
            else {
                Toast.makeText(this,
                    "Permissions not granted by the user.",
                    Toast.LENGTH_SHORT).show()
            }
    }

    override fun onDestroy() {
        super.onDestroy()
        cameraExecutor.shutdown()
    }


    private fun startCamera() {

        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)

        cameraProviderFuture.addListener({

            //used to bind the lifecycle of cameras
            val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()

            //preview
            val preview = Preview.Builder().build()
                .also { it.setSurfaceProvider(binding.barScanner.surfaceProvider) }

            val qrCodeImageAnalyzer = ImageAnalysis.Builder().build().also {
                it.setAnalyzer(cameraExecutor,QRCodeImageAnalyzer())
            }

            //select back camera as default
            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

            try {
                //unbind use cases before rebinding
                cameraProvider.unbindAll()

                //bind the use case to the camera
                cameraProvider.bindToLifecycle(this,cameraSelector,preview
                    ,qrCodeImageAnalyzer)

            }catch (e: Exception) {
                Log.d("Mainss","error")
            }
        },ContextCompat.getMainExecutor(this))
    }







    private fun allPermissionsGranted() = REQUIRED_PERMISSIONS.all {
        ContextCompat.checkSelfPermission(this,it) == PackageManager.PERMISSION_GRANTED
    }



    private fun tryRequestPermissions() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(this
                ,Manifest.permission.CAMERA)) {

            Snackbar.make(binding.root,"Need this permission", Snackbar.LENGTH_INDEFINITE)
                .setAction("OK"){
                    ActivityCompat.requestPermissions(
                        this,
                        REQUIRED_PERMISSIONS,
                        REQUEST_CODE_PERMISSIONS
                    )
                }.show()
        }
        else
            ActivityCompat.requestPermissions(
                this,
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
    }



    private inner class QRCodeImageAnalyzer : ImageAnalysis.Analyzer {

        private val options = BarcodeScannerOptions.Builder()
            .setBarcodeFormats(
                Barcode.FORMAT_ALL_FORMATS)
            .build()

        private val scanner = BarcodeScanning.getClient(options)




        @SuppressLint("UnsafeOptInUsageError")
        override fun analyze(imageProxy: ImageProxy) {


            val mediaImage = imageProxy.image ?: return
            val image = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)

            scanner.process(image).addOnSuccessListener { barcodes ->

                if (barcodes.size == 0)
                    return@addOnSuccessListener
                else if (barcodes.size != 1) {
                    Toast.makeText(
                        this@ScanActivity, "Try to make camera only one Qr code",
                        Toast.LENGTH_SHORT).show()
                    return@addOnSuccessListener
                }

                val barcode = barcodes[0]

                if (barcode.valueType != Barcode.TYPE_URL)
                    return@addOnSuccessListener

                val title = barcode.url!!.title
                val url = barcode.url!!.url

                Toast.makeText(
                    this@ScanActivity,
                    "title is $title,url is $url",
                    Toast.LENGTH_SHORT
                ).show()
            }.addOnFailureListener {

                Log.e("Mainss", it.toString())
                println("Exception")
                imageProxy.close()
            }.addOnCompleteListener {
                imageProxy.close()
            }
        }
    }

}