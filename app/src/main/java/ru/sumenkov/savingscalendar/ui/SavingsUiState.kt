package ru.sumenkov.savingscalendar.ui

import ru.sumenkov.savingscalendar.data.db.SavingsEntry
import ru.sumenkov.savingscalendar.data.settings.AppSettings
import ru.sumenkov.savingscalendar.data.update.AppUpdateInfo
import ru.sumenkov.savingscalendar.domain.MonthlyReport
import java.time.LocalDate

data class SavingsUiState(
    val today: LocalDate = LocalDate.now(),
    val todayAmount: Long = 0L,
    val todayDayNumberInPeriod: Int? = null,
    val todayConfirmed: Boolean = false,
    val periodTotal: Long = 0L,
    val historyTotal: Long = 0L,
    val forecastToEndOfPeriod: Long = 0L,
    val entries: List<SavingsEntry> = emptyList(),
    val settings: AppSettings = AppSettings(),
    val monthlyReport: MonthlyReport? = null,
    val availableUpdate: AppUpdateInfo? = null,
    val updateCheckInProgress: Boolean = false,
    val updateDownloadInProgress: Boolean = false,
    val updateDownloadProgress: Int? = null,
    val updateErrorMessage: String? = null,
    val isLoading: Boolean = true
)
