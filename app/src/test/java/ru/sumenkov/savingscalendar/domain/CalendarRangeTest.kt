package ru.sumenkov.savingscalendar.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate
import java.time.YearMonth

class CalendarRangeTest {
    @Test
    fun rangeStartsOneYearBeforeCurrentDateMonth() {
        val range = calendarRangeAround(LocalDate.of(2026, 5, 5))

        assertEquals(YearMonth.of(2025, 5), range.minMonth)
    }

    @Test
    fun rangeEndsOneYearAfterCurrentDateMonth() {
        val range = calendarRangeAround(LocalDate.of(2026, 5, 5))

        assertEquals(YearMonth.of(2027, 5), range.maxMonth)
    }

    @Test
    fun navigationAllowsMonthsInsideRangeOnly() {
        val range = calendarRangeAround(LocalDate.of(2026, 5, 5))

        assertFalse(range.canGoPrevious(YearMonth.of(2025, 5)))
        assertTrue(range.canGoPrevious(YearMonth.of(2025, 6)))
        assertTrue(range.canGoNext(YearMonth.of(2027, 4)))
        assertFalse(range.canGoNext(YearMonth.of(2027, 5)))
    }

    @Test
    fun rangeIncludesSelectedAccumulationPeriodWhenItIsWiderThanDefaultWindow() {
        val range = calendarRangeForPeriod(
            today = LocalDate.of(2026, 5, 5),
            accumulationStartDate = LocalDate.of(2024, 12, 31),
            accumulationEndDate = LocalDate.of(2028, 1, 1)
        )

        assertEquals(YearMonth.of(2024, 12), range.minMonth)
        assertEquals(YearMonth.of(2028, 1), range.maxMonth)
    }
}
