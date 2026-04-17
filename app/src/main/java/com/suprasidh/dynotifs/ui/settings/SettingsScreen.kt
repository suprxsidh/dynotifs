package com.suprasidh.dynotifs.ui.settings

import android.content.Intent
import android.provider.Settings
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.suprasidh.dynotifs.data.datastore.DynotifsDataStore
import kotlinx.coroutines.launch

@Composable
fun SettingsScreen(
    onStartDynotifs: () -> Unit,
    onStopDynotifs: () -> Unit,
    onOpenAppRegistry: () -> Unit
) {
    val context = LocalContext.current
    val dataStore = DynotifsDataStore(context)
    val settings by dataStore.appSettingsFlow.collectAsState(initial = com.suprasidh.dynotifs.data.model.AppSettings())
    val scope = rememberCoroutineScope()

    Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        Column(modifier = Modifier.fillMaxSize().padding(32.dp)) {
            Text("Dynotifs", style = MaterialTheme.typography.headlineLarge)
            Text("Dynamic Island for Android", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))

            Spacer(Modifier.height(32.dp))

            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("Enable Dynotifs", style = MaterialTheme.typography.titleMedium, modifier = Modifier.weight(1f))
                        Switch(checked = settings.islandEnabled, onCheckedChange = { 
                            scope.launch { dataStore.setIslandEnabled(it) }
                        })
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            Button(onClick = onStartDynotifs, modifier = Modifier.fillMaxWidth()) { Text("Start Dynotifs") }
            Spacer(Modifier.height(8.dp))
            Button(onClick = onStopDynotifs, modifier = Modifier.fillMaxWidth(), colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)) { Text("Stop Dynotifs") }

            Spacer(Modifier.height(24.dp))
            Button(onClick = onOpenAppRegistry, modifier = Modifier.fillMaxWidth()) { Text("App Registry") }

            Spacer(Modifier.height(16.dp))

            Card(modifier = Modifier.fillMaxWidth().clickable {
                context.startActivity(Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS))
            }) {
                Text("Open System Notification Settings", modifier = Modifier.padding(16.dp))
            }

            Spacer(Modifier.weight(1f))
            Text("Version 1.0.0", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f))
        }
    }
}