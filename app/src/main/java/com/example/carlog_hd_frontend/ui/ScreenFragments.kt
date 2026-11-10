package com.example.carlog_hd_frontend.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.StringRes
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.Fragment
import com.google.android.material.button.MaterialButton
import com.example.carlog_hd_frontend.R
import com.example.carlog_hd_frontend.ui.map.CarlogRoutes
import java.util.Locale

interface DriveActions {
    fun startDemoDrive(courseId: String? = null)
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
            (activity as? DriveActions)?.startDemoDrive(courseId = null)
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

    fun renderSession(elapsedSeconds: Int, durationSeconds: Int, paused: Boolean, course: DemoCourse?) {
        val view = view ?: return
        val progress = (elapsedSeconds.toFloat() / durationSeconds).coerceIn(0f, 1f)
        val totalDistance = course?.distanceKm ?: 60.4
        val distance = totalDistance * progress
        val route = course?.route ?: CarlogRoutes.MINE
        val durationMinutes = course?.durationMinutes ?: 67

        view.findViewById<com.example.carlog_hd_frontend.ui.map.RouteMapView>(R.id.driveMap).apply {
            this.route = route
            this.progress = progress
            showCar = true
            showTrack = true
        }
        view.findViewById<TextView>(R.id.etaDest).text = course?.destination ?: getString(R.string.home_dest_name_sample)
        view.findViewById<TextView>(R.id.etaLeft).text = String.format(
            Locale.getDefault(), "%.1f km", (totalDistance - distance).coerceAtLeast(0.0),
        )
        val arrivalMinutes = (14 * 60 + 12 + durationMinutes) % (24 * 60)
        view.findViewById<TextView>(R.id.etaTime).text = String.format(
            Locale.getDefault(), "%02d:%02d", arrivalMinutes / 60, arrivalMinutes % 60,
        )
        val simulatedSeconds = (durationMinutes * 60 * progress).toInt()
        view.findViewById<TextView>(R.id.driveTime).text = String.format(
            Locale.getDefault(), "%02d:%02d:%02d", simulatedSeconds / 3_600,
            simulatedSeconds / 60 % 60, simulatedSeconds % 60,
        )
        view.findViewById<TextView>(R.id.driveDistance).text = String.format(Locale.getDefault(), "%.1f", distance)
        view.findViewById<TextView>(R.id.driveAvgSpeed).text = (course?.averageSpeedKmh ?: 54).toString()
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
    private var selectedCourseId = DemoCourses.all.first().id
    private var selectedFilter = DemoCourses.FILTER_ALL

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        renderFilters(view)
        renderChoices(view)
        bindCourse(view, DemoCourses.byId(selectedCourseId) ?: DemoCourses.all.first())

        view.findViewById<View>(R.id.btnSaveCourse).setOnClickListener {
            val saved = SavedCourseStore.toggle(requireContext(), selectedCourseId)
            Toast.makeText(
                requireContext(),
                if (saved) R.string.course_saved_toast else R.string.course_removed_toast,
                Toast.LENGTH_SHORT,
            ).show()
            bindCourse(view, DemoCourses.byId(selectedCourseId) ?: return@setOnClickListener)
            renderChoices(view)
        }
        view.findViewById<View>(R.id.btnFollowCourse).setOnClickListener {
            (activity as? DriveActions)?.startDemoDrive(courseId = selectedCourseId)
        }
    }

