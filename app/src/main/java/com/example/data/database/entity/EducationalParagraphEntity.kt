package com.example.data.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Room Entity representing educational paragraphs stored offline for Speech Unlock challenges.
 */
@Entity(tableName = "educational_paragraphs")
data class EducationalParagraphEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val category: String,
    val text: String
)
