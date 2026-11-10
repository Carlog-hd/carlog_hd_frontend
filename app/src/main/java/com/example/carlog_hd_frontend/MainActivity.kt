package com.example.carlog_hd_frontend

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.IdRes
import androidx.annotation.StringRes
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.example.carlog_hd_frontend.ui.CourseFragment
import com.example.carlog_hd_frontend.ui.DriveActions
import com.example.carlog_hd_frontend.ui.DriveFragment
import com.example.carlog_hd_frontend.ui.DriveHistoryStore
import com.example.carlog_hd_frontend.ui.DriveRecord
import com.example.carlog_hd_frontend.ui.DemoCourses
import com.example.carlog_hd_frontend.ui.HomeFragment
import com.example.carlog_hd_frontend.ui.TitlesFragment
import com.example.carlog_hd_frontend.ui.TripResultFragment
import java.util.Locale

/** CARLOG IVI shell and repeatable local drive simulator. */
class MainActivity : AppCompatActivity(), DriveActions {

    private enum class Tab(@IdRes val navId: Int, val recording: Boolean, val create: () -> Fragment) {
        HOME(R.id.navHome, recording = false, create = ::HomeFragment),
        DRIVE(R.id.navDrive, recording = true, create = ::DriveFragment),
        COURSE(R.id.navCourse, recording = false, create = ::CourseFragment),
        TITLES(R.id.navTitles, recording = false, create = ::TitlesFragment),
    }

