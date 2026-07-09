package com.example.domain.repository

import com.example.data.database.entity.AllowedAppEntity
import com.example.data.database.entity.BlockedWebsiteEntity
import kotlinx.coroutines.flow.Flow

interface BlockerRepository {

    // --- Apps ---
    fun getAllAllowedApps(): Flow<List<AllowedAppEntity>>
    suspend fun addAllowedApp(packageName: String, appName: String)
    suspend fun removeAllowedApp(packageName: String)
    suspend fun isAppAllowed(packageName: String): Boolean

    // --- Websites ---
    fun getAllBlockedWebsites(): Flow<List<BlockedWebsiteEntity>>
    suspend fun addBlockedWebsite(domain: String, isBlocked: Boolean)
    suspend fun removeBlockedWebsite(domain: String)
    suspend fun isWebsiteBlocked(urlOrDomain: String): Boolean

    // --- Active Study Session Checks ---
    suspend fun isStudySessionActiveNow(): Boolean
}
