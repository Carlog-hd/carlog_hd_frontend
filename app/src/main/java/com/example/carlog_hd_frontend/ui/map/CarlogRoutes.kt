package com.example.carlog_hd_frontend.ui.map

import android.graphics.PointF

/**
 * 시연용 가상 경로 (carlog-demo.html ROUTES). 좌표는 700×420 지도 기준입니다.
 *
 * @param stopAt 경로상 정차 지점 비율(0~1). 없으면 null
 * @param waypoints 경유지 위치 비율 목록(0~1). 지도에 1, 2, 3… 번호로 표시
 */
data class CarlogRoute(
    val pathData: String,
    val start: PointF,
    val end: PointF,
    val stopAt: Float? = null,
    val waypoints: List<Float> = emptyList(),
)

object CarlogRoutes {
    /** 서울 마포구 → 남이섬 선착장 (가평휴게소 정차) */
    val MINE = CarlogRoute(
        pathData = "M60,360 C140,330 170,290 230,250 S330,220 390,180 C440,150 470,170 520,130 S600,80 640,60",
        start = PointF(60f, 360f),
        end = PointF(640f, 60f),
        stopAt = 0.866f,
    )

    /** 민지 님 · 북한강 따라 가평까지 (경유지 3곳) */
    val MINJI = CarlogRoute(
        pathData = "M70,370 C140,340 170,300 220,270 S300,220 340,200 S430,160 470,130 S560,70 640,50",
        start = PointF(70f, 370f),
        end = PointF(640f, 50f),
        waypoints = listOf(0.25f, 0.5f, 0.75f),
    )

    /** attrs.xml carlogRoute enum 값 순서와 같아야 합니다. */
    fun byIndex(index: Int): CarlogRoute = if (index == 1) MINJI else MINE

    const val RIVER = "M-20,300 C120,270 220,330 330,290 S520,200 720,230"
}
