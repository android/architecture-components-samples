package com.android.samples.donuttracker.extension

import android.view.View

fun View.clickWithDebounce(debounceTime: Long = 1000L, action: () -> Unit) {
    this.setOnClickListener(object : View.OnClickListener {
        private var lastClickTime: Long = 0

        override fun onClick(v: View?) {
            if (System.currentTimeMillis() - lastClickTime < debounceTime) {
                return
            }
            action()
            lastClickTime = System.currentTimeMillis()
        }

    })
}