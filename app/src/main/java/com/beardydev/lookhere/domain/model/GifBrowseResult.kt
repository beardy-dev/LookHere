package com.beardydev.lookhere.domain.model

import com.beardydev.lookhere.domain.error.AppError

sealed interface GifBrowseResult {
    data object Loading : GifBrowseResult
    data class Recents(val items: List<SelectedGif>) : GifBrowseResult
    data class Results(val items: List<GifResult>) : GifBrowseResult
    data class Error(val error: AppError) : GifBrowseResult
}
