package com.beardydev.lookhere.ui.gifsearch

import com.beardydev.lookhere.domain.error.AppError
import com.beardydev.lookhere.domain.model.GifResult
import com.beardydev.lookhere.domain.model.SelectedGif
import com.beardydev.lookhere.domain.model.TrendingState

enum class BrowseTab { TRENDING, RECENT, UPLOADS }

sealed class GifSearchUiState {
    /** Blank query: the tabbed Recent/Uploads/Trending area. */
    data class Browsing(
        val selectedTab: BrowseTab,
        val recent: List<SelectedGif>,
        val uploads: List<SelectedGif>,
        val trending: TrendingState,
    ) : GifSearchUiState()

    /** Non-blank query: the tabbed area is replaced by search results. */
    data object SearchLoading : GifSearchUiState()
    data class SearchResults(val results: List<GifResult>) : GifSearchUiState()
    data class SearchError(val error: AppError) : GifSearchUiState()
}
