package com.suprasidh.dynotifs.ui.settings

import android.content.Context
import android.content.pm.PackageManager
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.suprasidh.dynotifs.data.database.RegisteredApp
import com.suprasidh.dynotifs.data.repository.AppRegistryRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class AppRegistryViewModel @Inject constructor(
    private val repository: AppRegistryRepository
) : ViewModel() {

    private val _registeredApps = MutableStateFlow<List<RegisteredApp>>(emptyList())
    val registeredApps: StateFlow<List<RegisteredApp>> = _registeredApps.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _loading = MutableStateFlow(true)
    val loading: StateFlow<Boolean> = _loading.asStateFlow()

    private val _installedApps = MutableStateFlow<List<InstalledApp>>(emptyList())
    val installedApps: StateFlow<List<InstalledApp>> = _installedApps.asStateFlow()

    init {
        viewModelScope.launch {
            repository.getRegisteredApps().collect { apps ->
                _registeredApps.value = apps
            }
        }
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun loadInstalledApps(context: Context) {
        viewModelScope.launch {
            _loading.value = true
            val apps = withContext(Dispatchers.IO) {
                getNotificationCapableApps(context)
            }
            _installedApps.value = apps
            _loading.value = false
        }
    }

    fun addApp(packageName: String) {
        viewModelScope.launch {
            repository.addApp(packageName)
        }
    }

    fun removeApp(packageName: String) {
        viewModelScope.launch {
            repository.removeApp(packageName)
        }
    }

    fun setAppBlocked(packageName: String, isBlocked: Boolean) {
        viewModelScope.launch {
            repository.setAppBlocked(packageName, isBlocked)
        }
    }

    private fun getNotificationCapableApps(context: Context): List<InstalledApp> {
        val pm = context.packageManager
        val installedApps = pm.getInstalledApplications(PackageManager.GET_META_DATA)
        
        return installedApps
            .filter { app ->
                (app.flags and ApplicationInfo.FLAG_SYSTEM) == 0 ||
                app.packageName in commonNotificationPackages
            }
            .mapNotNull { appInfo ->
                try {
                    val appName = pm.getApplicationLabel(appInfo).toString()
                    val icon = try { pm.getApplicationIcon(appInfo.packageName) } catch (e: Exception) { null }
                    InstalledApp(
                        packageName = appInfo.packageName,
                        appName = appName,
                        icon = icon
                    )
                } catch (e: Exception) { null }
            }
            .sortedBy { it.appName.lowercase() }
            .take(150)
    }

    companion object {
        private val commonNotificationPackages = setOf(
            "com.whatsapp", "com.google.android.gm", "com.facebook.orca",
            "com.facebook.katana", "com.instagram.android", "com.twitter.android",
            "com.google.android.apps.inbox", "com.slack", "com.discord",
            "com.h两条.msq", "org.telegram.messenger", "ru.mail.mailapp",
            "com.google.android.calendar", "com.microsoft.teams", "com.zulipchat",
            "com.sify.umn", "com.linkedin.android", "com.viber.voip",
            "jp.naver.line.android", "com.bbm", "com.fring",
            "com.skype.raider", "com.wechat", "com.tencent.mm",
            "com.snapchat.android", "com.tiktok", "com.google.android.youtube",
            "tv.twitch", "com.reddit.frontpage", "com.netflix.mediaclient",
            "com.spotify.music", "com.amazon.mShop.android.shopping",
            "com.amazon.device.associateshopping", "com.ebay.mobile",
            "com.paypal.android", "com.squareup.card"
        )
    }
}