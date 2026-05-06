package ru.sumenkov.savingscalendar.data.settings

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import ru.sumenkov.savingscalendar.domain.SavingsAmountMode
import java.time.LocalDate

private val Context.dataStore by preferencesDataStore(name = "settings")

class SettingsRepository(
    private val context: Context
) {
    @Volatile
    private var accumulationPeriodMigrationChecked = false

    private object Keys {
        val baseRate = longPreferencesKey("base_rate")
        val remindersEnabled = booleanPreferencesKey("reminders_enabled")
        val reminderHour = intPreferencesKey("reminder_hour")
        val reminderMinute = intPreferencesKey("reminder_minute")
        val monthlyReportsEnabled = booleanPreferencesKey("monthly_reports_enabled")
        val monthlyReportHour = intPreferencesKey("monthly_report_hour")
        val monthlyReportMinute = intPreferencesKey("monthly_report_minute")
        val allowPastDays = booleanPreferencesKey("allow_past_days")
        val currencySymbol = stringPreferencesKey("currency_symbol")
        val accumulationStart = stringPreferencesKey("accumulation_start")
        val accumulationEnd = stringPreferencesKey("accumulation_end")
        val amountMode = stringPreferencesKey("amount_mode")
    }

    val settings: Flow<AppSettings> = flow {
        migrateAccumulationPeriodDatesIfNeeded()
        emitAll(context.dataStore.data.map { prefs ->
            prefs.toAppSettings()
        })
    }.distinctUntilChanged()

    private fun androidx.datastore.preferences.core.Preferences.toAppSettings(): AppSettings {
        val defaultYear = LocalDate.now().year
        val defaultStart = LocalDate.of(defaultYear, 1, 1)
        val defaultEnd = LocalDate.of(defaultYear, 12, 31)
        val accumulationStart = parseStoredAccumulationDate(
            value = this[Keys.accumulationStart],
            fallback = defaultStart,
            migrationYear = defaultYear
        ).date
        val rawAccumulationEnd = parseStoredAccumulationDate(
            value = this[Keys.accumulationEnd],
            fallback = defaultEnd,
            migrationYear = defaultYear
        ).date
        return AppSettings(
            baseRate = this[Keys.baseRate] ?: 1L,
            remindersEnabled = this[Keys.remindersEnabled] ?: true,
            reminderHour = this[Keys.reminderHour] ?: 20,
            reminderMinute = this[Keys.reminderMinute] ?: 0,
            monthlyReportsEnabled = this[Keys.monthlyReportsEnabled] ?: true,
            monthlyReportHour = this[Keys.monthlyReportHour] ?: 20,
            monthlyReportMinute = this[Keys.monthlyReportMinute] ?: 30,
            allowPastDays = this[Keys.allowPastDays] ?: true,
            currencySymbol = this[Keys.currencySymbol] ?: "₽",
            accumulationStart = accumulationStart,
            accumulationEnd = if (rawAccumulationEnd.isBefore(accumulationStart)) {
                accumulationStart
            } else {
                rawAccumulationEnd
            },
            amountMode = parseAmountMode(this[Keys.amountMode])
        )
    }

    suspend fun setBaseRate(value: Long) {
        context.dataStore.edit { prefs -> prefs[Keys.baseRate] = value.coerceAtLeast(0L) }
    }

    suspend fun setReminderTime(hour: Int, minute: Int) {
        context.dataStore.edit { prefs ->
            prefs[Keys.reminderHour] = hour.coerceIn(0, 23)
            prefs[Keys.reminderMinute] = minute.coerceIn(0, 59)
        }
    }

    suspend fun setMonthlyReportTime(hour: Int, minute: Int) {
        context.dataStore.edit { prefs ->
            prefs[Keys.monthlyReportHour] = hour.coerceIn(0, 23)
            prefs[Keys.monthlyReportMinute] = minute.coerceIn(0, 59)
        }
    }

    suspend fun setRemindersEnabled(enabled: Boolean) {
        context.dataStore.edit { prefs -> prefs[Keys.remindersEnabled] = enabled }
    }

    suspend fun setMonthlyReportsEnabled(enabled: Boolean) {
        context.dataStore.edit { prefs -> prefs[Keys.monthlyReportsEnabled] = enabled }
    }

    suspend fun setAllowPastDays(enabled: Boolean) {
        context.dataStore.edit { prefs -> prefs[Keys.allowPastDays] = enabled }
    }

    suspend fun setCurrencySymbol(value: String) {
        val normalized = value.trim().ifBlank { "₽" }.take(4)
        context.dataStore.edit { prefs -> prefs[Keys.currencySymbol] = normalized }
    }

    suspend fun setAccumulationStartDate(date: LocalDate) {
        context.dataStore.edit { prefs ->
            val currentEnd = parseStoredAccumulationDate(
                value = prefs[Keys.accumulationEnd],
                fallback = defaultEndDate(),
                migrationYear = LocalDate.now().year
            ).date
            prefs[Keys.accumulationStart] = date.toString()
            if (currentEnd.isBefore(date)) {
                prefs[Keys.accumulationEnd] = date.toString()
            }
        }
    }

    suspend fun setAccumulationEndDate(date: LocalDate) {
        context.dataStore.edit { prefs ->
            val currentStart = parseStoredAccumulationDate(
                value = prefs[Keys.accumulationStart],
                fallback = defaultStartDate(),
                migrationYear = LocalDate.now().year
            ).date
            if (currentStart.isAfter(date)) {
                prefs[Keys.accumulationStart] = date.toString()
            }
            prefs[Keys.accumulationEnd] = date.toString()
        }
    }

    suspend fun setAmountMode(mode: SavingsAmountMode) {
        context.dataStore.edit { prefs -> prefs[Keys.amountMode] = mode.name }
    }

    private suspend fun migrateAccumulationPeriodDatesIfNeeded() {
        if (accumulationPeriodMigrationChecked) return

        context.dataStore.edit { prefs ->
            val migrationYear = LocalDate.now().year
            val start = parseStoredAccumulationDate(
                value = prefs[Keys.accumulationStart],
                fallback = LocalDate.of(migrationYear, 1, 1),
                migrationYear = migrationYear
            )
            val end = parseStoredAccumulationDate(
                value = prefs[Keys.accumulationEnd],
                fallback = LocalDate.of(migrationYear, 12, 31),
                migrationYear = migrationYear
            )

            if (start.shouldPersist) {
                prefs[Keys.accumulationStart] = start.date.toString()
            }

            val normalizedEnd = if (end.date.isBefore(start.date)) start.date else end.date
            if (end.shouldPersist || normalizedEnd != end.date) {
                prefs[Keys.accumulationEnd] = normalizedEnd.toString()
            }
        }

        accumulationPeriodMigrationChecked = true
    }

    private fun parseAmountMode(value: String?): SavingsAmountMode {
        return value
            ?.let { stored -> SavingsAmountMode.entries.firstOrNull { it.name == stored } }
            ?: SavingsAmountMode.DAILY_GROWTH
    }

    private fun defaultStartDate(): LocalDate {
        return LocalDate.of(LocalDate.now().year, 1, 1)
    }

    private fun defaultEndDate(): LocalDate {
        return LocalDate.of(LocalDate.now().year, 12, 31)
    }
}
