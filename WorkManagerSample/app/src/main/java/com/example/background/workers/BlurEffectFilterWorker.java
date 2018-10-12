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

import android.content.Context;
import android.graphics.Bitmap;
import androidx.annotation.NonNull;
import android.renderscript.Allocation;
import android.renderscript.Element;
import android.renderscript.RenderScript;
import android.renderscript.ScriptIntrinsicBlur;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

/**
 * Applies a Gaussian Blur effect using a RenderScript {@link ScriptIntrinsicBlur}.
 */
public class BlurEffectFilterWorker extends BaseFilterWorker {

    /**
     * Creates an instance of the {@link Worker}.
     *
     * @param appContext   the application {@link Context}
     * @param workerParams the set of {@link WorkerParameters}
     */
    public BlurEffectFilterWorker(@NonNull Context appContext, @NonNull WorkerParameters workerParams) {
        super(appContext, workerParams);
    }

    @Override
    Bitmap applyFilter(@NonNull Bitmap bitmap) {
        Context applicationContext = getApplicationContext();
        RenderScript rsContext = null;
        try {
            Bitmap output = Bitmap.createBitmap(
                    bitmap.getWidth(), bitmap.getHeight(), bitmap.getConfig());

            // Create a RenderScript context.
            rsContext = RenderScript.create(applicationContext, RenderScript.ContextType.DEBUG);

            // Creates a RenderScript allocation for the blurred result.
            Allocation inAlloc = Allocation.createFromBitmap(rsContext, bitmap);
            Allocation outAlloc = Allocation.createTyped(rsContext, inAlloc.getType());

            // Use the ScriptIntrinsicBlur intrinsic.
            ScriptIntrinsicBlur theIntrinsic =
                    ScriptIntrinsicBlur.create(rsContext, Element.U8_4(rsContext));
            theIntrinsic.setRadius(25.f);
            theIntrinsic.setInput(inAlloc);
            theIntrinsic.forEach(outAlloc);

            // Copy to the output bitmap from the allocation.
            outAlloc.copyTo(output);
            return output;
        } finally {
            if (rsContext != null) {
                rsContext.finish();
            }
        }
    }
}
