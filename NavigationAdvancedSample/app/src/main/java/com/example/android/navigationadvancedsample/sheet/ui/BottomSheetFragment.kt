package com.example.android.navigationadvancedsample.sheet.ui

import android.graphics.RectF
import android.os.Bundle
import android.view.*
import android.widget.FrameLayout
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import androidx.core.view.children
import androidx.fragment.app.Fragment
import com.example.android.navigationadvancedsample.sheet.ActionSheetLayout
import com.example.android.navigationadvancedsample.sheet.IBottomSheetOperator
import com.example.android.navigationadvancedsample.sheet.action.ActionFunction
import com.example.android.navigationadvancedsample.sheet.action.BottomSheetFunction
import com.example.android.navigationadvancedsample.R
import com.example.android.navigationadvancedsample.dp2px
import com.example.android.navigationadvancedsample.findMainNavController


/**
 * @author yyf
 * @since 06-01-2019
 */

abstract class BottomSheetFragment: Fragment(), IBottomSheetOperator {

    sealed class InterceptTouchType {

        // 拦截全部
        object All: InterceptTouchType()
        // 拦截 TitleBar + 操作栏 12 dp
        object Default: InterceptTouchType()
        // 自定义
        object Other: InterceptTouchType()
    }

    private var isInitBottomSheetHidden = true

    private var topIndicatorLineHeight = 12.dp2px

    private lateinit var sheetFunction: BottomSheetFunction

    private lateinit var actionSheet: ActionSheetLayout

    override fun peek(action: Runnable, duration: Int) {
        sheetFunction.executePeek(action, duration)
    }

    /**
     * 开启全屏
     */
    override fun expend(action: Runnable, duration: Int) {
        sheetFunction.executeExpend(action, duration)
    }

    /**
     * 收起
     */
    override fun hidden(action: Runnable, duration: Int) {
        sheetFunction.executeHidden(Runnable {
            action.run()
        }, duration)
    }

    fun getSheetFunction(): BottomSheetFunction = sheetFunction

    fun popBack() {
        findMainNavController().navigateUp()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {

        val containerView = inflater.inflate(getContainerId(), container, false).apply {
            setOnClickListener {  }
        }
        val rootView = inflater.inflate(R.layout.fragment_bottom_sheet, container, false) as ConstraintLayout
        val shadow = rootView.findViewById<View>(R.id.shadow)

        actionSheet = rootView.findViewById(R.id.actionSheet)
        actionSheet.apply {
            layoutParams = ((layoutParams as ConstraintLayout.LayoutParams).apply {
                height = if (isHasPeekState()) {
                    ConstraintLayout.LayoutParams.MATCH_PARENT
                } else {
                    ConstraintLayout.LayoutParams.WRAP_CONTENT
                }
            })

            val constraintSet = ConstraintSet()
            constraintSet.clone(rootView)
            if (isHasPeekState()) {
                constraintSet.connect(
                    R.id.actionSheet,
                    ConstraintSet.TOP,
                    R.id.bottomSheetFragmentRoot,
                    ConstraintSet.TOP,
                    0)

                rootView.setPadding(0, 0, 0, 0)
            }
            constraintSet.applyTo(rootView)

            addView(FrameLayout(context).apply {
                setBackgroundResource(R.drawable.sheet_bg)
                elevation = 10f
            }.apply {
                addView(containerView, FrameLayout.LayoutParams(
                    FrameLayout.LayoutParams.MATCH_PARENT,
                    FrameLayout.LayoutParams.MATCH_PARENT
                ).apply {
                    this.topMargin = topIndicatorLineHeight
                })

                addView(View(context).apply {
                    setBackgroundResource(R.drawable.indicator_line)
                }, FrameLayout.LayoutParams(
                    48.dp2px,
                    4.dp2px
                ).apply {
                    this.gravity = Gravity.CENTER_HORIZONTAL
                    this.topMargin = 6.dp2px
                })
            }, FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT
            ))
        }

        actionSheet.registerNestScrollChildCallback(object: ActionSheetLayout.INestChildScrollChange {
            override fun onNestChildScrollChange(deltaY: Float, velocityY: Float) {
                val value = 1 - (deltaY / (actionSheet.measuredHeight * (1 - getPeekHeightF())))
                shadow.alpha = if (value >= 0 && isShowShadow()) {
                    0.35f * value
                } else {
                    0f
                }
            }

            override fun onNestChildScrollRelease(deltaY: Float, velocityY: Int) {
            }

            override fun onFingerUp(velocityY: Float) {
            }

            override fun onNestChildHorizationScroll(event: MotionEvent, deltaX: Float, deltaY: Float) {
            }

            override fun onNestScrollingState(state: Int) {
            }

            override fun onNestShowState(state: Int) {
            }

        })

        // 用 post 是因为要拿到真实的 measureHeight
        actionSheet.post {
            when (getInterceptTouchType()) {
                InterceptTouchType.All -> {
                    actionSheet.setInterceptTouchArea(rectF = RectF(0f, 0f, actionSheet.measuredWidth.toFloat(),
                        actionSheet.measuredHeight.toFloat()))
                }
                else -> {
                    actionSheet.setInterceptTouchArea(rectF = getInterceptTouchArea())
                }
            }
        }

        shadow.run {
            alpha = 0f
            if (!isHasPeekState()) {
                setOnClickListener { hidden() }
            }
        }
        return rootView
    }

