package ru.sumenkov.savingscalendar.domain

import java.time.LocalDate
import java.time.YearMonth
import java.time.temporal.ChronoUnit

class SavingsCalculator {
    fun amountForDay(
        dayOfYear: Int,
        baseRate: Long,
        amountMode: SavingsAmountMode = SavingsAmountMode.DAILY_GROWTH
    ): Long {
        require(dayOfYear in 1..366) { "dayOfYear must be in 1..366" }
        require(baseRate >= 0) { "baseRate must be non-negative" }

        return when (amountMode) {
            SavingsAmountMode.DAILY_GROWTH -> dayOfYear.toLong() * baseRate
            SavingsAmountMode.FIXED -> baseRate
        }
    }

    fun amountForDate(
        date: LocalDate,
        baseRate: Long,
        amountMode: SavingsAmountMode = SavingsAmountMode.DAILY_GROWTH,
        accumulationStartDate: LocalDate = LocalDate.of(date.year, 1, 1)
    ): Long {
        return amountForDay(
            dayOfYear = dayNumberInPeriod(date, accumulationStartDate),
            baseRate = baseRate,
            amountMode = amountMode
        )
    }

    fun dayNumberInPeriod(date: LocalDate, accumulationStartDate: LocalDate): Int {
        require(date.year == accumulationStartDate.year) { "date and accumulationStartDate must be in the same year" }
        require(!date.isBefore(accumulationStartDate)) { "date must be on or after accumulationStartDate" }

        return ChronoUnit.DAYS.between(accumulationStartDate, date).toInt() + 1
    }

    fun fullYearPlan(
        year: Int,
        baseRate: Long,
        amountMode: SavingsAmountMode = SavingsAmountMode.DAILY_GROWTH
    ): Long {
        return plannedTotal(
            startDate = LocalDate.of(year, 1, 1),
            endDate = LocalDate.of(year, 12, 31),
            baseRate = baseRate,
            amountMode = amountMode
        )
    }

    fun planFromDateToEndOfYear(
        date: LocalDate,
        baseRate: Long,
        amountMode: SavingsAmountMode = SavingsAmountMode.DAILY_GROWTH
    ): Long {
        return plannedTotal(
            startDate = date,
            endDate = LocalDate.of(date.year, 12, 31),
            baseRate = baseRate,
            amountMode = amountMode
        )
    }

    fun forecastToEndOfYear(
        today: LocalDate,
        baseRate: Long,
        confirmedDates: Set<LocalDate>,
        confirmedBalance: Long,
        amountMode: SavingsAmountMode = SavingsAmountMode.DAILY_GROWTH
    ): Long {
        return forecastToEndOfPeriod(
            today = today,
            baseRate = baseRate,
            confirmedDates = confirmedDates,
            confirmedBalance = confirmedBalance,
            amountMode = amountMode,
            accumulationStartDate = LocalDate.of(today.year, 1, 1),
            accumulationEndDate = LocalDate.of(today.year, 12, 31)
        )
    }

    fun forecastToEndOfPeriod(
        today: LocalDate,
        baseRate: Long,
        confirmedDates: Set<LocalDate>,
        confirmedBalance: Long,
        accumulationStartDate: LocalDate,
        accumulationEndDate: LocalDate,
        amountMode: SavingsAmountMode = SavingsAmountMode.DAILY_GROWTH
    ): Long {
        require(confirmedBalance >= 0) { "confirmedBalance must be non-negative" }
        require(accumulationStartDate.year == today.year) { "accumulationStartDate must be in today's year" }
        require(accumulationEndDate.year == today.year) { "accumulationEndDate must be in today's year" }
        require(!accumulationEndDate.isBefore(accumulationStartDate)) {
            "accumulationEndDate must be on or after accumulationStartDate"
        }

        val firstPlannedDate = maxOf(today, accumulationStartDate)
        if (firstPlannedDate.isAfter(accumulationEndDate)) return confirmedBalance

        val futurePlan = sumDates(firstPlannedDate, accumulationEndDate) { date ->
            if (date in confirmedDates) {
                0L
            } else {
                amountForDate(
                    date = date,
                    baseRate = baseRate,
                    amountMode = amountMode,
                    accumulationStartDate = accumulationStartDate
                )
            }
        }
        return confirmedBalance + futurePlan
    }

    fun plannedTotal(
        startDate: LocalDate,
        endDate: LocalDate,
        baseRate: Long,
        amountMode: SavingsAmountMode = SavingsAmountMode.DAILY_GROWTH
    ): Long {
        require(startDate.year == endDate.year) { "startDate and endDate must be in the same year" }
        require(!endDate.isBefore(startDate)) { "endDate must be on or after startDate" }

        return sumDates(startDate, endDate) { date ->
            amountForDate(
                date = date,
                baseRate = baseRate,
                amountMode = amountMode,
                accumulationStartDate = startDate
            )
        }
    }

    fun monthlyPlannedTotal(
        yearMonth: YearMonth,
        baseRate: Long,
        amountMode: SavingsAmountMode = SavingsAmountMode.DAILY_GROWTH
    ): Long {
        return plannedTotal(
            startDate = yearMonth.atDay(1),
            endDate = yearMonth.atEndOfMonth(),
            baseRate = baseRate,
            amountMode = amountMode
        )
    }

    fun isLastDayOfMonth(date: LocalDate): Boolean {
        return date.dayOfMonth == date.lengthOfMonth()
    }

    private fun sumDates(
        startDate: LocalDate,
        endDate: LocalDate,
        amountForDate: (LocalDate) -> Long
    ): Long {
        var total = 0L
        var date = startDate
        while (!date.isAfter(endDate)) {
            total += amountForDate(date)
            date = date.plusDays(1)
        }
        return total
    }
}
