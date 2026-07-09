package com.example.data.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Room Entity representing website blocking/allowing rules.
 */
@Entity(tableName = "blocked_websites")
data class BlockedWebsiteEntity(
    @PrimaryKey
    val domain: String,
    val isBlocked: Boolean // true for blocklist, false for whitelist
)
