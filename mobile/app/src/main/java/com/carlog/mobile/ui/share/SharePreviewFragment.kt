package com.carlog.mobile.ui.share

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import com.carlog.mobile.MainActivity
import com.carlog.mobile.R

/** M05 피드 미리보기 바텀시트. 공유 편집 위에 겹쳐 뜨고, 회색 배경을 누르면 닫힌다. */
class SharePreviewFragment : Fragment(R.layout.fragment_share_preview) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val activity = requireActivity() as MainActivity

        bindSheetDismiss(view, R.id.scroll_preview) { activity.closeScreen() }

        // "이대로 공유하기": 미리보기를 닫고 공유 수단 시트를 연다.
        view.findViewById<View>(R.id.btn_share_as_is).setOnClickListener {
            activity.closeScreen()
            activity.openSheet(ShareOptionsFragment())
        }
    }
}

/**
 * 바텀시트 공통 닫기 처리.
 * 회색 배경(루트)을 누르면 닫고, 시트 안쪽을 누를 때는 닫히지 않도록 터치를 시트에서 받는다.
 */
internal fun bindSheetDismiss(root: View, sheetId: Int, onDismiss: () -> Unit) {
    root.setOnClickListener { onDismiss() }
    root.findViewById<View>(sheetId).isClickable = true
}
