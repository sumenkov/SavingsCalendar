package ru.sumenkov.savingscalendar.notification

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId

class ReminderScheduler(
    private val context: Context
) {
    private val alarmManager: AlarmManager = context.getSystemService(AlarmManager::class.java)

    fun scheduleDaily(hour: Int, minute: Int) {
        val triggerAt = nextDateTime(hour, minute)
        schedule(
            requestCode = DAILY_REQUEST_CODE,
            intent = Intent(context, DailyReminderReceiver::class.java),
            triggerAtMillis = triggerAt.toMillis()
        )
    }

    fun scheduleMonthlyReport(hour: Int, minute: Int) {
        val now = LocalDateTime.now()
        var date = LocalDate.now().withDayOfMonth(LocalDate.now().lengthOfMonth())
        var triggerAt = LocalDateTime.of(date, LocalTime.of(hour, minute))
        if (!triggerAt.isAfter(now)) {
            val nextMonth = LocalDate.now().plusMonths(1)
            date = nextMonth.withDayOfMonth(nextMonth.lengthOfMonth())
            triggerAt = LocalDateTime.of(date, LocalTime.of(hour, minute))
        }

        schedule(
            requestCode = MONTHLY_REQUEST_CODE,
            intent = Intent(context, MonthlyReportReceiver::class.java),
            triggerAtMillis = triggerAt.toMillis()
        )
    }

    fun cancelDaily() {
        alarmManager.cancel(pendingIntent(DAILY_REQUEST_CODE, Intent(context, DailyReminderReceiver::class.java)))
    }

    fun cancelMonthlyReport() {
        alarmManager.cancel(pendingIntent(MONTHLY_REQUEST_CODE, Intent(context, MonthlyReportReceiver::class.java)))
    }

    private fun schedule(requestCode: Int, intent: Intent, triggerAtMillis: Long) {
        val pendingIntent = pendingIntent(requestCode, intent)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && !alarmManager.canScheduleExactAlarms()) {
            alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAtMillis, pendingIntent)
        } else {
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAtMillis, pendingIntent)
        }
    }

    private fun pendingIntent(requestCode: Int, intent: Intent): PendingIntent {
        return PendingIntent.getBroadcast(
            context,
            requestCode,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    private fun nextDateTime(hour: Int, minute: Int): LocalDateTime {
        val now = LocalDateTime.now()
        var target = LocalDateTime.of(LocalDate.now(), LocalTime.of(hour, minute))
        if (!target.isAfter(now)) target = target.plusDays(1)
        return target
    }

    private fun LocalDateTime.toMillis(): Long {
        return atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
    }

    companion object {
        private const val DAILY_REQUEST_CODE = 1001
        private const val MONTHLY_REQUEST_CODE = 1002
    }
}
