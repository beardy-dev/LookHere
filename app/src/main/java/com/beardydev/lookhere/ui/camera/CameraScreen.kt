package com.beardydev.lookhere.ui.camera

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.net.Uri
import android.util.Log
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.view.PreviewView
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Camera
import androidx.compose.material.icons.filled.FiberManualRecord
import androidx.compose.material.icons.filled.FlipCameraAndroid
import androidx.compose.material.icons.filled.GifBox
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.SwapHorizontalCircle
import androidx.compose.material.icons.filled.SwapVerticalCircle
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.FilledIconToggleButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.beardydev.lookhere.LookHereApp
import com.beardydev.lookhere.domain.model.SelectedGif
import com.beardydev.lookhere.display.RearDisplayGifController
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import java.io.File
import kotlinx.coroutines.launch

sealed interface CapturedMedia {
    data class Photo(val uri: Uri) : CapturedMedia
    data class Video(val uri: Uri) : CapturedMedia
}

@Composable
fun CameraScreen(
    onChangeGifRequested: () -> Unit,
    rearDisplayGifController: RearDisplayGifController,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val scope = rememberCoroutineScope()
    val cameraController = remember { CameraController(context) }
    val container = remember { (context.applicationContext as LookHereApp).container }

    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) ==
                PackageManager.PERMISSION_GRANTED
        )
    }
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted -> hasCameraPermission = granted }

    LaunchedEffect(Unit) {
        if (!hasCameraPermission) permissionLauncher.launch(Manifest.permission.CAMERA)
    }

    if (!hasCameraPermission) {
        Box(modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("Camera permission is required to take photos.")
                Button(onClick = { permissionLauncher.launch(Manifest.permission.CAMERA) }) {
                    Text("Grant permission")
                }
            }
        }
        return
    }

    var hasAudioPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) ==
                PackageManager.PERMISSION_GRANTED
        )
    }
    // RECORD_AUDIO is requested lazily -- only the first time the user switches into
    // video mode, not upfront alongside CAMERA -- to match this app's minimal-permissions
    // stance. Guarded so re-toggling video mode doesn't re-prompt after the first ask.
    var hasRequestedAudioPermission by remember { mutableStateOf(false) }
    val audioPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted -> hasAudioPermission = granted }

    val previewView = remember {
        PreviewView(context).apply {
            // Default PERFORMANCE mode prefers a SurfaceView, which composites via
            // its own hardware surface outside the normal View drawing/clipping
            // pipeline -- it can render outside its logical bounds when placed in
            // a more complex layout like this split-pane one. COMPATIBLE forces a
            // TextureView, which is an ordinary hardware-accelerated View and
            // correctly respects the bounds Compose assigns it.
            implementationMode = PreviewView.ImplementationMode.COMPATIBLE
        }
    }
    var lastMedia by remember { mutableStateOf<CapturedMedia?>(null) }
    var isVideoMode by remember { mutableStateOf(false) }
    var videoAvailable by remember { mutableStateOf(false) }
    val recordingState by cameraController.recordingState.collectAsStateWithLifecycle()
    // Default to selfie mode when there's no second screen to show the GIF on right
    // now -- folded shut (only the cover screen visible) or a plain single-display
    // phone. Only decided once per fresh entry to this screen; the user can still
    // flip the camera manually afterward.
    var useFrontCamera by remember { mutableStateOf(!rearDisplayGifController.isDualDisplayAvailable.value) }
    var gifFirst by remember { mutableStateOf(true) }
    val selectedGif by container.selectedGifRepository.selectedGif.collectAsStateWithLifecycle(initialValue = null)
    val isLandscape = LocalConfiguration.current.orientation == Configuration.ORIENTATION_LANDSCAPE

    // Live reaction to folding shut mid-session: if the second screen disappears
    // while we're in rear-camera mode, there's no one left to see the cover-screen
    // GIF, so drop straight into selfie mode. One-directional on purpose -- we
    // don't auto-switch back to rear mode on unfolding, in case the user picked
    // selfie mode deliberately for another reason.
    val isDualDisplayAvailable by rearDisplayGifController.isDualDisplayAvailable.collectAsStateWithLifecycle()
    LaunchedEffect(isDualDisplayAvailable) {
        if (!isDualDisplayAvailable && !useFrontCamera) {
            useFrontCamera = true
        }
    }

    DisposableEffect(lifecycleOwner, useFrontCamera) {
        val selector = if (useFrontCamera) CameraSelector.DEFAULT_FRONT_CAMERA else CameraSelector.DEFAULT_BACK_CAMERA
        scope.launch {
            cameraController.bind(lifecycleOwner, previewView, selector)
            videoAvailable = cameraController.videoAvailable
        }
        // A camera flip -- manual, or the automatic fold-triggered fallback below --
        // tears down the bound session via unbind(), which itself stops any in-progress
        // recording first so the file finalizes as a valid (if short) clip instead of
        // being corrupted/truncated.
        onDispose { cameraController.unbind() }
    }

    // Selfie mode shows the GIF in-frame instead of on the cover screen (there's no
    // one else to show that to). Un-suppress again on leaving this screen entirely
    // so it doesn't stay stuck off if we navigate away mid-selfie-mode.
    DisposableEffect(useFrontCamera) {
        rearDisplayGifController.setSuppressed(useFrontCamera)
        onDispose { }
    }
    DisposableEffect(Unit) {
        onDispose { rearDisplayGifController.setSuppressed(false) }
    }

    Box(modifier = modifier.fillMaxSize()) {
        when {
            // Keyed the same as the viewfinder inside SplitPane below: this shares
            // a single remembered PreviewView between two different structural
            // positions (this fullscreen branch vs. inside SplitPane), so without
            // matching keys Compose could try to attach it to the new spot before
            // detaching it from the old one -- "the specified child already has a
            // parent."
            !useFrontCamera -> key("camera_preview") {
                AndroidView(factory = { previewView }, modifier = Modifier.fillMaxSize())
            }

            // Selfie mode: there's no one else to show the cover screen to, so the
            // GIF plays in-frame alongside the viewfinder instead -- side by side in
            // landscape, stacked in portrait, whichever pane comes first per gifFirst.
            else -> SplitPane(
                isLandscape = isLandscape,
                gifFirst = gifFirst,
                gifContent = { SelfieGifPane(gif = selectedGif, modifier = Modifier) },
                viewfinderContent = {
                    key("camera_preview") {
                        AndroidView(factory = { previewView }, modifier = Modifier)
                    }
                },
                modifier = Modifier.fillMaxSize(),
            )
        }

        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(24.dp),
        ) {
            lastMedia?.let { media ->
                MediaThumbnail(
                    media = media,
                    modifier = Modifier
                        .align(Alignment.CenterStart)
                        .size(56.dp)
                        .clickable {
                            val uri = when (media) {
                                is CapturedMedia.Photo -> media.uri
                                is CapturedMedia.Video -> media.uri
                            }
                            val mimeType = when (media) {
                                is CapturedMedia.Photo -> "image/*"
                                is CapturedMedia.Video -> "video/*"
                            }
                            val intent = Intent(Intent.ACTION_VIEW).apply {
                                setDataAndType(uri, mimeType)
                                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                            }
                            context.startActivity(intent)
                        },
                )
            }

            FilledIconButton(
                onClick = {
                    when {
                        !isVideoMode -> cameraController.takePhoto(
                            onSaved = { uri ->
                                lastMedia = CapturedMedia.Photo(uri)
                                Toast.makeText(context, "Photo saved!", Toast.LENGTH_SHORT).show()
                            },
                            onError = { error ->
                                Log.e("LookHereCamera", "Failed to save photo", error)
                                Toast.makeText(context, "Couldn't save photo: ${error.message}", Toast.LENGTH_LONG).show()
                            },
                        )

                        recordingState == RecordingState.Idle -> cameraController.startRecording(
                            recordAudio = hasAudioPermission,
                            onSaved = { uri ->
                                lastMedia = CapturedMedia.Video(uri)
                                Toast.makeText(context, "Video saved!", Toast.LENGTH_SHORT).show()
                            },
                            onError = { error ->
                                Log.e("LookHereCamera", "Failed to save video", error)
                                Toast.makeText(context, "Couldn't save video: ${error.message}", Toast.LENGTH_LONG).show()
                            },
                        )

                        else -> cameraController.stopRecording()
                    }
                },
                colors = if (isVideoMode && recordingState != RecordingState.Idle) {
                    IconButtonDefaults.filledIconButtonColors(containerColor = MaterialTheme.colorScheme.error)
                } else {
                    IconButtonDefaults.filledIconButtonColors()
                },
                modifier = Modifier.align(Alignment.Center).size(72.dp),
            ) {
                Icon(
                    imageVector = when {
                        !isVideoMode -> Icons.Filled.Camera
                        recordingState == RecordingState.Idle -> Icons.Filled.FiberManualRecord
                        else -> Icons.Filled.Stop
                    },
                    contentDescription = if (!isVideoMode) "Take photo" else if (recordingState == RecordingState.Idle) "Start recording" else "Stop recording",
                    modifier = Modifier.size(36.dp),
                )
            }

            Row(
                modifier = Modifier.align(Alignment.CenterEnd),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                if (useFrontCamera) {
                    FilledIconButton(
                        onClick = { gifFirst = !gifFirst },
                        enabled = recordingState == RecordingState.Idle,
                        modifier = Modifier.size(48.dp),
                    ) {
                        Icon(
                            imageVector = if (isLandscape) Icons.Filled.SwapHorizontalCircle else Icons.Filled.SwapVerticalCircle,
                            contentDescription = if (isLandscape) "Swap sides" else "Swap top/bottom",
                        )
                    }
                }
                FilledIconButton(
                    onClick = { useFrontCamera = !useFrontCamera },
                    enabled = recordingState == RecordingState.Idle,
                    modifier = Modifier.size(48.dp),
                ) {
                    Icon(
                        imageVector = Icons.Filled.FlipCameraAndroid,
                        contentDescription = if (useFrontCamera) "Switch to rear camera" else "Switch to front camera",
                    )
                }
            }
        }

        if (recordingState is RecordingState.InProgress) {
            Text(
                text = formatDuration((recordingState as RecordingState.InProgress).durationMillis),
                color = MaterialTheme.colorScheme.onError,
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .statusBarsPadding()
                    .padding(16.dp)
                    .background(MaterialTheme.colorScheme.error, RoundedCornerShape(8.dp))
                    .padding(horizontal = 12.dp, vertical = 4.dp),
            )
        }

        Row(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .statusBarsPadding()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            FilledIconToggleButton(
                checked = isVideoMode,
                onCheckedChange = { checked ->
                    if (checked && !hasRequestedAudioPermission) {
                        audioPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                        hasRequestedAudioPermission = true
                    }
                    isVideoMode = checked
                },
                enabled = videoAvailable,
            ) {
                Icon(
                    imageVector = if (isVideoMode) Icons.Filled.Videocam else Icons.Filled.PhotoCamera,
                    contentDescription = if (isVideoMode) "Switch to photo mode" else "Switch to video mode",
                )
            }
            FilledIconButton(onClick = onChangeGifRequested) {
                Icon(
                    imageVector = Icons.Filled.GifBox,
                    contentDescription = "Change GIF",
                )
            }
        }
    }
}

