package com.beardydev.lookhere.domain.usecase

import com.beardydev.lookhere.domain.error.AppError
import com.beardydev.lookhere.domain.model.GifBrowseResult
import com.beardydev.lookhere.domain.model.GifPage
import com.beardydev.lookhere.domain.model.GifResult
import com.beardydev.lookhere.domain.model.SelectedGif
import com.beardydev.lookhere.domain.model.TrendingState
import java.io.IOException
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class ObserveGifBrowseResultsUseCaseTest {

    private val gif = GifResult(
        id = "1",
        previewUrl = "https://example.com/1p.gif",
        fullUrl = "https://example.com/1.gif",
        description = "A happy dog",
    )
    private val recent = SelectedGif.FromSearch(
        id = "2",
        fullUrl = "https://example.com/2.gif",
        previewUrl = "https://example.com/2p.gif",
        description = "A cat waving",
    )

    @Test
    fun `blank query browses recents and trending together`() = runTest {
        val selectedGifRepository = FakeSelectedGifRepository(initialRecents = listOf(recent))
        val useCase = ObserveGifBrowseResultsUseCase(
            gifRepository = FakeGifRepository(trendingResult = Result.success(GifPage(listOf(gif), hasNext = false))),
            selectedGifRepository = selectedGifRepository,
            isApiKeyConfigured = true,
        )

        val results = useCase(flowOf("")).take(2).toList()

        assertEquals(
            listOf(
                GifBrowseResult.Browsing(recents = listOf(recent), trending = TrendingState.Loading),
                GifBrowseResult.Browsing(recents = listOf(recent), trending = TrendingState.Loaded(listOf(gif), hasNext = false)),
            ),
            results,
        )
    }

    @Test
    fun `scrolling near the end of trending loads and appends the next page`() = runTest {
        val page1 = GifResult(id = "1", previewUrl = "p1", fullUrl = "f1", description = "one")
        val page2 = GifResult(id = "2", previewUrl = "p2", fullUrl = "f2", description = "two")
        val useCase = ObserveGifBrowseResultsUseCase(
            gifRepository = FakeGifRepository(
                trendingPages = mapOf(
                    1 to Result.success(GifPage(listOf(page1), hasNext = true)),
                    2 to Result.success(GifPage(listOf(page2), hasNext = false)),
                ),
            ),
            selectedGifRepository = FakeSelectedGifRepository(),
            isApiKeyConfigured = true,
        )

        val results = useCase(flowOf(""), loadMoreTrending = flowOf(Unit)).take(4).toList()

        assertEquals(
            listOf(
                GifBrowseResult.Browsing(recents = emptyList(), trending = TrendingState.Loading),
                GifBrowseResult.Browsing(recents = emptyList(), trending = TrendingState.Loaded(listOf(page1), hasNext = true)),
                GifBrowseResult.Browsing(
                    recents = emptyList(),
                    trending = TrendingState.Loaded(listOf(page1), hasNext = true, isLoadingMore = true),
                ),
                GifBrowseResult.Browsing(
                    recents = emptyList(),
                    trending = TrendingState.Loaded(listOf(page1, page2), hasNext = false),
                ),
            ),
            results,
        )
    }

    @Test
    fun `missing api key still allows browsing recents, only trending errors`() = runTest {
        val useCase = ObserveGifBrowseResultsUseCase(
            gifRepository = FakeGifRepository(),
            selectedGifRepository = FakeSelectedGifRepository(initialRecents = listOf(recent)),
            isApiKeyConfigured = false,
        )

        val results = useCase(flowOf("")).take(1).toList()

        assertEquals(
            listOf(GifBrowseResult.Browsing(recents = listOf(recent), trending = TrendingState.Error(AppError.ApiKeyMissing))),
            results,
        )
    }

    @Test
    fun `non-blank query searches`() = runTest {
        val useCase = ObserveGifBrowseResultsUseCase(
            gifRepository = FakeGifRepository(searchResult = Result.success(listOf(gif))),
            selectedGifRepository = FakeSelectedGifRepository(),
            isApiKeyConfigured = true,
        )

        val results = useCase(flowOf("dog")).take(2).toList()

        assertEquals(
            listOf(GifBrowseResult.Loading, GifBrowseResult.Results(listOf(gif))),
            results,
        )
    }

    @Test
    fun `missing api key short-circuits search with ApiKeyMissing, before any loading state`() = runTest {
        val useCase = ObserveGifBrowseResultsUseCase(
            gifRepository = FakeGifRepository(),
            selectedGifRepository = FakeSelectedGifRepository(),
            isApiKeyConfigured = false,
        )

        val results = useCase(flowOf("dog")).take(1).toList()

        assertEquals(listOf(GifBrowseResult.Error(AppError.ApiKeyMissing)), results)
    }

    @Test
    fun `repository failure maps to a typed AppError`() = runTest {
        val useCase = ObserveGifBrowseResultsUseCase(
            gifRepository = FakeGifRepository(searchResult = Result.failure(IOException("offline"))),
            selectedGifRepository = FakeSelectedGifRepository(),
            isApiKeyConfigured = true,
        )

        val results = useCase(flowOf("dog")).take(2).toList()

        assertEquals(
            listOf(GifBrowseResult.Loading, GifBrowseResult.Error(AppError.Network)),
            results,
        )
    }
}
