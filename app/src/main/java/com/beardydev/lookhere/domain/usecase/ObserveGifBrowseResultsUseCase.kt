package com.beardydev.lookhere.domain.usecase

import com.beardydev.lookhere.domain.error.AppError
import com.beardydev.lookhere.domain.error.toAppError
import com.beardydev.lookhere.domain.model.GifBrowseResult
import com.beardydev.lookhere.domain.repository.GifRepository
import com.beardydev.lookhere.domain.repository.SelectedGifRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow

/**
 * Given a settled search query, decides what the GIF browse screen should
 * show: a blank query prefers recents (falling back to trending only once
 * there's no history), a non-blank query searches, and a missing API key
 * short-circuits everything with a dedicated error.
 */
class ObserveGifBrowseResultsUseCase(
    private val gifRepository: GifRepository,
    private val selectedGifRepository: SelectedGifRepository,
    private val isApiKeyConfigured: Boolean,
) {
    operator fun invoke(queries: Flow<String>): Flow<GifBrowseResult> =
        queries.flatMapLatest { text ->
            flow {
                if (!isApiKeyConfigured) {
                    emit(GifBrowseResult.Error(AppError.ApiKeyMissing))
                    return@flow
                }

                if (text.isBlank()) {
                    // A blank query shows recently used GIFs first, if there are any,
                    // so re-picking a favorite doesn't require a re-search. Only once
                    // there's no history does it fall back to trending as the default
                    // browse list, so there's always something to pick from.
                    val recents = selectedGifRepository.recentGifs.first()
                    if (recents.isNotEmpty()) {
                        emit(GifBrowseResult.Recents(recents))
                        return@flow
                    }
                }

                emit(GifBrowseResult.Loading)
                val customerId = selectedGifRepository.getOrCreateCustomerId()
                val result = if (text.isBlank()) {
                    gifRepository.trending(customerId)
                } else {
                    gifRepository.search(text, customerId)
                }
                result.fold(
                    onSuccess = { emit(GifBrowseResult.Results(it)) },
                    onFailure = { emit(GifBrowseResult.Error(it.toAppError())) },
                )
            }
        }
}
