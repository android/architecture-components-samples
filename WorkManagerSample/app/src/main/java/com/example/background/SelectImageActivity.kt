/*
 *
 *  * Copyright (C) 2018 The Android Open Source Project
 *  *
 *  * Licensed under the Apache License, Version 2.0 (the "License");
 *  * you may not use this file except in compliance with the License.
 *  * You may obtain a copy of the License at
 *  *
 *  *      http://www.apache.org/licenses/LICENSE-2.0
 *  *
 *  * Unless required by applicable law or agreed to in writing, software
 *  * distributed under the License is distributed on an "AS IS" BASIS,
 *  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  * See the License for the specific language governing permissions and
 *  * limitations under the License.
 *
 */

package com.example.background

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.text.Html
import android.text.Spanned
import android.text.method.LinkMovementMethod
import android.util.Log
import android.view.View
import android.widget.TextView
import androidx.annotation.VisibleForTesting
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.material.snackbar.Snackbar
import java.util.*

/**
 * Helps select an image for the [FilterActivity] and handles permission requests.
 *
 * There are two sources for the images: [MediaStore] and [StockImages].
 */
class SelectImageActivity : AppCompatActivity() {

    private var mPermissionRequestCount: Int = 0
    private var mHasPermissions: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_select)
        // Show stock image credits.
        val credits = findViewById<TextView>(R.id.credits)
        credits.text = fromHtml(getString(R.string.credits))
        // Enable link following.
        credits.movementMethod = LinkMovementMethod.getInstance()

        // We keep track of the number of times we requested for permissions.
        // If the user did not want to grant permissions twice - show a Snackbar and don't
        // ask for permissions again for the rest of the session.
        if (savedInstanceState != null) {
            mPermissionRequestCount = savedInstanceState.getInt(KEY_PERMISSIONS_REQUEST_COUNT, 0)
        }

        requestPermissionsIfNecessary()

        findViewById<View>(R.id.selectImage).setOnClickListener {
            val chooseIntent = Intent(
                    Intent.ACTION_PICK,
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            startActivityForResult(chooseIntent, REQUEST_CODE_IMAGE)
        }

        findViewById<View>(R.id.selectStockImage).setOnClickListener {
            startActivity(FilterActivity.newIntent(
                    this@SelectImageActivity, StockImages.randomStockImage()))
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putInt(KEY_PERMISSIONS_REQUEST_COUNT, mPermissionRequestCount)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (resultCode == Activity.RESULT_OK) {
            when (requestCode) {
                REQUEST_CODE_IMAGE -> handleImageRequestResult(data)
                else -> Log.d(TAG, "Unknown request code.")
            }
        } else {
            Log.e(TAG, String.format("Unexpected Result code %s", resultCode))
        }
    }

    override fun onRequestPermissionsResult(
            requestCode: Int,
            permissions: Array<String>,
            grantResults: IntArray) {

        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        // Check if permissions were granted after a permissions request flow.
        if (requestCode == REQUEST_CODE_PERMISSIONS) {
            requestPermissionsIfNecessary() // no-op if permissions are granted already.
        }
    }

    private fun requestPermissionsIfNecessary() {
        // Check to see if we have all the permissions we need.
        // Otherwise request permissions up to MAX_NUMBER_REQUESTED_PERMISSIONS.
        mHasPermissions = checkAllPermissions()
        if (!mHasPermissions) {
            if (mPermissionRequestCount < MAX_NUMBER_REQUEST_PERMISSIONS) {
                mPermissionRequestCount += 1
                ActivityCompat.requestPermissions(
                        this,
                        sPermissions.toTypedArray(),
                        REQUEST_CODE_PERMISSIONS)
            } else {
                Snackbar.make(
                        findViewById(R.id.coordinatorLayout),
                        R.string.set_permissions_in_settings,
                        Snackbar.LENGTH_INDEFINITE).show()

                findViewById<View>(R.id.selectImage).isEnabled = false
            }
        }
    }

    private fun handleImageRequestResult(data: Intent?) {
        // Get the imageUri the user picked, from the Intent.ACTION_PICK result.
        var imageUri: Uri? = null
        // Use clip data if SDK_INT >= 16
        if (Build.VERSION.SDK_INT >= 16 && data!!.clipData != null) {
            imageUri = data.clipData!!.getItemAt(0).uri
        } else if (data!!.data != null) {
            // fallback to getData() on the intent.
            imageUri = data.data
        }

        if (imageUri == null) {
            Log.e(TAG, "Invalid input image Uri.")
            return
        }
        startActivity(FilterActivity.newIntent(this, imageUri))
    }

    private fun checkAllPermissions(): Boolean {
        var hasPermissions = true
        for (permission in sPermissions) {
            hasPermissions = hasPermissions and (ContextCompat.checkSelfPermission(
                    this, permission) == PackageManager.PERMISSION_GRANTED)
        }
        return hasPermissions
    }

    companion object {

        private const val TAG = "SelectImageActivity"
        private const val KEY_PERMISSIONS_REQUEST_COUNT = "KEY_PERMISSIONS_REQUEST_COUNT"

        private const val MAX_NUMBER_REQUEST_PERMISSIONS = 2
        private const val REQUEST_CODE_IMAGE = 100
        private const val REQUEST_CODE_PERMISSIONS = 101

        // A list of permissions the application needs.
        @VisibleForTesting
        val sPermissions: MutableList<String> = object : ArrayList<String>() {
            init {
                add(Manifest.permission.INTERNET)
                add(Manifest.permission.WRITE_EXTERNAL_STORAGE)
            }
        }

        init {
            if (Build.VERSION.SDK_INT >= 16) {
                sPermissions.add(Manifest.permission.READ_EXTERNAL_STORAGE)
            }
        }

        private fun fromHtml(input: String): Spanned {
            return if (Build.VERSION.SDK_INT > Build.VERSION_CODES.M) {
                Html.fromHtml(input, Html.FROM_HTML_MODE_COMPACT)
            } else {
                // method deprecated at API 24.
                Html.fromHtml(input)
            }
        }
    }
}
