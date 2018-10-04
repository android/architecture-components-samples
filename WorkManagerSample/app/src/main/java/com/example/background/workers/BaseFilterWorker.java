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
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import androidx.annotation.NonNull;
import androidx.annotation.VisibleForTesting;
import androidx.annotation.WorkerThread;
import android.text.TextUtils;
import android.util.Log;

import androidx.work.WorkerParameters;
import com.example.background.Constants;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.UUID;

import androidx.work.Data;
import androidx.work.Worker;

/**
 * The {@link Worker} that all filters extend from. The only method the underlying filters need
 * to implement is the {@link BaseFilterWorker#applyFilter(Bitmap)} method.
 */
public abstract class BaseFilterWorker extends Worker {

    private static final String TAG = "BaseFilterWorker";

    /** A prefix shared by all Android Asset URIs. */
    @VisibleForTesting
    public static final String ASSET_PREFIX = "file:///android_asset/";

    /**
     * Creates an instance of the {@link Worker}.
     *
     * @param appContext   the application {@link Context}
     * @param workerParams the set of {@link WorkerParameters}
     */
    public BaseFilterWorker(@NonNull Context appContext, @NonNull WorkerParameters workerParams) {
        super(appContext, workerParams);
    }

    /**
     * Reads a {@link Bitmap} given an {@code imageUri} via {@code KEY_IMAGE_URI} and
     * delegates to the implementation of {@link BaseFilterWorker#applyFilter(Bitmap)}.
     *
     * @return an instance of {@link androidx.work.Worker.Result}.
     */
    @Override
    @NonNull
    public Result doWork() {
        String resourceUri = getInputData().getString(Constants.KEY_IMAGE_URI);
        try {
            if (TextUtils.isEmpty(resourceUri)) {
                Log.e(TAG, "Invalid input uri");
                throw new IllegalArgumentException("Invalid input uri");
            }
            Context context = getApplicationContext();
            InputStream inputStream = inputStreamFor(context, resourceUri);
            Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
            Bitmap output = applyFilter(bitmap);
            // write bitmap to a file and set the output
            Uri outputUri = writeBitmapToFile(getApplicationContext(), output);
            setOutputData(new Data.Builder().putString(
                    Constants.KEY_IMAGE_URI, outputUri.toString()).build());
            return Result.SUCCESS;
        } catch (FileNotFoundException fileNotFoundException) {
            Log.e(TAG, "Failed to decode input stream", fileNotFoundException);
            throw new RuntimeException("Failed to decode input stream", fileNotFoundException);
        } catch (Throwable throwable) {
            Log.e(TAG, "Error applying filter", throwable);
            return Result.FAILURE;
        }
    }

    /**
     * The implementation of a filter.
     *
     * @param input represents an input {@link Bitmap} image.
     * @return the filtered {@link Bitmap}.
     */
    @WorkerThread
    abstract Bitmap applyFilter(@NonNull Bitmap input);

    /**
     * Creates an input stream which can be used to read the given {@code resourceUri}.
     *
     * @param context     the application {@link Context}.
     * @param resourceUri the {@link String} resourceUri.
     * @return the {@link InputStream} for the resourceUri.
     */
    @VisibleForTesting
    public static InputStream inputStreamFor(
            @NonNull Context context,
            @NonNull String resourceUri) throws IOException {

        // If the resourceUri is an Android asset URI, then use AssetManager to get a handle to
        // the input stream. (Stock Images are Asset URIs).
        if (resourceUri.startsWith(ASSET_PREFIX)) {
            AssetManager assetManager = context.getResources().getAssets();
            return assetManager.open(resourceUri.substring(ASSET_PREFIX.length()));
        } else {
            // Not an Android asset Uri. Use a ContentResolver to get a handle to the input stream.
            ContentResolver resolver = context.getContentResolver();
            return resolver.openInputStream(Uri.parse(resourceUri));
        }
    }

    /**
     * Writes a given {@link Bitmap} to the {@link Context#getFilesDir()} directory.
     *
     * @param applicationContext the application {@link Context}.
     * @param bitmap             the {@link Bitmap} which needs to be written to the files
     *                           directory.
     * @return a {@link Uri} to the output {@link Bitmap}.
     */
    private static Uri writeBitmapToFile(
            @NonNull Context applicationContext,
            @NonNull Bitmap bitmap) throws FileNotFoundException {

        // Bitmaps are being written to a temporary directory. This is so they can serve as inputs
        // for workers downstream, via Worker chaining.
        String name = String.format("filter-output-%s.png", UUID.randomUUID().toString());
        File outputDir = new File(applicationContext.getFilesDir(), Constants.OUTPUT_PATH);
        if (!outputDir.exists()) {
            outputDir.mkdirs(); // should succeed
        }
        File outputFile = new File(outputDir, name);
        FileOutputStream out = null;
        try {
            out = new FileOutputStream(outputFile);
            bitmap.compress(Bitmap.CompressFormat.PNG, 0 /* ignored for PNG */, out);
        } finally {
            if (out != null) {
                try {
                    out.close();
                } catch (IOException ignore) {
                }
            }
        }
        return Uri.fromFile(outputFile);
    }
}
