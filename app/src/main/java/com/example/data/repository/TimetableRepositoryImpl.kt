package com.example.data.repository

import com.example.data.database.dao.StudySessionDao
import com.example.data.database.entity.StudySessionEntity
import com.example.domain.model.StudySession
import com.example.domain.repository.TimetableRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TimetableRepositoryImpl @Inject constructor(
    private val studySessionDao: StudySessionDao
) : TimetableRepository {

    override fun getAllStudySessions(): Flow<List<StudySession>> {
        return studySessionDao.getAllStudySessionsFlow().map { list ->
            list.map { it.toDomain() }
        }
    }

    override suspend fun getStudySessionById(id: Long): StudySession? {
        return studySessionDao.getStudySessionById(id)?.toDomain()
    }

    override suspend fun saveStudySession(session: StudySession): Result<Unit> {
        if (isOverlapping(session)) {
            return Result.failure(IllegalArgumentException("This session overlaps with an existing study session on your schedule!"))
        }
        
        val entity = session.toEntity()
        if (session.id == 0L) {
            studySessionDao.insertStudySession(entity)
        } else {
            studySessionDao.updateStudySession(entity)
        }
        return Result.success(Unit)
    }

    override suspend fun deleteStudySession(session: StudySession) {
        studySessionDao.deleteStudySession(session.toEntity())
    }

    override suspend fun isOverlapping(proposedSession: StudySession): Boolean {
        val existingSessions = studySessionDao.getAllStudySessions().map { it.toDomain() }
        
        return existingSessions.any { existing ->
            // Skip self-check for editing an existing session
            if (existing.id == proposedSession.id) return@any false
            
            proposedSession.overlapsWith(existing)
        }
    }
}

// Map Room entity to Domain Model
private fun StudySessionEntity.toDomain() = StudySession(
    id = id,
    subjectName = subjectName,
    startTime = startTime,
    endTime = endTime,
    repeatType = repeatType,
    repeatDays = repeatDays
)

// Map Domain Model to Room entity
private fun StudySession.toEntity() = StudySessionEntity(
    id = id,
    subjectName = subjectName,
    startTime = startTime,
    endTime = endTime,
    repeatType = repeatType,
    repeatDays = repeatDays
)
