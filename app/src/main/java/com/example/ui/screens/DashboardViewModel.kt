package com.example.ui.screens

import androidx.lifecycle.viewModelScope
import com.example.domain.model.FocusSession
import com.example.domain.model.StudySession
import com.example.domain.repository.FocusRepository
import com.example.domain.repository.TimetableRepository
import com.example.ui.base.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import java.util.Calendar
import javax.inject.Inject

// UI State
data class DashboardState(
    val defaultMinutes: Int = 25,
    val isAppBlockingEnabled: Boolean = false,
    val selectedCategory: String = "Work",
    val recentSessions: List<FocusSession> = emptyList(),
    val upcomingStudySession: StudySession? = null,
    val isProtectionPaused: Boolean = false,
    val focusScore: Int = 0,
    val todayCompletedSessionsCount: Int = 0,
    val todayFocusTimeMinutes: Int = 0,
    val currentStreak: Int = 0,
    val isLoading: Boolean = false
)

// UI Action / Intent
sealed interface DashboardAction {
    data class SelectMinutes(val minutes: Int) : DashboardAction
    data class SelectCategory(val category: String) : DashboardAction
    data class ToggleAppBlocking(val enabled: Boolean) : DashboardAction
    object StartFocusSession : DashboardAction
    object ClearHistory : DashboardAction
}

// UI One-Shot Event
sealed interface DashboardEvent {
    data class NavigateToTimer(val minutes: Int, val category: String) : DashboardEvent
    data class Message(val content: String) : DashboardEvent
}

/**
 * Dashboard ViewModel extending [BaseViewModel] with Clean Architecture repository injections.
 */
@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val repository: FocusRepository,
    private val timetableRepository: TimetableRepository,
    private val securityRepository: com.example.domain.repository.SecurityRepository,
    private val getAnalyticsUseCase: com.example.domain.usecase.GetAnalyticsUseCase
) : BaseViewModel<DashboardState, DashboardAction, DashboardEvent>(DashboardState()) {

    init {
        loadDashboardData()
    }

    private fun loadDashboardData() {
        updateState { it.copy(isLoading = true) }
        
        viewModelScope.launch {
            // Combine our reactive preferences Flow, focus sessions, timetable sessions, security config, and analytics
            combine(
                repository.getUserPreferences(),
                repository.getAllSessions(),
                timetableRepository.getAllStudySessions(),
                securityRepository.getSecurityConfigFlow(),
                getAnalyticsUseCase.execute()
            ) { prefs, sessions, studySessions, securityConfig, analytics ->
                val currentDay = getCurrentDayOfWeek()
                val currentMinutes = getCurrentTimeInMinutes()
                
                // Filter sessions that occur today, and find the next upcoming one
                val upcoming = studySessions
                    .filter { it.activeDaysSet.contains(currentDay) }
                    .sortedBy { it.startTimeMinutes }
                    .firstOrNull { it.startTimeMinutes > currentMinutes }
                    ?: studySessions.filter { it.activeDaysSet.contains(currentDay) }.sortedBy { it.startTimeMinutes }.firstOrNull()

                DashboardState(
                    defaultMinutes = prefs.defaultFocusDurationMinutes,
                    isAppBlockingEnabled = prefs.appBlockingEnabled,
                    selectedCategory = currentState.selectedCategory,
                    recentSessions = sessions.take(5), // Keep top 5 sessions
                    upcomingStudySession = upcoming,
                    isProtectionPaused = securityConfig?.isProtectionPaused == true,
                    focusScore = analytics.focusScore,
                    todayCompletedSessionsCount = analytics.todayCompletedSessions,
                    todayFocusTimeMinutes = analytics.todayFocusTimeMinutes,
                    currentStreak = analytics.currentStreak,
                    isLoading = false
                )
            }.collect { combinedState ->
                updateState { combinedState }
            }
        }
    }

    private fun getCurrentDayOfWeek(): Int {
        val calendar = Calendar.getInstance()
        return when (calendar.get(Calendar.DAY_OF_WEEK)) {
            Calendar.MONDAY -> 1
            Calendar.TUESDAY -> 2
            Calendar.WEDNESDAY -> 3
            Calendar.THURSDAY -> 4
            Calendar.FRIDAY -> 5
            Calendar.SATURDAY -> 6
            Calendar.SUNDAY -> 7
            else -> 1
        }
    }

    private fun getCurrentTimeInMinutes(): Int {
        val calendar = Calendar.getInstance()
        return calendar.get(Calendar.HOUR_OF_DAY) * 60 + calendar.get(Calendar.MINUTE)
    }

    override fun onAction(action: DashboardAction) {
        when (action) {
            is DashboardAction.SelectMinutes -> {
                viewModelScope.launch {
                    repository.updateDefaultFocusDuration(action.minutes)
                    sendEvent(DashboardEvent.Message("Timer duration updated to ${action.minutes}m"))
                }
            }
            is DashboardAction.SelectCategory -> {
                updateState { it.copy(selectedCategory = action.category) }
            }
            is DashboardAction.ToggleAppBlocking -> {
                viewModelScope.launch {
                    repository.updateAppBlockingEnabled(action.enabled)
                    sendEvent(DashboardEvent.Message(
                        if (action.enabled) "Focus Bridge app blocker armed!" else "App blocker disabled."
                    ))
                }
            }
            is DashboardAction.StartFocusSession -> {
                sendEvent(DashboardEvent.NavigateToTimer(currentState.defaultMinutes, currentState.selectedCategory))
            }
            is DashboardAction.ClearHistory -> {
                viewModelScope.launch {
                    repository.clearAllSessions()
                    sendEvent(DashboardEvent.Message("Focus session log cleared."))
                }
            }
        }
    }
}
