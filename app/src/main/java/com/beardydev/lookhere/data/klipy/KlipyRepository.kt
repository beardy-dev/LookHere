package com.beardydev.lookhere.data.klipy

class KlipyRepository(private val api: KlipyApi) {

    suspend fun search(query: String, customerId: String): Result<List<GifResult>> {
        return runCatching {
            api.search(query = query, customerId = customerId)
                .data.data
                .filter { it.type == "gif" } // defensively drop any sponsored ("ad") placements
                .mapNotNull { it.toGifResult() }
        }
    }

    suspend fun trending(customerId: String): Result<List<GifResult>> {
        return runCatching {
            api.trending(customerId = customerId)
                .data.data
                .filter { it.type == "gif" } // defensively drop any sponsored ("ad") placements
                .mapNotNull { it.toGifResult() }
        }
    }

    private fun KlipyItem.toGifResult(): GifResult? {
        val previewUrl = file.sm?.gif?.url ?: file.xs?.gif?.url ?: return null
        val fullUrl = file.md?.gif?.url ?: file.hd?.gif?.url ?: return null
        return GifResult(id = slug, previewUrl = previewUrl, fullUrl = fullUrl, description = title)
    }
}
