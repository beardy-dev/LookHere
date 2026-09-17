package com.beardydev.lookhere.domain.usecase

import com.beardydev.lookhere.domain.model.SelectedGif
import com.beardydev.lookhere.domain.repository.SelectedGifRepository

/** Persists the chosen GIF as the current selection and records it in recents. */
class SelectGifUseCase(private val selectedGifRepository: SelectedGifRepository) {
    suspend operator fun invoke(gif: SelectedGif) = selectedGifRepository.save(gif)
}
