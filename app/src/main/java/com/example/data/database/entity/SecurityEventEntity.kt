package com.example.data.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Room Entity representing a security event log.
 */
@Entity(tableName = "security_events")
data class SecurityEventEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val timestamp: Long = System.currentTimeMillis(),
    val eventType: String, // e.g. PERMISSION_CHANGED, BOOT_COMPLETED, PUZZLE_ATTEMPT, PROTECTION_PAUSED, PROTECTION_RESUMED
    val details: String,
    val severity: String // INFO, WARNING, CRITICAL
)
