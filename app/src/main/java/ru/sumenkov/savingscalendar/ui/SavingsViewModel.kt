package ru.sumenkov.savingscalendar.ui

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import ru.sumenkov.savingscalendar.data.repository.SavingsRepository
import ru.sumenkov.savingscalendar.data.settings.AppSettings
import ru.sumenkov.savingscalendar.data.settings.SettingsRepository
import ru.sumenkov.savingscalendar.data.update.ApkUpdateInstaller
import ru.sumenkov.savingscalendar.data.update.UpdateRepository
import ru.sumenkov.savingscalendar.domain.SavingsAmountMode
import ru.sumenkov.savingscalendar.domain.SavingsCalculator
import ru.sumenkov.savingscalendar.notification.ReminderScheduler
import java.time.Duration
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.YearMonth

class SavingsViewModel(
    private val savingsRepository: SavingsRepository,
    private val settingsRepository: SettingsRepository,
    private val reminderScheduler: ReminderScheduler,
    private val updateRepository: UpdateRepository,
    private val apkUpdateInstaller: ApkUpdateInstaller,
    private val calculator: SavingsCalculator = SavingsCalculator()
) : ViewModel() {
    private val _state = MutableStateFlow(SavingsUiState())
    private val today = MutableStateFlow(LocalDate.now())
    private var lastUpdateCheckMillis = 0L
    val state: StateFlow<SavingsUiState> = _state

    init {
        observeState()
        refreshMonthlyReport()
        startTodayTicker()
        syncNotificationSchedule()
        checkForUpdates()
        startUpdateTicker()
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
                accumulationStartDate = settings.accumulationStartDate()
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
            refreshMonthlyReport()
        }
    }

    fun setAccumulationEndDate(date: LocalDate) {
        viewModelScope.launch {
            settingsRepository.setAccumulationEndDate(date)
            refreshMonthlyReport()
        }
    }

    fun setAmountMode(mode: SavingsAmountMode) {
        viewModelScope.launch {
            settingsRepository.setAmountMode(mode)
        }
    }

    fun plannedAmountFor(date: LocalDate): Long {
        val settings = _state.value.settings
        if (!settings.isDateInAccumulationPeriod(date)) return 0L

        return calculator.amountForDate(
            date = date,
            baseRate = settings.baseRate,
            amountMode = settings.amountMode,
            accumulationStartDate = settings.accumulationStartDate()
        )
    }

    fun dayNumberInPeriodFor(date: LocalDate): Int? {
        val settings = _state.value.settings
        if (!settings.isDateInAccumulationPeriod(date)) return null

        return calculator.dayNumberInPeriod(
            date = date,
            accumulationStartDate = settings.accumulationStartDate()
        )
    }

    fun syncNotificationSchedule() {
        viewModelScope.launch {
            syncNotificationSchedule(settingsRepository.settings.first())
        }
    }

    fun refreshToday() {
        val currentDate = LocalDate.now()
        today.value = currentDate
        refreshMonthlyReport(currentDate)
    }

    fun checkForUpdates(force: Boolean = false) {
        val now = System.currentTimeMillis()
        if (!force && now - lastUpdateCheckMillis < UPDATE_CHECK_INTERVAL_MILLIS) return
        if (_state.value.updateCheckInProgress || _state.value.updateDownloadInProgress) return

        lastUpdateCheckMillis = now
        viewModelScope.launch {
            _state.update { it.copy(updateCheckInProgress = true, updateErrorMessage = null) }
            runCatching { updateRepository.findAvailableUpdate() }
                .onSuccess { update ->
                    _state.update {
                        it.copy(
                            availableUpdate = update,
                            updateCheckInProgress = false
                        )
                    }
                }
                .onFailure {
                    _state.update { it.copy(updateCheckInProgress = false) }
                }
        }
    }

    fun checkForUpdatesAfterBackground(backgroundDurationMillis: Long) {
        if (backgroundDurationMillis < BACKGROUND_UPDATE_CHECK_DELAY_MILLIS) return

        checkForUpdates(force = true)
    }

    fun dismissUpdate() {
        if (_state.value.updateDownloadInProgress) return

        _state.update {
            it.copy(
                availableUpdate = null,
                updateErrorMessage = null,
                updateDownloadProgress = null
            )
        }
    }

    fun downloadUpdate(onReadyToInstall: (Uri) -> Unit) {
        val update = _state.value.availableUpdate ?: return
        if (_state.value.updateDownloadInProgress) return

        viewModelScope.launch {
            _state.update {
                it.copy(
                    updateDownloadInProgress = true,
                    updateDownloadProgress = null,
                    updateErrorMessage = null
                )
            }
            runCatching {
                apkUpdateInstaller.download(update) { progress ->
                    _state.update { current -> current.copy(updateDownloadProgress = progress) }
                }
            }
                .onSuccess { apkUri ->
                    _state.update {
                        it.copy(
                            updateDownloadInProgress = false,
                            updateDownloadProgress = null,
                            updateErrorMessage = null
                        )
                    }
                    onReadyToInstall(apkUri)
                }
                .onFailure {
                    _state.update {
                        it.copy(
                            updateDownloadInProgress = false,
                            updateDownloadProgress = null,
                            updateErrorMessage = "Не удалось скачать обновление. Проверьте интернет и попробуйте ещё раз."
                        )
                    }
                }
        }
    }

    private fun observeState() {
        viewModelScope.launch {
            combine(
                savingsRepository.observeAll(),
                settingsRepository.settings,
                today
            ) { entries, settings, today ->
                val periodEntries = entries.filter { settings.isDateInAccumulationPeriod(it.date) }
                val periodTotal = periodEntries.sumOf { it.amount }
                val todayDayNumberInPeriod = if (settings.isDateInAccumulationPeriod(today)) {
                    calculator.dayNumberInPeriod(
                        date = today,
                        accumulationStartDate = settings.accumulationStartDate()
                    )
                } else {
                    null
                }
                val todayAmount = if (settings.isDateInAccumulationPeriod(today)) {
                    calculator.amountForDate(
                        date = today,
                        baseRate = settings.baseRate,
                        amountMode = settings.amountMode,
                        accumulationStartDate = settings.accumulationStartDate()
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
                    periodTotal = periodTotal,
                    historyTotal = entries.sumOf { it.amount },
                    forecastToEndOfPeriod = calculator.forecastToEndOfPeriod(
                        today = today,
                        baseRate = settings.baseRate,
                        confirmedDates = periodEntries.map { it.date }.toSet(),
                        confirmedBalance = periodTotal,
                        amountMode = settings.amountMode,
                        accumulationStartDate = settings.accumulationStartDate(),
                        accumulationEndDate = settings.accumulationEndDate()
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

    private fun refreshMonthlyReport(date: LocalDate = today.value) {
        viewModelScope.launch {
            val settings = settingsRepository.settings.first()
            val report = savingsRepository.monthlyReport(
                yearMonth = YearMonth.from(date),
                accumulationStartDate = settings.accumulationStartDate(),
                accumulationEndDate = settings.accumulationEndDate()
            )
            _state.update { it.copy(monthlyReport = report) }
        }
    }

    private fun startTodayTicker() {
        viewModelScope.launch {
            while (isActive) {
                val currentDate = LocalDate.now()
                if (today.value != currentDate) {
                    today.value = currentDate
                    refreshMonthlyReport(currentDate)
                }
                delay(millisUntilNextDate())
            }
        }
    }

    private fun startUpdateTicker() {
        viewModelScope.launch {
            while (isActive) {
                delay(UPDATE_TIMER_INTERVAL_MILLIS)
                checkForUpdates(force = true)
            }
        }
    }

    private fun millisUntilNextDate(): Long {
        val now = LocalDateTime.now()
        val nextDate = now.toLocalDate().plusDays(1).atStartOfDay().plusSeconds(1)
        return maxOf(1_000L, Duration.between(now, nextDate).toMillis())
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

    private companion object {
        const val UPDATE_CHECK_INTERVAL_MILLIS = 60 * 60 * 1000L
        const val UPDATE_TIMER_INTERVAL_MILLIS = 60 * 60 * 1000L
        const val BACKGROUND_UPDATE_CHECK_DELAY_MILLIS = 15 * 60 * 1000L
    }
}

class SavingsViewModelFactory(
    private val savingsRepository: SavingsRepository,
    private val settingsRepository: SettingsRepository,
    private val reminderScheduler: ReminderScheduler,
    private val updateRepository: UpdateRepository,
    private val apkUpdateInstaller: ApkUpdateInstaller
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return SavingsViewModel(
            savingsRepository = savingsRepository,
            settingsRepository = settingsRepository,
            reminderScheduler = reminderScheduler,
            updateRepository = updateRepository,
            apkUpdateInstaller = apkUpdateInstaller
        ) as T
    }
}
