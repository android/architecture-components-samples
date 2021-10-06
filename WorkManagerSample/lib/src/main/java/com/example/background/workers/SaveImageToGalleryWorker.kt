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

import android.content.ContentResolver
import android.annotation.TargetApi
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.provider.MediaStore.Images.Media
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.work.*
import com.example.background.Constants
import com.example.background.library.R
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Saves an output image to the [MediaStore].
 */
class SaveImageToGalleryWorker(appContext: Context, workerParams: WorkerParameters) :
    CoroutineWorker(appContext, workerParams) {

    private val notificationManager =
        appContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    override suspend fun doWork(): Result {
        val resolver = applicationContext.contentResolver
        return try {
            val input = Uri.parse(inputData.getString(Constants.KEY_IMAGE_URI))
            val imageLocation = insertImage(resolver, input)
            if (imageLocation.isNullOrEmpty()) {
                Log.e(TAG, "Writing to MediaStore failed")
                Result.failure()
            }
            // Set the result of the worker by calling setOutputData().
            val output = Data.Builder()
                .putString(Constants.KEY_IMAGE_URI, imageLocation)
                .build()
            Result.success(output)
        } catch (exception: Exception) {
            Log.e(TAG, "Unable to save image to Gallery", exception)
            Result.failure()
        }
    }

    private fun insertImage(resolver: ContentResolver, resourceUri: Uri): String? {
        val bitmap = BitmapFactory.decodeStream(resolver.openInputStream(resourceUri))
        return Media.insertImage(
            resolver, bitmap, DATE_FORMATTER.format(Date()), TITLE
        )

    }

    override suspend fun getForegroundInfo(): ForegroundInfo {
        // For a real world app you might want to use a different id for each Notification.
        val notificationId = 1
        return ForegroundInfo(notificationId, createNotification())
    }

    private fun createNotification(): Notification {
        val channelId = applicationContext.getString(R.string.notification_channel_id)
        val title = applicationContext.getString(R.string.notification_title)
        val cancel = applicationContext.getString(R.string.cancel_processing)
        val name = applicationContext.getString(R.string.channel_name)
        // This PendingIntent can be used to cancel the Worker.
        val intent = WorkManager.getInstance(applicationContext).createCancelPendingIntent(id)

        val builder = NotificationCompat.Builder(applicationContext, channelId)
            .setContentTitle(title)
            .setTicker(title)
            .setSmallIcon(R.drawable.baseline_gradient)
            .setOngoing(true)
            .addAction(android.R.drawable.ic_delete, cancel, intent)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createNotificationChannel(channelId, name).also {
                builder.setChannelId(it.id)
            }
        }
        return builder.build()
    }

    /**
     * Create the required notification channel for O+ devices.
     */
    @TargetApi(Build.VERSION_CODES.O)
    private fun createNotificationChannel(
        channelId: String,
        name: String
    ): NotificationChannel {
        return NotificationChannel(
            channelId, name, NotificationManager.IMPORTANCE_LOW
        ).also { channel ->
            notificationManager.createNotificationChannel(channel)
        }
    }

    companion object {
        private const val TAG = "SvImageToGalleryWrkr"
        private const val TITLE = "Filtered Image"
        private val DATE_FORMATTER =
            SimpleDateFormat("yyyy.MM.dd 'at' HH:mm:ss z", Locale.getDefault())
    }
}
