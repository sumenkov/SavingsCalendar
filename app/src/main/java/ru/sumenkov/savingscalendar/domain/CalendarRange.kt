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
    return calendarRangeForPeriod(
        today = today,
        accumulationStartDate = today.minusYears(1),
        accumulationEndDate = today.plusYears(1)
    )
}

fun calendarRangeForPeriod(
    today: LocalDate,
    accumulationStartDate: LocalDate,
    accumulationEndDate: LocalDate
): CalendarRange {
    return CalendarRange(
        minMonth = minOf(
            YearMonth.from(today.minusYears(1)),
            YearMonth.from(accumulationStartDate)
        ),
        maxMonth = maxOf(
            YearMonth.from(today.plusYears(1)),
            YearMonth.from(accumulationEndDate)
        )
    )
}
