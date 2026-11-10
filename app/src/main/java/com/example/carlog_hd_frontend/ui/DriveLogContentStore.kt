package com.example.carlog_hd_frontend.ui

import android.content.Context
import android.net.Uri

/** Local per-log media references and editable copy for the IVI demo. */
object DriveLogContentStore {
    private const val PREFS = "carlog_drive_log_content"

    fun media(context: Context, recordId: String): List<Uri> {
        val encoded = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .getString("media:$recordId", "")
            .orEmpty()
        return encoded.split('\n').mapNotNull { value ->
            value.takeIf(String::isNotBlank)?.let(Uri::parse)
        }
    }

    fun addMedia(context: Context, recordId: String, uris: List<Uri>): Int {
        val current = media(context, recordId)
        val merged = (current + uris).distinctBy(Uri::toString).take(30)
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .edit()
            .putString("media:$recordId", merged.joinToString("\n") { it.toString() })
            .apply()
        return merged.size - current.size
    }

    fun caption(context: Context, recordId: String): String? =
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).getString("caption:$recordId", null)

    fun saveCaption(context: Context, recordId: String, caption: String) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .edit()
            .putString("caption:$recordId", caption)
            .apply()
    }
}
