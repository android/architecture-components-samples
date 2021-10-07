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

package com.example.background.workers

import android.content.ContentResolver
import android.content.Context
import android.graphics.BitmapFactory
import android.net.Uri
import android.provider.MediaStore
import android.provider.MediaStore.Images.Media
import android.util.Log
import androidx.work.*
import com.example.background.Constants
import com.example.background.library.R
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Saves an output image to the [MediaStore].
 */
class SaveImageToGalleryWorker(appContext: Context, workerParams: WorkerParameters) :
    CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {

        val resolver = applicationContext.contentResolver
        return try {
            val input = Uri.parse(inputData.getString(Constants.KEY_IMAGE_URI))
            val imageLocation = insertImage(resolver, input)
            if (imageLocation.isNullOrEmpty()) {
                Log.e(TAG, "Writing to MediaStore failed")
                Result.failure()
            }
            // Set the result of the worker by calling setOutputData().
            val output = Data.Builder()
                .putString(Constants.KEY_IMAGE_URI, imageLocation)
                .build()
            Result.success(output)
        } catch (exception: Exception) {
            Log.e(TAG, "Unable to save image to Gallery", exception)
            Result.failure()
        }
    }

    private fun insertImage(resolver: ContentResolver, resourceUri: Uri): String? {
        val bitmap = BitmapFactory.decodeStream(resolver.openInputStream(resourceUri))
        return Media.insertImage(
            resolver, bitmap, DATE_FORMATTER.format(Date()), TITLE
        )

    }

    override suspend fun getForegroundInfo(): ForegroundInfo {
        return ForegroundInfo(
            NOTIFICATION_ID, createNotification(applicationContext, id,
            applicationContext.getString(R.string.notification_title_saving_image)))
    }

    companion object {
        // Use same notification id as BaseFilter worker to update existing notification. For a real
        // world app you might consider using a different id for each notification.
        private const val NOTIFICATION_ID = 1
        private const val TAG = "SvImageToGalleryWrkr"
        private const val TITLE = "Filtered Image"
        private val DATE_FORMATTER =
            SimpleDateFormat("yyyy.MM.dd 'at' HH:mm:ss z", Locale.getDefault())
    }
}
