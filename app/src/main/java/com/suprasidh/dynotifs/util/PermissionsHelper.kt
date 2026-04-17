package com.suprasidh.dynotifs.util

import android.content.Context
import android.provider.Settings

object PermissionsHelper {

    fun hasOverlayPermission(context: Context): Boolean {
        return Settings.canDrawOverlays(context)
    }

    fun hasNotificationListenerPermission(context: Context): Boolean {
        val componentName = android.content.ComponentName(
            context,
            com.suprasidh.dynotifs.services.DynotifsNotificationService::class.java
        )
        val enabledListeners = android.provider.Settings.Secure.getString(
            context.contentResolver,
            "enabled_notification_listeners"
        )
        return enabledListeners?.contains(componentName.flattenToString()) == true
    }

    fun isNotificationAccessGranted(context: Context): Boolean {
        return hasNotificationListenerPermission(context)
    }

    fun hasWriteSecureSettingsPermission(): Boolean {
        // Can only be granted via ADB
        // adb shell pm grant com.suprasidh.dynotifs android.permission.WRITE_SECURE_SETTINGS
        return false
    }

    fun getSystemNotificationSettingsIntent(): Intent {
        return Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS)
    }

    fun getOverlaySettingsIntent(context: Context): Intent {
        return Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION).apply {
            data = android.net.Uri.parse("package:${context.packageName}")
        }
    }
}