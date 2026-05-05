package ru.sumenkov.savingscalendar.data.settings

import ru.sumenkov.savingscalendar.domain.SavingsAmountMode
import java.time.LocalDate
import java.time.MonthDay

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
    val accumulationStart: MonthDay = MonthDay.of(1, 1),
    val accumulationEnd: MonthDay = MonthDay.of(12, 31),
    val amountMode: SavingsAmountMode = SavingsAmountMode.DAILY_GROWTH
) {
    fun accumulationStartDate(year: Int): LocalDate {
        return accumulationStart.atYear(year)
    }

    fun accumulationEndDate(year: Int): LocalDate {
        return accumulationEnd.atYear(year)
    }

    fun isDateInAccumulationPeriod(date: LocalDate): Boolean {
        val startDate = accumulationStartDate(date.year)
        val endDate = accumulationEndDate(date.year)
        return !date.isBefore(startDate) && !date.isAfter(endDate)
    }
}
