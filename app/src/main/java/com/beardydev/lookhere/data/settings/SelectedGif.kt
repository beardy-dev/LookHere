package com.beardydev.lookhere.data.settings

import kotlinx.serialization.Serializable

@Serializable
sealed class SelectedGif {
    @Serializable
    data class FromSearch(
        val id: String,
        val fullUrl: String,
        val previewUrl: String,
        val description: String,
    ) : SelectedGif()

    @Serializable
    data class FromDevice(val filePath: String) : SelectedGif()
}

/** Stable identity for diffing/deduping recents, independent of field order. */
val SelectedGif.stableKey: String
    get() = when (this) {
        is SelectedGif.FromSearch -> "search:$id"
        is SelectedGif.FromDevice -> "device:$filePath"
    }
