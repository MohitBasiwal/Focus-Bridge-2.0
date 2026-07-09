package com.example.data.repository

import com.example.data.database.dao.AnalyticsDao
import com.example.data.database.dao.BlockerDao
import com.example.data.database.dao.FocusDao
import com.example.data.database.dao.SpeechDao
import com.example.data.database.dao.StudySessionDao
import com.example.data.database.entity.AllowedAppEntity
import com.example.data.database.entity.BlockedDistractionEntity
import com.example.data.database.entity.BlockedWebsiteEntity
import com.example.data.database.entity.FocusSessionEntity
import com.example.data.database.entity.MissedSessionEntity
import com.example.data.database.entity.SpeechConfigEntity
import com.example.data.database.entity.StudySessionEntity
import com.example.data.datastore.UserPreferencesDataSource
import com.example.domain.model.UserPreferences
import com.example.domain.repository.BackupRepository
import kotlinx.coroutines.flow.first
import org.json.JSONArray
import org.json.JSONObject
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BackupRepositoryImpl @Inject constructor(
    private val studySessionDao: StudySessionDao,
    private val blockerDao: BlockerDao,
    private val focusDao: FocusDao,
    private val speechDao: SpeechDao,
    private val analyticsDao: AnalyticsDao,
    private val preferencesDataSource: UserPreferencesDataSource
) : BackupRepository {

    override suspend fun exportBackupJson(): String {
        val root = JSONObject()
        root.put("version", 1)

        // Settings
        val prefs = preferencesDataSource.userPreferencesFlow.first()
        val settingsJson = JSONObject().apply {
            put("defaultFocusDurationMinutes", prefs.defaultFocusDurationMinutes)
            put("appBlockingEnabled", prefs.appBlockingEnabled)
            put("selectedTheme", prefs.selectedTheme)
            put("darkModeEnabled", prefs.darkModeEnabled)
            put("dynamicColorEnabled", prefs.dynamicColorEnabled)
            put("paragraphCategory", prefs.paragraphCategory)
            put("speechDurationSeconds", prefs.speechDurationSeconds)
            put("remindersEnabled", prefs.remindersEnabled)
            put("sessionStartEndNotifEnabled", prefs.sessionStartEndNotifEnabled)
            put("missedRemindersEnabled", prefs.missedRemindersEnabled)
            put("summariesEnabled", prefs.summariesEnabled)
            put("isOnboarded", prefs.isOnboarded)
        }
        root.put("settings", settingsJson)

        // Timetable
        val timetableArray = JSONArray()
        val studySessions = studySessionDao.getAllStudySessions()
        for (session in studySessions) {
            val sessionJson = JSONObject().apply {
                put("id", session.id)
                put("subjectName", session.subjectName)
                put("startTime", session.startTime)
                put("endTime", session.endTime)
                put("repeatType", session.repeatType)
                put("repeatDays", session.repeatDays)
            }
            timetableArray.put(sessionJson)
        }
        root.put("timetable", timetableArray)

        // Allowed Apps
        val allowedAppsArray = JSONArray()
        val allowedApps = blockerDao.getAllAllowedApps()
        for (app in allowedApps) {
            val appJson = JSONObject().apply {
                put("packageName", app.packageName)
                put("appName", app.appName)
            }
            allowedAppsArray.put(appJson)
        }
        root.put("allowedApps", allowedAppsArray)

        // Website rules
        val websitesArray = JSONArray()
        val websites = blockerDao.getAllBlockedWebsites()
        for (site in websites) {
            val siteJson = JSONObject().apply {
                put("domain", site.domain)
                put("isBlocked", site.isBlocked)
            }
            websitesArray.put(siteJson)
        }
        root.put("blockedWebsites", websitesArray)

        // Speech Config
        val speechConfig = speechDao.getSpeechConfig()
        if (speechConfig != null) {
            val speechJson = JSONObject().apply {
                put("selectedCategories", speechConfig.selectedCategories)
                put("currentParagraphId", speechConfig.currentParagraphId ?: -1)
                put("bypassUntilMs", speechConfig.bypassUntilMs)
            }
            root.put("speechConfig", speechJson)
        }

        // Analytics
        val analyticsJson = JSONObject()
        
        val focusSessionsArray = JSONArray()
        val focusSessions = focusDao.getAllSessions().first()
        for (session in focusSessions) {
            val fsJson = JSONObject().apply {
                put("id", session.id)
                put("durationMinutes", session.durationMinutes)
                put("category", session.category)
                put("timestamp", session.timestamp)
                put("success", session.success)
            }
            focusSessionsArray.put(fsJson)
        }
        analyticsJson.put("focusSessions", focusSessionsArray)

        val blockedDistractionsArray = JSONArray()
        val blockedDistractions = analyticsDao.getAllBlockedDistractions()
        for (dist in blockedDistractions) {
            val dJson = JSONObject().apply {
                put("id", dist.id)
                put("packageNameOrDomain", dist.packageNameOrDomain)
                put("appNameOrTitle", dist.appNameOrTitle)
                put("timestamp", dist.timestamp)
            }
            blockedDistractionsArray.put(dJson)
        }
        analyticsJson.put("blockedDistractions", blockedDistractionsArray)

        val missedSessionsArray = JSONArray()
        val missedSessions = analyticsDao.getAllMissedSessions()
        for (miss in missedSessions) {
            val mJson = JSONObject().apply {
                put("id", miss.id)
                put("subjectName", miss.subjectName)
                put("scheduledStartTime", miss.scheduledStartTime)
                put("scheduledEndTime", miss.scheduledEndTime)
                put("timestamp", miss.timestamp)
            }
            missedSessionsArray.put(mJson)
        }
        analyticsJson.put("missedSessions", missedSessionsArray)

        root.put("analytics", analyticsJson)

        return root.toString(2)
    }

    override suspend fun importBackupJson(json: String): Result<Unit> {
        return try {
            val root = JSONObject(json)
            
            // Restore Settings
            if (root.has("settings")) {
                val settingsJson = root.getJSONObject("settings")
                preferencesDataSource.updateDefaultFocusDuration(settingsJson.optInt("defaultFocusDurationMinutes", 25))
                preferencesDataSource.updateAppBlockingEnabled(settingsJson.optBoolean("appBlockingEnabled", false))
                preferencesDataSource.updateSelectedTheme(settingsJson.optString("selectedTheme", "GlassmorphicDark"))
                preferencesDataSource.updateDarkModeEnabled(settingsJson.optBoolean("darkModeEnabled", true))
                preferencesDataSource.updateDynamicColorEnabled(settingsJson.optBoolean("dynamicColorEnabled", false))
                preferencesDataSource.updateParagraphCategory(settingsJson.optString("paragraphCategory", "Motivation"))
                preferencesDataSource.updateSpeechDurationSeconds(settingsJson.optInt("speechDurationSeconds", 30))
                preferencesDataSource.updateRemindersEnabled(settingsJson.optBoolean("remindersEnabled", true))
                preferencesDataSource.updateSessionStartEndNotifEnabled(settingsJson.optBoolean("sessionStartEndNotifEnabled", true))
                preferencesDataSource.updateMissedRemindersEnabled(settingsJson.optBoolean("missedRemindersEnabled", true))
                preferencesDataSource.updateSummariesEnabled(settingsJson.optBoolean("summariesEnabled", true))
                preferencesDataSource.updateIsOnboarded(settingsJson.optBoolean("isOnboarded", true))
            }

            // Restore Timetable
            if (root.has("timetable")) {
                val timetableArray = root.getJSONArray("timetable")
                studySessionDao.clearAllStudySessions()
                for (i in 0 until timetableArray.length()) {
                    val item = timetableArray.getJSONObject(i)
                    val session = StudySessionEntity(
                        id = item.optLong("id", 0),
                        subjectName = item.getString("subjectName"),
                        startTime = item.getString("startTime"),
                        endTime = item.getString("endTime"),
                        repeatType = item.getString("repeatType"),
                        repeatDays = item.getString("repeatDays")
                    )
                    studySessionDao.insertStudySession(session)
                }
            }

            // Restore Allowed Apps
            if (root.has("allowedApps")) {
                val appsArray = root.getJSONArray("allowedApps")
                blockerDao.clearAllowedApps()
                for (i in 0 until appsArray.length()) {
                    val item = appsArray.getJSONObject(i)
                    val app = AllowedAppEntity(
                        packageName = item.getString("packageName"),
                        appName = item.getString("appName")
                    )
                    blockerDao.insertAllowedApp(app)
                }
            }

            // Restore Blocked Websites
            if (root.has("blockedWebsites")) {
                val websitesArray = root.getJSONArray("blockedWebsites")
                blockerDao.clearBlockedWebsites()
                for (i in 0 until websitesArray.length()) {
                    val item = websitesArray.getJSONObject(i)
                    val site = BlockedWebsiteEntity(
                        domain = item.getString("domain"),
                        isBlocked = item.getBoolean("isBlocked")
                    )
                    blockerDao.insertBlockedWebsite(site)
                }
            }

            // Restore Speech Config
            if (root.has("speechConfig")) {
                val speechJson = root.getJSONObject("speechConfig")
                val config = SpeechConfigEntity(
                    id = 1,
                    selectedCategories = speechJson.getString("selectedCategories"),
                    currentParagraphId = if (speechJson.has("currentParagraphId") && speechJson.getInt("currentParagraphId") != -1) speechJson.getInt("currentParagraphId") else null,
                    bypassUntilMs = speechJson.optLong("bypassUntilMs", 0L)
                )
                speechDao.saveSpeechConfig(config)
            }

            // Restore Analytics
            if (root.has("analytics")) {
                val analyticsJson = root.getJSONObject("analytics")

                if (analyticsJson.has("focusSessions")) {
                    val focusSessionsArray = analyticsJson.getJSONArray("focusSessions")
                    focusDao.clearAllSessions()
                    for (i in 0 until focusSessionsArray.length()) {
                        val item = focusSessionsArray.getJSONObject(i)
                        val session = FocusSessionEntity(
                            id = item.optLong("id", 0),
                            durationMinutes = item.getInt("durationMinutes"),
                            category = item.getString("category"),
                            timestamp = item.getLong("timestamp"),
                            success = item.getBoolean("success")
                        )
                        focusDao.insertSession(session)
                    }
                }

                if (analyticsJson.has("blockedDistractions")) {
                    val distractionsArray = analyticsJson.getJSONArray("blockedDistractions")
                    analyticsDao.clearAllBlockedDistractions()
                    for (i in 0 until distractionsArray.length()) {
                        val item = distractionsArray.getJSONObject(i)
                        val distraction = BlockedDistractionEntity(
                            id = item.optLong("id", 0),
                            packageNameOrDomain = item.getString("packageNameOrDomain"),
                            appNameOrTitle = item.getString("appNameOrTitle"),
                            timestamp = item.getLong("timestamp")
                        )
                        analyticsDao.insertBlockedDistraction(distraction)
                    }
                }

                if (analyticsJson.has("missedSessions")) {
                    val missedArray = analyticsJson.getJSONArray("missedSessions")
                    analyticsDao.clearAllMissedSessions()
                    for (i in 0 until missedArray.length()) {
                        val item = missedArray.getJSONObject(i)
                        val missed = MissedSessionEntity(
                            id = item.optLong("id", 0),
                            subjectName = item.getString("subjectName"),
                            scheduledStartTime = item.getString("scheduledStartTime"),
                            scheduledEndTime = item.getString("scheduledEndTime"),
                            timestamp = item.getLong("timestamp")
                        )
                        analyticsDao.insertMissedSession(missed)
                    }
                }
            }

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
