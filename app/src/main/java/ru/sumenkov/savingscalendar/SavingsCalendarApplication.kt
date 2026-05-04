package ru.sumenkov.savingscalendar

import android.app.Application
import ru.sumenkov.savingscalendar.notification.NotificationChannels

class SavingsCalendarApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        NotificationChannels.ensureCreated(this)
    }
}
