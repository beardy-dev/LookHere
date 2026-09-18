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
import androidx.camera.video.FallbackStrategy
import androidx.camera.video.MediaStoreOutputOptions
import androidx.camera.video.Quality
import androidx.camera.video.QualitySelector
import androidx.camera.video.Recorder
import androidx.camera.video.Recording
import androidx.camera.video.VideoCapture
import androidx.camera.video.VideoRecordEvent
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import java.text.SimpleDateFormat
import java.util.Locale
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

sealed interface RecordingState {
    data object Idle : RecordingState
    data class InProgress(val durationMillis: Long) : RecordingState
}

class CameraController(private val context: Context) {

    private var cameraProvider: ProcessCameraProvider? = null
    private var imageCapture: ImageCapture? = null
    private var videoCapture: VideoCapture<Recorder>? = null
    private var activeRecording: Recording? = null

    var videoAvailable: Boolean = false
        private set

    private val _recordingState = MutableStateFlow<RecordingState>(RecordingState.Idle)
    val recordingState: StateFlow<RecordingState> = _recordingState.asStateFlow()

    // 1080p by default -- this is a casual/kids'-photo app where storage matters
    // more than pro-grade fidelity, not Quality.HIGHEST, which on the Z Fold's
    // sensor could mean multi-GB files for a few minutes of clip. The fallback
    // list only ever steps *down* from FHD, never silently up past it.
    private val recorder = Recorder.Builder()
        .setQualitySelector(
            QualitySelector.fromOrderedList(
                listOf(Quality.FHD, Quality.HD, Quality.SD),
                FallbackStrategy.lowerQualityOrHigherThan(Quality.FHD),
            )
        )
        .build()

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
        val video = VideoCapture.withOutput(recorder)
        // previewView.display can be null this early (not attached to a window yet),
        // so read the current rotation off the context instead of the view.
        val currentRotation = ContextCompat.getDisplayOrDefault(context).rotation
        capture.targetRotation = currentRotation
        video.targetRotation = currentRotation

        provider.unbindAll()
        // Concurrent Preview+ImageCapture+VideoCapture can fail on lower camera
        // hardware levels (bindToLifecycle throws IllegalArgumentException for
        // unsupported combinations). Fall back to today's photo-only bind rather
        // than assume every device supports it.
        videoAvailable = try {
            provider.bindToLifecycle(lifecycleOwner, cameraSelector, preview, capture, video)
            true
        } catch (e: IllegalArgumentException) {
            provider.bindToLifecycle(lifecycleOwner, cameraSelector, preview, capture)
            false
        }

        cameraProvider = provider
        imageCapture = capture
        videoCapture = if (videoAvailable) video else null
    }

    // ImageCapture/VideoCapture only pick up the display rotation that was current at
    // bind() time -- they don't track it afterward on their own. Since this screen keeps
    // the same bound session across orientation changes (no rebind on rotate, only on a
    // front/rear flip), the caller needs to push rotation updates here as the device
    // turns, or a photo/video shot after rotating without flipping cameras saves rotated
    // wrong. Takes a Surface.ROTATION_* constant.
    fun setTargetRotation(rotation: Int) {
        imageCapture?.targetRotation = rotation
        videoCapture?.targetRotation = rotation
    }

    fun unbind() {
        // Recording.stop() requests finalization before unbindAll() tears down the
        // session, so VideoRecordEvent.Finalize still fires with whatever was
        // captured -- a valid, playable (if short) file -- rather than a flip (manual,
        // or the automatic fold-triggered one in CameraScreen.kt) corrupting/truncating it.
        activeRecording?.stop()
        activeRecording = null
        cameraProvider?.unbindAll()
        cameraProvider = null
        imageCapture = null
        videoCapture = null
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

    fun startRecording(recordAudio: Boolean, onSaved: (Uri) -> Unit, onError: (Throwable) -> Unit) {
        val video = videoCapture
        if (video == null) {
            onError(IllegalStateException("Video capture is not available"))
            return
        }
        if (activeRecording != null) return // ignore duplicate taps

        val name = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(System.currentTimeMillis())
        val contentValues = ContentValues().apply {
            put(MediaStore.Video.Media.DISPLAY_NAME, "LookHere_$name")
            put(MediaStore.Video.Media.MIME_TYPE, "video/mp4")
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                put(MediaStore.Video.Media.RELATIVE_PATH, "${Environment.DIRECTORY_DCIM}/Camera")
            }
        }
        val outputOptions = MediaStoreOutputOptions.Builder(context.contentResolver, MediaStore.Video.Media.EXTERNAL_CONTENT_URI)
            .setContentValues(contentValues)
            .build()

        var pending = video.output.prepareRecording(context, outputOptions)
        if (recordAudio) pending = pending.withAudioEnabled()

        activeRecording = pending.start(ContextCompat.getMainExecutor(context)) { event ->
            when (event) {
                is VideoRecordEvent.Status ->
                    _recordingState.value = RecordingState.InProgress(event.recordingStats.recordedDurationNanos / 1_000_000)

                is VideoRecordEvent.Finalize -> {
                    activeRecording = null
                    _recordingState.value = RecordingState.Idle
                    if (!event.hasError()) onSaved(event.outputResults.outputUri)
                    else onError(RuntimeException("Video recording error code ${event.error}"))
                }

                else -> Unit
            }
        }
    }

    fun stopRecording() {
        activeRecording?.stop()
        activeRecording = null
    }

    private suspend fun getCameraProvider(): ProcessCameraProvider = suspendCoroutine { continuation ->
        val future = ProcessCameraProvider.getInstance(context)
        future.addListener(
            { continuation.resume(future.get()) },
            ContextCompat.getMainExecutor(context),
        )
    }
}
