package com.example.ui.screens

import androidx.lifecycle.viewModelScope
import com.example.domain.model.StudySession
import com.example.domain.repository.TimetableRepository
import com.example.ui.base.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

// Timetable UI State representation
data class TimetableState(
    val studySessions: List<StudySession> = emptyList(),
    val isEditingSession: Boolean = false,
    val selectedSessionId: Long? = null, // null means a brand new session, non-null means we are editing
    val subjectNameInput: String = "",
    val startTimeInput: String = "09:00",
    val endTimeInput: String = "10:00",
    val repeatTypeInput: String = "None", // "None", "Daily", "Weekly", "Custom"
    val customDaysInput: Set<Int> = emptySet(), // integers 1 (Mon) to 7 (Sun)
    val isSaving: Boolean = false,
    val errorMessage: String? = null,
    val puzzleDifficulty: String = "MEDIUM"
)

// Actions flowing from the Composable to the ViewModel
sealed interface TimetableAction {
    object AddSessionClick : TimetableAction
    data class EditSessionClick(val session: StudySession) : TimetableAction
    data class DeleteSessionClick(val session: StudySession) : TimetableAction
    object CancelEditClick : TimetableAction
    
    data class SubjectNameChanged(val value: String) : TimetableAction
    data class StartTimeChanged(val value: String) : TimetableAction
    data class EndTimeChanged(val value: String) : TimetableAction
    data class RepeatTypeChanged(val value: String) : TimetableAction
    data class CustomDayToggled(val day: Int) : TimetableAction
    
    object SaveSessionClick : TimetableAction
    object DismissError : TimetableAction
    
    // Logging failed puzzle solve attempt
    data class LogFailedPuzzle(val details: String) : TimetableAction
    // Logging successful change event
    data class LogSecurityEvent(val eventType: String, val details: String, val severity: String) : TimetableAction
}

// Single-shot UI event dispatching
sealed interface TimetableEvent {
    data class ToastMessage(val message: String) : TimetableEvent
    object SaveSuccess : TimetableEvent
}

