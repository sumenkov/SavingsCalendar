package ru.sumenkov.savingscalendar.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.migration.Migration
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities = [SavingsEntry::class],
    version = 2,
    exportSchema = true
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun savingsDao(): SavingsDao

    companion object {
        @Volatile
        private var instance: AppDatabase? = null

        fun get(context: Context): AppDatabase {
            return instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "savings_calendar.db"
                ).addMigrations(MIGRATION_1_2)
                    .build()
                    .also { instance = it }
            }
        }

        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS savings_entries_new (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        date TEXT NOT NULL,
                        year INTEGER NOT NULL,
                        baseRate INTEGER NOT NULL,
                        amount INTEGER NOT NULL,
                        confirmedAt TEXT NOT NULL,
                        note TEXT
                    )
                    """.trimIndent()
                )
                db.execSQL(
                    """
                    INSERT INTO savings_entries_new (id, date, year, baseRate, amount, confirmedAt, note)
                    SELECT id, date, year, baseRate, amount, confirmedAt, note
                    FROM savings_entries
                    """.trimIndent()
                )
                db.execSQL("DROP TABLE savings_entries")
                db.execSQL("ALTER TABLE savings_entries_new RENAME TO savings_entries")
                db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS index_savings_entries_date ON savings_entries(date)")
            }
        }
    }
}
