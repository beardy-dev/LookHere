package com.beardydev.lookhere.ui.gifsearch

import android.net.Uri
import android.widget.ImageView
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.beardydev.lookhere.data.settings.SelectedGif
import com.beardydev.lookhere.data.settings.stableKey
import com.bumptech.glide.Glide
import java.io.File

@Composable
fun GifSearchScreen(
    viewModel: GifSearchViewModel,
    canCancel: Boolean,
    onGifPicked: (SelectedGif) -> Unit,
    onCancel: () -> Unit,
    modifier: Modifier = Modifier,
) {
    BackHandler(enabled = canCancel) { onCancel() }

    var query by rememberSaveable { mutableStateOf("") }
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val launchUpload = rememberGifUploadLauncher { filePath ->
        onGifPicked(SelectedGif.FromDevice(filePath))
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .statusBarsPadding()
            .padding(horizontal = 16.dp)
            .padding(top = 24.dp, bottom = 16.dp)
    ) {
        OutlinedTextField(
            value = query,
            onValueChange = {
                query = it
                viewModel.onQueryChange(it)
            },
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text("Search KLIPY") },
            singleLine = true,
        )
        Spacer(Modifier.height(12.dp))
        Button(onClick = launchUpload, modifier = Modifier.fillMaxWidth()) {
            Text("Upload a GIF from your phone")
        }
        Spacer(Modifier.height(12.dp))
        when (val state = uiState) {
            is GifSearchUiState.Loading ->
                Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }

            is GifSearchUiState.Error -> Text(state.message)

            is GifSearchUiState.Recents -> {
                Text("Recent", style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.height(8.dp))
                LazyVerticalGrid(
                    columns = GridCells.Adaptive(minSize = 90.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    items(state.items, key = { it.stableKey }) { recentGif ->
                        GifThumbnail(
                            model = recentGif.previewModel(),
                            modifier = Modifier
                                .aspectRatio(1f)
                                .clickable { onGifPicked(recentGif) },
                        )
                    }
                }
            }

            is GifSearchUiState.Success ->
                LazyVerticalGrid(
                    columns = GridCells.Adaptive(minSize = 90.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    items(state.results, key = { it.id }) { gif ->
                        GifThumbnail(
                            model = gif.previewUrl,
                            modifier = Modifier
                                .aspectRatio(1f)
                                .clickable {
                                    onGifPicked(
                                        SelectedGif.FromSearch(
                                            id = gif.id,
                                            fullUrl = gif.fullUrl,
                                            previewUrl = gif.previewUrl,
                                            description = gif.description,
                                        )
                                    )
                                },
                        )
                    }
                }
        }
    }
}

/** The model Glide should load to show this gif's thumbnail: a URL for search
 *  results, or the local file itself for device uploads. */
private fun SelectedGif.previewModel(): Any = when (this) {
    is SelectedGif.FromSearch -> previewUrl
    is SelectedGif.FromDevice -> Uri.fromFile(File(filePath))
}

@Composable
private fun GifThumbnail(model: Any, modifier: Modifier = Modifier) {
    AndroidView(
        modifier = modifier,
        factory = { context ->
            ImageView(context).apply { scaleType = ImageView.ScaleType.CENTER_CROP }
        },
        update = { imageView -> Glide.with(imageView).asGif().load(model).into(imageView) },
    )
}
