package com.example.ui.navigation

import kotlinx.serialization.Serializable

/**
 * Type-safe navigation routes compiled with Kotlin Serialization.
 * Eliminates the need for raw string-matching paths, preventing runtime failures.
 */
sealed interface Screen {

    @Serializable
    object Dashboard : Screen

    @Serializable
    data class Timer(val minutes: Int, val category: String) : Screen

    @Serializable
    object History : Screen

    @Serializable
    object Settings : Screen

    @Serializable
    object Timetable : Screen

    @Serializable
    object Blocker : Screen

    @Serializable
    object SpeechChallenge : Screen

    @Serializable
    object SecurityCenter : Screen

    @Serializable
    object Analytics : Screen

    @Serializable
    object Onboarding : Screen
}
