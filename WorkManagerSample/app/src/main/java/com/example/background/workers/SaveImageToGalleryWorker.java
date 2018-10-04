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

package com.example.background.workers;

import android.content.ContentResolver;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.provider.MediaStore;
import androidx.annotation.NonNull;
import android.text.TextUtils;
import android.util.Log;

import androidx.work.WorkerParameters;
import com.example.background.Constants;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import androidx.work.Data;
import androidx.work.Worker;

/**
 * Saves an output image to the {@link MediaStore}.
 */
public class SaveImageToGalleryWorker extends Worker {

    /**
     * Creates an instance of the {@link Worker}.
     *
     * @param appContext   the application {@link Context}
     * @param workerParams the set of {@link WorkerParameters}
     */
    public SaveImageToGalleryWorker(@NonNull Context appContext, @NonNull WorkerParameters workerParams) {
        super(appContext, workerParams);
    }

    private static final String TAG = "SvImageToGalleryWrkr";

    private static final String TITLE = "Filtered Image";
    private static final SimpleDateFormat DATE_FORMATTER =
            new SimpleDateFormat("yyyy.MM.dd 'at' HH:mm:ss z", Locale.getDefault());

    @Override
    @NonNull
    public Result doWork() {
        Context applicationContext = getApplicationContext();
        ContentResolver resolver = applicationContext.getContentResolver();
        try {
            String resourceUri = getInputData()
                    .getString(Constants.KEY_IMAGE_URI);
            Bitmap bitmap = BitmapFactory.decodeStream(
                    resolver.openInputStream(Uri.parse(resourceUri)));
            String imageUrl = MediaStore.Images.Media.insertImage(
                    resolver, bitmap, TITLE, DATE_FORMATTER.format(new Date()));
            if (TextUtils.isEmpty(imageUrl)) {
                Log.e(TAG, "Writing to MediaStore failed");
                return Result.FAILURE;
            }
            // Set the result of the worker by calling setOutputData().
            Data output = new Data.Builder()
                    .putString(Constants.KEY_IMAGE_URI, imageUrl)
                    .build();
            setOutputData(output);
            return Result.SUCCESS;
        } catch (Exception exception) {
            Log.e(TAG, "Unable to save image to Gallery", exception);
            return Result.FAILURE;
        }
    }
}
