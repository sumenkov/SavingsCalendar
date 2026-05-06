package ru.sumenkov.savingscalendar.data.settings

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

class AppSettingsTest {
    @Test
    fun accumulationPeriodKeepsSelectedYears() {
        val settings = AppSettings(
            accumulationStart = LocalDate.of(2025, 5, 6),
            accumulationEnd = LocalDate.of(2027, 5, 10)
        )

        assertEquals(LocalDate.of(2025, 5, 6), settings.accumulationStartDate())
        assertEquals(LocalDate.of(2027, 5, 10), settings.accumulationEndDate())
        assertTrue(settings.isDateInAccumulationPeriod(LocalDate.of(2026, 5, 6)))
        assertFalse(settings.isDateInAccumulationPeriod(LocalDate.of(2027, 5, 11)))
    }
}
