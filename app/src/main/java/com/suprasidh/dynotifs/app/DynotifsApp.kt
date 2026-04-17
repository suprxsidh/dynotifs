package com.suprasidh.dynotifs.app

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class DynotifsApp : Application() {

    override fun onCreate() {
        super.onCreate()
        createNotificationChannels()
    }

    private fun createNotificationChannels() {
        val notificationManager = getSystemService(NotificationManager::class.java)

        val overlayChannel = NotificationChannel(
            CHANNEL_ID_OVERLAY,
            "Dynotifs Overlay",
            NotificationManager.IMPORTANCE_LOW
        ).apply {
            description = "Foreground service for Dynotifs overlay"
            setShowBadge(false)
        }

        notificationManager.createNotificationChannel(overlayChannel)
    }

    companion object {
        const val CHANNEL_ID_OVERLAY = "dynotifs_overlay"
    }
}