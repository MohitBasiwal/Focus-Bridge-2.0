package com.example.domain.model

/**
 * Domain-level model representing a Focus Session, completely decoupled from Room database annotations.
 */
data class FocusSession(
    val id: Long = 0,
    val durationMinutes: Int,
    val category: String,
    val timestamp: Long,
    val success: Boolean
)
