package ru.sumenkov.savingscalendar.data.db

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import ru.sumenkov.savingscalendar.data.repository.SavingsRepository
import java.time.LocalDate
import java.time.YearMonth

@RunWith(AndroidJUnit4::class)
class AppDatabaseTest {
    private lateinit var database: AppDatabase
    private lateinit var repository: SavingsRepository

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        repository = SavingsRepository(database.savingsDao())
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun confirmedAmountsKeepBaseRateUsedAtConfirmation() = runBlocking {
        repository.confirmDate(LocalDate.of(2026, 1, 1), baseRate = 1L)
        repository.confirmDate(LocalDate.of(2026, 1, 2), baseRate = 5L)

        val report = repository.monthlyReport(YearMonth.of(2026, 1))

        assertEquals(11L, report.monthTotal)
        assertEquals(11L, report.yearTotal)
        assertEquals(2, report.completedDaysInMonth)
    }

    @Test
    fun monthlyReportUsesOnlyConfirmedEntriesInSelectedMonth() = runBlocking {
        repository.confirmDate(LocalDate.of(2026, 1, 31), baseRate = 1L)
        repository.confirmDate(LocalDate.of(2026, 2, 1), baseRate = 1L)

        val january = repository.monthlyReport(YearMonth.of(2026, 1))

        assertEquals(31L, january.monthTotal)
        assertEquals(63L, january.yearTotal)
        assertEquals(1, january.completedDaysInMonth)
        assertEquals(31, january.daysInMonth)
    }
}
