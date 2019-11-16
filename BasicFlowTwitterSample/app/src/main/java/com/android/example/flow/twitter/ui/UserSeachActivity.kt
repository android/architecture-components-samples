package com.android.example.flow.twitter.ui

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import androidx.appcompat.widget.SearchView
import com.android.example.flow.twitter.R
import kotlinx.android.synthetic.main.activity_search.*

class UserSeachActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_search)

        // to open the searchView default and make it clicked
        svUser.findViewById<View>(R.id.search_button).performClick() 
        subscribeToQueryTextChange()
    }

    private fun subscribeToQueryTextChange() {
        svUser.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                return true
            }

        })
    }
}
