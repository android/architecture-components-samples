/*
 * Copyright 2018 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.background

import android.Manifest
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
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.VisibleForTesting
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.background.databinding.ActivitySelectBinding
import com.google.android.material.snackbar.Snackbar
import java.util.ArrayList

/**
 * Helps select an image for the [FilterActivity] and handles permission requests.
 *
 * There are two sources for the images: [MediaStore] and [StockImages].
 */
class SelectImageActivity : AppCompatActivity() {

    private var permissionRequestCount = 0
    private var hasPermissions = false

    private lateinit var requestMultiplePermissions: ActivityResultLauncher<Array<String>>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivitySelectBinding.inflate(layoutInflater).apply {
            setContentView(root)
        }

        with(binding) {
            // Show stock image credits.
            credits.text = fromHtml(getString(R.string.credits))
            // Enable link following.
            credits.movementMethod = LinkMovementMethod.getInstance()
        }

        // We keep track of the number of times we requested for permissions.
        // If the user did not want to grant permissions twice - show a Snackbar and don't
        // ask for permissions again for the rest of the session.
        if (savedInstanceState != null) {
            permissionRequestCount = savedInstanceState.getInt(KEY_PERMISSIONS_REQUEST_COUNT, 0)
        }

        requestMultiplePermissions = registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) {
            requestPermissionsIfNecessary()
        }
        requestPermissionsIfNecessary()

        val pickContract = registerForActivityResult(PickContract()) { imageUri ->
            handleImageRequestResult(imageUri)
        }

        binding.selectImage.setOnClickListener {
            pickContract.launch(MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        }

        binding.selectStockImage.setOnClickListener {
            startActivity(
                    FilterActivity.newIntent(
                            this@SelectImageActivity, StockImages.randomStockImage()
                    )
            )
        }

    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putInt(KEY_PERMISSIONS_REQUEST_COUNT, permissionRequestCount)
    }

    private fun requestPermissionsIfNecessary() {
        // Check to see if we have all the permissions we need.
        // Otherwise request permissions up to MAX_NUMBER_REQUESTED_PERMISSIONS.
        hasPermissions = checkAllPermissions()
        if (!hasPermissions) {
            if (permissionRequestCount < MAX_NUMBER_REQUEST_PERMISSIONS) {
                permissionRequestCount += 1
                requestMultiplePermissions.launch(sPermissions.toTypedArray())
            } else {
                Snackbar.make(
                        findViewById(R.id.coordinatorLayout),
                        R.string.set_permissions_in_settings,
                        Snackbar.LENGTH_INDEFINITE
                ).show()
                findViewById<View>(R.id.selectImage).isEnabled = false
            }
        }
    }

    private fun handleImageRequestResult(imageUri: Uri?) {
        imageUri?.let {
            startActivity(FilterActivity.newIntent(this, it))
        } ?: Log.e(TAG, "Invalid input image Uri.")
    }

    private fun checkAllPermissions(): Boolean {
        var hasPermissions = true
        for (permission in sPermissions) {
            hasPermissions = hasPermissions and (ContextCompat.checkSelfPermission(
                    this, permission
            ) == PackageManager.PERMISSION_GRANTED)
        }
        return hasPermissions
    }

    companion object {

        private const val TAG = "SelectImageActivity"
        private const val KEY_PERMISSIONS_REQUEST_COUNT = "KEY_PERMISSIONS_REQUEST_COUNT"

        private const val MAX_NUMBER_REQUEST_PERMISSIONS = 2

        // A list of permissions the application needs.
        @VisibleForTesting
        val sPermissions: MutableList<String> = object : ArrayList<String>() {
            init {
                add(Manifest.permission.INTERNET)
                add(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                add(Manifest.permission.READ_EXTERNAL_STORAGE)
            }
        }

        private fun fromHtml(input: String): Spanned {
            return if (Build.VERSION.SDK_INT > Build.VERSION_CODES.M) {
                Html.fromHtml(input, Html.FROM_HTML_MODE_COMPACT)
            } else {
                // method deprecated at API 24.
                @Suppress("DEPRECATION")
                Html.fromHtml(input)
            }
        }
    }
}
