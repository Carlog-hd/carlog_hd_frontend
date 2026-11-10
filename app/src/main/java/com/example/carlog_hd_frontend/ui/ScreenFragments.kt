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
import com.example.carlog_hd_frontend.ui.map.CarlogRoutes
import java.util.Locale

interface DriveActions {
    fun startDemoDrive(followCourse: Boolean = false)
    fun toggleDemoDrive()
    fun finishDemoDrive()
    fun returnHome()
    fun showCourses()
}

/** Home screen with a runnable, local-only guidance simulation. */
class HomeFragment : Fragment(R.layout.fragment_home) {
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val records = DriveHistoryStore.all(requireContext())
        view.findViewById<TextView>(R.id.statSavedDrives).text = records.size.toString()
        view.findViewById<TextView>(R.id.statTitleCollection).text = if (records.isEmpty()) "0 / 6" else "1 / 6"
        view.findViewById<TextView>(R.id.myDriverTitle).setText(
            if (records.isEmpty()) R.string.home_no_title else R.string.title_firefly_name,
        )
        view.findViewById<View>(R.id.btnStartGuidance).setOnClickListener {
            (activity as? DriveActions)?.startDemoDrive(followCourse = false)
        }
        view.findViewById<View>(R.id.btnOpenCourse).apply {
            isEnabled = true
            alpha = 1f
            setOnClickListener { (activity as? DriveActions)?.showCourses() }
        }
    }
}

/** Live drive simulation. Vehicle and navigation signals are generated locally for emulator demos. */
class DriveFragment : Fragment(R.layout.fragment_drive) {
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        view.findViewById<View>(R.id.btnPauseResume).setOnClickListener {
            (activity as? DriveActions)?.toggleDemoDrive()
        }
        view.findViewById<View>(R.id.btnFinishTrip).setOnClickListener {
            (activity as? DriveActions)?.finishDemoDrive()
        }
    }

    fun renderSession(elapsedSeconds: Int, durationSeconds: Int, paused: Boolean, friendCourse: Boolean) {
        val view = view ?: return
        val progress = (elapsedSeconds.toFloat() / durationSeconds).coerceIn(0f, 1f)
        val distance = (if (friendCourse) 167.0 else 60.4) * progress
        val speed = if (paused) 0 else 35 + elapsedSeconds * 7 % 31
        val route = if (friendCourse) CarlogRoutes.MINJI else CarlogRoutes.MINE

        view.findViewById<com.example.carlog_hd_frontend.ui.map.RouteMapView>(R.id.driveMap).apply {
            this.route = route
            this.progress = progress
            showCar = true
            showTrack = true
        }
        view.findViewById<TextView>(R.id.etaDest).text = getString(
            if (friendCourse) R.string.course_dest_sample else R.string.home_dest_name_sample,
        )
        val simulatedSeconds = elapsedSeconds * 90
        view.findViewById<TextView>(R.id.driveTime).text = String.format(
            Locale.getDefault(), "%02d:%02d:%02d", simulatedSeconds / 3_600,
            simulatedSeconds / 60 % 60, simulatedSeconds % 60,
        )
        view.findViewById<TextView>(R.id.driveDistance).text = String.format(Locale.getDefault(), "%.1f", distance)
        view.findViewById<TextView>(R.id.driveAvgSpeed).text = speed.toString()
        view.findViewById<TextView>(R.id.nextDistance).text = if (progress < 0.55f) "1.2 km" else "300 m"
        view.findViewById<TextView>(R.id.nextTurn).text = if (progress < 0.55f) {
            getString(R.string.drive_sim_turn_first)
        } else {
            getString(R.string.drive_sim_turn_last)
        }
        view.findViewById<TextView>(R.id.driveProgressText).text = getString(
            R.string.drive_sim_progress, (progress * 100).toInt(),
        )
        view.findViewById<TextView>(R.id.recordPill).apply {
            setText(if (paused) R.string.drive_paused else R.string.drive_recording_pill)
            if (paused) setBackgroundResource(R.drawable.bg_pill_gold) else setBackgroundResource(R.drawable.bg_pill)
        }
        view.findViewById<com.google.android.material.button.MaterialButton>(R.id.btnPauseResume)
            .setText(if (paused) R.string.drive_resume else R.string.drive_pause)
        view.findViewById<TextView>(R.id.challengeStatus).text = getString(
            if (progress >= 0.65f) R.string.drive_sim_event_done else R.string.drive_sim_event_pending,
        )
        view.findViewById<View>(R.id.challengeBar).apply {
            val params = layoutParams as? LinearLayout.LayoutParams ?: return@apply
            params.weight = progress.coerceAtLeast(0.02f)
            layoutParams = params
        }
    }
}

