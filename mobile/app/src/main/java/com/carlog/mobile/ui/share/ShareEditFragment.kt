package com.carlog.mobile.ui.share

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import com.carlog.mobile.MainActivity
import com.carlog.mobile.R

/** M04 공유 편집 (기록 상세의 "편집하고 공유하기"를 누르면 열리는 하위 화면) */
class ShareEditFragment : Fragment(R.layout.fragment_share_edit) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val activity = requireActivity() as MainActivity

        view.findViewById<View>(R.id.btn_back).setOnClickListener { activity.closeScreen() }
        view.findViewById<View>(R.id.btn_preview).setOnClickListener {
            activity.openSheet(SharePreviewFragment())
        }
        view.findViewById<View>(R.id.btn_share).setOnClickListener {
            activity.openSheet(ShareOptionsFragment())
        }
    }
}
