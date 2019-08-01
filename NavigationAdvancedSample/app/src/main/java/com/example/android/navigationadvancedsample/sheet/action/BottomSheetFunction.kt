package com.example.android.navigationadvancedsample.sheet.action

import android.graphics.RectF
import android.view.MotionEvent
import android.view.View
import com.example.android.navigationadvancedsample.KeyboardUtils
import com.example.android.navigationadvancedsample.dp2px
import com.example.android.navigationadvancedsample.px2dp
import com.example.android.navigationadvancedsample.sheet.ActionSheetLayout

/**
 * @author yyf
 * @since 06-01-2019
 * example :
 * 初始化 :
 * val sheetFunction: BottomSheetFunction = {
 *    BottomSheetFunction(actionSheet).injectView(
 *        Config().apply {
 *            hasPeekState = true
 *            peekHeight = 400
 *        }
 *    )
 * }
 * Function api :
 * sheetFunction.executeExpend(Runnable { })
 * sheetFunction.executePeek(Runnable { })
 * sheetFunction.executeHidden(Runnable { })
 *
 * ActionSheet api : 请查看 https://github.com/JarvisGG/NestedTouchScrollingLayout
 *
 */
class BottomSheetFunction(var sheet: ActionSheetLayout, private val hiddenAction: (()->Unit)? = null) : ActionFunction {

    var peekHeight = 0

    var peekHeightF = 0f

    var hasPeekState = false

    var velocityYBound = 0

    lateinit var listener: ActionFunction.StateListener

    override val provideDirection = ActionSheetLayout.SheetDirection.BOTTOM

    override val provideScrollCallback = object : ActionSheetLayout.INestChildScrollChange {

        override fun onNestChildScrollChange(deltaY: Float, velocityY: Float) {

            if (deltaY > 0 && velocityY > 0)
                KeyboardUtils.checkVisibleOnce(sheet, object : KeyboardUtils.OnKeyboardVisibilityListener {
                    override fun onVisibility(visible: Boolean) {
                        if (visible) {
                            KeyboardUtils.hideKeyboard(sheet, Runnable {
                                if (hasPeekState) {
                                    sheet.peek(sheet.height - peekHeight.dp2px)
                                }
                            }, 100)
                        }
                    }
                })
        }

        override fun onNestChildScrollRelease(deltaY: Float, velocityY: Int) {
            val totalYRange = sheet.height
            val halfHeight = peekHeight.dp2px / 2
            val halfLimit = (totalYRange - peekHeight.dp2px) / 2
            val hideLimit = totalYRange - peekHeight.dp2px / 2
            val emptyHeight = totalYRange - peekHeight.dp2px

            when {
                hasPeekState -> when {
                    velocityY > velocityYBound && velocityY > 0 -> when {
                        Math.abs(deltaY) > emptyHeight -> executeHidden()
                        else -> sheet.peek(totalYRange - peekHeight.dp2px)
                    }
                    velocityY < -velocityYBound && velocityY < 0 -> when {
                        Math.abs(deltaY) < emptyHeight -> sheet.expand()
                        else -> sheet.peek(totalYRange - peekHeight.dp2px)
                    }
                    else -> when {
                        Math.abs(deltaY) > hideLimit -> executeHidden()
                        Math.abs(deltaY) > halfLimit -> sheet.peek(totalYRange - peekHeight.dp2px)
                        else -> sheet.expand()
                    }
                }
                else -> when {
                    velocityY > velocityYBound && velocityY > 0 -> when {
                        Math.abs(deltaY) > halfHeight -> executeHidden()
                        else -> sheet.expand()
                    }
                    velocityY < -velocityYBound && velocityY < 0 -> when {
                        Math.abs(deltaY) < halfHeight -> sheet.expand()
                        else -> executeHidden()
                    }
                    else -> when {
                        Math.abs(deltaY) > halfHeight -> executeHidden()
                        Math.abs(deltaY) < halfHeight -> sheet.expand()
                    }
                }
            }
        }

        override fun onFingerUp(velocityY: Float) {
        }

        override fun onNestChildHorizationScroll(event: MotionEvent, deltaX: Float, deltaY: Float) {
        }

        override fun onNestScrollingState(@ActionSheetLayout.ScrollState state: Int) {
        }

        override fun onNestShowState(@ActionSheetLayout.ShowState state: Int) {
            listener.onState(state)
        }
    }


    private fun provideScrollCallback(config: Config): ActionSheetLayout.INestChildScrollChange =
        run {
            peekHeight = config.peekHeight
            peekHeightF = config.peekHeightF
            hasPeekState = config.hasPeekState
            velocityYBound = config.velocityYBound
            listener = config.listener
            provideScrollCallback
        }

    fun injectView(config: Config): BottomSheetFunction =
        apply {
            sheet.run {
                registerNestScrollChildCallback(provideScrollCallback(config))
                setSheetDirection(provideDirection)
                visibility = View.INVISIBLE
                setInterceptTouchArea(config.interceptTouchArea)
                post { hidden(Runnable { visibility = View.VISIBLE }, 0) }
            }
    }

    fun releaseView() {
        sheet.removeNestScrollChildCallback(provideScrollCallback)
    }

    fun ActionSheetLayout.open(action: Runnable = Runnable {  }, duration: Int = 300, isNeedNotify: Boolean = true) {
        peekHeight = if (peekHeight > 0) peekHeight else sheet.height.px2dp
        if(peekHeightF > 0) {
            peekHeight = (sheet.height * peekHeightF).px2dp
        }
        if (hasPeekState) {
            peek(sheet.height - peekHeight.dp2px, action, duration, isNeedNotify)
        } else {
            expand(action, duration, isNeedNotify)
        }
    }

    /**
     * 开启半窗
     */
    fun executePeek(action: Runnable = Runnable {  }, duration: Int = 300, isNeedNotify: Boolean = true) {
        sheet.post {
            sheet.open(action, duration, isNeedNotify)
        }
    }

    /**
     * 开启全屏
     */
    fun executeExpend(action: Runnable = Runnable {  }, duration: Int = 300, isNeedNotify: Boolean = true) {
        sheet.post {
            sheet.expand(action, duration, isNeedNotify)
        }
    }

    /**
     * 收起
     */
    fun executeHidden(action: Runnable = Runnable {  }, duration: Int = 300, isNeedNotify: Boolean = true) {
        sheet.post {
            sheet.hidden(Runnable {
                action.run()
                hiddenAction?.invoke()
            }, duration, isNeedNotify)
        }
    }

    /**
     * BottomSheet 属性
     * hasPeekState : 是否存在中间态
     * peekHeight : 中间态弹起高度，默认布局 1 / 1 高度
     * velocityYBound :  根据手指脱离速度判断是否切换状态
     * listener : 当前弹窗状态的回掉
     */
    class Config {
        var peekHeight = 0
        var peekHeightF = 0f
        var hasPeekState = false
        var velocityYBound = 10000
        var interceptTouchArea: RectF = RectF()
        var listener: ActionFunction.StateListener = object : ActionFunction.StateListener {
            override fun onState(@ActionSheetLayout.ShowState state: Int) {

            }
        }
    }
}