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

import android.annotation.SuppressLint
import android.content.Context
import android.net.Uri
import androidx.work.Data
import androidx.work.ExistingWorkPolicy
import androidx.work.ListenableWorker
import androidx.work.OneTimeWorkRequest
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkContinuation
import androidx.work.WorkManager
import androidx.work.workDataOf
import com.example.background.workers.CleanupWorker
import com.example.background.workers.SaveImageToGalleryWorker
import com.example.background.workers.UploadWorker
import com.example.background.workers.filters.BlurEffectFilterWorker
import com.example.background.workers.filters.GrayScaleFilterWorker
import com.example.background.workers.filters.WaterColorFilterWorker

/**
 * Builds and holds WorkContinuation based on supplied filters.
 */
@SuppressLint("EnqueueWork")
internal class ImageOperations(
    context: Context,
    private val imageUri: Uri,
    waterColor: Boolean = false,
    grayScale: Boolean = false,
    blur: Boolean = false,
    save: Boolean = false,
    upload: Boolean = false
) {

    private lateinit var inputData: Data
    val continuation: WorkContinuation

    init {
        var tmpContinuation = WorkManager.getInstance(context)
            .beginUniqueWork(
                Constants.IMAGE_MANIPULATION_WORK_NAME,
                ExistingWorkPolicy.REPLACE,
                OneTimeWorkRequest.from(CleanupWorker::class.java)
            )

        if (waterColor) {
            tmpContinuation = tmpContinuation.then(workRequest<WaterColorFilterWorker>())
        }
        if (grayScale) {
            tmpContinuation = tmpContinuation.then(workRequest<GrayScaleFilterWorker>())
        }
        if (blur) {
            tmpContinuation = tmpContinuation.then(workRequest<BlurEffectFilterWorker>())
        }
        if (save) {
            tmpContinuation = tmpContinuation.then(workRequest<SaveImageToGalleryWorker>(true))
        }
        if (upload) {
            tmpContinuation = tmpContinuation.then(workRequest<UploadWorker>(true))
        }

        continuation = tmpContinuation
    }

    private inline fun <reified T : ListenableWorker> workRequest(shouldOutput: Boolean = false) =
        OneTimeWorkRequestBuilder<T>().apply {
            if (!::inputData.isInitialized) {
                inputData = workDataOf(Constants.KEY_IMAGE_URI to imageUri.toString())
                setInputData(inputData)
            }
            if (shouldOutput) {
                addTag(Constants.TAG_OUTPUT)
            }
        }.build()
}
