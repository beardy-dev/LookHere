package com.beardydev.lookhere.domain.model

import com.beardydev.lookhere.domain.error.AppError

sealed interface GifBrowseResult {
    /** Blank query: the Recent/Uploads/Trending tabs, browsable side by side. */
    data class Browsing(val recents: List<SelectedGif>, val trending: TrendingState) : GifBrowseResult

    /** Non-blank query: a search is in flight or has settled. */
    data object Loading : GifBrowseResult
    data class Results(val items: List<GifResult>) : GifBrowseResult
    data class Error(val error: AppError) : GifBrowseResult
}

sealed interface TrendingState {
    data object Loading : TrendingState

    /** [isLoadingMore] is true while a next page fetch (triggered by scrolling
     *  near the end) is in flight; [hasNext] says whether there's a next page
     *  left to load at all. */
    data class Loaded(
        val items: List<GifResult>,
        val hasNext: Boolean = false,
        val isLoadingMore: Boolean = false,
    ) : TrendingState

    data class Error(val error: AppError) : TrendingState
}
