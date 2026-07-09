package com.example.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.toRoute
import com.example.ui.screens.DashboardScreen
import com.example.ui.screens.DashboardViewModel
import com.example.ui.screens.SettingsScreen
import com.example.ui.screens.SettingsViewModel
import com.example.ui.screens.OnboardingScreen
import com.example.ui.screens.OnboardingViewModel
import com.example.ui.screens.TimerScreen

/**
 * Modern Type-safe Jetpack Navigation Graph mapping available screens.
 * Integrates hiltViewModel() scopes for Clean ViewModel isolation.
 */
@Composable
fun NavigationGraph(
    navController: NavHostController,
    startDestination: Screen = Screen.Dashboard
) {
    val settingsViewModel: SettingsViewModel = hiltViewModel()
    val prefs by settingsViewModel.userPreferences.collectAsState()

    LaunchedEffect(prefs.isOnboarded) {
        // If not onboarded, redirect strictly to the onboarding screen
        if (!prefs.isOnboarded) {
            navController.navigate(Screen.Onboarding) {
                popUpTo(0) { inclusive = true }
            }
        }
    }

    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        composable<Screen.Dashboard> {
            val dashboardViewModel: DashboardViewModel = hiltViewModel()
            DashboardScreen(
                viewModel = dashboardViewModel,
                onNavigateToTimer = { minutes, category ->
                    navController.navigate(Screen.Timer(minutes, category))
                },
                onNavigateToHistory = {
                    // Optional history secondary pane transition
                },
                onNavigateToSettings = {
                    navController.navigate(Screen.Settings)
                },
                onNavigateToTimetable = {
                    navController.navigate(Screen.Timetable)
                },
                onNavigateToBlocker = {
                    navController.navigate(Screen.Blocker)
                },
                onNavigateToSpeechChallenge = {
                    navController.navigate(Screen.SpeechChallenge)
                },
                onNavigateToSecurity = {
                    navController.navigate(Screen.SecurityCenter)
                },
                onNavigateToAnalytics = {
                    navController.navigate(Screen.Analytics)
                }
            )
        }

        composable<Screen.Timetable> {
            val timetableViewModel: com.example.ui.screens.TimetableViewModel = hiltViewModel()
            com.example.ui.screens.TimetableScreen(
                viewModel = timetableViewModel,
                onBack = {
                    navController.popBackStack()
                }
            )
        }

        composable<Screen.Blocker> {
            val blockerViewModel: com.example.ui.screens.BlockerViewModel = hiltViewModel()
            com.example.ui.screens.BlockerScreen(
                viewModel = blockerViewModel,
                onBack = {
                    navController.popBackStack()
                }
            )
        }

        composable<Screen.SpeechChallenge> {
            val speechViewModel: com.example.ui.screens.SpeechViewModel = hiltViewModel()
            com.example.ui.screens.SpeechChallengeScreen(
                viewModel = speechViewModel,
                onBack = {
                    navController.popBackStack()
                }
            )
        }

        composable<Screen.Timer> { backStackEntry ->
            val timerRoute: Screen.Timer = backStackEntry.toRoute()
            TimerScreen(
                minutes = timerRoute.minutes,
                category = timerRoute.category,
                onSessionCompleted = { _ ->
                    navController.popBackStack()
                },
                onAborted = {
                    navController.popBackStack()
                }
            )
        }

        composable<Screen.Settings> {
            SettingsScreen(
                viewModel = settingsViewModel,
                onBack = {
                    navController.popBackStack()
                }
            )
        }

        composable<Screen.SecurityCenter> {
            val securityViewModel: com.example.ui.screens.SecurityCenterViewModel = hiltViewModel()
            com.example.ui.screens.SecurityCenterScreen(
                viewModel = securityViewModel,
                onBack = {
                    navController.popBackStack()
                }
            )
        }

        composable<Screen.Analytics> {
            val analyticsViewModel: com.example.ui.screens.AnalyticsViewModel = hiltViewModel()
            com.example.ui.screens.AnalyticsScreen(
                viewModel = analyticsViewModel,
                onBack = {
                    navController.popBackStack()
                }
            )
        }

        composable<Screen.Onboarding> {
            val onboardingViewModel: OnboardingViewModel = hiltViewModel()
            OnboardingScreen(
                viewModel = onboardingViewModel,
                onComplete = {
                    navController.navigate(Screen.Dashboard) {
                        popUpTo(Screen.Onboarding) { inclusive = true }
                    }
                }
            )
        }
    }
}
