package com.example.benchmark

import android.content.Context
import android.graphics.BitmapFactory
import android.util.Log
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.benchmark.BenchmarkRule
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
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
@SmallTest
class WorkerBenchmark {

    companion object {
        private const val JETPACK = "${BaseFilterWorker.ASSET_PREFIX}images/jetpack.png"
    }

    private lateinit var mContext: Context
    private lateinit var mTargetContext: Context

    @get:Rule
    val benchmarkRule = BenchmarkRule()

    @get:Rule
    var instantTaskExecutorRule = InstantTaskExecutorRule()

    @Before
    fun setUp() {
        mContext = InstrumentationRegistry.getContext()
        mTargetContext = InstrumentationRegistry.getTargetContext()
    }

    @Test
    fun testBlurEffectFilterWorker() {
        val worker = createWorker { appContext: Context, _: String,
                                    workerParameters: WorkerParameters ->
            BlurEffectFilterWorker(appContext, workerParameters)
        }

        benchmarkRule.measure {
            val inputStream = inputStreamFor(mContext, JETPACK)
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

        benchmarkRule.measure {
            val inputStream = inputStreamFor(mContext, JETPACK)
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

        benchmarkRule.measure {
            val inputStream = inputStreamFor(mContext, JETPACK)
            worker.applyFilter(BitmapFactory.decodeStream(inputStream))
            inputStream?.close()
        }
    }

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

        WorkManagerTestInitHelper.initializeTestWorkManager(mTargetContext, configuration)
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