package com.example.domain.repository

import com.example.domain.model.StudySession
import kotlinx.coroutines.flow.Flow

/**
 * Repository interface governing study session timetable CRUD operations and schedule conflicts.
 */
interface TimetableRepository {
    
    /**
     * Observable flow of all configured study sessions.
     */
    fun getAllStudySessions(): Flow<List<StudySession>>

    /**
     * Retrieves a single study session by its unique ID.
     */
    suspend fun getStudySessionById(id: Long): StudySession?

    /**
     * Inserts or replaces a study session. Returns true if successful, or false if there is an overlapping session.
     */
    suspend fun saveStudySession(session: StudySession): Result<Unit>

    /**
     * Deletes the specified study session.
     */
    suspend fun deleteStudySession(session: StudySession)

    /**
     * Utility to check whether a proposed study session overlaps with any existing sessions (except itself).
     */
    suspend fun isOverlapping(proposedSession: StudySession): Boolean
}
