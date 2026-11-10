package com.carlog.mobile

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.carlog.mobile.ui.dex.DexFragment
import com.carlog.mobile.ui.discover.DiscoverFragment
import com.carlog.mobile.ui.home.HomeFragment
import com.carlog.mobile.ui.logs.LogsFragment
import com.carlog.mobile.ui.my.MyFragment
import com.google.android.material.bottomnavigation.BottomNavigationView

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val bottomNav = findViewById<BottomNavigationView>(R.id.bottom_nav)
        bottomNav.setOnItemSelectedListener { item ->
            showTab(item.itemId)
            true
        }

        // 앱 첫 실행 시 홈 탭 표시 (화면 회전 등으로 다시 만들어질 때는 기존 화면 유지)
        if (savedInstanceState == null) {
            showTab(R.id.nav_home)
        }
    }

    /** 하단 탭에 맞는 화면으로 교체한다. */
    private fun showTab(itemId: Int) {
        val fragment: Fragment = when (itemId) {
            R.id.nav_discover -> DiscoverFragment()
            R.id.nav_logs -> LogsFragment()
            R.id.nav_dex -> DexFragment()
            R.id.nav_my -> MyFragment()
            else -> HomeFragment()
        }
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .commit()
    }
}
