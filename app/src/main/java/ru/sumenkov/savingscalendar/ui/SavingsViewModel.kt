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
import ru.sumenkov.savingscalendar.domain.SavingsAmountMode
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
            if (!canConfirmDate(date, today, settings)) return@launch

            savingsRepository.confirmDate(
                date = date,
                baseRate = settings.baseRate,
                amountMode = settings.amountMode,
                accumulationStartDate = settings.accumulationStartDate(date.year)
            )
            refreshMonthlyReport()
        }
    }

    fun deleteDate(date: LocalDate) {
        viewModelScope.launch {
            savingsRepository.deleteDate(date)
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

    fun setAccumulationStartDate(date: LocalDate) {
        viewModelScope.launch {
            settingsRepository.setAccumulationStartDate(date)
        }
    }

    fun setAccumulationEndDate(date: LocalDate) {
        viewModelScope.launch {
            settingsRepository.setAccumulationEndDate(date)
        }
    }

    fun setAmountMode(mode: SavingsAmountMode) {
        viewModelScope.launch {
            settingsRepository.setAmountMode(mode)
        }
    }

    fun plannedAmountFor(date: LocalDate): Long {
        val settings = _state.value.settings
        return calculator.amountForDate(
            date = date,
            baseRate = settings.baseRate,
            amountMode = settings.amountMode,
            accumulationStartDate = settings.accumulationStartDate(date.year)
        )
    }

    fun dayNumberInPeriodFor(date: LocalDate): Int? {
        val settings = _state.value.settings
        if (!settings.isDateInAccumulationPeriod(date)) return null

        return calculator.dayNumberInPeriod(
            date = date,
            accumulationStartDate = settings.accumulationStartDate(date.year)
        )
    }

    fun syncNotificationSchedule() {
        viewModelScope.launch {
            syncNotificationSchedule(settingsRepository.settings.first())
        }
    }

    private fun observeState() {
        viewModelScope.launch {
            combine(
                savingsRepository.observeAll(),
                settingsRepository.settings
            ) { entries, settings ->
                val today = LocalDate.now()
                val currentYearEntries = entries.filter { it.date.year == today.year }
                val periodEntries = currentYearEntries.filter { settings.isDateInAccumulationPeriod(it.date) }
                val periodTotal = periodEntries.sumOf { it.amount }
                val todayDayNumberInPeriod = if (settings.isDateInAccumulationPeriod(today)) {
                    calculator.dayNumberInPeriod(
                        date = today,
                        accumulationStartDate = settings.accumulationStartDate(today.year)
                    )
                } else {
                    null
                }
                val todayAmount = if (settings.isDateInAccumulationPeriod(today)) {
                    calculator.amountForDate(
                        date = today,
                        baseRate = settings.baseRate,
                        amountMode = settings.amountMode,
                        accumulationStartDate = settings.accumulationStartDate(today.year)
                    )
                } else {
                    0L
                }
                val todayConfirmed = entries.any { it.date == today }
                SavingsUiState(
                    today = today,
                    todayAmount = todayAmount,
                    todayDayNumberInPeriod = todayDayNumberInPeriod,
                    todayConfirmed = todayConfirmed,
                    yearTotal = periodTotal,
                    historyTotal = entries.sumOf { it.amount },
                    forecastToEndOfPeriod = calculator.forecastToEndOfPeriod(
                        today = today,
                        baseRate = settings.baseRate,
                        confirmedDates = periodEntries.map { it.date }.toSet(),
                        confirmedBalance = periodTotal,
                        amountMode = settings.amountMode,
                        accumulationStartDate = settings.accumulationStartDate(today.year),
                        accumulationEndDate = settings.accumulationEndDate(today.year)
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

    private fun canConfirmDate(
        date: LocalDate,
        today: LocalDate,
        settings: AppSettings
    ): Boolean {
        if (date.isBefore(today.minusYears(1)) || date.isAfter(today.plusYears(1))) return false
        if (!settings.isDateInAccumulationPeriod(date)) return false

        return !date.isBefore(today) || settings.allowPastDays
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
