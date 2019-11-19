package com.android.example.flow.twitter.data.preferences

import android.content.Context
import android.content.SharedPreferences
import com.android.example.flow.twitter.Constants


/**
 * Created by Santanu 😁 on 2019-11-19.
 */
class PreferencesManager(context: Context) {
    private val sharedPreferencesManager: SharedPreferences =
        context.getSharedPreferences(Constants.PREFS, Context.MODE_PRIVATE)
    private val editor: SharedPreferences.Editor = sharedPreferencesManager.edit()

    fun storeQuery(query: String) {
        val storedQueries = getAllQueries()
        storedQueries.add(query)
        editor.putStringSet(Constants.QUERIES, storedQueries).apply()
    }

    fun getAllQueries(): MutableSet<String> =
        sharedPreferencesManager.getStringSet(Constants.QUERIES, mutableSetOf())!!
}