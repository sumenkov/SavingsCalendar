package ru.sumenkov.savingscalendar.data.db

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import java.time.LocalDate
import java.time.LocalDateTime

@Entity(
    tableName = "savings_entries",
    indices = [Index(value = ["date"], unique = true)]
)
data class SavingsEntry(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val date: LocalDate,
    val year: Int,
    val dayOfYear: Int,
    val baseRate: Long,
    val amount: Long,
    val confirmedAt: LocalDateTime,
    val note: String? = null
)
