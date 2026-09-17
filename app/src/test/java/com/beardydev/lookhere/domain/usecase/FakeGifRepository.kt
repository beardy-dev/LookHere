package com.beardydev.lookhere.domain.usecase

import com.beardydev.lookhere.domain.model.GifResult
import com.beardydev.lookhere.domain.repository.GifRepository

class FakeGifRepository(
    private val searchResult: Result<List<GifResult>> = Result.success(emptyList()),
    private val trendingResult: Result<List<GifResult>> = Result.success(emptyList()),
) : GifRepository {
    override suspend fun search(query: String, customerId: String): Result<List<GifResult>> = searchResult
    override suspend fun trending(customerId: String): Result<List<GifResult>> = trendingResult
}