    private fun renderFilters(root: View) {
        val container = root.findViewById<LinearLayout>(R.id.courseFilters)
        container.removeAllViews()
        listOf(
            DemoCourses.FILTER_ALL to R.string.course_filter_all,
            DemoCourses.FILTER_RIVER to R.string.course_filter_river,
            DemoCourses.FILTER_COAST to R.string.course_filter_coast,
            DemoCourses.FILTER_NIGHT to R.string.course_filter_night,
            DemoCourses.FILTER_SAVED to R.string.course_filter_saved,
        ).forEach { (filter, label) ->
            val selected = filter == selectedFilter
            val button = MaterialButton(requireContext()).apply {
                text = getString(label)
                isAllCaps = false
                textSize = 20f
                cornerRadius = dp(24)
                insetTop = 0
                insetBottom = 0
                minHeight = dp(60)
                minimumHeight = dp(60)
                backgroundTintList = android.content.res.ColorStateList.valueOf(
                    ContextCompat.getColor(context, if (selected) R.color.cl_lime else R.color.cl_ghost),
                )
                setTextColor(ContextCompat.getColor(context, if (selected) R.color.cl_on_lime else R.color.cl_on_ghost))
                setOnClickListener {
                    selectedFilter = filter
                    val choices = filteredCourses()
                    if (choices.isNotEmpty() && choices.none { it.id == selectedCourseId }) {
                        selectedCourseId = choices.first().id
                        bindCourse(root, choices.first())
                    }
                    renderFilters(root)
                    renderChoices(root)
                }
            }
            container.addView(button, LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                dp(60),
            ).apply { marginEnd = dp(12) })
        }
    }

    private fun renderChoices(root: View) {
        val container = root.findViewById<LinearLayout>(R.id.courseChoices)
        container.removeAllViews()
        val choices = filteredCourses()
        if (choices.isEmpty()) {
            root.findViewById<View>(R.id.courseDetails).visibility = View.GONE
            container.addView(TextView(requireContext()).apply {
                text = getString(R.string.course_empty_filter)
                setTextColor(ContextCompat.getColor(context, R.color.cl_muted))
                textSize = 22f
            })
            return
        }
        root.findViewById<View>(R.id.courseDetails).visibility = View.VISIBLE
        choices.forEach { course ->
            val card = LinearLayout(requireContext()).apply {
                orientation = LinearLayout.VERTICAL
                gravity = android.view.Gravity.CENTER_VERTICAL
                setPadding(dp(24), dp(16), dp(24), dp(16))
                background = ResourcesCompat.getDrawable(
                    resources,
                    if (course.id == selectedCourseId) R.drawable.bg_navcard else R.drawable.bg_panel,
                    requireContext().theme,
                )
                isClickable = true
                isFocusable = true
                contentDescription = getString(R.string.course_choice_format, course.name, course.distanceKm.toInt())
                setOnClickListener {
                    selectedCourseId = course.id
                    renderChoices(root)
                    bindCourse(root, course)
                }
            }
            card.addView(TextView(requireContext()).apply {
                text = course.name
                setTextColor(ContextCompat.getColor(context, R.color.cl_fg))
                textSize = 24f
                setTypeface(typeface, android.graphics.Typeface.BOLD)
                maxLines = 1
                ellipsize = android.text.TextUtils.TruncateAt.END
            })
            card.addView(TextView(requireContext()).apply {
                text = getString(R.string.course_route_meta_format, course.author, course.distanceKm.toInt(), course.waypoints.size)
                setTextColor(ContextCompat.getColor(context, R.color.cl_muted))
                textSize = 19f
                maxLines = 1
            })
            container.addView(card, LinearLayout.LayoutParams(dp(390), dp(112)).apply { marginEnd = dp(16) })
        }
    }

    private fun filteredCourses(): List<DemoCourse> {
        val savedIds = SavedCourseStore.ids(requireContext())
        return DemoCourses.all.filter { course ->
            when (selectedFilter) {
                DemoCourses.FILTER_ALL -> true
                DemoCourses.FILTER_SAVED -> course.id in savedIds
                else -> course.category == selectedFilter
            }
        }
    }

    private fun bindCourse(root: View, course: DemoCourse) {
        root.findViewById<TextView>(R.id.courseHeadline).text = "${course.author}님의 드라이브 · ${course.name}"
        root.findViewById<TextView>(R.id.courseLead).text = course.description
        root.findViewById<com.example.carlog_hd_frontend.ui.map.RouteMapView>(R.id.courseMap).apply {
            route = course.route
            progress = 1f
            showTrack = true
            hideStart = true
            contentDescription = getString(R.string.course_map_desc, course.name)
        }
        root.findViewById<TextView>(R.id.courseDistance).text = course.distanceKm.toInt().toString()
        root.findViewById<TextView>(R.id.courseTime).text = course.duration
        root.findViewById<TextView>(R.id.courseWaypoint1).text = course.waypoints.getOrElse(0) { "-" }
        root.findViewById<TextView>(R.id.courseWaypoint2).text = course.waypoints.getOrElse(1) { "-" }
        root.findViewById<TextView>(R.id.courseWaypoint3).text = course.waypoints.getOrElse(2) { "-" }
        root.findViewById<TextView>(R.id.courseDestination).text = course.destination
        root.findViewById<TextView>(R.id.courseDemoNotice).setText(R.string.course_note_dynamic)
        root.findViewById<MaterialButton>(R.id.btnSaveCourse).setText(
            if (SavedCourseStore.contains(requireContext(), course.id)) R.string.course_unsave else R.string.course_save,
        )
    }

    private fun dp(value: Int): Int = (value * resources.displayMetrics.density).toInt()
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
