package com.example.data.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Room Entity representing the central Security Configuration.
 * Single row layout with PrimaryKey id = 1.
 */
@Entity(tableName = "security_configs")
data class SecurityConfigEntity(
    @PrimaryKey
    val id: Int = 1,
    val isProtectionPaused: Boolean = false,
    val puzzleDifficulty: String = "MEDIUM", // EASY, MEDIUM, HARD
    val puzzleType: String = "MATH", // MATH, SEQUENCE
    val lastCheckTimestamp: Long = System.currentTimeMillis()
)
