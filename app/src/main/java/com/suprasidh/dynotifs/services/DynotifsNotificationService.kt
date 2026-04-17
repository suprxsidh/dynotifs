package com.suprasidh.dynotifs.services

import android.app.Notification
import android.content.ComponentName
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.BitmapDrawable
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import com.suprasidh.dynotifs.domain.model.NotificationAction
import com.suprasidh.dynotifs.domain.model.NotificationItem
import com.suprasidh.dynotifs.domain.model.PriorityLevel
import com.suprasidh.dynotifs.domain.model.getPriorityLevel
import com.suprasidh.dynotifs.overlay.IslandStateMachine
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
class DynotifsNotificationService : NotificationListenerService() {

    @Inject
    lateinit var stateMachine: IslandStateMachine

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)

    override fun onNotificationPosted(sbn: StatusBarNotification?) {
        sbn ?: return

        val notification = sbn.notification ?: return
        val extras = notification.extras ?: return

        if (isSystemPackage(sbn.packageName)) return

        val key = sbn.key
        val title = extras.getCharSequence(android.app.Notification.EXTRA_TITLE)?.toString() ?: ""
        val text = extras.getCharSequence(android.app.Notification.EXTRA_TEXT)?.toString() ?: ""
        val category = notification.category

        val icon = getNotificationIcon(notification)
        val contentIntent = notification.contentIntent
        val priorityLevel = getPriorityLevel(category)

        val actions = extractActions(notification)

        val chronometerBase = extras.getLong(android.app.Notification.EXTRA_CHRONOMETER_BASE, -1L).takeIf { it > 0 }

        val item = NotificationItem(
            key = key,
            packageName = sbn.packageName,
            title = title,
            text = text,
            icon = icon,
            category = category,
            priorityLevel = priorityLevel,
            contentIntent = contentIntent,
            actions = actions,
            chronometerBase = chronometerBase,
            timestamp = sbn.postTime
        )

        scope.launch {
            stateMachine.onNotificationPosted(item)
        }
    }

    override fun onNotificationRemoved(sbn: StatusBarNotification?) {
        sbn ?: return

        val key = sbn.key

        scope.launch {
            stateMachine.onNotificationRemoved(key)
        }
    }

    override fun onListenerConnected() {
        super.onListenerConnected()
    }

    override fun onListenerDisconnected() {
        super.onListenerDisconnected()
    }

    private fun isSystemPackage(packageName: String): Boolean {
        return packageName in SYSTEM_IGNORED_PACKAGES
    }

    private fun getNotificationIcon(notification: Notification): Bitmap? {
        return try {
            val drawable = notification.smallIcon?.loadDrawable(this)
            drawable?.toBitmap()
        } catch (e: Exception) {
            null
        }
    }

    private fun android.graphics.drawable.Drawable.toBitmap(): Bitmap {
        if (this is BitmapDrawable) {
            return bitmap
        }

        val bitmap = Bitmap.createBitmap(
            intrinsicWidth.coerceAtLeast(1),
            intrinsicHeight.coerceAtLeast(1),
            Bitmap.Config.ARGB_8888
        )
        val canvas = Canvas(bitmap)
        setBounds(0, 0, canvas.width, canvas.height)
        draw(canvas)
        return bitmap
    }

    private fun extractActions(notification: Notification): List<NotificationAction> {
        return try {
            val actionArray = notification.actions ?: return emptyList()
            actionArray.map { action ->
                NotificationAction(
                    title = action.title?.toString() ?: "",
                    intent = action.actionIntent,
                    remoteInput = action.remoteInput
                )
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    companion object {
        private val SYSTEM_IGNORED_PACKAGES = setOf(
            "com.android.systemui",
            "com.android.launcher",
            "com.google.android.gms",
            "com.suprasidh.dynotifs"
        )

        fun getPackageName(): String = "com.suprasidh.dynotifs"
    }
}