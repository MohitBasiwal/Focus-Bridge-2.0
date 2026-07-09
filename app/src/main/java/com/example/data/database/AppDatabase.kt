package com.example.data.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.data.database.dao.FocusDao
import com.example.data.database.entity.FocusSessionEntity

/**
 * Main application Room database.
 */
@Database(
    entities = [
        FocusSessionEntity::class,
        com.example.data.database.entity.StudySessionEntity::class,
        com.example.data.database.entity.AllowedAppEntity::class,
        com.example.data.database.entity.BlockedWebsiteEntity::class,
        com.example.data.database.entity.EducationalParagraphEntity::class,
        com.example.data.database.entity.SpeechConfigEntity::class,
        com.example.data.database.entity.SecurityEventEntity::class,
        com.example.data.database.entity.SecurityConfigEntity::class,
        com.example.data.database.entity.BlockedDistractionEntity::class,
        com.example.data.database.entity.MissedSessionEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun focusDao(): FocusDao
    abstract fun studySessionDao(): com.example.data.database.dao.StudySessionDao
    abstract fun blockerDao(): com.example.data.database.dao.BlockerDao
    abstract fun speechDao(): com.example.data.database.dao.SpeechDao
    abstract fun securityDao(): com.example.data.database.dao.SecurityDao
    abstract fun analyticsDao(): com.example.data.database.dao.AnalyticsDao
}
