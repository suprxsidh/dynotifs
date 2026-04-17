package com.suprasidh.dynotifs.ui.main

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.suprasidh.dynotifs.data.datastore.DynotifsDataStore
import com.suprasidh.dynotifs.util.PermissionsHelper
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
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
    @ApplicationContext private val context: Context,
    private val dataStore: DynotifsDataStore
) : ViewModel() {

    private val _uiState = MutableStateFlow<MainUiState>(MainUiState.Loading)
    val uiState: StateFlow<MainUiState> = _uiState.asStateFlow()

    fun checkPermissions() {
        viewModelScope.launch {
            val hasOverlay = PermissionsHelper.hasOverlayPermission(context)
            val hasNotificationListener = PermissionsHelper.hasNotificationListenerPermission(context)
            val onboardingComplete = dataStore.onboardingCompleteFlow.collectAsState(initial = false).value

            _uiState.value = when {
                !hasOverlay || !hasNotificationListener -> MainUiState.NeedsPermissions
                !onboardingComplete -> MainUiState.NeedsCalibration
                else -> MainUiState.Ready
            }
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