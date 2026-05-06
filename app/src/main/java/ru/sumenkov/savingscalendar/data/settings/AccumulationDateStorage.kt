package ru.sumenkov.savingscalendar.data.settings

import java.time.LocalDate
import java.time.MonthDay

internal data class StoredAccumulationDate(
    val date: LocalDate,
    val shouldPersist: Boolean
)

internal fun parseStoredAccumulationDate(
    value: String?,
    fallback: LocalDate,
    migrationYear: Int
): StoredAccumulationDate {
    if (value == null) return StoredAccumulationDate(fallback, shouldPersist = false)

    val fullDate = runCatching { LocalDate.parse(value) }.getOrNull()
    if (fullDate != null) return StoredAccumulationDate(fullDate, shouldPersist = false)

    val monthDay = runCatching { MonthDay.parse(value) }.getOrNull()
    if (monthDay != null) {
        return StoredAccumulationDate(monthDay.atYear(migrationYear), shouldPersist = true)
    }

    return StoredAccumulationDate(fallback, shouldPersist = true)
}
