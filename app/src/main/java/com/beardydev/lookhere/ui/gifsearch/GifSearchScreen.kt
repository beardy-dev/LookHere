package com.beardydev.lookhere.ui.gifsearch

import android.net.Uri
import android.widget.ImageView
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.FileUpload
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SearchBarDefaults
import androidx.compose.material3.SecondaryTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.beardydev.lookhere.domain.error.AppError
import com.beardydev.lookhere.domain.model.GifResult
import com.beardydev.lookhere.domain.model.SelectedGif
import com.beardydev.lookhere.domain.model.TrendingState
import com.beardydev.lookhere.domain.model.stableKey
import com.beardydev.lookhere.ui.theme.LookHereTheme
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

    GifSearchContent(
        uiState = uiState,
        query = query,
        onQueryChange = {
            query = it
            viewModel.onQueryChange(it)
        },
        onUploadClick = launchUpload,
        onGifPicked = onGifPicked,
        onTabSelected = viewModel::onTabSelected,
        onRetry = viewModel::retry,
        modifier = modifier,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun GifSearchContent(
    uiState: GifSearchUiState,
    query: String,
    onQueryChange: (String) -> Unit,
    onUploadClick: () -> Unit,
    onGifPicked: (SelectedGif) -> Unit,
    onTabSelected: (BrowseTab) -> Unit,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .statusBarsPadding()
            .padding(horizontal = 16.dp)
            .padding(top = 24.dp, bottom = 16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            SearchBarDefaults.InputField(
                query = query,
                onQueryChange = onQueryChange,
                onSearch = {},
                expanded = false,
                onExpandedChange = {},
                modifier = Modifier.weight(1f),
                placeholder = { Text("Search KLIPY") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
            )
            Spacer(Modifier.width(8.dp))
            FilledIconButton(
                onClick = onUploadClick,
                modifier = Modifier.size(56.dp),
            ) {
                Icon(Icons.Filled.FileUpload, contentDescription = "Upload a GIF from your phone")
            }
        }
        Spacer(Modifier.height(12.dp))
        when (uiState) {
            is GifSearchUiState.Browsing ->
                BrowsingContent(
                    state = uiState,
                    onTabSelected = onTabSelected,
                    onGifPicked = onGifPicked,
                    onRetry = onRetry,
                )

            is GifSearchUiState.SearchLoading ->
                Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }

            is GifSearchUiState.SearchError -> ErrorContent(error = uiState.error, onRetry = onRetry)

            is GifSearchUiState.SearchResults ->
                GifResultGrid(items = uiState.results, onGifPicked = onGifPicked)
        }
    }
}

@Composable
private fun BrowsingContent(
    state: GifSearchUiState.Browsing,
    onTabSelected: (BrowseTab) -> Unit,
    onGifPicked: (SelectedGif) -> Unit,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val tabs = BrowseTab.entries
    Column(modifier) {
        SecondaryTabRow(selectedTabIndex = tabs.indexOf(state.selectedTab)) {
            tabs.forEach { tab ->
                Tab(
                    selected = tab == state.selectedTab,
                    onClick = { onTabSelected(tab) },
                    text = { Text(tab.label()) },
                )
            }
        }
        Spacer(Modifier.height(8.dp))
        when (state.selectedTab) {
            BrowseTab.RECENT ->
                SelectedGifGrid(
                    items = state.recent,
                    emptyMessage = "GIFs you pick will show up here.",
                    onGifPicked = onGifPicked,
                )

            BrowseTab.UPLOADS ->
                SelectedGifGrid(
                    items = state.uploads,
                    emptyMessage = "GIFs you upload from your phone will show up here.",
                    onGifPicked = onGifPicked,
                )

            BrowseTab.TRENDING ->
                when (val trending = state.trending) {
                    TrendingState.Loading ->
                        Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator()
                        }

                    is TrendingState.Loaded -> GifResultGrid(items = trending.items, onGifPicked = onGifPicked)

                    is TrendingState.Error -> ErrorContent(error = trending.error, onRetry = onRetry)
                }
        }
    }
}

private fun BrowseTab.label(): String = when (this) {
    BrowseTab.RECENT -> "Recent"
    BrowseTab.UPLOADS -> "Uploads"
    BrowseTab.TRENDING -> "Trending"
}

