package com.example.data.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Room Entity representing an app package that is explicitly ALLOWED during a study session.
 * All other non-system / non-essential apps will be blocked during an active study session.
 */
@Entity(tableName = "allowed_apps")
data class AllowedAppEntity(
    @PrimaryKey
    val packageName: String,
    val appName: String
)
