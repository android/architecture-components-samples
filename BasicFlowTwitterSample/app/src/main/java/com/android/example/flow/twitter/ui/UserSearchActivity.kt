package com.android.example.flow.twitter.ui

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelLazy
import androidx.recyclerview.widget.LinearLayoutManager
import com.android.example.flow.twitter.R
import com.android.example.flow.twitter.data.models.User
import com.android.example.flow.twitter.data.preferences.PreferencesManager
import com.android.example.flow.twitter.ui.adapters.RvCachedQueries
import com.android.example.flow.twitter.ui.adapters.RvUsersAdapter
import com.android.example.flow.twitter.viewModels.UserSearchViewModel
import com.android.example.flow.twitter.viewModels.ViewModelFactory
import kotlinx.android.synthetic.main.activity_search.*

class UserSearchActivity : AppCompatActivity() {

    private val _viewModel: UserSearchViewModel by ViewModelLazy(
        UserSearchViewModel::class,
        { viewModelStore },
        { ViewModelFactory(PreferencesManager(this)) })

    private lateinit var _rvUsersAdapter: RvUsersAdapter

    private lateinit var _rvQueriesAdapter: RvCachedQueries

    private val submitCachedQuery = { query: String ->
        svUser.setQuery(query, false)
        submitQuery(query)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_search)

        // to open the searchView default and make it clicked
        svUser.findViewById<View>(R.id.search_button).performClick()
        subscribeToQueryTextChange()
        setUpRecyclerViews()
        observeUsersLiveData()
        observeTextChangeLiveData()
    }

    private fun setUpRecyclerViews() {
        // users list recyclerView
        _rvUsersAdapter = RvUsersAdapter(this)
        rvUsers.layoutManager = LinearLayoutManager(this)
        rvUsers.adapter = _rvUsersAdapter

        // cached queries recyclerView
        _rvQueriesAdapter = RvCachedQueries(submitCachedQuery)
        rvSearchOverlay.layoutManager = LinearLayoutManager(this)
        rvSearchOverlay.adapter = _rvQueriesAdapter
    }

    /**
     * Observe the list of twitter users
     */
    private fun observeUsersLiveData() {
        _viewModel.twitteeLiveData.observe(this, Observer {
            it.exception?.let { exception ->
                handleException(exception)
            } ?: kotlin.run {
                handleData(it.data!!)
            }
            progress.visibility = View.GONE
        })
    }

    /**
     * @param exception If the API throws exception it would be handled
     */
    private fun handleException(exception: Exception) {
        Toast.makeText(this, exception.message, Toast.LENGTH_LONG).show()
    }

    /**
     * @param data if there is no exception thrown by the API it handles the data
     */
    private fun handleData(data: List<User>) {
        _rvUsersAdapter.updateUserList(data)
    }

    /**
     * This shows the previous search history
     */
    private fun observeTextChangeLiveData() {
        _viewModel.textChangeLiveData.observe(this, Observer {
            it?.let {
                if (it.isNotEmpty()) {
                    showSearchOverlay(it)
                } else {
                    hideSearchOverlay()
                }
            } ?: kotlin.run {
                hideSearchOverlay()
            }
        })
    }

    private fun hideSearchOverlay() {
        rvSearchOverlay.visibility = View.GONE
        vOverlay.visibility = View.GONE
    }

    private fun showSearchOverlay(it: List<String>) {
        rvSearchOverlay.visibility = View.VISIBLE
        vOverlay.visibility = View.VISIBLE
        _rvQueriesAdapter.updateList(it)
    }

    /**
     * Observe the text submit query of searchView
     */
    private fun subscribeToQueryTextChange() {
        svUser.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                submitQuery(query!!)
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                _viewModel.setTextChange(newText.toString())
                return true
            }
        })
    }

    private fun submitQuery(query: String) {
        _viewModel.getUsers(query) // the api is called
        progress.visibility = View.VISIBLE
        hideSearchOverlay()
    }
}
