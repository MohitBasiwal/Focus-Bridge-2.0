package com.example.domain.model

data class AnalyticsData(
    val focusScore: Int,
    val todayCompletedSessions: Int,
    val todayFocusTimeMinutes: Int,
    val currentStreak: Int,
    val upcomingSessionName: String?,
    val isBlockingActive: Boolean,
    
    // Statistics
    val dailyStats: DailyStats,
    val weeklyStats: WeeklyStats,
    val monthlyStats: MonthlyStats,
    
    val totalStudyHours: Double,
    val totalBlockedDistractions: Int,
    val averageFocusTimeMinutes: Int,
    val mostStudiedSubject: String,
    val missedSessionsCount: Int,
    
    // Achievements
    val achievements: List<Achievement>,
    
    // Chart lists
    val dailyChartData: List<ChartItem>, // Day of week to minutes
    val weeklyChartData: List<ChartItem>, // Week label to hours
    val monthlyChartData: List<ChartItem>, // Month label to hours
    val subjectDistribution: List<SubjectDistributionItem> // Subject name to percentage
)

data class DailyStats(
    val studyTimeMinutes: Int,
    val completedSessions: Int,
    val distractionsBlocked: Int
)

data class WeeklyStats(
    val studyTimeMinutes: Int,
    val completedSessions: Int,
    val distractionsBlocked: Int
)

data class MonthlyStats(
    val studyTimeMinutes: Int,
    val completedSessions: Int,
    val distractionsBlocked: Int
)

data class Achievement(
    val id: String,
    val title: String,
    val description: String,
    val isUnlocked: Boolean,
    val progress: Float, // 0.0f to 1.0f
    val iconName: String
)

data class ChartItem(
    val label: String,
    val value: Float
)

data class SubjectDistributionItem(
    val subject: String,
    val minutes: Int,
    val percentage: Float,
    val colorHex: String
)
