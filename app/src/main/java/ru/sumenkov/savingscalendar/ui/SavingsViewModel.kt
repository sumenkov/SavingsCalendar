package ru.sumenkov.savingscalendar.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import ru.sumenkov.savingscalendar.data.repository.SavingsRepository
import ru.sumenkov.savingscalendar.data.settings.AppSettings
import ru.sumenkov.savingscalendar.data.settings.SettingsRepository
import ru.sumenkov.savingscalendar.domain.SavingsCalculator
import ru.sumenkov.savingscalendar.notification.ReminderScheduler
import java.time.LocalDate
import java.time.YearMonth

class SavingsViewModel(
    private val savingsRepository: SavingsRepository,
    private val settingsRepository: SettingsRepository,
    private val reminderScheduler: ReminderScheduler,
    private val calculator: SavingsCalculator = SavingsCalculator()
) : ViewModel() {
    private val _state = MutableStateFlow(SavingsUiState())
    val state: StateFlow<SavingsUiState> = _state

    init {
        observeState()
        refreshMonthlyReport()
        syncNotificationSchedule()
    }

    fun confirmToday() {
        confirmDate(LocalDate.now())
    }

    fun confirmDate(date: LocalDate) {
        viewModelScope.launch {
            val settings = settingsRepository.settings.first()
            val today = LocalDate.now()
            val canConfirm = date == today || (date.isBefore(today) && settings.allowPastDays)
            if (!canConfirm || date.isAfter(today)) return@launch

            savingsRepository.confirmDate(date, settings.baseRate)
            refreshMonthlyReport()
        }
    }

    fun updateBaseRate(value: Long) {
        viewModelScope.launch {
            settingsRepository.setBaseRate(value)
        }
    }

    fun setRemindersEnabled(enabled: Boolean) {
        viewModelScope.launch {
            settingsRepository.setRemindersEnabled(enabled)
            syncNotificationSchedule(settingsRepository.settings.first())
        }
    }

    fun setMonthlyReportsEnabled(enabled: Boolean) {
        viewModelScope.launch {
            settingsRepository.setMonthlyReportsEnabled(enabled)
            syncNotificationSchedule(settingsRepository.settings.first())
        }
    }

    fun setReminderTime(hour: Int, minute: Int) {
        viewModelScope.launch {
            settingsRepository.setReminderTime(hour, minute)
            syncNotificationSchedule(settingsRepository.settings.first())
        }
    }

    fun setMonthlyReportTime(hour: Int, minute: Int) {
        viewModelScope.launch {
            settingsRepository.setMonthlyReportTime(hour, minute)
            syncNotificationSchedule(settingsRepository.settings.first())
        }
    }

    fun setAllowPastDays(enabled: Boolean) {
        viewModelScope.launch {
            settingsRepository.setAllowPastDays(enabled)
        }
    }

    fun setCurrencySymbol(value: String) {
        viewModelScope.launch {
            settingsRepository.setCurrencySymbol(value)
        }
    }

    fun plannedAmountFor(date: LocalDate): Long {
        return calculator.amountForDay(date.dayOfYear, _state.value.settings.baseRate)
    }

    fun syncNotificationSchedule() {
        viewModelScope.launch {
            syncNotificationSchedule(settingsRepository.settings.first())
        }
    }

    private fun observeState() {
        viewModelScope.launch {
            combine(
                savingsRepository.observeByYear(LocalDate.now().year),
                savingsRepository.observeYearTotal(LocalDate.now().year),
                settingsRepository.settings
            ) { entries, yearTotal, settings ->
                val today = LocalDate.now()
                val todayAmount = calculator.amountForDay(today.dayOfYear, settings.baseRate)
                val todayConfirmed = entries.any { it.date == today }
                SavingsUiState(
                    today = today,
                    todayAmount = todayAmount,
                    todayConfirmed = todayConfirmed,
                    yearTotal = yearTotal,
                    forecastToEndOfYear = calculator.forecastToEndOfYear(
                        today = today,
                        baseRate = settings.baseRate,
                        confirmedDates = entries.map { it.date }.toSet(),
                        confirmedBalance = yearTotal
                    ),
                    entries = entries.sortedByDescending { it.date },
                    settings = settings,
                    monthlyReport = _state.value.monthlyReport,
                    isLoading = false
                )
            }.collect { next ->
                _state.value = next
            }
        }
    }

    private fun refreshMonthlyReport() {
        viewModelScope.launch {
            val report = savingsRepository.monthlyReport(YearMonth.now())
            _state.update { it.copy(monthlyReport = report) }
        }
    }

    private fun syncNotificationSchedule(settings: AppSettings) {
        if (settings.remindersEnabled) {
            reminderScheduler.scheduleDaily(settings.reminderHour, settings.reminderMinute)
        } else {
            reminderScheduler.cancelDaily()
        }

        if (settings.monthlyReportsEnabled) {
            reminderScheduler.scheduleMonthlyReport(
                settings.monthlyReportHour,
                settings.monthlyReportMinute
            )
        } else {
            reminderScheduler.cancelMonthlyReport()
        }
    }
}

class SavingsViewModelFactory(
    private val savingsRepository: SavingsRepository,
    private val settingsRepository: SettingsRepository,
    private val reminderScheduler: ReminderScheduler
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return SavingsViewModel(
            savingsRepository = savingsRepository,
            settingsRepository = settingsRepository,
            reminderScheduler = reminderScheduler
        ) as T
    }
}
