package com.example.data

import kotlinx.coroutines.flow.Flow

class PrayerRepository(private val prayerDao: PrayerDao) {
    fun getLogsForDate(date: String): Flow<List<PrayerLog>> = prayerDao.getLogsForDate(date)
    
    fun getAllLogs(): Flow<List<PrayerLog>> = prayerDao.getAllLogs()

    suspend fun insertLog(log: PrayerLog) {
        prayerDao.insertLog(log)
    }

    suspend fun insertAll(logs: List<PrayerLog>) {
        prayerDao.insertAll(logs)
    }

    suspend fun deleteLog(date: String, prayerName: String) {
        prayerDao.deleteLog(date, prayerName)
    }

    suspend fun clearAll() {
        prayerDao.clearAll()
    }
}

class QuranRepository(private val quranDao: QuranDao) {
    val allProgress: Flow<List<QuranProgress>> = quranDao.getAllProgress()

    suspend fun insertProgress(progress: QuranProgress) {
        quranDao.insertProgress(progress)
    }

    suspend fun insertAll(progressList: List<QuranProgress>) {
        quranDao.insertAll(progressList)
    }

    suspend fun deleteProgress(id: Int) {
        quranDao.deleteProgress(id)
    }

    suspend fun clearAll() {
        quranDao.clearAll()
    }
}

class BookmarkRepository(private val bookmarkDao: BookmarkDao) {
    val allBookmarks: Flow<List<Bookmark>> = bookmarkDao.getAllBookmarks()

    suspend fun addBookmark(type: String, itemId: String) {
        bookmarkDao.insertBookmark(Bookmark(type = type, itemId = itemId))
    }

    suspend fun removeBookmark(type: String, itemId: String) {
        bookmarkDao.deleteBookmark(type, itemId)
    }
}

class EventReminderRepository(private val eventReminderDao: EventReminderDao) {
    val allReminders: Flow<List<EventReminder>> = eventReminderDao.getAllReminders()

    suspend fun toggleReminder(eventKey: String, enabled: Boolean) {
        if (enabled) {
            eventReminderDao.insertReminder(EventReminder(eventKey = eventKey, reminderEnabled = true))
        } else {
            eventReminderDao.deleteReminder(eventKey)
        }
    }
}

