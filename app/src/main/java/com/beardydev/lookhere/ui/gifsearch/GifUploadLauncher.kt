package com.beardydev.lookhere.ui.gifsearch

import android.content.Context
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import java.io.File
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Launches the Android Photo Picker restricted to GIFs. The picker's URI grant
 * isn't guaranteed durable across reboots, so the file is copied into app-private
 * storage immediately; [onGifCopied] receives that copy's absolute path.
 */
@Composable
fun rememberGifUploadLauncher(onGifCopied: (String) -> Unit): () -> Unit {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        if (uri != null) {
            scope.launch {
                copyGifToInternalStorage(context, uri)?.let(onGifCopied)
            }
        }
    }
    return {
        launcher.launch(
            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.SingleMimeType("image/gif"))
        )
    }
}

private const val UPLOADED_GIF_PREFIX = "uploaded_gif_"

private suspend fun copyGifToInternalStorage(context: Context, uri: Uri): String? =
    withContext(Dispatchers.IO) {
        runCatching {
            // A unique filename per upload, not a fixed one: reusing the same path
            // would overwrite a file while a previous decode (e.g. the looping cover
            // screen or GIF pane) might still be actively reading it via a memory-
            // mapped buffer, which crashes the decoder with a native SIGBUS and can
            // also serve stale cached frames for the reused path.
            val destination = File(context.filesDir, "$UPLOADED_GIF_PREFIX${System.currentTimeMillis()}.gif")
            context.contentResolver.openInputStream(uri)?.use { input ->
                destination.outputStream().use { output -> input.copyTo(output) }
            } ?: return@runCatching null

            // Safe to clean up older uploads now: deleting a file doesn't disturb any
            // process still reading it via an existing file descriptor/mapping, unlike
            // overwriting one in place.
            context.filesDir
                .listFiles { file -> file.name.startsWith(UPLOADED_GIF_PREFIX) && file != destination }
                ?.forEach { it.delete() }

            destination.absolutePath
        }.getOrNull()
    }
