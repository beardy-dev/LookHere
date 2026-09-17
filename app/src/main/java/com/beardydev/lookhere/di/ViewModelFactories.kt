package com.beardydev.lookhere.di

import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.beardydev.lookhere.ui.gifsearch.GifSearchViewModel

fun AppContainer.gifSearchViewModelFactory() = viewModelFactory {
    initializer {
        GifSearchViewModel(
            klipyRepository = klipyRepository,
            selectedGifRepository = selectedGifRepository,
            klipyApiKeyConfigured = isKlipyApiKeyConfigured,
        )
    }
}
