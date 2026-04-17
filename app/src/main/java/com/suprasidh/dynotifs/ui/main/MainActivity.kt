package com.suprasidh.dynotifs.ui.main

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.suprasidh.dynotifs.data.datastore.DynotifsDataStore
import com.suprasidh.dynotifs.services.DynotifsForegroundService
import com.suprasidh.dynotifs.ui.onboarding.CalibrationScreen
import com.suprasidh.dynotifs.ui.onboarding.PermissionScreen
import com.suprasidh.dynotifs.ui.settings.SettingsScreen
import com.suprasidh.dynotifs.ui.settings.AppRegistryScreen
import com.suprasidh.dynotifs.util.PermissionsHelper
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class MainUiState {
    data object Loading : MainUiState()
    data object NeedsPermissions : MainUiState()
    data object NeedsCalibration : MainUiState()
    data object Ready : MainUiState()
}

@HiltViewModel
class MainViewModel @Inject constructor(
    private val dataStore: DynotifsDataStore
) : ViewModel() {

    private val _uiState = MutableStateFlow<MainUiState>(MainUiState.Loading)
    val uiState: StateFlow<MainUiState> = _uiState.asStateFlow()

    fun checkPermissions() {
        viewModelScope.launch {
            _uiState.value = MainUiState.NeedsPermissions
        }
    }

    fun onPermissionsGranted() {
        _uiState.value = MainUiState.NeedsCalibration
    }

    fun onCalibrationComplete() {
        viewModelScope.launch {
            dataStore.setOnboardingComplete(true)
            _uiState.value = MainUiState.Ready
        }
    }

    fun onNotificationPermissionGranted() {
        viewModelScope.launch {
            checkPermissions()
        }
    }
}

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by lazy {
        androidx.hilt.lifecycle.viewModel(this)
    }

    private val notificationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            viewModel.onNotificationPermissionGranted()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        window.statusBarColor = Color.TRANSPARENT
        window.navigationBarColor = Color.TRANSPARENT

        setContent {
            val uiState by viewModel.uiState.collectAsState()

            Surface(
                modifier = Modifier.fillMaxSize(),
                color = MaterialTheme.colorScheme.background
            ) {
                when (uiState) {
                    is MainUiState.Loading -> {
                        LoadingScreen()
                    }
                    is MainUiState.NeedsPermissions -> {
                        PermissionScreen(
                            onPermissionsGranted = { viewModel.onPermissionsGranted() }
                        )
                    }
                    is MainUiState.NeedsCalibration -> {
                        CalibrationScreen(
                            onCalibrationComplete = { viewModel.onCalibrationComplete() }
                        )
                    }
                    is MainUiState.Ready -> {
                        SettingsScreen(
                            onStartDynotifs = { startDynotifs() },
                            onStopDynotifs = { stopDynotifs() },
                            onOpenAppRegistry = { openAppRegistry() }
                        )
                    }
                }
            }
        }

        viewModel.checkPermissions()
    }

    private fun startDynotifs() {
        if (!PermissionsHelper.hasOverlayPermission(this)) {
            requestOverlayPermission()
            return
        }

        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            return
        }

        DynotifsForegroundService.start(this)
    }

    private fun stopDynotifs() {
        DynotifsForegroundService.stop(this)
    }

    private fun requestOverlayPermission() {
        val intent = Intent(
            Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
            android.net.Uri.parse("package:$packageName")
        )
        startActivity(intent)
    }

    private fun openAppRegistry() {
        startActivity(Intent(this, AppRegistryScreen::class.java))
    }
}

@Composable
fun LoadingScreen() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        verticalArrangement = Arrangement.Center
    ) {
        Text("Loading...")
    }
}