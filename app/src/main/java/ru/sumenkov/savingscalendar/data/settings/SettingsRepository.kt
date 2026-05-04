package ru.sumenkov.savingscalendar.data.settings

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "settings")

class SettingsRepository(
    private val context: Context
) {
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
    }

    val settings: Flow<AppSettings> = context.dataStore.data.map { prefs ->
        AppSettings(
            baseRate = prefs[Keys.baseRate] ?: 1L,
            remindersEnabled = prefs[Keys.remindersEnabled] ?: true,
            reminderHour = prefs[Keys.reminderHour] ?: 20,
            reminderMinute = prefs[Keys.reminderMinute] ?: 0,
            monthlyReportsEnabled = prefs[Keys.monthlyReportsEnabled] ?: true,
            monthlyReportHour = prefs[Keys.monthlyReportHour] ?: 20,
            monthlyReportMinute = prefs[Keys.monthlyReportMinute] ?: 30,
            allowPastDays = prefs[Keys.allowPastDays] ?: true,
            currencySymbol = prefs[Keys.currencySymbol] ?: "₽"
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
}
