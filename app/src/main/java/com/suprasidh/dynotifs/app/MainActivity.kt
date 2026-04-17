package com.suprasidh.dynotifs.app

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import com.suprasidh.dynotifs.data.datastore.DynotifsDataStore
import com.suprasidh.dynotifs.data.model.AppSettings
import com.suprasidh.dynotifs.services.DynotifsForegroundService
import com.suprasidh.dynotifs.ui.onboarding.CalibrationScreen
import com.suprasidh.dynotifs.ui.onboarding.PermissionScreen
import com.suprasidh.dynotifs.ui.settings.AppRegistryScreen
import com.suprasidh.dynotifs.ui.settings.SettingsScreen
import com.suprasidh.dynotifs.util.PermissionsHelper
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

enum class Screen { LOADING, PERMISSIONS, CALIBRATION, SETTINGS, APP_REGISTRY }

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val dataStore by lazy { DynotifsDataStore(this) }

    private val notificationPermissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) { }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        window.statusBarColor = Color.TRANSPARENT
        window.navigationBarColor = Color.TRANSPARENT

        setContent {
            val settings by dataStore.appSettingsFlow.collectAsState(initial = AppSettings())
            var currentScreen by remember { mutableStateOf(Screen.LOADING) }
            val scope = rememberCoroutineScope()

            currentScreen = when {
                !settings.onboardingComplete -> Screen.CALIBRATION
                else -> Screen.SETTINGS
            }

            Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                when (currentScreen) {
                    Screen.LOADING -> { }
                    Screen.PERMISSIONS -> PermissionScreen {
                        scope.launch { dataStore.setOnboardingComplete(true) }
                        currentScreen = Screen.CALIBRATION
                    }
                    Screen.CALIBRATION -> CalibrationScreen {
                        scope.launch { dataStore.setOnboardingComplete(true) }
                        currentScreen = Screen.SETTINGS
                    }
                    Screen.SETTINGS -> SettingsScreen(
                        onStartDynotifs = { startDynotifs() },
                        onStopDynotifs = { DynotifsForegroundService.stop(this@MainActivity) },
                        onOpenAppRegistry = { currentScreen = Screen.APP_REGISTRY }
                    )
                    Screen.APP_REGISTRY -> AppRegistryScreen()
                }
            }
        }
    }

    private fun startDynotifs() {
        if (!PermissionsHelper.hasOverlayPermission(this)) {
            startActivity(android.content.Intent(android.provider.Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                android.net.Uri.parse("package:$packageName")))
            return
        }
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            return
        }
        DynotifsForegroundService.start(this)
    }
}