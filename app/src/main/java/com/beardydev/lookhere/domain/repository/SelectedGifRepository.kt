package com.beardydev.lookhere.domain.repository

import com.beardydev.lookhere.domain.model.SelectedGif
import kotlinx.coroutines.flow.Flow

/** How many past picks [SelectedGifRepository.recentGifs] retains. The Uploads
 *  tab reuses this same list (filtered to device picks), so anything that
 *  backs a device pick on disk must be kept at least this long too -- see
 *  GifUploadLauncher's upload retention. */
const val MAX_RECENT_GIFS = 12

interface SelectedGifRepository {
    val selectedGif: Flow<SelectedGif?>
    val recentGifs: Flow<List<SelectedGif>>
    suspend fun save(gif: SelectedGif)
    suspend fun clear()
    suspend fun getOrCreateCustomerId(): String
}
