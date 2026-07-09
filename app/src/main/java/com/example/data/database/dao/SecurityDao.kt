package com.example.data.database.dao

import androidx.room.*
import com.example.data.database.entity.SecurityEventEntity
import com.example.data.database.entity.SecurityConfigEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SecurityDao {

    // --- Security Events ---
    @Query("SELECT * FROM security_events ORDER BY timestamp DESC")
    fun getAllSecurityEventsFlow(): Flow<List<SecurityEventEntity>>

    @Query("SELECT * FROM security_events ORDER BY timestamp DESC LIMIT 50")
    suspend fun getRecentSecurityEvents(): List<SecurityEventEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSecurityEvent(event: SecurityEventEntity)

    @Query("DELETE FROM security_events")
    suspend fun clearSecurityEvents()

    // --- Security Config Single Row ---
    @Query("SELECT * FROM security_configs WHERE id = 1")
    fun getSecurityConfigFlow(): Flow<SecurityConfigEntity?>

    @Query("SELECT * FROM security_configs WHERE id = 1")
    suspend fun getSecurityConfig(): SecurityConfigEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveSecurityConfig(config: SecurityConfigEntity)
}
