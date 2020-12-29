/*
 * Copyright (C) 2018 The Android Open Source Project
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

import android.view.View
import android.view.ViewGroup
import androidx.core.view.children
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.fragment.app.Fragment
import androidx.fragment.app.testing.FragmentScenario
import androidx.test.espresso.IdlingResource
import java.util.UUID

/**
 * An espresso idling resource implementation that reports idle status for all data binding
 * layouts. Data Binding uses a mechanism to post messages which Espresso doesn't track yet.
 *
 * Since this application runs UI tests at the fragment layer, this relies on implementations
 * calling [monitorFragment] with a [FragmentScenario], thereby monitoring all bindings in that
 * fragment and any child views.
 */
class DataBindingIdlingResource : IdlingResource {
    // list of registered callbacks
    private val idlingCallbacks = mutableListOf<IdlingResource.ResourceCallback>()
    // give it a unique id to workaround an espresso bug where you cannot register/unregister
    // an idling resource w/ the same name.
    private val id = UUID.randomUUID().toString()
    // holds whether isIdle is called and the result was false. We track this to avoid calling
    // onTransitionToIdle callbacks if Espresso never thought we were idle in the first place.
    private var wasNotIdle = false

    private lateinit var scenario: FragmentScenario<out Fragment>

    override fun getName() = "DataBinding $id"

    /**
     * Sets the fragment from a [FragmentScenario] to be used from [DataBindingIdlingResource].
     */
    fun monitorFragment(fragmentScenario: FragmentScenario<out Fragment>) {
        scenario = fragmentScenario
    }

    override fun isIdleNow(): Boolean {
        val idle = !getBindings().any { it.hasPendingBindings() }
        @Suppress("LiftReturnOrAssignment")
        if (idle) {
            if (wasNotIdle) {
                // notify observers to avoid espresso race detector
                idlingCallbacks.forEach { it.onTransitionToIdle() }
            }
            wasNotIdle = false
        } else {
            wasNotIdle = true
            // check next frame
            scenario.onFragment { fragment ->
                fragment.view?.postDelayed({
                    if (fragment.view != null) {
                        isIdleNow
                    }
                }, 16)
            }
        }
        return idle
    }

    override fun registerIdleTransitionCallback(callback: IdlingResource.ResourceCallback) {
        idlingCallbacks.add(callback)
    }

    /**
     * Find all binding classes in all currently available fragments.
     */
    private fun getBindings(): List<ViewDataBinding> {
        lateinit var bindings: List<ViewDataBinding>
        scenario.onFragment {  fragment ->
            bindings = fragment.requireView().flattenHierarchy().mapNotNull { view ->
                DataBindingUtil.getBinding<ViewDataBinding>(view)
            }
        }
        return bindings
    }

    private fun View.flattenHierarchy(): List<View> = if (this is ViewGroup) {
        listOf(this) + children.map { it.flattenHierarchy() }.flatten()
    } else {
        listOf(this)
    }
}