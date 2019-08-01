package com.example.android.navigationadvancedsample.sheetscreen

import android.os.Bundle
import android.view.View
import androidx.navigation.fragment.navArgs
import com.example.android.navigationadvancedsample.R
import com.example.android.navigationadvancedsample.sheet.ui.BottomSheetFragment
import kotlinx.android.synthetic.main.fragment_hint_sheet.*


class HintSheetFragment : BottomSheetFragment() {
    private val args by navArgs<HintSheetFragmentArgs>()

    override fun getContainerId() = R.layout.fragment_hint_sheet

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        tvHint.text = args.hint

        btnConfirm.setOnClickListener {
            hidden()
        }
    }

    override fun getInterceptTouchType(): InterceptTouchType {
        return InterceptTouchType.All
    }
}