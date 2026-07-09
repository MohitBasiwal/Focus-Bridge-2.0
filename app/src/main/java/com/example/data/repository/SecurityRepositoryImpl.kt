package com.example.data.repository

import android.content.ComponentName
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.provider.Settings
import android.text.TextUtils
import android.app.AppOpsManager
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.example.data.database.dao.*
import com.example.data.database.entity.SecurityConfigEntity
import com.example.data.database.entity.SecurityEventEntity
import com.example.domain.repository.SecurityRepository
import com.example.service.FocusBridgeAccessibilityService
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SecurityRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val securityDao: SecurityDao,
    private val focusDao: FocusDao,
    private val studySessionDao: StudySessionDao,
    private val blockerDao: BlockerDao,
    private val speechDao: SpeechDao
) : SecurityRepository {

    override fun getSecurityConfigFlow(): Flow<SecurityConfigEntity?> {
        return securityDao.getSecurityConfigFlow()
    }

    override suspend fun getSecurityConfigSnapshot(): SecurityConfigEntity? {
        var config = securityDao.getSecurityConfig()
        if (config == null) {
            config = SecurityConfigEntity()
            securityDao.saveSecurityConfig(config)
        }
        return config
    }

    override suspend fun saveSecurityConfig(config: SecurityConfigEntity) {
        securityDao.saveSecurityConfig(config)
    }

    override fun getAllSecurityEventsFlow(): Flow<List<SecurityEventEntity>> {
        return securityDao.getAllSecurityEventsFlow()
    }

    override suspend fun getLastSecurityEvent(): SecurityEventEntity? {
        val events = securityDao.getRecentSecurityEvents()
        return events.firstOrNull()
    }

    override suspend fun logSecurityEvent(eventType: String, details: String, severity: String) {
        val event = SecurityEventEntity(
            eventType = eventType,
            details = details,
            severity = severity
        )
        securityDao.insertSecurityEvent(event)
    }

    override suspend fun clearSecurityEvents() {
        securityDao.clearSecurityEvents()
    }

    override fun checkPermissionsStatus(): Map<String, Boolean> {
        return mapOf(
            "Accessibility Service" to isAccessibilityEnabled(),
            "Usage Access" to isUsageAccessGranted(),
            "Draw Overlays" to isOverlayGranted(),
            "Notifications" to isNotificationsGranted()
        )
    }

    override fun isProtectionActive(): Boolean {
        // Checking if all crucial permissions are fully granted
        val acc = isAccessibilityEnabled()
        val usage = isUsageAccessGranted()
        val overlay = isOverlayGranted()
        
        // Protection is only active if no permission is disabled and configuration is not manually paused
        return acc && usage && overlay
    }

    override fun isAccessibilityEnabled(): Boolean {
        val expectedComponentName = ComponentName(context, FocusBridgeAccessibilityService::class.java)
        val enabledServicesSetting = Settings.Secure.getString(
            context.contentResolver,
            Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
        ) ?: return false
        val colonSplitter = TextUtils.SimpleStringSplitter(':')
        colonSplitter.setString(enabledServicesSetting)
        while (colonSplitter.hasNext()) {
            val componentNameString = colonSplitter.next()
            val enabledService = ComponentName.unflattenFromString(componentNameString)
            if (enabledService != null && enabledService == expectedComponentName) {
                return true
            }
        }
        return false
    }

    override fun isUsageAccessGranted(): Boolean {
        val appOps = context.getSystemService(Context.APP_OPS_SERVICE) as AppOpsManager
        val mode = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            appOps.unsafeCheckOpNoThrow(
                AppOpsManager.OPSTR_GET_USAGE_STATS,
                android.os.Process.myUid(),
                context.packageName
            )
        } else {
            @Suppress("DEPRECATION")
            appOps.checkOpNoThrow(
                AppOpsManager.OPSTR_GET_USAGE_STATS,
                android.os.Process.myUid(),
                context.packageName
            )
        }
        return mode == AppOpsManager.MODE_ALLOWED
    }

    override fun isOverlayGranted(): Boolean {
        return Settings.canDrawOverlays(context)
    }

    override fun isNotificationsGranted(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(
                context,
                android.Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            NotificationManagerCompat.from(context).areNotificationsEnabled()
        }
    }

    override suspend fun resetAppLocalData() {
        focusDao.clearAllSessions()
        studySessionDao.clearAllStudySessions()
        blockerDao.clearAllowedApps()
        blockerDao.clearBlockedWebsites()
        
        // Log the complete reset
        logSecurityEvent(
            eventType = "APP_RESET",
            details = "All local schedules, history, and block rules cleared via puzzle authentication.",
            severity = "CRITICAL"
        )
    }
}
