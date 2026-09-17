package com.beardydev.lookhere.domain.usecase

import com.beardydev.lookhere.domain.model.SelectedGif
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Test

class ObserveCoverScreenGifUseCaseTest {

    private val gif = SelectedGif.FromSearch(
        id = "1",
        fullUrl = "https://example.com/1.gif",
        previewUrl = "https://example.com/1p.gif",
        description = "A happy dog",
    )

    @Test
    fun `emits the selected gif when not suppressed`() = runBlocking {
        val repository = FakeSelectedGifRepository(initialSelectedGif = gif)
        val useCase = ObserveCoverScreenGifUseCase(repository)
        val suppressed = MutableStateFlow(false)

        val first = useCase(suppressed).take(1).toList().single()

        assertEquals(gif, first)
    }

    @Test
    fun `emits null when suppressed, even with a gif selected`() = runBlocking {
        val repository = FakeSelectedGifRepository(initialSelectedGif = gif)
        val useCase = ObserveCoverScreenGifUseCase(repository)
        val suppressed = MutableStateFlow(true)

        val first = useCase(suppressed).take(1).toList().single()

        assertEquals(null, first)
    }

    @Test
    fun `reacts to suppression changing without a new gif selection`() = runBlocking {
        val repository = FakeSelectedGifRepository(initialSelectedGif = gif)
        val useCase = ObserveCoverScreenGifUseCase(repository)
        val suppressed = MutableStateFlow(false)

        val emissions = mutableListOf<SelectedGif?>()
        val job = launch(Dispatchers.Unconfined) {
            useCase(suppressed).collect { emissions.add(it) }
        }

        suppressed.value = true
        suppressed.value = false
        job.cancel()

        assertEquals(listOf(gif, null, gif), emissions)
    }
}
