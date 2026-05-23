package com.example.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface PrayerDao {
    @Query("SELECT * FROM prayer_logs WHERE date = :date")
    fun getLogsForDate(date: String): Flow<List<PrayerLog>>

    @Query("SELECT * FROM prayer_logs ORDER BY date DESC, timestamp DESC")
    fun getAllLogs(): Flow<List<PrayerLog>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLog(log: PrayerLog)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(logs: List<PrayerLog>)

    @Query("DELETE FROM prayer_logs WHERE date = :date AND prayerName = :prayerName")
    suspend fun deleteLog(date: String, prayerName: String)

    @Query("DELETE FROM prayer_logs")
    suspend fun clearAll()
}

@Dao
interface QuranDao {
    @Query("SELECT * FROM quran_progress ORDER BY timestamp DESC")
    fun getAllProgress(): Flow<List<QuranProgress>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProgress(progress: QuranProgress)

    @Query("DELETE FROM quran_progress WHERE id = :id")
    suspend fun deleteProgress(id: Int)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(progressList: List<QuranProgress>)

    @Query("DELETE FROM quran_progress")
    suspend fun clearAll()
}

@Dao
interface BookmarkDao {
    @Query("SELECT * FROM bookmarks ORDER BY timestamp DESC")
    fun getAllBookmarks(): Flow<List<Bookmark>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBookmark(bookmark: Bookmark)

    @Query("DELETE FROM bookmarks WHERE type = :type AND itemId = :itemId")
    suspend fun deleteBookmark(type: String, itemId: String)

    @Query("SELECT EXISTS(SELECT 1 FROM bookmarks WHERE type = :type AND itemId = :itemId LIMIT 1)")
    fun isBookmarked(type: String, itemId: String): Flow<Boolean>
}

@Dao
interface EventReminderDao {
    @Query("SELECT * FROM event_reminders")
    fun getAllReminders(): Flow<List<EventReminder>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReminder(reminder: EventReminder)

    @Query("DELETE FROM event_reminders WHERE eventKey = :eventKey")
    suspend fun deleteReminder(eventKey: String)
}

