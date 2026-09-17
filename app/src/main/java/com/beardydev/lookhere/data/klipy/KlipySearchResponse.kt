package com.beardydev.lookhere.data.klipy

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class KlipySearchResponse(
    val result: Boolean,
    val data: KlipySearchData,
)

@Serializable
data class KlipySearchData(
    val data: List<KlipyItem>,
    @SerialName("current_page") val currentPage: Int,
    @SerialName("per_page") val perPage: Int,
    @SerialName("has_next") val hasNext: Boolean,
)

@Serializable
data class KlipyItem(
    val id: Long,
    val slug: String,
    val title: String,
    val file: KlipyFileSizes,
    val type: String,
)

@Serializable
data class KlipyFileSizes(
    val hd: KlipyFormats? = null,
    val md: KlipyFormats? = null,
    val sm: KlipyFormats? = null,
    val xs: KlipyFormats? = null,
)

@Serializable
data class KlipyFormats(
    val gif: KlipyMedia? = null,
    val webp: KlipyMedia? = null,
    val jpg: KlipyMedia? = null,
    val mp4: KlipyMedia? = null,
    val webm: KlipyMedia? = null,
)

@Serializable
data class KlipyMedia(
    val url: String,
    val width: Int,
    val height: Int,
    val size: Long,
)
