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

import android.content.Context
import android.graphics.BitmapFactory
import android.net.Uri
import android.provider.MediaStore
import android.text.TextUtils
import android.util.Log
import androidx.work.Data
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.example.background.Constants
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Saves an output image to the [MediaStore].
 */
class SaveImageToGalleryWorker(appContext: Context, workerParams: WorkerParameters) :
    Worker(appContext, workerParams) {

    override fun doWork(): Result {
        val resolver = applicationContext.contentResolver
        return try {
            val resourceUri = Uri.parse(inputData.getString(Constants.KEY_IMAGE_URI))
            val bitmap = BitmapFactory.decodeStream(resolver.openInputStream(resourceUri))
            val imageUrl = MediaStore.Images.Media.insertImage(
                resolver, bitmap, TITLE, DATE_FORMATTER.format(Date())
            )
            if (imageUrl.isEmpty()) {
                Log.e(TAG, "Writing to MediaStore failed")
                Result.failure()
            }
            // Set the result of the worker by calling setOutputData().
            val output = Data.Builder()
                .putString(Constants.KEY_IMAGE_URI, imageUrl)
                .build()
            Result.success(output)
        } catch (exception: Exception) {
            Log.e(TAG, "Unable to save image to Gallery", exception)
            Result.failure()
        }
    }

    companion object {
        private const val TAG = "SvImageToGalleryWrkr"
        private const val TITLE = "Filtered Image"
        private val DATE_FORMATTER =
            SimpleDateFormat("yyyy.MM.dd 'at' HH:mm:ss z", Locale.getDefault())
    }
}
