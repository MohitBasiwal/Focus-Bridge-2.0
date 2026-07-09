package com.example.ui.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.database.entity.SecurityConfigEntity
import com.example.data.database.entity.SecurityEventEntity
import com.example.domain.repository.BlockerRepository
import com.example.domain.repository.SecurityRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SecurityCenterUiState(
    val isProtectionActive: Boolean = false,
    val permissions: Map<String, Boolean> = emptyMap(),
    val isProtectionPaused: Boolean = false,
    val lastEvent: SecurityEventEntity? = null,
    val recentEvents: List<SecurityEventEntity> = emptyList(),
    val isBlockingActiveNow: Boolean = false,
    val puzzleDifficulty: String = "MEDIUM",
    val puzzleType: String = "MATH"
)

@HiltViewModel
class SecurityCenterViewModel @Inject constructor(
    private val securityRepository: SecurityRepository,
    private val blockerRepository: BlockerRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(SecurityCenterUiState())
    val uiState: StateFlow<SecurityCenterUiState> = _uiState.asStateFlow()

    private var monitoringJob: Job? = null
    private var lastPermissionSnapshot: Map<String, Boolean>? = null

    init {
        // Observe Security Configuration Flow
        viewModelScope.launch {
            securityRepository.getSecurityConfigFlow().collect { config ->
                val currentConfig = config ?: SecurityConfigEntity()
                _uiState.update {
                    it.copy(
                        isProtectionPaused = currentConfig.isProtectionPaused,
                        puzzleDifficulty = currentConfig.puzzleDifficulty,
                        puzzleType = currentConfig.puzzleType
                    )
                }
            }
        }

        // Observe Security Events Log Flow
        viewModelScope.launch {
            securityRepository.getAllSecurityEventsFlow().collect { events ->
                _uiState.update {
                    it.copy(
                        recentEvents = events,
                        lastEvent = events.firstOrNull()
                    )
                }
            }
        }

        // Start active system permission monitoring loop
        startPermissionMonitoring()
    }

    private fun startPermissionMonitoring() {
        monitoringJob?.cancel()
        monitoringJob = viewModelScope.launch {
            while (true) {
                checkAndReportPermissions()
                delay(2000) // Poll every 2 seconds
            }
        }
    }

    private suspend fun checkAndReportPermissions() {
        val currentPermissions = securityRepository.checkPermissionsStatus()
        val isHealthy = securityRepository.isProtectionActive()
        val isBlockingActiveNow = blockerRepository.isStudySessionActiveNow()

        // 1. Detect Permission state changes
        val previous = lastPermissionSnapshot
        if (previous != null) {
            currentPermissions.forEach { (permName, isGranted) ->
                val wasGranted = previous[permName] ?: false
                if (wasGranted != isGranted) {
                    val statusStr = if (isGranted) "GRANTED" else "REVOKED"
                    securityRepository.logSecurityEvent(
                        eventType = "PERMISSION_CHANGED",
                        details = "Permission '$permName' was $statusStr by the user or system.",
                        severity = if (isGranted) "INFO" else "CRITICAL"
                    )

                    // If a core permission goes missing, log that protection is paused
                    if (!isGranted && (permName == "Accessibility Service" || permName == "Usage Access" || permName == "Draw Overlays")) {
                        securityRepository.logSecurityEvent(
                            eventType = "PROTECTION_PAUSED",
                            details = "Focus Bridge protection was auto-paused due to missing required permission: $permName.",
                            severity = "CRITICAL"
                        )
                        // Sync manual pause too
                        val config = securityRepository.getSecurityConfigSnapshot() ?: SecurityConfigEntity()
                        securityRepository.saveSecurityConfig(config.copy(isProtectionPaused = true))
                    }
                }
            }
        } else {
            // First run: log permissions if we have none granted and it's a fresh app start
            if (!isHealthy) {
                val missing = currentPermissions.filter { !it.value }.keys.joinToString(", ")
                securityRepository.logSecurityEvent(
                    eventType = "PROTECTION_PAUSED",
                    details = "Protection is inactive. Missing required settings: $missing.",
                    severity = "WARNING"
                )
            }
        }

        lastPermissionSnapshot = currentPermissions

        _uiState.update {
            it.copy(
                isProtectionActive = isHealthy && !it.isProtectionPaused,
                permissions = currentPermissions,
                isBlockingActiveNow = isBlockingActiveNow
            )
        }
    }

    fun toggleProtectionPause(onVerified: () -> Unit) {
        viewModelScope.launch {
            val config = securityRepository.getSecurityConfigSnapshot() ?: SecurityConfigEntity()
            val newPauseState = !config.isProtectionPaused
            securityRepository.saveSecurityConfig(config.copy(isProtectionPaused = newPauseState))
            
            val eventType = if (newPauseState) "PROTECTION_PAUSED" else "PROTECTION_RESUMED"
            val details = if (newPauseState) "Focus Bridge protection was manually paused by the user." else "Focus Bridge protection was manually resumed by the user."
            securityRepository.logSecurityEvent(
                eventType = eventType,
                details = details,
                severity = "INFO"
            )
            checkAndReportPermissions()
            onVerified()
        }
    }

    fun setPuzzleDifficulty(difficulty: String) {
        viewModelScope.launch {
            val config = securityRepository.getSecurityConfigSnapshot() ?: SecurityConfigEntity()
            securityRepository.saveSecurityConfig(config.copy(puzzleDifficulty = difficulty))
            securityRepository.logSecurityEvent(
                eventType = "CONFIG_CHANGED",
                details = "Puzzle difficulty level set to $difficulty.",
                severity = "INFO"
            )
        }
    }

    fun resetApp(onComplete: () -> Unit) {
        viewModelScope.launch {
            securityRepository.resetAppLocalData()
            onComplete()
        }
    }

    fun logFailedPuzzleAttempt(details: String) {
        viewModelScope.launch {
            securityRepository.logSecurityEvent(
                eventType = "PUZZLE_ATTEMPT",
                details = details,
                severity = "WARNING"
            )
        }
    }

    fun clearLogs() {
        viewModelScope.launch {
            securityRepository.clearSecurityEvents()
            securityRepository.logSecurityEvent(
                eventType = "LOGS_CLEARED",
                details = "Security logs successfully cleared by administrative access.",
                severity = "INFO"
            )
        }
    }

    override fun onCleared() {
        super.onCleared()
        monitoringJob?.cancel()
    }
}
