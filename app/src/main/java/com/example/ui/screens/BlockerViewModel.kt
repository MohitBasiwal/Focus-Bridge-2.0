package com.example.ui.screens

import androidx.lifecycle.viewModelScope
import com.example.data.database.entity.AllowedAppEntity
import com.example.data.database.entity.BlockedWebsiteEntity
import com.example.domain.repository.BlockerRepository
import com.example.ui.base.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

// UI State representing current configuration choices for Blocker Screen
data class BlockerState(
    val allowedApps: List<AllowedAppEntity> = emptyList(),
    val blockedWebsites: List<BlockedWebsiteEntity> = emptyList(),
    
    // Manual Package Input fields
    val customAppNameInput: String = "",
    val customPackageInput: String = "",
    
    // Website Domain Input field
    val websiteDomainInput: String = "",
    val websiteIsBlockedType: Boolean = true, // Default to blacklist rule (blocked)
    
    val errorMessage: String? = null,
    val infoMessage: String? = null,
    val puzzleDifficulty: String = "MEDIUM"
)

// Actions flowing from Composable to the ViewModel
sealed interface BlockerAction {
    data class TogglePredefinedApp(val appName: String, val packageName: String) : BlockerAction
    data class AddCustomApp(val appName: String, val packageName: String) : BlockerAction
    data class RemoveAllowedApp(val app: AllowedAppEntity) : BlockerAction
    
    data class WebsiteDomainInputChanged(val value: String) : BlockerAction
    data class WebsiteIsBlockedTypeChanged(val isBlocked: Boolean) : BlockerAction
    object AddBlockedWebsite : BlockerAction
    data class RemoveBlockedWebsite(val website: BlockedWebsiteEntity) : BlockerAction
    
    data class CustomAppNameInputChanged(val value: String) : BlockerAction
    data class CustomPackageInputChanged(val value: String) : BlockerAction
    
    object DismissError : BlockerAction
    
    // Security and Puzzle actions
    data class LogFailedPuzzle(val details: String) : BlockerAction
    data class LogSecurityEvent(val eventType: String, val details: String, val severity: String) : BlockerAction
}

// Single-shot events
sealed interface BlockerEvent {
    data class ToastMessage(val message: String) : BlockerEvent
}