private fun formatDuration(durationMillis: Long): String {
    val totalSeconds = durationMillis / 1000
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return "%02d:%02d".format(minutes, seconds)
}

/**
 * Places [gifContent] and [viewfinderContent] into exactly half the available
 * space each -- side by side in landscape, stacked in portrait -- with
 * [gifFirst] choosing which comes first on screen.
 *
 * Both children are always composed, in the same fixed order, on every call:
 * only their placement *coordinates* change here, never their position in
 * the composition tree. That matters because viewfinderContent wraps a
 * single shared, remembered PreviewView -- an earlier version of this let
 * gifFirst change which composable (and which parent) hosted that PreviewView
 * (via Row/Column + key()), which tore it down from one parent and
 * re-attached it to another. That either crashed ("the specified child
 * already has a parent") or, when it didn't crash, threw off the weight-based
 * 50/50 split depending on timing. A custom Layout sidesteps both: the
 * PreviewView's AndroidView is composed in the exact same spot every time,
 * and swapping gifFirst only swaps which measured Placeable gets placed at
 * which offset.
 */
@Composable
private fun SplitPane(
    isLandscape: Boolean,
    gifFirst: Boolean,
    gifContent: @Composable () -> Unit,
    viewfinderContent: @Composable () -> Unit,
    modifier: Modifier = Modifier,
) {
    Layout(
        content = {
            // Compose doesn't clip a child's drawing to its layout bounds by
            // default -- measuring/placing each pane at exactly half (below) only
            // controls where Compose *thinks* each pane is; it doesn't stop the
            // camera preview from painting beyond that box if its own internal
            // rendering wants to. clipToBounds() enforces the boundary regardless
            // of what either pane tries to draw internally.
            //
            // propagateMinConstraints = true matters here: Box normally relaxes
            // its own minimum constraint before passing constraints to its child
            // (so a plain Box(Modifier.clipToBounds()) below would hand
            // SelfieGifPane a *loose* 0..halfSize range instead of the exact
            // fixed size measured just below). SelfieGifPane waits for a nonzero
            // measured size before rendering anything, so on the first pass --
            // with no content yet -- a loosely-constrained Box collapses to 0x0,
            // and since its size started at 0x0 too, that "new" 0x0 never counts
            // as a change, so it never gets a second chance to measure correctly.
            Box(Modifier.clipToBounds(), propagateMinConstraints = true) { gifContent() }
            Box(Modifier.clipToBounds(), propagateMinConstraints = true) { viewfinderContent() }
        },
        modifier = modifier,
    ) { measurables, constraints ->
        val (gifMeasurable, viewfinderMeasurable) = measurables
        val width = constraints.maxWidth
        val height = constraints.maxHeight

        if (isLandscape) {
            val halfWidth = width / 2
            val paneConstraints = Constraints.fixed(halfWidth, height)
            val gifPlaceable = gifMeasurable.measure(paneConstraints)
            val viewfinderPlaceable = viewfinderMeasurable.measure(paneConstraints)
            layout(width, height) {
                if (gifFirst) {
                    gifPlaceable.placeRelative(0, 0)
                    viewfinderPlaceable.placeRelative(halfWidth, 0)
                } else {
                    viewfinderPlaceable.placeRelative(0, 0)
                    gifPlaceable.placeRelative(halfWidth, 0)
                }
            }
        } else {
            val halfHeight = height / 2
            val paneConstraints = Constraints.fixed(width, halfHeight)
            val gifPlaceable = gifMeasurable.measure(paneConstraints)
            val viewfinderPlaceable = viewfinderMeasurable.measure(paneConstraints)
            layout(width, height) {
                if (gifFirst) {
                    gifPlaceable.placeRelative(0, 0)
                    viewfinderPlaceable.placeRelative(0, halfHeight)
                } else {
                    viewfinderPlaceable.placeRelative(0, 0)
                    gifPlaceable.placeRelative(0, halfHeight)
                }
            }
        }
    }
}

