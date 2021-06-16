/*
 * Copyright 2021 The Android Open Source Project
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

package com.example.background.workers.filters

import android.content.Context
import android.graphics.Bitmap
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.work.ListenableWorker.Result.Failure
import androidx.work.ListenableWorker.Result.Success
import androidx.work.WorkerParameters
import androidx.work.testing.TestWorkerBuilder
import androidx.work.workDataOf
import com.example.background.Constants
import org.junit.After
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.lang.RuntimeException
import java.util.concurrent.Executor
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors.newSingleThreadExecutor

@RunWith(AndroidJUnit4::class)
class BaseFilterWorkerTest {

    private lateinit var worker: TestBaseFilterWorker
    private val context: Context = ApplicationProvider.getApplicationContext()

    private lateinit var executor: ExecutorService

    @Before
    fun setup() {
        executor = newSingleThreadExecutor()
    }

    @After
    fun tearDown() {
        executor.shutdown()
    }

    @Test
    fun testFiltering_withExistingUri() {
        val worker = TestWorkerBuilder<TestBaseFilterWorker>(
            context,
            executor,
            workDataOf(Constants.KEY_IMAGE_URI to "file:///android_asset/watson.jpg")
        ).build()

        val result = worker.doWork()
        assertTrue(result is Success)
    }

    @Test
    fun testFiltering_invalidUri() {
        worker = TestWorkerBuilder<TestBaseFilterWorker>(
            context,
            executor,
            inputData = workDataOf(Constants.KEY_IMAGE_URI to "file:///android_asset/invalid")
        ).build()

        val result = worker.doWork()
        assertTrue(result is Failure)
    }

    @Test(expected = IllegalArgumentException::class)
    fun testFiltering_missingUri() {
        worker = TestWorkerBuilder<TestBaseFilterWorker>(context, executor).build()
        worker.doWork() // throws Exception
    }
}

/** Implementation of [BaseFilterWorker] for tests. GNDN */
class TestBaseFilterWorker(
    context: Context,
    workerParameters: WorkerParameters
) : BaseFilterWorker(context, workerParameters) {
    // Don't filter, just return the same bitmap
    override fun applyFilter(input: Bitmap) = input
}