@HiltViewModel
class BlockerViewModel @Inject constructor(
    private val repository: BlockerRepository,
    private val securityRepository: com.example.domain.repository.SecurityRepository
) : BaseViewModel<BlockerState, BlockerAction, BlockerEvent>(BlockerState()) {

    // Curated pre-defined list of popular apps representing study utilities
    val studyAppsPreset = listOf(
        AppPreset("Focus Bridge", "com.example"),
        AppPreset("Chrome Browser", "com.android.chrome"),
        AppPreset("Duolingo", "com.duolingo"),
        AppPreset("Notion", "notion.id"),
        AppPreset("Zoom", "us.zoom.videomeetings"),
        AppPreset("Spotify", "com.spotify.music"),
        AppPreset("Calculator", "com.google.android.calculator"),
        AppPreset("Google Calendar", "com.google.android.calendar")
    )

    init {
        loadAllowedApps()
        loadBlockedWebsites()
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

    private fun loadAllowedApps() {
        viewModelScope.launch {
            repository.getAllAllowedApps().collectLatest { list ->
                updateState { it.copy(allowedApps = list) }
            }
        }
    }

    private fun loadBlockedWebsites() {
        viewModelScope.launch {
            repository.getAllBlockedWebsites().collectLatest { list ->
                updateState { it.copy(blockedWebsites = list) }
            }
        }
    }

    override fun onAction(action: BlockerAction) {
        when (action) {
            is BlockerAction.TogglePredefinedApp -> {
                viewModelScope.launch {
                    val alreadyAllowed = currentState.allowedApps.any { it.packageName == action.packageName }
                    if (alreadyAllowed) {
                        repository.removeAllowedApp(action.packageName)
                        securityRepository.logSecurityEvent(
                            eventType = "BLOCKER_CHANGED",
                            details = "Removed app '${action.appName}' from the allowed study list (re-enabled app block).",
                            severity = "INFO"
                        )
                        sendEvent(BlockerEvent.ToastMessage("Removed allowed app: ${action.appName}"))
                    } else {
                        repository.addAllowedApp(action.packageName, action.appName)
                        securityRepository.logSecurityEvent(
                            eventType = "BLOCKER_CHANGED",
                            details = "Added app '${action.appName}' to the allowed study list (app whitelisted).",
                            severity = "WARNING"
                        )
                        sendEvent(BlockerEvent.ToastMessage("Allowed app: ${action.appName}"))
                    }
                }
            }
            is BlockerAction.AddCustomApp -> {
                if (action.appName.isBlank() || action.packageName.isBlank()) {
                    updateState { it.copy(errorMessage = "Please enter both app name and package name.") }
                    return
                }
                viewModelScope.launch {
                    repository.addAllowedApp(action.packageName.trim(), action.appName.trim())
                    securityRepository.logSecurityEvent(
                        eventType = "BLOCKER_CHANGED",
                        details = "Added custom allowed app '${action.appName}' (${action.packageName}).",
                        severity = "WARNING"
                    )
                    updateState { it.copy(customAppNameInput = "", customPackageInput = "") }
                    sendEvent(BlockerEvent.ToastMessage("Custom app allowed: ${action.appName}"))
                }
            }
            is BlockerAction.RemoveAllowedApp -> {
                viewModelScope.launch {
                    repository.removeAllowedApp(action.app.packageName)
                    securityRepository.logSecurityEvent(
                        eventType = "BLOCKER_CHANGED",
                        details = "Removed custom allowed app '${action.app.appName}' (re-enabled app block).",
                        severity = "INFO"
                    )
                    sendEvent(BlockerEvent.ToastMessage("Removed allowed app: ${action.app.appName}"))
                }
            }
            is BlockerAction.WebsiteDomainInputChanged -> {
                updateState { it.copy(websiteDomainInput = action.value) }
            }
            is BlockerAction.WebsiteIsBlockedTypeChanged -> {
                updateState { it.copy(websiteIsBlockedType = action.isBlocked) }
            }
            is BlockerAction.AddBlockedWebsite -> {
                val domain = currentState.websiteDomainInput.trim().lowercase()
                if (domain.isBlank()) {
                    updateState { it.copy(errorMessage = "Please enter a domain address.") }
                    return
                }
                viewModelScope.launch {
                    repository.addBlockedWebsite(domain, currentState.websiteIsBlockedType)
                    securityRepository.logSecurityEvent(
                        eventType = "BLOCKER_CHANGED",
                        details = "Added website block rule for '$domain' (IsBlocked: ${currentState.websiteIsBlockedType}).",
                        severity = "INFO"
                    )
                    updateState { it.copy(websiteDomainInput = "") }
                    sendEvent(BlockerEvent.ToastMessage("Website rule added for: $domain"))
                }
            }
            is BlockerAction.RemoveBlockedWebsite -> {
                viewModelScope.launch {
                    repository.removeBlockedWebsite(action.website.domain)
                    securityRepository.logSecurityEvent(
                        eventType = "BLOCKER_CHANGED",
                        details = "Disabled website block rule for '${action.website.domain}' (website rule deleted).",
                        severity = "WARNING"
                    )
                    sendEvent(BlockerEvent.ToastMessage("Removed website rule: ${action.website.domain}"))
                }
            }
            is BlockerAction.CustomAppNameInputChanged -> {
                updateState { it.copy(customAppNameInput = action.value) }
            }
            is BlockerAction.CustomPackageInputChanged -> {
                updateState { it.copy(customPackageInput = action.value) }
            }
            is BlockerAction.DismissError -> {
                updateState { it.copy(errorMessage = null) }
            }
            is BlockerAction.LogFailedPuzzle -> {
                viewModelScope.launch {
                    securityRepository.logSecurityEvent(
                        eventType = "PUZZLE_ATTEMPT",
                        details = action.details,
                        severity = "WARNING"
                    )
                }
            }
            is BlockerAction.LogSecurityEvent -> {
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
}

data class AppPreset(
    val name: String,
    val packageName: String
)
