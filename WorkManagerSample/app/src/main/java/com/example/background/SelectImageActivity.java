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

package com.example.background;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.VisibleForTesting;
import com.google.android.material.snackbar.Snackbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.appcompat.app.AppCompatActivity;
import android.text.Html;
import android.text.Spanned;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

/**
 * Helps select an image for the {@link FilterActivity} and handles permission requests.
 *
 * There are two sources for the images: {@link MediaStore} and {@link StockImages}.
 */
public class SelectImageActivity extends AppCompatActivity {

    private static final String TAG = "SelectImageActivity";
    private static final String KEY_PERMISSIONS_REQUEST_COUNT = "KEY_PERMISSIONS_REQUEST_COUNT";

    private static final int MAX_NUMBER_REQUEST_PERMISSIONS = 2;
    private static final int REQUEST_CODE_IMAGE = 100;
    private static final int REQUEST_CODE_PERMISSIONS = 101;

    // A list of permissions the application needs.
    @VisibleForTesting
    public static final List<String> sPermissions = new ArrayList<String>() {{
        add(Manifest.permission.INTERNET);
        add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
    }};

    static {
        if (Build.VERSION.SDK_INT >= 16) {
            sPermissions.add(Manifest.permission.READ_EXTERNAL_STORAGE);
        }
    }

    private int mPermissionRequestCount;
    private boolean mHasPermissions;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select);
        // Show stock image credits.
        TextView credits = findViewById(R.id.credits);
        credits.setText(fromHtml(getString(R.string.credits)));
        // Enable link following.
        credits.setMovementMethod(LinkMovementMethod.getInstance());

        // We keep track of the number of times we requested for permissions.
        // If the user did not want to grant permissions twice - show a Snackbar and don't
        // ask for permissions again for the rest of the session.
        if (savedInstanceState != null) {
            mPermissionRequestCount =
                    savedInstanceState.getInt(KEY_PERMISSIONS_REQUEST_COUNT, 0);
        }

        requestPermissionsIfNecessary();

        findViewById(R.id.selectImage).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent chooseIntent = new Intent(
                        Intent.ACTION_PICK,
                        MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(chooseIntent, REQUEST_CODE_IMAGE);
            }
        });

        findViewById(R.id.selectStockImage).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(FilterActivity.newIntent(
                        SelectImageActivity.this, StockImages.randomStockImage()));
            }
        });
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(KEY_PERMISSIONS_REQUEST_COUNT, mPermissionRequestCount);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == Activity.RESULT_OK) {
            switch (requestCode) {
                case REQUEST_CODE_IMAGE:
                    handleImageRequestResult(data);
                    break;
                default:
                    Log.d(TAG, "Unknown request code.");
            }
        } else {
            Log.e(TAG, String.format("Unexpected Result code %s", resultCode));
        }
    }

    @Override
    public void onRequestPermissionsResult(
            int requestCode,
            @NonNull String[] permissions,
            @NonNull int[] grantResults) {

        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        // Check if permissions were granted after a permissions request flow.
        if (requestCode == REQUEST_CODE_PERMISSIONS) {
            requestPermissionsIfNecessary(); // no-op if permissions are granted already.
        }
    }

    private void requestPermissionsIfNecessary() {
        // Check to see if we have all the permissions we need.
        // Otherwise request permissions up to MAX_NUMBER_REQUESTED_PERMISSIONS.
        mHasPermissions = checkAllPermissions();
        if (!mHasPermissions) {
            if (mPermissionRequestCount < MAX_NUMBER_REQUEST_PERMISSIONS) {
                mPermissionRequestCount += 1;
                ActivityCompat.requestPermissions(
                        this,
                        sPermissions.toArray(new String[0]),
                        REQUEST_CODE_PERMISSIONS);
            } else {
                Snackbar.make(
                        findViewById(R.id.coordinatorLayout),
                        R.string.set_permissions_in_settings,
                        Snackbar.LENGTH_INDEFINITE).show();

                findViewById(R.id.selectImage).setEnabled(false);
            }
        }
    }

    private void handleImageRequestResult(Intent data) {
        // Get the imageUri the user picked, from the Intent.ACTION_PICK result.
        Uri imageUri = null;
        // Use clip data if SDK_INT >= 16
        if (Build.VERSION.SDK_INT >= 16 && data.getClipData() != null) {
            imageUri = data.getClipData().getItemAt(0).getUri();
        } else if (data.getData() != null) {
            // fallback to getData() on the intent.
            imageUri = data.getData();
        }

        if (imageUri == null) {
            Log.e(TAG, "Invalid input image Uri.");
            return;
        }
        startActivity(FilterActivity.newIntent(this, imageUri));
    }

    private boolean checkAllPermissions() {
        boolean hasPermissions = true;
        for (String permission : sPermissions) {
            hasPermissions &=
                    ContextCompat.checkSelfPermission(
                            this, permission) == PackageManager.PERMISSION_GRANTED;
        }
        return hasPermissions;
    }

    private static Spanned fromHtml(String input) {
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.M) {
            return Html.fromHtml(input, Html.FROM_HTML_MODE_COMPACT);
        } else {
            // method deprecated at API 24.
            return Html.fromHtml(input);
        }
    }
}
