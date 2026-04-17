package com.suprasidh.dynotifs.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Dao
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Room
import androidx.room.RoomDatabase
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "app_registry")
data class RegisteredApp(
    @PrimaryKey val packageName: String,
    val isBlocked: Boolean = false,
    val isMonitored: Boolean = true,
    val addedAt: Long = System.currentTimeMillis()
)

@Dao
interface AppDao {
    @Query("SELECT * FROM app_registry")
    fun getAllApps(): Flow<List<RegisteredApp>>

    @Query("SELECT * FROM app_registry WHERE isBlocked = 0")
    fun getAllowedApps(): Flow<List<RegisteredApp>>

    @Query("SELECT * FROM app_registry WHERE packageName = :packageName")
    suspend fun getApp(packageName: String): RegisteredApp?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertApp(app: RegisteredApp)

    @Query("DELETE FROM app_registry WHERE packageName = :packageName")
    suspend fun deleteApp(packageName: String)

    @Query("UPDATE app_registry SET isBlocked = :isBlocked WHERE packageName = :packageName")
    suspend fun setBlocked(packageName: String, isBlocked: Boolean)
}

@Database(entities = [RegisteredApp::class], version = 1, exportSchema = false)
abstract class AppRegistryDatabase : RoomDatabase() {
    abstract fun appDao(): AppDao

    companion object {
        @Volatile
        private var INSTANCE: AppRegistryDatabase? = null

        fun getInstance(context: Context): AppRegistryDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppRegistryDatabase::class.java,
                    "app_registry_db"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}