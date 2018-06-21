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

import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.util.Log;

import com.example.background.Constants;

import java.io.File;

import androidx.work.Worker;

/**
 * Cleans up temporary files from the output folder.
 */
public class CleanupWorker extends Worker {
    private static final String TAG = "CleanupWorker";

    @Override
    @NonNull
    public Result doWork() {
        try {
            File outputDirectory =
                    new File(getApplicationContext().getFilesDir(), Constants.OUTPUT_PATH);
            if (outputDirectory.exists()) {
                File[] entries = outputDirectory.listFiles();
                if (entries != null && entries.length > 0) {
                    for (File entry : entries) {
                        String name = entry.getName();
                        if (!TextUtils.isEmpty(name) && name.endsWith(".png")) {
                            boolean deleted = entry.delete();
                            Log.i(TAG, String.format("Deleted %s - %s", name, deleted));
                        }
                    }
                }
            }
            return Result.SUCCESS;
        } catch (Exception exception) {
            Log.e(TAG, "Error cleaning up", exception);
            return Result.FAILURE;
        }
    }
}
