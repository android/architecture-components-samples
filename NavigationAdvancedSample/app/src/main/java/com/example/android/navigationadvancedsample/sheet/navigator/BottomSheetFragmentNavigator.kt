package com.example.android.navigationadvancedsample.sheet.navigator

import android.content.Context
import android.os.Bundle
import android.text.TextUtils
import android.util.AttributeSet
import android.util.Log
import androidx.annotation.CallSuper
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.LifecycleEventObserver
import androidx.navigation.NavDestination
import androidx.navigation.NavOptions
import androidx.navigation.Navigator
import androidx.navigation.NavigatorProvider
import com.example.android.navigationadvancedsample.R
import com.example.android.navigationadvancedsample.sheet.ui.BottomSheetFragment
import java.util.*

/**
 * @author yyf
 * @since 06-19-2019
 */
@Navigator.Name("sheet")
class BottomSheetFragmentNavigator(var context: Context, var  manager: FragmentManager) : Navigator<BottomSheetFragmentNavigator.Destination>() {

    private val TAG = "BottomSheetNavigator"
    private val SHEET_TAG = "androidx-nav-fragment:navigator:sheet:id:"
    private val SHEET_STACK_TAG = "androidx-nav-fragment:navigator:sheet:stack"

    private val observer = LifecycleEventObserver { _, _ -> }

    private var stack: ArrayDeque<Int> = ArrayDeque()

    override fun createDestination(): Destination {
        return Destination(this)
    }

    override fun navigate(
            destination: Destination,
            args: Bundle?,
            navOptions: NavOptions?,
            navigatorExtras: Extras?
    ): NavDestination? {
        if (manager.isStateSaved) {
            Log.i(TAG, "Ignoring navigate() call: FragmentManager has already" + " saved its state")
            return null
        }

        var className = destination.className
        if (className[0] == '.') {
            className = context.packageName + className
        }

        val frag = manager.fragmentFactory.instantiate(context.classLoader, className)
        if (!BottomSheetFragment::class.java.isAssignableFrom(frag.javaClass)) {
            throw IllegalArgumentException(
                "BottomSheet destination " + destination.className
                        + " is not an instance of BottomSheetFragment"
            )
        }
        val bottomSheetFragment = frag as BottomSheetFragment
        bottomSheetFragment.arguments = args
        bottomSheetFragment.lifecycle.addObserver(observer)

        val ft = manager.beginTransaction()
        ft.add(R.id.bottomSheet, bottomSheetFragment, SHEET_TAG + destination.id)
        ft.commit()
        stack.addLast(destination.id)
        return destination
    }

    override fun popBackStack(): Boolean {
        if (manager.isStateSaved) {
            Log.i(TAG, "Ignoring popBackStack() call: FragmentManager has already" + " saved its state")
            return false
        }
        val tag = stack.pollLast()
        val existingFragment = manager
            .findFragmentByTag(SHEET_TAG + tag)
        if (existingFragment != null) {
            existingFragment.lifecycle.removeObserver(observer)
            (existingFragment as BottomSheetFragment).getSheetFunction().executeHidden(
                Runnable {
                    manager.beginTransaction().remove(existingFragment).commitNowAllowingStateLoss()
                }, 300, false
            )
        }
        return true
    }

    override fun onSaveState(): Bundle? {
        val b = Bundle()
        val backStack = IntArray(stack.size)
        var index = 0
        for (id in stack) {
            backStack[index++] = id!!
        }
        b.putIntArray(SHEET_STACK_TAG, backStack)
        return b
    }

    override fun onRestoreState(savedState: Bundle) {
        savedState.let {
            val backStack = savedState.getIntArray(SHEET_STACK_TAG)
            if (backStack != null) {
                stack.clear()
                for (destId in backStack) {
                    stack.add(destId)
                }
            }
        }
    }

    @NavDestination.ClassType(BottomSheetFragment::class)
    class Destination : NavDestination {

        var className: String = ""
            get() {
                if (TextUtils.isEmpty(field)) {
                    throw IllegalStateException("BottomSheetFragment class was not set")
                }
                return field
            }

        constructor(navigatorProvider: NavigatorProvider) : super(
            navigatorProvider.getNavigator<BottomSheetFragmentNavigator>(
                BottomSheetFragmentNavigator::class.java
            ))

        constructor(fragmentNavigator: Navigator<out Destination>) : super(fragmentNavigator)


        @CallSuper
        override fun onInflate(context: Context, attrs: AttributeSet) {
            super.onInflate(context, attrs)
            val a = context.resources.obtainAttributes(
                attrs,
                R.styleable.BottomSheetFragmentNavigator
            )
            className = a.getString(R.styleable.BottomSheetFragmentNavigator_android_name) ?: ""
            a.recycle()
        }
    }

}