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

package com.example.background;

import static com.example.background.Constants.IMAGE_MANIPULATION_WORK_NAME;
import static com.example.background.Constants.KEY_IMAGE_URI;
import static com.example.background.Constants.TAG_OUTPUT;

import android.net.Uri;
import androidx.annotation.NonNull;
import androidx.work.Data;
import androidx.work.ExistingWorkPolicy;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkContinuation;
import androidx.work.WorkManager;
import com.example.background.workers.BlurEffectFilterWorker;
import com.example.background.workers.CleanupWorker;
import com.example.background.workers.GrayScaleFilterWorker;
import com.example.background.workers.SaveImageToGalleryWorker;
import com.example.background.workers.UploadWorker;
import com.example.background.workers.WaterColorFilterWorker;

/**
 * Builds and holds WorkContinuation based on supplied filters.
 */
class ImageOperations {

    private final WorkContinuation mContinuation;

    private ImageOperations(@NonNull WorkContinuation continuation) {
        mContinuation = continuation;
    }

    WorkContinuation getContinuation() {
        return mContinuation;
    }

    static class Builder {

        private Uri mImageUri;
        private boolean mApplyWaterColor;
        private boolean mApplyGrayScale;
        private boolean mApplyBlur;
        private boolean mApplySave;
        private boolean mApplyUpload;

        Builder(@NonNull Uri imageUri) {
            mImageUri = imageUri;
        }

        Builder setApplyWaterColor(boolean applyWaterColor) {
            mApplyWaterColor = applyWaterColor;
            return this;
        }

        Builder setApplyGrayScale(boolean applyGrayScale) {
            mApplyGrayScale = applyGrayScale;
            return this;
        }

        Builder setApplyBlur(boolean applyBlur) {
            mApplyBlur = applyBlur;
            return this;
        }

        Builder setApplySave(boolean applySave) {
            mApplySave = applySave;
            return this;
        }

        Builder setApplyUpload(boolean applyUpload) {
            mApplyUpload = applyUpload;
            return this;
        }

        /**
         * Creates the {@link WorkContinuation} depending on the list of selected filters.
         *
         * @return the instance of {@link WorkContinuation}.
         */
        ImageOperations build() {
            boolean hasInputData = false;
            WorkContinuation continuation = WorkManager.getInstance()
                    .beginUniqueWork(IMAGE_MANIPULATION_WORK_NAME,
                            ExistingWorkPolicy.REPLACE,
                            OneTimeWorkRequest.from(CleanupWorker.class));

            if (mApplyWaterColor) {
                OneTimeWorkRequest waterColor =
                        new OneTimeWorkRequest.Builder(WaterColorFilterWorker.class)
                            .setInputData(createInputData())
                            .build();
                continuation = continuation.then(waterColor);
                hasInputData = true;
            }

            if (mApplyGrayScale) {
                OneTimeWorkRequest.Builder grayScaleBuilder =
                        new OneTimeWorkRequest.Builder(GrayScaleFilterWorker.class);
                if (!hasInputData) {
                    grayScaleBuilder.setInputData(createInputData());
                    hasInputData = true;
                }
                OneTimeWorkRequest grayScale = grayScaleBuilder.build();
                continuation = continuation.then(grayScale);
            }

            if (mApplyBlur) {
                OneTimeWorkRequest.Builder blurBuilder =
                        new OneTimeWorkRequest.Builder(BlurEffectFilterWorker.class);
                if (!hasInputData) {
                    blurBuilder.setInputData(createInputData());
                    hasInputData = true;
                }
                OneTimeWorkRequest blur = blurBuilder.build();
                continuation = continuation.then(blur);
            }

            if (mApplySave) {
                OneTimeWorkRequest save =
                        new OneTimeWorkRequest.Builder(SaveImageToGalleryWorker.class)
                            .setInputData(createInputData())
                            .addTag(TAG_OUTPUT)
                            .build();
                continuation = continuation.then(save);
            }

            if (mApplyUpload) {
                OneTimeWorkRequest upload =
                        new OneTimeWorkRequest.Builder(UploadWorker.class)
                            .setInputData(createInputData())
                            .addTag(TAG_OUTPUT)
                            .build();
                continuation = continuation.then(upload);
            }
            return new ImageOperations(continuation);
        }

        private Data createInputData() {
            return new Data.Builder()
                    .putString(KEY_IMAGE_URI, mImageUri.toString()).build();
        }
    }
}
