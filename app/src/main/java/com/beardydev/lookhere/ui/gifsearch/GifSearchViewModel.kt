package com.beardydev.lookhere.ui.gifsearch

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.beardydev.lookhere.domain.model.GifBrowseResult
import com.beardydev.lookhere.domain.model.SelectedGif
import com.beardydev.lookhere.domain.model.TrendingState
import com.beardydev.lookhere.domain.usecase.ObserveGifBrowseResultsUseCase
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update

class GifSearchViewModel(
    private val observeGifBrowseResults: ObserveGifBrowseResultsUseCase,
) : ViewModel() {

    private val query = MutableStateFlow("")
    private val retryTick = MutableStateFlow(0)
    private val selectedTab = MutableStateFlow(BrowseTab.TRENDING)
    // Buffered (not conflated to 0) so a scroll-triggered load-more that fires while
    // the previous page is still in flight isn't dropped, but capped at 1 so a burst
    // of scroll events only ever queues a single extra fetch, not one per event.
    private val loadMoreTrending = MutableSharedFlow<Unit>(extraBufferCapacity = 1, onBufferOverflow = BufferOverflow.DROP_OLDEST)

    // retryTick lets retry() re-trigger a lookup even for an unchanged query,
    // which distinctUntilChanged() would otherwise suppress. It restarts
    // whichever side (browsing or search) is currently active.
    private val searchTrigger: Flow<String> = combine(
        query.debounce(300).distinctUntilChanged(),
        retryTick,
    ) { text, _ -> text }

    val uiState: StateFlow<GifSearchUiState> = combine(
        observeGifBrowseResults(searchTrigger, loadMoreTrending),
        selectedTab,
    ) { result, tab -> result.toUiState(tab) }
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5_000),
            GifSearchUiState.Browsing(
                selectedTab = BrowseTab.TRENDING,
                recent = emptyList(),
                uploads = emptyList(),
                trending = TrendingState.Loading,
            ),
        )

    fun onQueryChange(text: String) {
        query.value = text
    }

    fun onTabSelected(tab: BrowseTab) {
        selectedTab.value = tab
    }

    fun retry() {
        retryTick.update { it + 1 }
    }

    fun onTrendingLoadMore() {
        loadMoreTrending.tryEmit(Unit)
    }
}

private fun GifBrowseResult.toUiState(tab: BrowseTab): GifSearchUiState = when (this) {
    is GifBrowseResult.Browsing -> GifSearchUiState.Browsing(
        selectedTab = tab,
        recent = recents,
        uploads = recents.filterIsInstance<SelectedGif.FromDevice>(),
        trending = trending,
    )
    GifBrowseResult.Loading -> GifSearchUiState.SearchLoading
    is GifBrowseResult.Results -> GifSearchUiState.SearchResults(items)
    is GifBrowseResult.Error -> GifSearchUiState.SearchError(error)
}
