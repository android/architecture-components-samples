package com.example.android.navigationadvancedsample.sampletwo

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.navigation.ui.setupActionBarWithNavController
import com.example.android.navigationadvancedsample.R
import kotlinx.coroutines.delay

class SampleTwoMainActivity : AppCompatActivity() {

    var currentNavController: NavController? = null
    set(value) {
        setupActionBarWithNavController(value?:return)
        field = value
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sample_two_main)

        lifecycleScope.launchWhenResumed {
            findNavController(R.id.nav_main_host).addOnDestinationChangedListener { controller, destination, arguments ->
                currentNavController = controller
            }
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        return currentNavController?.navigateUp() ?: false
    }
}