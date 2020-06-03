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

package com.example.background.workers

import android.content.Context
import android.graphics.Bitmap
import android.renderscript.Allocation
import android.renderscript.RenderScript
import androidx.work.WorkerParameters
import com.example.background.ScriptC_waterColorEffect

class WaterColorFilterWorker(context: Context, parameters: WorkerParameters) :
    BaseFilterWorker(context, parameters) {

    override fun applyFilter(input: Bitmap): Bitmap {
        var rsContext: RenderScript? = null
        try {
            val output = Bitmap.createBitmap(input.width, input.height, input.config)
            rsContext = RenderScript.create(applicationContext, RenderScript.ContextType.DEBUG)
            val inAlloc = Allocation.createFromBitmap(rsContext, input)
            val outAlloc = Allocation.createTyped(rsContext, inAlloc.type)
            // The Renderscript function that generates the water color effect is defined in
            // `src/main/rs/waterColorEffect.rs`. The main idea, is to select a window of the image
            // and then find the most dominant pixel value. Then we set the r, g, b, channels of the
            // pixels to the one with the dominant pixel value.
            val oilFilterEffect = ScriptC_waterColorEffect(rsContext)
            oilFilterEffect._script = oilFilterEffect
            oilFilterEffect._width = input.width.toLong()
            oilFilterEffect._height = input.height.toLong()
            oilFilterEffect._in = inAlloc
            oilFilterEffect._out = outAlloc
            oilFilterEffect.invoke_filter()
            outAlloc.copyTo(output)
            return output
        } finally {
            rsContext?.finish()
        }
    }
}
