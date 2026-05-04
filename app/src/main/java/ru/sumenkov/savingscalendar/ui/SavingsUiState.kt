package ru.sumenkov.savingscalendar.ui

import ru.sumenkov.savingscalendar.data.db.SavingsEntry
import ru.sumenkov.savingscalendar.data.settings.AppSettings
import ru.sumenkov.savingscalendar.domain.MonthlyReport
import java.time.LocalDate

data class SavingsUiState(
    val today: LocalDate = LocalDate.now(),
    val todayAmount: Long = 0L,
    val todayConfirmed: Boolean = false,
    val yearTotal: Long = 0L,
    val forecastToEndOfYear: Long = 0L,
    val entries: List<SavingsEntry> = emptyList(),
    val settings: AppSettings = AppSettings(),
    val monthlyReport: MonthlyReport? = null,
    val isLoading: Boolean = true
)
