/*
 * Copyright (C) 2021 The Android Open Source Project
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

@file:JvmName("NotificationUtils")

package com.example.background.workers

import android.annotation.TargetApi
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.work.WorkManager
import com.example.background.library.R
import java.util.UUID

/**
 * Create the notification and required channel (O+) for running work in a foreground service.
 */
fun createNotification(context: Context, workRequestId: UUID, notificationTitle: String): Notification {
    val channelId = context.getString(R.string.notification_channel_id)
    val cancelText = context.getString(R.string.cancel_processing)
    val name = context.getString(R.string.channel_name)
    // This PendingIntent can be used to cancel the Worker.
    val cancelIntent = WorkManager.getInstance(context).createCancelPendingIntent(workRequestId)

    val builder = NotificationCompat.Builder(context, channelId)
        .setContentTitle(notificationTitle)
        .setTicker(notificationTitle)
        .setSmallIcon(R.drawable.baseline_gradient)
        .setOngoing(true)
        .addAction(android.R.drawable.ic_delete, cancelText, cancelIntent)
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        createNotificationChannel(context, channelId, name).also {
            builder.setChannelId(it.id)
        }
    }
    return builder.build()
}

/**
 * Create the required notification channel for O+ devices.
 */
@TargetApi(Build.VERSION_CODES.O)
fun createNotificationChannel(
    context: Context,
    channelId: String,
    name: String,
    notificationImportance: Int = NotificationManager.IMPORTANCE_HIGH
): NotificationChannel {
    val notificationManager =
        context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    return NotificationChannel(
        channelId, name, notificationImportance
    ).also { channel ->
        notificationManager.createNotificationChannel(channel)
    }
}
