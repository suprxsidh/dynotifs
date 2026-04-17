package com.suprasidh.dynotifs.ui.onboarding

import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp

@Composable
fun PermissionScreen(onPermissionsGranted: () -> Unit) {
    val context = LocalContext.current

    Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        Column(modifier = Modifier.fillMaxSize().padding(32.dp)) {
            Text("Dynotifs Setup", style = MaterialTheme.typography.headlineMedium)
            Spacer(Modifier.height(24.dp))
            Text("Required permissions:")
            Spacer(Modifier.height(8.dp))
            Text("1. Overlay Permission - Display floating island")
            Spacer(Modifier.height(4.dp))
            Text("2. Notification Listener - Receive notifications")
            Spacer(Modifier.height(4.dp))
            Text("3. Post Notifications - Android 13+ requirement")

            Spacer(Modifier.height(32.dp))

            Button(
                onClick = {
                    val intent = Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:${context.packageName}"))
                    context.startActivity(intent)
                },
                modifier = Modifier.fillMaxWidth()
            ) { Text("Grant Overlay Permission") }

            Spacer(Modifier.height(16.dp))

            Button(
                onClick = {
                    context.startActivity(Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS))
                },
                modifier = Modifier.fillMaxWidth()
            ) { Text("Grant Notification Listener") }

            Spacer(Modifier.height(32.dp))

            Button(onClick = onPermissionsGranted, modifier = Modifier.fillMaxWidth()) { Text("Continue") }
        }
    }
}