package com.example.data.database.dao

import androidx.room.*
import com.example.data.database.entity.StudySessionEntity
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for handling Study Session queries and manipulations.
 */
@Dao
interface StudySessionDao {

    @Query("SELECT * FROM study_sessions ORDER BY startTime ASC")
    fun getAllStudySessionsFlow(): Flow<List<StudySessionEntity>>

    @Query("SELECT * FROM study_sessions")
    suspend fun getAllStudySessions(): List<StudySessionEntity>

    @Query("SELECT * FROM study_sessions WHERE id = :id")
    suspend fun getStudySessionById(id: Long): StudySessionEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStudySession(session: StudySessionEntity)

    @Update
    suspend fun updateStudySession(session: StudySessionEntity)

    @Delete
    suspend fun deleteStudySession(session: StudySessionEntity)

    @Query("DELETE FROM study_sessions")
    suspend fun clearAllStudySessions()
}
