package com.example.cameraxapp2

import android.Manifest
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.annotation.RequiresApi
import android.os.Build
import android.os.Bundle
import androidx.core.content.ContextCompat
import android.annotation.SuppressLint
import androidx.lifecycle.LifecycleOwner
import android.content.Intent
import android.content.ContentValues
import android.provider.MediaStore
import androidx.core.app.ActivityCompat
import android.content.pm.PackageManager
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.Toast
import androidx.camera.core.*
import com.google.common.util.concurrent.ListenableFuture
import java.lang.Exception
import java.util.concurrent.ExecutionException
import java.util.concurrent.Executor

class VideoActivity : AppCompatActivity(), ImageAnalysis.Analyzer, View.OnClickListener {
    private var cameraProviderFuture: ListenableFuture<ProcessCameraProvider>? = null
    var previewView: PreviewView? = null
    private var videoCapture: VideoCapture? = null
    private lateinit var bRecord: Button
    private lateinit var bSwitch: Button
    private lateinit var bSwitchToPhot: Button
    private lateinit var bGallery: Button
    private var lensFacing = CameraSelector.DEFAULT_BACK_CAMERA
    @RequiresApi(api = Build.VERSION_CODES.M)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_video)
        Log.d(LOG_TAG, "Запрашиваем разрешение")
        previewView = findViewById(R.id.previewView)
        bRecord = findViewById(R.id.btnRecordVideo)
        bSwitch = findViewById(R.id.switch_btn_video)
        bSwitchToPhot = findViewById(R.id.btnSwitchToPhoto)
        bGallery = findViewById(R.id.btnGallery)
        bRecord.setText("record")
        bSwitchToPhot.setOnClickListener(this)
        bGallery.setOnClickListener(this)
        bRecord.setOnClickListener(this)


        // bRecord.setOnClickListener(this);
        // bSwitch.setOnClickListener(this);
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


        // Video capture use case
        videoCapture = VideoCapture.Builder()
            .setVideoFrameRate(30)
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
            videoCapture
        )
        bSwitch = findViewById(R.id.switch_btn_video)
        bSwitch.setOnClickListener(View.OnClickListener { view: View? ->
            if (lensFacing == CameraSelector.DEFAULT_FRONT_CAMERA) lensFacing =
                CameraSelector.DEFAULT_BACK_CAMERA else if (lensFacing == CameraSelector.DEFAULT_BACK_CAMERA) lensFacing =
                CameraSelector.DEFAULT_FRONT_CAMERA
            cameraProvider.unbindAll()
            val preview1 = Preview.Builder()
                .build()
            preview1.setSurfaceProvider(previewView!!.surfaceProvider)


            // Video capture use case
            videoCapture = VideoCapture.Builder()
                .setVideoFrameRate(30)
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
                videoCapture
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
            R.id.btnRecordVideo -> if (bRecord!!.text === "record") {
                bRecord!!.text = "stop recording"
                recordVideo()
            } else {
                bRecord!!.text = "record"
                videoCapture!!.stopRecording()
            }
            R.id.btnSwitchToPhoto -> {
                val intent = Intent(this, MainActivity::class.java)
                startActivity(intent)
            }
            R.id.btnGallery -> {
                val intent2 = Intent(this, GalleryActivity::class.java)
                startActivity(intent2)
            }
        }
    }

    @SuppressLint("RestrictedApi")
    private fun recordVideo() {
        if (videoCapture != null) {
            val timestamp = System.currentTimeMillis()
            val contentValues = ContentValues()
            contentValues.put(MediaStore.MediaColumns.DISPLAY_NAME, timestamp)
            contentValues.put(MediaStore.MediaColumns.MIME_TYPE, "video/mp4")
            try {
                if (ActivityCompat.checkSelfPermission(
                        this,
                        Manifest.permission.RECORD_AUDIO
                    ) != PackageManager.PERMISSION_GRANTED
                ) {
                    return
                }
                videoCapture!!.startRecording(
                    VideoCapture.OutputFileOptions.Builder(
                        contentResolver,
                        MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
                        contentValues
                    ).build(),
                    executor,
                    object : VideoCapture.OnVideoSavedCallback {
                        override fun onVideoSaved(outputFileResults: VideoCapture.OutputFileResults) {
                            Toast.makeText(
                                this@VideoActivity,
                                "Video has been saved successfully.",
                                Toast.LENGTH_SHORT
                            ).show()
                        }

                        override fun onError(
                            videoCaptureError: Int,
                            message: String,
                            cause: Throwable?
                        ) {
                            Toast.makeText(
                                this@VideoActivity,
                                "Error saving video: $message",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                )
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    companion object {
        private const val LOG_TAG = "myLogs"
    }
}