package com.example.cameraxp.democamera

import android.content.pm.PackageManager
import android.graphics.Matrix
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.util.DisplayMetrics
import android.util.Log
import android.view.Surface
import android.view.TextureView
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.*
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.example.cameraxp.databinding.ActivityCameraBinding
import java.io.File
import java.util.concurrent.Executors

class CameraActivity : AppCompatActivity() {
    lateinit var binding : ActivityCameraBinding
    private val executor = Executors.newSingleThreadExecutor()
    lateinit var imagePreview : TextureView
    private val REQUEST_CODE_PERMISSION = 1
    private val REQUIRED_PERMISSIONS = arrayOf(android.Manifest.permission.CAMERA)
    lateinit var outputDirectory : File
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCameraBinding.inflate(layoutInflater)
        setContentView(binding.root)
        imagePreview = binding.imageRealtime
        outputDirectory = File(
            this.getExternalFilesDir(Environment.DIRECTORY_DCIM)?.absolutePath
                ?:this.externalCacheDirs.first().absolutePath
        )
        if(checkAllPermissionGranted()){
            binding.imageRealtime.post{
                startCamera()
            }
        }
        else{
            //nếu permission chưa đc chấp thuận thì cần phải request lại
            ActivityCompat.requestPermissions(
                this,REQUIRED_PERMISSIONS,REQUEST_CODE_PERMISSION
            )
        }
        imagePreview.addOnLayoutChangeListener { _, _, _, _, _, _, _, _, _ ->
            updateTransform()
        }
    }
    private fun startCamera(){
        //set for preview
        val displayMetrics = DisplayMetrics()
        windowManager.defaultDisplay.getMetrics(displayMetrics)
        val previewConfig = PreviewConfig.Builder()
                .setTargetAspectRatio(AspectRatio.RATIO_16_9)
//                .setTargetResolution(Size(displayMetrics.widthPixels,displayMetrics.heightPixels))
                .build()

        val preview = Preview(previewConfig)
        preview.setOnPreviewOutputUpdateListener{
            val parent = binding.imageRealtime.parent as ViewGroup
            parent.removeView(binding.imageRealtime)
            parent.addView(binding.imageRealtime,0)
            binding.imageRealtime.surfaceTexture = it.surfaceTexture
            updateTransform()
        }
        val imageCaptureConfig = ImageCaptureConfig.Builder()
            .apply {
                setTargetAspectRatio(AspectRatio.RATIO_16_9)
                setCaptureMode(ImageCapture.CaptureMode.MAX_QUALITY)
                setTargetRotation(imagePreview.display.rotation)
            }.build()
        val imageCapture = ImageCapture(imageCaptureConfig)
        binding.capture.setOnClickListener{
            val file = File(outputDirectory,
                "${System.currentTimeMillis()}.jpg")

            imageCapture.takePicture(file, executor,
                object : ImageCapture.OnImageSavedListener {
                    override fun onError(
                        imageCaptureError: ImageCapture.ImageCaptureError,
                        message: String,
                        exc: Throwable?
                    ) {
                        val msg = "Photo capture failed: $message"
                        Log.e("CameraXApp", msg, exc)
                        imagePreview.post {
                            Toast.makeText(baseContext, msg, Toast.LENGTH_SHORT).show()
                        }
                    }


                    override fun onImageSaved(file: File) {
                        val msg = "Photo capture succeeded: ${file.absolutePath}"
                        binding.imageStorage.post{
                            Glide.with(this@CameraActivity)
                                .load(file)
                                .apply(RequestOptions.circleCropTransform())
                                .into(binding.imageStorage)
                        }
                        imagePreview.post {
                            Toast.makeText(baseContext, msg, Toast.LENGTH_SHORT).show()
                        }
                    }
                })
        }
        CameraX.bindToLifecycle(this,preview,imageCapture)
    }
    private fun updateTransform() {
        val matrix = Matrix()

        // Compute the center of the view finder
        val centerX = imagePreview.width / 2f
        val centerY = imagePreview.height / 2f

        // Correct preview output to account for display rotation
        val rotationDegrees = when(imagePreview.display.rotation) {
            Surface.ROTATION_0 -> 0
            Surface.ROTATION_90 -> 90
            Surface.ROTATION_180 -> 180
            Surface.ROTATION_270 -> 270
            else -> return
        }
        //quay lại ảnh
        matrix.postRotate(-rotationDegrees.toFloat(), centerX, centerY)
        imagePreview.setTransform(matrix)
    }
    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        if (requestCode == REQUEST_CODE_PERMISSION) {
            if (checkAllPermissionGranted()) {
                binding.imageRealtime.post {
                    startCamera()
                }
            } else {
                Toast.makeText(this,
                    "Permissions not granted by the user.",
                    Toast.LENGTH_SHORT).show()
                finish()
            }
        }
    }
    private fun checkAllPermissionGranted() =
        REQUIRED_PERMISSIONS.all {
            ContextCompat.checkSelfPermission(
                baseContext,it
            ) == PackageManager.PERMISSION_GRANTED
        }

}
