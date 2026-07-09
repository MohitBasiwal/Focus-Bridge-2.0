package com.example.domain.usecase

import com.example.domain.model.*
import com.example.domain.repository.AnalyticsRepository
import com.example.domain.repository.FocusRepository
import com.example.domain.repository.TimetableRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GetAnalyticsUseCase @Inject constructor(
    private val focusRepository: FocusRepository,
    private val timetableRepository: TimetableRepository,
    private val analyticsRepository: AnalyticsRepository
) {

    fun execute(): Flow<AnalyticsData> {
        return combine(
            focusRepository.getAllSessions(),
            focusRepository.getUserPreferences(),
            timetableRepository.getAllStudySessions(),
            analyticsRepository.getAllBlockedDistractions(),
            analyticsRepository.getAllMissedSessions()
        ) { sessions, prefs, timetable, distractions, missed ->
            
            val calendar = Calendar.getInstance()
            val now = System.currentTimeMillis()
            
            // Time boundaries
            val startOfToday = getStartOfDay(now)
            val startOfThisWeek = getStartOfWeek(now)
            val startOfThisMonth = getStartOfMonth(now)
            
            // Completed focus sessions
            val successfulSessions = sessions.filter { it.success }
            val todaySessions = successfulSessions.filter { it.timestamp >= startOfToday }
            val thisWeekSessions = successfulSessions.filter { it.timestamp >= startOfThisWeek }
            val thisMonthSessions = successfulSessions.filter { it.timestamp >= startOfThisMonth }
            
            // Blocked distractions
            val todayDistractions = distractions.filter { it.timestamp >= startOfToday }
            val thisWeekDistractions = distractions.filter { it.timestamp >= startOfThisWeek }
            val thisMonthDistractions = distractions.filter { it.timestamp >= startOfThisMonth }
            
            // Totals
            val todayMinutes = todaySessions.sumOf { it.durationMinutes }
            val thisWeekMinutes = thisWeekSessions.sumOf { it.durationMinutes }
            val thisMonthMinutes = thisMonthSessions.sumOf { it.durationMinutes }
            
            val totalMinutes = successfulSessions.sumOf { it.durationMinutes }
            val totalHours = totalMinutes / 60.0
            
            // Average Focus Time
            val avgFocusTime = if (successfulSessions.isNotEmpty()) {
                successfulSessions.sumOf { it.durationMinutes } / successfulSessions.size
            } else {
                0
            }
            
            // Streak
            val currentStreak = calculateStreak(successfulSessions)
            
            // Most Studied Subject/Category
            val mostStudied = successfulSessions
                .groupBy { it.category }
                .maxByOrNull { group -> group.value.sumOf { it.durationMinutes } }
                ?.key ?: "None"
                
            // Missed sessions
            val totalMissedCount = missed.size
            
            // Focus Score Calculation
            // Base score is 50. Add points for completed sessions, total minutes, distractions blocked.
            // Deduct points for missed timetable sessions.
            val completedCount = successfulSessions.size
            val distractionsCount = distractions.size
            
            val completedPoints = (completedCount * 5).coerceAtMost(30)
            val timePoints = (totalMinutes / 15).toInt().coerceAtMost(30)
            val distractionPoints = (distractionsCount * 3).coerceAtMost(15)
            
            val adherenceRatio = if (completedCount + totalMissedCount > 0) {
                completedCount.toFloat() / (completedCount + totalMissedCount)
            } else {
                1.0f
            }
            val adherencePoints = (adherenceRatio * 25).toInt()
            
            val calculatedFocusScore = (10 + completedPoints + timePoints + distractionPoints + adherencePoints).coerceIn(0, 100)
            
            // Daily Chart Data (Days of current week)
            val dailyChart = getDailyChartData(successfulSessions)
            
            // Weekly Chart Data (Last 4 weeks)
            val weeklyChart = getWeeklyChartData(successfulSessions)
            
            // Monthly Chart Data (Last 6 months)
            val monthlyChart = getMonthlyChartData(successfulSessions)
            
            // Subject Distribution
            val subjectDist = getSubjectDistribution(successfulSessions)
            
            // Upcoming study session
            val currentDay = getCurrentDayOfWeek()
            val currentMinutes = getCurrentTimeInMinutes()
            val upcoming = timetable
                .filter { it.activeDaysSet.contains(currentDay) }
                .sortedBy { it.startTimeMinutes }
                .firstOrNull { it.startTimeMinutes > currentMinutes }
                ?: timetable.filter { it.activeDaysSet.contains(currentDay) }.sortedBy { it.startTimeMinutes }.firstOrNull()
                
            // Achievements check
            val achievements = listOf(
                Achievement(
                    id = "first_session",
                    title = "First Study Session",
                    description = "Logged your first focused session on the bridge",
                    isUnlocked = completedCount >= 1,
                    progress = (completedCount / 1.0f).coerceIn(0.0f, 1.0f),
                    iconName = "ic_achievement_first"
                ),
                Achievement(
                    id = "7_day_streak",
                    title = "7-Day Streak",
                    description = "Maintained a focus streak for 7 consecutive days",
                    isUnlocked = currentStreak >= 7,
                    progress = (currentStreak / 7.0f).coerceIn(0.0f, 1.0f),
                    iconName = "ic_achievement_streak"
                ),
                Achievement(
                    id = "30_day_streak",
                    title = "30-Day Streak",
                    description = "Committed focused student for 30 consecutive days",
                    isUnlocked = currentStreak >= 30,
                    progress = (currentStreak / 30.0f).coerceIn(0.0f, 1.0f),
                    iconName = "ic_achievement_month"
                ),
                Achievement(
                    id = "100_hours",
                    title = "100 Hours Studied",
                    description = "Spent 100 hours of focused time on study sessions",
                    isUnlocked = totalHours >= 100.0,
                    progress = (totalHours / 100.0).toFloat().coerceIn(0.0f, 1.0f),
                    iconName = "ic_achievement_hours"
                ),
                Achievement(
                    id = "focus_master",
                    title = "Focus Master",
                    description = "Unlock 50 successful focus sessions on Focus Bridge",
                    isUnlocked = completedCount >= 50,
                    progress = (completedCount / 50.0f).coerceIn(0.0f, 1.0f),
                    iconName = "ic_achievement_master"
                )
            )

            AnalyticsData(
                focusScore = calculatedFocusScore,
                todayCompletedSessions = todaySessions.size,
                todayFocusTimeMinutes = todayMinutes,
                currentStreak = currentStreak,
                upcomingSessionName = upcoming?.subjectName,
                isBlockingActive = prefs.appBlockingEnabled,
                
                dailyStats = DailyStats(
                    studyTimeMinutes = todayMinutes,
                    completedSessions = todaySessions.size,
                    distractionsBlocked = todayDistractions.size
                ),
                weeklyStats = WeeklyStats(
                    studyTimeMinutes = thisWeekMinutes,
                    completedSessions = thisWeekSessions.size,
                    distractionsBlocked = thisWeekDistractions.size
                ),
                monthlyStats = MonthlyStats(
                    studyTimeMinutes = thisMonthMinutes,
                    completedSessions = thisMonthSessions.size,
                    distractionsBlocked = thisMonthDistractions.size
                ),
                
                totalStudyHours = Math.round(totalHours * 10.0) / 10.0,
                totalBlockedDistractions = distractions.size,
                averageFocusTimeMinutes = avgFocusTime,
                mostStudiedSubject = mostStudied,
                missedSessionsCount = totalMissedCount,
                achievements = achievements,
                dailyChartData = dailyChart,
                weeklyChartData = weeklyChart,
                monthlyChartData = monthlyChart,
                subjectDistribution = subjectDist
            )
        }
    }

    private fun getStartOfDay(time: Long): Long {
        val cal = Calendar.getInstance()
        cal.timeInMillis = time
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
        return cal.timeInMillis
    }

    private fun getStartOfWeek(time: Long): Long {
        val cal = Calendar.getInstance()
        cal.timeInMillis = time
        cal.set(Calendar.DAY_OF_WEEK, cal.firstDayOfWeek)
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
        return cal.timeInMillis
    }

    private fun getStartOfMonth(time: Long): Long {
        val cal = Calendar.getInstance()
        cal.timeInMillis = time
        cal.set(Calendar.DAY_OF_MONTH, 1)
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
        return cal.timeInMillis
    }

    private fun calculateStreak(sessions: List<FocusSession>): Int {
        if (sessions.isEmpty()) return 0
        
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val uniqueDays = sessions.map { sdf.format(Date(it.timestamp)) }.toSet()
        
        var streak = 0
        val cal = Calendar.getInstance()
        
        // Check today
        val todayStr = sdf.format(cal.time)
        val yesterdayCal = Calendar.getInstance()
        yesterdayCal.add(Calendar.DATE, -1)
        val yesterdayStr = sdf.format(yesterdayCal.time)
        
        // If neither today nor yesterday has a session, streak is broken / 0
        if (!uniqueDays.contains(todayStr) && !uniqueDays.contains(yesterdayStr)) {
            return 0
        }
        
        // Start checking day-by-day backwards
        if (uniqueDays.contains(todayStr)) {
            // Start counting from today
        } else {
            // Start counting from yesterday
            cal.add(Calendar.DATE, -1)
        }
        
        while (uniqueDays.contains(sdf.format(cal.time))) {
            streak++
            cal.add(Calendar.DATE, -1)
        }
        
        return streak
    }

    private fun getDailyChartData(sessions: List<FocusSession>): List<ChartItem> {
        val days = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")
        val cal = Calendar.getInstance()
        val dailyMap = mutableMapOf<String, Float>()
        
        days.forEach { dailyMap[it] = 0f }
        
        // Map current week's sessions
        val startOfWeek = getStartOfWeek(System.currentTimeMillis())
        val currentWeekSessions = sessions.filter { it.timestamp >= startOfWeek }
        
        val sdf = SimpleDateFormat("EEE", Locale.US)
        currentWeekSessions.forEach { session ->
            val dayName = sdf.format(Date(session.timestamp))
            if (dailyMap.containsKey(dayName)) {
                dailyMap[dayName] = dailyMap[dayName]!! + session.durationMinutes.toFloat()
            }
        }
        
        return days.map { ChartItem(it, dailyMap[it] ?: 0f) }
    }

    private fun getWeeklyChartData(sessions: List<FocusSession>): List<ChartItem> {
        val weeklyData = mutableListOf<ChartItem>()
        val cal = Calendar.getInstance()
        
        // Calculate last 4 weeks study hours
        for (i in 3 downTo 0) {
            val weekCalStart = Calendar.getInstance()
            weekCalStart.add(Calendar.WEEK_OF_YEAR, -i)
            weekCalStart.set(Calendar.DAY_OF_WEEK, weekCalStart.firstDayOfWeek)
            weekCalStart.set(Calendar.HOUR_OF_DAY, 0)
            weekCalStart.set(Calendar.MINUTE, 0)
            
            val weekCalEnd = Calendar.getInstance()
            weekCalEnd.timeInMillis = weekCalStart.timeInMillis
            weekCalEnd.add(Calendar.WEEK_OF_YEAR, 1)
            
            val start = weekCalStart.timeInMillis
            val end = weekCalEnd.timeInMillis
            
            val weekSessions = sessions.filter { it.timestamp in start until end }
            val hours = weekSessions.sumOf { it.durationMinutes } / 60f
            
            weeklyData.add(ChartItem("Wk ${4 - i}", hours))
        }
        
        return weeklyData
    }

    private fun getMonthlyChartData(sessions: List<FocusSession>): List<ChartItem> {
        val monthlyData = mutableListOf<ChartItem>()
        val sdf = SimpleDateFormat("MMM", Locale.getDefault())
        
        // Calculate last 6 months study hours
        for (i in 5 downTo 0) {
            val monthCalStart = Calendar.getInstance()
            monthCalStart.add(Calendar.MONTH, -i)
            monthCalStart.set(Calendar.DAY_OF_MONTH, 1)
            monthCalStart.set(Calendar.HOUR_OF_DAY, 0)
            monthCalStart.set(Calendar.MINUTE, 0)
            
            val monthCalEnd = Calendar.getInstance()
            monthCalEnd.timeInMillis = monthCalStart.timeInMillis
            monthCalEnd.add(Calendar.MONTH, 1)
            
            val start = monthCalStart.timeInMillis
            val end = monthCalEnd.timeInMillis
            
            val monthSessions = sessions.filter { it.timestamp in start until end }
            val hours = monthSessions.sumOf { it.durationMinutes } / 60f
            
            val label = sdf.format(monthCalStart.time)
            monthlyData.add(ChartItem(label, hours))
        }
        
        return monthlyData
    }

    private fun getSubjectDistribution(sessions: List<FocusSession>): List<SubjectDistributionItem> {
        if (sessions.isEmpty()) return emptyList()
        
        val categoryMinutes = sessions.groupBy { it.category }
            .mapValues { entry -> entry.value.sumOf { it.durationMinutes } }
            
        val totalMins = categoryMinutes.values.sum().toFloat()
        if (totalMins == 0f) return emptyList()
        
        val colors = listOf("#00F0FF", "#8A2BE2", "#FF007F", "#00FF66", "#FFFFFF")
        
        return categoryMinutes.entries.mapIndexed { index, entry ->
            val color = colors[index % colors.size]
            SubjectDistributionItem(
                subject = entry.key,
                minutes = entry.value,
                percentage = (entry.value / totalMins) * 100f,
                colorHex = color
            )
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
