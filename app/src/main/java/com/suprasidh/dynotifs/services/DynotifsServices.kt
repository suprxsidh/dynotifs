package com.suprasidh.dynotifs.services

import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.BitmapDrawable
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import com.suprasidh.dynotifs.app.DynotifsApp
import com.suprasidh.dynotifs.domain.model.NotificationAction
import com.suprasidh.dynotifs.domain.model.NotificationItem
import com.suprasidh.dynotifs.domain.model.getPriorityLevel
import com.suprasidh.dynotifs.domain.queue.PriorityNotificationQueue
import com.suprasidh.dynotifs.data.datastore.DynotifsDataStore
import com.suprasidh.dynotifs.overlay.IslandStateMachine
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class DynotifsForegroundService : Service() {

    private val notificationManager by lazy { getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager }

    override fun onBind(intent: Intent?) = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.action == ACTION_STOP) {
            stopSelf()
            return START_NOT_STICKY
        }

        val stopIntent = Intent(this, DynotifsForegroundService::class.java).apply { action = ACTION_STOP }
        val stopPending = PendingIntent.getService(this, 0, stopIntent, PendingIntent.FLAG_IMMUTABLE)

        val openIntent = Intent(this, DynotifsApp::class.java)
        val openPending = PendingIntent.getActivity(this, 0, openIntent, PendingIntent.FLAG_IMMUTABLE)

        val notification = androidx.core.app.NotificationCompat.Builder(this, DynotifsApp.CHANNEL_ID)
            .setContentTitle("Dynotifs")
            .setContentText("Running - tap to open settings")
            .setSmallIcon(android.R.drawable.ic_menu_view)
            .setContentIntent(openPending)
            .addAction(android.R.drawable.ic_menu_close_clear_cancel, "KILL DYNOTIFS", stopPending)
            .setOngoing(true)
            .build()

        startForeground(NOTIF_ID, notification)
        return START_STICKY
    }

    companion object {
        const val NOTIF_ID = 1001
        const val ACTION_STOP = "com.suprasidh.dynotifs.STOP"
        fun start(ctx: Context) = ctx.startForegroundService(Intent(ctx, DynotifsForegroundService::class.java))
        fun stop(ctx: Context) = ctx.stopService(Intent(ctx, DynotifsForegroundService::class.java))
    }
}

class DynotifsNotificationService : NotificationListenerService() {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    private val queue = PriorityNotificationQueue()
    private val dataStore by lazy { DynotifsDataStore(applicationContext) }
    private val stateMachine by lazy { IslandStateMachine(queue, dataStore, com.suprasidh.dynotifs.overlay.OverlayWindowManager(applicationContext, dataStore)) }

    private val ignoredPackages = setOf("com.android.systemui", "com.android.launcher", "com.suprasidh.dynotifs")

    override fun onNotificationPosted(sbn: StatusBarNotification?) {
        sbn ?: return
        if (sbn.packageName in ignoredPackages) return

        val notification = sbn.notification ?: return
        val extras = notification.extras ?: return

        val item = NotificationItem(
            key = sbn.key,
            packageName = sbn.packageName,
            title = extras.getCharSequence("android.title")?.toString() ?: "",
            text = extras.getCharSequence("android.text")?.toString() ?: "",
            icon = getIcon(notification),
            category = notification.category,
            priorityLevel = getPriorityLevel(notification.category),
            contentIntent = notification.contentIntent,
            actions = extractActions(notification),
            chronometerBase = -1L,
            timestamp = sbn.postTime
        )

        scope.launch { stateMachine.onNotificationPosted(item) }
    }

    override fun onNotificationRemoved(sbn: StatusBarNotification?) {
        sbn ?: return
        scope.launch { stateMachine.onNotificationRemoved(sbn.key) }
    }

    private fun getIcon(notification: android.app.Notification): Bitmap? {
        return try {
            notification.smallIcon?.loadDrawable(this)?.toBitmap()
        } catch (e: Exception) { null }
    }

    private fun android.graphics.drawable.Drawable.toBitmap(): Bitmap {
        if (this is BitmapDrawable) return bitmap
        val bmp = Bitmap.createBitmap(intrinsicWidth.coerceAtLeast(1), intrinsicHeight.coerceAtLeast(1), Bitmap.Config.ARGB_8888)
        val c = Canvas(bmp)
        setBounds(0, 0, c.width, c.height)
        draw(c)
        return bmp
    }

    private fun extractActions(notification: android.app.Notification): List<NotificationAction> {
        return try {
            notification.actions?.map { action ->
                NotificationAction(action.title?.toString() ?: "", action.actionIntent, null)
            } ?: emptyList()
        } catch (e: Exception) { emptyList() }
    }
}