package com.example.carlog_hd_frontend.ui

import android.content.Context
import com.example.carlog_hd_frontend.ui.map.CarlogRoute
import com.example.carlog_hd_frontend.ui.map.CarlogRoutes

data class DemoCourse(
    val id: String,
    val name: String,
    val author: String,
    val category: String,
    val description: String,
    val distanceKm: Double,
    val averageSpeedKmh: Int,
    val duration: String,
    val durationMinutes: Int,
    val destination: String,
    val waypoints: List<String>,
    val route: CarlogRoute,
)

object DemoCourses {
    const val FILTER_ALL = "all"
    const val FILTER_RIVER = "river"
    const val FILTER_COAST = "coast"
    const val FILTER_NIGHT = "night"
    const val FILTER_SAVED = "saved"

    val all = listOf(
        DemoCourse(
            id = "north-river",
            name = "북한강 따라 가평까지",
            author = "민지",
            category = FILTER_RIVER,
            description = "토요일 오후 · 맑음 · 부드러운 구름 드라이버",
            distanceKm = 167.0,
            averageSpeedKmh = 74,
            duration = "2시간 14분",
            durationMinutes = 134,
            destination = "가평 자라섬",
            waypoints = listOf("미사리 조정경기장", "두물머리", "청평호반"),
            route = CarlogRoutes.MINJI,
        ),
        DemoCourse(
            id = "east-coast",
            name = "양양에서 강릉 해안도로",
            author = "준호",
            category = FILTER_COAST,
            description = "동해 바다와 휴게소를 잇는 해안 코스",
            distanceKm = 132.4,
            averageSpeedKmh = 71,
            duration = "1시간 52분",
            durationMinutes = 112,
            destination = "강릉 안목해변",
            waypoints = listOf("낙산사 입구", "주문진항", "경포호"),
            route = CarlogRoutes.COAST,
        ),
        DemoCourse(
            id = "seoul-night",
            name = "성수동에서 반포 한강 야경",
            author = "소연",
            category = FILTER_NIGHT,
            description = "금요일 밤 · 도심 야경 드라이브",
            distanceKm = 18.6,
            averageSpeedKmh = 23,
            duration = "48분",
            durationMinutes = 48,
            destination = "반포 한강공원",
            waypoints = listOf("서울숲", "성수대교", "반포대교"),
            route = CarlogRoutes.NIGHT,
        ),
    )

    fun byId(id: String?): DemoCourse? = all.firstOrNull { it.id == id }
}

/** Saved-course state is local to this emulator until a backend repository is connected. */
object SavedCourseStore {
    private const val PREFS = "carlog_demo"
    private const val KEY_SAVED_COURSES = "saved_course_ids"

    fun ids(context: Context): Set<String> = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        .getStringSet(KEY_SAVED_COURSES, emptySet())
        ?.toSet()
        .orEmpty()

    fun contains(context: Context, courseId: String): Boolean = courseId in ids(context)

    fun toggle(context: Context, courseId: String): Boolean {
        val saved = ids(context).toMutableSet()
        val isNowSaved = if (courseId in saved) {
            saved.remove(courseId)
            false
        } else {
            saved.add(courseId)
            true
        }
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .edit()
            .putStringSet(KEY_SAVED_COURSES, saved)
            .apply()
        return isNowSaved
    }
}
