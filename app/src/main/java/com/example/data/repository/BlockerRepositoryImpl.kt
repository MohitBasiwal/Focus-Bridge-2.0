package com.example.data.repository

import android.app.AppOpsManager
import android.content.ComponentName
import android.content.Context
import android.os.Build
import android.provider.Settings
import android.text.TextUtils
import com.example.data.database.dao.BlockerDao
import com.example.data.database.dao.StudySessionDao
import com.example.data.database.dao.SecurityDao
import com.example.data.database.entity.AllowedAppEntity
import com.example.data.database.entity.BlockedWebsiteEntity
import com.example.domain.model.StudySession
import com.example.domain.repository.BlockerRepository
import com.example.service.FocusBridgeAccessibilityService
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import java.util.Calendar
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BlockerRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val blockerDao: BlockerDao,
    private val studySessionDao: StudySessionDao,
    private val speechDao: com.example.data.database.dao.SpeechDao,
    private val securityDao: SecurityDao
) : BlockerRepository {

    override fun getAllAllowedApps(): Flow<List<AllowedAppEntity>> {
        return blockerDao.getAllAllowedAppsFlow()
    }

    override suspend fun addAllowedApp(packageName: String, appName: String) {
        blockerDao.insertAllowedApp(AllowedAppEntity(packageName, appName))
    }

    override suspend fun removeAllowedApp(packageName: String) {
        blockerDao.deleteAllowedApp(AllowedAppEntity(packageName, ""))
    }

    override suspend fun isAppAllowed(packageName: String): Boolean {
        // System / Launcher / Essential packages are always allowed to prevent soft locking
        if (packageName == "com.example" || 
            packageName.contains("launcher") || 
            packageName.contains("systemui") || 
            packageName == "android" ||
            packageName == "com.android.settings" ||
            packageName.contains("packageinstaller")
        ) {
            return true
        }
        
        val allowed = blockerDao.getAllAllowedApps()
        // If the user has not configured any allowed apps, let's treat it as "everything is allowed" or "strictly block unless explicitly allowed".
        // The requirement says: "Student manually selects which apps are allowed during each study session. All other apps are blocked during that session."
        // This implies if a student starts a session, everything else is blocked.
        // Let's check if the package is in our allowed list.
        return allowed.any { it.packageName == packageName }
    }

    override fun getAllBlockedWebsites(): Flow<List<BlockedWebsiteEntity>> {
        return blockerDao.getAllBlockedWebsitesFlow()
    }

    override suspend fun addBlockedWebsite(domain: String, isBlocked: Boolean) {
        blockerDao.insertBlockedWebsite(BlockedWebsiteEntity(domain, isBlocked))
    }

    override suspend fun removeBlockedWebsite(domain: String) {
        blockerDao.deleteBlockedWebsite(BlockedWebsiteEntity(domain, false))
    }

    override suspend fun isWebsiteBlocked(urlOrDomain: String): Boolean {
        val rules = blockerDao.getAllBlockedWebsites()
        if (rules.isEmpty()) return false

        // Check matching rules
        return rules.any { rule ->
            val host = urlOrDomain.lowercase()
            val ruleDomain = rule.domain.lowercase()
            
            val matches = host.contains(ruleDomain) || ruleDomain.contains(host)
            
            if (matches) {
                rule.isBlocked // Return whether it is blocked (true) or whitelisted (false)
            } else {
                false
            }
        }
    }

    private suspend fun isBypassActive(): Boolean {
        val config = speechDao.getSpeechConfig() ?: return false
        return System.currentTimeMillis() < config.bypassUntilMs
    }

    private suspend fun isProtectionActive(): Boolean {
        // Check if protection is manually paused in database
        val securityConfig = securityDao.getSecurityConfig()
        if (securityConfig != null && securityConfig.isProtectionPaused) {
            return false
        }

        // Check required permissions
        val accEnabled = isAccessibilityEnabled()
        val usageEnabled = isUsageAccessGranted()
        val overlayEnabled = isOverlayGranted()

        return accEnabled && usageEnabled && overlayEnabled
    }

    private fun isAccessibilityEnabled(): Boolean {
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

    private fun isUsageAccessGranted(): Boolean {
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

    private fun isOverlayGranted(): Boolean {
        return Settings.canDrawOverlays(context)
    }

    override suspend fun isStudySessionActiveNow(): Boolean {
        if (!isProtectionActive()) return false
        if (isBypassActive()) return false
        
        val sessions = studySessionDao.getAllStudySessions()
        if (sessions.isEmpty()) return false

        val currentDay = getCurrentDayOfWeek()
        val currentMinutes = getCurrentTimeInMinutes()

        return sessions.any { entity ->
            val activeDays = if (entity.repeatDays.isBlank()) {
                emptySet()
            } else {
                entity.repeatDays.split(",")
                    .mapNotNull { it.trim().toIntOrNull() }
                    .toSet()
            }

            if (activeDays.contains(currentDay)) {
                val startMin = StudySession.parseTimeToMinutes(entity.startTime)
                val endMin = StudySession.parseTimeToMinutes(entity.endTime)
                currentMinutes in startMin..endMin
            } else {
                false
            }
        }
    }

    private fun getCurrentDayOfWeek(): Int {
        val calendar = Calendar.getInstance()
        return when (calendar.get(Calendar.DAY_OF_WEEK)) {
            Calendar.MONDAY -> 1
            Calendar.TUESDAY -> 2
            Calendar.WEDNESDAY -> 3
            Calendar.THURSDAY -> 4
            Calendar.FRIDAY -> 5
            Calendar.SATURDAY -> 6
            Calendar.SUNDAY -> 7
            else -> 1
        }
    }

    private fun getCurrentTimeInMinutes(): Int {
        val calendar = Calendar.getInstance()
        return calendar.get(Calendar.HOUR_OF_DAY) * 60 + calendar.get(Calendar.MINUTE)
    }
}
