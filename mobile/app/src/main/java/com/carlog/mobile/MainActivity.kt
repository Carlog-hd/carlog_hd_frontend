package com.carlog.mobile

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import com.carlog.mobile.ui.dex.DexFragment
import com.carlog.mobile.ui.discover.DiscoverFragment
import com.carlog.mobile.ui.home.HomeFragment
import com.carlog.mobile.ui.logs.LogsFragment
import com.carlog.mobile.ui.my.MyFragment
import com.google.android.material.bottomnavigation.BottomNavigationView

class MainActivity : AppCompatActivity() {

    private lateinit var bottomNav: BottomNavigationView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        bottomNav = findViewById(R.id.bottom_nav)
        bottomNav.setOnItemSelectedListener { item ->
            showTab(item.itemId)
            true
        }
        // 하위 화면에 있을 때 같은 탭을 다시 누르면 탭 첫 화면으로 돌아간다.
        bottomNav.setOnItemReselectedListener { clearSubScreens() }

        // 앱 첫 실행 시 홈 탭 표시 (화면 회전 등으로 다시 만들어질 때는 기존 화면 유지)
        if (savedInstanceState == null) {
            showTab(R.id.nav_home)
        }
    }

    /** 하위 화면(기록 상세, 공유 편집 등)을 현재 화면 대신 띄운다. 뒤로 가기로 이전 화면에 돌아간다. */
    fun openSubScreen(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .setReorderingAllowed(true)
            .replace(R.id.fragment_container, fragment)
            .addToBackStack(null)
            .commit()
    }

    /** 바텀시트 화면(미리보기, 공유 수단)을 현재 화면 위에 겹쳐 띄운다. */
    fun openSheet(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .setReorderingAllowed(true)
            .setCustomAnimations(
                android.R.anim.fade_in, android.R.anim.fade_out,
                android.R.anim.fade_in, android.R.anim.fade_out
            )
            .add(R.id.fragment_container, fragment)
            .addToBackStack(null)
            .commit()
    }

    /** 지금 화면을 닫고 이전 화면으로 돌아간다. (상단 뒤로 버튼, 시트 닫기) */
    fun closeScreen() {
        supportFragmentManager.popBackStack()
    }

    /** 하단 탭을 코드로 선택한다. (예: 기록 상세의 "도감 보기") */
    fun selectTab(itemId: Int) {
        bottomNav.selectedItemId = itemId
    }

    /** 하단 탭에 맞는 화면으로 교체한다. 다른 탭으로 가면 열려 있던 하위 화면은 모두 닫는다. */
    private fun showTab(itemId: Int) {
        clearSubScreens()
        val fragment: Fragment = when (itemId) {
            R.id.nav_discover -> DiscoverFragment()
            R.id.nav_logs -> LogsFragment()
            R.id.nav_dex -> DexFragment()
            R.id.nav_my -> MyFragment()
            else -> HomeFragment()
        }
        supportFragmentManager.beginTransaction()
            .setReorderingAllowed(true)
            .replace(R.id.fragment_container, fragment)
            .commit()
    }

    private fun clearSubScreens() {
        supportFragmentManager.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE)
    }
}
