package com.example.carlog_hd_frontend.ui

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject

data class BackupImportSummary(
    val driveRecords: Int,
    val savedCourses: Int,
    val likedCourses: Int,
)

/** Local JSON backup for the CARLOG data currently stored on this IVI emulator. */
object CarlogBackupStore {
    private const val FORMAT = "carlog-ivi-backup"
    private const val VERSION = 1
    private const val SETTINGS_PREFS = "carlog_my_settings"

    fun export(context: Context): String {
        val settings = context.getSharedPreferences(SETTINGS_PREFS, Context.MODE_PRIVATE)
        return JSONObject().apply {
            put("format", FORMAT)
            put("version", VERSION)
            put("exported_at", System.currentTimeMillis())
            put("drive_records", JSONArray().apply {
                DriveHistoryStore.all(context).forEach { record ->
                    put(JSONObject().apply {
                        put("id", record.id)
                        put("destination", record.destination)
                        put("distance_km", record.distanceKm)
                        put("duration_seconds", record.durationSeconds)
                        put("average_speed_kmh", record.averageSpeedKmh)
                        put("followed_friend_course", record.followedFriendCourse)
                        put("completed_at", record.completedAt)
                        put("likes_received", record.likesReceived)
                        put("course_id", record.courseId)
                    })
                }
            })
            put("saved_course_ids", JSONArray().apply { SavedCourseStore.ids(context).sorted().forEach(::put) })
            put("liked_course_ids", JSONArray().apply { LikedCourseStore.ids(context).sorted().forEach(::put) })
            put("settings", JSONObject().apply {
                put("auto_record", settings.getBoolean("auto_record", true))
                put("resume_after_stop", settings.getBoolean("resume_after_stop", true))
            })
        }.toString(2)
    }

    fun import(context: Context, contents: String): BackupImportSummary {
        val root = JSONObject(contents)
        require(root.getString("format") == FORMAT) { "CARLOG 백업 파일이 아닙니다." }
        require(root.getInt("version") == VERSION) { "지원하지 않는 백업 파일 버전입니다." }

        val recordsJson = root.getJSONArray("drive_records")
        require(recordsJson.length() <= 5_000) { "기록 개수가 너무 많습니다." }
        val records = (0 until recordsJson.length()).map { index ->
            val item = recordsJson.getJSONObject(index)
            val distance = item.getDouble("distance_km")
            val duration = item.getInt("duration_seconds")
            val speed = item.getInt("average_speed_kmh")
            require(distance.isFinite() && distance >= 0.0) { "주행 거리 데이터가 올바르지 않습니다." }
            require(duration >= 0 && speed >= 0) { "주행 기록 값이 올바르지 않습니다." }
            DriveRecord(
                id = item.getString("id").also { require(it.isNotBlank()) },
                destination = item.getString("destination"),
                distanceKm = distance,
                durationSeconds = duration,
                averageSpeedKmh = speed,
                followedFriendCourse = item.optBoolean("followed_friend_course", false),
                completedAt = item.getLong("completed_at"),
                likesReceived = item.optInt("likes_received", 0).coerceAtLeast(0),
                courseId = item.optString("course_id").takeIf(String::isNotBlank),
            )
        }
        val saved = root.getJSONArray("saved_course_ids").toStringSet()
        val liked = root.getJSONArray("liked_course_ids").toStringSet()
        val settings = root.getJSONObject("settings")

        val importedRecords = DriveHistoryStore.merge(context, records)
        val importedSaved = SavedCourseStore.merge(context, saved)
        val importedLiked = LikedCourseStore.merge(context, liked)
        context.getSharedPreferences(SETTINGS_PREFS, Context.MODE_PRIVATE)
            .edit()
            .putBoolean("auto_record", settings.optBoolean("auto_record", true))
            .putBoolean("resume_after_stop", settings.optBoolean("resume_after_stop", true))
            .apply()

        return BackupImportSummary(importedRecords, importedSaved, importedLiked)
    }

    private fun JSONArray.toStringSet(): Set<String> = (0 until length())
        .mapNotNull { index -> optString(index).takeIf(String::isNotBlank) }
        .toSet()
}
