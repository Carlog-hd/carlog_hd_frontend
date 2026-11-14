package com.example.carlog_hd_frontend.ui

import android.content.Context
import com.example.carlog_hd_frontend.R
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
    val legDistancesKm: List<Double>,
    val legDurationMinutes: List<Int>,
    val coverDrawable: Int,
    val route: CarlogRoute,
) {
    fun legs(): List<CourseLeg> = (waypoints + destination).mapIndexed { index, name ->
        CourseLeg(name, legDistancesKm[index], legDurationMinutes[index])
    }
}

data class CourseLeg(val destination: String, val distanceKm: Double, val durationMinutes: Int)

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
            legDistancesKm = listOf(48.0, 35.0, 42.0, 42.0),
            legDurationMinutes = listOf(39, 28, 34, 33),
            coverDrawable = R.drawable.cover_course_river,
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
            legDistancesKm = listOf(25.4, 39.0, 31.0, 37.0),
            legDurationMinutes = listOf(22, 33, 26, 31),
            coverDrawable = R.drawable.cover_course_coast,
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
            legDistancesKm = listOf(4.2, 3.6, 5.1, 5.7),
            legDurationMinutes = listOf(12, 9, 13, 14),
            coverDrawable = R.drawable.cover_course_night,
            route = CarlogRoutes.NIGHT,
        ),
        DemoCourse(
            id = "chuncheon-lake",
            name = "춘천 의암호 물길",
            author = "하린",
            category = FILTER_RIVER,
            description = "호숫가를 따라 천천히 달리는 오후 코스",
            distanceKm = 46.0,
            averageSpeedKmh = 39,
            duration = "1시간 10분",
            durationMinutes = 70,
            destination = "삼악산 호수케이블카",
            waypoints = listOf("공지천", "소양강 스카이워크", "의암호 전망대"),
            legDistancesKm = listOf(11.0, 12.0, 9.0, 14.0),
            legDurationMinutes = listOf(16, 18, 14, 22),
            coverDrawable = R.drawable.cover_course_lake,
            route = CarlogRoutes.LAKE,
        ),
        DemoCourse(
            id = "namhae-coast",
            name = "남해 바다 곁 드라이브",
            author = "지우",
            category = FILTER_COAST,
            description = "해안선과 작은 마을을 잇는 남해 코스",
            distanceKm = 72.0,
            averageSpeedKmh = 40,
            duration = "1시간 48분",
            durationMinutes = 108,
            destination = "상주은모래비치",
            waypoints = listOf("남해대교", "다랭이마을", "사천해변"),
            legDistancesKm = listOf(14.0, 21.0, 18.0, 19.0),
            legDurationMinutes = listOf(20, 31, 27, 30),
            coverDrawable = R.drawable.cover_course_sunset,
            route = CarlogRoutes.SUNSET,
        ),
        DemoCourse(
            id = "busan-night",
            name = "부산 광안대교 밤바다",
            author = "도윤",
            category = FILTER_NIGHT,
            description = "도시의 불빛과 바다가 만나는 야경 코스",
            distanceKm = 27.0,
            averageSpeedKmh = 31,
            duration = "52분",
            durationMinutes = 52,
            destination = "이기대 해안산책로",
            waypoints = listOf("해운대", "마린시티", "광안리"),
            legDistancesKm = listOf(7.0, 6.0, 8.0, 6.0),
            legDurationMinutes = listOf(13, 12, 15, 12),
            coverDrawable = R.drawable.cover_course_bridge,
            route = CarlogRoutes.BRIDGE,
        ),
        DemoCourse(
            id = "sokcho-autumn",
            name = "속초 설악산 단풍길",
            author = "예린",
            category = FILTER_RIVER,
            description = "숲과 산자락을 따라 이어지는 가을 코스",
            distanceKm = 58.0,
            averageSpeedKmh = 41,
            duration = "1시간 25분",
            durationMinutes = 85,
            destination = "설악산 소공원",
            waypoints = listOf("청초호", "영랑호", "설악동 입구"),
            legDistancesKm = listOf(12.0, 15.0, 17.0, 14.0),
            legDurationMinutes = listOf(18, 22, 25, 20),
            coverDrawable = R.drawable.cover_course_forest,
            route = CarlogRoutes.FOREST,
        ),
        DemoCourse(
            id = "yeongjong-island",
            name = "영종도 해안선 한 바퀴",
            author = "시온",
            category = FILTER_COAST,
            description = "바다와 비행기를 함께 만나는 섬 코스",
            distanceKm = 51.0,
            averageSpeedKmh = 41,
            duration = "1시간 15분",
            durationMinutes = 75,
            destination = "왕산해수욕장",
            waypoints = listOf("영종대교 전망대", "씨사이드파크", "을왕리해수욕장"),
            legDistancesKm = listOf(14.0, 13.0, 12.0, 12.0),
            legDurationMinutes = listOf(21, 19, 18, 17),
            coverDrawable = R.drawable.cover_course_island,
            route = CarlogRoutes.ISLAND,
        ),
        DemoCourse(
            id = "daejeon-sunset",
            name = "대전 갑천 노을길",
            author = "유나",
            category = FILTER_NIGHT,
            description = "강변에서 해 질 녘까지 이어지는 짧은 코스",
            distanceKm = 31.0,
            averageSpeedKmh = 34,
            duration = "55분",
            durationMinutes = 55,
            destination = "한빛탑 광장",
            waypoints = listOf("갑천변", "엑스포다리", "대전시립미술관"),
            legDistancesKm = listOf(8.0, 7.0, 9.0, 7.0),
            legDurationMinutes = listOf(14, 13, 16, 12),
            coverDrawable = R.drawable.cover_course_dusk,
            route = CarlogRoutes.DUSK,
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

    fun merge(context: Context, courseIds: Set<String>): Int {
        val validIds = courseIds.intersect(DemoCourses.all.map { it.id }.toSet())
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .edit()
            .putStringSet(KEY_SAVED_COURSES, ids(context) + validIds)
            .apply()
        return validIds.size
    }

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

/** Like state is stored locally on this emulator until the social API is connected. */
object LikedCourseStore {
    private const val PREFS = "carlog_demo"
    private const val KEY_LIKED_COURSES = "liked_course_ids"

    fun ids(context: Context): Set<String> = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        .getStringSet(KEY_LIKED_COURSES, emptySet())
        ?.toSet()
        .orEmpty()

    fun contains(context: Context, courseId: String): Boolean = courseId in ids(context)

    fun merge(context: Context, courseIds: Set<String>): Int {
        val validIds = courseIds.intersect(DemoCourses.all.map { it.id }.toSet())
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .edit()
            .putStringSet(KEY_LIKED_COURSES, ids(context) + validIds)
            .apply()
        return validIds.size
    }

    fun toggle(context: Context, courseId: String): Boolean {
        val liked = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .getStringSet(KEY_LIKED_COURSES, emptySet())
            ?.toMutableSet()
            ?: mutableSetOf()
        val isNowLiked = if (courseId in liked) {
            liked.remove(courseId)
            false
        } else {
            liked.add(courseId)
            true
        }
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .edit()
            .putStringSet(KEY_LIKED_COURSES, liked)
            .apply()
        return isNowLiked
    }
}
