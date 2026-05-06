package ru.sumenkov.savingscalendar.data.repository

import kotlinx.coroutines.flow.Flow
import ru.sumenkov.savingscalendar.data.db.SavingsDao
import ru.sumenkov.savingscalendar.data.db.SavingsEntry
import ru.sumenkov.savingscalendar.domain.MonthlyReport
import ru.sumenkov.savingscalendar.domain.SavingsAmountMode
import ru.sumenkov.savingscalendar.domain.SavingsCalculator
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.YearMonth
import java.time.temporal.ChronoUnit

class SavingsRepository(
    private val dao: SavingsDao,
    private val calculator: SavingsCalculator = SavingsCalculator()
) {
    fun observeAll(): Flow<List<SavingsEntry>> = dao.observeAll()

    fun observeByYear(year: Int): Flow<List<SavingsEntry>> = dao.observeByYear(year)

    fun observeYearTotal(year: Int): Flow<Long> = dao.observeYearTotal(year)

    suspend fun confirmDate(
        date: LocalDate,
        baseRate: Long,
        amountMode: SavingsAmountMode = SavingsAmountMode.DAILY_GROWTH,
        accumulationStartDate: LocalDate = LocalDate.of(date.year, 1, 1)
    ) {
        val amount = calculator.amountForDate(
            date = date,
            baseRate = baseRate,
            amountMode = amountMode,
            accumulationStartDate = accumulationStartDate
        )
        dao.upsert(
            SavingsEntry(
                date = date,
                year = date.year,
                baseRate = baseRate,
                amount = amount,
                confirmedAt = LocalDateTime.now()
            )
        )
    }

    suspend fun isConfirmed(date: LocalDate): Boolean {
        return dao.findByDate(date) != null
    }

    suspend fun deleteDate(date: LocalDate) {
        dao.findByDate(date)?.let { entry ->
            dao.delete(entry)
        }
    }

    suspend fun monthlyReport(
        yearMonth: YearMonth,
        accumulationStartDate: LocalDate,
        accumulationEndDate: LocalDate
    ): MonthlyReport {
        require(!accumulationEndDate.isBefore(accumulationStartDate)) {
            "accumulationEndDate must be on or after accumulationStartDate"
        }

        val monthStart = yearMonth.atDay(1)
        val monthEnd = yearMonth.atEndOfMonth()
        val reportStart = maxOf(monthStart, accumulationStartDate)
        val reportEnd = minOf(monthEnd, accumulationEndDate)
        val monthIntersectsPeriod = !reportEnd.isBefore(reportStart)

        return MonthlyReport(
            yearMonth = yearMonth,
            monthTotal = if (monthIntersectsPeriod) dao.sumBetween(reportStart, reportEnd) else 0L,
            periodTotal = dao.sumBetween(accumulationStartDate, accumulationEndDate),
            completedDaysInMonth = if (monthIntersectsPeriod) dao.countBetween(reportStart, reportEnd) else 0,
            plannedDaysInMonth = if (monthIntersectsPeriod) {
                ChronoUnit.DAYS.between(reportStart, reportEnd).toInt() + 1
            } else {
                0
            }
        )
    }
}
