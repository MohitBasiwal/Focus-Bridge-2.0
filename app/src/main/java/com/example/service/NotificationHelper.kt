package com.example.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.example.MainActivity
import com.example.domain.repository.FocusRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NotificationHelper @Inject constructor(
    @ApplicationContext private val context: Context,
    private val focusRepository: FocusRepository
) {
    private val notificationManager =
        context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    companion object {
        const val CHANNEL_REMINDERS = "focus_bridge_reminders"
        const val CHANNEL_SESSIONS = "focus_bridge_sessions"
        const val CHANNEL_SUMMARIES = "focus_bridge_summaries"
    }

    init {
        createNotificationChannels()
    }

    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val remindersChannel = NotificationChannel(
                CHANNEL_REMINDERS,
                "Study Reminders",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Reminders before and after study sessions"
            }

            val sessionsChannel = NotificationChannel(
                CHANNEL_SESSIONS,
                "Study Sessions",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Active study session starts and ends"
            }

            val summariesChannel = NotificationChannel(
                CHANNEL_SUMMARIES,
                "Progress Summaries",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Daily and weekly study summaries"
            }

            notificationManager.createNotificationChannel(remindersChannel)
            notificationManager.createNotificationChannel(sessionsChannel)
            notificationManager.createNotificationChannel(summariesChannel)
        }
    }

    suspend fun showPreSessionReminder(subjectName: String, minutesBefore: Int) {
        val prefs = focusRepository.getUserPreferences().first()
        if (!prefs.remindersEnabled) return

        val intent = Intent(context, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            context, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val builder = NotificationCompat.Builder(context, CHANNEL_REMINDERS)
            .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
            .setContentTitle("Upcoming Session: $subjectName")
            .setContentText("Your scheduled study session starts in $minutesBefore minutes. Prepare your study space!")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)

        notificationManager.notify(101, builder.build())
    }

    suspend fun showSessionStarted(subjectName: String) {
        val prefs = focusRepository.getUserPreferences().first()
        if (!prefs.sessionStartEndNotifEnabled) return

        val intent = Intent(context, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            context, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val builder = NotificationCompat.Builder(context, CHANNEL_SESSIONS)
            .setSmallIcon(android.R.drawable.ic_media_play)
            .setContentTitle("Focus Session Started: $subjectName")
            .setContentText("Focus Bridge has initiated the app and website blocker. Time to study!")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .setAutoCancel(false)

        notificationManager.notify(102, builder.build())
    }

    suspend fun showSessionEnded(subjectName: String, success: Boolean) {
        val prefs = focusRepository.getUserPreferences().first()
        if (!prefs.sessionStartEndNotifEnabled) return

        // Dismiss start notification
        notificationManager.cancel(102)

        val intent = Intent(context, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            context, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val message = if (success) {
            "Amazing job! You successfully completed your study session for $subjectName."
        } else {
            "Your session for $subjectName has ended. Keep trying, you can do it next time!"
        }

        val builder = NotificationCompat.Builder(context, CHANNEL_SESSIONS)
            .setSmallIcon(android.R.drawable.ic_media_pause)
            .setContentTitle(if (success) "Session Completed! 🎉" else "Session Ended")
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)

        notificationManager.notify(103, builder.build())
    }

    suspend fun showMissedSessionReminder(subjectName: String) {
        val prefs = focusRepository.getUserPreferences().first()
        if (!prefs.missedRemindersEnabled) return

        val intent = Intent(context, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            context, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val builder = NotificationCompat.Builder(context, CHANNEL_REMINDERS)
            .setSmallIcon(android.R.drawable.ic_dialog_alert)
            .setContentTitle("Missed Session: $subjectName")
            .setContentText("You missed your scheduled study session. Dedicate some time today to catch up!")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)

        notificationManager.notify(104, builder.build())
    }

    suspend fun showDailySummary(completedSessions: Int, totalMinutes: Int) {
        val prefs = focusRepository.getUserPreferences().first()
        if (!prefs.summariesEnabled) return

        val intent = Intent(context, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            context, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val builder = NotificationCompat.Builder(context, CHANNEL_SUMMARIES)
            .setSmallIcon(android.R.drawable.ic_menu_my_calendar)
            .setContentTitle("Daily Study Summary 📊")
            .setContentText("Today, you completed $completedSessions sessions ($totalMinutes mins of focused work). Keep up the great streak!")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)

        notificationManager.notify(105, builder.build())
    }

    suspend fun showWeeklySummary(completedSessions: Int, totalMinutes: Int, streakDays: Int) {
        val prefs = focusRepository.getUserPreferences().first()
        if (!prefs.summariesEnabled) return

        val intent = Intent(context, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            context, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val builder = NotificationCompat.Builder(context, CHANNEL_SUMMARIES)
            .setSmallIcon(android.R.drawable.star_big_on)
            .setContentTitle("Weekly Progress Report 🏆")
            .setContentText("This week: $completedSessions sessions, $totalMinutes mins focused. Streak: $streakDays days active!")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)

        notificationManager.notify(106, builder.build())
    }
}
