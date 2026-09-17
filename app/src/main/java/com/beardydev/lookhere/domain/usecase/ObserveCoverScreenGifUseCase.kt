package com.beardydev.lookhere.domain.usecase

import com.beardydev.lookhere.domain.model.SelectedGif
import com.beardydev.lookhere.domain.repository.SelectedGifRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine

/**
 * Decides what GIF, if any, the cover screen should currently show:
 * the selected GIF, or nothing while suppressed (e.g. selfie mode, where
 * the GIF plays in-frame on the main screen instead).
 */
class ObserveCoverScreenGifUseCase(private val selectedGifRepository: SelectedGifRepository) {
    operator fun invoke(suppressed: Flow<Boolean>): Flow<SelectedGif?> =
        combine(selectedGifRepository.selectedGif, suppressed) { gif, isSuppressed ->
            if (isSuppressed) null else gif
        }
}
