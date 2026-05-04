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
