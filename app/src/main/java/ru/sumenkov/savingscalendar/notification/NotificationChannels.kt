package ru.sumenkov.savingscalendar.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import ru.sumenkov.savingscalendar.R

object NotificationChannels {
    const val DAILY_CHANNEL_ID = "daily_savings"
    const val MONTHLY_CHANNEL_ID = "monthly_reports"

    fun ensureCreated(context: Context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return

        val manager = context.getSystemService(NotificationManager::class.java)
        val channels = listOf(
            NotificationChannel(
                DAILY_CHANNEL_ID,
                context.getString(R.string.notification_channel_daily_name),
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = context.getString(R.string.notification_channel_daily_description)
            },
            NotificationChannel(
                MONTHLY_CHANNEL_ID,
                context.getString(R.string.notification_channel_monthly_name),
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = context.getString(R.string.notification_channel_monthly_description)
            }
        )
        manager.createNotificationChannels(channels)
    }
}
