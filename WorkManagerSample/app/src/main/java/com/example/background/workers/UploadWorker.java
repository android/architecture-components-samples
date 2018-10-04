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

import android.content.Context;
import android.net.Uri;
import androidx.annotation.NonNull;
import android.util.Log;
import android.widget.Toast;

import androidx.work.WorkerParameters;
import com.example.background.Constants;
import com.example.background.imgur.ImgurApi;
import com.example.background.imgur.PostImageResponse;

import java.util.Date;

import androidx.work.Data;
import androidx.work.Worker;
import okhttp3.ResponseBody;
import retrofit2.Response;

/**
 * Uploads an image to Imgur using the {@link ImgurApi}.
 */
public class UploadWorker extends Worker {

    private static final String TAG = "UploadWorker";

    /**
     * Creates an instance of the {@link Worker}.
     *
     * @param appContext   the application {@link Context}
     * @param workerParams the set of {@link WorkerParameters}
     */
    public UploadWorker(@NonNull Context appContext, @NonNull WorkerParameters workerParams) {
        super(appContext, workerParams);
    }

    @Override
    @NonNull
    public Result doWork() {
        String imageUriInput = null;
        try {
            Data args = getInputData();
            imageUriInput = args.getString(Constants.KEY_IMAGE_URI);
            Uri imageUri = Uri.parse(imageUriInput);
            ImgurApi imgurApi = ImgurApi.getInstance();
            // Upload the image to Imgur.
            Response<PostImageResponse> response = imgurApi.uploadImage(imageUri).execute();
            // Check to see if the upload succeeded.
            if (!response.isSuccessful()) {
                ResponseBody errorBody = response.errorBody();
                String error = errorBody != null ? errorBody.string() : null;
                String message = String.format("Request failed %s (%s)", imageUriInput, error);
                Log.e(TAG, message);
                Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
                return Result.FAILURE;
            } else {
                PostImageResponse imageResponse = response.body();
                if (imageResponse != null) {
                    String imgurLink = imageResponse.getData().getLink();
                    // Set the result of the worker by calling setOutputData().
                    setOutputData(new Data.Builder()
                            .putString(Constants.KEY_IMAGE_URI, imgurLink)
                            .build()
                    );
                }
                return Result.SUCCESS;
            }
        } catch (Exception e) {
            String message = String.format("Failed to upload image with URI %s", imageUriInput);
            Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
            Log.e(TAG, message);
            return Result.FAILURE;
        }
    }
}
