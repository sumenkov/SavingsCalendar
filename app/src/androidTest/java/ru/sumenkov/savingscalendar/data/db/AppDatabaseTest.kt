package ru.sumenkov.savingscalendar.data.db

import android.content.Context
import androidx.room.Room
import androidx.room.testing.MigrationTestHelper
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import ru.sumenkov.savingscalendar.data.repository.SavingsRepository
import java.time.LocalDate
import java.time.YearMonth

@RunWith(AndroidJUnit4::class)
class AppDatabaseTest {
    @get:Rule
    val migrationHelper = MigrationTestHelper(
        InstrumentationRegistry.getInstrumentation(),
        AppDatabase::class.java
    )

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

    @Test
    fun deletingDateRemovesItFromReports() = runBlocking {
        val date = LocalDate.of(2026, 1, 10)
        repository.confirmDate(date, baseRate = 2L)

        repository.deleteDate(date)

        val report = repository.monthlyReport(YearMonth.of(2026, 1))
        assertEquals(0L, report.monthTotal)
        assertEquals(0L, report.yearTotal)
        assertEquals(0, report.completedDaysInMonth)
    }

    @Test
    fun migrationFrom1To2DropsStoredCalendarDay() {
        migrationHelper.createDatabase(TEST_DB_NAME, 1).apply {
            execSQL(
                """
                INSERT INTO savings_entries (id, date, year, dayOfYear, baseRate, amount, confirmedAt, note)
                VALUES (1, '2026-05-10', 2026, 130, 1, 5, '2026-05-10T20:00:00', NULL)
                """.trimIndent()
            )
            close()
        }

        val db = migrationHelper.runMigrationsAndValidate(
            TEST_DB_NAME,
            2,
            true,
            AppDatabase.MIGRATION_1_2
        )

        db.query("SELECT date, year, baseRate, amount, confirmedAt FROM savings_entries").use { cursor ->
            cursor.moveToFirst()
            assertEquals("2026-05-10", cursor.getString(0))
            assertEquals(2026, cursor.getInt(1))
            assertEquals(1L, cursor.getLong(2))
            assertEquals(5L, cursor.getLong(3))
            assertEquals("2026-05-10T20:00:00", cursor.getString(4))
        }
        db.query("PRAGMA table_info(savings_entries)").use { cursor ->
            val columns = buildList {
                while (cursor.moveToNext()) add(cursor.getString(cursor.getColumnIndexOrThrow("name")))
            }
            assertFalse("dayOfYear" in columns)
        }
        db.close()
    }

    private companion object {
        const val TEST_DB_NAME = "migration-test"
    }
}
