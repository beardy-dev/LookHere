package com.beardydev.lookhere.ui.navigation

import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.beardydev.lookhere.LookHereApp
import com.beardydev.lookhere.di.gifSearchViewModelFactory
import com.beardydev.lookhere.display.RearDisplayGifController
import com.beardydev.lookhere.ui.camera.CameraScreen
import com.beardydev.lookhere.ui.gifsearch.GifSearchScreen
import com.beardydev.lookhere.ui.gifsearch.GifSearchViewModel
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

@Composable
fun LookHereRoot() {
    val context = LocalContext.current
    val container = remember { (context.applicationContext as LookHereApp).container }
    val scope = rememberCoroutineScope()

    // Owned here, not by CameraScreen: the dual-display session must survive
    // Camera <-> Search navigation (Change GIF), or every change forces a full
    // teardown+rebuild of the OS-level dual-display transition, which is slow
    // and flaky enough that a GIF change can silently appear to do nothing on
    // the first try.
    val rearDisplayGifController = remember {
        RearDisplayGifController(context as ComponentActivity, container.observeCoverScreenGif)
    }
    DisposableEffect(Unit) {
        rearDisplayGifController.start(scope)
        onDispose { rearDisplayGifController.stop() }
    }

    val gifLoadState by remember(container) {
        container.selectedGifRepository.selectedGif.map { GifLoadState.Loaded(it) as GifLoadState }
    }.collectAsStateWithLifecycle(initialValue = GifLoadState.Loading)

    when (val state = gifLoadState) {
        is GifLoadState.Loading -> {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        }

        is GifLoadState.Loaded -> {
            // Always land on the GIF picker at launch, even if a GIF is already
            // selected from a previous session -- lets the user confirm or swap
            // it before shooting instead of jumping straight into the camera.
            // canCancel still lets them back out to Camera and keep that
            // previous GIF if they don't want to pick a new one.
            var screen by remember { mutableStateOf<Screen>(Screen.Search) }
            val canCancel = state.gif != null

            when (screen) {
                Screen.Search -> {
                    val viewModel = viewModel<GifSearchViewModel>(
                        factory = container.gifSearchViewModelFactory(),
                    )
                    GifSearchScreen(
                        viewModel = viewModel,
                        canCancel = canCancel,
                        onGifPicked = { picked ->
                            scope.launch { container.selectGif(picked) }
                            screen = Screen.Camera
                        },
                        onCancel = { screen = Screen.Camera },
                    )
                }

                Screen.Camera -> {
                    CameraScreen(
                        onChangeGifRequested = { screen = Screen.Search },
                        rearDisplayGifController = rearDisplayGifController,
                    )
                }
            }
        }
    }
}
