package com.beardydev.lookhere.ui.gifsearch

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.beardydev.lookhere.data.klipy.KlipyRepository
import com.beardydev.lookhere.data.settings.SelectedGifRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.stateIn

class GifSearchViewModel(
    private val klipyRepository: KlipyRepository,
    private val selectedGifRepository: SelectedGifRepository,
    private val klipyApiKeyConfigured: Boolean,
) : ViewModel() {

    private val query = MutableStateFlow("")

    val uiState: StateFlow<GifSearchUiState> = query
        .debounce(300)
        .distinctUntilChanged()
        .flatMapLatest { text ->
            flow {
                if (!klipyApiKeyConfigured) {
                    emit(
                        GifSearchUiState.Error(
                            "Klipy API key is missing. Add KLIPY_API_KEY to local.properties and rebuild."
                        )
                    )
                    return@flow
                }
                if (text.isBlank()) {
                    // A blank query shows recently used GIFs first, if there are any,
                    // so re-picking a favorite doesn't require a re-search. Only once
                    // there's no history does it fall back to trending as the default
                    // browse list, so there's always something to pick from.
                    val recents = selectedGifRepository.recentGifs.first()
                    if (recents.isNotEmpty()) {
                        emit(GifSearchUiState.Recents(recents))
                        return@flow
                    }
                }

                emit(GifSearchUiState.Loading)
                val customerId = selectedGifRepository.getOrCreateCustomerId()
                val result = if (text.isBlank()) {
                    klipyRepository.trending(customerId)
                } else {
                    klipyRepository.search(text, customerId)
                }
                result.fold(
                    onSuccess = { emit(GifSearchUiState.Success(it)) },
                    onFailure = { emit(GifSearchUiState.Error(it.message ?: "Couldn't load GIFs. Check your connection.")) },
                )
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), GifSearchUiState.Loading)

    fun onQueryChange(text: String) {
        query.value = text
    }
}
