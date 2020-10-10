/*
 * Copyright 2019, The Android Open Source Project
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

package com.example.android.navigationadvancedsample

import android.content.Intent
import android.os.Bundle
import android.util.SparseArray
import androidx.annotation.IdRes
import androidx.annotation.NavigationRes
import androidx.core.util.forEach
import androidx.core.util.set
import androidx.core.view.ViewCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.navigation.*
import androidx.navigation.fragment.FragmentNavigator
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.fragment.findNavController
import com.google.android.material.bottomnavigation.BottomNavigationView

/**
 * Manages the various graphs needed for a [BottomNavigationView].
 *
 * This sample is a workaround until the Navigation Component supports multiple back stacks.
 */
fun BottomNavigationView.setupWithNavController(
        navGraphIds: List<Int>,
        fragmentManager: FragmentManager,
        containerId: Int,
        intent: Intent
): LiveData<NavController> {

    // Map of tags
    val graphIdToTagMap = SparseArray<String>()
    // Result. Mutable live data with the selected controlled
    val selectedNavController = MutableLiveData<NavController>()

    var firstFragmentGraphId = 0

    // First create a NavHostFragment for each NavGraph ID
    navGraphIds.forEachIndexed { index, navGraphId ->
        val fragmentTag = getFragmentTag(index)

        // Find or create the Navigation host fragment
        val navHostFragment = obtainNavHostFragment(
                fragmentManager,
                fragmentTag,
                navGraphId,
                containerId
        )

        // Obtain its id
        val graphId = navHostFragment.navController.graph.id

        if (index == 0) {
            firstFragmentGraphId = graphId
        }

        // Save to the map
        graphIdToTagMap[graphId] = fragmentTag

        // Attach or detach nav host fragment depending on whether it's the selected item.
        if (this.selectedItemId == graphId) {
            // Update livedata with the selected graph
            selectedNavController.value = navHostFragment.navController
            attachNavHostFragment(fragmentManager, navHostFragment, index == 0)
        } else {
            detachNavHostFragment(fragmentManager, navHostFragment)
        }
    }

    // Now connect selecting an item with swapping Fragments
    var selectedItemTag = graphIdToTagMap[this.selectedItemId]
    val firstFragmentTag = graphIdToTagMap[firstFragmentGraphId]
    var isOnFirstFragment = selectedItemTag == firstFragmentTag

    // When a navigation item is selected
    setOnNavigationItemSelectedListener { item ->
        // Don't do anything if the state is state has already been saved.
        if (fragmentManager.isStateSaved) {
            false
        } else {
            val newlySelectedItemTag = graphIdToTagMap[item.itemId]
            if (selectedItemTag != newlySelectedItemTag) {
                // Pop everything above the first fragment (the "fixed start destination")
                fragmentManager.popBackStack(firstFragmentTag,
                        FragmentManager.POP_BACK_STACK_INCLUSIVE)
                val selectedFragment = fragmentManager.findFragmentByTag(newlySelectedItemTag)
                        as NavHostFragment

                // Exclude the first fragment tag because it's always in the back stack.
                if (firstFragmentTag != newlySelectedItemTag) {
                    // Commit a transaction that cleans the back stack and adds the first fragment
                    // to it, creating the fixed started destination.
                    fragmentManager.beginTransaction()
                            .setCustomAnimations(
                                    R.anim.nav_default_enter_anim,
                                    R.anim.nav_default_exit_anim,
                                    R.anim.nav_default_pop_enter_anim,
                                    R.anim.nav_default_pop_exit_anim)
                            .attach(selectedFragment)
                            .setPrimaryNavigationFragment(selectedFragment)
                            .apply {
                                // Detach all other Fragments
                                graphIdToTagMap.forEach { _, fragmentTagIter ->
                                    if (fragmentTagIter != newlySelectedItemTag) {
                                        detach(fragmentManager.findFragmentByTag(firstFragmentTag)!!)
                                    }
                                }
                            }
                            .addToBackStack(firstFragmentTag)
                            .setReorderingAllowed(true)
                            .commit()
                }
                selectedItemTag = newlySelectedItemTag
                isOnFirstFragment = selectedItemTag == firstFragmentTag
                selectedNavController.value = selectedFragment.navController
                true
            } else {
                false
            }
        }
    }

    // Optional: on item reselected, pop back stack to the destination of the graph
    setupItemReselected(graphIdToTagMap, fragmentManager)

    // Handle deep link
    setupDeepLinks(navGraphIds, fragmentManager, containerId, intent)

    // Finally, ensure that we update our BottomNavigationView when the back stack changes
    fragmentManager.addOnBackStackChangedListener {
        if (!isOnFirstFragment && !fragmentManager.isOnBackStack(firstFragmentTag)) {
            this.selectedItemId = firstFragmentGraphId
        }

        // Reset the graph if the currentDestination is not valid (happens when the back
        // stack is popped after using the back button).
        selectedNavController.value?.let { controller ->
            if (controller.currentDestination == null) {
                controller.navigate(controller.graph.id)
            }
        }
    }
    return selectedNavController
}

private fun BottomNavigationView.setupDeepLinks(
        navGraphIds: List<Int>,
        fragmentManager: FragmentManager,
        containerId: Int,
        intent: Intent
) {
    navGraphIds.forEachIndexed { index, navGraphId ->
        val fragmentTag = getFragmentTag(index)

        // Find or create the Navigation host fragment
        val navHostFragment = obtainNavHostFragment(
                fragmentManager,
                fragmentTag,
                navGraphId,
                containerId
        )
        // Handle Intent
        if (navHostFragment.navController.handleDeepLink(intent)
                && selectedItemId != navHostFragment.navController.graph.id) {
            this.selectedItemId = navHostFragment.navController.graph.id
        }
    }
}

