package com.example.carlog_hd_frontend

import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.annotation.IdRes
import androidx.annotation.StringRes
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.example.carlog_hd_frontend.ui.CourseFragment
import com.example.carlog_hd_frontend.ui.DriveFragment
import com.example.carlog_hd_frontend.ui.HomeFragment
import com.example.carlog_hd_frontend.ui.TitlesFragment

/**
 * CARLOG IVI 메인 화면. 왼쪽 레일의 탭에 따라 가운데 화면(Fragment)을 바꿔 끼웁니다.
 * 각 화면과 상단 상태 바의 값은 carlog-demo.html 과 같은 샘플 문구입니다.
 */
class MainActivity : AppCompatActivity() {

    private enum class Tab(@IdRes val navId: Int, val recording: Boolean, val create: () -> Fragment) {
        HOME(R.id.navHome, recording = false, create = ::HomeFragment),
        DRIVE(R.id.navDrive, recording = true, create = ::DriveFragment),
        COURSE(R.id.navCourse, recording = false, create = ::CourseFragment),
        TITLES(R.id.navTitles, recording = false, create = ::TitlesFragment),
    }

    private var current = Tab.HOME

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        Tab.values().forEach { tab ->
            findViewById<View>(tab.navId).setOnClickListener { select(tab) }
        }

        // 화면 회전 등으로 다시 만들어질 때는 FragmentManager 가 화면을 복원하므로 상태만 맞춥니다.
        val restored = savedInstanceState?.getString(KEY_TAB)?.let { Tab.valueOf(it) }
        if (restored != null) {
            current = restored
            updateChrome()
        } else {
            select(Tab.HOME, force = true)
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString(KEY_TAB, current.name)
    }

    private fun select(tab: Tab, force: Boolean = false) {
        if (tab == current && !force) return
        current = tab
        supportFragmentManager.beginTransaction()
            .replace(R.id.screenContainer, tab.create())
            .commit()
        updateChrome()
    }

    /** 레일 선택 표시와 상단 상태 바(대기 중 / 자동 기록 중)를 현재 탭에 맞춥니다. */
    private fun updateChrome() {
        Tab.values().forEach { findViewById<View>(it.navId).isSelected = it == current }

        val rec = current.recording
        findViewById<View>(R.id.liveDot).setBackgroundResource(
            if (rec) R.drawable.bg_live_dot_rec else R.drawable.bg_live_dot,
        )
        findViewById<TextView>(R.id.liveText).apply {
            setText(if (rec) R.string.status_recording else R.string.status_idle)
            setTextColor(ContextCompat.getColor(context, if (rec) R.color.cl_rec_text else R.color.cl_aqua))
        }
        setText(R.id.statusClock, if (rec) R.string.status_clock_drive_sample else R.string.status_clock_sample)
        setText(R.id.statusSpeed, if (rec) R.string.status_speed_drive_sample else R.string.status_speed_sample)
        setText(R.id.statusGear, if (rec) R.string.status_gear_drive_sample else R.string.status_gear_sample)
    }

    private fun setText(@IdRes id: Int, @StringRes text: Int) {
        findViewById<TextView>(id).setText(text)
    }

    private companion object {
        const val KEY_TAB = "tab"
    }
}
