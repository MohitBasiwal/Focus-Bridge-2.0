package com.example.domain.repository

import com.example.data.database.entity.EducationalParagraphEntity
import com.example.data.database.entity.SpeechConfigEntity
import kotlinx.coroutines.flow.Flow

/**
 * Repository interface for educational speech challenge paragraphs, categories configuration, and temporary unlock bypasses.
 */
interface SpeechRepository {
    /**
     * Observe the speech configurations and active bypass times reactively.
     */
    fun getSpeechConfigFlow(): Flow<SpeechConfigEntity?>

    /**
     * Retrieve the current configuration snapshot directly.
     */
    suspend fun getSpeechConfigSnapshot(): SpeechConfigEntity?

    /**
     * Updates selected learning categories for the Speech Challenge setup.
     */
    suspend fun saveSelectedCategories(categories: List<String>)

    /**
     * Selects and locks a random paragraph of the chosen categories, with the requirement:
     * "Never change the paragraph until the current challenge is completed or restarted."
     */
    suspend fun selectAndLockRandomParagraph(): EducationalParagraphEntity?

    /**
     * Retrieve the currently locked challenge paragraph.
     */
    suspend fun getCurrentParagraph(): EducationalParagraphEntity?

    /**
     * Activates 5-minute temporary bypass unlocking for all blocked apps and websites.
     */
    suspend fun setTemporaryUnlockBypass(durationMinutes: Int = 5)

    /**
     * Clears any active bypass.
     */
    suspend fun clearBypass()

    /**
     * Seed educational paragraphs offline if they don't already exist.
     */
    suspend fun seedParagraphsIfNeeded()

    /**
     * Resets the locked paragraph so a new one can be chosen on next attempt.
     */
    suspend fun resetCurrentParagraph()
}
