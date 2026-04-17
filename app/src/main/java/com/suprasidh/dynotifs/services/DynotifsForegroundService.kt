package com.suprasidh.dynotifs.services

import android.app.Notification
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.suprasidh.dynotifs.R
import com.suprasidh.dynotifs.app.DynotifsApp
import com.suprasidh.dynotifs.overlay.IslandStateMachine
import com.suprasidh.dynotifs.ui.main.MainActivity
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class DynotifsForegroundService : Service() {

    @Inject
    lateinit var stateMachine: IslandStateMachine

    @Inject
    lateinit var notificationManager: NotificationManager

    override fun onCreate() {
        super.onCreate()
        startForeground(
            NOTIF_ID,
            buildNotification(),
            ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE
        )
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.action == ACTION_STOP_SERVICE) {
            stopSelf()
        }
        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        super.onDestroy()
        stateMachine.hideIsland()
    }

    private fun buildNotification(): Notification {
        val openIntent = Intent(this, MainActivity::class.java)
        val openPendingIntent = PendingIntent.getActivity(
            this,
            0,
            openIntent,
            PendingIntent.FLAG_IMMUTABLE
        )

        val stopIntent = Intent(this, DynotifsForegroundService::class.java).apply {
            action = ACTION_STOP_SERVICE
        }
        val stopPendingIntent = PendingIntent.getService(
            this,
            1,
            stopIntent,
            PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(this, DynotifsApp.CHANNEL_ID_OVERLAY)
            .setContentTitle(getString(R.string.app_name))
            .setContentText("Dynotifs is running")
            .setSmallIcon(R.drawable.ic_notification)
            .setContentIntent(openPendingIntent)
            .setOngoing(true)
            .setSilent(true)
            .addAction(R.drawable.ic_stop, "KILL DYNOTIFS", stopPendingIntent)
            .build()
    }

    companion object {
        const val NOTIF_ID = 1001
        const val ACTION_STOP_SERVICE = "com.suprasidh.dynotifs.STOP_SERVICE"

        fun start(context: Context) {
            val intent = Intent(context, DynotifsForegroundService::class.java)
            context.startForegroundService(intent)
        }

        fun stop(context: Context) {
            val intent = Intent(context, DynotifsForegroundService::class.java)
            context.stopService(intent)
        }
    }
}