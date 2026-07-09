package com.example.ui.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.domain.model.FocusSession
import com.example.domain.repository.FocusRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TimerViewModel @Inject constructor(
    private val focusRepository: FocusRepository
) : ViewModel() {

    fun completeSession(minutes: Int, category: String) {
        viewModelScope.launch {
            focusRepository.insertSession(
                FocusSession(
                    durationMinutes = minutes,
                    category = category,
                    timestamp = System.currentTimeMillis(),
                    success = true
                )
            )
        }
    }
}
