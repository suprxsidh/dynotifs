package com.suprasidh.dynotifs.ui.settings

import android.content.Intent
import android.provider.Settings
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppRegistryScreen() {
    val context = LocalContext.current

    Scaffold(modifier = Modifier.fillMaxSize(), topBar = { TopAppBar(title = { Text("App Registry") }) }) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp)) {
            Text("Manage which apps show notifications on the Dynotifs island.", style = MaterialTheme.typography.bodyMedium)
            Spacer(Modifier.height(16.dp))
            Text("No apps registered yet.", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
            Spacer(Modifier.height(16.dp))
            Text("Power User:", style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(8.dp))
            Text("Grant WRITE_SECURE_SETTINGS via ADB to auto-suppress system notifications.", style = MaterialTheme.typography.bodySmall)
            Spacer(Modifier.height(16.dp))
            Card(modifier = Modifier.fillMaxWidth()) {
                Text("Open System Notification Settings", modifier = Modifier.padding(16.dp), color = MaterialTheme.colorScheme.primary)
            }
        }
    }
}