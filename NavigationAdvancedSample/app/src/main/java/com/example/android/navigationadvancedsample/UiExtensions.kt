package com.example.android.navigationadvancedsample

import android.content.res.Resources
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController

/**
 * @author yyf
 * @since 07-31-2019
 */
val Number.dp2px get() = (toInt() * Resources.getSystem().displayMetrics.density).toInt()
val Number.px2dp get() = (toInt() / Resources.getSystem().displayMetrics.density).toInt()
val Number.sp2px get() = (toInt() * Resources.getSystem().displayMetrics.scaledDensity).toInt()
fun Fragment.findMainNavController() = requireActivity().findNavController(R.id.frag_nav_host)