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
import com.android.example.flow.twitter.ui.adapters.RvUsersAdzapter
import com.android.example.flow.twitter.viewModels.UserSearchViewModel
import com.android.example.flow.twitter.viewModels.ViewModelFactory
import kotlinx.android.synthetic.main.activity_search.*

class UserSearchActivity : AppCompatActivity() {

    private val _viewModel: UserSearchViewModel by ViewModelLazy(
        UserSearchViewModel::class,
        { viewModelStore },
        { ViewModelFactory() })

    private lateinit var _rvUsersAdapter: RvUsersAdzapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_search)

        // to open the searchView default and make it clicked
        svUser.findViewById<View>(R.id.search_button).performClick()
        subscribeToQueryTextChange()
        setUpRecyclerView()
        observeUsersLiveData()
    }

    private fun setUpRecyclerView() {
        _rvUsersAdapter = RvUsersAdzapter(this)
        rvUsers.layoutManager = LinearLayoutManager(this)
        rvUsers.adapter = _rvUsersAdapter
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
     * Observe the text submit query of searchView
     */
    private fun subscribeToQueryTextChange() {
        svUser.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                _viewModel.getUsers(query.toString())
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                return true
            }
        })
    }
}
