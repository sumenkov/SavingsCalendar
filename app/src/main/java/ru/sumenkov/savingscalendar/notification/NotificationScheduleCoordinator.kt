package ru.sumenkov.savingscalendar.notification

import android.content.Context
import kotlinx.coroutines.flow.first
import ru.sumenkov.savingscalendar.data.settings.SettingsRepository

class NotificationScheduleCoordinator(
    context: Context
) {
    private val appContext = context.applicationContext
    private val settingsRepository = SettingsRepository(appContext)
    private val reminderScheduler = ReminderScheduler(appContext)

    suspend fun sync() {
        val settings = settingsRepository.settings.first()

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
