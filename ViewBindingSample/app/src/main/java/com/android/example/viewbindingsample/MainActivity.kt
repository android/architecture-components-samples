package com.android.example.viewbindingsample

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.android.example.viewbindingsample.R.string
import com.android.example.viewbindingsample.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }

    override fun onStart() {
        super.onStart()
        binding.textViewActivity.text = getString(string.hello_from_vb_activity)
    }
}
