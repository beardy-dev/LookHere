package com.beardydev.lookhere.ui.gifsearch

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.beardydev.lookhere.domain.model.GifBrowseResult
import com.beardydev.lookhere.domain.usecase.ObserveGifBrowseResultsUseCase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update

class GifSearchViewModel(
    private val observeGifBrowseResults: ObserveGifBrowseResultsUseCase,
) : ViewModel() {

    private val query = MutableStateFlow("")
    private val retryTick = MutableStateFlow(0)

    // retryTick lets retry() re-trigger a lookup even for an unchanged query,
    // which distinctUntilChanged() would otherwise suppress.
    private val searchTrigger: Flow<String> = combine(
        query.debounce(300).distinctUntilChanged(),
        retryTick,
    ) { text, _ -> text }

    val uiState: StateFlow<GifSearchUiState> = observeGifBrowseResults(searchTrigger)
        .map { it.toUiState() }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), GifSearchUiState.Loading)

    fun onQueryChange(text: String) {
        query.value = text
    }

    fun retry() {
        retryTick.update { it + 1 }
    }
}

private fun GifBrowseResult.toUiState(): GifSearchUiState = when (this) {
    GifBrowseResult.Loading -> GifSearchUiState.Loading
    is GifBrowseResult.Recents -> GifSearchUiState.Recents(items)
    is GifBrowseResult.Results -> GifSearchUiState.Success(items)
    is GifBrowseResult.Error -> GifSearchUiState.Error(error)
}
