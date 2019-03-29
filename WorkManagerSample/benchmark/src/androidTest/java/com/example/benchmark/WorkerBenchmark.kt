package com.example.benchmark

import android.content.Context
import android.graphics.BitmapFactory
import android.util.Log
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.benchmark.BenchmarkRule
import androidx.benchmark.measureRepeated
import androidx.test.InstrumentationRegistry
import androidx.test.filters.SmallTest
import androidx.test.runner.AndroidJUnit4
import androidx.work.*
import androidx.work.impl.utils.SynchronousExecutor
import androidx.work.testing.WorkManagerTestInitHelper
import com.example.background.Constants
import com.example.background.workers.BaseFilterWorker
import com.example.background.workers.BaseFilterWorker.Companion.inputStreamFor
import com.example.background.workers.BlurEffectFilterWorker
import com.example.background.workers.GrayScaleFilterWorker
import com.example.background.workers.WaterColorFilterWorker
import kotlinx.coroutines.runBlocking
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
@SmallTest
class WorkerBenchmark {

    companion object {
        private const val JETPACK = "${BaseFilterWorker.ASSET_PREFIX}images/jetpack.png"
    }

    private val context = InstrumentationRegistry.getTargetContext()

    @get:Rule
    val benchmarkRule = BenchmarkRule()

    @get:Rule
    var instantTaskExecutorRule = InstantTaskExecutorRule()

    @Test
    fun testBlurEffectFilterWorker() {
        val worker = createWorker { appContext: Context, _: String,
                                    workerParameters: WorkerParameters ->
            BlurEffectFilterWorker(appContext, workerParameters)
        }

        benchmarkRule.measureRepeated {
            val inputStream = inputStreamFor(context, JETPACK)
            worker.applyFilter(BitmapFactory.decodeStream(inputStream))
            inputStream?.close()
        }
    }

    @Test
    fun testGrayScaleFilterWorker() {
        val worker = createWorker { appContext: Context, _: String,
                                    workerParameters: WorkerParameters ->
            GrayScaleFilterWorker(appContext, workerParameters)
        }

        benchmarkRule.measureRepeated {
            val inputStream = inputStreamFor(context, JETPACK)
            worker.applyFilter(BitmapFactory.decodeStream(inputStream))
            inputStream?.close()
        }
    }

    @Test
    fun testWaterColorEffectFilterWorker() {
        val worker = createWorker { appContext: Context, _: String,
                                    workerParameters: WorkerParameters ->
            WaterColorFilterWorker(appContext, workerParameters)
        }

        benchmarkRule.measureRepeated {
            val inputStream = inputStreamFor(context, JETPACK)
            worker.applyFilter(BitmapFactory.decodeStream(inputStream))
            inputStream?.close()
        }
    }

    /**
     * Helper method to instantiate a worker and get a reference to it.
     *
     * You must use a WorkerFactory set in a custom Configuration to create an instance of
     * WorkerParams necessary for constructing a Worker. Worker instantiation through WorkManager is
     * triggered through enqueueing a WorkRequest that depends on the required Worker.
     *
     * @return reference to ListenableWorker instantiated from the provided factory block
     */
    private fun <T : ListenableWorker> createWorker(
            createWorkerBlock: (appContext: Context, workerClassName: String,
                                workerParameters: WorkerParameters) -> T
    ): T {
        lateinit var worker: T
        val workerFactory = object : WorkerFactory() {
            override fun createWorker(appContext: Context, workerClassName: String,
                                      workerParameters: WorkerParameters): ListenableWorker? {
                worker = createWorkerBlock(appContext, workerClassName, workerParameters)
                return worker
            }
        }

        val configuration = Configuration.Builder()
                .setExecutor(SynchronousExecutor())
                .setWorkerFactory(workerFactory)
                .setMinimumLoggingLevel(Log.DEBUG)
                .build()

        WorkManagerTestInitHelper.initializeTestWorkManager(context, configuration)
        val workManager = WorkManager.getInstance()

        // Enqueue a WorkRequest in a blocking way to guarantee WorkManager has instantiated the worker.
        val workRequest = OneTimeWorkRequestBuilder<WaterColorFilterWorker>()
                .setInputData(workDataOf(Constants.KEY_IMAGE_URI to JETPACK))
                .build()
        runBlocking {
            workManager.enqueue(workRequest).result.await()
        }

        return worker
    }
}