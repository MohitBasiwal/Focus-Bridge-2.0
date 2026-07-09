package com.example.ui.screens

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.domain.model.UserPreferences
import com.example.domain.repository.BackupRepository
import com.example.domain.repository.FocusRepository
import com.example.service.NotificationHelper
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.io.BufferedReader
import java.io.InputStreamReader
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val focusRepository: FocusRepository,
    private val backupRepository: BackupRepository,
    private val notificationHelper: NotificationHelper,
    @ApplicationContext private val context: Context
) : ViewModel() {

    val userPreferences: StateFlow<UserPreferences> = focusRepository.getUserPreferences()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = UserPreferences()
        )

    private val _uiEvent = MutableSharedFlow<SettingsUiEvent>()
    val uiEvent: SharedFlow<SettingsUiEvent> = _uiEvent

    fun updateDarkMode(enabled: Boolean) {
        viewModelScope.launch {
            focusRepository.updateDarkModeEnabled(enabled)
        }
    }

    fun updateDynamicColor(enabled: Boolean) {
        viewModelScope.launch {
            focusRepository.updateDynamicColorEnabled(enabled)
        }
    }

    fun updateSelectedTheme(theme: String) {
        viewModelScope.launch {
            focusRepository.updateSelectedTheme(theme)
        }
    }

    fun updateParagraphCategory(category: String) {
        viewModelScope.launch {
            focusRepository.updateParagraphCategory(category)
        }
    }

    fun updateSpeechDuration(seconds: Int) {
        viewModelScope.launch {
            focusRepository.updateSpeechDurationSeconds(seconds)
        }
    }

    fun updateReminders(enabled: Boolean) {
        viewModelScope.launch {
            focusRepository.updateRemindersEnabled(enabled)
        }
    }

    fun updateSessionStartEndNotif(enabled: Boolean) {
        viewModelScope.launch {
            focusRepository.updateSessionStartEndNotifEnabled(enabled)
        }
    }

    fun updateMissedReminders(enabled: Boolean) {
        viewModelScope.launch {
            focusRepository.updateMissedRemindersEnabled(enabled)
        }
    }

    fun updateSummaries(enabled: Boolean) {
        viewModelScope.launch {
            focusRepository.updateSummariesEnabled(enabled)
        }
    }

    fun exportBackup(onShare: (String) -> Unit) {
        viewModelScope.launch {
            try {
                val json = backupRepository.exportBackupJson()
                onShare(json)
                _uiEvent.emit(SettingsUiEvent.ShowToast("Backup exported successfully!"))
            } catch (e: Exception) {
                _uiEvent.emit(SettingsUiEvent.ShowToast("Export failed: ${e.message}"))
            }
        }
    }

    fun importBackup(uri: Uri) {
        viewModelScope.launch {
            try {
                val stringBuilder = StringBuilder()
                context.contentResolver.openInputStream(uri)?.use { inputStream ->
                    BufferedReader(InputStreamReader(inputStream)).use { reader ->
                        var line: String? = reader.readLine()
                        while (line != null) {
                            stringBuilder.append(line)
                            line = reader.readLine()
                        }
                    }
                }
                val json = stringBuilder.toString()
                val result = backupRepository.importBackupJson(json)
                if (result.isSuccess) {
                    _uiEvent.emit(SettingsUiEvent.ShowToast("Backup restored successfully! App will now refresh settings."))
                } else {
                    _uiEvent.emit(SettingsUiEvent.ShowToast("Restore failed: ${result.exceptionOrNull()?.message}"))
                }
            } catch (e: Exception) {
                _uiEvent.emit(SettingsUiEvent.ShowToast("Import failed: ${e.message}"))
            }
        }
    }

    // --- Interactive Notification Testing ---
    fun testPreSessionReminder() {
        viewModelScope.launch {
            notificationHelper.showPreSessionReminder("Mathematics Advanced", 10)
        }
    }

    fun testSessionStart() {
        viewModelScope.launch {
            notificationHelper.showSessionStarted("Advanced Chemistry")
        }
    }

    fun testSessionEnd(success: Boolean) {
        viewModelScope.launch {
            notificationHelper.showSessionEnded("Advanced Chemistry", success)
        }
    }

    fun testMissedSession() {
        viewModelScope.launch {
            notificationHelper.showMissedSessionReminder("Physics Lab")
        }
    }

    fun testDailySummary() {
        viewModelScope.launch {
            notificationHelper.showDailySummary(3, 115)
        }
    }

    fun testWeeklySummary() {
        viewModelScope.launch {
            notificationHelper.showWeeklySummary(14, 450, 5)
        }
    }
}

sealed interface SettingsUiEvent {
    data class ShowToast(val message: String) : SettingsUiEvent
}
