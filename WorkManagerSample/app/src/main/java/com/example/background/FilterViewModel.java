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

import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;

import java.util.List;

import androidx.work.WorkManager;
import androidx.work.WorkStatus;

import static com.example.background.Constants.IMAGE_MANIPULATION_WORK_NAME;
import static com.example.background.Constants.TAG_OUTPUT;

/**
 * A {@link ViewModel} for {@link FilterActivity}.
 *
 * Keeps track of pending image filter operations.
 */
public class FilterViewModel extends ViewModel {
    private WorkManager mWorkManager;

    public FilterViewModel() {
        mWorkManager = WorkManager.getInstance();
    }

    void apply(ImageOperations imageOperations) {
        imageOperations.getContinuation().enqueue();
    }

    void cancel() {
        mWorkManager.cancelUniqueWork(IMAGE_MANIPULATION_WORK_NAME);
    }

    LiveData<List<WorkStatus>> getOutputStatus() {
        return mWorkManager.getStatusesByTagLiveData(TAG_OUTPUT);
    }
}
