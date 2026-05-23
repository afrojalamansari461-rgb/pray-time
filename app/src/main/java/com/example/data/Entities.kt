package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "prayer_logs")
data class PrayerLog(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val date: String,             // Format: yyyy-MM-dd
    val prayerName: String,       // Fajr, Dhuhr, Asr, Maghrib, Isha
    val completed: Boolean,
    val timestamp: Long = System.currentTimeMillis(),
    val prayedInCongregation: Boolean = false,
    val isQada: Boolean = false
)

@Entity(tableName = "quran_progress")
data class QuranProgress(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val date: String,             // Format: yyyy-MM-dd
    val surahName: String,
    val surahNumber: Int,         // 1 to 114
    val startAyah: Int,
    val endAyah: Int,
    val timestamp: Long = System.currentTimeMillis(),
    val durationMinutes: Int = 0
)

@Entity(tableName = "bookmarks")
data class Bookmark(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val type: String,             // "hadith" or "dua"
    val itemId: String,           // String identifier of the item
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "event_reminders")
data class EventReminder(
    @PrimaryKey val eventKey: String, // Format: eventCode_year, e.g. "ramadan_2026"
    val reminderEnabled: Boolean,
    val timestamp: Long = System.currentTimeMillis()
)

