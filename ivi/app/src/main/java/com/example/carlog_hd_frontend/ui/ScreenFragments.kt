package com.example.carlog_hd_frontend.ui

import android.os.Bundle
import android.content.Intent
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.widget.FrameLayout
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import android.graphics.drawable.GradientDrawable
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.annotation.StringRes
import androidx.core.content.ContextCompat
import androidx.core.widget.NestedScrollView
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
    fun showRecords()
    fun showDriveLog(record: DriveRecord)
    fun returnToMy()
}

/** Home screen with a runnable, local-only guidance simulation. */
class HomeFragment : Fragment(R.layout.fragment_home) {
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val records = DriveHistoryStore.all(requireContext())
        val latest = records.firstOrNull()
        view.findViewById<TextView>(R.id.statSavedDrives).text = records.size.toString()
        view.findViewById<TextView>(R.id.statTitleCollection).text = if (records.isEmpty()) "0 / 6" else "1 / 6"
        view.findViewById<TextView>(R.id.myDriverTitle).setText(
            if (records.isEmpty()) R.string.home_no_title else R.string.title_firefly_name,
        )
        if (latest != null) {
            view.findViewById<TextView>(R.id.recentBadge).text =
                java.text.SimpleDateFormat("M월의 드라이브", Locale.KOREAN).format(java.util.Date(latest.completedAt))
            view.findViewById<TextView>(R.id.recentTitle).text = "${latest.destination}까지의 드라이브"
            view.findViewById<TextView>(R.id.recentMeta).text = getString(
                R.string.home_recent_meta, latest.destination, latest.distanceKm, latest.durationSeconds / 60,
            )
            view.findViewById<TextView>(R.id.recentTitlePill).setText(R.string.title_firefly_name)
        }
        view.findViewById<MaterialButton>(R.id.btnRecentDrive).apply {
            if (latest == null) setText(R.string.record_new_drive)
            setOnClickListener {
                if (latest == null) (activity as? DriveActions)?.startDemoDrive()
                else (activity as? DriveActions)?.showDriveLog(latest)
            }
        }
        view.findViewById<View>(R.id.btnStartGuidance).setOnClickListener {
            (activity as? DriveActions)?.startDemoDrive()
        }
        view.findViewById<View>(R.id.btnOpenCourse).setOnClickListener {
            (activity as? DriveActions)?.showCourses()
        }
    }
}

