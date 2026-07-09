package com.example.domain.model

/**
 * Domain-level model representing a student study session.
 * Completely decoupled from Room annotations to adhere to Clean Architecture principles.
 */
data class StudySession(
    val id: Long = 0,
    val subjectName: String,
    val startTime: String,   // "HH:mm" format (e.g., "09:30")
    val endTime: String,     // "HH:mm" format (e.g., "11:00")
    val repeatType: String,  // "None", "Daily", "Weekly", "Custom"
    val repeatDays: String   // Comma separated list of integers representing days of the week (1 = Mon, ..., 7 = Sun)
) {
    /**
     * Set of day integers where this session repeats.
     */
    val activeDaysSet: Set<Int>
        get() = if (repeatDays.isBlank()) {
            emptySet()
        } else {
            repeatDays.split(",")
                .mapNotNull { it.trim().toIntOrNull() }
                .toSet()
        }

    /**
     * Converts start time string to minutes since midnight for range calculation.
     */
    val startTimeMinutes: Int
        get() = parseTimeToMinutes(startTime)

    /**
     * Converts end time string to minutes since midnight for range calculation.
     */
    val endTimeMinutes: Int
        get() = parseTimeToMinutes(endTime)

    /**
     * Checks if this session overlaps with another session.
     */
    fun overlapsWith(other: StudySession): Boolean {
        // If they share no repeating days, they cannot overlap
        val sharedDays = this.activeDaysSet.intersect(other.activeDaysSet)
        if (sharedDays.isEmpty()) return false

        // Mathematical overlap check: max(startA, startB) < min(endA, endB)
        return maxOf(this.startTimeMinutes, other.startTimeMinutes) < minOf(this.endTimeMinutes, other.endTimeMinutes)
    }

    companion object {
        fun parseTimeToMinutes(timeStr: String): Int {
            val parts = timeStr.split(":")
            if (parts.size != 2) return 0
            val hours = parts[0].toIntOrNull() ?: 0
            val minutes = parts[1].toIntOrNull() ?: 0
            return hours * 60 + minutes
        }
    }
}