@HiltViewModel
class TimetableViewModel @Inject constructor(
    private val repository: TimetableRepository,
    private val securityRepository: com.example.domain.repository.SecurityRepository
) : BaseViewModel<TimetableState, TimetableAction, TimetableEvent>(TimetableState()) {

    init {
        loadSessions()
        observeSecurityConfig()
    }

    private fun observeSecurityConfig() {
        viewModelScope.launch {
            securityRepository.getSecurityConfigFlow().collect { config ->
                val diff = config?.puzzleDifficulty ?: "MEDIUM"
                updateState { it.copy(puzzleDifficulty = diff) }
            }
        }
    }

    private fun loadSessions() {
        viewModelScope.launch {
            repository.getAllStudySessions().collectLatest { list ->
                updateState { it.copy(studySessions = list) }
            }
        }
    }

    override fun onAction(action: TimetableAction) {
        when (action) {
            is TimetableAction.AddSessionClick -> {
                // Determine current day of week to pre-select for standard convenience (1 = Monday)
                val currentDayOfWeek = 1 
                updateState {
                    it.copy(
                        isEditingSession = true,
                        selectedSessionId = null,
                        subjectNameInput = "",
                        startTimeInput = "09:00",
                        endTimeInput = "10:00",
                        repeatTypeInput = "None",
                        customDaysInput = setOf(currentDayOfWeek),
                        errorMessage = null
                    )
                }
            }
            is TimetableAction.EditSessionClick -> {
                updateState {
                    it.copy(
                        isEditingSession = true,
                        selectedSessionId = action.session.id,
                        subjectNameInput = action.session.subjectName,
                        startTimeInput = action.session.startTime,
                        endTimeInput = action.session.endTime,
                        repeatTypeInput = action.session.repeatType,
                        customDaysInput = action.session.activeDaysSet,
                        errorMessage = null
                    )
                }
            }
            is TimetableAction.DeleteSessionClick -> {
                viewModelScope.launch {
                    repository.deleteStudySession(action.session)
                    securityRepository.logSecurityEvent(
                        eventType = "TIMETABLE_CHANGED",
                        details = "Deleted study class schedule: '${action.session.subjectName}'.",
                        severity = "WARNING"
                    )
                    sendEvent(TimetableEvent.ToastMessage("Study session deleted: ${action.session.subjectName}"))
                }
            }
            is TimetableAction.CancelEditClick -> {
                updateState { it.copy(isEditingSession = false, errorMessage = null) }
            }
            is TimetableAction.SubjectNameChanged -> {
                updateState { it.copy(subjectNameInput = action.value) }
            }
            is TimetableAction.StartTimeChanged -> {
                updateState { it.copy(startTimeInput = action.value) }
            }
            is TimetableAction.EndTimeChanged -> {
                updateState { it.copy(endTimeInput = action.value) }
            }
            is TimetableAction.RepeatTypeChanged -> {
                val updatedDays = when (action.value) {
                    "Daily" -> (1..7).toSet()
                    "Weekly" -> setOf(1) // Default to Monday
                    "None" -> setOf(1) // Default to Monday
                    else -> emptySet()
                }
                updateState {
                    it.copy(
                        repeatTypeInput = action.value,
                        customDaysInput = updatedDays
                    )
                }
            }
            is TimetableAction.CustomDayToggled -> {
                val currentDays = currentState.customDaysInput.toMutableSet()
                if (currentDays.contains(action.day)) {
                    // Always keep at least one day selected
                    if (currentDays.size > 1) {
                        currentDays.remove(action.day)
                    }
                } else {
                    currentDays.add(action.day)
                }
                updateState { it.copy(customDaysInput = currentDays) }
            }
            is TimetableAction.SaveSessionClick -> {
                saveSession()
            }
            is TimetableAction.DismissError -> {
                updateState { it.copy(errorMessage = null) }
            }
            is TimetableAction.LogFailedPuzzle -> {
                viewModelScope.launch {
                    securityRepository.logSecurityEvent(
                        eventType = "PUZZLE_ATTEMPT",
                        details = action.details,
                        severity = "WARNING"
                    )
                }
            }
            is TimetableAction.LogSecurityEvent -> {
                viewModelScope.launch {
                    securityRepository.logSecurityEvent(
                        eventType = action.eventType,
                        details = action.details,
                        severity = action.severity
                    )
                }
            }
        }
    }

    private fun saveSession() {
        val subject = currentState.subjectNameInput.trim()
        if (subject.isEmpty()) {
            updateState { it.copy(errorMessage = "Please enter a subject name.") }
            return
        }

        val startMin = StudySession.parseTimeToMinutes(currentState.startTimeInput)
        val endMin = StudySession.parseTimeToMinutes(currentState.endTimeInput)
        if (startMin >= endMin) {
            updateState { it.copy(errorMessage = "End time must be after start time.") }
            return
        }

        val daysString = when (currentState.repeatTypeInput) {
            "Daily" -> (1..7).joinToString(",")
            "Weekly", "None" -> {
                // If it's a non-repeating or weekly session, it operates on a single selected day
                val singleDay = currentState.customDaysInput.firstOrNull() ?: 1
                singleDay.toString()
            }
            "Custom" -> {
                if (currentState.customDaysInput.isEmpty()) {
                    updateState { it.copy(errorMessage = "Please select at least one day of the week.") }
                    return
                }
                currentState.customDaysInput.sorted().joinToString(",")
            }
            else -> "1"
        }

        val sessionToSave = StudySession(
            id = currentState.selectedSessionId ?: 0,
            subjectName = subject,
            startTime = currentState.startTimeInput,
            endTime = currentState.endTimeInput,
            repeatType = currentState.repeatTypeInput,
            repeatDays = daysString
        )

        updateState { it.copy(isSaving = true, errorMessage = null) }

        viewModelScope.launch {
            repository.saveStudySession(sessionToSave)
                .onSuccess {
                    val changeType = if (currentState.selectedSessionId == null) "Created" else "Modified"
                    securityRepository.logSecurityEvent(
                        eventType = "TIMETABLE_CHANGED",
                        details = "$changeType study class schedule: '$subject' (${currentState.startTimeInput} - ${currentState.endTimeInput}).",
                        severity = "INFO"
                    )
                    updateState { it.copy(isSaving = false, isEditingSession = false) }
                    sendEvent(TimetableEvent.ToastMessage("Successfully scheduled: $subject"))
                    sendEvent(TimetableEvent.SaveSuccess)
                }
                .onFailure { error ->
                    updateState { it.copy(isSaving = false, errorMessage = error.message) }
                }
        }
    }
}
