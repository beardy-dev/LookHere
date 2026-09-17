package com.beardydev.lookhere.ui.gifsearch

import com.beardydev.lookhere.data.klipy.GifResult
import com.beardydev.lookhere.data.settings.SelectedGif

sealed class GifSearchUiState {
    data object Loading : GifSearchUiState()
    data class Recents(val items: List<SelectedGif>) : GifSearchUiState()
    data class Success(val results: List<GifResult>) : GifSearchUiState()
    data class Error(val message: String) : GifSearchUiState()
}
