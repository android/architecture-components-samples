package com.example.android.navigationadvancedsample.sampletwo

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import com.example.android.navigationadvancedsample.R
import com.example.android.navigationadvancedsample.setupWithNavController
import com.google.android.material.bottomnavigation.BottomNavigationView

class MainFragment : Fragment(R.layout.fragment_main){

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (savedInstanceState == null) {
            setupBottomNavigationBar()
        } // Else, need to wait for onRestoreInstanceState
    }


    override fun onViewStateRestored(savedInstanceState: Bundle?) {
        super.onViewStateRestored(savedInstanceState)
        setupBottomNavigationBar()
    }

    /**
     * Called on first creation and when restoring state.
     */
    @SuppressLint("RestrictedApi")
    private fun setupBottomNavigationBar() {
        val bottomNavigationView = view?.findViewById<BottomNavigationView>(R.id.bottom_nav)

        val navGraphIds = listOf(R.navigation.home, R.navigation.list, R.navigation.form)

        lifecycleScope.launchWhenResumed {
            // Setup the bottom navigation view with a list of navigation graphs
            val controller = bottomNavigationView?.setupWithNavController(
                    navGraphIds = navGraphIds,
                    fragmentManager = childFragmentManager,
                    containerId = R.id.nav_host_container,
                    intent = activity?.intent?:return@launchWhenResumed
            )

            // Whenever the selected controller changes, setup the action bar.
            controller?.observe(viewLifecycleOwner, Observer { navController ->
                (activity as SampleTwoMainActivity).currentNavController = navController
            })
        }
    }
}

