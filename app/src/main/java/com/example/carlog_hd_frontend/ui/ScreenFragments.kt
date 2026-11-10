package com.example.carlog_hd_frontend.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import androidx.annotation.StringRes
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.example.carlog_hd_frontend.R

/** 홈 탭. 레이아웃만 표시합니다. */
class HomeFragment : Fragment(R.layout.fragment_home)

/** 주행 탭. 주행 중 상태의 샘플 레이아웃입니다. */
class DriveFragment : Fragment(R.layout.fragment_drive)

/** 코스 탭. 친구 코스를 받은 상태의 샘플 레이아웃입니다. */
class CourseFragment : Fragment(R.layout.fragment_course)

/** 칭호 탭. 칭호 카드 6장을 3열 그리드로 표시합니다. */
class TitlesFragment : Fragment(R.layout.fragment_titles) {

    private data class Title(
        @StringRes val name: Int,
        @StringRes val rule: Int,
        @StringRes val data: Int,
        val earned: Boolean,
    )

    // 획득 여부는 시연용 샘플 값입니다 (첫 드라이브 후 반딧불이 칭호를 받은 상태).
    private val titles = listOf(
        Title(R.string.title_firefly_name, R.string.title_firefly_rule, R.string.title_firefly_data, earned = true),
        Title(R.string.title_cloud_name, R.string.title_cloud_rule, R.string.title_cloud_data, earned = false),
        Title(R.string.title_cruise_name, R.string.title_cruise_rule, R.string.title_cruise_data, earned = false),
        Title(R.string.title_rain_name, R.string.title_rain_rule, R.string.title_rain_data, earned = false),
        Title(R.string.title_night_name, R.string.title_night_rule, R.string.title_night_data, earned = false),
        Title(R.string.title_follower_name, R.string.title_follower_rule, R.string.title_follower_data, earned = false),
    )

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val grid = view.findViewById<LinearLayout>(R.id.titleGrid)
        val inflater = LayoutInflater.from(view.context)
        val gap = resources.getDimensionPixelSize(R.dimen.title_grid_gap)

        titles.chunked(COLUMNS).forEachIndexed { rowIndex, rowTitles ->
            val row = LinearLayout(view.context).apply {
                orientation = LinearLayout.HORIZONTAL
                isBaselineAligned = false
            }
            rowTitles.forEachIndexed { i, title ->
                val card = inflater.inflate(R.layout.item_title_card, row, false)
                bind(card, title)
                (card.layoutParams as LinearLayout.LayoutParams).marginStart = if (i == 0) 0 else gap
                row.addView(card)
            }
            val params = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT,
            ).apply { topMargin = if (rowIndex == 0) 0 else gap }
            grid.addView(row, params)
        }
    }

    private fun bind(card: View, title: Title) {
        card.findViewById<TextView>(R.id.titleName).setText(title.name)
        card.findViewById<TextView>(R.id.titleRule).setText(title.rule)
        card.findViewById<TextView>(R.id.titleData).setText(title.data)
        card.findViewById<TextView>(R.id.titleState).apply {
            if (title.earned) {
                setText(R.string.titles_earned)
                setBackgroundResource(R.drawable.bg_pill_gold)
                setTextColor(ContextCompat.getColor(context, R.color.cl_gold_pill_fg))
            } else {
                setText(R.string.titles_locked)
            }
        }
        if (!title.earned) card.alpha = LOCKED_ALPHA
    }

    private companion object {
        const val COLUMNS = 3
        const val LOCKED_ALPHA = 0.38f
    }
}
