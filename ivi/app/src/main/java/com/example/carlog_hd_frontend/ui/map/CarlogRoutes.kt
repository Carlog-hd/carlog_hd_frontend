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

    /** Sample coastal route used by the IVI course discovery simulator. */
    val COAST = CarlogRoute(
        pathData = "M55,350 C130,330 175,285 225,270 S300,305 355,250 C420,185 465,220 505,165 S585,105 650,58",
        start = PointF(55f, 350f),
        end = PointF(650f, 58f),
        waypoints = listOf(0.28f, 0.55f, 0.78f),
    )

    /** Sample city night route used by the IVI course discovery simulator. */
    val NIGHT = CarlogRoute(
        pathData = "M58,70 C160,95 165,180 250,190 S360,105 405,175 C455,250 520,200 548,275 S610,330 650,355",
        start = PointF(58f, 70f),
        end = PointF(650f, 355f),
        waypoints = listOf(0.22f, 0.5f, 0.76f),
    )

    val LAKE = CarlogRoute(
        pathData = "M65,325 C150,365 205,230 275,245 S365,330 430,205 S565,130 635,80",
        start = PointF(65f, 325f), end = PointF(635f, 80f),
        waypoints = listOf(0.24f, 0.5f, 0.76f),
    )
    val SUNSET = CarlogRoute(
        pathData = "M55,350 C135,275 180,320 255,260 S345,180 415,225 S565,110 645,65",
        start = PointF(55f, 350f), end = PointF(645f, 65f),
        waypoints = listOf(0.26f, 0.5f, 0.78f),
    )
    val BRIDGE = CarlogRoute(
        pathData = "M60,90 C170,95 175,275 285,260 S380,110 475,160 S580,300 650,345",
        start = PointF(60f, 90f), end = PointF(650f, 345f),
        waypoints = listOf(0.23f, 0.5f, 0.75f),
    )
    val FOREST = CarlogRoute(
        pathData = "M60,345 C145,320 125,205 225,210 S305,320 390,235 S510,165 645,60",
        start = PointF(60f, 345f), end = PointF(645f, 60f),
        waypoints = listOf(0.2f, 0.48f, 0.75f),
    )
    val ISLAND = CarlogRoute(
        pathData = "M70,130 C160,60 240,95 250,190 S305,320 415,325 S545,205 640,105",
        start = PointF(70f, 130f), end = PointF(640f, 105f),
        waypoints = listOf(0.25f, 0.5f, 0.75f),
    )
    val DUSK = CarlogRoute(
        pathData = "M60,300 C135,270 175,350 245,285 S335,180 405,210 S545,95 650,105",
        start = PointF(60f, 300f), end = PointF(650f, 105f),
        waypoints = listOf(0.24f, 0.52f, 0.78f),
    )

    /** attrs.xml carlogRoute enum 값 순서와 같아야 합니다. */
    fun byIndex(index: Int): CarlogRoute = if (index == 1) MINJI else MINE

    const val RIVER = "M-20,300 C120,270 220,330 330,290 S520,200 720,230"
}
