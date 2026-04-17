package com.suprasidh.dynotifs.data.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.suprasidh.dynotifs.data.model.AppSettings
import com.suprasidh.dynotifs.data.model.CalibrationData
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "dynotifs_prefs")

class DynotifsDataStore(private val context: Context) {

    private object Keys {
        val OFFSET_X = floatPreferencesKey("offset_x")
        val OFFSET_Y = floatPreferencesKey("offset_y")
        val WIDTH = floatPreferencesKey("width")
        val HEIGHT = floatPreferencesKey("height")
        val ISLAND_ENABLED = booleanPreferencesKey("island_enabled")
        val ONBOARDING_COMPLETE = booleanPreferencesKey("onboarding_complete")
    }

    val calibrationFlow: Flow<CalibrationData> = context.dataStore.data.map { prefs ->
        CalibrationData(
            offsetXPercent = prefs[Keys.OFFSET_X] ?: 0.5f,
            offsetYPercent = prefs[Keys.OFFSET_Y] ?: 0.02f,
            widthPercent = prefs[Keys.WIDTH] ?: 0.30f,
            heightPercent = prefs[Keys.HEIGHT] ?: 0.05f
        )
    }

    val appSettingsFlow: Flow<AppSettings> = context.dataStore.data.map { prefs ->
        AppSettings(
            islandEnabled = prefs[Keys.ISLAND_ENABLED] ?: true,
            onboardingComplete = prefs[Keys.ONBOARDING_COMPLETE] ?: false
        )
    }

    suspend fun updateCalibration(data: CalibrationData) {
        context.dataStore.edit { prefs ->
            prefs[Keys.OFFSET_X] = data.offsetXPercent
            prefs[Keys.OFFSET_Y] = data.offsetYPercent
            prefs[Keys.WIDTH] = data.widthPercent
            prefs[Keys.HEIGHT] = data.heightPercent
        }
    }

    suspend fun setIslandEnabled(enabled: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[Keys.ISLAND_ENABLED] = enabled
        }
    }

    suspend fun setOnboardingComplete(complete: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[Keys.ONBOARDING_COMPLETE] = complete
        }
    }
}