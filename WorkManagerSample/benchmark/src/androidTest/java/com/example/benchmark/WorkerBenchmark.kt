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

package com.example.benchmark

import android.graphics.BitmapFactory
import androidx.benchmark.junit4.BenchmarkRule
import androidx.benchmark.junit4.measureRepeated
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
