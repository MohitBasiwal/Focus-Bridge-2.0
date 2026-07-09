package com.example.domain.repository

import com.example.data.database.entity.BlockedDistractionEntity
import com.example.data.database.entity.MissedSessionEntity
import kotlinx.coroutines.flow.Flow

interface AnalyticsRepository {
    fun getAllBlockedDistractions(): Flow<List<BlockedDistractionEntity>>
    suspend fun addBlockedDistraction(packageName: String, appName: String)
    suspend fun clearBlockedDistractions()

    fun getAllMissedSessions(): Flow<List<MissedSessionEntity>>
    suspend fun addMissedSession(subjectName: String, startTime: String, endTime: String)
    suspend fun clearMissedSessions()
}
