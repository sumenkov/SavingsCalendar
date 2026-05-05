package ru.sumenkov.savingscalendar.domain

import java.time.LocalDate
import java.time.YearMonth

data class CalendarRange(
    val minMonth: YearMonth,
    val maxMonth: YearMonth
) {
    fun canGoPrevious(currentMonth: YearMonth): Boolean = currentMonth.isAfter(minMonth)

    fun canGoNext(currentMonth: YearMonth): Boolean = currentMonth.isBefore(maxMonth)
}

fun calendarRangeAround(today: LocalDate): CalendarRange {
    return CalendarRange(
        minMonth = YearMonth.from(today.minusYears(1)),
        maxMonth = YearMonth.from(today.plusYears(1))
    )
}
