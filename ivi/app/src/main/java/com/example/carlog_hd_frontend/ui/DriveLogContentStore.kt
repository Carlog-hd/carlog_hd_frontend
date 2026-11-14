package com.example.carlog_hd_frontend.ui

import android.content.Context
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.webkit.MimeTypeMap
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.security.MessageDigest
import java.util.UUID

data class StoryClip(
    val id: String,
    val uri: Uri,
    val mimeType: String,
    val durationMs: Long,
    val startMs: Long = 0,
    val endMs: Long = durationMs,
) {
    val isVideo: Boolean get() = mimeType.startsWith("video/")
    val playDurationMs: Long get() = (endMs - startMs).coerceAtLeast(100)
}

data class ClipImportResult(val added: Int, val failed: Int)

/** Story drafts remain on this IVI device until a phone or camera source is connected. */
object DriveLogContentStore {
    private const val PREFS = "carlog_drive_log_content"
    private const val MAX_CLIPS = 30
    private const val MAX_SOURCE_BYTES = 500L * 1024 * 1024
    private const val PHOTO_DURATION_MS = 3_500L

    fun clips(context: Context, recordId: String): List<StoryClip> {
        val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        val encoded = prefs.getString("clips:$recordId", null)
        if (encoded != null) return runCatching {
            val array = JSONArray(encoded)
            (0 until array.length()).map { array.getJSONObject(it).toClip() }
        }.getOrDefault(emptyList())
        // Older builds stored only document URIs. Preserve those attachments.
        return prefs.getString("media:$recordId", "").orEmpty().split('\n')
            .filter(String::isNotBlank).mapNotNull { raw -> runCatching {
                val uri = Uri.parse(raw)
                val mime = context.contentResolver.getType(uri) ?: "image/*"
                val duration = if (mime.startsWith("video/")) videoDuration(context, uri) else PHOTO_DURATION_MS
                StoryClip(UUID.nameUUIDFromBytes(raw.toByteArray()).toString(), uri, mime, duration)
            }.getOrNull() }
    }

    fun media(context: Context, recordId: String): List<Uri> = clips(context, recordId).map(StoryClip::uri)

    /** Copy selected sources into app-private storage on a worker thread. */
    fun importMedia(context: Context, recordId: String, uris: List<Uri>): ClipImportResult {
        val current = clips(context, recordId).toMutableList()
        var added = 0
        var failed = 0
        uris.take((MAX_CLIPS - current.size).coerceAtLeast(0)).forEach { uri ->
            val imported = runCatching {
                val mime = context.contentResolver.getType(uri).orEmpty()
                require(mime.startsWith("image/") || mime.startsWith("video/"))
                val extension = MimeTypeMap.getSingleton().getExtensionFromMimeType(mime)
                    ?: if (mime.startsWith("video/")) "mp4" else "jpg"
                val file = File(clipDirectory(context, recordId), "${UUID.randomUUID()}.$extension")
                try {
                    context.contentResolver.openInputStream(uri)?.use { input ->
                        file.outputStream().use { output ->
                            val buffer = ByteArray(64 * 1024)
                            var copied = 0L
                            while (true) {
                                val count = input.read(buffer)
                                if (count < 0) break
                                copied += count
                                require(copied <= MAX_SOURCE_BYTES)
                                output.write(buffer, 0, count)
                            }
                        }
                    } ?: error("Cannot read source")
                    require(file.length() > 0)
                    val local = Uri.fromFile(file)
                    val duration = if (mime.startsWith("video/")) videoDuration(context, local) else PHOTO_DURATION_MS
                    StoryClip(UUID.randomUUID().toString(), local, mime, duration)
                } catch (error: Exception) {
                    file.delete()
                    throw error
                }
            }.getOrNull()
            if (imported == null) failed++ else { current.add(imported); added++ }
        }
        failed += uris.size - added - failed
        if (added > 0) saveClips(context, recordId, current)
        return ClipImportResult(added, failed)
    }

    fun moveClip(context: Context, recordId: String, from: Int, to: Int): Boolean {
        val current = clips(context, recordId).toMutableList()
        if (from !in current.indices || to !in current.indices) return false
        current.add(to, current.removeAt(from))
        saveClips(context, recordId, current)
        return true
    }

    fun updateClip(context: Context, recordId: String, updated: StoryClip) {
        val current = clips(context, recordId).toMutableList()
        val index = current.indexOfFirst { it.id == updated.id }
        if (index < 0) return
        current[index] = updated
        saveClips(context, recordId, current)
    }

    fun removeClip(context: Context, recordId: String, clipId: String) {
        val current = clips(context, recordId).toMutableList()
        val removed = current.firstOrNull { it.id == clipId } ?: return
        current.remove(removed)
        saveClips(context, recordId, current)
        runCatching {
            val file = removed.uri.path?.let(::File) ?: return@runCatching
            val base = File(context.filesDir, "story_clips").canonicalPath + File.separator
            if (file.canonicalPath.startsWith(base)) file.delete()
        }
    }

    fun caption(context: Context, recordId: String): String? =
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).getString("caption:$recordId", null)

    fun saveCaption(context: Context, recordId: String, caption: String) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .edit().putString("caption:$recordId", caption).apply()
    }

    /** Confirm the current draft on disk when the driver taps Save. */
    fun saveDraft(context: Context, recordId: String): Boolean {
        val array = JSONArray().apply { clips(context, recordId).forEach { put(it.toJson()) } }
        return context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit()
            .putString("clips:$recordId", array.toString())
            .commit()
    }

    private fun saveClips(context: Context, recordId: String, clips: List<StoryClip>) {
        val array = JSONArray().apply { clips.forEach { put(it.toJson()) } }
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .edit().putString("clips:$recordId", array.toString()).apply()
    }

    private fun clipDirectory(context: Context, recordId: String): File {
        val digest = MessageDigest.getInstance("SHA-256").digest(recordId.toByteArray())
        val safeId = digest.take(12).joinToString("") { "%02x".format(it) }
        return File(context.filesDir, "story_clips/$safeId").apply { mkdirs() }
    }

    private fun videoDuration(context: Context, uri: Uri): Long = runCatching {
        MediaMetadataRetriever().let { retriever ->
            try {
                retriever.setDataSource(context, uri)
                retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)?.toLongOrNull()
                    ?.coerceAtLeast(1_000) ?: 10_000L
            } finally { retriever.release() }
        }
    }.getOrDefault(10_000L)

    private fun StoryClip.toJson() = JSONObject().apply {
        put("id", id); put("uri", uri.toString()); put("mime", mimeType)
        put("duration_ms", durationMs); put("start_ms", startMs); put("end_ms", endMs)
    }

    private fun JSONObject.toClip(): StoryClip {
        val duration = optLong("duration_ms", PHOTO_DURATION_MS).coerceAtLeast(1_000)
        val start = optLong("start_ms", 0).coerceIn(0, duration - 1)
        val end = optLong("end_ms", duration).coerceIn(start + 1, duration)
        return StoryClip(getString("id"), Uri.parse(getString("uri")), optString("mime", "image/*"), duration, start, end)
    }
}
