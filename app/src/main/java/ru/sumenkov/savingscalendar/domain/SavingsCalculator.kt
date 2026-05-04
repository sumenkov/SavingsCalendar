package ru.sumenkov.savingscalendar.domain

import java.time.LocalDate
import java.time.Year
import java.time.YearMonth

class SavingsCalculator {
    fun amountForDay(dayOfYear: Int, baseRate: Long): Long {
        require(dayOfYear in 1..366) { "dayOfYear must be in 1..366" }
        require(baseRate >= 0) { "baseRate must be non-negative" }
        return dayOfYear.toLong() * baseRate
    }

    fun fullYearPlan(year: Int, baseRate: Long): Long {
        val days = Year.of(year).length()
        return days.toLong() * (days + 1L) / 2L * baseRate
    }

    fun planFromDateToEndOfYear(date: LocalDate, baseRate: Long): Long {
        val daysInYear = Year.of(date.year).length()
        return (date.dayOfYear..daysInYear).sumOf { day -> amountForDay(day, baseRate) }
    }

    fun forecastToEndOfYear(
        today: LocalDate,
        baseRate: Long,
        confirmedDates: Set<LocalDate>,
        confirmedBalance: Long
    ): Long {
        require(confirmedBalance >= 0) { "confirmedBalance must be non-negative" }

        val startDate = if (today in confirmedDates) today.plusDays(1) else today
        if (startDate.year != today.year) return confirmedBalance

        val daysInYear = Year.of(today.year).length()
        val futurePlan = (startDate.dayOfYear..daysInYear).sumOf { day ->
            val date = LocalDate.ofYearDay(today.year, day)
            if (date in confirmedDates) 0L else amountForDay(day, baseRate)
        }
        return confirmedBalance + futurePlan
    }

    fun monthlyPlannedTotal(yearMonth: YearMonth, baseRate: Long): Long {
        return (1..yearMonth.lengthOfMonth()).sumOf { dayOfMonth ->
            val date = yearMonth.atDay(dayOfMonth)
            amountForDay(date.dayOfYear, baseRate)
        }
    }

    fun isLastDayOfMonth(date: LocalDate): Boolean {
        return date.dayOfMonth == date.lengthOfMonth()
    }
}
