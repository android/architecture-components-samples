package com.example.android.navigationadvancedsample.sheet

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.content.Context
import android.graphics.RectF
import android.util.AttributeSet
import android.util.Property
import android.view.*
import android.view.animation.DecelerateInterpolator
import android.webkit.WebView
import android.widget.FrameLayout
import androidx.annotation.IntDef
import androidx.annotation.NonNull
import androidx.annotation.Nullable
import androidx.core.view.NestedScrollingParent
import androidx.core.view.animation.PathInterpolatorCompat
import java.lang.reflect.InvocationTargetException
import java.util.ArrayList
import kotlin.annotation.Retention

/**
 * @author yyf @ JarvisGG.io
 * @since 10-16-2018
 * @function 无缝拖拽 parentView，假如 childView 可以滚动，犟 touch dispatch 给它，假如不可以，当前会自己消化 touch 事件
 */
open class ActionSheetLayout : FrameLayout, NestedScrollingParent {

    private var mChildView: View? = null

    private var mTransYAnim: ObjectAnimator? = null

    private var currentAnimator: ObjectAnimator? = null

    private var velocityTracker: VelocityTracker? = null

    private val minFlingVelocity: Float = 0.toFloat()

    private var mTouchSlop: Float = 0.toFloat()

    private var mDownY: Float = 0.toFloat()

    private var mDownX: Float = 0.toFloat()

    private var mDownSheetTranslation: Float = 0.toFloat()

    private var mOriginTranslate = 0f

    /**
     * 假如 cover BottomSheet 场景，sheetView ** 会 ** 从哪个方向弹出
     */
    @SheetDirection
    private var mSheetDirection = SheetDirection.ALL

    /**
     * 当前 Layout 展示状态
     */
    /**
     * 获得 bottom_sheet 状态
     * @return
     */
    @ShowState
    @get:ShowState
    var showState = ShowState.INIT
        private set

    /**
     * 当前拖拽状态
     */
    /**
     * 获得 scroll_state 状态
     * @return
     */
    @ScrollState
    @get:ScrollState
    var scrollState = ScrollState.SCROLL_STATE_SETTLING
        private set

    /**
     * 手指向上阻尼值
     */
    private var mDampingUp = 1f

    /**
     * 手指向下阻尼值
     */
    private var mDampingDown = 1f

    /**
     * 键盘收起，导致 reLayout，getHeight 发生改变，所以一开始就锁定高度
     */
    private var mTouchParentViewOriginMeasureHeight = 0

    /**
     * 针对包含的子 View 为 webview 的情况
     */
    private var mWebViewContentHeight: Int = 0

    /**
     * 横向拖拽 dispatchTouch 给 childView
     */
    private var isVerticalDirectionHolderTouch: Boolean = false

    /**
     * 竖向拖拽 NestedTouchScrollingLayout 是否消化 touch（根据 childView (canScrollUp or canScrollDown)）
     */
    private var isActionSheetHoldTouchEvent = true

    private var mSheetTranslation: Float = 0.toFloat()

    /**
     * 记录事件流起点 ActionDown
     */
    private var isFingerHolderTouch = false

    /**
     * 是否拦截 Touch 事件，默认开启
     */
    private var isActionSheetDispatchTouchEvent = true

    private var mNestChildScrollChangeCallbacks: MutableList<INestChildScrollChange>? = null

    private var mNestChildDispatchTouchEvent: INestChildDispatchTouchEvent? = null

    private var mNestInterceptTouchListener: INestInterceptTouchListener? = null

    /**
     * 是否开始锁定顶部高度
     */
    private var isLockTop = false

    /**
     * 顶部锁定高度
     */
    private var mLockTopTranslateY = 0

    /**
     * 是否开始锁定底部高度
     */
    private var isLockBottom = false

    /**
     * 底部锁定高度
     */
    private var mLockBottomTranslateY = 0

    /**
     * 可拦截区域
     */
    private var mInterceptTouchArea = RectF()

