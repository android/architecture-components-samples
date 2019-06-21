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

package com.example.background


import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Log
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.Observer
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.SdkSuppress
import androidx.test.filters.SmallTest
import androidx.test.platform.app.InstrumentationRegistry
import androidx.work.Configuration
import androidx.work.WorkManager
import androidx.work.testing.SynchronousExecutor
import androidx.work.testing.WorkManagerTestInitHelper
import com.example.background.Constants.KEY_IMAGE_URI
import com.example.background.Constants.TAG_OUTPUT
import com.example.background.workers.BaseFilterWorker
import com.example.background.workers.BaseFilterWorker.Companion.inputStreamFor
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

@RunWith(AndroidJUnit4::class)
@SmallTest
class ImageOperationsTest {

    companion object {
        // Maximum wait time for a test.
        private const val TEST_TIMEOUT = 5L
        // Making the input image to the ImageOperationsBuilder look like a URI.
        // However the underlying image is loaded using AssetManager. For more information
        // look at BaseFilterWorker#inputStreamFor(...).
        private const val JETPACK = "${BaseFilterWorker.ASSET_PREFIX}images/jetpack.png"
        private const val JETPACK_GRAYSCALED =
                "${BaseFilterWorker.ASSET_PREFIX}test_outputs/grayscale.png"
        private val IMAGE = Uri.parse(JETPACK)
        private val IMAGE_GRAYSCALE = Uri.parse(JETPACK_GRAYSCALED) // grayscale
        private val DEFAULT_IMAGE_URI = Uri.EMPTY.toString()
    }

    private lateinit var mContext: Context
    private lateinit var mTargetContext: Context
    private lateinit var mLifeCycleOwner: LifecycleOwner
    private lateinit var mConfiguration: Configuration
    private var mWorkManager: WorkManager? = null

    @get:Rule
    var instantTaskExecutorRule = InstantTaskExecutorRule()

    @Before
    fun setUp() {
        mContext = InstrumentationRegistry.getInstrumentation().context
        mTargetContext = InstrumentationRegistry.getInstrumentation().targetContext
        mLifeCycleOwner = TestLifeCycleOwner()
        mConfiguration = Configuration.Builder()
                .setExecutor(SynchronousExecutor())
                .setMinimumLoggingLevel(Log.DEBUG)
                .build()
        // Initialize WorkManager using the WorkManagerTestInitHelper.
        WorkManagerTestInitHelper.initializeTestWorkManager(mTargetContext, mConfiguration)
        mWorkManager = WorkManager.getInstance(mTargetContext)
    }

    @Test
    fun testImageOperations() {
        val imageOperations = ImageOperations.Builder(mTargetContext, IMAGE)
                .setApplyGrayScale(true)
                .build()

        imageOperations.continuation
                .enqueue()
                .result
                .get()

        val latch = CountDownLatch(1)
        val outputs: MutableList<Uri> = mutableListOf()

        imageOperations.continuation.workInfosLiveData.observe(mLifeCycleOwner, Observer { workInfos ->
            val statuses = workInfos ?: return@Observer
            val finished = statuses.all { it.state.isFinished }
            if (finished) {
                val outputUris = statuses.map {
                    val output = it.outputData.getString(KEY_IMAGE_URI) ?: DEFAULT_IMAGE_URI
                    Uri.parse(output)
                }.filter {
                    it != Uri.EMPTY
                }
                outputs.addAll(outputUris)
                latch.countDown()
            }
        })

        assertTrue(latch.await(TEST_TIMEOUT, TimeUnit.SECONDS))
        assertEquals(outputs.size, 1)
        assertTrue(sameBitmaps(outputs[0], IMAGE_GRAYSCALE))
    }

    @Test
    @SdkSuppress(maxSdkVersion = 22)
    fun testImageOperationsChain() {
        val imageOperations = ImageOperations.Builder(mTargetContext, IMAGE)
                .setApplyWaterColor(true)
                .setApplyGrayScale(true)
                .setApplyBlur(true)
                .setApplySave(true)
                .build()

        imageOperations.continuation
                .enqueue()
                .result
                .get()

        val latch = CountDownLatch(2)
        val outputs: MutableList<Uri> = mutableListOf()

        imageOperations.continuation.workInfosLiveData.observe(mLifeCycleOwner, Observer { workInfos ->
            val statuses = workInfos ?: return@Observer
            val finished = statuses.all { it.state.isFinished }
            if (finished) {
                val outputUris = statuses.map {
                    val output = it.outputData.getString(KEY_IMAGE_URI) ?: DEFAULT_IMAGE_URI
                    Uri.parse(output)
                }.filter {
                    it != Uri.EMPTY
                }
                outputs.addAll(outputUris)
                latch.countDown()
            }
        })

        var outputUri: Uri? = null
        mWorkManager?.getWorkInfosByTagLiveData(TAG_OUTPUT)?.observe(mLifeCycleOwner, Observer { workInfos ->
            val statuses = workInfos ?: return@Observer
            val finished = statuses.all { it.state.isFinished }
            if (finished) {
                outputUri =
                        statuses.firstOrNull()
                                ?.outputData?.getString(KEY_IMAGE_URI)
                                ?.let { Uri.parse(it) }
                latch.countDown()
            }
        })

        assertTrue(latch.await(TEST_TIMEOUT, TimeUnit.SECONDS))
        assertEquals(outputs.size, 4)
        assertNotNull(outputUri)
    }

    private fun sameBitmaps(outputUri: Uri, compareWith: Uri): Boolean {
        val outputBitmap: Bitmap = BitmapFactory.decodeStream(
                inputStreamFor(mContext, outputUri.toString()))
        val compareBitmap: Bitmap = BitmapFactory.decodeStream(
                inputStreamFor(mContext, compareWith.toString()))
        return outputBitmap.sameAs(compareBitmap)
    }

}
