/*
 * Copyright 2021 The Android Open Source Project
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
import androidx.work.*
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
class ImageOperations(
    context: Context,
    imageUri: Uri,
    waterColor: Boolean = false,
    grayScale: Boolean = false,
    blur: Boolean = false,
    save: Boolean = false
) {

    private val imageInputData = workDataOf(Constants.KEY_IMAGE_URI to imageUri.toString())
    val continuation: WorkContinuation

    init {
        continuation = WorkManager.getInstance(context)
            .beginUniqueWork(
                Constants.IMAGE_MANIPULATION_WORK_NAME,
                ExistingWorkPolicy.REPLACE,
                OneTimeWorkRequest.from(CleanupWorker::class.java)
            ).thenMaybe<WaterColorFilterWorker>(waterColor)
            .thenMaybe<GrayScaleFilterWorker>(grayScale)
            .thenMaybe<BlurEffectFilterWorker>(blur)
            .then(
                if (save) {
                    workRequest<SaveImageToGalleryWorker>(tag = Constants.TAG_OUTPUT)
                } else /* upload */ {
                    workRequest<UploadWorker>(tag = Constants.TAG_OUTPUT)
                }
            )
    }

    /**
     * Applies a [ListenableWorker] to a [WorkContinuation] in case [apply] is `true`.
     */
    private inline fun <reified T : ListenableWorker> WorkContinuation.thenMaybe(
        apply: Boolean
    ): WorkContinuation {
        return if (apply) {
            then(workRequest<T>())
        } else {
            this
        }
    }

    /**
     * Creates a [OneTimeWorkRequest] with the given inputData and a [tag] if set.
     */
    private inline fun <reified T : ListenableWorker> workRequest(
        inputData: Data = imageInputData,
        tag: String? = null
    ) =
        OneTimeWorkRequestBuilder<T>().apply {
            setInputData(inputData)
            setExpedited(OutOfQuotaPolicy.RUN_AS_NON_EXPEDITED_WORK_REQUEST)
            if (!tag.isNullOrEmpty()) {
                addTag(tag)
            }
        }.build()
}
