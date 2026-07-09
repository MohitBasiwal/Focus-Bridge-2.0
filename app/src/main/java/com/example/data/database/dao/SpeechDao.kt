package com.example.data.database.dao

import androidx.room.*
import com.example.data.database.entity.EducationalParagraphEntity
import com.example.data.database.entity.SpeechConfigEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SpeechDao {

    // --- Educational Paragraphs ---
    @Query("SELECT * FROM educational_paragraphs")
    suspend fun getAllParagraphs(): List<EducationalParagraphEntity>

    @Query("SELECT * FROM educational_paragraphs WHERE category IN (:categories)")
    suspend fun getParagraphsByCategories(categories: List<String>): List<EducationalParagraphEntity>

    @Query("SELECT * FROM educational_paragraphs WHERE id = :id")
    suspend fun getParagraphById(id: Int): EducationalParagraphEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertParagraphs(paragraphs: List<EducationalParagraphEntity>)

    @Query("SELECT COUNT(*) FROM educational_paragraphs")
    suspend fun getParagraphCount(): Int

    // --- Speech Config Single Row ---
    @Query("SELECT * FROM speech_configs WHERE id = 1")
    fun getSpeechConfigFlow(): Flow<SpeechConfigEntity?>

    @Query("SELECT * FROM speech_configs WHERE id = 1")
    suspend fun getSpeechConfig(): SpeechConfigEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveSpeechConfig(config: SpeechConfigEntity)
}
