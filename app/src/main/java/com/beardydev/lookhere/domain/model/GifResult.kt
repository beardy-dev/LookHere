package com.beardydev.lookhere.domain.model

data class GifResult(
    val id: String,
    val previewUrl: String,
    val fullUrl: String,
    val description: String,
)

/** One page of a paginated GIF listing (currently just trending). */
data class GifPage(
    val items: List<GifResult>,
    val hasNext: Boolean,
)
