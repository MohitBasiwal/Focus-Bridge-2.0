package com.example.data.repository

import com.example.data.database.dao.AnalyticsDao
import com.example.data.database.entity.BlockedDistractionEntity
import com.example.data.database.entity.MissedSessionEntity
import com.example.domain.repository.AnalyticsRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AnalyticsRepositoryImpl @Inject constructor(
    private val analyticsDao: AnalyticsDao
) : AnalyticsRepository {

    override fun getAllBlockedDistractions(): Flow<List<BlockedDistractionEntity>> {
        return analyticsDao.getAllBlockedDistractionsFlow()
    }

    override suspend fun addBlockedDistraction(packageName: String, appName: String) {
        analyticsDao.insertBlockedDistraction(
            BlockedDistractionEntity(
                packageNameOrDomain = packageName,
                appNameOrTitle = appName
            )
        )
    }

    override suspend fun clearBlockedDistractions() {
        analyticsDao.clearAllBlockedDistractions()
    }

    override fun getAllMissedSessions(): Flow<List<MissedSessionEntity>> {
        return analyticsDao.getAllMissedSessionsFlow()
    }

    override suspend fun addMissedSession(subjectName: String, startTime: String, endTime: String) {
        analyticsDao.insertMissedSession(
            MissedSessionEntity(
                subjectName = subjectName,
                scheduledStartTime = startTime,
                scheduledEndTime = endTime
            )
        )
    }

    override suspend fun clearMissedSessions() {
        analyticsDao.clearAllMissedSessions()
    }
}
