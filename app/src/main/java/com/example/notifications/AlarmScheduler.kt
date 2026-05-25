package com.example.notifications

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import com.example.utils.PrayerTimeCalculator
import java.text.SimpleDateFormat
import java.util.*

object AlarmScheduler {

    private const val TAG = "AlarmScheduler"

    fun scheduleNextPrayer(
        context: Context,
        latitude: Double,
        longitude: Double,
        asrSchool: PrayerTimeCalculator.AsrSchool = PrayerTimeCalculator.AsrSchool.STANDARD
    ) {
        val calendar = Calendar.getInstance()
        val now = calendar.timeInMillis
        val tz = calendar.timeZone
        // Raw offset + DST offset in hours
        val tzOffset = (tz.rawOffset + tz.dstSavings).toDouble() / 3600000.0

        val prefs = context.getSharedPreferences("PrayerPrefs", Context.MODE_PRIVATE)
        val fOff = prefs.getInt("fajr_offset", 0)
        val sOff = prefs.getInt("sunrise_offset", 0)
        val dOff = prefs.getInt("dhuhr_offset", 0)
        val aOff = prefs.getInt("asr_offset", 0)
        val mOff = prefs.getInt("maghrib_offset", 0)
        val iOff = prefs.getInt("isha_offset", 0)

        val baseTimes = PrayerTimeCalculator.calculate(
            latitude = latitude,
            longitude = longitude,
            timezoneOffset = tzOffset,
            calendar = calendar,
            asrSchool = asrSchool
        )

        fun adjustTime(timeStr: String, offsetMinutes: Int): String {
            if (timeStr.isEmpty()) return ""
            try {
                val parts = timeStr.split(":")
                if (parts.size != 2) return timeStr
                val hrs = parts[0].toIntOrNull() ?: return timeStr
                val mins = parts[1].toIntOrNull() ?: return timeStr
                val totalMinutes = (hrs * 60 + mins + offsetMinutes + 1440) % 1440
                val newHrs = totalMinutes / 60
                val newMins = totalMinutes % 60
                return String.format("%02d:%02d", newHrs, newMins)
            } catch (e: Exception) {
                return timeStr
            }
        }

        val format = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val dateString = format.format(calendar.time)

        // Maps prayer name to its HH:mm string format with user adjustments applied
        val list = listOf(
            "Fajr" to adjustTime(baseTimes.fajr, fOff),
            "Dhuhr" to adjustTime(baseTimes.dhuhr, dOff),
            "Asr" to adjustTime(baseTimes.asr, aOff),
            "Maghrib" to adjustTime(baseTimes.maghrib, mOff),
            "Isha" to adjustTime(baseTimes.isha, iOff)
        )

        var scheduledPrayerName = ""
        var scheduledTimeMillis = 0L

        // Find the first upcoming prayer for today
        for ((name, timeStr) in list) {
            val prayerTime = parseTimeToMillis(calendar, timeStr)
            if (prayerTime > now) {
                scheduledPrayerName = name
                scheduledTimeMillis = prayerTime
                break
            }
        }

        // If all prayers today have passed, schedule Fajr for tomorrow
        if (scheduledTimeMillis == 0L) {
            val tomorrowCalendar = Calendar.getInstance().apply { add(Calendar.DATE, 1) }
            val tomorrowTimes = PrayerTimeCalculator.calculate(
                latitude = latitude,
                longitude = longitude,
                timezoneOffset = tzOffset,
                calendar = tomorrowCalendar,
                asrSchool = asrSchool
            )
            scheduledPrayerName = "Fajr"
            val adjustedTomorrowFajr = adjustTime(tomorrowTimes.fajr, fOff)
            scheduledTimeMillis = parseTimeToMillis(tomorrowCalendar, adjustedTomorrowFajr)
        }

        // Write to SharedPreferences so the UI knows which one is scheduled
        prefs.edit().apply {
            putFloat("latitude", latitude.toFloat())
            putFloat("longitude", longitude.toFloat())
            putString("next_prayer_name", scheduledPrayerName)
            putLong("next_prayer_time", scheduledTimeMillis)
            putString("asr_school", asrSchool.name)
            apply()
        }

        // Create notification intent
        val intent = Intent(context, PrayerReceiver::class.java).apply {
            putExtra("prayer_name", scheduledPrayerName)
            putExtra("is_reminder", false)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            12345, // static pending intent id for notifications
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    scheduledTimeMillis,
                    pendingIntent
                )
            } else {
                alarmManager.set(
                    AlarmManager.RTC_WAKEUP,
                    scheduledTimeMillis,
                    pendingIntent
                )
            }
            Log.d(TAG, "Scheduled alarm for $scheduledPrayerName at ${Date(scheduledTimeMillis)}")
        } catch (e: SecurityException) {
            // Under Android 13/14, SCHEDULE_EXACT_ALARM might require explicit user toggle or system permissions.
            // Safe fallback is standard set() which preserves battery but triggers around the requested time.
            alarmManager.set(
                AlarmManager.RTC_WAKEUP,
                scheduledTimeMillis,
                pendingIntent
            )
            Log.e(TAG, "SecurityException: Exact alarms not allowed. Standard set used.", e)
        }

        // Also schedule daily general reminder at 9 PM (21:00) to log stats
        scheduleDailyReminder(context)
    }

    private fun scheduleDailyReminder(context: Context) {
        val calendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 21)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
        }
        
        if (calendar.timeInMillis < System.currentTimeMillis()) {
            calendar.add(Calendar.DATE, 1)
        }

        val intent = Intent(context, PrayerReceiver::class.java).apply {
            putExtra("is_reminder", true)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            54321,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        alarmManager.setInexactRepeating(
            AlarmManager.RTC_WAKEUP,
            calendar.timeInMillis,
            AlarmManager.INTERVAL_DAY,
            pendingIntent
        )
    }

    private fun parseTimeToMillis(dateCalendar: Calendar, timeStr: String): Long {
        val parts = timeStr.split(":")
        if (parts.size != 2) return 0L
        val hours = parts[0].toIntOrNull() ?: 0
        val minutes = parts[1].toIntOrNull() ?: 0

        val cal = Calendar.getInstance().apply {
            timeZone = dateCalendar.timeZone
            set(Calendar.YEAR, dateCalendar.get(Calendar.YEAR))
            set(Calendar.MONTH, dateCalendar.get(Calendar.MONTH))
            set(Calendar.DAY_OF_MONTH, dateCalendar.get(Calendar.DAY_OF_MONTH))
            set(Calendar.HOUR_OF_DAY, hours)
            set(Calendar.MINUTE, minutes)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        return cal.timeInMillis
    }
}
