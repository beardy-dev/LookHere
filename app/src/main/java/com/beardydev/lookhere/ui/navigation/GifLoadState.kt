package com.beardydev.lookhere.ui.navigation

import com.beardydev.lookhere.domain.model.SelectedGif

/** Distinguishes "still reading the persisted GIF from DataStore" from "read it, and there wasn't one". */
sealed class GifLoadState {
    data object Loading : GifLoadState()
    data class Loaded(val gif: SelectedGif?) : GifLoadState()
}
