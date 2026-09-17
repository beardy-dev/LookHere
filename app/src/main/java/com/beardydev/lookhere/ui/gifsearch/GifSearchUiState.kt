package com.beardydev.lookhere.ui.gifsearch

import com.beardydev.lookhere.domain.error.AppError
import com.beardydev.lookhere.domain.model.GifResult
import com.beardydev.lookhere.domain.model.SelectedGif

sealed class GifSearchUiState {
    data object Loading : GifSearchUiState()
    data class Recents(val items: List<SelectedGif>) : GifSearchUiState()
    data class Success(val results: List<GifResult>) : GifSearchUiState()
    data class Error(val error: AppError) : GifSearchUiState()
}
