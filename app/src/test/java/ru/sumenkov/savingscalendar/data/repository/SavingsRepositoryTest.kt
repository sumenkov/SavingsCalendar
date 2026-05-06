package ru.sumenkov.savingscalendar.data.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import ru.sumenkov.savingscalendar.data.db.SavingsDao
import ru.sumenkov.savingscalendar.data.db.SavingsEntry
import ru.sumenkov.savingscalendar.domain.SavingsAmountMode
import java.time.LocalDate
import java.time.YearMonth

class SavingsRepositoryTest {
    @Test
    fun confirmDateUsesDayNumberFromAccumulationStart() = runBlocking {
        val dao = FakeSavingsDao()
        val repository = SavingsRepository(dao)

        repository.confirmDate(
            date = LocalDate.of(2026, 5, 10),
            baseRate = 1L,
            accumulationStartDate = LocalDate.of(2026, 5, 6)
        )

        assertEquals(5L, dao.findByDate(LocalDate.of(2026, 5, 10))?.amount)
    }

    @Test
    fun confirmDateUsesFixedAmountMode() = runBlocking {
        val dao = FakeSavingsDao()
        val repository = SavingsRepository(dao)

        repository.confirmDate(
            date = LocalDate.of(2026, 5, 10),
            baseRate = 100L,
            amountMode = SavingsAmountMode.FIXED,
            accumulationStartDate = LocalDate.of(2026, 5, 6)
        )

        assertEquals(100L, dao.findByDate(LocalDate.of(2026, 5, 10))?.amount)
    }

    @Test
    fun monthlyReportUsesOnlySelectedMonthForMonthTotal() = runBlocking {
        val dao = FakeSavingsDao()
        val repository = SavingsRepository(dao)
        val startDate = LocalDate.of(2026, 5, 6)
        val endDate = LocalDate.of(2026, 5, 10)

        repository.confirmDate(LocalDate.of(2026, 5, 5), baseRate = 1L)
        repository.confirmDate(LocalDate.of(2026, 5, 6), baseRate = 1L, accumulationStartDate = startDate)
        repository.confirmDate(LocalDate.of(2026, 5, 10), baseRate = 1L, accumulationStartDate = startDate)
        repository.confirmDate(LocalDate.of(2026, 5, 11), baseRate = 1L)

        val report = repository.monthlyReport(
            yearMonth = YearMonth.of(2026, 5),
            accumulationStartDate = startDate,
            accumulationEndDate = endDate
        )

        assertEquals(6L, report.monthTotal)
        assertEquals(6L, report.periodTotal)
        assertEquals(2, report.completedDaysInMonth)
        assertEquals(5, report.plannedDaysInMonth)
        assertEquals(40, report.completionPercent)
    }

    @Test
    fun monthlyReportReturnsEmptyMonthWhenMonthIsOutsidePeriod() = runBlocking {
        val dao = FakeSavingsDao()
        val repository = SavingsRepository(dao)

        repository.confirmDate(LocalDate.of(2026, 5, 6), baseRate = 1L, accumulationStartDate = LocalDate.of(2026, 5, 6))

        val report = repository.monthlyReport(
            yearMonth = YearMonth.of(2026, 6),
            accumulationStartDate = LocalDate.of(2026, 5, 6),
            accumulationEndDate = LocalDate.of(2026, 5, 10)
        )

        assertEquals(0L, report.monthTotal)
        assertEquals(1L, report.periodTotal)
        assertEquals(0, report.completedDaysInMonth)
        assertEquals(0, report.plannedDaysInMonth)
    }

    @Test
    fun deleteDateRemovesEntry() = runBlocking {
        val dao = FakeSavingsDao()
        val repository = SavingsRepository(dao)
        val date = LocalDate.of(2026, 1, 10)

        repository.confirmDate(date, baseRate = 1L)
        repository.deleteDate(date)

        assertNull(dao.findByDate(date))
    }

    private class FakeSavingsDao : SavingsDao {
        private val entries = MutableStateFlow<List<SavingsEntry>>(emptyList())
        private var nextId = 1L

        override fun observeAll(): Flow<List<SavingsEntry>> = entries

        override suspend fun findByDate(date: LocalDate): SavingsEntry? {
            return entries.value.firstOrNull { it.date == date }
        }

        override fun observeByYear(year: Int): Flow<List<SavingsEntry>> {
            return MutableStateFlow(entries.value.filter { it.year == year }.sortedBy { it.date })
        }

        override fun observeYearTotal(year: Int): Flow<Long> {
            return MutableStateFlow(entries.value.filter { it.year == year }.sumOf { it.amount })
        }

        override suspend fun sumBetween(from: LocalDate, to: LocalDate): Long {
            return entries.value
                .filter { !it.date.isBefore(from) && !it.date.isAfter(to) }
                .sumOf { it.amount }
        }

        override suspend fun countBetween(from: LocalDate, to: LocalDate): Int {
            return entries.value.count { !it.date.isBefore(from) && !it.date.isAfter(to) }
        }

        override suspend fun upsert(entry: SavingsEntry) {
            val storedEntry = entry.copy(id = entry.id.takeIf { it != 0L } ?: nextId++)
            entries.value = entries.value.filterNot { it.date == storedEntry.date } + storedEntry
        }

        override suspend fun delete(entry: SavingsEntry) {
            entries.value = entries.value.filterNot { it.id == entry.id }
        }
    }
}
