package com.beardydev.lookhere.ui.gifsearch

import android.net.Uri
import android.widget.ImageView
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.FileUpload
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SearchBarDefaults
import androidx.compose.material3.SecondaryTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
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
    var query by rememberSaveable { mutableStateOf("") }
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    // A tap on any GIF (grid, recents, uploads, or a fresh device upload) opens a
    // preview instead of picking it outright -- picking only actually happens on
    // the preview's confirm. Backing out of the preview (X, or the system back
    // button) returns to this same tabbed view with `query` untouched, since it's
    // just local state here, not a navigation change.
    var previewedGif by remember { mutableStateOf<SelectedGif?>(null) }
    val launchUpload = rememberGifUploadLauncher { filePath ->
        previewedGif = SelectedGif.FromDevice(filePath)
    }

    val gifToPreview = previewedGif
    if (gifToPreview != null) {
        BackHandler { previewedGif = null }
        GifPreviewScreen(
            gif = gifToPreview,
            onConfirm = { onGifPicked(gifToPreview) },
            onCancel = { previewedGif = null },
            modifier = modifier,
        )
    } else {
        BackHandler(enabled = canCancel) { onCancel() }
        GifSearchContent(
            uiState = uiState,
            query = query,
            onQueryChange = {
                query = it
                viewModel.onQueryChange(it)
            },
            onUploadClick = launchUpload,
            onGifPicked = { previewedGif = it },
            onTabSelected = viewModel::onTabSelected,
            onTrendingLoadMore = viewModel::onTrendingLoadMore,
            onRetry = viewModel::retry,
            modifier = modifier,
        )
    }
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
    onTrendingLoadMore: () -> Unit,
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
                trailingIcon = {
                    // Clears the query, which drops SearchResults/SearchLoading/SearchError
                    // straight back to the Browsing tabbed view underneath -- that's just
                    // whatever GifSearchUiState a blank query already produces, no separate
                    // "return to tabs" state to manage.
                    if (query.isNotEmpty()) {
                        IconButton(onClick = { onQueryChange("") }) {
                            Icon(Icons.Default.Close, contentDescription = "Clear search")
                        }
                    }
                },
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
                    onTrendingLoadMore = onTrendingLoadMore,
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
    onTrendingLoadMore: () -> Unit,
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

                    is TrendingState.Loaded -> GifResultGrid(
                        items = trending.items,
                        onGifPicked = onGifPicked,
                        onLoadMore = onTrendingLoadMore,
                        isLoadingMore = trending.isLoadingMore,
                    )

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
            columns = GridCells.Adaptive(minSize = 130.dp),
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
    onLoadMore: (() -> Unit)? = null,
    isLoadingMore: Boolean = false,
) {
    val gridState = rememberLazyGridState()

    if (onLoadMore != null) {
        // Fires once as the last few items scroll into view, not once per frame:
        // shouldLoadMore only flips true->false->true again as the threshold is
        // actually crossed, and LaunchedEffect only re-runs on that flip.
        val shouldLoadMore by remember {
            derivedStateOf {
                val layoutInfo = gridState.layoutInfo
                val totalItems = layoutInfo.totalItemsCount
                val lastVisibleIndex = layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: -1
                totalItems > 0 && lastVisibleIndex >= totalItems - 6
            }
        }
        LaunchedEffect(shouldLoadMore) {
            if (shouldLoadMore) onLoadMore()
        }
    }

    LazyVerticalGrid(
        state = gridState,
        modifier = modifier,
        columns = GridCells.Adaptive(minSize = 130.dp),
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
        if (isLoadingMore) {
            item(span = { GridItemSpan(maxLineSpan) }) {
                Box(Modifier.fillMaxWidth().padding(vertical = 16.dp), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp))
                }
            }
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

/** The model for the full-size confirmation preview -- the higher-res fullUrl
 *  for a search result, versus previewModel()'s smaller thumbnail-sized one. */
private fun SelectedGif.fullPreviewModel(): Any = when (this) {
    is SelectedGif.FromSearch -> fullUrl
    is SelectedGif.FromDevice -> Uri.fromFile(File(filePath))
}

/** Shown after picking a GIF from any tab or search results, or after a fresh
 *  device upload: a full-size look before committing to it. Confirm hands the
 *  pick up to the caller (which saves it and navigates to Camera); cancel just
 *  drops this and returns to the browsing view underneath, unchanged. */
@Composable
private fun GifPreviewScreen(
    gif: SelectedGif,
    onConfirm: () -> Unit,
    onCancel: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(modifier.fillMaxSize().background(Color.Black)) {
        AndroidView(
            modifier = Modifier.fillMaxSize(),
            factory = { context ->
                ImageView(context).apply { scaleType = ImageView.ScaleType.FIT_CENTER }
            },
            update = { imageView ->
                imageView.contentDescription = gif.description()
                Glide.with(imageView).asGif().load(gif.fullPreviewModel()).into(imageView)
            },
            onRelease = { imageView -> Glide.with(imageView).clear(imageView) },
        )

        Row(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .navigationBarsPadding()
                .padding(32.dp),
            horizontalArrangement = Arrangement.spacedBy(64.dp),
        ) {
            FilledIconButton(
                onClick = onCancel,
                modifier = Modifier.size(64.dp),
                colors = IconButtonDefaults.filledIconButtonColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant,
                    contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                ),
            ) {
                Icon(
                    imageVector = Icons.Filled.Close,
                    contentDescription = "Discard, back to browsing",
                    modifier = Modifier.size(32.dp),
                )
            }
            FilledIconButton(
                onClick = onConfirm,
                modifier = Modifier.size(64.dp),
            ) {
                Icon(
                    imageVector = Icons.Filled.Check,
                    contentDescription = "Use this GIF",
                    modifier = Modifier.size(32.dp),
                )
            }
        }
    }
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
                onTrendingLoadMore = {},
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
                onTrendingLoadMore = {},
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
                onTrendingLoadMore = {},
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
                onTrendingLoadMore = {},
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
                onTrendingLoadMore = {},
                onRetry = {},
            )
        }
    }
}
