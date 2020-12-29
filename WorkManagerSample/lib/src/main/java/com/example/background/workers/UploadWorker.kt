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
import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.work.Data
import androidx.work.Worker
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import com.example.background.Constants
import com.example.background.imgur.ImgurApi

/**
 * Uploads an image to Imgur using the [ImgurApi].
 */
class UploadWorker(appContext: Context, workerParams: WorkerParameters) :
    Worker(appContext, workerParams) {

    override fun doWork(): Result {
        var imageUriInput: String? = null
        return try {
            val args = inputData
            imageUriInput = args.getString(Constants.KEY_IMAGE_URI)
            val imageUri = Uri.parse(imageUriInput)
            val imgurApi = ImgurApi.instance.value
            // Upload the image to Imgur.
            val response = imgurApi.uploadImage(imageUri).execute()
            // Check to see if the upload succeeded.
            if (!response.isSuccessful) {
                val errorBody = response.errorBody()
                val error = errorBody?.string()
                toastAndLog("Request failed $imageUriInput ($error)")
                Result.failure()
            } else {
                val imageResponse = response.body()
                var outputData = workDataOf()
                if (imageResponse != null) {
                    val imgurLink = imageResponse.data!!.link
                    // Set the result of the worker by calling setOutputData().
                    outputData = Data.Builder()
                        .putString(Constants.KEY_IMAGE_URI, imgurLink)
                        .build()
                }
                Result.success(outputData)
            }
        } catch (e: Exception) {
            toastAndLog("Failed to upload image with URI $imageUriInput")
            Result.failure()
        }
    }

    private fun toastAndLog(message: String) {
        Toast.makeText(applicationContext, message, Toast.LENGTH_SHORT).show()
        Log.e(TAG, message)
    }

    companion object {
        private const val TAG = "UploadWorker"
    }
}
