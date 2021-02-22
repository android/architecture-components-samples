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

package com.example.background.workers

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.work.Constraints
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkInfo
import androidx.work.WorkManager
import androidx.work.testing.TestListenableWorkerBuilder
import androidx.work.testing.WorkManagerTestInitHelper
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File
import java.io.FilenameFilter
import java.util.concurrent.TimeUnit.HOURS

/** Tests for [CleanupWorker]. */
@RunWith(AndroidJUnit4::class)
class CleanupWorkerTest {

    private var context: Context = ApplicationProvider.getApplicationContext()
    private lateinit var worker: CleanupWorker

    @Before
    fun setUp() {
        worker = TestListenableWorkerBuilder<CleanupWorker>(context).build()
    }

    @After
    fun tearDown() {
        val existingFiles = listFilesInTargetDir() ?: return
        for (file in existingFiles) {
            file.delete()
        }
    }

    @Test
    fun testPeriodicCleanup() {
        WorkManagerTestInitHelper.initializeTestWorkManager(context)
        val testDriver = WorkManagerTestInitHelper.getTestDriver(context)!!
        val workManager = WorkManager.getInstance(context)

        val constraints = Constraints.Builder()
            .setRequiresCharging(true)
            .setRequiresBatteryNotLow(true)
            .setRequiresDeviceIdle(true).build()

        val request = PeriodicWorkRequestBuilder<CleanupWorker>(24, HOURS)
            .setConstraints(constraints).build()

        workManager.enqueue(request).result.get()

        with(testDriver) {
            setPeriodDelayMet(request.id)
            setAllConstraintsMet(request.id)
        }

        val workInfo = workManager.getWorkInfoById(request.id).get()

        assertEquals(workInfo.state, WorkInfo.State.RUNNING)
    }

    @Test
    fun testCleanupWorker_pngAreRemoved() {
        // Create an empty test file that should be caught by CleanupWorker and removed
        for (i in 0..9) {
            createFileUnderTest("test$i.png")
        }

        // Run test
        runBlocking {
            worker.doWork()
        }

        // Assert that file has been removed
        val remainingFiles = countPngInTargetDir()
        assertTrue(remainingFiles == 0)
    }

    @Test
    fun testCleanupWorker_nonPngRemain() {
        // Create an empty test file that should NOT be caught by CleanupWorker and removed
        for (i in 0..9) {
            createFileUnderTest("test$i")
            createFileUnderTest("test$i.png")
        }
        //Run test
        runBlocking {
            worker.doWork()
        }

        val existingPng = countPngInTargetDir()
        assertTrue(existingPng == 0)
        val existingFiles = countFilesInTargetDir()
        assertTrue(existingFiles == 10)
    }

    /** Create the file for testing the [CleanupWorker]. */
    private fun createFileUnderTest(fileName: String): File {
        val testFile = File("${worker.targetDirectory.path}/$fileName")
        testFile.createNewFile()
        assertTrue("Created file doesn't exist", testFile.exists())
        return testFile
    }

    private fun listFilesInTargetDir(filter: FilenameFilter? = null) =
        worker.targetDirectory.listFiles(filter)

    private fun countFilesInTargetDir(filter: FilenameFilter? = null) =
        listFilesInTargetDir(filter)?.size

    private fun countPngInTargetDir() =
        listFilesInTargetDir { _, name -> name.endsWith(".png") }?.size
}