package com.example.data.database.dao

import androidx.room.*
import com.example.data.database.entity.AllowedAppEntity
import com.example.data.database.entity.BlockedWebsiteEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface BlockerDao {

    // --- Allowed Apps ---
    @Query("SELECT * FROM allowed_apps ORDER BY appName ASC")
    fun getAllAllowedAppsFlow(): Flow<List<AllowedAppEntity>>

    @Query("SELECT * FROM allowed_apps")
    suspend fun getAllAllowedApps(): List<AllowedAppEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllowedApp(app: AllowedAppEntity)

    @Delete
    suspend fun deleteAllowedApp(app: AllowedAppEntity)

    @Query("DELETE FROM allowed_apps")
    suspend fun clearAllowedApps()

    // --- Blocked/Allowed Websites ---
    @Query("SELECT * FROM blocked_websites ORDER BY domain ASC")
    fun getAllBlockedWebsitesFlow(): Flow<List<BlockedWebsiteEntity>>

    @Query("SELECT * FROM blocked_websites")
    suspend fun getAllBlockedWebsites(): List<BlockedWebsiteEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBlockedWebsite(website: BlockedWebsiteEntity)

    @Delete
    suspend fun deleteBlockedWebsite(website: BlockedWebsiteEntity)

    @Query("DELETE FROM blocked_websites")
    suspend fun clearBlockedWebsites()
}
