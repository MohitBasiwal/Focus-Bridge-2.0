package com.example.data.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Room Entity representing a persistent study session item in the SQLite database.
 */
@Entity(tableName = "study_sessions")
data class StudySessionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val subjectName: String,
    val startTime: String,
    val endTime: String,
    val repeatType: String,
    val repeatDays: String
)
