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
internal class ImageOperations private constructor(val continuation: WorkContinuation) {

    internal class Builder(private val context: Context, private val imageUri: Uri) {
        private var applyWaterColor: Boolean = false
        private var applyGrayScale: Boolean = false
        private var applyBlur: Boolean = false
        private var applySave: Boolean = false
        private var applyUpload: Boolean = false

        fun setApplyWaterColor(applyWaterColor: Boolean): Builder {
            this.applyWaterColor = applyWaterColor
            return this
        }

        fun setApplyGrayScale(applyGrayScale: Boolean): Builder {
            this.applyGrayScale = applyGrayScale
            return this
        }

        fun setApplyBlur(applyBlur: Boolean): Builder {
            this.applyBlur = applyBlur
            return this
        }

        fun setApplySave(applySave: Boolean): Builder {
            this.applySave = applySave
            return this
        }

        fun setApplyUpload(applyUpload: Boolean): Builder {
            this.applyUpload = applyUpload
            return this
        }

        /**
         * Creates the [WorkContinuation] depending on the list of selected filters.
         *
         * @return the instance of [WorkContinuation].
         */
        fun build(): ImageOperations {
            var hasInputData = false
            var continuation = WorkManager.getInstance(context)
                .beginUniqueWork(
                    Constants.IMAGE_MANIPULATION_WORK_NAME,
                    ExistingWorkPolicy.REPLACE,
                    OneTimeWorkRequest.from(CleanupWorker::class.java)
                )

            if (applyWaterColor) {
                val waterColor = OneTimeWorkRequestBuilder<WaterColorFilterWorker>()
                    .setInputData(createInputData())
                    .build()
                continuation = continuation.then(waterColor)
                hasInputData = true
            }

            if (applyGrayScale) {
                val grayScaleBuilder = OneTimeWorkRequestBuilder<GrayScaleFilterWorker>()
                if (!hasInputData) {
                    grayScaleBuilder.setInputData(createInputData())
                    hasInputData = true
                }
                val grayScale = grayScaleBuilder.build()
                continuation = continuation.then(grayScale)
            }

            if (applyBlur) {
                val blurBuilder = OneTimeWorkRequestBuilder<BlurEffectFilterWorker>()
                if (!hasInputData) {
                    blurBuilder.setInputData(createInputData())
                    hasInputData = true
                }
                val blur = blurBuilder.build()
                continuation = continuation.then(blur)
            }

            if (applySave) {
                val save = makeOneTimeWorkRequestBuilder<SaveImageToGalleryWorker>().build()
                continuation = continuation.then(save)
            }

            if (applyUpload) {
                val upload = makeOneTimeWorkRequestBuilder<UploadWorker>().build()
                continuation = continuation.then(upload)
            }
            return ImageOperations(continuation)
        }

        private inline fun <reified T : ListenableWorker> makeOneTimeWorkRequestBuilder() =
            OneTimeWorkRequestBuilder<T>()
                .setInputData(createInputData())
                .addTag(Constants.TAG_OUTPUT)

        private fun createInputData(): Data {
            return workDataOf(Constants.KEY_IMAGE_URI to imageUri.toString())
        }
    }
}
