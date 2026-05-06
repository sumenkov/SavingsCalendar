package ru.sumenkov.savingscalendar.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate
import java.time.YearMonth

class SavingsCalculatorTest {
    private val calculator = SavingsCalculator()

    @Test
    fun amountForDayUsesDayNumberAndBaseRate() {
        assertEquals(1L, calculator.amountForDay(dayOfYear = 1, baseRate = 1L))
        assertEquals(53L, calculator.amountForDay(dayOfYear = 53, baseRate = 1L))
        assertEquals(265L, calculator.amountForDay(dayOfYear = 53, baseRate = 5L))
    }

    @Test
    fun fixedAmountModeUsesBaseRateForEveryDay() {
        assertEquals(
            5L,
            calculator.amountForDay(
                dayOfYear = 53,
                baseRate = 5L,
                amountMode = SavingsAmountMode.FIXED
            )
        )
    }

    @Test
    fun amountForDateUsesDayNumberFromAccumulationStart() {
        val startDate = LocalDate.of(2026, 5, 6)

        assertEquals(
            1L,
            calculator.amountForDate(
                date = LocalDate.of(2026, 5, 6),
                baseRate = 1L,
                accumulationStartDate = startDate
            )
        )
        assertEquals(
            5L,
            calculator.amountForDate(
                date = LocalDate.of(2026, 5, 10),
                baseRate = 1L,
                accumulationStartDate = startDate
            )
        )
    }

    @Test
    fun amountForDateSupportsAccumulationPeriodAcrossYears() {
        val startDate = LocalDate.of(2025, 12, 31)

        assertEquals(
            3L,
            calculator.amountForDate(
                date = LocalDate.of(2026, 1, 2),
                baseRate = 1L,
                accumulationStartDate = startDate
            )
        )
    }

    @Test
    fun fullYearPlanSupportsRegularAndLeapYears() {
        assertEquals(66_795L, calculator.fullYearPlan(year = 2025, baseRate = 1L))
        assertEquals(67_161L, calculator.fullYearPlan(year = 2024, baseRate = 1L))
    }

    @Test
    fun fullYearPlanSupportsFixedAmountMode() {
        assertEquals(
            3_650L,
            calculator.fullYearPlan(
                year = 2025,
                baseRate = 10L,
                amountMode = SavingsAmountMode.FIXED
            )
        )
        assertEquals(
            3_660L,
            calculator.fullYearPlan(
                year = 2024,
                baseRate = 10L,
                amountMode = SavingsAmountMode.FIXED
            )
        )
    }

    @Test
    fun lastDayOfMonthIsDetected() {
        assertTrue(calculator.isLastDayOfMonth(LocalDate.of(2026, 2, 28)))
        assertFalse(calculator.isLastDayOfMonth(LocalDate.of(2026, 2, 27)))
    }

    @Test
    fun monthlyPlannedTotalUsesRealDayOfYear() {
        val january2026 = YearMonth.of(2026, 1)
        assertEquals(496L, calculator.monthlyPlannedTotal(january2026, baseRate = 1L))
    }

    @Test
    fun plannedTotalUsesSelectedAccumulationPeriod() {
        assertEquals(
            12L,
            calculator.plannedTotal(
                startDate = LocalDate.of(2026, 1, 10),
                endDate = LocalDate.of(2026, 1, 12),
                baseRate = 2L
            )
        )
    }

    @Test
    fun plannedTotalStartsGrowthFromSelectedPeriodStart() {
        assertEquals(
            15L,
            calculator.plannedTotal(
                startDate = LocalDate.of(2026, 5, 6),
                endDate = LocalDate.of(2026, 5, 10),
                baseRate = 1L
            )
        )
    }

    @Test
    fun plannedTotalSupportsAccumulationPeriodAcrossYears() {
        assertEquals(
            6L,
            calculator.plannedTotal(
                startDate = LocalDate.of(2025, 12, 31),
                endDate = LocalDate.of(2026, 1, 2),
                baseRate = 1L
            )
        )
    }

