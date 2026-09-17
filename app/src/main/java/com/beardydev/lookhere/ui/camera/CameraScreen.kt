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
import androidx.compose.material.icons.filled.FlipCameraAndroid
import androidx.compose.material.icons.filled.GifBox
import androidx.compose.material.icons.filled.SwapHorizontalCircle
import androidx.compose.material.icons.filled.SwapVerticalCircle
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
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

    val previewView = remember { PreviewView(context) }
    var lastPhotoUri by remember { mutableStateOf<Uri?>(null) }
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
        scope.launch { cameraController.bind(lifecycleOwner, previewView, selector) }
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

    // Keyed so Compose treats these as the same node across every reorder (front/rear
    // camera, gifFirst, orientation) instead of disposing and recreating them -- the
    // viewfinder in particular wraps a native PreviewView that must not be torn down
    // and re-parented just because its position in the layout changed.
    val gifPane = @Composable { paneModifier: Modifier ->
        key("gif_pane") {
            SelfieGifPane(gif = selectedGif, modifier = paneModifier)
        }
    }
    val viewfinderPane = @Composable { paneModifier: Modifier ->
        key("camera_preview") {
            AndroidView(factory = { previewView }, modifier = paneModifier)
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        when {
            !useFrontCamera -> viewfinderPane(Modifier.fillMaxSize())

            // Selfie mode: there's no one else to show the cover screen to, so the
            // GIF plays in-frame alongside the viewfinder instead -- side by side in
            // landscape, stacked in portrait, whichever pane comes first per gifFirst.
            isLandscape && gifFirst -> Row(Modifier.fillMaxSize()) {
                gifPane(Modifier.weight(1f).fillMaxHeight())
                viewfinderPane(Modifier.weight(1f).fillMaxHeight())
            }

            isLandscape -> Row(Modifier.fillMaxSize()) {
                viewfinderPane(Modifier.weight(1f).fillMaxHeight())
                gifPane(Modifier.weight(1f).fillMaxHeight())
            }

            gifFirst -> Column(Modifier.fillMaxSize()) {
                gifPane(Modifier.weight(1f).fillMaxWidth())
                viewfinderPane(Modifier.weight(1f).fillMaxWidth())
            }

            else -> Column(Modifier.fillMaxSize()) {
                viewfinderPane(Modifier.weight(1f).fillMaxWidth())
                gifPane(Modifier.weight(1f).fillMaxWidth())
            }
        }

        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(24.dp),
        ) {
            lastPhotoUri?.let { uri ->
                PhotoThumbnail(
                    uri = uri,
                    modifier = Modifier
                        .align(Alignment.CenterStart)
                        .size(56.dp)
                        .clickable {
                            val intent = Intent(Intent.ACTION_VIEW).apply {
                                setDataAndType(uri, "image/*")
                                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                            }
                            context.startActivity(intent)
                        },
                )
            }

            FilledIconButton(
                onClick = {
                    cameraController.takePhoto(
                        onSaved = { uri ->
                            lastPhotoUri = uri
                            Toast.makeText(context, "Photo saved!", Toast.LENGTH_SHORT).show()
                        },
                        onError = { error ->
                            Log.e("LookHereCamera", "Failed to save photo", error)
                            Toast.makeText(context, "Couldn't save photo: ${error.message}", Toast.LENGTH_LONG).show()
                        },
                    )
                },
                modifier = Modifier.align(Alignment.Center).size(72.dp),
            ) {
                Icon(
                    imageVector = Icons.Filled.Camera,
                    contentDescription = "Take photo",
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
                    modifier = Modifier.size(48.dp),
                ) {
                    Icon(
                        imageVector = Icons.Filled.FlipCameraAndroid,
                        contentDescription = if (useFrontCamera) "Switch to rear camera" else "Switch to front camera",
                    )
                }
            }
        }

        Row(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .statusBarsPadding()
                .padding(16.dp),
        ) {
            FilledIconButton(onClick = onChangeGifRequested) {
                Icon(
                    imageVector = Icons.Filled.GifBox,
                    contentDescription = "Change GIF",
                )
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
                    // decoding frames in the background.
                    onRelease = { imageView -> Glide.with(imageView).clear(imageView) },
                )
            }
        }
    }
}

@Composable
private fun PhotoThumbnail(uri: Uri, modifier: Modifier = Modifier) {
    AndroidView(
        modifier = modifier,
        factory = { context ->
            ImageView(context).apply { scaleType = ImageView.ScaleType.CENTER_CROP }
        },
        update = { imageView -> Glide.with(imageView).load(uri).into(imageView) },
        onRelease = { imageView -> Glide.with(imageView).clear(imageView) },
    )
}