/** The sample friend course is selectable and can start a separate simulated drive. */
class CourseFragment : Fragment(R.layout.fragment_course) {
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        view.findViewById<View>(R.id.btnFollowCourse).setOnClickListener {
            (activity as? DriveActions)?.startDemoDrive(followCourse = true)
        }
    }
}

/** Result of the last locally persisted demo drive. */
class TripResultFragment : Fragment(R.layout.fragment_trip_result) {
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val args = requireArguments()
        view.findViewById<TextView>(R.id.resultDestination).text = args.getString(ARG_DESTINATION).orEmpty()
        view.findViewById<TextView>(R.id.resultDistance).text = String.format(
            Locale.getDefault(), "%.1f km", args.getDouble(ARG_DISTANCE),
        )
        view.findViewById<TextView>(R.id.resultDuration).text = getString(
            R.string.result_duration_format, args.getInt(ARG_DURATION) / 60, args.getInt(ARG_DURATION) % 60,
        )
        view.findViewById<TextView>(R.id.resultAverage).text = getString(
            R.string.result_speed_format, args.getInt(ARG_AVERAGE),
        )
        view.findViewById<TextView>(R.id.resultEvents).text = getString(
            if (args.getBoolean(ARG_FOLLOWED_COURSE)) R.string.result_course_events else R.string.result_home_events,
        )
        view.findViewById<View>(R.id.btnResultHome).setOnClickListener {
            (activity as? DriveActions)?.returnHome()
        }
    }

    companion object {
        private const val ARG_DESTINATION = "destination"
        private const val ARG_DISTANCE = "distance"
        private const val ARG_DURATION = "duration"
        private const val ARG_AVERAGE = "average"
        private const val ARG_FOLLOWED_COURSE = "followed_course"

        fun newInstance(record: DriveRecord) = TripResultFragment().apply {
            arguments = Bundle().apply {
                putString(ARG_DESTINATION, record.destination)
                putDouble(ARG_DISTANCE, record.distanceKm)
                putInt(ARG_DURATION, record.durationSeconds)
                putInt(ARG_AVERAGE, record.averageSpeedKmh)
                putBoolean(ARG_FOLLOWED_COURSE, record.followedFriendCourse)
            }
        }
    }
}

/** Titles are still demo data; the first successful simulation awards a sample title. */
class TitlesFragment : Fragment(R.layout.fragment_titles) {

    private data class Title(
        @StringRes val name: Int,
        @StringRes val rule: Int,
        @StringRes val data: Int,
        val earned: Boolean,
    )

    private val titles by lazy {
        val hasCompletedDrive = DriveHistoryStore.all(requireContext()).isNotEmpty()
        listOf(
            Title(R.string.title_firefly_name, R.string.title_firefly_rule, R.string.title_firefly_data, earned = hasCompletedDrive),
            Title(R.string.title_cloud_name, R.string.title_cloud_rule, R.string.title_cloud_data, earned = false),
            Title(R.string.title_cruise_name, R.string.title_cruise_rule, R.string.title_cruise_data, earned = false),
            Title(R.string.title_rain_name, R.string.title_rain_rule, R.string.title_rain_data, earned = false),
            Title(R.string.title_night_name, R.string.title_night_rule, R.string.title_night_data, earned = false),
            Title(R.string.title_follower_name, R.string.title_follower_rule, R.string.title_follower_data, earned = false),
        )
    }

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
