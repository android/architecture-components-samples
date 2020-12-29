/*
 * Copyright (C) 2017 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.android.example.github.util

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import androidx.fragment.app.Fragment

/**
 * Disables progress bar animations for all views in the fragment
 */
fun Fragment.disableProgressBarAnimations() {
    // traverse all views, if any is a progress bar, replace its animation
    traverseViews(requireView())
}

private fun traverseViews(view: View?) {
    if (view is ViewGroup) {
        traverseViewGroup(view)
    } else if (view is ProgressBar) {
        disableProgressBarAnimation(view)
    }
}

private fun traverseViewGroup(view: ViewGroup) {
    val count = view.childCount
    (0 until count).forEach {
        traverseViews(view.getChildAt(it))
    }
}

/**
 * necessary to run tests on older API levels where progress bar uses handler loop to animate.
 *
 * @param progressBar The progress bar whose animation will be swapped with a drawable
 */
private fun disableProgressBarAnimation(progressBar: ProgressBar) {
    progressBar.indeterminateDrawable = ColorDrawable(Color.BLUE)
}
