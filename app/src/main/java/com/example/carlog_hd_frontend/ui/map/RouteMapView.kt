package com.example.carlog_hd_frontend.ui.map

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.graphics.PathMeasure
import android.graphics.RectF
import android.graphics.Typeface
import android.util.AttributeSet
import android.view.View
import androidx.core.content.ContextCompat
import androidx.core.graphics.PathParser
import com.example.carlog_hd_frontend.R
import kotlin.math.min

/**
 * 시연용 경로 지도 (carlog-demo.html mapSVG 의 다크 버전).
 * 700×420 지도를 잘리지 않게 가운데 맞춤으로 그리고, 남는 영역은 바탕색으로 채웁니다.
 * 실제 Pleos 내비 지도가 연동되면 교체될 임시 뷰입니다.
 */
class RouteMapView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
) : View(context, attrs, defStyleAttr) {

    var route: CarlogRoute = CarlogRoutes.MINE
        set(value) {
            field = value
            measureRoute()
            invalidate()
        }

    /** 지나온 구간 비율 (0~1) */
    var progress: Float = 1f
        set(value) {
            field = value.coerceIn(0f, 1f)
            invalidate()
        }

    var showTrack = false
        set(value) { field = value; invalidate() }
    var showCar = false
        set(value) { field = value; invalidate() }
    var hideStart = false
        set(value) { field = value; invalidate() }
    var showStop = true
        set(value) { field = value; invalidate() }

    private var cornerRadius = 0f

    private val routePath = Path()
    private val riverPath: Path = PathParser.createPathFromPathData(CarlogRoutes.RIVER)
    private val segment = Path()
    private val measure = PathMeasure()
    private var routeLength = 0f
    private val pos = FloatArray(2)
    private val clip = Path()

    private val ground = color(R.color.cl_map_ground)
    private val accent = color(R.color.cl_aqua)
    private val gold = color(R.color.cl_gold)

    private val stroke = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeCap = Paint.Cap.ROUND
        strokeJoin = Paint.Join.ROUND
    }
    private val fill = Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.FILL }
    private val label = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        textAlign = Paint.Align.CENTER
        textSize = 15f
        typeface = Typeface.DEFAULT_BOLD
        color = color(R.color.cl_rail)
    }

    init {
        context.obtainStyledAttributes(attrs, R.styleable.RouteMapView, defStyleAttr, 0).apply {
            try {
                route = CarlogRoutes.byIndex(getInt(R.styleable.RouteMapView_carlogRoute, 0))
                progress = getFloat(R.styleable.RouteMapView_carlogProgress, 1f)
                showTrack = getBoolean(R.styleable.RouteMapView_carlogShowTrack, false)
                showCar = getBoolean(R.styleable.RouteMapView_carlogShowCar, false)
                hideStart = getBoolean(R.styleable.RouteMapView_carlogHideStart, false)
                showStop = getBoolean(R.styleable.RouteMapView_carlogShowStop, true)
                cornerRadius = getDimension(
                    R.styleable.RouteMapView_carlogCornerRadius,
                    resources.displayMetrics.density * 40f,
                )
            } finally {
                recycle()
            }
        }
        measureRoute()
    }

    private fun color(id: Int) = ContextCompat.getColor(context, id)

    private fun measureRoute() {
        routePath.set(PathParser.createPathFromPathData(route.pathData))
        measure.setPath(routePath, false)
        routeLength = measure.length
    }

    private fun pointAt(t: Float): FloatArray {
        measure.getPosTan(routeLength * t, pos, null)
        return pos
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        clip.reset()
        clip.addRoundRect(RectF(0f, 0f, w.toFloat(), h.toFloat()), cornerRadius, cornerRadius, Path.Direction.CW)
    }

    override fun onDraw(canvas: Canvas) {
        canvas.save()
        canvas.clipPath(clip)
        canvas.drawColor(ground)

        // 700×420 지도를 가운데 맞춤 (SVG xMidYMid meet)
        val scale = min(width / MAP_W, height / MAP_H)
        canvas.translate((width - MAP_W * scale) / 2f, (height - MAP_H * scale) / 2f)
        canvas.scale(scale, scale)

        drawStroke(canvas, riverPath, color(R.color.cl_map_river), 32f, Paint.Cap.BUTT)
        val road = color(R.color.cl_map_road)
        drawLine(canvas, 0f, 120f, 700f, 170f, road, 8f)
        drawLine(canvas, 200f, 0f, 240f, 420f, road, 8f)
        drawLine(canvas, 480f, 0f, 450f, 420f, road, 7f)

        if (showTrack) drawStroke(canvas, routePath, color(R.color.cl_map_track), 14f)

        if (progress > 0f) {
            segment.reset()
            measure.getSegment(0f, routeLength * progress, segment, true)
            drawStroke(canvas, segment, accent, 9f)
        }

        route.waypoints.forEachIndexed { i, t ->
            val p = pointAt(t)
            drawDot(canvas, p[0], p[1], 15f, accent)
            canvas.drawText((i + 1).toString(), p[0], p[1] + 5f, label)
        }

        val stopAt = route.stopAt
        if (stopAt != null && showStop) {
            val p = pointAt(stopAt)
            drawDot(canvas, p[0], p[1], 9f, gold)
        }

        if (!hideStart) drawDot(canvas, route.start.x, route.start.y, 9f, color(R.color.cl_fg))
        drawDot(canvas, route.end.x, route.end.y, 10f, gold)
        if (hideStart) drawDot(canvas, route.start.x, route.start.y, 58f, color(R.color.cl_map_mask))

        if (showCar) {
            val p = pointAt(progress)
            drawDot(canvas, p[0], p[1], 22f, Color.argb(56, Color.red(accent), Color.green(accent), Color.blue(accent)))
            drawDot(canvas, p[0], p[1], 10f, color(R.color.cl_fg))
            stroke.color = accent
            stroke.strokeWidth = 4f
            canvas.drawCircle(p[0], p[1], 10f, stroke)
        }

        canvas.restore()
    }

    private fun drawStroke(canvas: Canvas, path: Path, c: Int, width: Float, cap: Paint.Cap = Paint.Cap.ROUND) {
        stroke.color = c
        stroke.strokeWidth = width
        stroke.strokeCap = cap
        canvas.drawPath(path, stroke)
        stroke.strokeCap = Paint.Cap.ROUND
    }

    private fun drawLine(canvas: Canvas, x1: Float, y1: Float, x2: Float, y2: Float, c: Int, width: Float) {
        stroke.color = c
        stroke.strokeWidth = width
        stroke.strokeCap = Paint.Cap.BUTT
        canvas.drawLine(x1, y1, x2, y2, stroke)
        stroke.strokeCap = Paint.Cap.ROUND
    }

    private fun drawDot(canvas: Canvas, x: Float, y: Float, r: Float, c: Int) {
        fill.color = c
        canvas.drawCircle(x, y, r, fill)
    }

    private companion object {
        const val MAP_W = 700f
        const val MAP_H = 420f
    }
}