    private val mTranslation =
        object : Property<ActionSheetLayout, Float>(Float::class.java, "sheetTranslation") {
            override fun get(obj : ActionSheetLayout): Float {
                return mTouchParentViewOriginMeasureHeight - obj.mSheetTranslation
            }

            override fun set(obj: ActionSheetLayout, value: Float?) {
                obj.seAnimtTranslation(value!!)
            }
        }

    /**
     * 动画是否正在执行
     * @return
     */
    private val isAnimating: Boolean
        get() = currentAnimator != null && currentAnimator!!.isRunning

    /**
     * 允许拖拽方向
     */
    @Target(AnnotationTarget.TYPE, AnnotationTarget.PROPERTY, AnnotationTarget.CLASS, AnnotationTarget.VALUE_PARAMETER)
    @IntDef(
            SheetDirection.ALL,
            SheetDirection.TOP,
            SheetDirection.BOTTOM
    )
    @Retention(AnnotationRetention.SOURCE)
    annotation class SheetDirection {
        companion object {
            const val ALL = 0x000
            const val TOP = 0x001
            const val BOTTOM = 0x002
        }
    }

    /**
     * bottom_sheet state
     */
    @IntDef(
            ShowState.HIDE,
            ShowState.PEEK,
            ShowState.EXTEND,
            ShowState.INIT
    )
    @Retention(AnnotationRetention.SOURCE)
    annotation class ShowState {
        companion object {
            const val HIDE = 0x000
            const val PEEK = 0x001
            const val EXTEND = 0x002
            const val INIT = 0x003
        }
    }

    /**
     * scrolling state
     */
    @IntDef(
            ScrollState.SCROLL_STATE_DRAGGING,
            ScrollState.SCROLL_STATE_SETTLING
    )
    @Retention(AnnotationRetention.SOURCE)
    annotation class ScrollState {
        companion object {
            const val SCROLL_STATE_DRAGGING = 0x000
            const val SCROLL_STATE_SETTLING = 0x001
        }
    }

    interface INestChildScrollChange {
        /**
         * nestChild scroll change
         * @param deltaY
         */
        fun onNestChildScrollChange(deltaY: Float, velocityY: Float)

        fun onNestChildScrollRelease(deltaY: Float, velocityY: Int)

        fun onFingerUp(velocityY: Float)

        fun onNestChildHorizationScroll(event: MotionEvent, deltaX: Float, deltaY: Float)

        fun onNestScrollingState(@ScrollState state: Int)

        fun onNestShowState(@ShowState state: Int)
    }

    interface INestChildDispatchTouchEvent {
        fun dispatchWrapperTouchEvent(event: MotionEvent)
    }

    interface INestInterceptTouchListener {
        fun onInterceptTouchListener(event: MotionEvent) : Boolean
    }

    constructor(@NonNull context: Context) : super(context) {
        init()
    }

    constructor(@NonNull context: Context, @Nullable attrs: AttributeSet) : super(context, attrs) {
        init()
    }

    constructor(@NonNull context: Context, @Nullable attrs: AttributeSet, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    ) {
        init()
    }

    private fun init() {
        mNestChildScrollChangeCallbacks = ArrayList()
        mTouchSlop = ViewConfiguration.get(context).scaledTouchSlop.toFloat()
        post { mTouchParentViewOriginMeasureHeight = this@ActionSheetLayout.measuredHeight }
    }

    override fun onFinishInflate() {
        super.onFinishInflate()
        if (childCount > 1) {
            throw IllegalStateException("child must be 1!!!")
        }
        mChildView = getChildAt(0)
    }

    override fun addView(child: View) {
        if (childCount >= 1) {
            throw IllegalStateException("child must be 1!!!")
        }
        mChildView = child
        super.addView(child)
    }

    override fun addView(child: View, params: ViewGroup.LayoutParams) {
        if (childCount >= 1) {
            throw IllegalStateException("child must be 1!!!")
        }
        mChildView = child
        super.addView(child, params)
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        velocityTracker = VelocityTracker.obtain()
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        clearNestScrollChildCallback()
        velocityTracker!!.clear()
    }