@Composable
private fun SelectedGifGrid(
    items: List<SelectedGif>,
    emptyMessage: String,
    onGifPicked: (SelectedGif) -> Unit,
    modifier: Modifier = Modifier,
) {
    if (items.isEmpty()) {
        EmptyState(message = emptyMessage, modifier = modifier)
    } else {
        LazyVerticalGrid(
            modifier = modifier,
            columns = GridCells.Adaptive(minSize = 90.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            items(items, key = { it.stableKey }) { gif ->
                GifThumbnail(
                    model = gif.previewModel(),
                    contentDescription = gif.description(),
                    modifier = Modifier
                        .aspectRatio(1f)
                        .clickable { onGifPicked(gif) },
                )
            }
        }
    }
}

@Composable
private fun GifResultGrid(
    items: List<GifResult>,
    onGifPicked: (SelectedGif) -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyVerticalGrid(
        modifier = modifier,
        columns = GridCells.Adaptive(minSize = 90.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        items(items, key = { it.id }) { gif ->
            GifThumbnail(
                model = gif.previewUrl,
                contentDescription = gif.description,
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

@Composable
private fun EmptyState(message: String, modifier: Modifier = Modifier) {
    Box(modifier.fillMaxWidth().padding(vertical = 32.dp), contentAlignment = Alignment.Center) {
        Text(
            text = message,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
private fun ErrorContent(error: AppError, onRetry: () -> Unit, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.fillMaxWidth().padding(vertical = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Icon(
            imageVector = Icons.Default.ErrorOutline,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.error,
            modifier = Modifier.size(40.dp),
        )
        Spacer(Modifier.height(12.dp))
        Text(
            text = error.toMessage(),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.height(16.dp))
        Button(onClick = onRetry) {
            Text("Retry")
        }
    }
}

private fun AppError.toMessage(): String = when (this) {
    AppError.Network -> "Couldn't load GIFs. Check your connection."
    AppError.ApiKeyMissing -> "Klipy API key is missing. Add KLIPY_API_KEY to local.properties and rebuild."
    is AppError.Unknown -> message ?: "Something went wrong."
}

/** The model Glide should load to show this gif's thumbnail: a URL for search
 *  results, or the local file itself for device uploads. */
private fun SelectedGif.previewModel(): Any = when (this) {
    is SelectedGif.FromSearch -> previewUrl
    is SelectedGif.FromDevice -> Uri.fromFile(File(filePath))
}

/** Device uploads have no description to surface -- null is a legitimate a11y
 *  choice for a local thumbnail the user picked themselves. */
private fun SelectedGif.description(): String? = when (this) {
    is SelectedGif.FromSearch -> description
    is SelectedGif.FromDevice -> null
}

@Composable
private fun GifThumbnail(model: Any, contentDescription: String?, modifier: Modifier = Modifier) {
    AndroidView(
        modifier = modifier,
        factory = { context ->
            ImageView(context).apply { scaleType = ImageView.ScaleType.CENTER_CROP }
        },
        update = { imageView ->
            imageView.contentDescription = contentDescription
            Glide.with(imageView).asGif().load(model).into(imageView)
        },
        onRelease = { imageView -> Glide.with(imageView).clear(imageView) },
    )
}

@Preview(showBackground = true)
@Composable
private fun GifSearchContentRecentPreview() {
    LookHereTheme {
        Surface {
            GifSearchContent(
                uiState = GifSearchUiState.Browsing(
                    selectedTab = BrowseTab.RECENT,
                    recent = listOf(
                        SelectedGif.FromSearch("1", "https://example.com/1.gif", "https://example.com/1p.gif", "A happy dog"),
                        SelectedGif.FromSearch("2", "https://example.com/2.gif", "https://example.com/2p.gif", "A cat waving"),
                    ),
                    uploads = emptyList(),
                    trending = TrendingState.Loading,
                ),
                query = "",
                onQueryChange = {},
                onUploadClick = {},
                onGifPicked = {},
                onTabSelected = {},
                onRetry = {},
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun GifSearchContentUploadsEmptyPreview() {
    LookHereTheme {
        Surface {
            GifSearchContent(
                uiState = GifSearchUiState.Browsing(
                    selectedTab = BrowseTab.UPLOADS,
                    recent = emptyList(),
                    uploads = emptyList(),
                    trending = TrendingState.Loading,
                ),
                query = "",
                onQueryChange = {},
                onUploadClick = {},
                onGifPicked = {},
                onTabSelected = {},
                onRetry = {},
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun GifSearchContentTrendingPreview() {
    LookHereTheme {
        Surface {
            GifSearchContent(
                uiState = GifSearchUiState.Browsing(
                    selectedTab = BrowseTab.TRENDING,
                    recent = emptyList(),
                    uploads = emptyList(),
                    trending = TrendingState.Loaded(
                        items = listOf(
                            GifResult("1", "https://example.com/1p.gif", "https://example.com/1.gif", "A happy dog"),
                            GifResult("2", "https://example.com/2p.gif", "https://example.com/2.gif", "A cat waving"),
                        )
                    ),
                ),
                query = "",
                onQueryChange = {},
                onUploadClick = {},
                onGifPicked = {},
                onTabSelected = {},
                onRetry = {},
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun GifSearchContentSearchResultsPreview() {
    LookHereTheme {
        Surface {
            GifSearchContent(
                uiState = GifSearchUiState.SearchResults(
                    results = listOf(
                        GifResult("1", "https://example.com/1p.gif", "https://example.com/1.gif", "A happy dog"),
                        GifResult("2", "https://example.com/2p.gif", "https://example.com/2.gif", "A cat waving"),
                    )
                ),
                query = "dog",
                onQueryChange = {},
                onUploadClick = {},
                onGifPicked = {},
                onTabSelected = {},
                onRetry = {},
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun GifSearchContentSearchErrorPreview() {
    LookHereTheme {
        Surface {
            GifSearchContent(
                uiState = GifSearchUiState.SearchError(AppError.Network),
                query = "dog",
                onQueryChange = {},
                onUploadClick = {},
                onGifPicked = {},
                onTabSelected = {},
                onRetry = {},
            )
        }
    }
}
