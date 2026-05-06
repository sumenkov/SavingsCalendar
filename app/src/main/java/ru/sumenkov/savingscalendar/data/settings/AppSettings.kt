package ru.sumenkov.savingscalendar.data.settings

import ru.sumenkov.savingscalendar.domain.SavingsAmountMode
import java.time.LocalDate

data class AppSettings(
    val baseRate: Long = 1L,
    val remindersEnabled: Boolean = true,
    val reminderHour: Int = 20,
    val reminderMinute: Int = 0,
    val monthlyReportsEnabled: Boolean = true,
    val monthlyReportHour: Int = 20,
    val monthlyReportMinute: Int = 30,
    val allowPastDays: Boolean = true,
    val currencySymbol: String = "₽",
    val accumulationStart: LocalDate = LocalDate.of(LocalDate.now().year, 1, 1),
    val accumulationEnd: LocalDate = LocalDate.of(LocalDate.now().year, 12, 31),
    val amountMode: SavingsAmountMode = SavingsAmountMode.DAILY_GROWTH
) {
    fun accumulationStartDate(): LocalDate {
        return accumulationStart
    }

    fun accumulationEndDate(): LocalDate {
        return accumulationEnd
    }

    fun isDateInAccumulationPeriod(date: LocalDate): Boolean {
        return !date.isBefore(accumulationStart) && !date.isAfter(accumulationEnd)
    }
}
