package com.beardydev.lookhere.domain.repository

import com.beardydev.lookhere.domain.model.SelectedGif
import kotlinx.coroutines.flow.Flow

interface SelectedGifRepository {
    val selectedGif: Flow<SelectedGif?>
    val recentGifs: Flow<List<SelectedGif>>
    suspend fun save(gif: SelectedGif)
    suspend fun clear()
    suspend fun getOrCreateCustomerId(): String
}
