package ru.sumenkov.savingscalendar.notification

import android.Manifest
import android.app.PendingIntent
import android.content.pm.PackageManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import ru.sumenkov.savingscalendar.MainActivity
import ru.sumenkov.savingscalendar.R
import ru.sumenkov.savingscalendar.data.db.AppDatabase
import ru.sumenkov.savingscalendar.data.repository.SavingsRepository
import ru.sumenkov.savingscalendar.data.settings.SettingsRepository
import java.time.YearMonth
import java.time.format.TextStyle
import java.util.Locale

class MonthlyReportReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val pendingResult = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val settingsRepository = SettingsRepository(context.applicationContext)
                val settings = settingsRepository.settings.first()
                val repository = SavingsRepository(AppDatabase.get(context).savingsDao())
                val yearMonth = YearMonth.now()
                val report = repository.monthlyReport(yearMonth)
                val monthName = yearMonth.month.getDisplayName(TextStyle.FULL_STANDALONE, Locale("ru"))

                val openIntent = PendingIntent.getActivity(
                    context,
                    0,
                    Intent(context, MainActivity::class.java),
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )

                val notification = NotificationCompat.Builder(context, NotificationChannels.MONTHLY_CHANNEL_ID)
                    .setSmallIcon(R.drawable.ic_notification_small)
                    .setContentTitle("Итоги месяца: $monthName")
                    .setContentText("За месяц: ${report.monthTotal} ${settings.currencySymbol}, всего за год: ${report.yearTotal} ${settings.currencySymbol}.")
                    .setStyle(
                        NotificationCompat.BigTextStyle().bigText(
                            "За месяц внесено ${report.monthTotal} ${settings.currencySymbol}. " +
                                "Всего с начала года: ${report.yearTotal} ${settings.currencySymbol}. " +
                                "Отмечено дней: ${report.completedDaysInMonth} из ${report.daysInMonth}."
                        )
                    )
                    .setContentIntent(openIntent)
                    .setAutoCancel(true)
                    .build()

                val canNotify = android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.TIRAMISU ||
                    ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED
                if (canNotify) {
                    NotificationManagerCompat.from(context).notify(MONTHLY_NOTIFICATION_ID, notification)
                }

                if (settings.monthlyReportsEnabled) {
                    ReminderScheduler(context.applicationContext).scheduleMonthlyReport(
                        settings.monthlyReportHour,
                        settings.monthlyReportMinute
                    )
                }
            } finally {
                pendingResult.finish()
            }
        }
    }

    companion object {
        private const val MONTHLY_NOTIFICATION_ID = 2002
    }
}
