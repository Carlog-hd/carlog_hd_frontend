package com.carlog.mobile.ui.common

import android.content.Context
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.content.withStyledAttributes
import androidx.core.view.isVisible
import com.carlog.mobile.R

/**
 * 지도 박스 (데모 .mapbox + .maptag): 지도 이미지 + 왼쪽 위 장소 라벨.
 * 실제 지도 연동 전까지는 기본으로 img_map_placeholder 를 표시한다.
 *
 * ```xml
 * <com.carlog.mobile.ui.common.MapBoxView
 *     android:layout_width="match_parent"
 *     android:layout_height="180dp"
 *     app:mapTagText="7번 국도 새벽 바닷길" />
 * ```
 * mapTagText 를 지정하지 않으면 라벨은 숨겨진다.
 */
class MapBoxView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
) : FrameLayout(context, attrs, defStyleAttr) {

    private val mapView: ImageView
    private val tagView: TextView

    /** 지도 이미지 */
    var mapImage: Drawable?
        get() = mapView.drawable
        set(drawable) {
            mapView.setImageDrawable(drawable)
        }

    /** 장소 라벨 (null 또는 빈 값이면 숨김) */
    var tagText: CharSequence?
        get() = tagView.text
        set(text) {
            tagView.text = text
            tagView.isVisible = !text.isNullOrEmpty()
        }

    init {
        LayoutInflater.from(context).inflate(R.layout.view_map_box, this, true)
        mapView = findViewById(R.id.iv_map)
        tagView = findViewById(R.id.tv_map_tag)
        setBackgroundColor(ContextCompat.getColor(context, R.color.cl_map_land))

        context.withStyledAttributes(attrs, R.styleable.MapBoxView, defStyleAttr) {
            mapImage = getDrawable(R.styleable.MapBoxView_mapImage)
                ?: ContextCompat.getDrawable(context, R.drawable.img_map_placeholder)
            tagText = getText(R.styleable.MapBoxView_mapTagText)
        }
    }
}
