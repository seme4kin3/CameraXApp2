package com.example.cameraxapp2

import android.Manifest
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.annotation.RequiresApi
import android.os.Build
import android.os.Bundle
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import android.annotation.SuppressLint
import androidx.lifecycle.LifecycleOwner
import android.content.Intent
import android.content.ContentValues
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.Toast
import androidx.camera.core.*
import com.google.common.util.concurrent.ListenableFuture
import java.util.concurrent.ExecutionException
import java.util.concurrent.Executor

class MainActivity : AppCompatActivity(), ImageAnalysis.Analyzer, View.OnClickListener {
    private var cameraProviderFuture: ListenableFuture<ProcessCameraProvider>? = null
    var previewView: PreviewView? = null
    private lateinit var imageCapture: ImageCapture
    private lateinit var bSwitchToVideo: Button
    private lateinit var bSwitch: Button
    private lateinit var bCapture: Button
    private lateinit var bGallery: Button
    private var lensFacing = CameraSelector.DEFAULT_BACK_CAMERA
    @SuppressLint("MissingInflatedId")
    @RequiresApi(api = Build.VERSION_CODES.M)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        Log.d(LOG_TAG, "Запрашиваем разрешение")
        if (checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED ||
            ContextCompat.checkSelfPermission(
                this@MainActivity,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            ) != PackageManager.PERMISSION_GRANTED
            ||
            ContextCompat.checkSelfPermission(
                this@MainActivity,
                Manifest.permission.RECORD_AUDIO
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            requestPermissions(
                arrayOf(
                    Manifest.permission.CAMERA,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    Manifest.permission.RECORD_AUDIO
                ), 1
            )
        }
        bGallery = findViewById(R.id.btnGallery)
        previewView = findViewById(R.id.previewView)
        // здесь тоже замена активити
        bCapture = findViewById(R.id.btnTake)
        bSwitchToVideo = findViewById(R.id.btnSwitchToVideo)
        bCapture.setOnClickListener(this)
        bSwitchToVideo.setOnClickListener(this)
        bGallery.setOnClickListener(this)
        cameraProviderFuture = ProcessCameraProvider.getInstance(this)
        cameraProviderFuture!!.addListener({
            try {
                val cameraProvider = cameraProviderFuture!!.get()
                startCameraX(cameraProvider)
            } catch (e: ExecutionException) {
                e.printStackTrace()
            } catch (e: InterruptedException) {
                e.printStackTrace()
            }
        }, executor)
    }

    val executor: Executor
        get() = ContextCompat.getMainExecutor(this)

    @SuppressLint("RestrictedApi")
    private fun startCameraX(cameraProvider: ProcessCameraProvider) {
        val cameraSelector = CameraSelector.Builder()
            .requireLensFacing(CameraSelector.LENS_FACING_BACK)
            .build()
        val preview = Preview.Builder()
            .build()
        preview.setSurfaceProvider(previewView!!.surfaceProvider)

        // Image capture use case
        imageCapture = ImageCapture.Builder()
            .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
            .build()


        // Image analysis use case
        val imageAnalysis = ImageAnalysis.Builder()
            .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
            .build()
        imageAnalysis.setAnalyzer(executor, this)
        cameraProvider.bindToLifecycle(
            (this as LifecycleOwner),
            cameraSelector,
            preview,
            imageCapture
        )
        bSwitch = findViewById(R.id.switch_btn)
        bSwitch.setOnClickListener(View.OnClickListener { view: View? ->
            if (lensFacing == CameraSelector.DEFAULT_FRONT_CAMERA) lensFacing =
                CameraSelector.DEFAULT_BACK_CAMERA else if (lensFacing == CameraSelector.DEFAULT_BACK_CAMERA) lensFacing =
                CameraSelector.DEFAULT_FRONT_CAMERA
            cameraProvider.unbindAll()
            val preview1 = Preview.Builder()
                .build()
            preview1.setSurfaceProvider(previewView!!.surfaceProvider)

            // Image capture use case
            imageCapture = ImageCapture.Builder()
                .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
                .build()


            // Image analysis use case
            val imageAnalysis1 = ImageAnalysis.Builder()
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .build()
            imageAnalysis1.setAnalyzer(executor, this)
            cameraProvider.bindToLifecycle(
                (this as LifecycleOwner),
                lensFacing,
                preview1,
                imageCapture
            )
        })
    }

    override fun analyze(image: ImageProxy) {
        // image processing here for the current frame
        Log.d("TAG", "analyze: got the frame at: " + image.imageInfo.timestamp)
        image.close()
    }

    @SuppressLint("RestrictedApi")
    override fun onClick(view: View) {
        when (view.id) {
            R.id.btnTake -> capturePhoto()
            R.id.btnSwitchToVideo -> {
                val intent = Intent(this, VideoActivity::class.java)
                startActivity(intent)
            }
            R.id.btnGallery -> {
                val intent2 = Intent(this, GalleryActivity::class.java)
                startActivity(intent2)
            }
        }
    }

    private fun capturePhoto() {
        val timestamp = System.currentTimeMillis()
        val contentValues = ContentValues()
        contentValues.put(MediaStore.MediaColumns.DISPLAY_NAME, timestamp)
        contentValues.put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg")
        imageCapture!!.takePicture(
            ImageCapture.OutputFileOptions.Builder(
                contentResolver,
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                contentValues
            ).build(),
            executor,
            object : ImageCapture.OnImageSavedCallback {
                override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
                    Toast.makeText(
                        this@MainActivity,
                        "Photo has been saved successfully.",
                        Toast.LENGTH_SHORT
                    ).show()
                }

                override fun onError(exception: ImageCaptureException) {
                    Toast.makeText(
                        this@MainActivity,
                        "Error saving photo: " + exception.message,
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        )
    }

    companion object {
        private const val LOG_TAG = "myLogs"
    }
}