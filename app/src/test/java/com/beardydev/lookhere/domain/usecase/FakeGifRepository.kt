package com.beardydev.lookhere.domain.usecase

import com.beardydev.lookhere.domain.model.GifPage
import com.beardydev.lookhere.domain.model.GifResult
import com.beardydev.lookhere.domain.repository.GifRepository

class FakeGifRepository(
    private val searchResult: Result<List<GifResult>> = Result.success(emptyList()),
    private val trendingPages: Map<Int, Result<GifPage>> = emptyMap(),
    private val trendingResult: Result<GifPage> = Result.success(GifPage(items = emptyList(), hasNext = false)),
) : GifRepository {
    override suspend fun search(query: String, customerId: String): Result<List<GifResult>> = searchResult
    override suspend fun trending(customerId: String, page: Int): Result<GifPage> =
        trendingPages[page] ?: trendingResult
}
