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
import android.renderscript.Element
import android.renderscript.RenderScript
import android.renderscript.ScriptIntrinsicBlur
import androidx.work.WorkerParameters

class BlurEffectFilterWorker(context: Context, parameters: WorkerParameters) :
    BaseFilterWorker(context, parameters) {

    override fun applyFilter(input: Bitmap): Bitmap {
        var rsContext: RenderScript? = null
        return try {
            // Create a RenderScript context.
            rsContext = RenderScript.create(applicationContext, RenderScript.ContextType.DEBUG)

            // Creates a RenderScript allocation for the blurred result.
            val inAlloc = Allocation.createFromBitmap(rsContext, input)
            val outAlloc = Allocation.createTyped(rsContext, inAlloc.type)

            // Use the ScriptIntrinsicBlur intrinsic.
            val theIntrinsic = ScriptIntrinsicBlur.create(rsContext, Element.U8_4(rsContext))
            theIntrinsic.setRadius(25f)
            theIntrinsic.setInput(inAlloc)
            theIntrinsic.forEach(outAlloc)

            Bitmap.createBitmap(input.width, input.height, input.config).apply {
                // Copy to the output input from the allocation.
                outAlloc.copyTo(this)
            }
        } finally {
            rsContext?.finish()
        }
    }
}
