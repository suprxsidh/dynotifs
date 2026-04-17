package com.suprasidh.dynotifs.domain.model

import android.app.PendingIntent
import android.graphics.Bitmap

data class NotificationItem(
    val key: String,
    val packageName: String,
    val title: String,
    val text: String,
    val icon: Bitmap?,
    val category: String?,
    val priorityLevel: PriorityLevel,
    val contentIntent: PendingIntent?,
    val actions: List<NotificationAction> = emptyList(),
    val remoteInput: android.app.RemoteInput? = null,
    val chronometerBase: Long? = null,
    val timestamp: Long = System.currentTimeMillis()
)

data class NotificationAction(
    val title: String,
    val intent: PendingIntent?,
    val remoteInput: android.app.RemoteInput? = null
)