package com.beardydev.lookhere.domain.usecase

import com.beardydev.lookhere.domain.model.SelectedGif
import com.beardydev.lookhere.domain.repository.SelectedGifRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

class FakeSelectedGifRepository(
    initialSelectedGif: SelectedGif? = null,
    initialRecents: List<SelectedGif> = emptyList(),
    private val customerId: String = "customer-id",
) : SelectedGifRepository {
    val selectedGifFlow = MutableStateFlow(initialSelectedGif)
    val recentGifsFlow = MutableStateFlow(initialRecents)

    override val selectedGif: Flow<SelectedGif?> get() = selectedGifFlow
    override val recentGifs: Flow<List<SelectedGif>> get() = recentGifsFlow
    override suspend fun save(gif: SelectedGif) {}
    override suspend fun clear() {}
    override suspend fun getOrCreateCustomerId(): String = customerId
}
