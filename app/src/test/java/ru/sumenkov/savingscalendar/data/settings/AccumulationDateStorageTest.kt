package ru.sumenkov.savingscalendar.data.settings

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

class AccumulationDateStorageTest {
    @Test
    fun parsesFullDateWithoutMigration() {
        val parsed = parseStoredAccumulationDate(
            value = "2026-05-06",
            fallback = LocalDate.of(2026, 1, 1),
            migrationYear = 2026
        )

        assertEquals(LocalDate.of(2026, 5, 6), parsed.date)
        assertFalse(parsed.shouldPersist)
    }

    @Test
    fun parsesOldMonthDayAndMarksForMigration() {
        val parsed = parseStoredAccumulationDate(
            value = "--05-06",
            fallback = LocalDate.of(2026, 1, 1),
            migrationYear = 2026
        )

        assertEquals(LocalDate.of(2026, 5, 6), parsed.date)
        assertTrue(parsed.shouldPersist)
    }

    @Test
    fun invalidStoredValueFallsBackAndMarksForMigration() {
        val fallback = LocalDate.of(2026, 1, 1)
        val parsed = parseStoredAccumulationDate(
            value = "bad-date",
            fallback = fallback,
            migrationYear = 2026
        )

        assertEquals(fallback, parsed.date)
        assertTrue(parsed.shouldPersist)
    }
}
