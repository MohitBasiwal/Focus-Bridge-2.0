package com.example.ui.screens

import androidx.lifecycle.viewModelScope
import com.example.domain.model.AnalyticsData
import com.example.domain.model.FocusSession
import com.example.domain.repository.AnalyticsRepository
import com.example.domain.repository.FocusRepository
import com.example.domain.usecase.GetAnalyticsUseCase
import com.example.ui.base.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AnalyticsState(
    val data: AnalyticsData? = null,
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

sealed interface AnalyticsAction {
    data class SimulateDistraction(val appName: String, val packageName: String) : AnalyticsAction
    data class LogMissedSession(val subjectName: String, val start: String, val end: String) : AnalyticsAction
    data class SimulateSuccessSession(val minutes: Int, val category: String) : AnalyticsAction
    object ClearAllData : AnalyticsAction
}

sealed interface AnalyticsEvent {
    data class Message(val content: String) : AnalyticsEvent
}

@HiltViewModel
class AnalyticsViewModel @Inject constructor(
    private val getAnalyticsUseCase: GetAnalyticsUseCase,
    private val analyticsRepository: AnalyticsRepository,
    private val focusRepository: FocusRepository
) : BaseViewModel<AnalyticsState, AnalyticsAction, AnalyticsEvent>(AnalyticsState(isLoading = true)) {

    init {
        observeAnalytics()
    }

    private fun observeAnalytics() {
        viewModelScope.launch {
            try {
                getAnalyticsUseCase.execute().collect { analyticsData ->
                    updateState { it.copy(data = analyticsData, isLoading = false) }
                }
            } catch (e: Exception) {
                updateState { it.copy(errorMessage = e.message, isLoading = false) }
            }
        }
    }

    override fun onAction(action: AnalyticsAction) {
        when (action) {
            is AnalyticsAction.SimulateDistraction -> {
                viewModelScope.launch {
                    analyticsRepository.addBlockedDistraction(action.packageName, action.appName)
                    sendEvent(AnalyticsEvent.Message("Blocked distraction logged: ${action.appName}"))
                }
            }
            is AnalyticsAction.LogMissedSession -> {
                viewModelScope.launch {
                    analyticsRepository.addMissedSession(action.subjectName, action.start, action.end)
                    sendEvent(AnalyticsEvent.Message("Missed study session logged: ${action.subjectName}"))
                }
            }
            is AnalyticsAction.SimulateSuccessSession -> {
                viewModelScope.launch {
                    val session = FocusSession(
                        durationMinutes = action.minutes,
                        category = action.category,
                        timestamp = System.currentTimeMillis(),
                        success = true
                    )
                    focusRepository.insertSession(session)
                    sendEvent(AnalyticsEvent.Message("Successful ${action.minutes}m focus session added!"))
                }
            }
            is AnalyticsAction.ClearAllData -> {
                viewModelScope.launch {
                    focusRepository.clearAllSessions()
                    analyticsRepository.clearBlockedDistractions()
                    analyticsRepository.clearMissedSessions()
                    sendEvent(AnalyticsEvent.Message("All focus analytics and statistics cleared!"))
                }
            }
        }
    }
}