    override fun onInterceptTouchEvent(ev: MotionEvent): Boolean {
        // warning 这里简单对 弹窗定制一下
        isActionSheetDispatchTouchEvent = true

        return if (isActionSheetDispatchTouchEvent &&
            !isTouchUnderNestedTouchScrollingView(this.getChildAt(0), ev) &&
            isTouchUnderChildViewAndInterceptTouchArea(getChildAt(0), ev) &&
            mNestInterceptTouchListener?.onInterceptTouchListener(ev) != true
        ) {
            true
        } else {
            super.onInterceptTouchEvent(ev)
        }
    }

    private fun isTouchUnderChildViewAndInterceptTouchArea(targetChildView: View, event: MotionEvent): Boolean {
        val location = IntArray(2)
        targetChildView.getLocationOnScreen(location)
        return mInterceptTouchArea.contains(event.rawX - location[0], event.rawY - location[1])
    }

    private fun isTouchUnderNestedTouchScrollingView(view: View, event: MotionEvent): Boolean {
        val clazz = view.javaClass
        if (clazz.simpleName == ActionSheetLayout::class.java.simpleName) {
            try {
                val m = clazz.getDeclaredMethod("getShowState")
                m.isAccessible = true
                val result = m.invoke(view)
                if (result is Int) {
                    if (0x000 == result) {
                        return false
                    }
                }
            } catch (e: NoSuchMethodException) {
                e.printStackTrace()
            } catch (e: IllegalAccessException) {
                e.printStackTrace()
            } catch (e: InvocationTargetException) {
                e.printStackTrace()
            }
            return isTouchUnderChildViewAndInterceptTouchArea(view, event)
        }
        if (view is ViewGroup) {
            var res = false
            for (i in 0 until view.childCount) {
                if (isTouchUnderNestedTouchScrollingView(view.getChildAt(i), event)) {
                    res = true
                }
            }
            return res
        }
        return false
    }

