package com.suprasidh.dynotifs.util

import android.app.Notification

object Constants {
    const val NOTIF_ID = 1001
    const val CHANNEL_ID = "dynotifs_overlay"
    const val CHANNEL_NAME = "Dynotifs Overlay"

    const val TIMER_COLLAPSE_DELAY_MS = 5000L
    const val INACTIVITY_TIMEOUT_MS = 25_000L

    object NotificationCategories {
        const val CALL = Notification.CATEGORY_CALL
        const val MESSAGE = Notification.CATEGORY_MESSAGE
        const val ALARM = Notification.CATEGORY_ALARM
        const val TIMER = Notification.CATEGORY_TIMER
        const val TRANSPORT = Notification.CATEGORY_TRANSPORT
    }
}