@Composable
private fun SelfieGifPane(gif: SelectedGif?, modifier: Modifier = Modifier) {
    // Track the pane's actual laid-out pixel size and pass it to Glide explicitly via
    // .override(...) instead of letting Glide auto-detect the ImageView's size. In this
    // weighted Row/Column layout the pane can settle into its final size a beat after
    // the AndroidView is first created (front/rear toggle, orientation change), and
    // Glide's own size detection can grab an earlier/wrong size and bake it into the
    // fitCenter-transformed frames -- that's what was showing as GIFs cut off.
    var paneSize by remember { mutableStateOf(IntSize.Zero) }

    Box(
        modifier = modifier
            .background(Color.Black)
            .onSizeChanged { paneSize = it },
    ) {
        if (gif != null && paneSize.width > 0 && paneSize.height > 0) {
            // Keyed on the gif itself: forces a brand-new ImageView (and Glide load)
            // whenever the selected gif changes, rather than relying on Glide/View
            // reuse to swap the animated content, which was showing the previous
            // GIF still playing after a change.
            key(gif) {
                AndroidView(
                    modifier = Modifier.fillMaxSize(),
                    factory = { context ->
                        ImageView(context).apply { scaleType = ImageView.ScaleType.FIT_CENTER }
                    },
                    update = { imageView ->
                        val requestOptions = RequestOptions()
                            .fitCenter()
                            .override(paneSize.width, paneSize.height)
                        when (gif) {
                            is SelectedGif.FromSearch ->
                                Glide.with(imageView).asGif().load(gif.fullUrl).apply(requestOptions).into(imageView)

                            is SelectedGif.FromDevice ->
                                Glide.with(imageView).asGif().load(File(gif.filePath)).apply(requestOptions).into(imageView)
                        }
                    },
                    // Compose disposes this View outright every time key(gif) changes (a
                    // fresh ImageView per gif, by design -- see comment above), but never
                    // tells Glide about it: Glide's request is tied to the Activity-level
                    // RequestManager, not this View's own lifecycle, so without an explicit
                    // clear() the discarded view's request/target is leaked and keeps
                    // decoding frames in the background. (Not the cause of the "previous GIF
                    // still showing" bug that was here -- that turned out to be a real race in
                    // LookHereRoot.onGifPicked navigating before the DataStore save committed,
                    // now fixed there. This clear() is still worth keeping to avoid the leak.)
                    onRelease = { imageView -> Glide.with(imageView).clear(imageView) },
                )
            }
        }
    }
}

@Composable
private fun MediaThumbnail(media: CapturedMedia, modifier: Modifier = Modifier) {
    val uri = when (media) {
        is CapturedMedia.Photo -> media.uri
        is CapturedMedia.Video -> media.uri
    }
    AndroidView(
        modifier = modifier,
        factory = { context ->
            ImageView(context).apply { scaleType = ImageView.ScaleType.CENTER_CROP }
        },
        // Glide resolves a video Uri to a decoded frame thumbnail the same way it
        // resolves an image Uri to a bitmap -- no separate thumbnail-generation path
        // needed for CapturedMedia.Video.
        update = { imageView -> Glide.with(imageView).load(uri).into(imageView) },
        onRelease = { imageView -> Glide.with(imageView).clear(imageView) },
    )
}
