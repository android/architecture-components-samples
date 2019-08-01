package com.example.android.navigationadvancedsample.sheet

/**
 * @author yyf
 * @since 06-13-2019
 */
interface IBottomSheetOperator {

    fun peek(action: Runnable = Runnable {  }, duration: Int = 300)

    fun expend(action: Runnable = Runnable {  }, duration: Int = 300)

    fun hidden(action: Runnable = Runnable {  }, duration: Int = 300)
}
