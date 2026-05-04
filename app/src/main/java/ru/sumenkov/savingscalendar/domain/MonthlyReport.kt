package ru.sumenkov.savingscalendar.domain

import java.time.YearMonth

data class MonthlyReport(
    val yearMonth: YearMonth,
    val monthTotal: Long,
    val yearTotal: Long,
    val completedDaysInMonth: Int,
    val daysInMonth: Int
) {
    val completionPercent: Int
        get() = if (daysInMonth == 0) 0 else completedDaysInMonth * 100 / daysInMonth
}
