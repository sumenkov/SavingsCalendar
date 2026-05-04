package ru.sumenkov.savingscalendar.data.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

@Dao
interface SavingsDao {
    @Query("SELECT * FROM savings_entries ORDER BY date DESC")
    fun observeAll(): Flow<List<SavingsEntry>>

    @Query("SELECT * FROM savings_entries WHERE date = :date LIMIT 1")
    suspend fun findByDate(date: LocalDate): SavingsEntry?

    @Query("SELECT * FROM savings_entries WHERE year = :year ORDER BY date ASC")
    fun observeByYear(year: Int): Flow<List<SavingsEntry>>

    @Query("SELECT COALESCE(SUM(amount), 0) FROM savings_entries WHERE year = :year")
    fun observeYearTotal(year: Int): Flow<Long>

    @Query("SELECT COALESCE(SUM(amount), 0) FROM savings_entries WHERE date BETWEEN :from AND :to")
    suspend fun sumBetween(from: LocalDate, to: LocalDate): Long

    @Query("SELECT COUNT(*) FROM savings_entries WHERE date BETWEEN :from AND :to")
    suspend fun countBetween(from: LocalDate, to: LocalDate): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entry: SavingsEntry)

    @Delete
    suspend fun delete(entry: SavingsEntry)
}
