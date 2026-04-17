package com.suprasidh.dynotifs.ui.settings

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.suprasidh.dynotifs.data.datastore.DynotifsDataStore

@Composable
fun SettingsScreen(
    onStartDynotifs: () -> Unit,
    onStopDynotifs: () -> Unit,
    onOpenAppRegistry: () -> Unit,
    modifier: Modifier = Modifier
) {
    val dataStore = com.suprasidh.dynotifs.data.datastore.DynotifsDataStore(
        androidx.compose.ui.platform.LocalContext.current.baseContext as android.content.Context
    )
    val isEnabled by dataStore.islandEnabledFlow.collectAsState(initial = true)

    Surface(
        modifier = modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp)
        ) {
            Text(
                text = "Dynotifs",
                style = MaterialTheme.typography.headlineLarge
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Dynamic Island for Android",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )

            Spacer(modifier = Modifier.height(32.dp))

            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Enable Dynotifs",
                            style = MaterialTheme.typography.titleMedium,
                            modifier = Modifier.weight(1f)
                        )
                        Switch(
                            checked = isEnabled,
                            onCheckedChange = { enabled ->
                                // Will be handled by service
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = onStartDynotifs,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Start Dynotifs")
            }

            Spacer(modifier = Modifier.height(8.dp))

            Button(
                onClick = onStopDynotifs,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.error
                )
            ) {
                Text("Stop Dynotifs")
            }

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = onOpenAppRegistry,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("App Registry")
            }

            Spacer(modifier = Modifier.weight(1f))

            Text(
                text = "Version 1.0.0",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
            )
        }
    }
}