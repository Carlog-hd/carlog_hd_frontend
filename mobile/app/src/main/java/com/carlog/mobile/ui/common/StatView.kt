package com.carlog.mobile.ui.common

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.withStyledAttributes
import com.carlog.mobile.R

/**
 * 통계 칸 (데모 .stat): 큰 값 + 작은 라벨.
 *
 * ```xml
 * <LinearLayout style="@style/Widget.Carlog.StatsRow">
 *     <com.carlog.mobile.ui.common.StatView
 *         style="@style/Widget.Carlog.Stat"
 *         app:statValue="167km"
 *         app:statLabel="거리" />
 * </LinearLayout>
 * ```
 */
class StatView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
) : LinearLayout(context, attrs, defStyleAttr) {

    private val valueView: TextView
    private val labelView: TextView

    /** 통계 값 (예: 167km) */
    var value: CharSequence?
        get() = valueView.text
        set(text) {
            valueView.text = text
        }

    /** 통계 라벨 (예: 거리) */
    var label: CharSequence?
        get() = labelView.text
        set(text) {
            labelView.text = text
        }

    init {
        orientation = VERTICAL
        LayoutInflater.from(context).inflate(R.layout.view_stat, this, true)
        valueView = findViewById(R.id.tv_stat_value)
        labelView = findViewById(R.id.tv_stat_label)

        context.withStyledAttributes(attrs, R.styleable.StatView, defStyleAttr) {
            value = getText(R.styleable.StatView_statValue)
            label = getText(R.styleable.StatView_statLabel)
        }
    }
}
