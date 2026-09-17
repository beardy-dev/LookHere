package com.beardydev.lookhere.domain.usecase

import com.beardydev.lookhere.domain.error.AppError
import com.beardydev.lookhere.domain.error.toAppError
import com.beardydev.lookhere.domain.model.GifBrowseResult
import com.beardydev.lookhere.domain.model.TrendingState
import com.beardydev.lookhere.domain.repository.GifRepository
import com.beardydev.lookhere.domain.repository.SelectedGifRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow

/**
 * Given a settled search query, decides what the GIF browse screen should
 * show: a blank query browses recents and trending side by side (for the
 * Recent/Uploads/Trending tabs -- which tab is active is a presentation
 * concern, not this use case's), a non-blank query searches. A missing API
 * key only blocks the parts that actually need the network (search,
 * trending); recents/uploads are pure local data and work regardless.
 */
class ObserveGifBrowseResultsUseCase(
    private val gifRepository: GifRepository,
    private val selectedGifRepository: SelectedGifRepository,
    private val isApiKeyConfigured: Boolean,
) {
    operator fun invoke(queries: Flow<String>): Flow<GifBrowseResult> =
        queries.flatMapLatest { text ->
            if (text.isBlank()) observeBrowsing() else observeSearch(text)
        }

    private fun observeBrowsing(): Flow<GifBrowseResult> =
        combine(selectedGifRepository.recentGifs, observeTrending()) { recents, trending ->
            GifBrowseResult.Browsing(recents = recents, trending = trending)
        }

    private fun observeTrending(): Flow<TrendingState> = flow {
        if (!isApiKeyConfigured) {
            emit(TrendingState.Error(AppError.ApiKeyMissing))
            return@flow
        }
        emit(TrendingState.Loading)
        val customerId = selectedGifRepository.getOrCreateCustomerId()
        gifRepository.trending(customerId).fold(
            onSuccess = { emit(TrendingState.Loaded(it)) },
            onFailure = { emit(TrendingState.Error(it.toAppError())) },
        )
    }

    private fun observeSearch(text: String): Flow<GifBrowseResult> = flow {
        if (!isApiKeyConfigured) {
            emit(GifBrowseResult.Error(AppError.ApiKeyMissing))
            return@flow
        }
        emit(GifBrowseResult.Loading)
        val customerId = selectedGifRepository.getOrCreateCustomerId()
        gifRepository.search(text, customerId).fold(
            onSuccess = { emit(GifBrowseResult.Results(it)) },
            onFailure = { emit(GifBrowseResult.Error(it.toAppError())) },
        )
    }
}
