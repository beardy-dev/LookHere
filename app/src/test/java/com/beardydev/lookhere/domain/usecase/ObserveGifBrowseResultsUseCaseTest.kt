package com.beardydev.lookhere.domain.usecase

import com.beardydev.lookhere.domain.error.AppError
import com.beardydev.lookhere.domain.model.GifBrowseResult
import com.beardydev.lookhere.domain.model.GifResult
import com.beardydev.lookhere.domain.model.SelectedGif
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
    fun `blank query with existing recents shows recents, skipping loading and trending`() = runTest {
        val selectedGifRepository = FakeSelectedGifRepository(initialRecents = listOf(recent))
        val useCase = ObserveGifBrowseResultsUseCase(
            gifRepository = FakeGifRepository(),
            selectedGifRepository = selectedGifRepository,
            isApiKeyConfigured = true,
        )

        val results = useCase(flowOf("")).take(1).toList()

        assertEquals(listOf(GifBrowseResult.Recents(listOf(recent))), results)
    }

    @Test
    fun `blank query with no recents falls back to trending`() = runTest {
        val useCase = ObserveGifBrowseResultsUseCase(
            gifRepository = FakeGifRepository(trendingResult = Result.success(listOf(gif))),
            selectedGifRepository = FakeSelectedGifRepository(initialRecents = emptyList()),
            isApiKeyConfigured = true,
        )

        val results = useCase(flowOf("")).take(2).toList()

        assertEquals(
            listOf(GifBrowseResult.Loading, GifBrowseResult.Results(listOf(gif))),
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
    fun `missing api key short-circuits with ApiKeyMissing, before any loading state`() = runTest {
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
