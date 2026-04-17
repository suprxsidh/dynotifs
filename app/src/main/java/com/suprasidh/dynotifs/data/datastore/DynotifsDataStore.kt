package com.suprasidh.dynotifs.data.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.suprasidh.dynotifs.data.model.CalibrationData
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "dynotifs_prefs")

@Singleton
class DynotifsDataStore @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private object PreferencesKeys {
        val OFFSET_X = floatPreferencesKey("offset_x_percent")
        val OFFSET_Y = floatPreferencesKey("offset_y_percent")
        val WIDTH = floatPreferencesKey("width_percent")
        val HEIGHT = floatPreferencesKey("height_percent")
        val ISLAND_ENABLED = booleanPreferencesKey("island_enabled")
        val ONBOARDING_COMPLETE = booleanPreferencesKey("onboarding_complete")
    }

    val calibrationFlow: Flow<CalibrationData> = context.dataStore.data.map { prefs ->
        CalibrationData(
            offsetXPercent = prefs[PreferencesKeys.OFFSET_X] ?: 0.5f,
            offsetYPercent = prefs[PreferencesKeys.OFFSET_Y] ?: 0.02f,
            widthPercent = prefs[PreferencesKeys.WIDTH] ?: 0.30f,
            heightPercent = prefs[PreferencesKeys.HEIGHT] ?: 0.05f
        )
    }

    val islandEnabledFlow: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[PreferencesKeys.ISLAND_ENABLED] ?: true
    }

    val onboardingCompleteFlow: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[PreferencesKeys.ONBOARDING_COMPLETE] ?: false
    }

    suspend fun updateCalibration(data: CalibrationData) {
        context.dataStore.edit { prefs ->
            prefs[PreferencesKeys.OFFSET_X] = data.offsetXPercent
            prefs[PreferencesKeys.OFFSET_Y] = data.offsetYPercent
            prefs[PreferencesKeys.WIDTH] = data.widthPercent
            prefs[PreferencesKeys.HEIGHT] = data.heightPercent
        }
    }

    suspend fun setIslandEnabled(enabled: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[PreferencesKeys.ISLAND_ENABLED] = enabled
        }
    }

    suspend fun setOnboardingComplete(complete: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[PreferencesKeys.ONBOARDING_COMPLETE] = complete
        }
    }
}