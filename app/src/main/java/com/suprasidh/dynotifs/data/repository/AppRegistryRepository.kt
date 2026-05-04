package com.suprasidh.dynotifs.data.repository

import android.content.Context
import com.suprasidh.dynotifs.data.database.AppDao
import com.suprasidh.dynotifs.data.database.AppRegistryDatabase
import com.suprasidh.dynotifs.data.database.RegisteredApp
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AppRegistryRepository @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val database by lazy { AppRegistryDatabase.getInstance(context) }
    private val appDao: AppDao get() = database.appDao()

    fun getRegisteredApps(): Flow<List<RegisteredApp>> = appDao.getAllApps()

    suspend fun addApp(packageName: String) {
        appDao.insert(RegisteredApp(packageName = packageName))
    }

    suspend fun removeApp(packageName: String) {
        appDao.delete(packageName)
    }

    suspend fun setAppBlocked(packageName: String, isBlocked: Boolean) {
        appDao.setBlocked(packageName, isBlocked)
    }
}