/** Parked-state record list; live driving remains in DriveFragment. */
class RecordFragment : Fragment(R.layout.fragment_record) {
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val logTab = view.findViewById<MaterialButton>(R.id.btnRecordLog)
        val studioTab = view.findViewById<MaterialButton>(R.id.btnRecordStudio)
        fun showStudio(show: Boolean) {
            view.findViewById<View>(R.id.recordLogPanel).visibility = if (show) View.GONE else View.VISIBLE
            view.findViewById<View>(R.id.recordStudioPanel).visibility = if (show) View.VISIBLE else View.GONE
            listOf(logTab to !show, studioTab to show).forEach { (button, selected) ->
                button.backgroundTintList = android.content.res.ColorStateList.valueOf(
                    ContextCompat.getColor(requireContext(), if (selected) R.color.cl_lime else R.color.cl_ghost),
                )
                button.setTextColor(ContextCompat.getColor(requireContext(), if (selected) R.color.cl_on_lime else R.color.cl_on_ghost))
            }
            view.findViewById<NestedScrollView>(R.id.recordScroll).smoothScrollTo(0, 0)
        }
        logTab.setOnClickListener { showStudio(false) }
        studioTab.setOnClickListener { showStudio(true) }
        showStudio(false)
        val records = DriveHistoryStore.all(requireContext())
        view.findViewById<TextView>(R.id.recordCount).text = getString(R.string.record_count, records.size)
        view.findViewById<View>(R.id.btnStartNewDrive).setOnClickListener {
            (activity as? DriveActions)?.startDemoDrive()
        }
        val latest = records.firstOrNull()
        val previous = view.findViewById<MaterialButton>(R.id.btnPrevScene)
        val next = view.findViewById<MaterialButton>(R.id.btnNextScene)
        val openStudioLog = view.findViewById<MaterialButton>(R.id.btnOpenStudioLog)
        previous.isEnabled = latest != null
        next.isEnabled = latest != null
        openStudioLog.isEnabled = latest != null
        if (latest != null) {
            view.findViewById<View>(R.id.recordLatestSummary).visibility = View.VISIBLE
            view.findViewById<TextView>(R.id.recordLatestDestination).text = latest.destination
            view.findViewById<TextView>(R.id.recordLatestDistance).text =
                getString(R.string.record_latest_distance, latest.distanceKm)
            view.findViewById<TextView>(R.id.recordLatestDuration).text =
                getString(R.string.record_latest_duration, latest.durationSeconds / 60)
            val momentList = view.findViewById<LinearLayout>(R.id.recordMomentList)
            listOf(
                getString(R.string.record_moment_depart),
                getString(R.string.record_moment_distance, latest.distanceKm),
                getString(R.string.record_moment_arrive, latest.destination),
            ).forEach { moment ->
                momentList.addView(TextView(requireContext()).apply {
                    text = moment
                    setTextColor(ContextCompat.getColor(context, R.color.cl_fg))
                    textSize = 22f
                    setPadding(dp(20), dp(14), dp(20), dp(14))
                    setBackgroundResource(R.drawable.bg_stat)
                }, LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                ).apply { bottomMargin = dp(10) })
            }
            view.findViewById<View>(R.id.btnSummaryStudio).setOnClickListener { showStudio(true) }
            view.findViewById<TextView>(R.id.studioJournal).text =
                DriveLogContentStore.caption(requireContext(), latest.id)
                    ?: getString(
                        R.string.record_studio_journal_sample,
                        latest.destination,
                        latest.distanceKm,
                        latest.durationSeconds / 60,
                    )
            openStudioLog.setOnClickListener { (activity as? DriveActions)?.showDriveLog(latest) }
            val scenes = listOf(
                getString(R.string.record_studio_scene_departure, latest.destination),
                getString(R.string.record_studio_scene_distance, latest.distanceKm),
                getString(R.string.record_studio_scene_duration, latest.durationSeconds / 60),
                getString(R.string.record_studio_scene_finish),
            )
            var scene = 0
            fun renderScene() {
                view.findViewById<TextView>(R.id.studioSlideTitle).text = scenes[scene]
                view.findViewById<TextView>(R.id.studioSlideLabel).text =
                    getString(R.string.record_studio_scene_count, scene + 1)
            }
            previous.setOnClickListener { scene = (scene + scenes.size - 1) % scenes.size; renderScene() }
            next.setOnClickListener { scene = (scene + 1) % scenes.size; renderScene() }
            renderScene()
        }
        val list = view.findViewById<LinearLayout>(R.id.recordList)
        if (records.isEmpty()) {
            list.addView(TextView(requireContext()).apply {
                setText(R.string.record_empty)
                setTextColor(ContextCompat.getColor(context, R.color.cl_muted))
                textSize = 24f
                setPadding(0, dp(20), 0, dp(20))
            })
            return
        }
        records.take(8).forEach { record ->
            list.addView(LinearLayout(requireContext()).apply {
                orientation = LinearLayout.HORIZONTAL
                gravity = android.view.Gravity.CENTER_VERTICAL
                setPadding(dp(24), dp(22), dp(24), dp(22))
                setBackgroundResource(R.drawable.bg_stat)
                isClickable = true
                isFocusable = true
                contentDescription = getString(R.string.my_open_drive_log, record.destination)
                setOnClickListener { (activity as? DriveActions)?.showDriveLog(record) }
                addView(LinearLayout(context).apply {
                    orientation = LinearLayout.VERTICAL
                    addView(TextView(context).apply {
                        text = record.destination
                        setTextColor(ContextCompat.getColor(context, R.color.cl_fg))
                        textSize = 28f
                        setTypeface(typeface, android.graphics.Typeface.BOLD)
                    })
                    addView(TextView(context).apply {
                        text = getString(
                            R.string.my_drive_row_meta,
                            String.format(Locale.getDefault(), "%.1f", record.distanceKm),
                            record.durationSeconds / 60,
                        )
                        setTextColor(ContextCompat.getColor(context, R.color.cl_muted))
                        textSize = 21f
                    })
                }, LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f))
                addView(TextView(context).apply {
                    text = "보기 →"
                    setTextColor(ContextCompat.getColor(context, R.color.cl_lime))
                    textSize = 23f
                })
            }, LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT,
            ).apply { bottomMargin = dp(14) })
        }
    }

    private fun dp(value: Int): Int = (value * resources.displayMetrics.density).toInt()
}
/** Car-display MY screen modeled after the vehicle settings panel in the HTML demo. */
class MyFragment : Fragment(R.layout.fragment_my_refresh) {
    private val exportBackup = registerForActivityResult(ActivityResultContracts.CreateDocument("application/json")) { uri ->
        if (uri == null) return@registerForActivityResult
        val context = context ?: return@registerForActivityResult
        val result = runCatching {
            val output = context.contentResolver.openOutputStream(uri, "wt")
                ?: error(getString(R.string.my_backup_open_failed))
            output.bufferedWriter(Charsets.UTF_8).use { it.write(CarlogBackupStore.export(context)) }
        }
        Toast.makeText(
            context,
            if (result.isSuccess) R.string.my_backup_exported else R.string.my_backup_failed,
            Toast.LENGTH_LONG,
        ).show()
    }

