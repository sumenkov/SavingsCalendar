package ru.sumenkov.savingscalendar

import android.app.Application
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import ru.sumenkov.savingscalendar.notification.NotificationChannels
import ru.sumenkov.savingscalendar.notification.NotificationScheduleCoordinator

class SavingsCalendarApplication : Application() {
    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onCreate() {
        super.onCreate()
        NotificationChannels.ensureCreated(this)
        applicationScope.launch {
            NotificationScheduleCoordinator(this@SavingsCalendarApplication).sync()
        }
    }
}
