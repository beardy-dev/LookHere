package com.beardydev.lookhere.domain.repository

import com.beardydev.lookhere.domain.model.GifPage
import com.beardydev.lookhere.domain.model.GifResult

interface GifRepository {
    suspend fun search(query: String, customerId: String): Result<List<GifResult>>
    suspend fun trending(customerId: String, page: Int = 1): Result<GifPage>
}
