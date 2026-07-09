package com.example.ui.screens

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import androidx.lifecycle.viewModelScope
import com.example.data.database.entity.EducationalParagraphEntity
import com.example.data.database.entity.SpeechConfigEntity
import com.example.domain.repository.SpeechRepository
import com.example.ui.base.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.collectLatest
import java.util.Locale
import javax.inject.Inject

data class SpeechState(
    val categories: List<CategoryOption> = emptyList(),
    val currentParagraph: EducationalParagraphEntity? = null,
    val paragraphWords: List<String> = emptyList(),
    val spokenWordCount: Int = 0,
    val silenceRemainingSeconds: Int = 5,
    val bypassRemainingMs: Long = 0L,
    val isBypassActive: Boolean = false,
    val isListening: Boolean = false,
    val isPermissionGranted: Boolean = false,
    val speechError: String? = null,
    val infoMessage: String? = null
)

data class CategoryOption(
    val name: String,
    val isSelected: Boolean
)

sealed interface SpeechAction {
    data class PermissionStateChanged(val isGranted: Boolean) : SpeechAction
    data class ToggleCategory(val category: String) : SpeechAction
    object StartChallenge : SpeechAction
    object RestartChallenge : SpeechAction
    object StopChallenge : SpeechAction
    object RefreshBypass : SpeechAction
    object ClearBypassNow : SpeechAction
    object DismissError : SpeechAction
}

sealed interface SpeechEvent {
    data class ToastMessage(val message: String) : SpeechEvent
}

