package com.example.benchmark

import android.graphics.BitmapFactory
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.benchmark.BenchmarkRule
import androidx.benchmark.measureRepeated
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.SmallTest
import androidx.test.platform.app.InstrumentationRegistry
import androidx.work.testing.TestListenableWorkerBuilder
import com.example.background.workers.BaseFilterWorker
import com.example.background.workers.BaseFilterWorker.Companion.inputStreamFor
import com.example.background.workers.BlurEffectFilterWorker
import com.example.background.workers.GrayScaleFilterWorker
import com.example.background.workers.WaterColorFilterWorker
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

private const val JETPACK = "${BaseFilterWorker.ASSET_PREFIX}images/jetpack.png"

@RunWith(AndroidJUnit4::class)
@SmallTest
class WorkerBenchmark {
    private val context = InstrumentationRegistry.getInstrumentation().targetContext

    @get:Rule
    val benchmarkRule = BenchmarkRule()

    @get:Rule
    var instantTaskExecutorRule = InstantTaskExecutorRule()

    @Test
    fun testBlurEffectFilterWorker() {
        val worker = TestListenableWorkerBuilder<BlurEffectFilterWorker>(context).build()
        benchmarkRule.measureRepeated {
            val inputStream = inputStreamFor(context, JETPACK)
            worker.applyFilter(BitmapFactory.decodeStream(inputStream))
            inputStream?.close()
        }
    }

    @Test
    fun testGrayScaleFilterWorkerNew() {
        val worker = TestListenableWorkerBuilder<GrayScaleFilterWorker>(context).build()
        benchmarkRule.measureRepeated {
            val inputStream = inputStreamFor(context, JETPACK)
            worker.applyFilter(BitmapFactory.decodeStream(inputStream))
            inputStream?.close()
        }
    }

    @Test
    fun testWaterColorEffectFilterWorker() {
        val worker = TestListenableWorkerBuilder<WaterColorFilterWorker>(context).build()
        benchmarkRule.measureRepeated {
            val inputStream = inputStreamFor(context, JETPACK)
            worker.applyFilter(BitmapFactory.decodeStream(inputStream))
            inputStream?.close()
        }
    }
}