    /**
     * 不拦截 Touch 事件的几种情况
     * 1.不开启父亲布局拦截.
     * 2.当前子 View 为 null
     * 3.当前 Touch 事件没用作用到子 View
     * @param event
     * @return
     */
    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent): Boolean {
        var velocityY = 0f

        // 保证 down 落在制定区域，其他后续事件不需要强制在指定区域
        if (event.action == MotionEvent.ACTION_DOWN && isTouchUnderChildViewAndInterceptTouchArea(getChildAt(0), event)) {
            isFingerHolderTouch = true
        }

        if (!isActionSheetDispatchTouchEvent ||
            !isFingerHolderTouch) {
            return super.onTouchEvent(event)
        }
        if (isAnimating) {
            return false
        }

        if (event.action == MotionEvent.ACTION_DOWN) {

            mOriginTranslate = mChildView!!.translationY
            mTouchParentViewOriginMeasureHeight = this.measuredHeight

            isVerticalDirectionHolderTouch = false
            mDownY = event.y
            mDownX = event.x
            mSheetTranslation = mTouchParentViewOriginMeasureHeight - mOriginTranslate
            mDownSheetTranslation = mSheetTranslation
            velocityTracker!!.clear()


            initWebViewContentHeight(getChildAt(0), event)
        }

        velocityTracker!!.addMovement(event)

        if (event.action == MotionEvent.ACTION_UP || event.action == MotionEvent.ACTION_CANCEL) {
            isFingerHolderTouch = false
            velocityTracker!!.computeCurrentVelocity(1000)
            velocityY = velocityTracker!!.yVelocity
            notifyOnFingerUp(velocityY)
        }

        parent.requestDisallowInterceptTouchEvent(true)

        val maxSheetTranslation = mTouchParentViewOriginMeasureHeight.toFloat()

        var deltaY = mDownY - event.y
        var deltaX = mDownX - event.x

        if (deltaY > 0) {
            deltaY *= mDampingDown
        } else if (deltaY < 0) {
            deltaY *= mDampingUp
        }

        if (!isVerticalDirectionHolderTouch) {
            isVerticalDirectionHolderTouch = Math.abs(deltaY) > ViewConfiguration.get(context).scaledTouchSlop && Math.abs(deltaY) > Math.abs(deltaX)
            deltaY = 0f
            deltaX = 0f
        }

        var newSheetTranslation = mDownSheetTranslation + deltaY

        dispatchWrapperTouchEvent(event)

        if (isVerticalDirectionHolderTouch) {

            if (isActionSheetHoldTouchEvent && !isChildCanScroll(event, deltaY) && deltaY != 0f) {
                mDownY = event.y
                velocityTracker!!.clear()
                isActionSheetHoldTouchEvent = false
                newSheetTranslation = mSheetTranslation

                val cancelEvent = MotionEvent.obtain(event)
                cancelEvent.action = MotionEvent.ACTION_CANCEL
                getChildAt(0).dispatchTouchEvent(cancelEvent)
                cancelEvent.recycle()
            }

            if (!isActionSheetHoldTouchEvent && isChildCanScroll(event, deltaY) && deltaY != 0f) {
                setSheetTranslation(maxSheetTranslation, 0f)
                isActionSheetHoldTouchEvent = true
                if (event.action == MotionEvent.ACTION_MOVE) {
                    val downEvent = MotionEvent.obtain(event)
                    downEvent.action = MotionEvent.ACTION_DOWN
                    getChildAt(0).dispatchTouchEvent(downEvent)
                    downEvent.recycle()

                    notifyOnFingerUp(0f)
                    notifyNestScrollingStateCallback(ScrollState.SCROLL_STATE_SETTLING)
                }
            }

            if (isActionSheetHoldTouchEvent && deltaY != 0f) {
                event.offsetLocation(0f, mSheetTranslation - mTouchParentViewOriginMeasureHeight)
                getChildAt(0).dispatchTouchEvent(event)

                notifyNestScrollingStateCallback(ScrollState.SCROLL_STATE_SETTLING)
            } else {

                if (isLockTop) {
                    newSheetTranslation =
                        if (newSheetTranslation > mLockTopTranslateY) mLockTopTranslateY.toFloat() else newSheetTranslation
                }

                if (isLockBottom) {
                    newSheetTranslation =
                        if (newSheetTranslation < mLockBottomTranslateY) mLockBottomTranslateY.toFloat() else newSheetTranslation
                }

                setSheetTranslation(newSheetTranslation, deltaY)

                if (event.action == MotionEvent.ACTION_UP || event.action == MotionEvent.ACTION_CANCEL) {
                    isActionSheetHoldTouchEvent = true
                    parent.requestDisallowInterceptTouchEvent(false)

                    if (Math.abs(velocityY) < minFlingVelocity) {
                        if (mSheetTranslation > height / 2) {
                        } else {
                        }
                    } else {
                        if (velocityY < 0) {
                        } else {
                        }
                    }
                    notifyNestScrollChildReleaseCallback(velocityY.toInt())
                }
            }
        } else {
            event.offsetLocation(0f, mSheetTranslation - mTouchParentViewOriginMeasureHeight)
            getChildAt(0).dispatchTouchEvent(event)
        }
        return true
    }

    private fun isChildCanScroll(event: MotionEvent, deltaY: Float): Boolean {
        val fingerDown = deltaY - mOriginTranslate < 0
        val canScrollDown = canScrollDown(getChildAt(0), event, event.x, event.y + (mSheetTranslation - height), false)
        val fingerUp = deltaY - mOriginTranslate > 0
        val canScrollUp = canScrollUp(getChildAt(0), event, event.x, event.y + (mSheetTranslation - height), false)
        return fingerDown && canScrollUp || fingerUp && canScrollDown
    }

    /**
     * child can scroll
     * @param view
     * @param event
     * @param x
     * @param y
     * @param lockRect 是否开启 touch 所动在当前 view 区域
     * @return
     */
    protected fun canScrollUp(view: View, event: MotionEvent, x: Float, y: Float, lockRect: Boolean): Boolean {

        if (view is WebView) {
            return canWebViewScrollUp(view)
        }
        if (view is ViewGroup) {
            for (i in 0 until view.childCount) {
                val child = view.getChildAt(i)
                val childLeft = child.left - view.getScrollX()
                val childTop = child.top - view.getScrollY()
                val childRight = child.right - view.getScrollX()
                val childBottom = child.bottom - view.getScrollY()
                val intersects = x > childLeft && x < childRight && y > childTop && y < childBottom
                if ((!lockRect || intersects) && canScrollUp(child, event, x - childLeft, y - childTop, lockRect)) {
                    return true
                }
            }
        }


        return isTouchUnderNestedTouchScrollingView(view, event) && view.canScrollVertically(-1)
    }

    /**
     * child can scroll
     * @param view
     * @param event
     * @param x
     * @param y
     * @param lockRect 是否开启 touch 所动在当前 view 区域
     * @return
     */
    protected fun canScrollDown(view: View, event: MotionEvent, x: Float, y: Float, lockRect: Boolean): Boolean {
        if (view is WebView) {
            return canWebViewScrollDown(view)
        }
        if (view is ViewGroup) {
            for (i in 0 until view.childCount) {
                val child = view.getChildAt(i)
                val childLeft = child.left - view.getScrollX()
                val childTop = child.top - view.getScrollY()
                val childRight = child.right - view.getScrollX()
                val childBottom = child.bottom - view.getScrollY()
                val intersects = x > childLeft && x < childRight && y > childTop && y < childBottom
                if ((!lockRect || intersects) && canScrollDown(child, event, x - childLeft, y - childTop, lockRect)) {
                    return true
                }
            }
        }

        return isTouchUnderNestedTouchScrollingView(view, event) && view.canScrollVertically(1)
    }

    private fun canScrollLeft(view: View, x: Float, y: Float): Boolean {
        if (view is ViewGroup) {
            for (i in 0 until view.childCount) {
                val child = view.getChildAt(i)
                val childLeft = child.left - view.getScrollX()
                val childTop = child.top - view.getScrollY()
                val childRight = child.right - view.getScrollX()
                val childBottom = child.bottom - view.getScrollY()
                val intersects = x > childLeft && x < childRight && y > childTop && y < childBottom
                if (intersects && canScrollLeft(child, x - childLeft, y - childTop)) {
                    return true
                }
            }
        }
        return view.canScrollHorizontally(-1)
    }

    private fun canScrollRight(view: View, x: Float, y: Float): Boolean {
        if (view is ViewGroup) {
            for (i in 0 until view.childCount) {
                val child = view.getChildAt(i)
                val childLeft = child.left - view.getScrollX()
                val childTop = child.top - view.getScrollY()
                val childRight = child.right - view.getScrollX()
                val childBottom = child.bottom - view.getScrollY()
                val intersects = x > childLeft && x < childRight && y > childTop && y < childBottom
                if (intersects && canScrollRight(child, x - childLeft, y - childTop)) {
                    return true
                }
            }
        }
        return view.canScrollHorizontally(1)
    }

    /**
     * 规避 contentHeight 异步变化
     * @return
     */
    private fun canWebViewScrollUp(webView: WebView): Boolean {
        if (mWebViewContentHeight == 0) {
            mWebViewContentHeight = (webView.contentHeight * webView.scale).toInt()
        }
        val offset = webView.scrollY
        val range = mWebViewContentHeight - webView.height
        return if (range == 0) {
            false
        } else offset > 2
    }

    /**
     * 规避 contentHeight 异步变化
     * @return
     */
    private fun canWebViewScrollDown(webView: WebView): Boolean {
        if (mWebViewContentHeight == 0) {
            mWebViewContentHeight = (webView.contentHeight * webView.scale).toInt()
        }
        val offset = webView.scrollY
        val range = mWebViewContentHeight - webView.height
        return if (range == 0) {
            false
        } else offset < range - 2
    }

    private fun dispatchWrapperTouchEvent(event: MotionEvent) {
        val motionEvent = MotionEvent.obtain(event)
        motionEvent.offsetLocation(0f, mSheetTranslation - mTouchParentViewOriginMeasureHeight)
        if (mNestChildDispatchTouchEvent != null) {
            mNestChildDispatchTouchEvent!!.dispatchWrapperTouchEvent(motionEvent)
        }
    }

    private fun initWebViewContentHeight(view: View, event: MotionEvent) {
        if (view is WebView && isTouchUnderNestedTouchScrollingView(view, event)) {
            mWebViewContentHeight = (view.contentHeight * view.scale).toInt()
            return
        }
        if (view is ViewGroup) {
            for (i in 0 until view.childCount) {
                initWebViewContentHeight(view.getChildAt(i), event)
            }
        }
    }


    private fun setSheetTranslation(newTranslation: Float, deltaY: Float) {
        this.mSheetTranslation = newTranslation
        val bottomClip = (mTouchParentViewOriginMeasureHeight - Math.ceil(mSheetTranslation.toDouble())).toInt()
        velocityTracker!!.computeCurrentVelocity(1000)
        setTranslation(bottomClip.toFloat(), velocityTracker!!.yVelocity)
    }

    private fun seAnimtTranslation(transY: Float) {
        this.mSheetTranslation = mTouchParentViewOriginMeasureHeight - transY
        setTranslation(transY, 0f)
    }

    private fun setTranslation(transY: Float, velocityY: Float) {
        if (mSheetDirection == SheetDirection.BOTTOM && transY < 0) {
            mChildView!!.translationY = 0f
            notifyNestScrollChildChangeCallback(0f, velocityY)
            return
        }
        if (mSheetDirection == SheetDirection.TOP && transY > 0) {
            mChildView!!.translationY = 0f
            notifyNestScrollChildChangeCallback(0f, velocityY)
            return
        }
        notifyNestScrollChildChangeCallback(transY, velocityY)
        notifyNestScrollingStateCallback(ScrollState.SCROLL_STATE_DRAGGING)
        if (mChildView != null) {
            mChildView!!.translationY = transY
        }
        if (transY == 0f) {
            mDownSheetTranslation = mTouchParentViewOriginMeasureHeight.toFloat()
            mDownY -= mOriginTranslate
            mOriginTranslate = 0f
        }
    }

    @JvmOverloads
    open fun recover(target: Int, runnable: Runnable, time: Int = 300) {
        if (mTranslation.get(this) < 0 || mTranslation.get(this) > measuredHeight) {
            // 一些不正常的情况直接返回
            runnable.run()
            return
        }

        currentAnimator = ObjectAnimator.ofFloat(this, mTranslation, target.toFloat()).apply {
            duration = time.toLong()
            interpolator = DecelerateInterpolator(1.0f)
            addListener(object : CancelDetectionAnimationListener() {
                override fun onAnimationEnd(@NonNull animation: Animator) {
                    notifyNestScrollingStateCallback(ScrollState.SCROLL_STATE_SETTLING)
                    if (!canceled) {
                        currentAnimator = null
                    }
                    runnable.run()
                }
            })
            start()
        }
    }

    private fun interceptHorizontalTouch(event: MotionEvent, deltaX: Float, deltaY: Float) {
        if (event.action == MotionEvent.ACTION_UP || event.action == MotionEvent.ACTION_CANCEL) {
            notifyNestScrollChildHorizontalCallback(event, deltaX, deltaY)
            return
        }
        if (Math.abs(deltaX) > mTouchSlop * 8 && Math.abs(deltaX) > Math.abs(deltaY) && Math.abs(deltaX) > 0) {
            notifyNestScrollChildHorizontalCallback(event, deltaX, deltaY)
        }
    }

    private open class CancelDetectionAnimationListener : AnimatorListenerAdapter() {

        protected var canceled: Boolean = false

        override fun onAnimationCancel(animation: Animator) {
            canceled = true
        }

    }

    /**
     * 即将下掉
     * @param event
     */
    @Deprecated("")
    private fun onActionMove(event: MotionEvent) {
        val distance = countDragDistanceFromMotionEvent(event)
        mChildView!!.translationY = distance
    }

    /**
     * 即将下掉
     * @param event
     */
    @Deprecated("")
    fun onActionRelease(event: MotionEvent) {
        val distance = countDragDistanceFromMotionEvent(event)
        if (mTransYAnim != null && mTransYAnim!!.isRunning) {
            mTransYAnim!!.cancel()
        }

        mTransYAnim = ObjectAnimator.ofFloat(
            mChildView, View.TRANSLATION_Y,
            mChildView!!.translationY, 0.0f
        ).apply {
            duration = 200L
            interpolator = PathInterpolatorCompat.create(0.4f, 0.0f, 0.2f, 1.0f)
            start()
        }
    }

    fun registerNestScrollChildCallback(childScrollChange: INestChildScrollChange) {
        if (!mNestChildScrollChangeCallbacks!!.contains(childScrollChange)) {
            mNestChildScrollChangeCallbacks!!.add(childScrollChange)
        }
    }

    fun registerWrapperDispatchEvent(touchEvent: INestChildDispatchTouchEvent) {
        mNestChildDispatchTouchEvent = touchEvent
    }

    fun registerOnInterceptTouchListener(interceptor: INestInterceptTouchListener) {
        mNestInterceptTouchListener = interceptor
    }

    fun removeNestScrollChildCallback(childScrollChange: INestChildScrollChange) {
        if (mNestChildScrollChangeCallbacks!!.contains(childScrollChange)) {
            mNestChildScrollChangeCallbacks!!.remove(childScrollChange)
        }
    }

    fun clearNestScrollChildCallback() {
        mNestChildScrollChangeCallbacks!!.clear()
    }

    private fun notifyNestScrollChildChangeCallback(detlaY: Float, velocityY: Float) {
        for (change in mNestChildScrollChangeCallbacks!!) {
            change.onNestChildScrollChange(detlaY, velocityY)
        }
    }

    private fun notifyNestScrollChildReleaseCallback(velocityY: Int) {
        for (change in mNestChildScrollChangeCallbacks!!) {
            change.onNestChildScrollRelease(getChildAt(0).translationY, velocityY)
        }
    }

    private fun notifyNestScrollChildHorizontalCallback(event: MotionEvent, deltaX: Float, deltaY: Float) {
        for (change in mNestChildScrollChangeCallbacks!!) {
            change.onNestChildHorizationScroll(event, deltaX, deltaY)
        }
    }

    private fun notifyOnFingerUp(velocityY: Float) {
        for (change in mNestChildScrollChangeCallbacks!!) {
            change.onFingerUp(velocityY)
        }
    }

    private fun notifyNestScrollingStateCallback(@ScrollState state: Int) {
        for (change in mNestChildScrollChangeCallbacks!!) {
            change.onNestScrollingState(state)
        }
    }

    private fun notifyNestShowStateCallback(@ShowState state: Int) {
        this.showState = state
        for (change in mNestChildScrollChangeCallbacks!!) {
            change.onNestShowState(state)
        }
    }

    /**
     * 处理 bound fling 的核心，下一个 feature
     * @param deltaX
     * @param deltaY
     * @param scrollX
     * @param scrollY
     * @param scrollRangeX
     * @param scrollRangeY
     * @param maxOverScrollX
     * @param maxOverScrollY
     * @param isTouchEvent
     * @return
     */
    override fun overScrollBy(
        deltaX: Int,
        deltaY: Int,
        scrollX: Int,
        scrollY: Int,
        scrollRangeX: Int,
        scrollRangeY: Int,
        maxOverScrollX: Int,
        maxOverScrollY: Int,
        isTouchEvent: Boolean
    ): Boolean {
        return super.overScrollBy(
            deltaX,
            deltaY,
            scrollX,
            scrollY,
            scrollRangeX,
            scrollRangeY,
            maxOverScrollX,
            maxOverScrollY,
            isTouchEvent
        )
    }

    private fun countDragDistanceFromMotionEvent(@NonNull event: MotionEvent): Float {
        return event.rawY
    }

    override fun onStartNestedScroll(child: View, target: View, nestedScrollAxes: Int): Boolean {
        return true
    }

    override fun onNestedScrollAccepted(child: View, target: View, axes: Int) {
        super.onNestedScrollAccepted(child, target, axes)
    }

    override fun onNestedPreScroll(target: View, dx: Int, dy: Int, consumed: IntArray) {
        super.onNestedPreScroll(target, dx, dy, consumed)
    }

    override fun onNestedScroll(target: View, dxConsumed: Int, dyConsumed: Int, dxUnconsumed: Int, dyUnconsumed: Int) {
        super.onNestedScroll(target, dxConsumed, dyConsumed, dxUnconsumed, dyUnconsumed)
    }

    override fun onNestedPreFling(target: View, velocityX: Float, velocityY: Float): Boolean {
        return super.onNestedPreFling(target, velocityX, velocityY)
    }

    override fun onNestedFling(target: View, velocityX: Float, velocityY: Float, consumed: Boolean): Boolean {
        return super.onNestedFling(target, velocityX, velocityY, consumed)
    }


    override fun onStopNestedScroll(child: View) {
        super.onStopNestedScroll(child)
    }

    override fun getNestedScrollAxes(): Int {
        return super.getNestedScrollAxes()
    }

    @JvmOverloads
    fun expand(runnable: Runnable? = null, duration: Int = 250, isNeedNotify: Boolean = true) {
        recover(0, Runnable {
            if (isNeedNotify) {
                notifyNestShowStateCallback(ShowState.EXTEND)
            }
            runnable?.run()
        }, duration)
    }

    @JvmOverloads
    fun peek(offset: Int, runnable: Runnable? = null, duration: Int = 250, isNeedNotify: Boolean = true) {
        recover(offset, Runnable {
            if (isNeedNotify) {
                notifyNestShowStateCallback(ShowState.PEEK)
            }
            runnable?.run()
        }, duration)
    }

    @JvmOverloads
    fun hidden(runnable: Runnable? = null, duration: Int = 250, isNeedNotify: Boolean = true) {
        recover(measuredHeight, Runnable {
            if (isNeedNotify) {
                notifyNestShowStateCallback(ShowState.HIDE)
            }
            runnable?.run()
        }, duration)
    }


    /**
     * bottomSheet 方向
     * @param direction
     */
    fun setSheetDirection(@SheetDirection direction: Int) {
        mSheetDirection = direction
    }

    fun setInterceptTouchArea(rectF: RectF) {
        mInterceptTouchArea = rectF
    }

    /**
     * 下阻尼
     * @param mDampingDown
     */
    fun setDampingDown(mDampingDown: Float) {
        this.mDampingDown = mDampingDown
    }

    /**
     * 上阻尼
     * @param mDampingUp
     */
    fun setDampingUp(mDampingUp: Float) {
        this.mDampingUp = mDampingUp
    }

    /**
     * 是否开启拦截
     * @param b
     */
    fun setParentDispatchTouchEvent(b: Boolean) {
        isActionSheetDispatchTouchEvent = b
    }

    /**
     * 锁定顶部
     * @param lockTop
     * @param lockTopY
     */
    fun setLockTop(lockTop: Boolean, lockTopY: Int) {
        isLockTop = lockTop
        mLockTopTranslateY = lockTopY
    }

    /**
     * 锁定底部
     * @param lockBottom
     * @param lockBottomY
     */
    fun setLockBottom(lockBottom: Boolean, lockBottomY: Int) {
        isLockBottom = lockBottom
        mLockBottomTranslateY = lockBottomY
    }

    fun setTouchParentViewOriginMeasureHeight(originHieght: Int) {
        mTouchParentViewOriginMeasureHeight = originHieght
    }

    companion object {
        private val TAG = "NestedTouchScrolling"
    }
}
