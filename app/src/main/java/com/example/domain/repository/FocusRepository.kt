package com.example.domain.repository

import com.example.domain.model.FocusSession
import com.example.domain.model.UserPreferences
import kotlinx.coroutines.flow.Flow

/**
 * Repository interface defining all data operations relating to Focus and Preference management.
 */
interface FocusRepository {
    fun getAllSessions(): Flow<List<FocusSession>>
    fun getSuccessfulSessions(): Flow<List<FocusSession>>
    suspend fun insertSession(session: FocusSession)
    suspend fun deleteSession(session: FocusSession)
    suspend fun clearAllSessions()

    fun getUserPreferences(): Flow<UserPreferences>
    suspend fun updateDefaultFocusDuration(minutes: Int)
    suspend fun updateAppBlockingEnabled(enabled: Boolean)
    suspend fun updateSelectedTheme(theme: String)
    suspend fun updateDarkModeEnabled(enabled: Boolean)
    suspend fun updateDynamicColorEnabled(enabled: Boolean)
    suspend fun updateParagraphCategory(category: String)
    suspend fun updateSpeechDurationSeconds(seconds: Int)
    suspend fun updateRemindersEnabled(enabled: Boolean)
    suspend fun updateSessionStartEndNotifEnabled(enabled: Boolean)
    suspend fun updateMissedRemindersEnabled(enabled: Boolean)
    suspend fun updateSummariesEnabled(enabled: Boolean)
    suspend fun updateIsOnboarded(onboarded: Boolean)
}
