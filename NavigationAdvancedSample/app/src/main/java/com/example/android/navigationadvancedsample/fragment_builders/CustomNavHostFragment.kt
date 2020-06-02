package com.example.android.navigationadvancedsample.fragment_builders

import android.os.Bundle
import androidx.navigation.fragment.NavHostFragment

class CustomNavHostFragment : NavHostFragment(){

    override fun onCreate(savedInstanceState: Bundle?) {
        childFragmentManager.fragmentFactory = CustomFragmentFactory()
        super.onCreate(savedInstanceState)
    }
}