    private val importBackup = registerForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri == null) return@registerForActivityResult
        val context = context ?: return@registerForActivityResult
        val result = runCatching {
            val input = context.contentResolver.openInputStream(uri)
                ?: error(getString(R.string.my_backup_open_failed))
            val contents = input.bufferedReader(Charsets.UTF_8).use { it.readText() }
            CarlogBackupStore.import(context, contents)
        }
        result.onSuccess { summary ->
            view?.let { root ->
                bindSetting(root, R.id.switchAutoRecord, "auto_record", true,
                    context.getSharedPreferences("carlog_my_settings", android.content.Context.MODE_PRIVATE))
                bindSetting(root, R.id.switchResumeAfterStop, "resume_after_stop", true,
                    context.getSharedPreferences("carlog_my_settings", android.content.Context.MODE_PRIVATE))
                refreshMyData(root)
            }
            Toast.makeText(
                context,
                getString(R.string.my_backup_imported, summary.driveRecords, summary.savedCourses, summary.likedCourses),
                Toast.LENGTH_LONG,
            ).show()
        }.onFailure {
            Toast.makeText(context, getString(R.string.my_backup_import_error, it.message.orEmpty()), Toast.LENGTH_LONG).show()
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val prefs = requireContext().getSharedPreferences("carlog_my_settings", android.content.Context.MODE_PRIVATE)
        bindSetting(view, R.id.switchAutoRecord, "auto_record", defaultValue = true, prefs = prefs)
        bindSetting(view, R.id.switchResumeAfterStop, "resume_after_stop", defaultValue = true, prefs = prefs)
        refreshMyData(view)
        view.findViewById<View>(R.id.btnExportBackup).setOnClickListener {
            val date = java.text.SimpleDateFormat("yyyyMMdd", Locale.US).format(java.util.Date())
            exportBackup.launch("carlog-backup-$date.json")
        }
        view.findViewById<View>(R.id.btnImportBackup).setOnClickListener {
            importBackup.launch(arrayOf("application/json", "text/*"))
        }
        view.findViewById<View>(R.id.btnPhoneLinkDetails).setOnClickListener {
            AlertDialog.Builder(requireContext())
                .setTitle(R.string.my_phone_title)
                .setMessage(R.string.my_phone_details)
                .setPositiveButton(android.R.string.ok, null)
                .show()
        }
        view.findViewById<View>(R.id.btnMyAllRecords).setOnClickListener {
            (activity as? DriveActions)?.showRecords()
        }
    }

    private fun refreshMyData(root: View) {
        val records = DriveHistoryStore.all(requireContext())
        val now = java.util.Calendar.getInstance()
        val monthly = records.filter { record ->
            val date = java.util.Calendar.getInstance().apply { timeInMillis = record.completedAt }
            date.get(java.util.Calendar.YEAR) == now.get(java.util.Calendar.YEAR) &&
                date.get(java.util.Calendar.MONTH) == now.get(java.util.Calendar.MONTH)
        }
        root.findViewById<TextView>(R.id.myProfileTitle).setText(
            if (records.isEmpty()) R.string.my_profile_title_empty else R.string.title_firefly_name,
        )
        root.findViewById<TextView>(R.id.myDriveCount).text = monthly.size.toString()
        root.findViewById<TextView>(R.id.myTotalDistance).text = String.format(
            Locale.getDefault(), "%.1f km", monthly.sumOf { it.distanceKm },
        )
        root.findViewById<TextView>(R.id.myReceivedLikes).text = monthly.sumOf { it.likesReceived }.toString()
        root.findViewById<TextView>(R.id.myAverageSpeed).text = if (monthly.isEmpty()) "—" else {
            "${monthly.map { it.averageSpeedKmh }.average().toInt()} km/h"
        }

        val container = root.findViewById<LinearLayout>(R.id.myRecentDrives)
        container.removeAllViews()
        if (records.isEmpty()) {
            container.addView(TextView(requireContext()).apply {
                text = getString(R.string.my_no_drives)
                setTextColor(ContextCompat.getColor(context, R.color.cl_muted))
                textSize = 20f
                setPadding(0, dp(18), 0, dp(8))
            })
            return
        }
        records.take(3).forEachIndexed { index, record ->
            val row = LinearLayout(requireContext()).apply {
                orientation = LinearLayout.HORIZONTAL
                gravity = android.view.Gravity.CENTER_VERTICAL
                setPadding(0, dp(16), 0, dp(16))
                isClickable = true
                isFocusable = true
                val selectable = android.util.TypedValue()
                context.theme.resolveAttribute(android.R.attr.selectableItemBackground, selectable, true)
                if (selectable.resourceId != 0) setBackgroundResource(selectable.resourceId)
                contentDescription = getString(R.string.my_open_drive_log, record.destination)
                setOnClickListener { (activity as? DriveActions)?.showDriveLog(record) }
            }
            row.addView(TextView(requireContext()).apply {
                text = "${index + 1}"
                gravity = android.view.Gravity.CENTER
                setTextColor(ContextCompat.getColor(context, R.color.cl_rail))
                textSize = 20f
                setTypeface(typeface, android.graphics.Typeface.BOLD)
                setBackgroundResource(R.drawable.bg_wp_num)
                layoutParams = LinearLayout.LayoutParams(dp(48), dp(48))
            })
            val details = LinearLayout(requireContext()).apply {
                orientation = LinearLayout.VERTICAL
                setPadding(dp(18), 0, 0, 0)
            }
            details.addView(TextView(requireContext()).apply {
                text = record.destination
                setTextColor(ContextCompat.getColor(context, R.color.cl_fg))
                textSize = 22f
            })
            details.addView(TextView(requireContext()).apply {
                text = getString(
                    R.string.my_drive_row_meta,
                    String.format(Locale.getDefault(), "%.1f", record.distanceKm),
                    record.durationSeconds / 60,
                )
                setTextColor(ContextCompat.getColor(context, R.color.cl_muted))
                textSize = 18f
            })
            row.addView(details, LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f))
            row.addView(TextView(requireContext()).apply {
                text = "♥ ${record.likesReceived}"
                setTextColor(ContextCompat.getColor(context, R.color.cl_aqua))
                textSize = 20f
                setTypeface(typeface, android.graphics.Typeface.BOLD)
            })
            row.addView(TextView(requireContext()).apply {
                text = "›"
                setTextColor(ContextCompat.getColor(context, R.color.cl_muted))
                textSize = 32f
                setPadding(dp(18), 0, dp(8), 0)
            })
            container.addView(row)
            if (index < minOf(records.size, 3) - 1) {
                container.addView(View(requireContext()).apply {
                    setBackgroundColor(ContextCompat.getColor(context, R.color.cl_edge))
                    layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, dp(1))
                })
            }
        }
    }

    private fun dp(value: Int): Int = (value * resources.displayMetrics.density).toInt()

    private fun bindSetting(
        root: View,
        viewId: Int,
        key: String,
        defaultValue: Boolean,
        prefs: android.content.SharedPreferences,
    ) {
        val toggle = root.findViewById<com.google.android.material.switchmaterial.SwitchMaterial>(viewId)
        toggle.isChecked = prefs.getBoolean(key, defaultValue)
        toggle.setOnCheckedChangeListener { _, checked ->
            prefs.edit().putBoolean(key, checked).apply()
            Toast.makeText(requireContext(), R.string.my_setting_saved, Toast.LENGTH_SHORT).show()
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
    private companion object {
        const val FILTER_FOLLOWING = "following"
        const val FILTER_COURSES = "courses"
        const val FILTER_GUEST = "guest"
    }
    private var selectedCourseId = DemoCourses.all.first().id
    private var selectedFilter = DemoCourses.FILTER_ALL

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        renderFilters(view)
        renderChoices(view)
        bindCourse(view, DemoCourses.byId(selectedCourseId) ?: DemoCourses.all.first())
        val guestPrefs = requireContext().getSharedPreferences("carlog_guest_demo", android.content.Context.MODE_PRIVATE)
        fun renderGuestEntry() {
            val place = guestPrefs.getString("place", "").orEmpty()
            val message = guestPrefs.getString("message", "").orEmpty()
            view.findViewById<TextView>(R.id.guestAdded).apply {
                visibility = if (place.isNotBlank() && message.isNotBlank()) View.VISIBLE else View.GONE
                text = getString(R.string.course_guest_added, place, message)
            }
        }
        renderGuestEntry()
        view.findViewById<View>(R.id.btnAddGuest).setOnClickListener {
            val place = view.findViewById<EditText>(R.id.guestPlace).text.toString().trim()
            val message = view.findViewById<EditText>(R.id.guestMessage).text.toString().trim()
            if (place.isEmpty() || message.isEmpty()) {
                Toast.makeText(requireContext(), R.string.course_guest_required, Toast.LENGTH_SHORT).show()
            } else {
                guestPrefs.edit().putString("place", place).putString("message", message).apply()
                view.findViewById<EditText>(R.id.guestMessage).text.clear()
                renderGuestEntry()
            }
        }

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
        view.findViewById<View>(R.id.btnLikeCourse).setOnClickListener {
            LikedCourseStore.toggle(requireContext(), selectedCourseId)
            bindCourse(view, DemoCourses.byId(selectedCourseId) ?: return@setOnClickListener)
        }
        view.findViewById<View>(R.id.btnFollowCourse).setOnClickListener {
            (activity as? DriveActions)?.startDemoDrive(courseId = selectedCourseId)
        }
        view.findViewById<View>(R.id.btnCourseBack).setOnClickListener {
            view.findViewById<View>(R.id.courseDetails).visibility = View.GONE
            view.findViewById<View>(R.id.courseList).visibility = View.VISIBLE
            renderChoices(view)
            view.findViewById<NestedScrollView>(R.id.courseScroll).smoothScrollTo(0, 0)
        }
    }

    private fun renderFilters(root: View) {
        val container = root.findViewById<LinearLayout>(R.id.courseFilters)
        container.removeAllViews()
        listOf(
            DemoCourses.FILTER_ALL to R.string.course_filter_popular,
            FILTER_FOLLOWING to R.string.course_filter_following,
            FILTER_COURSES to R.string.course_filter_courses,
            FILTER_GUEST to R.string.course_filter_guest,
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
        val guestSelected = selectedFilter == FILTER_GUEST
        container.visibility = if (guestSelected) View.GONE else View.VISIBLE
        root.findViewById<View>(R.id.courseGuest).visibility = if (guestSelected) View.VISIBLE else View.GONE
        root.findViewById<View>(R.id.courseListNotice).visibility = if (guestSelected) View.GONE else View.VISIBLE
        if (guestSelected) return
        val choices = filteredCourses()
        if (choices.isEmpty()) {
            container.addView(TextView(requireContext()).apply {
                text = getString(R.string.course_empty_filter)
                setTextColor(ContextCompat.getColor(context, R.color.cl_muted))
                textSize = 22f
            })
            return
        }
        choices.chunked(3).forEach { pair ->
            val row = LinearLayout(requireContext()).apply { orientation = LinearLayout.HORIZONTAL }
            pair.forEach { course ->
                val isSelected = course.id == selectedCourseId
                val card = LinearLayout(requireContext()).apply {
                    orientation = LinearLayout.VERTICAL
                    val inset = dp(if (isSelected) 3 else 1)
                    setPadding(inset, inset, inset, inset)
                    background = GradientDrawable().apply {
                        cornerRadius = dp(24).toFloat()
                        setColor(ContextCompat.getColor(context, if (isSelected) R.color.cl_nav_active_bg else R.color.cl_panel))
                        setStroke(dp(if (isSelected) 3 else 1), ContextCompat.getColor(context, if (isSelected) R.color.cl_lime else R.color.cl_edge))
                    }
                    clipToOutline = true
                    isClickable = true
                    isFocusable = true
                    this.isSelected = isSelected
                    contentDescription = getString(R.string.course_choice_format, course.name, course.distanceKm.toInt())
                    setOnClickListener {
                        selectedCourseId = course.id
                        bindCourse(root, course)
                        root.findViewById<View>(R.id.courseList).visibility = View.GONE
                        root.findViewById<View>(R.id.courseDetails).visibility = View.VISIBLE
                        root.findViewById<NestedScrollView>(R.id.courseScroll).smoothScrollTo(0, 0)
                    }
                }
                card.addView(ImageView(requireContext()).apply {
                    setImageResource(course.coverDrawable)
                    scaleType = ImageView.ScaleType.CENTER_CROP
                    contentDescription = getString(R.string.course_cover_desc, course.name)
                    layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, dp(180))
                })
                val cardText = LinearLayout(requireContext()).apply {
                    orientation = LinearLayout.VERTICAL
                    setPadding(dp(22), dp(16), dp(22), dp(18))
                }
                cardText.addView(TextView(requireContext()).apply {
                    text = course.name
                    setTextColor(ContextCompat.getColor(context, if (isSelected) R.color.cl_lime else R.color.cl_fg))
                    textSize = 24f
                    setTypeface(typeface, android.graphics.Typeface.BOLD)
                    maxLines = 1
                    ellipsize = android.text.TextUtils.TruncateAt.END
                })
                cardText.addView(TextView(requireContext()).apply {
                    text = getString(R.string.course_route_meta_format, course.author, course.distanceKm.toInt(), course.waypoints.size)
                    setTextColor(ContextCompat.getColor(context, if (isSelected) R.color.cl_fg else R.color.cl_muted))
                    textSize = 18f
                    maxLines = 1
                })
                card.addView(cardText)
                row.addView(card, LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f).apply {
                    marginEnd = dp(14)
                })
            }
            repeat(3 - pair.size) {
                row.addView(View(requireContext()), LinearLayout.LayoutParams(0, 1, 1f))
            }
            container.addView(row, LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT).apply {
                bottomMargin = dp(14)
            })
        }
    }

    private fun filteredCourses(): List<DemoCourse> {
        val savedIds = SavedCourseStore.ids(requireContext())
        return DemoCourses.all.filter { course ->
            when (selectedFilter) {
                DemoCourses.FILTER_ALL, FILTER_FOLLOWING, FILTER_COURSES -> true
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
        root.findViewById<TextView>(R.id.courseDistance).text = if (course.distanceKm % 1.0 == 0.0) {
            "${course.distanceKm.toInt()} km"
        } else {
            String.format(Locale.getDefault(), "%.1f km", course.distanceKm)
        }
        root.findViewById<TextView>(R.id.courseTime).text = course.duration
        renderWaypoints(root, course)
        root.findViewById<TextView>(R.id.courseDemoNotice).setText(R.string.course_note_dynamic)
        root.findViewById<MaterialButton>(R.id.btnSaveCourse).setText(
            if (SavedCourseStore.contains(requireContext(), course.id)) R.string.course_unsave else R.string.course_save,
        )
        root.findViewById<MaterialButton>(R.id.btnLikeCourse).setText(
            if (LikedCourseStore.contains(requireContext(), course.id)) R.string.course_unlike else R.string.course_like,
        )
    }

    private fun renderWaypoints(root: View, course: DemoCourse) {
        val container = root.findViewById<LinearLayout>(R.id.courseWaypoints)
        container.removeAllViews()
        val legs = course.legs()
        fun addStop(label: String, marker: String, arrival: Boolean = false) {
            val row = LinearLayout(requireContext()).apply {
                orientation = LinearLayout.HORIZONTAL
                gravity = android.view.Gravity.CENTER_VERTICAL
                setMinimumHeight(dp(48))
            }
            row.addView(TextView(requireContext()).apply {
                text = marker
                gravity = android.view.Gravity.CENTER
                setTextColor(ContextCompat.getColor(context, R.color.cl_rail))
                textSize = if (arrival || marker == "출") 14f else 20f
                setTypeface(typeface, android.graphics.Typeface.BOLD)
                setBackgroundResource(if (arrival) R.drawable.bg_wp_end else R.drawable.bg_wp_num)
                layoutParams = LinearLayout.LayoutParams(dp(48), dp(48))
            })
            row.addView(TextView(requireContext()).apply {
                text = label
                setPadding(dp(16), 0, 0, 0)
                setTextColor(ContextCompat.getColor(context, R.color.cl_fg))
                textSize = 22f
                maxLines = 1
                ellipsize = android.text.TextUtils.TruncateAt.END
            })
            container.addView(row)
        }

        fun addLeg(leg: CourseLeg) {
            val segment = LinearLayout(requireContext()).apply {
                orientation = LinearLayout.HORIZONTAL
                gravity = android.view.Gravity.CENTER_VERTICAL
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    dp(34),
                )
            }
            val lineSlot = FrameLayout(requireContext()).apply {
                layoutParams = LinearLayout.LayoutParams(dp(48), LinearLayout.LayoutParams.MATCH_PARENT)
                addView(View(context).apply {
                    setBackgroundColor(ContextCompat.getColor(context, R.color.cl_aqua))
                    layoutParams = FrameLayout.LayoutParams(dp(3), FrameLayout.LayoutParams.MATCH_PARENT, android.view.Gravity.CENTER)
                })
            }
            segment.addView(lineSlot)
            segment.addView(TextView(requireContext()).apply {
                text = getString(R.string.course_leg_format, leg.distanceKm, leg.durationMinutes)
                setTextColor(ContextCompat.getColor(context, R.color.cl_aqua))
                textSize = 17f
                setTypeface(typeface, android.graphics.Typeface.BOLD)
                setPadding(dp(16), 0, 0, 0)
            })
            container.addView(segment)
        }

        addStop(getString(R.string.course_start_hidden_short), "출")
        legs.forEachIndexed { index, leg ->
            addLeg(leg)
            val isArrival = index == legs.lastIndex
            addStop(
                leg.destination,
                if (isArrival) getString(R.string.course_arrival) else (index + 1).toString(),
                arrival = isArrival,
            )
        }
    }

    private fun dp(value: Int): Int = (value * resources.displayMetrics.density).toInt()
}

