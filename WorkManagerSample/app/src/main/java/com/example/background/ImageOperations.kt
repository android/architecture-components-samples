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

package com.example.background

import android.content.Context
import android.net.Uri
import androidx.work.*
import com.example.background.workers.*

/**
 * Builds and holds WorkContinuation based on supplied filters.
 */
internal class ImageOperations private constructor(val continuation: WorkContinuation) {

    internal class Builder(private val mContext: Context, private val mImageUri: Uri) {
        private var mApplyWaterColor: Boolean = false
        private var mApplyGrayScale: Boolean = false
        private var mApplyBlur: Boolean = false
        private var mApplySave: Boolean = false
        private var mApplyUpload: Boolean = false

        fun setApplyWaterColor(applyWaterColor: Boolean): Builder {
            mApplyWaterColor = applyWaterColor
            return this
        }

        fun setApplyGrayScale(applyGrayScale: Boolean): Builder {
            mApplyGrayScale = applyGrayScale
            return this
        }

        fun setApplyBlur(applyBlur: Boolean): Builder {
            mApplyBlur = applyBlur
            return this
        }

        fun setApplySave(applySave: Boolean): Builder {
            mApplySave = applySave
            return this
        }

        fun setApplyUpload(applyUpload: Boolean): Builder {
            mApplyUpload = applyUpload
            return this
        }

        /**
         * Creates the [WorkContinuation] depending on the list of selected filters.
         *
         * @return the instance of [WorkContinuation].
         */
        fun build(): ImageOperations {
            var hasInputData = false
            var continuation = WorkManager.getInstance(mContext)
                    .beginUniqueWork(Constants.IMAGE_MANIPULATION_WORK_NAME,
                            ExistingWorkPolicy.REPLACE,
                            OneTimeWorkRequest.from(CleanupWorker::class.java))

            if (mApplyWaterColor) {

                val waterColor = OneTimeWorkRequestBuilder<WaterColorFilterWorker>()
                        .setInputData(createInputData())
                        .build()
                continuation = continuation.then(waterColor)
                hasInputData = true
            }

            if (mApplyGrayScale) {
                val grayScaleBuilder = OneTimeWorkRequestBuilder<GrayScaleFilterWorker>()
                if (!hasInputData) {
                    grayScaleBuilder.setInputData(createInputData())
                    hasInputData = true
                }
                val grayScale = grayScaleBuilder.build()
                continuation = continuation.then(grayScale)
            }

            if (mApplyBlur) {
                val blurBuilder = OneTimeWorkRequestBuilder<BlurEffectFilterWorker>()
                if (!hasInputData) {
                    blurBuilder.setInputData(createInputData())
                    hasInputData = true
                }
                val blur = blurBuilder.build()
                continuation = continuation.then(blur)
            }

            if (mApplySave) {
                val save = OneTimeWorkRequestBuilder<SaveImageToGalleryWorker>()
                        .setInputData(createInputData())
                        .addTag(Constants.TAG_OUTPUT)
                        .build()
                continuation = continuation.then(save)
            }

            if (mApplyUpload) {
                val upload = OneTimeWorkRequestBuilder<UploadWorker>()
                        .setInputData(createInputData())
                        .addTag(Constants.TAG_OUTPUT)
                        .build()
                continuation = continuation.then(upload)
            }
            return ImageOperations(continuation)
        }

        private fun createInputData(): Data {
            return workDataOf(Constants.KEY_IMAGE_URI to mImageUri.toString())
        }
    }
}
