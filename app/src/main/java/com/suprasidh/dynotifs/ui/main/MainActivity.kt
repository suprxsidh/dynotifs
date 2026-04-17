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
import com.suprasidh.dynotifs.util.PermissionsHelper
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class MainUiState {
    data object Loading
    data object NeedsPermissions
    data object NeedsCalibration
    data object Ready
}

@HiltViewModel
class MainViewModel @Inject constructor(
    private val dataStore: DynotifsDataStore
) : ViewModel() {
    private val _uiState = MutableStateFlow<MainUiState>(MainUiState.Loading)
    val uiState: StateFlow<MainUiState> = _uiState.asStateFlow()

    fun checkPermissions() { _uiState.value = MainUiState.NeedsPermissions }
    fun onPermissionsGranted() { _uiState.value = MainUiState.NeedsCalibration }
    fun onCalibrationComplete() { _uiState.value = MainUiState.Ready }
    fun onNotificationPermissionGranted() { checkPermissions() }
}

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    private val viewModel: MainViewModel by lazy { (this as ComponentActivity).viewModels<MainViewModel>().value }

    private val notificationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { if (it) viewModel.onNotificationPermissionGranted() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        window.statusBarColor = Color.TRANSPARENT
        window.navigationBarColor = Color.TRANSPARENT

        setContent {
            val uiState by viewModel.uiState.collectAsState()
            Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                when (uiState) {
                    MainUiState.Loading -> LoadingScreen()
                    MainUiState.NeedsPermissions -> PermissionScreen { viewModel.onPermissionsGranted() }
                    MainUiState.NeedsCalibration -> CalibrationScreen { viewModel.onCalibrationComplete() }
                    MainUiState.Ready -> SettingsScreen({ startDynotifs() }, { stopDynotifs() }, { })
                }
            }
        }
        viewModel.checkPermissions()
    }

    private fun startDynotifs() {
        if (!PermissionsHelper.hasOverlayPermission(this)) { requestOverlayPermission(); return }
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS); return
        }
        DynotifsForegroundService.start(this)
    }
    private fun stopDynotifs() = DynotifsForegroundService.stop(this)
    private fun requestOverlayPermission() = startActivity(Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, android.net.Uri.parse("package:$packageName")))
}

@Composable fun LoadingScreen() = Column(Modifier.fillMaxSize().padding(32.dp), Arrangement.Center) { Text("Loading...") }