/** Result of the last locally persisted demo drive. */
class TripResultFragment : Fragment(R.layout.fragment_trip_result) {
    private val chooseLogMedia = registerForActivityResult(ActivityResultContracts.OpenMultipleDocuments()) { uris ->
        if (uris.isEmpty()) return@registerForActivityResult
        val context = context ?: return@registerForActivityResult
        val recordId = arguments?.getString(ARG_RECORD_ID) ?: return@registerForActivityResult
        uris.forEach { uri ->
            runCatching {
                context.contentResolver.takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
        }
        DriveLogContentStore.addMedia(context, recordId, uris)
        val record = DriveHistoryStore.all(context).firstOrNull { it.id == recordId }
        if (record != null && DriveLogContentStore.caption(context, recordId) == null) {
            DriveLogContentStore.saveCaption(context, recordId, autoCaption(record, uris.size))
        }
        view?.let { root ->
            renderLogMedia(root, recordId)
            if (record != null) renderLogCaption(root, record)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val args = requireArguments()
        val historyDetail = args.getBoolean(ARG_HISTORY_DETAIL)
        val recordId = args.getString(ARG_RECORD_ID).orEmpty()
        view.findViewById<TextView>(R.id.resultHeadline).setText(
            if (historyDetail) R.string.my_log_detail_title else R.string.result_title,
        )
        view.findViewById<TextView>(R.id.resultDestination).text = args.getString(ARG_DESTINATION).orEmpty()
        view.findViewById<TextView>(R.id.resultRecordedAt).apply {
            visibility = if (historyDetail) View.VISIBLE else View.GONE
            text = getString(
                R.string.my_log_recorded_at,
                java.text.SimpleDateFormat("yyyy.MM.dd (E) HH:mm", Locale.KOREA)
                    .format(java.util.Date(args.getLong(ARG_COMPLETED_AT))),
            )
        }
        view.findViewById<TextView>(R.id.resultSavedLabel).setText(
            if (historyDetail) R.string.my_log_saved_label else R.string.result_saved,
        )
        view.findViewById<TextView>(R.id.resultDistance).text = String.format(
            Locale.getDefault(), "%.1f km", args.getDouble(ARG_DISTANCE),
        )
        view.findViewById<TextView>(R.id.resultDuration).text = getString(
            R.string.result_duration_format, args.getInt(ARG_DURATION) / 60, args.getInt(ARG_DURATION) % 60,
        )
        view.findViewById<TextView>(R.id.resultAverage).text = getString(
            R.string.result_speed_format, args.getInt(ARG_AVERAGE),
        )
        view.findViewById<TextView>(R.id.resultEvents).text = if (historyDetail) {
            getString(R.string.my_log_likes_format, args.getInt(ARG_LIKES_RECEIVED))
        } else {
            getString(if (args.getBoolean(ARG_FOLLOWED_COURSE)) R.string.result_course_events else R.string.result_home_events)
        }
        val record = DriveHistoryStore.all(requireContext()).firstOrNull { it.id == recordId }
            ?: DriveRecord(
                id = recordId,
                destination = args.getString(ARG_DESTINATION).orEmpty(),
                distanceKm = args.getDouble(ARG_DISTANCE),
                durationSeconds = args.getInt(ARG_DURATION),
                averageSpeedKmh = args.getInt(ARG_AVERAGE),
                followedFriendCourse = args.getBoolean(ARG_FOLLOWED_COURSE),
                completedAt = args.getLong(ARG_COMPLETED_AT),
                likesReceived = args.getInt(ARG_LIKES_RECEIVED),
                courseId = args.getString(ARG_COURSE_ID),
            )
        view.findViewById<com.example.carlog_hd_frontend.ui.map.RouteMapView>(R.id.resultRouteMap).apply {
            route = DemoCourses.byId(record.courseId)?.route ?: CarlogRoutes.MINE
            progress = 1f
            showTrack = true
            showCar = false
        }
        renderLogMedia(view, recordId)
        renderLogCaption(view, record)
        view.findViewById<View>(R.id.btnAddLogMedia).setOnClickListener {
            chooseLogMedia.launch(arrayOf("image/*", "video/*"))
        }
        view.findViewById<View>(R.id.btnEditLog).setOnClickListener {
            showLogEditor(record, recordId)
        }
        view.findViewById<View>(R.id.btnPreviewShorts).setOnClickListener {
            showShortsPreview(record, recordId)
        }
        view.findViewById<MaterialButton>(R.id.btnResultHome).setText(
            if (historyDetail) R.string.my_log_back_to_my else R.string.result_home_button,
        )
        view.findViewById<View>(R.id.btnResultHome).setOnClickListener {
            if (historyDetail) (activity as? DriveActions)?.returnToMy()
            else (activity as? DriveActions)?.returnHome()
        }
    }

    companion object {
        private const val ARG_DESTINATION = "destination"
        private const val ARG_DISTANCE = "distance"
        private const val ARG_DURATION = "duration"
        private const val ARG_AVERAGE = "average"
        private const val ARG_FOLLOWED_COURSE = "followed_course"
        private const val ARG_HISTORY_DETAIL = "history_detail"
        private const val ARG_COMPLETED_AT = "completed_at"
        private const val ARG_LIKES_RECEIVED = "likes_received"
        private const val ARG_RECORD_ID = "record_id"
        private const val ARG_COURSE_ID = "course_id"

        fun newInstance(record: DriveRecord, historyDetail: Boolean = false) = TripResultFragment().apply {
            arguments = Bundle().apply {
                putString(ARG_DESTINATION, record.destination)
                putDouble(ARG_DISTANCE, record.distanceKm)
                putInt(ARG_DURATION, record.durationSeconds)
                putInt(ARG_AVERAGE, record.averageSpeedKmh)
                putBoolean(ARG_FOLLOWED_COURSE, record.followedFriendCourse)
                putBoolean(ARG_HISTORY_DETAIL, historyDetail)
                putLong(ARG_COMPLETED_AT, record.completedAt)
                putInt(ARG_LIKES_RECEIVED, record.likesReceived)
                putString(ARG_RECORD_ID, record.id)
                putString(ARG_COURSE_ID, record.courseId)
            }
        }
    }

    private fun renderLogMedia(root: View, recordId: String) {
        val media = DriveLogContentStore.media(requireContext(), recordId)
        val list = root.findViewById<LinearLayout>(R.id.resultMediaList)
        list.removeAllViews()
        root.findViewById<TextView>(R.id.resultMediaEmpty).apply {
            visibility = if (media.isEmpty()) View.VISIBLE else View.GONE
        }
        media.forEachIndexed { index, uri ->
            val tile = LinearLayout(requireContext()).apply {
                orientation = LinearLayout.VERTICAL
                gravity = android.view.Gravity.CENTER_HORIZONTAL
                setPadding(0, 0, dp(12), 0)
                layoutParams = LinearLayout.LayoutParams(dp(220), LinearLayout.LayoutParams.WRAP_CONTENT)
                isClickable = true
                isFocusable = true
                setOnClickListener { openLogMedia(uri) }
            }
            val preview = ImageView(requireContext()).apply {
                scaleType = ImageView.ScaleType.CENTER_CROP
                setBackgroundResource(R.drawable.bg_stat)
                layoutParams = LinearLayout.LayoutParams(dp(208), dp(136))
                val type = requireContext().contentResolver.getType(uri).orEmpty()
                val bitmap = runCatching {
                    if (type.startsWith("video/")) {
                        MediaMetadataRetriever().let { retriever ->
                            try {
                                retriever.setDataSource(requireContext(), uri)
                                retriever.getFrameAtTime(0)
                            } finally {
                                retriever.release()
                            }
                        }
                    } else {
                        requireContext().contentResolver.openInputStream(uri)?.use { android.graphics.BitmapFactory.decodeStream(it) }
                    }
                }.getOrNull()
                if (bitmap != null) setImageBitmap(bitmap)
                else setImageResource(if (type.startsWith("video/")) R.drawable.ic_nav_drive else R.drawable.ic_nav_course)
                contentDescription = getString(R.string.my_log_media_item, index + 1)
            }
            tile.addView(preview)
            tile.addView(TextView(requireContext()).apply {
                text = if (requireContext().contentResolver.getType(uri).orEmpty().startsWith("video/"))
                    getString(R.string.my_log_video_item, index + 1)
                else getString(R.string.my_log_photo_item, index + 1)
                setTextColor(ContextCompat.getColor(context, R.color.cl_muted))
                textSize = 16f
                setPadding(0, dp(6), 0, dp(4))
            })
            list.addView(tile)
        }
    }

    private fun renderLogCaption(root: View, record: DriveRecord) {
        val recordId = arguments?.getString(ARG_RECORD_ID).orEmpty()
        val mediaCount = DriveLogContentStore.media(requireContext(), recordId).size
        val caption = DriveLogContentStore.caption(requireContext(), recordId)
            ?: autoCaption(record, mediaCount)
        root.findViewById<TextView>(R.id.resultLogCaption).text = caption
    }

    private fun autoCaption(record: DriveRecord, mediaCount: Int): String {
        val resource = if (mediaCount == 0) R.string.my_log_auto_caption_no_media
        else R.string.my_log_auto_caption_with_media
        val distance = String.format(Locale.getDefault(), "%.1f", record.distanceKm)
        return if (mediaCount == 0) {
            getString(resource, record.destination, distance, record.durationSeconds / 60)
        } else {
            getString(resource, record.destination, distance, record.durationSeconds / 60, mediaCount)
        }
    }

    private fun showLogEditor(record: DriveRecord, recordId: String) {
        val editor = EditText(requireContext()).apply {
            minLines = 4
            maxLines = 8
            gravity = android.view.Gravity.TOP
            setText(DriveLogContentStore.caption(requireContext(), recordId) ?: autoCaption(
                record, DriveLogContentStore.media(requireContext(), recordId).size,
            ))
            setSelection(text.length)
            setPadding(dp(20), dp(16), dp(20), dp(16))
        }
        AlertDialog.Builder(requireContext())
            .setTitle(R.string.my_log_edit_title)
            .setView(editor)
            .setNegativeButton(android.R.string.cancel, null)
            .setPositiveButton(R.string.my_log_edit_save) { _, _ ->
                DriveLogContentStore.saveCaption(requireContext(), recordId, editor.text.toString().trim())
                view?.let { renderLogCaption(it, record) }
            }
            .show()
    }

    private fun showShortsPreview(record: DriveRecord, recordId: String) {
        val mediaCount = DriveLogContentStore.media(requireContext(), recordId).size
        val caption = DriveLogContentStore.caption(requireContext(), recordId) ?: autoCaption(record, mediaCount)
        AlertDialog.Builder(requireContext())
            .setTitle(R.string.my_log_preview_shorts_title)
            .setMessage(getString(R.string.my_log_preview_shorts_message, caption, mediaCount))
            .setPositiveButton(android.R.string.ok, null)
            .show()
    }

    private fun openLogMedia(uri: Uri) {
        val type = requireContext().contentResolver.getType(uri) ?: "*/*"
        runCatching {
            startActivity(Intent(Intent.ACTION_VIEW).setDataAndType(uri, type).addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION))
        }.onFailure {
            Toast.makeText(requireContext(), R.string.my_log_media_open_failed, Toast.LENGTH_SHORT).show()
        }
    }

    private fun dp(value: Int): Int = (value * resources.displayMetrics.density).toInt()
}

/** Titles are still demo data; the first successful simulation awards a sample title. */
class TitlesFragment : Fragment(R.layout.fragment_titles) {

