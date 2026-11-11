package com.example.carlog_hd_frontend.ui

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject

data class DriveRecord(
    val id: String,
    val destination: String,
    val distanceKm: Double,
    val durationSeconds: Int,
    val averageSpeedKmh: Int,
    val followedFriendCourse: Boolean,
    val completedAt: Long,
    val likesReceived: Int = 0,
    val courseId: String? = null,
)

/** Small local store for repeatable emulator demos; replace with the API repository later. */
object DriveHistoryStore {
    private const val PREFS = "carlog_demo"
    private const val KEY_RECORDS = "drive_records"
    private const val MAX_RECORDS = 50

    fun all(context: Context): List<DriveRecord> {
        val json = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .getString(KEY_RECORDS, "[]") ?: "[]"
        return runCatching {
            val array = JSONArray(json)
            (0 until array.length()).map { index -> array.getJSONObject(index).toRecord() }
                .sortedByDescending { it.completedAt }
        }.getOrDefault(emptyList())
    }

    fun save(context: Context, record: DriveRecord) {
        val records = (listOf(record) + all(context).filterNot { it.id == record.id })
            .take(MAX_RECORDS)
        persist(context, records)
    }

    fun merge(context: Context, imported: List<DriveRecord>): Int {
        val current = all(context)
        val currentIds = current.mapTo(mutableSetOf()) { it.id }
        val additions = imported.distinctBy { it.id }.filterNot { it.id in currentIds }
        persist(context, (current + additions).sortedByDescending { it.completedAt }.take(MAX_RECORDS))
        return additions.size
    }

    private fun persist(context: Context, records: List<DriveRecord>) {
        val array = JSONArray()
        records.forEach { array.put(it.toJson()) }
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .edit()
            .putString(KEY_RECORDS, array.toString())
            .apply()
    }

    private fun DriveRecord.toJson() = JSONObject().apply {
        put("id", id)
        put("destination", destination)
        put("distance_km", distanceKm)
        put("duration_seconds", durationSeconds)
        put("average_speed_kmh", averageSpeedKmh)
        put("followed_friend_course", followedFriendCourse)
        put("completed_at", completedAt)
        put("likes_received", likesReceived)
        put("course_id", courseId)
    }

    private fun JSONObject.toRecord() = DriveRecord(
        id = getString("id"),
        destination = getString("destination"),
        distanceKm = getDouble("distance_km"),
        durationSeconds = getInt("duration_seconds"),
        averageSpeedKmh = getInt("average_speed_kmh"),
        followedFriendCourse = getBoolean("followed_friend_course"),
        completedAt = getLong("completed_at"),
        likesReceived = optInt("likes_received", 0).coerceAtLeast(0),
        courseId = optString("course_id").takeIf(String::isNotBlank),
    )
}
