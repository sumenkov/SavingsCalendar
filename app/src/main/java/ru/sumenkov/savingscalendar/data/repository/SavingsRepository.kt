package ru.sumenkov.savingscalendar.data.repository

import kotlinx.coroutines.flow.Flow
import ru.sumenkov.savingscalendar.data.db.SavingsDao
import ru.sumenkov.savingscalendar.data.db.SavingsEntry
import ru.sumenkov.savingscalendar.domain.MonthlyReport
import ru.sumenkov.savingscalendar.domain.SavingsCalculator
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.YearMonth

class SavingsRepository(
    private val dao: SavingsDao,
    private val calculator: SavingsCalculator = SavingsCalculator()
) {
    fun observeAll(): Flow<List<SavingsEntry>> = dao.observeAll()

    fun observeByYear(year: Int): Flow<List<SavingsEntry>> = dao.observeByYear(year)

    fun observeYearTotal(year: Int): Flow<Long> = dao.observeYearTotal(year)

    suspend fun confirmDate(date: LocalDate, baseRate: Long) {
        val amount = calculator.amountForDay(date.dayOfYear, baseRate)
        dao.upsert(
            SavingsEntry(
                date = date,
                year = date.year,
                dayOfYear = date.dayOfYear,
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

    suspend fun monthlyReport(yearMonth: YearMonth): MonthlyReport {
        val from = yearMonth.atDay(1)
        val to = yearMonth.atEndOfMonth()
        val yearFrom = LocalDate.of(yearMonth.year, 1, 1)
        val yearTo = LocalDate.of(yearMonth.year, 12, 31)
        return MonthlyReport(
            yearMonth = yearMonth,
            monthTotal = dao.sumBetween(from, to),
            yearTotal = dao.sumBetween(yearFrom, yearTo),
            completedDaysInMonth = dao.countBetween(from, to),
            daysInMonth = yearMonth.lengthOfMonth()
        )
    }
}
