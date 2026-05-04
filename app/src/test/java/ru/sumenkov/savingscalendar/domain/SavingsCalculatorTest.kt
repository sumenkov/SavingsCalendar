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
    fun fullYearPlanSupportsRegularAndLeapYears() {
        assertEquals(66_795L, calculator.fullYearPlan(year = 2025, baseRate = 1L))
        assertEquals(67_161L, calculator.fullYearPlan(year = 2024, baseRate = 1L))
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
}
