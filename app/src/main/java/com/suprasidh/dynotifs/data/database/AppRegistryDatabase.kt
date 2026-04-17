package com.suprasidh.dynotifs.data.database

import android.content.Context
import androidx.room.Dao
import androidx.room.Database
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
    val addedAt: Long = System.currentTimeMillis()
)

@Dao
interface AppDao {
    @Query("SELECT * FROM app_registry") fun getAllApps(): Flow<List<RegisteredApp>>
    @Insert(onConflict = OnConflictStrategy.REPLACE) suspend fun insert(app: RegisteredApp)
    @Query("DELETE FROM app_registry WHERE packageName = :pkg") suspend fun delete(pkg: String)
    @Query("UPDATE app_registry SET isBlocked = :blocked WHERE packageName = :pkg") suspend fun setBlocked(pkg: String, blocked: Boolean)
}

@Database(entities = [RegisteredApp::class], version = 1, exportSchema = false)
abstract class AppRegistryDatabase : RoomDatabase() {
    abstract fun appDao(): AppDao
    companion object {
        @Volatile private var INSTANCE: AppRegistryDatabase? = null
        fun getInstance(ctx: Context): AppRegistryDatabase = INSTANCE ?: synchronized(this) {
            Room.databaseBuilder(ctx.applicationContext, AppRegistryDatabase::class.java, "app_registry").build().also { INSTANCE = it }
        }
    }
}