@HiltViewModel
class SpeechViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val speechRepository: SpeechRepository
) : BaseViewModel<SpeechState, SpeechAction, SpeechEvent>(SpeechState()) {

    private var speechRecognizer: SpeechRecognizer? = null
    private var silenceJob: Job? = null
    private var bypassJob: Job? = null
    
    private val availableCategories = listOf(
        "Science", "Mathematics", "History", "Geography", "Technology",
        "Biology", "Physics", "Chemistry", "General Knowledge", "English Vocabulary"
    )

    init {
        // Run database seed check and load config
        viewModelScope.launch {
            speechRepository.seedParagraphsIfNeeded()
            observeConfig()
        }
        startBypassCountdownTicker()
    }

    private fun observeConfig() {
        viewModelScope.launch {
            speechRepository.getSpeechConfigFlow().collectLatest { config ->
                if (config == null) {
                    // Initialize default
                    speechRepository.getSpeechConfigSnapshot()
                    return@collectLatest
                }

                val activeCategories = if (config.selectedCategories.isBlank()) {
                    emptySet()
                } else {
                    config.selectedCategories.split(",").map { it.trim() }.toSet()
                }

                val options = availableCategories.map { cat ->
                    CategoryOption(cat, activeCategories.contains(cat))
                }

                val paragraph = speechRepository.getCurrentParagraph()
                val words = paragraph?.text?.split(Regex("\\s+")) ?: emptyList()

                val now = System.currentTimeMillis()
                val isBypassOn = now < config.bypassUntilMs
                val remMs = if (isBypassOn) config.bypassUntilMs - now else 0L

                updateState {
                    it.copy(
                        categories = options,
                        currentParagraph = paragraph,
                        paragraphWords = words,
                        isBypassActive = isBypassOn,
                        bypassRemainingMs = remMs
                    )
                }
            }
        }
    }

    private fun startBypassCountdownTicker() {
        bypassJob?.cancel()
        bypassJob = viewModelScope.launch(Dispatchers.Main) {
            while (isActive) {
                val config = speechRepository.getSpeechConfigSnapshot()
                if (config != null) {
                    val now = System.currentTimeMillis()
                    val isBypassOn = now < config.bypassUntilMs
                    val remMs = if (isBypassOn) config.bypassUntilMs - now else 0L
                    
                    updateState {
                        it.copy(
                            isBypassActive = isBypassOn,
                            bypassRemainingMs = remMs
                        )
                    }
                }
                delay(1000)
            }
        }
    }

    override fun onAction(action: SpeechAction) {
        when (action) {
            is SpeechAction.PermissionStateChanged -> {
                updateState { it.copy(isPermissionGranted = action.isGranted) }
                if (action.isGranted) {
                    // Automatically start listening when the screen opens & permission is granted
                    startSpeechRecognition()
                }
            }
            is SpeechAction.ToggleCategory -> {
                viewModelScope.launch {
                    val currentCategories = currentState.categories
                        .filter { if (it.name == action.category) !it.isSelected else it.isSelected }
                        .map { it.name }
                    
                    speechRepository.saveSelectedCategories(currentCategories)
                    sendEvent(SpeechEvent.ToastMessage("Categories updated"))
                }
            }
            is SpeechAction.StartChallenge -> {
                startSpeechRecognition()
            }
            is SpeechAction.RestartChallenge -> {
                viewModelScope.launch {
                    speechRepository.resetCurrentParagraph()
                    speechRepository.getCurrentParagraph() // Fetch new matching categories paragraph
                    resetChallengeState("Challenge restarted manually")
                    startSpeechRecognition()
                }
            }
            is SpeechAction.StopChallenge -> {
                stopSpeechRecognition()
            }
            is SpeechAction.RefreshBypass -> {
                viewModelScope.launch {
                    observeConfig()
                }
            }
            is SpeechAction.ClearBypassNow -> {
                viewModelScope.launch {
                    speechRepository.clearBypass()
                    sendEvent(SpeechEvent.ToastMessage("Blocking rules re-enabled!"))
                }
            }
            is SpeechAction.DismissError -> {
                updateState { it.copy(speechError = null) }
            }
        }
    }

    private fun startSpeechRecognition() {
        if (!currentState.isPermissionGranted) {
            updateState { it.copy(speechError = "Microphone permission is required for speech recognition.") }
            return
        }

        // Must run on Main Thread
        viewModelScope.launch(Dispatchers.Main) {
            try {
                if (speechRecognizer == null) {
                    speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context).apply {
                        setRecognitionListener(SpeechListener())
                    }
                }

                val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                    putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                    putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
                    putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
                }

                speechRecognizer?.startListening(intent)
                updateState { it.copy(isListening = true, speechError = null) }
                startSilenceTimer()
            } catch (e: Exception) {
                updateState { it.copy(speechError = "Failed to initialize SpeechRecognizer: ${e.localizedMessage}") }
            }
        }
    }

    private fun stopSpeechRecognition() {
        viewModelScope.launch(Dispatchers.Main) {
            speechRecognizer?.stopListening()
            speechRecognizer?.destroy()
            speechRecognizer = null
            silenceJob?.cancel()
            updateState { it.copy(isListening = false) }
        }
    }

    private fun startSilenceTimer() {
        silenceJob?.cancel()
        updateState { it.copy(silenceRemainingSeconds = 5) }
        silenceJob = viewModelScope.launch(Dispatchers.Main) {
            while (isActive) {
                delay(1000)
                val remaining = currentState.silenceRemainingSeconds - 1
                if (remaining <= 0) {
                    resetChallengeState("Silence limit reached! Restarting.")
                    sendEvent(SpeechEvent.ToastMessage("Restarted due to 5 seconds of silence"))
                    startSpeechRecognition()
                    break
                } else {
                    updateState { it.copy(silenceRemainingSeconds = remaining) }
                }
            }
        }
    }

    private fun resetChallengeState(reason: String) {
        updateState {
            it.copy(
                spokenWordCount = 0,
                silenceRemainingSeconds = 5,
                infoMessage = reason
            )
        }
    }

    private fun handleSpokenPhrases(phrases: List<String>) {
        val words = currentState.paragraphWords
        if (words.isEmpty()) return

        // Clean targets
        val targetWordsCleaned = words.map { it.lowercase().replace(Regex("[^a-z]"), "") }

        // Find best spoken matches
        for (phrase in phrases) {
            val spokenWords = phrase.lowercase()
                .split(Regex("\\s+"))
                .map { it.replace(Regex("[^a-z]"), "") }
                .filter { it.isNotBlank() }

            var matchIndex = currentState.spokenWordCount
            var matchedNew = false

            for (spokenWord in spokenWords) {
                if (matchIndex < targetWordsCleaned.size) {
                    val target = targetWordsCleaned[matchIndex]
                    if (spokenWord == target) {
                        matchIndex++
                        matchedNew = true
                    } else {
                        // If it is a completely wrong word (and not an incomplete prefix of current target),
                        // restart from the beginning.
                        if (!target.startsWith(spokenWord) && spokenWord.length >= 3) {
                            resetChallengeState("Mismatch: Spoke '$spokenWord', expected '$target'")
                            return
                        }
                    }
                }
            }

            if (matchedNew) {
                // Progress matched! Update states and reset silence countdown
                updateState { it.copy(spokenWordCount = matchIndex, infoMessage = "Excellent! Continue reading.") }
                startSilenceTimer()

                // Check for complete paragraph success
                if (matchIndex >= targetWordsCleaned.size) {
                    onChallengeSuccess()
                }
                break
            }
        }
    }

    private fun onChallengeSuccess() {
        stopSpeechRecognition()
        viewModelScope.launch {
            speechRepository.setTemporaryUnlockBypass(5)
            sendEvent(SpeechEvent.ToastMessage("Challenge Completed! Bypassing all block rules for 5 minutes."))
        }
    }

    override fun onCleared() {
        super.onCleared()
        speechRecognizer?.destroy()
        silenceJob?.cancel()
        bypassJob?.cancel()
    }

    inner class SpeechListener : RecognitionListener {
        override fun onReadyForSpeech(params: Bundle?) {}
        override fun onBeginningOfSpeech() {}
        override fun onRmsChanged(rmsdB: Float) {}
        override fun onBufferReceived(buffer: ByteArray?) {}
        override fun onEndOfSpeech() {}
        
        override fun onError(error: Int) {
            val message = when (error) {
                SpeechRecognizer.ERROR_AUDIO -> "Audio recording error"
                SpeechRecognizer.ERROR_CLIENT -> "Client side error"
                SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> "Insufficient permissions"
                SpeechRecognizer.ERROR_NETWORK -> "Network error"
                SpeechRecognizer.ERROR_NETWORK_TIMEOUT -> "Network timeout"
                SpeechRecognizer.ERROR_NO_MATCH -> "No match found"
                SpeechRecognizer.ERROR_RECOGNIZER_BUSY -> "Speech service is busy"
                SpeechRecognizer.ERROR_SERVER -> "Server error"
                SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> "No speech input"
                else -> "Speech recognizer error"
            }
            
            // On NO_MATCH or TIMEOUT, we restart the recognizer gracefully
            if (error == SpeechRecognizer.ERROR_NO_MATCH || error == SpeechRecognizer.ERROR_SPEECH_TIMEOUT) {
                startSpeechRecognition()
            } else {
                updateState { it.copy(isListening = false, speechError = message) }
            }
        }

        override fun onResults(results: Bundle?) {
            val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
            if (!matches.isNullOrEmpty()) {
                handleSpokenPhrases(matches)
            }
            // Auto restart listening if not successful yet
            if (currentState.spokenWordCount < currentState.paragraphWords.size && !currentState.isBypassActive) {
                startSpeechRecognition()
            }
        }

        override fun onPartialResults(partialResults: Bundle?) {
            val matches = partialResults?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
            if (!matches.isNullOrEmpty()) {
                handleSpokenPhrases(matches)
            }
        }

        override fun onEvent(eventType: Int, params: Bundle?) {}
    }
}
