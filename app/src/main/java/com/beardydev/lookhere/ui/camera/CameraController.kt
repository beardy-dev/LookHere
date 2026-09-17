package com.beardydev.lookhere.ui.camera

import android.content.ContentValues
import android.content.Context
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import java.text.SimpleDateFormat
import java.util.Locale
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

class CameraController(private val context: Context) {

    private var cameraProvider: ProcessCameraProvider? = null
    private var imageCapture: ImageCapture? = null

    suspend fun bind(
        lifecycleOwner: LifecycleOwner,
        previewView: PreviewView,
        cameraSelector: CameraSelector = CameraSelector.DEFAULT_BACK_CAMERA,
    ) {
        val provider = getCameraProvider()
        val preview = Preview.Builder().build().apply {
            surfaceProvider = previewView.surfaceProvider
        }
        val capture = ImageCapture.Builder().build()

        provider.unbindAll()
        provider.bindToLifecycle(lifecycleOwner, cameraSelector, preview, capture)

        cameraProvider = provider
        imageCapture = capture
    }

    fun unbind() {
        cameraProvider?.unbindAll()
        cameraProvider = null
        imageCapture = null
    }

    fun takePhoto(onSaved: (Uri) -> Unit, onError: (Throwable) -> Unit) {
        val capture = imageCapture
        if (capture == null) {
            onError(IllegalStateException("Camera is not bound yet"))
            return
        }

        val name = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(System.currentTimeMillis())
        val contentValues = ContentValues().apply {
            put(MediaStore.Images.Media.DISPLAY_NAME, "LookHere_$name")
            put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                // The standard camera roll, not a separate album -- so photos show up
                // immediately in the main Gallery/Photos view where users expect them.
                put(MediaStore.Images.Media.RELATIVE_PATH, "${Environment.DIRECTORY_DCIM}/Camera")
            }
        }

        val outputOptions = ImageCapture.OutputFileOptions.Builder(
            context.contentResolver,
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
            contentValues,
        ).build()

        capture.takePicture(
            outputOptions,
            ContextCompat.getMainExecutor(context),
            object : ImageCapture.OnImageSavedCallback {
                override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
                    outputFileResults.savedUri?.let(onSaved)
                }

                override fun onError(exception: ImageCaptureException) {
                    onError(exception)
                }
            },
        )
    }

    private suspend fun getCameraProvider(): ProcessCameraProvider = suspendCoroutine { continuation ->
        val future = ProcessCameraProvider.getInstance(context)
        future.addListener(
            { continuation.resume(future.get()) },
            ContextCompat.getMainExecutor(context),
        )
    }
}
