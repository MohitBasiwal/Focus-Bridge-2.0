package com.example.domain.repository

import com.example.data.database.entity.SecurityConfigEntity
import com.example.data.database.entity.SecurityEventEntity
import kotlinx.coroutines.flow.Flow

/**
 * Repository interface for monitoring device permissions, security events log, protection status, and settings puzzles.
 */
interface SecurityRepository {

    /**
     * Observes the central security settings and pause state.
     */
    fun getSecurityConfigFlow(): Flow<SecurityConfigEntity?>

    /**
     * Gets a snapshot of the current security configuration.
     */
    suspend fun getSecurityConfigSnapshot(): SecurityConfigEntity?

    /**
     * Updates the central security settings.
     */
    suspend fun saveSecurityConfig(config: SecurityConfigEntity)

    /**
     * Observes all recorded security events ordered by timestamp descending.
     */
    fun getAllSecurityEventsFlow(): Flow<List<SecurityEventEntity>>

    /**
     * Retrieve the last recorded security event.
     */
    suspend fun getLastSecurityEvent(): SecurityEventEntity?

    /**
     * Logs a local security event to Room.
     */
    suspend fun logSecurityEvent(eventType: String, details: String, severity: String)

    /**
     * Clears all recorded security event logs.
     */
    suspend fun clearSecurityEvents()

    /**
     * Check permissions status.
     * Returns a map of permission names to their granted status.
     */
    fun checkPermissionsStatus(): Map<String, Boolean>

    /**
     * Verifies if overall Focus Bridge protection is fully active and healthy.
     * True if all required permissions are granted and protection is not paused.
     */
    fun isProtectionActive(): Boolean

    /**
     * Helper to check accessibility service status directly.
     */
    fun isAccessibilityEnabled(): Boolean

    /**
     * Helper to check usage stats access directly.
     */
    fun isUsageAccessGranted(): Boolean

    /**
     * Helper to check draw overlays directly.
     */
    fun isOverlayGranted(): Boolean

    /**
     * Helper to check notifications directly.
     */
    fun isNotificationsGranted(): Boolean

    /**
     * Resets the entire app local configuration (timetable, logs, blocker rules) after verification.
     */
    suspend fun resetAppLocalData()
}
