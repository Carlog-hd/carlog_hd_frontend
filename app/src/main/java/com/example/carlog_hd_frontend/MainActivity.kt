package com.example.carlog_hd_frontend

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity

/**
 * CARLOG IVI 홈 화면. 현재는 레이아웃만 구현되어 있으며,
 * 표시되는 값(시각·속도·목적지·통계)은 carlog-demo.html 과 같은 샘플 문자열입니다.
 */
class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // 레일에서 현재 탭(홈)을 활성 상태로 표시. 선택 상태는 아이콘·라벨에도 전파됩니다.
        findViewById<android.view.View>(R.id.navHome).isSelected = true
    }
}
