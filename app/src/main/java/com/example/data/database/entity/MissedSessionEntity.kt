package com.example.data.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Room Entity representing a study/timetable session that was missed.
 */
@Entity(tableName = "missed_sessions")
data class MissedSessionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val subjectName: String,
    val scheduledStartTime: String,
    val scheduledEndTime: String,
    val timestamp: Long = System.currentTimeMillis()
)
