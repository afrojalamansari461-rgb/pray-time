package com.example.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [PrayerLog::class, QuranProgress::class, Bookmark::class, EventReminder::class], version = 2, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun prayerDao(): PrayerDao
    abstract fun quranDao(): QuranDao
    abstract fun bookmarkDao(): BookmarkDao
    abstract fun eventReminderDao(): EventReminderDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "prayer_companion_database"
                )
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
