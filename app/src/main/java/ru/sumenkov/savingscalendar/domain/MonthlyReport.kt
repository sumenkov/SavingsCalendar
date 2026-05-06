package ru.sumenkov.savingscalendar.domain

import java.time.YearMonth

data class MonthlyReport(
    val yearMonth: YearMonth,
    val monthTotal: Long,
    val periodTotal: Long,
    val completedDaysInMonth: Int,
    val plannedDaysInMonth: Int
) {
    val completionPercent: Int
        get() = if (plannedDaysInMonth == 0) 0 else completedDaysInMonth * 100 / plannedDaysInMonth
}
