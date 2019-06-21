package com.example.background.workers

import android.content.Context
import android.graphics.Bitmap
import android.renderscript.Allocation
import android.renderscript.Element
import android.renderscript.RenderScript
import android.renderscript.ScriptIntrinsicBlur
import androidx.work.WorkerParameters

class BlurEffectFilterWorker(context: Context, parameters: WorkerParameters)
    : BaseFilterWorker(context, parameters) {

    override fun applyFilter(input: Bitmap): Bitmap {
        var rsContext: RenderScript? = null
        try {
            val output = Bitmap.createBitmap(input.width, input.height, input.config)
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

            // Copy to the output input from the allocation.
            outAlloc.copyTo(output)
            return output
        } finally {
            rsContext?.finish()
        }
    }
}
