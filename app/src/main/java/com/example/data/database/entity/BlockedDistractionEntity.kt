package com.example.data.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Room Entity representing a blocked distraction attempt (app launch or website visit during focus mode).
 */
@Entity(tableName = "blocked_distractions")
data class BlockedDistractionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val packageNameOrDomain: String,
    val appNameOrTitle: String,
    val timestamp: Long = System.currentTimeMillis()
)
