package ru.sumenkov.savingscalendar.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build

object NotificationChannels {
    const val DAILY_CHANNEL_ID = "daily_savings"
    const val MONTHLY_CHANNEL_ID = "monthly_reports"

    fun ensureCreated(context: Context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return

        val manager = context.getSystemService(NotificationManager::class.java)
        val channels = listOf(
            NotificationChannel(
                DAILY_CHANNEL_ID,
                "Ежедневные напоминания",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Напоминания о ежедневном взносе"
            },
            NotificationChannel(
                MONTHLY_CHANNEL_ID,
                "Месячные отчёты",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Ободряющие итоги последнего дня месяца"
            }
        )
        manager.createNotificationChannels(channels)
    }
}
