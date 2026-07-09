package com.example.data.database.dao

import androidx.room.*
import com.example.data.database.entity.BlockedDistractionEntity
import com.example.data.database.entity.MissedSessionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface AnalyticsDao {

    @Query("SELECT * FROM blocked_distractions ORDER BY timestamp DESC")
    fun getAllBlockedDistractionsFlow(): Flow<List<BlockedDistractionEntity>>

    @Query("SELECT * FROM blocked_distractions")
    suspend fun getAllBlockedDistractions(): List<BlockedDistractionEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBlockedDistraction(distraction: BlockedDistractionEntity)

    @Query("DELETE FROM blocked_distractions")
    suspend fun clearAllBlockedDistractions()

    @Query("SELECT * FROM missed_sessions ORDER BY timestamp DESC")
    fun getAllMissedSessionsFlow(): Flow<List<MissedSessionEntity>>

    @Query("SELECT * FROM missed_sessions")
    suspend fun getAllMissedSessions(): List<MissedSessionEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMissedSession(missedSession: MissedSessionEntity)

    @Query("DELETE FROM missed_sessions")
    suspend fun clearAllMissedSessions()
}