    /**
     * 默认有 titlebar 的 操作区域高度 tb.height + 12
     * 否则 操作区域高度 12
     */
    private fun <T> getActionLayoutHasViewType(node: View, type: Class<T>): View? {
        if (type == node.javaClass) {
            return node
        }
        if (node is ViewGroup) {
            for (child in node.children) {
                val target = getActionLayoutHasViewType(child, type)
                if (target != null) {
                    return target
                }
            }
        }
        return null
    }

    /**
     * 选择拦截类型
     */
    open fun getInterceptTouchType(): InterceptTouchType {
        return InterceptTouchType.Default
    }

    /**
     * 自定义拦截区域 Default， All， Other
     */
    open fun getInterceptTouchArea(): RectF {
        return RectF(-1f, -1f, -1f, -1f)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        sheetFunction = BottomSheetFunction(actionSheet) {
            // Hidden 时执行的操作
            fragmentManager?.beginTransaction()
                ?.remove(this@BottomSheetFragment)
                ?.commitNowAllowingStateLoss()

        }.injectView(
            BottomSheetFunction.Config().apply {
                hasPeekState = isHasPeekState()
                peekHeight = getPeekHeight()
                peekHeightF = getPeekHeightF()
                listener = getListener()
            }
        )
        actionSheet.registerOnInterceptTouchListener(object: ActionSheetLayout.INestInterceptTouchListener {
            override fun onInterceptTouchListener(event: MotionEvent): Boolean {
                return onInterceptTouch(event)
            }
        })
        initOperator()
        registerAutoChangeStateByLayoutChange()
    }

    private fun clearDeepFocus(parent: View) {
        parent.clearFocus()
        if (parent is ViewGroup) {
            parent.children.forEach {
                clearDeepFocus(it)
            }
        }
    }

    /**
     * 是否开启自动处理 Touch 事件，默认统一交给 sheet
     */
    open fun onInterceptTouch(event: MotionEvent): Boolean {
        return false
    }

    protected fun registerActionSheetListener(listener: ActionSheetLayout.INestChildScrollChange) {
        actionSheet.registerNestScrollChildCallback(listener)
    }

    /**
     * 默认启动就弹起，可以添加 runnable 和弹起时间 peek(Runnable {}, duration)
     */
    open fun initOperator() {
        peek()
    }

    /**
     * 是否开启遮照
     */
    open fun isShowShadow(): Boolean = true

    /**
     * container id
     */
    abstract fun getContainerId() : Int

    /**
     * 是否开启中间态
     */
    open fun isHasPeekState(): Boolean = false

    /**
     * 窗口状态
     */
    open fun getListener(): ActionFunction.StateListener = object : ActionFunction.StateListener {
        override fun onState(@ActionSheetLayout.ShowState state: Int) {
            when (state) {
                ActionSheetLayout.ShowState.HIDE -> if (isInitBottomSheetHidden) { isInitBottomSheetHidden = false } else { popBack() }
            }
        }
    }

    /**
     * 中间态高度
     */
    @Deprecated("use getPeekHeightF now", ReplaceWith("0"))
    open fun getPeekHeight() : Int  = 0

    open fun getPeekHeightF() : Float  = 0f

    override fun onDestroyView() {
        super.onDestroyView()
        sheetFunction.releaseView()
    }

    /**
     * Layout 发生变化 弹窗状态自我修正
     */
    open fun registerAutoChangeStateByLayoutChange() {
            actionSheet.addOnLayoutChangeListener { view, left, top, right, bottom,
                                                    oldLeft, oldRight, oldTop, oldBottom ->
                if (bottom <= 0 || oldBottom <= 0)
                    return@addOnLayoutChangeListener

                if (isHasPeekState()) {
                    if (bottom != oldBottom) {
                        if (bottom < oldBottom) {
                            expend()
                        } else if (bottom > oldBottom) {
                            actionSheet.setParentDispatchTouchEvent(false)
                        }
                    }
                } else {
                    if (bottom != oldBottom) {
                        peek(duration = 0)
                    }
                }
                onActionSheetLayoutChange(
                    view, left, top, right, bottom,
                    oldLeft, oldRight, oldTop, oldBottom
                )
            }

    }

    open fun onActionSheetLayoutChange(
        v: View, left: Int, top: Int, right: Int, bottom: Int,
        oldLeft: Int, oldTop: Int, oldRight: Int, oldBottom: Int
    ) {}
}