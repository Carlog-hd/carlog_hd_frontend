package com.carlog.mobile.ui.logs

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import com.carlog.mobile.MainActivity
import com.carlog.mobile.R
import com.carlog.mobile.ui.share.ShareEditFragment

/** M03 기록 상세 (기록 탭의 최근 로그를 누르면 열리는 하위 화면) */
class LogDetailFragment : Fragment(R.layout.fragment_log_detail) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val activity = requireActivity() as MainActivity

        view.findViewById<View>(R.id.btn_back).setOnClickListener { activity.closeScreen() }
        view.findViewById<View>(R.id.btn_go_dex).setOnClickListener { activity.selectTab(R.id.nav_dex) }
        view.findViewById<View>(R.id.btn_edit_share).setOnClickListener {
            activity.openSubScreen(ShareEditFragment())
        }
    }
}