    @Test
    fun forecastStartsFromTodayAndDoesNotCatchUpMissedPastDays() {
        val today = LocalDate.of(2026, 1, 3)

        val forecast = calculator.forecastToEndOfYear(
            today = today,
            baseRate = 1L,
            confirmedDates = emptySet(),
            confirmedBalance = 0L
        )

        assertEquals(66_792L, forecast)
    }

    @Test
    fun forecastDoesNotDoubleCountTodayWhenItIsAlreadyConfirmed() {
        val today = LocalDate.of(2026, 1, 3)

        val forecast = calculator.forecastToEndOfYear(
            today = today,
            baseRate = 1L,
            confirmedDates = setOf(today),
            confirmedBalance = 3L
        )

        assertEquals(66_792L, forecast)
    }

    @Test
    fun forecastExcludesAlreadyConfirmedFutureDates() {
        val today = LocalDate.of(2026, 1, 3)
        val futureConfirmedDate = LocalDate.of(2026, 1, 5)

        val forecast = calculator.forecastToEndOfYear(
            today = today,
            baseRate = 1L,
            confirmedDates = setOf(futureConfirmedDate),
            confirmedBalance = 5L
        )

        assertEquals(66_792L, forecast)
    }

    @Test
    fun forecastStartsFromAccumulationStartWhenTodayIsEarlier() {
        val forecast = calculator.forecastToEndOfPeriod(
            today = LocalDate.of(2026, 1, 3),
            baseRate = 1L,
            confirmedDates = emptySet(),
            confirmedBalance = 0L,
            accumulationStartDate = LocalDate.of(2026, 1, 10),
            accumulationEndDate = LocalDate.of(2026, 1, 12)
        )

        assertEquals(6L, forecast)
    }

    @Test
    fun forecastStopsAtAccumulationEndDate() {
        val forecast = calculator.forecastToEndOfPeriod(
            today = LocalDate.of(2026, 1, 3),
            baseRate = 1L,
            confirmedDates = emptySet(),
            confirmedBalance = 0L,
            accumulationStartDate = LocalDate.of(2026, 1, 1),
            accumulationEndDate = LocalDate.of(2026, 1, 5)
        )

        assertEquals(12L, forecast)
    }

    @Test
    fun forecastUsesDayNumberFromAccumulationStart() {
        val forecast = calculator.forecastToEndOfPeriod(
            today = LocalDate.of(2026, 5, 8),
            baseRate = 1L,
            confirmedDates = emptySet(),
            confirmedBalance = 0L,
            accumulationStartDate = LocalDate.of(2026, 5, 6),
            accumulationEndDate = LocalDate.of(2026, 5, 10)
        )

        assertEquals(12L, forecast)
    }

    @Test
    fun forecastSupportsAccumulationPeriodAcrossYears() {
        val forecast = calculator.forecastToEndOfPeriod(
            today = LocalDate.of(2026, 1, 1),
            baseRate = 1L,
            confirmedDates = emptySet(),
            confirmedBalance = 0L,
            accumulationStartDate = LocalDate.of(2025, 12, 31),
            accumulationEndDate = LocalDate.of(2026, 1, 2)
        )

        assertEquals(5L, forecast)
    }

    @Test
    fun forecastUsesFixedModeAndExcludesConfirmedFutureDates() {
        val futureConfirmedDate = LocalDate.of(2026, 1, 11)

        val forecast = calculator.forecastToEndOfPeriod(
            today = LocalDate.of(2026, 1, 10),
            baseRate = 10L,
            confirmedDates = setOf(futureConfirmedDate),
            confirmedBalance = 10L,
            amountMode = SavingsAmountMode.FIXED,
            accumulationStartDate = LocalDate.of(2026, 1, 1),
            accumulationEndDate = LocalDate.of(2026, 1, 12)
        )

        assertEquals(30L, forecast)
    }
}
