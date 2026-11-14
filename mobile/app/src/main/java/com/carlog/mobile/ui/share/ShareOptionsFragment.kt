package com.carlog.mobile.ui.share

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import com.carlog.mobile.MainActivity
import com.carlog.mobile.R

/** M05 공유 수단 바텀시트. 게시, 복사, 메시지, 이미지 저장 동작은 아직 연결하지 않는다. */
class ShareOptionsFragment : Fragment(R.layout.fragment_share_options) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val activity = requireActivity() as MainActivity

        bindSheetDismiss(view, R.id.scroll_share_options) { activity.closeScreen() }
        view.findViewById<View>(R.id.btn_close).setOnClickListener { activity.closeScreen() }
    }
}
