package ru.sumenkov.savingscalendar.notification

import android.Manifest
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import ru.sumenkov.savingscalendar.MainActivity
import ru.sumenkov.savingscalendar.R
import ru.sumenkov.savingscalendar.data.settings.SettingsRepository
import ru.sumenkov.savingscalendar.domain.SavingsCalculator
import java.time.LocalDate

class DailyReminderReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val pendingResult = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val settingsRepository = SettingsRepository(context.applicationContext)
                val settings = settingsRepository.settings.first()
                val today = LocalDate.now()
                val amount = SavingsCalculator().amountForDay(today.dayOfYear, settings.baseRate)

                val openIntent = PendingIntent.getActivity(
                    context,
                    0,
                    Intent(context, MainActivity::class.java),
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )

                val notification = NotificationCompat.Builder(context, NotificationChannels.DAILY_CHANNEL_ID)
                    .setSmallIcon(R.drawable.ic_notification)
                    .setContentTitle("Сегодня день №${today.dayOfYear}")
                    .setContentText("Отложите $amount ${settings.currencySymbol} в накопления.")
                    .setContentIntent(openIntent)
                    .setAutoCancel(true)
                    .build()

                val canNotify = android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.TIRAMISU ||
                    ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED
                if (canNotify) {
                    NotificationManagerCompat.from(context).notify(DAILY_NOTIFICATION_ID, notification)
                }

                if (settings.remindersEnabled) {
                    ReminderScheduler(context.applicationContext).scheduleDaily(
                        settings.reminderHour,
                        settings.reminderMinute
                    )
                }
            } finally {
                pendingResult.finish()
            }
        }
    }

    companion object {
        private const val DAILY_NOTIFICATION_ID = 2001
    }
}
