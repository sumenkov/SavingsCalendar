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
    }

    fun confirmToday() {
        viewModelScope.launch {
            val settings = settingsRepository.settings.first()
            savingsRepository.confirmDate(LocalDate.now(), settings.baseRate)
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
            val settings = settingsRepository.settings.first()
            if (enabled) {
                reminderScheduler.scheduleDaily(settings.reminderHour, settings.reminderMinute)
            } else {
                reminderScheduler.cancelDaily()
            }
        }
    }

    fun setMonthlyReportsEnabled(enabled: Boolean) {
        viewModelScope.launch {
            settingsRepository.setMonthlyReportsEnabled(enabled)
            val settings = settingsRepository.settings.first()
            if (enabled) {
                reminderScheduler.scheduleMonthlyReport(settings.monthlyReportHour, settings.monthlyReportMinute)
            } else {
                reminderScheduler.cancelMonthlyReport()
            }
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
                val forecastStartDate = if (todayConfirmed) today.plusDays(1) else today
                val futurePlan = if (forecastStartDate.year == today.year) {
                    calculator.planFromDateToEndOfYear(forecastStartDate, settings.baseRate)
                } else {
                    0L
                }
                SavingsUiState(
                    today = today,
                    todayAmount = todayAmount,
                    todayConfirmed = todayConfirmed,
                    yearTotal = yearTotal,
                    forecastToEndOfYear = yearTotal + futurePlan,
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