    private val handler = Handler(Looper.getMainLooper())
    private var current = Tab.HOME
    private var seconds = 0
    private var isDriving = false
    private var isPaused = false
    private var selectedCourseId: String? = null
    private val simulationDurationSeconds = 45
    private val tick = object : Runnable {
        override fun run() {
            if (!isDriving || isPaused) return
            seconds += 1
            if (seconds >= simulationDurationSeconds) {
                finishDemoDrive()
            } else {
                updateDriveScreen()
                updateChrome()
                handler.postDelayed(this, 1_000L)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        Tab.values().forEach { tab ->
            findViewById<View>(tab.navId).setOnClickListener {
                if (tab == Tab.DRIVE && !isDriving) startDemoDrive(courseId = null) else select(tab)
            }
        }

        val restored = savedInstanceState?.getString(KEY_TAB)?.let { runCatching { Tab.valueOf(it) }.getOrNull() }
        if (restored != null) {
            current = restored
            select(restored, force = true)
            seconds = savedInstanceState?.getInt(KEY_SECONDS, 0) ?: 0
            isDriving = savedInstanceState?.getBoolean(KEY_IS_DRIVING, false) ?: false
            isPaused = savedInstanceState?.getBoolean(KEY_IS_PAUSED, false) ?: false
            selectedCourseId = savedInstanceState.getString(KEY_COURSE_ID)
            if (isDriving) {
                supportFragmentManager.executePendingTransactions()
                updateDriveScreen()
                if (!isPaused) handler.postDelayed(tick, 1_000L)
            }
        } else {
            select(Tab.HOME, force = true)
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putString(KEY_TAB, current.name)
        outState.putInt(KEY_SECONDS, seconds)
        outState.putBoolean(KEY_IS_DRIVING, isDriving)
        outState.putBoolean(KEY_IS_PAUSED, isPaused)
        outState.putString(KEY_COURSE_ID, selectedCourseId)
        super.onSaveInstanceState(outState)
    }

    override fun onDestroy() {
        handler.removeCallbacks(tick)
        super.onDestroy()
    }

    private fun select(tab: Tab, force: Boolean = false) {
        if (isDriving && tab != Tab.DRIVE) {
            Toast.makeText(this, R.string.drive_navigation_locked, Toast.LENGTH_SHORT).show()
            return
        }
        if (tab == current && !force) return
        current = tab
        supportFragmentManager.beginTransaction()
            .replace(R.id.screenContainer, tab.create())
            .commit()
        updateChrome()
    }

    override fun startDemoDrive(courseId: String?) {
        handler.removeCallbacks(tick)
        selectedCourseId = courseId
        seconds = 0
        isDriving = true
        isPaused = false
        current = Tab.DRIVE
        supportFragmentManager.beginTransaction()
            .replace(R.id.screenContainer, DriveFragment())
            .commit()
        updateChrome()
        handler.postDelayed(tick, 1_000L)
    }

    override fun toggleDemoDrive() {
        if (!isDriving) return
        isPaused = !isPaused
        if (isPaused) {
            handler.removeCallbacks(tick)
        } else {
            handler.postDelayed(tick, 1_000L)
        }
        updateDriveScreen()
        updateChrome()
    }

    override fun finishDemoDrive() {
        if (!isDriving) return
        handler.removeCallbacks(tick)
        val selectedCourse = DemoCourses.byId(selectedCourseId)
        val totalDistance = selectedCourse?.distanceKm ?: 60.4
        val totalDurationSeconds = (selectedCourse?.durationMinutes ?: 67) * 60
        val progress = (seconds.toFloat() / simulationDurationSeconds).coerceIn(0f, 1f)
        val record = DriveRecord(
            id = System.currentTimeMillis().toString(),
            destination = selectedCourse?.destination ?: getString(R.string.home_dest_name_sample),
            distanceKm = totalDistance * progress,
            durationSeconds = (totalDurationSeconds * progress).toInt().coerceAtLeast(1),
            averageSpeedKmh = selectedCourse?.averageSpeedKmh ?: 54,
            followedFriendCourse = selectedCourse != null,
            completedAt = System.currentTimeMillis(),
        )
        DriveHistoryStore.save(this, record)
        isDriving = false
        isPaused = false
        current = Tab.HOME
        supportFragmentManager.beginTransaction()
            .replace(R.id.screenContainer, TripResultFragment.newInstance(record))
            .commit()
        updateChrome()
    }

    override fun returnHome() {
        handler.removeCallbacks(tick)
        isDriving = false
        isPaused = false
        select(Tab.HOME, force = true)
    }

    override fun showCourses() {
        select(Tab.COURSE)
    }

    private fun updateDriveScreen() {
        val fragment = supportFragmentManager.findFragmentById(R.id.screenContainer) as? DriveFragment ?: return
        fragment.renderSession(
            elapsedSeconds = seconds,
            durationSeconds = simulationDurationSeconds,
            paused = isPaused,
            course = DemoCourses.byId(selectedCourseId),
        )
    }

    /** Top status reflects the same deterministic values as the simulator. */
    private fun updateChrome() {
        Tab.values().forEach { findViewById<View>(it.navId).isSelected = it == current }

        val recording = isDriving
        findViewById<View>(R.id.liveDot).setBackgroundResource(
            if (recording) R.drawable.bg_live_dot_rec else R.drawable.bg_live_dot,
        )
        findViewById<TextView>(R.id.liveText).apply {
            setText(if (recording) R.string.status_recording else R.string.status_idle)
            setTextColor(ContextCompat.getColor(context, if (recording) R.color.cl_rec_text else R.color.cl_aqua))
        }
        findViewById<TextView>(R.id.statusClock).text = if (recording) {
            val simulationMinutes = seconds * 90 / 60
            String.format(Locale.getDefault(), "%02d:%02d", 14 + (12 + simulationMinutes) / 60, (12 + simulationMinutes) % 60)
        } else {
            getString(R.string.status_clock_sample)
        }
        findViewById<TextView>(R.id.statusSpeed).text = if (recording && !isPaused) {
            "${35 + (seconds * 7 % 31)} km/h"
        } else {
            "0 km/h"
        }
        findViewById<TextView>(R.id.statusGear).text = if (recording && !isPaused) "D" else "P"
    }

    private companion object {
        const val KEY_TAB = "tab"
        const val KEY_SECONDS = "seconds"
        const val KEY_IS_DRIVING = "is_driving"
        const val KEY_IS_PAUSED = "is_paused"
        const val KEY_COURSE_ID = "course_id"
    }
}
