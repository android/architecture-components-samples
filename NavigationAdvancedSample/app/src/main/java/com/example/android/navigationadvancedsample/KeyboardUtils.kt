package com.example.android.navigationadvancedsample

import android.content.Context
import android.graphics.Rect
import android.preference.PreferenceManager
import android.view.View
import android.view.inputmethod.InputMethodManager

/**
 * @author yyf
 * @since 06-04-2019
 */
object KeyboardUtils {
    private const val KEY_KEYBOARD_HEIGHT = "KEY_KEYBOARD_HEIGHT"
    private const val DEFAULT_KEYBOARD_HEIGHT = 240

    fun getKeyboardHeight(context: Context): Int {
        val defaultHeight = DEFAULT_KEYBOARD_HEIGHT.dp2px
        return getKeyboardHeight(context, defaultHeight)
    }

    fun getKeyboardHeight(context: Context, fallback: Int): Int {
        return PreferenceManager.getDefaultSharedPreferences(context).getInt(KEY_KEYBOARD_HEIGHT, fallback)
    }

    fun setKeyboardHeight(context: Context, height: Int) {
        if (height > 0) {
            PreferenceManager.getDefaultSharedPreferences(context).edit().putInt(KEY_KEYBOARD_HEIGHT, height).apply()
        }
    }

    @JvmOverloads
    fun showKeyboard(view: View, callback: Runnable? = null, delay: Long = 0L) {
        view.post {
            view.requestFocus()
            val manager = view.context
                .getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            manager.showSoftInput(view, InputMethodManager.SHOW_FORCED)

            view.postDelayed({ callback?.run() }, delay)
        }
    }

    @JvmOverloads
    fun hideKeyboard(view: View, callback: Runnable? = null, delay: Long = 0L) {
        view.post{
            view.clearFocus()
            val manager = view.context
                .getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            manager.hideSoftInputFromWindow(view.windowToken, 0)
            view.postDelayed({ callback?.run() }, delay)
        }
    }

    interface OnKeyboardVisibilityListener {
        fun onVisibility(visible: Boolean)
    }

    fun checkVisibleOnce(view: View, listener: OnKeyboardVisibilityListener?) {
        val r = Rect()
        view.getWindowVisibleDisplayFrame(r)
        val screenHeight = view.rootView.height
        val keypadHeight = screenHeight - r.bottom
        if (keypadHeight > screenHeight * 0.15) {
            listener?.onVisibility(true)
        } else {
            listener?.onVisibility(false)
        }
    }
}