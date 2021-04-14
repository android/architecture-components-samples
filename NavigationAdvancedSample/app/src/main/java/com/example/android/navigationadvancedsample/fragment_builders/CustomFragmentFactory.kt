package com.example.android.navigationadvancedsample.fragment_builders

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentFactory
import com.example.android.navigationadvancedsample.formscreen.Register
import com.example.android.navigationadvancedsample.formscreen.Registered
import com.example.android.navigationadvancedsample.homescreen.About
import com.example.android.navigationadvancedsample.homescreen.Title
import com.example.android.navigationadvancedsample.listscreen.Leaderboard
import com.example.android.navigationadvancedsample.listscreen.UserProfile

class CustomFragmentFactory :  FragmentFactory(){

    override fun instantiate(classLoader: ClassLoader, className: String): Fragment {
        return when(className){
            Register::class.java.name -> {
                Register()
            }

            Registered::class.java.name -> {
                Registered()
            }

            About::class.java.name -> {
                About()
            }

            Title::class.java.name -> {
                Title()
            }

            UserProfile::class.java.name -> {
                UserProfile()
            }

            Leaderboard::class.java.name -> {
                Leaderboard()
            }

            else -> super.instantiate(classLoader, className)
        }
    }
}