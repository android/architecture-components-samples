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
import com.example.background.ScriptC_waterColorEffect;

/**
 * Applies a water color effect effect on the image.
 */
public class WaterColorFilterWorker extends BaseFilterWorker {

    /**
     * Creates an instance of the {@link Worker}.
     *
     * @param appContext   the application {@link Context}
     * @param workerParams the set of {@link WorkerParameters}
     */
    public WaterColorFilterWorker(@NonNull Context appContext, @NonNull WorkerParameters workerParams) {
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
            // The Renderscript function that generates the water color effect is defined in
            // `src/main/rs/waterColorEffect.rs`. The main idea, is to select a window of the image
            // and then find the most dominant pixel value. Then we set the r, g, b, channels of the
            // pixels to the one with the dominant pixel value.
            ScriptC_waterColorEffect oilFilterEffect = new ScriptC_waterColorEffect(rsContext);
            oilFilterEffect.set_script(oilFilterEffect);
            oilFilterEffect.set_width(bitmap.getWidth());
            oilFilterEffect.set_height(bitmap.getHeight());
            oilFilterEffect.set_in(inAlloc);
            oilFilterEffect.set_out(outAlloc);
            oilFilterEffect.invoke_filter();
            outAlloc.copyTo(output);
            return output;
        } finally {
            if (rsContext != null) {
                rsContext.finish();
            }
        }
    }
}
