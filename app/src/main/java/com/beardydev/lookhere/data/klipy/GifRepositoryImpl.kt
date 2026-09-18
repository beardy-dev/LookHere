package com.beardydev.lookhere.data.klipy

import com.beardydev.lookhere.domain.model.GifPage
import com.beardydev.lookhere.domain.model.GifResult
import com.beardydev.lookhere.domain.repository.GifRepository

class GifRepositoryImpl(private val api: KlipyApi) : GifRepository {

    override suspend fun search(query: String, customerId: String): Result<List<GifResult>> {
        return runCatching {
            api.search(query = query, customerId = customerId)
                .data.data
                .filter { it.type == "gif" } // defensively drop any sponsored ("ad") placements
                .mapNotNull { it.toGifResult() }
        }
    }

    override suspend fun trending(customerId: String, page: Int): Result<GifPage> {
        return runCatching {
            val response = api.trending(customerId = customerId, page = page)
            GifPage(
                items = response.data.data
                    .filter { it.type == "gif" } // defensively drop any sponsored ("ad") placements
                    .mapNotNull { it.toGifResult() },
                hasNext = response.data.hasNext,
            )
        }
    }

    private fun KlipyItem.toGifResult(): GifResult? {
        val previewUrl = file.sm?.gif?.url ?: file.xs?.gif?.url ?: return null
        val fullUrl = file.md?.gif?.url ?: file.hd?.gif?.url ?: return null
        return GifResult(id = slug, previewUrl = previewUrl, fullUrl = fullUrl, description = title)
    }
}
