package com.recker.photoswipeview

import android.animation.ValueAnimator
import android.content.Context
import android.content.res.Resources
import android.util.AttributeSet
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.widget.ImageView
import androidx.annotation.LayoutRes
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.animation.doOnEnd
import coil.api.load
import java.util.*
import kotlin.math.abs


/**
 * Created by Santanu 😁 on 2020-02-08.
 */
class PhotoSwipeView(context: Context, attrs: AttributeSet?) :
    ConstraintLayout(context, attrs), View.OnTouchListener {

    /**
     * The photos pass to this widget , ( which are presented one after the other )
     */
    private val _mPhotos: MutableList<Photo> = mutableListOf()

    /**
     * It holds ONLY 2 Views at a time
     */
    private val _photosViewList: MutableList<View> = mutableListOf()

    /**
     * Get the corresponding User with respect of the photo
     * Taken Weak as views are constantly removed and added : better for garbage collection
     */
    private val _viewToPhotoMap: WeakHashMap<View, Photo> = WeakHashMap()

    /**
     * The current position within the _photos list which is swiped
     */
    private var _currentPosition = 0

    /**
     * Each photo layout into which each image will be loaded
     * It will have an imageView -- Its a layout resource file
     */
    @LayoutRes
    private var mLayoutPhoto: Int = R.layout.photos_root_layout

    /**
     * To notify the activity/view if the photo is completely swiped ( either right or left )
     * @see swipe directions
     */
    private var callbackLambda: ((Int, Photo) -> Unit)? = null

    /**
     * It holds the number of clicks
     * It holds true if like is clicked and false if dislike is clicked
     */
    private val eventStack = Stack<Boolean>()

    /**
     * This flag shows whether a photo is already animating or not
     * If its animating we wont start animation on the next view
     */
    private var isTransitionInProgress: Boolean = false

    /**
     * The threshold till which each photo needs to be dragged
     * to completely move out of the screen either left side / right side
     */
    private var dragThreshold = 300

    fun setCallbackLambda(lambda: ((Int, Photo) -> Unit)) {
        this.callbackLambda = lambda
    }

    fun likeOrDislikeClicked(isLike: Boolean = true) {
        if (!isTransitionInProgress) {
            if (childCount > 0) {
                val viewToBeAnimated = if (childCount > 1) getChildAt(1) else getChildAt(0)
                valueAnim(
                    0.0f,
                    if (isLike) viewToBeAnimated.width.toFloat() else -viewToBeAnimated.width.toFloat(),
                    viewToBeAnimated,
                    "X",
                    true
                )
            }
        } else {
            if (eventStack.size > 0)
                eventStack.pop()
            eventStack.push(isLike)
        }
    }

    private fun popEvent() {
        if (eventStack.size > 0) {
            likeOrDislikeClicked(eventStack.pop())
        }
    }

    /**
     * This is the animation duration for slide out of the screen
     * and also slide back to the initial position -- When the user does not drag the THRESHOLD length
     */
    private var mAnimationDuration: Int = 500

    init {
        attrs?.let {
            val typedArray = context.obtainStyledAttributes(attrs, R.styleable.PhotoSwipeView)
            for (i in 0 until typedArray.indexCount) {
                when (typedArray.getIndex(i)) {
                    R.styleable.PhotoSwipeView_photoLayout -> mLayoutPhoto =
                        typedArray.getResourceId(typedArray.getIndex(i), 0)
                    R.styleable.PhotoSwipeView_animationDuration -> mAnimationDuration =
                        typedArray.getInteger(typedArray.getIndex(i), 0)
                    R.styleable.PhotoSwipeView_dragThreshold -> dragThreshold =
                        typedArray.getInteger(typedArray.getIndex(i), 0)
                }
            }
            typedArray.recycle()
        }
    }

    /**
     * This adds all the photos to be shown
     * @param photos  Cumulative of all the photos to be shown one after another
     */
    fun setPhotos(photos: List<Photo>) {
        _mPhotos.apply {
            clear()
            addAll(photos)
        }
        notifyPhotosAdded()
        notifyPhotosAdded()
    }

    /**
     * This adds the latest photo to the queue
     */
    private fun notifyPhotosAdded() {
        if (_mPhotos.size > _currentPosition) {
            val view = LayoutInflater
                .from(context)
                .inflate(mLayoutPhoto, this, false)
            val ivPhoto = view.findViewById<View>(R.id.ivPhoto) as ImageView
            val model = _mPhotos[_currentPosition]

            ivPhoto.load(model.url)

            // TODO set the image to the imageView
            val layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT)
            view.layoutParams = layoutParams

            view.setOnTouchListener(this)
            addView(view, 0)

            _photosViewList.add(view)
            _viewToPhotoMap[view] = _mPhotos[_currentPosition]
            _currentPosition++
        }
    }

    private var dX = 0f
    private var dY: Float = 0f

    /**
     * Swipe / touch logic of the photos -- Swiped right or left
     */
    override fun onTouch(view: View, event: MotionEvent?): Boolean {
        when (event?.action) {
            MotionEvent.ACTION_DOWN -> {
                dX = view.x - event.rawX
                dY = view.y - event.rawY
            }
            MotionEvent.ACTION_MOVE -> {
                view.animate()
                    .x(event.rawX + dX)
                    .y(event.rawY + dY)
                    .setDuration(0)
                    .start()

                val percentage = (abs(event.rawX + dX) * 100) / view.width
                val rotate = when {
                    (event.rawX + dX) > 0 -> {
                        (percentage / 100.0f) * 1.0f
                    }
                    (event.rawX + dX) < 0 -> {
                        -((percentage / 100.0f) * 1.0f)
                    }
                    else -> {
                        0.0f
                    }
                }

                view.animate()
                    .rotation(rotate * 25)
                    .setDuration(0)
                    .start()

                if (_photosViewList.size > 1) {
                    _photosViewList[0].animate()
                        .rotation(rotate * 12)
                        .setDuration(0)
                        .start()
                }
            }

            MotionEvent.ACTION_UP -> {
                if (abs((event.rawX + dX)) > dragThreshold) {
                    when {
                        (event.rawX + dX) > 0 -> {
                            valueAnim(event.rawX + dX, width.toFloat(), view, "X")
                        }
                        (event.rawX + dX) < 0 -> {
                            valueAnim(event.rawX + dX, -(width.toFloat()), view, "X")
                        }
                    }
                } else {
                    valueAnim(view.rotation, 0.0f, view, "R")
                    valueAnim(event.rawX + dX, 0.0f, view, "X")
                    valueAnim(event.rawY + dY, 0.0f, view, "Y")
                }
            }
            else -> return false
        }
        return true
    }

    private fun valueAnim(
        initialX: Float,
        finalX: Float,
        view: View,
        type: String,
        isLikeOrDislike: Boolean = false
    ) {
        isTransitionInProgress = true // turn on the boolean flag
        val anim = ValueAnimator.ofFloat(initialX, finalX)
        anim.apply {
            addUpdateListener {
                when (type) {
                    "X" -> view.x = it.animatedValue as Float
                    "R" -> view.rotation = it.animatedValue as Float
                    "Y" -> view.y = it.animatedValue as Float
                }
                if (isLikeOrDislike && view.rotation < 0.4f && view.rotation >= -0.4f) {
                    view.rotation = it.animatedValue as Float / 2
                }
            }
            doOnEnd {
                isTransitionInProgress = false // turn off the boolean flag
                if (abs(finalX) >= width || isLikeOrDislike) {
                    if (finalX < 0)
                        callbackLambda?.invoke(LEFT, _viewToPhotoMap[view]!!)
                    else
                        callbackLambda?.invoke(RIGHT, _viewToPhotoMap[view]!!)
                    removeView(view)
                    _viewToPhotoMap.remove(view)
                    _photosViewList.remove(view)
                    notifyPhotosAdded()
                    popEvent()
                }

                // TODO MOVE IT TO SMOOTH ANIMATION -- FOR THE VIEW BEHIND
                if (_photosViewList.size > 1) {
                    _photosViewList[0].animate()
                        .rotation(0.0f)
                        .setDuration(0)
                        .start()
                }
            }
            duration = if (!isLikeOrDislike) mAnimationDuration.toLong() else 300
            start()
        }
    }

    fun Float.convertDpToPx(): Float {
        val dip = 14f
        val r: Resources = context.resources
        return TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            this,
            r.displayMetrics
        )
    }

    companion object {
        @JvmStatic
        val RIGHT = 1

        @JvmStatic
        val LEFT = 0
    }

    /**
     * Created by Santanu 😁 on 2020-02-08.
     */
    abstract class Photo {

        /**
         * Must be provided by the client
         */
        abstract val url: String?
    }
}