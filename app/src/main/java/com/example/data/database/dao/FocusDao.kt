package com.example.data.database.dao

import androidx.room.*
import com.example.data.database.entity.FocusSessionEntity
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for handling Focus Session queries and manipulations.
 */
@Dao
interface FocusDao {
    @Query("SELECT * FROM focus_sessions ORDER BY timestamp DESC")
    fun getAllSessions(): Flow<List<FocusSessionEntity>>

    @Query("SELECT * FROM focus_sessions WHERE success = 1")
    fun getSuccessfulSessions(): Flow<List<FocusSessionEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSession(session: FocusSessionEntity)

    @Delete
    suspend fun deleteSession(session: FocusSessionEntity)

    @Query("DELETE FROM focus_sessions")
    suspend fun clearAllSessions()
}