private fun BottomNavigationView.setupItemReselected(
        graphIdToTagMap: SparseArray<String>,
        fragmentManager: FragmentManager
) {
    setOnNavigationItemReselectedListener { item ->
        val newlySelectedItemTag = graphIdToTagMap[item.itemId]
        val selectedFragment = fragmentManager.findFragmentByTag(newlySelectedItemTag)
                as NavHostFragment
        val navController = selectedFragment.navController
        // Pop the back stack to the start destination of the current navController graph
        navController.popBackStack(
                navController.graph.startDestination, false
        )
    }
}

private fun detachNavHostFragment(
        fragmentManager: FragmentManager,
        navHostFragment: NavHostFragment
) {
    fragmentManager.beginTransaction()
            .detach(navHostFragment)
            .commitNow()
}

private fun attachNavHostFragment(
        fragmentManager: FragmentManager,
        navHostFragment: NavHostFragment,
        isPrimaryNavFragment: Boolean
) {
    fragmentManager.beginTransaction()
            .attach(navHostFragment)
            .apply {
                if (isPrimaryNavFragment) {
                    setPrimaryNavigationFragment(navHostFragment)
                }
            }
            .commitNow()

}

private fun obtainNavHostFragment(
        fragmentManager: FragmentManager,
        fragmentTag: String,
        navGraphId: Int,
        containerId: Int
): NavHostFragment {
    // If the Nav Host fragment exists, return it
    val existingFragment = fragmentManager.findFragmentByTag(fragmentTag) as NavHostFragment?
    existingFragment?.let { return it }

    // Otherwise, create it and return it.
    val navHostFragment = NavHostFragment.create(navGraphId)
    fragmentManager.beginTransaction()
            .add(containerId, navHostFragment, fragmentTag)
            .commitNow()
    return navHostFragment
}

private fun FragmentManager.isOnBackStack(backStackName: String): Boolean {
    val backStackCount = backStackEntryCount
    for (index in 0 until backStackCount) {
        if (getBackStackEntryAt(index).name == backStackName) {
            return true
        }
    }
    return false
}

private fun getFragmentTag(index: Int) = "bottomNavigation#$index"


fun Fragment.push(@IdRes activityContainerId: Int, @NavigationRes graphId: Int, @IdRes startDestination: Int? = null, arguments: Bundle? = null) {
    val navHost = activity
    val navController = navHost?.findNavController(activityContainerId)
    val graphInflater = navController!!.navInflater
    val navGraph = graphInflater.inflate(graphId)
    val node = if (startDestination != null) {
        navGraph.findNode(startDestination)
    } else {
        navGraph.findNode(navGraph.startDestination)
    }
    navGraph.startDestination = node?.id?:throw IllegalStateException("Please give a destination")
    arguments?.keySet()?.forEach {
        node.addArgument(it, NavArgument.Builder()
                .setDefaultValue(arguments.get(it))
                .setIsNullable(true)
                .setType(NavType.fromArgType(
                        arguments.get(it)!!.javaClass.name,
                        this::class.java.`package`?.name
                ))
                .build())
    }

    navController.graph.addAll(navGraph)
    navController.navigate(node.id)
}

/**
 * If there is a single activity, you can use static @IdRes
 */
fun Fragment.push(@IdRes activityContainerId: Int, fragment: Fragment) {
    val newId = ViewCompat.generateViewId()
    val navController = activity?.findNavController(activityContainerId)
    val currentNavigator = navController!!.getFragmentNavigator()
    navController.graph.addDestination(currentNavigator.newDestination(fragment, newId))
    navController.navigate(newId)
}

fun Fragment.navigate(graphId: Int, startDestination: Int? = null, arguments: Bundle? = null) {
    val navHost = this
    val navController = navHost?.findNavController()
    val graphInflater = navController!!.navInflater
    val navGraph = graphInflater.inflate(graphId)
    val node = if (startDestination != null) {
        navGraph.findNode(startDestination)
    } else {
        navGraph.findNode(navGraph.startDestination)
    }
    navGraph.startDestination = node?.id?:throw IllegalStateException("Please give a destination")
    arguments?.keySet()?.forEach {
        node.addArgument(it, NavArgument.Builder()
                .setDefaultValue(arguments.get(it))
                .setIsNullable(true)
                .setType(NavType.fromArgType(
                        arguments.get(it)!!.javaClass.name,
                        this::class.java.`package`?.name
                ))
                .build())
    }
    navController.graph.addAll(navGraph)
    navController.navigate(navGraph.startDestination)
}


fun Fragment.navigate(fragment: Fragment) {
    val newId = ViewCompat.generateViewId()
    val navController = this.findNavController()
    val currentNavigator = navController.getFragmentNavigator()
    navController.graph.addDestination(currentNavigator.newDestination(fragment, newId))
    navController.navigate(newId)
}


fun Navigator<FragmentNavigator.Destination>.newDestination(
        fragment: Fragment,
        newId: Int
): FragmentNavigator.Destination {
    return createDestination().apply {
        id = newId
        className = fragment::class.java.name
        fragment.arguments?.keySet()?.forEach {
            addArgument(
                    it, NavArgument.Builder()
                    .setIsNullable(true)
                    .setDefaultValue(fragment.requireArguments().get(it))
                    .setType(
                            NavType.fromArgType(
                                    fragment.requireArguments().get(it)!!.javaClass.name,
                                    fragment::class.java.`package`?.name
                            )
                    )
                    .build()
            )
        }
    }
}


fun NavController.getFragmentNavigator(): Navigator<FragmentNavigator.Destination> {
    return navigatorProvider.getNavigator<Navigator<FragmentNavigator.Destination>>(
            currentDestination!!.navigatorName
    )
}
