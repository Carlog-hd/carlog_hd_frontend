package com.carlog.mobile.ui.logs

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import com.carlog.mobile.MainActivity
import com.carlog.mobile.R

/** M03 내 기록 */
class LogsFragment : Fragment(R.layout.fragment_logs) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 목업 단계: 어떤 기록을 눌러도 같은 데모 기록 상세를 연다.
        listOf(R.id.log_row_1, R.id.log_row_2, R.id.log_row_3, R.id.log_row_4).forEach { id ->
            view.findViewById<View>(id).setOnClickListener {
                (requireActivity() as MainActivity).openSubScreen(LogDetailFragment())
            }
        }
    }
}
