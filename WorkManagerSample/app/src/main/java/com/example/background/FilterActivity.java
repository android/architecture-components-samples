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

import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import androidx.annotation.IdRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.Checkable;
import android.widget.ImageView;
import android.widget.ProgressBar;

import com.bumptech.glide.Glide;

import java.util.List;

import androidx.work.Data;
import androidx.work.WorkStatus;

/**
 * The {@link android.app.Activity} where the user picks filters to be applied on an
 * image.
 */
public class FilterActivity extends AppCompatActivity {

    private FilterViewModel mViewModel;
    private Uri mImageUri;
    private Uri mOutputImageUri;

    /**
     * Creates a new intent which can be used to start {@link FilterActivity}.
     *
     * @param context  the application {@link Context}.
     * @param imageUri the input image {@link Uri}.
     * @return the instance of {@link Intent}.
     */
    static Intent newIntent(@NonNull Context context,  @NonNull Uri imageUri) {
        Intent intent = new Intent(context, FilterActivity.class);
        intent.putExtra(Constants.KEY_IMAGE_URI, imageUri.toString());
        return intent;
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_processing);

        // Don't enable upload to Imgur, unless the developer specifies their own clientId.
        boolean enableUpload = !TextUtils.isEmpty(Constants.IMGUR_CLIENT_ID);
        findViewById(R.id.upload).setEnabled(enableUpload);

        mViewModel = ViewModelProviders.of(this).get(FilterViewModel.class);

        Intent intent = getIntent();
        String imageUriExtra = intent.getStringExtra(Constants.KEY_IMAGE_URI);

        if (!TextUtils.isEmpty(imageUriExtra)) {
            mImageUri = Uri.parse(imageUriExtra);
            ImageView imageView = findViewById(R.id.imageView);
            Glide.with(this).load(mImageUri).into(imageView);
        }

        findViewById(R.id.go).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                boolean applyWaterColor = isChecked(R.id.filter_watercolor);
                boolean applyGrayScale = isChecked(R.id.filter_grayscale);
                boolean applyBlur = isChecked(R.id.filter_blur);
                boolean save = isChecked(R.id.save);
                boolean upload = isChecked(R.id.upload);

                ImageOperations imageOperations = new ImageOperations.Builder(mImageUri)
                        .setApplyWaterColor(applyWaterColor)
                        .setApplyGrayScale(applyGrayScale)
                        .setApplyBlur(applyBlur)
                        .setApplySave(save)
                        .setApplyUpload(upload)
                        .build();

                mViewModel.apply(imageOperations);
            }
        });

        findViewById(R.id.output).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mOutputImageUri != null) {
                    Intent actionView = new Intent(Intent.ACTION_VIEW, mOutputImageUri);
                    if (actionView.resolveActivity(getPackageManager()) != null) {
                        startActivity(actionView);
                    }
                }
            }
        });

        findViewById(R.id.cancel).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mViewModel.cancel();
            }
        });

        // Check to see if we have output.
        mViewModel.getOutputStatus().observe(this, new Observer<List<WorkStatus>>() {
            @Override
            public void onChanged(@Nullable List<WorkStatus> listOfStatuses) {
                if (listOfStatuses == null || listOfStatuses.isEmpty()) {
                    return;
                }

                // We only care about the one output status.
                // Every continuation has only one worker tagged TAG_OUTPUT
                WorkStatus status = listOfStatuses.get(0);
                boolean finished = status.getState().isFinished();
                ProgressBar progressBar = findViewById(R.id.progressBar);
                Button go = findViewById(R.id.go);
                Button cancel = findViewById(R.id.cancel);
                Button output = findViewById(R.id.output);
                if (!finished) {
                    progressBar.setVisibility(View.VISIBLE);
                    cancel.setVisibility(View.VISIBLE);
                    go.setVisibility(View.GONE);
                    output.setVisibility(View.GONE);
                } else {
                    progressBar.setVisibility(View.GONE);
                    cancel.setVisibility(View.GONE);
                    go.setVisibility(View.VISIBLE);

                    Data outputData = status.getOutputData();
                    String outputImageUri =
                            outputData.getString(Constants.KEY_IMAGE_URI);

                    if (!TextUtils.isEmpty(outputImageUri)) {
                        mOutputImageUri = Uri.parse(outputImageUri);
                        output.setVisibility(View.VISIBLE);
                    }
                }
            }
        });
    }

    private boolean isChecked(@IdRes int resourceId) {
        View view = findViewById(resourceId);
        return (view instanceof Checkable) && ((Checkable) view).isChecked();
    }
}
