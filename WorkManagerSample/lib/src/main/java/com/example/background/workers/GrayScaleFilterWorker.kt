package com.example.background.workers

import android.content.Context
import android.graphics.Bitmap
import android.renderscript.Allocation
import android.renderscript.RenderScript
import androidx.work.WorkerParameters
import com.example.background.ScriptC_grayscale

class GrayScaleFilterWorker(context: Context, parameters: WorkerParameters)
    : BaseFilterWorker(context, parameters) {

    override fun applyFilter(input: Bitmap): Bitmap {
        var rsContext: RenderScript? = null
        try {
            val output = Bitmap
                    .createBitmap(input.width, input.height, input.config)
            rsContext = RenderScript.create(applicationContext, RenderScript.ContextType.DEBUG)
            val inAlloc = Allocation.createFromBitmap(rsContext, input)
            val outAlloc = Allocation.createTyped(rsContext, inAlloc.type)
            // The Renderscript function that computes gray scale pixel values is defined in
            // `src/main/rs/grayscale.rs`. We compute a new pixel value for every pixel which is
            // out = (r + g + b) / 3 where r, g, b are the red, green and blue channels in the
            // input image.
            val grayscale = ScriptC_grayscale(rsContext)
            grayscale._script = grayscale
            grayscale._width = input.width.toLong()
            grayscale._height = input.height.toLong()
            grayscale._in = inAlloc
            grayscale._out = outAlloc
            grayscale.invoke_filter()
            outAlloc.copyTo(output)
            return output
        } finally {
            rsContext?.finish()
        }
    }
}
