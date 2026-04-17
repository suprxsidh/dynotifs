package com.suprasidh.dynotifs.services

import android.app.Service
import android.content.Intent
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.suprasidh.dynotifs.app.DynotifsApp

class DynotifsForegroundService : Service() {
    override fun onBind(intent: Intent?): IBinder? = null
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        startForeground(NOTIF_ID, NotificationCompat.Builder(this, DynotifsApp.CHANNEL_ID).setSmallIcon(android.R.drawable.ic_menu_view).setContentTitle("Dynotifs").setContentText("Running").build())
        return START_STICKY
    }
    companion object { const val NOTIF_ID = 1001 }
}

class DynotifsNotificationService : android.service.notification.NotificationListenerService() {
    override fun onNotificationPosted(sbn: android.service.notification.StatusBarNotification?) {}
    override fun onNotificationRemoved(sbn: android.service.notification.StatusBarNotification?) {}
}