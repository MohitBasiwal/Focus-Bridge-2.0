package com.example.data.datastore

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import com.example.domain.model.UserPreferences
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

/**
 * DataStore source handling local application preferences and settings.
 */
@Singleton
class UserPreferencesDataSource @Inject constructor(
    private val dataStore: DataStore<Preferences>
) {
    private object PreferencesKeys {
        val DEFAULT_FOCUS_DURATION_MINUTES = intPreferencesKey("default_focus_duration_minutes")
        val APP_BLOCKING_ENABLED = booleanPreferencesKey("app_blocking_enabled")
        val SELECTED_THEME = stringPreferencesKey("selected_theme")
        val DARK_MODE_ENABLED = booleanPreferencesKey("dark_mode_enabled")
        val DYNAMIC_COLOR_ENABLED = booleanPreferencesKey("dynamic_color_enabled")
        val PARAGRAPH_CATEGORY = stringPreferencesKey("paragraph_category")
        val SPEECH_DURATION_SECONDS = intPreferencesKey("speech_duration_seconds")
        val REMINDERS_ENABLED = booleanPreferencesKey("reminders_enabled")
        val SESSION_START_END_NOTIF_ENABLED = booleanPreferencesKey("session_start_end_notif_enabled")
        val MISSED_REMINDERS_ENABLED = booleanPreferencesKey("missed_reminders_enabled")
        val SUMMARIES_ENABLED = booleanPreferencesKey("summaries_enabled")
        val IS_ONBOARDED = booleanPreferencesKey("is_onboarded")
    }

    val userPreferencesFlow: Flow<UserPreferences> = dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { preferences ->
            UserPreferences(
                defaultFocusDurationMinutes = preferences[PreferencesKeys.DEFAULT_FOCUS_DURATION_MINUTES] ?: 25,
                appBlockingEnabled = preferences[PreferencesKeys.APP_BLOCKING_ENABLED] ?: false,
                selectedTheme = preferences[PreferencesKeys.SELECTED_THEME] ?: "GlassmorphicDark",
                darkModeEnabled = preferences[PreferencesKeys.DARK_MODE_ENABLED] ?: true,
                dynamicColorEnabled = preferences[PreferencesKeys.DYNAMIC_COLOR_ENABLED] ?: false,
                paragraphCategory = preferences[PreferencesKeys.PARAGRAPH_CATEGORY] ?: "Motivation",
                speechDurationSeconds = preferences[PreferencesKeys.SPEECH_DURATION_SECONDS] ?: 30,
                remindersEnabled = preferences[PreferencesKeys.REMINDERS_ENABLED] ?: true,
                sessionStartEndNotifEnabled = preferences[PreferencesKeys.SESSION_START_END_NOTIF_ENABLED] ?: true,
                missedRemindersEnabled = preferences[PreferencesKeys.MISSED_REMINDERS_ENABLED] ?: true,
                summariesEnabled = preferences[PreferencesKeys.SUMMARIES_ENABLED] ?: true,
                isOnboarded = preferences[PreferencesKeys.IS_ONBOARDED] ?: false
            )
        }

    suspend fun updateDefaultFocusDuration(minutes: Int) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.DEFAULT_FOCUS_DURATION_MINUTES] = minutes
        }
    }

    suspend fun updateAppBlockingEnabled(enabled: Boolean) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.APP_BLOCKING_ENABLED] = enabled
        }
    }

    suspend fun updateSelectedTheme(theme: String) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.SELECTED_THEME] = theme
        }
    }

    suspend fun updateDarkModeEnabled(enabled: Boolean) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.DARK_MODE_ENABLED] = enabled
        }
    }

    suspend fun updateDynamicColorEnabled(enabled: Boolean) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.DYNAMIC_COLOR_ENABLED] = enabled
        }
    }

    suspend fun updateParagraphCategory(category: String) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.PARAGRAPH_CATEGORY] = category
        }
    }

    suspend fun updateSpeechDurationSeconds(seconds: Int) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.SPEECH_DURATION_SECONDS] = seconds
        }
    }

    suspend fun updateRemindersEnabled(enabled: Boolean) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.REMINDERS_ENABLED] = enabled
        }
    }

    suspend fun updateSessionStartEndNotifEnabled(enabled: Boolean) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.SESSION_START_END_NOTIF_ENABLED] = enabled
        }
    }

    suspend fun updateMissedRemindersEnabled(enabled: Boolean) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.MISSED_REMINDERS_ENABLED] = enabled
        }
    }

    suspend fun updateSummariesEnabled(enabled: Boolean) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.SUMMARIES_ENABLED] = enabled
        }
    }

    suspend fun updateIsOnboarded(onboarded: Boolean) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.IS_ONBOARDED] = onboarded
        }
    }
}
