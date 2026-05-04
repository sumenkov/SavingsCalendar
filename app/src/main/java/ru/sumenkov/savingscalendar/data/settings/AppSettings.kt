package ru.sumenkov.savingscalendar.data.settings

data class AppSettings(
    val baseRate: Long = 1L,
    val remindersEnabled: Boolean = true,
    val reminderHour: Int = 20,
    val reminderMinute: Int = 0,
    val monthlyReportsEnabled: Boolean = true,
    val monthlyReportHour: Int = 20,
    val monthlyReportMinute: Int = 30,
    val allowPastDays: Boolean = true,
    val currencySymbol: String = "₽"
)
