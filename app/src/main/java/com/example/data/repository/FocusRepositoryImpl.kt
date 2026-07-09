package com.example.data.repository

import com.example.data.database.dao.FocusDao
import com.example.data.database.entity.FocusSessionEntity
import com.example.data.datastore.UserPreferencesDataSource
import com.example.domain.model.FocusSession
import com.example.domain.model.UserPreferences
import com.example.domain.repository.FocusRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Concrete implementation of FocusRepository bridging database and preference data sources.
 */
@Singleton
class FocusRepositoryImpl @Inject constructor(
    private val focusDao: FocusDao,
    private val preferencesDataSource: UserPreferencesDataSource
) : FocusRepository {

    override fun getAllSessions(): Flow<List<FocusSession>> {
        return focusDao.getAllSessions().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override fun getSuccessfulSessions(): Flow<List<FocusSession>> {
        return focusDao.getSuccessfulSessions().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override fun insertSession(session: FocusSession) {
        // Room runs suspend database calls on its own dispatcher internally.
        // Safe to call directly.
        focusDao.insertSession(session.toEntity())
    }

    override suspend fun deleteSession(session: FocusSession) {
        focusDao.deleteSession(session.toEntity())
    }

    override suspend fun clearAllSessions() {
        focusDao.clearAllSessions()
    }

    override fun getUserPreferences(): Flow<UserPreferences> {
        return preferencesDataSource.userPreferencesFlow
    }

    override suspend fun updateDefaultFocusDuration(minutes: Int) {
        preferencesDataSource.updateDefaultFocusDuration(minutes)
    }

    override suspend fun updateAppBlockingEnabled(enabled: Boolean) {
        preferencesDataSource.updateAppBlockingEnabled(enabled)
    }

    override suspend fun updateSelectedTheme(theme: String) {
        preferencesDataSource.updateSelectedTheme(theme)
    }

    override suspend fun updateDarkModeEnabled(enabled: Boolean) {
        preferencesDataSource.updateDarkModeEnabled(enabled)
    }

    override suspend fun updateDynamicColorEnabled(enabled: Boolean) {
        preferencesDataSource.updateDynamicColorEnabled(enabled)
    }

    override suspend fun updateParagraphCategory(category: String) {
        preferencesDataSource.updateParagraphCategory(category)
    }

    override suspend fun updateSpeechDurationSeconds(seconds: Int) {
        preferencesDataSource.updateSpeechDurationSeconds(seconds)
    }

    override suspend fun updateRemindersEnabled(enabled: Boolean) {
        preferencesDataSource.updateRemindersEnabled(enabled)
    }

    override suspend fun updateSessionStartEndNotifEnabled(enabled: Boolean) {
        preferencesDataSource.updateSessionStartEndNotifEnabled(enabled)
    }

    override suspend fun updateMissedRemindersEnabled(enabled: Boolean) {
        preferencesDataSource.updateMissedRemindersEnabled(enabled)
    }

    override suspend fun updateSummariesEnabled(enabled: Boolean) {
        preferencesDataSource.updateSummariesEnabled(enabled)
    }

    override suspend fun updateIsOnboarded(onboarded: Boolean) {
        preferencesDataSource.updateIsOnboarded(onboarded)
    }
}

// Extension Mappers for Clean Architecture Isolation
private fun FocusSessionEntity.toDomain(): FocusSession = FocusSession(
    id = id,
    durationMinutes = durationMinutes,
    category = category,
    timestamp = timestamp,
    success = success
)

private fun FocusSession.toEntity(): FocusSessionEntity = FocusSessionEntity(
    id = id,
    durationMinutes = durationMinutes,
    category = category,
    timestamp = timestamp,
    success = success
)
