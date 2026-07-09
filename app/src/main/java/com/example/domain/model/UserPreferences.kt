package com.example.domain.model

/**
 * Domain model representing configured user preferences.
 */
data class UserPreferences(
    val defaultFocusDurationMinutes: Int = 25,
    val appBlockingEnabled: Boolean = false,
    val selectedTheme: String = "GlassmorphicDark",
    val darkModeEnabled: Boolean = true,
    val dynamicColorEnabled: Boolean = false,
    val paragraphCategory: String = "Motivation",
    val speechDurationSeconds: Int = 30,
    val remindersEnabled: Boolean = true,
    val sessionStartEndNotifEnabled: Boolean = true,
    val missedRemindersEnabled: Boolean = true,
    val summariesEnabled: Boolean = true,
    val isOnboarded: Boolean = false
)
