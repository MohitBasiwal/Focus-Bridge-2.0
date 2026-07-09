package com.example.ui.base

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

/**
 * Base ViewModel supporting Uni-directional Data Flow (UDF) principles.
 * Manages reactive UI state, action processing, and single-shot events.
 *
 * @param State The UI State data class.
 * @param Action Interactivity actions (or intents) sent by the UI.
 * @param Event Transient one-shot actions (like showing a Toast, navigating, or playing sound).
 */
abstract class BaseViewModel<State, Action, Event>(
    initialState: State
) : ViewModel() {

    // Immutable state exposed to Compose
    private val _uiState = MutableStateFlow(initialState)
    val uiState: StateFlow<State> = _uiState.asStateFlow()

    // Transient event flow for one-shot actions
    private val _uiEvent = MutableSharedFlow<Event>()
    val uiEvent: SharedFlow<Event> = _uiEvent.asSharedFlow()

    // Current state getter helper
    protected val currentState: State
        get() = _uiState.value

    /**
     * Entry point for sending UI interactions to the ViewModel.
     */
    abstract fun onAction(action: Action)

    /**
     * Updates the UI state atomically in a thread-safe manner.
     */
    protected fun updateState(update: (State) -> State) {
        _uiState.update(update)
    }

    /**
     * Dispatches a single-shot event to the UI (e.g. snackbar or navigation).
     */
    protected fun sendEvent(event: Event) {
        viewModelScope.launch {
            _uiEvent.emit(event)
        }
    }
}
