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
import android.renderscript.Allocation;
import android.renderscript.RenderScript;
import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;
import com.example.background.ScriptC_grayscale;

/**
 * Applies a gray scale filter.
 */
public class GrayScaleFilterWorker extends BaseFilterWorker {

    /**
     * Creates an instance of the {@link Worker}.
     *
     * @param appContext   the application {@link Context}
     * @param workerParams the set of {@link WorkerParameters}
     */
    public GrayScaleFilterWorker(@NonNull Context appContext, @NonNull WorkerParameters workerParams) {
        super(appContext, workerParams);
    }

    @Override
    Bitmap applyFilter(@NonNull Bitmap bitmap) {
        Context applicationContext = getApplicationContext();
        RenderScript rsContext = null;
        try {
            Bitmap output = Bitmap
                    .createBitmap(bitmap.getWidth(), bitmap.getHeight(), bitmap.getConfig());
            rsContext = RenderScript.create(applicationContext, RenderScript.ContextType.DEBUG);
            Allocation inAlloc = Allocation.createFromBitmap(rsContext, bitmap);
            Allocation outAlloc = Allocation.createTyped(rsContext, inAlloc.getType());
            // The Renderscript function that computes gray scale pixel values is defined in
            // `src/main/rs/grayscale.rs`. We compute a new pixel value for every pixel which is
            // out = (r + g + b) / 3 where r, g, b are the red, green and blue channels in the
            // input image.
            ScriptC_grayscale grayscale = new ScriptC_grayscale(rsContext);
            grayscale.set_script(grayscale);
            grayscale.set_width(bitmap.getWidth());
            grayscale.set_height(bitmap.getHeight());
            grayscale.set_in(inAlloc);
            grayscale.set_out(outAlloc);
            grayscale.invoke_filter();
            outAlloc.copyTo(output);
            return output;
        } finally {
            if (rsContext != null) {
                rsContext.finish();
            }
        }
    }
}