    private data class Title(
        @StringRes val name: Int,
        @StringRes val rule: Int,
        @StringRes val data: Int,
        val emoji: String,
        val earned: Boolean,
    )

    private val titles by lazy {
        val hasCompletedDrive = DriveHistoryStore.all(requireContext()).isNotEmpty()
        listOf(
            Title(R.string.title_firefly_name, R.string.title_firefly_rule, R.string.title_firefly_data, "🚦", earned = hasCompletedDrive),
            Title(R.string.title_cloud_name, R.string.title_cloud_rule, R.string.title_cloud_data, "☁️", earned = false),
            Title(R.string.title_cruise_name, R.string.title_cruise_rule, R.string.title_cruise_data, "🛣️", earned = false),
            Title(R.string.title_rain_name, R.string.title_rain_rule, R.string.title_rain_data, "🌧️", earned = false),
            Title(R.string.title_night_name, R.string.title_night_rule, R.string.title_night_data, "🌙", earned = false),
            Title(R.string.title_follower_name, R.string.title_follower_rule, R.string.title_follower_data, "🧭", earned = false),
        )
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val grid = view.findViewById<LinearLayout>(R.id.titleGrid)
        view.findViewById<TextView>(R.id.titlesCollectionCount).text =
            getString(R.string.titles_collection_count, titles.count { it.earned }, titles.size)
        view.findViewById<TextView>(R.id.titlesChallengeStatus).setText(
            if (titles.any { it.earned }) R.string.titles_challenge_first else R.string.titles_challenge_waiting,
        )
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
        card.findViewById<TextView>(R.id.titleEmoji).text = title.emoji
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
