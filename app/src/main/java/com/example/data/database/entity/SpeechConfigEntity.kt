package com.example.data.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Room Entity representing Speech configuration and active unlock states.
 * Uses a single row structure with PrimaryKey id = 1.
 */
@Entity(tableName = "speech_configs")
data class SpeechConfigEntity(
    @PrimaryKey
    val id: Int = 1,
    val selectedCategories: String, // Comma-separated categories
    val currentParagraphId: Int? = null, // Current active challenge paragraph ID
    val bypassUntilMs: Long = 0L // End time of temporary unlock bypass (0 if not active)
)
