package com.example.domain.repository

interface BackupRepository {
    suspend fun exportBackupJson(): String
    suspend fun importBackupJson(json: String): Result<Unit>
}
