package com.beardydev.lookhere.domain.usecase

import com.beardydev.lookhere.domain.error.AppError
import com.beardydev.lookhere.domain.error.toAppError
import com.beardydev.lookhere.domain.model.GifBrowseResult
import com.beardydev.lookhere.domain.model.TrendingState
import com.beardydev.lookhere.domain.repository.GifRepository
import com.beardydev.lookhere.domain.repository.SelectedGifRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.emptyFlow
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
    /** [loadMoreTrending] is a UI-driven signal (one emission per "near the end
     *  of the trending grid" scroll event) requesting the next trending page. */
    operator fun invoke(queries: Flow<String>, loadMoreTrending: Flow<Unit> = emptyFlow()): Flow<GifBrowseResult> =
        queries.flatMapLatest { text ->
            if (text.isBlank()) observeBrowsing(loadMoreTrending) else observeSearch(text)
        }

    private fun observeBrowsing(loadMoreTrending: Flow<Unit>): Flow<GifBrowseResult> =
        combine(selectedGifRepository.recentGifs, observeTrending(loadMoreTrending)) { recents, trending ->
            GifBrowseResult.Browsing(recents = recents, trending = trending)
        }

    private fun observeTrending(loadMoreTrending: Flow<Unit>): Flow<TrendingState> = flow {
        if (!isApiKeyConfigured) {
            emit(TrendingState.Error(AppError.ApiKeyMissing))
            return@flow
        }
        emit(TrendingState.Loading)
        val customerId = selectedGifRepository.getOrCreateCustomerId()

        val first = gifRepository.trending(customerId, page = 1)
        val firstPage = first.getOrElse {
            emit(TrendingState.Error(it.toAppError()))
            return@flow
        }
        var items = firstPage.items
        var hasNext = firstPage.hasNext
        var page = 1
        emit(TrendingState.Loaded(items, hasNext))

        // Pagination lives entirely in this collector loop rather than as separate
        // use-case state: each "near the end" signal from the UI fetches exactly one
        // more page, in order, for as long as this Browsing subscription stays alive.
        loadMoreTrending.collect {
            if (!hasNext) return@collect
            emit(TrendingState.Loaded(items, hasNext, isLoadingMore = true))
            val next = gifRepository.trending(customerId, page = page + 1)
            next.onSuccess { nextPage ->
                page += 1
                items = items + nextPage.items
                hasNext = nextPage.hasNext
            }
            // On failure, leave page/hasNext unchanged so the next scroll-triggered
            // signal simply retries the same page rather than skipping it or getting
            // stuck -- no separate error state for a failed "load more", since the
            // already-loaded trending grid is still fully usable.
            emit(TrendingState.Loaded(items, hasNext, isLoadingMore = false))
        }
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
