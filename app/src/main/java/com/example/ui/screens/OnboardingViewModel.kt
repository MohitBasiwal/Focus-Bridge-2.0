package com.example.ui.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.domain.model.StudySession
import com.example.domain.repository.BlockerRepository
import com.example.domain.repository.FocusRepository
import com.example.domain.repository.SpeechRepository
import com.example.domain.repository.TimetableRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class OnboardingViewModel @Inject constructor(
    private val focusRepository: FocusRepository,
    private val timetableRepository: TimetableRepository,
    private val blockerRepository: BlockerRepository,
    private val speechRepository: SpeechRepository
) : ViewModel() {

    private val _currentStep = MutableStateFlow(0)
    val currentStep: StateFlow<Int> = _currentStep.asStateFlow()

    // Timetable Setup State
    val subjectName = MutableStateFlow("General Study")
    val startTime = MutableStateFlow("09:00")
    val endTime = MutableStateFlow("10:00")
    val repeatDays = MutableStateFlow(setOf("Mon", "Wed", "Fri"))

    // Allowed Apps State (Default suggestions)
    val availableApps = listOf(
        AppPreset("com.android.chrome", "Google Chrome", true),
        AppPreset("com.google.android.apps.docs", "Google Docs", true),
        AppPreset("com.google.android.calculator", "Calculator", true),
        AppPreset("com.google.android.gm", "Gmail", false),
        AppPreset("com.slack", "Slack", false),
        AppPreset("com.spotify.music", "Spotify", false)
    )
    val allowedApps = MutableStateFlow(setOf("com.android.chrome", "com.google.android.apps.docs", "com.google.android.calculator"))

    // Website Rules State
    val availableWebsites = listOf(
        WebsitePreset("wikipedia.org", "Wikipedia (Research)", true),
        WebsitePreset("stackoverflow.com", "Stack Overflow (Coding)", true),
        WebsitePreset("youtube.com", "YouTube (Video)", false),
        WebsitePreset("facebook.com", "Facebook (Social)", false),
        WebsitePreset("instagram.com", "Instagram (Social)", false)
    )
    val blockedWebsites = MutableStateFlow(setOf("youtube.com", "facebook.com", "instagram.com"))

    // Paragraph Categories State
    val availableCategories = listOf("Motivation", "Science", "History", "Discipline")
    val selectedCategories = MutableStateFlow(setOf("Motivation", "Discipline"))

    // Security Passcode State
    val isPasscodeEnabled = MutableStateFlow(true)
    val passcode = MutableStateFlow("1234")

    fun nextStep() {
        if (_currentStep.value < 8) {
            _currentStep.value += 1
        }
    }

    fun previousStep() {
        if (_currentStep.value > 0) {
            _currentStep.value -= 1
        }
    }

    fun toggleAllowedApp(packageName: String) {
        val current = allowedApps.value.toMutableSet()
        if (current.contains(packageName)) {
            current.remove(packageName)
        } else {
            current.add(packageName)
        }
        allowedApps.value = current
    }

    fun toggleBlockedWebsite(domain: String) {
        val current = blockedWebsites.value.toMutableSet()
        if (current.contains(domain)) {
            current.remove(domain)
        } else {
            current.add(domain)
        }
        blockedWebsites.value = current
    }

    fun toggleCategory(category: String) {
        val current = selectedCategories.value.toMutableSet()
        if (current.contains(category)) {
            current.remove(category)
        } else {
            current.add(category)
        }
        selectedCategories.value = current
    }

    fun saveAndCompleteOnboarding(onSuccess: () -> Unit) {
        viewModelScope.launch {
            // 1. Save Timetable Session
            val firstSession = StudySession(
                id = 0,
                subjectName = subjectName.value,
                startTime = startTime.value,
                endTime = endTime.value,
                repeatType = "Weekly",
                repeatDays = repeatDays.value.joinToString(",")
            )
            timetableRepository.saveStudySession(firstSession)

            // 2. Save Allowed Apps
            for (app in availableApps) {
                if (allowedApps.value.contains(app.packageName)) {
                    blockerRepository.addAllowedApp(app.packageName, app.name)
                }
            }

            // 3. Save Website Rules
            for (web in availableWebsites) {
                val isBlocked = blockedWebsites.value.contains(web.domain)
                blockerRepository.addBlockedWebsite(web.domain, isBlocked)
            }

            // 4. Save Selected Speech Categories
            speechRepository.saveSelectedCategories(selectedCategories.value.toList())

            // 5. Update onboarding completed state
            focusRepository.updateIsOnboarded(true)

            onSuccess()
        }
    }
}

data class AppPreset(
    val packageName: String,
    val name: String,
    val defaultSelected: Boolean
)

data class WebsitePreset(
    val domain: String,
    val name: String,
    val defaultSelected: Boolean
)
