package com.suprasidh.dynotifs.domain.model

sealed class IslandState {
    data object Hidden : IslandState()
    data object Pill : IslandState()
    data object Expanded : IslandState()
    data object Expanding : IslandState()
    data object Collapsing : IslandState()
}

data class NotificationItem(
    val key: String,
    val packageName: String,
    val title: String,
    val text: String,
    val icon: android.graphics.Bitmap?,
    val category: String?,
    val priorityLevel: PriorityLevel,
    val contentIntent: android.app.PendingIntent?,
    val actions: List<NotificationAction> = emptyList(),
    val remoteInput: android.app.RemoteInput? = null,
    val chronometerBase: Long? = null,
    val timestamp: Long = System.currentTimeMillis()
)

data class NotificationAction(
    val title: String,
    val intent: android.app.PendingIntent?,
    val remoteInput: android.app.RemoteInput? = null
)