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

        val fIqamah = prefs.getInt("fajr_iqamah_delay", 15)
        val dIqamah = prefs.getInt("dhuhr_iqamah_delay", 15)
        val aIqamah = prefs.getInt("asr_iqamah_delay", 15)
        val mIqamah = prefs.getInt("maghrib_iqamah_delay", 10)
        val iIqamah = prefs.getInt("isha_iqamah_delay", 15)

        val fajrOverrideAdhan = prefs.getString("fajr_override_adhan", "") ?: ""
        val fajrOverrideNamaj = prefs.getString("fajr_override_namaj", "") ?: ""
        val dhuhrOverrideAdhan = prefs.getString("dhuhr_override_adhan", "") ?: ""
        val dhuhrOverrideNamaj = prefs.getString("dhuhr_override_namaj", "") ?: ""
        val asrOverrideAdhan = prefs.getString("asr_override_adhan", "") ?: ""
        val asrOverrideNamaj = prefs.getString("asr_override_namaj", "") ?: ""
        val maghribOverrideAdhan = prefs.getString("maghrib_override_adhan", "") ?: ""
        val maghribOverrideNamaj = prefs.getString("maghrib_override_namaj", "") ?: ""
        val ishaOverrideAdhan = prefs.getString("isha_override_adhan", "") ?: ""
        val ishaOverrideNamaj = prefs.getString("isha_override_namaj", "") ?: ""

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
                val parts = timeStr.trim().split(":")
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

        fun getEventsForDay(cal: Calendar, bt: com.example.utils.PrayerTimeCalculator.PrayerTimes): List<Pair<String, String>> {
            val fAd = if (fajrOverrideAdhan.isNotEmpty()) fajrOverrideAdhan else adjustTime(bt.fajr, fOff)
            val fNm = if (fajrOverrideNamaj.isNotEmpty()) fajrOverrideNamaj else adjustTime(fAd, fIqamah)

            val dAd = if (dhuhrOverrideAdhan.isNotEmpty()) dhuhrOverrideAdhan else adjustTime(bt.dhuhr, dOff)
            val dNm = if (dhuhrOverrideNamaj.isNotEmpty()) dhuhrOverrideNamaj else adjustTime(dAd, dIqamah)

            val aAd = if (asrOverrideAdhan.isNotEmpty()) asrOverrideAdhan else adjustTime(bt.asr, aOff)
            val aNm = if (asrOverrideNamaj.isNotEmpty()) asrOverrideNamaj else adjustTime(aAd, aIqamah)

            val mAd = if (maghribOverrideAdhan.isNotEmpty()) maghribOverrideAdhan else adjustTime(bt.maghrib, mOff)
            val mNm = if (maghribOverrideNamaj.isNotEmpty()) maghribOverrideNamaj else adjustTime(mAd, mIqamah)

            val iAd = if (ishaOverrideAdhan.isNotEmpty()) ishaOverrideAdhan else adjustTime(bt.isha, iOff)
            val iNm = if (ishaOverrideNamaj.isNotEmpty()) ishaOverrideNamaj else adjustTime(iAd, iIqamah)

            val ishraq = adjustTime(bt.sunrise, 15)
            val awabin = adjustTime(bt.maghrib, 15)

            return listOf(
                "Tahajjud Namaj" to "02:00",
                "Fajr Adhan" to fAd,
                "Fajr Namaj" to fNm,
                "Ishraq Namaj" to ishraq,
                "Duha Namaj" to "08:30",
                "Dhuhr Adhan" to dAd,
                "Dhuhr Namaj" to dNm,
                "Asr Adhan" to aAd,
                "Asr Namaj" to aNm,
                "Maghrib Adhan" to mAd,
                "Maghrib Namaj" to mNm,
                "Awabin Namaj" to awabin,
                "Isha Adhan" to iAd,
                "Isha Namaj" to iNm
            )
        }

        val format = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val dateString = format.format(calendar.time)

        val todayEvents = getEventsForDay(calendar, baseTimes)

        var scheduledPrayerName = ""
        var scheduledTimeMillis = 0L

        // Find the first upcoming prayer/event for today
        for ((name, timeStr) in todayEvents) {
            val prayerTime = parseTimeToMillis(calendar, timeStr)
            if (prayerTime > now) {
                scheduledPrayerName = name
                scheduledTimeMillis = prayerTime
                break
            }
        }

        // If all prayers today have passed, schedule the first upcoming event for tomorrow
        if (scheduledTimeMillis == 0L) {
            val tomorrowCalendar = Calendar.getInstance().apply { add(Calendar.DATE, 1) }
            val tomorrowTimes = PrayerTimeCalculator.calculate(
                latitude = latitude,
                longitude = longitude,
                timezoneOffset = tzOffset,
                calendar = tomorrowCalendar,
                asrSchool = asrSchool
            )
            val tomorrowEvents = getEventsForDay(tomorrowCalendar, tomorrowTimes)
            for ((name, timeStr) in tomorrowEvents) {
                val prayerTime = parseTimeToMillis(tomorrowCalendar, timeStr)
                if (prayerTime > now) {
                    scheduledPrayerName = name
                    scheduledTimeMillis = prayerTime
                    break
                }
            }
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
        
        val canScheduleExact = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            try {
                alarmManager.canScheduleExactAlarms()
            } catch (e: Throwable) {
                false
            }
        } else {
            true
        }

        try {
            if (canScheduleExact && Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    scheduledTimeMillis,
                    pendingIntent
                )
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                alarmManager.setAndAllowWhileIdle(
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
        } catch (e: Throwable) {
            // Safe fallback if exact/inexact alarms are restricted or throw any exception
            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    alarmManager.setAndAllowWhileIdle(
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
            } catch (ex: Throwable) {
                try {
                    alarmManager.set(
                        AlarmManager.RTC_WAKEUP,
                        scheduledTimeMillis,
                        pendingIntent
                    )
                } catch (err: Throwable) {
                    Log.e(TAG, "Failed to schedule alarm even with fallback: ${err.message}")
                }
            }
            Log.w(TAG, "Exception handled - fell back to alternative alarm setting: ${e.message}")
        }

        // Also schedule daily general reminder at 9 PM (21:00) to log stats
        scheduleDailyReminder(context)
    }

    private fun scheduleDailyReminder(context: Context) {
        try {
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
        } catch (e: Throwable) {
            Log.e(TAG, "Error in scheduleDailyReminder: ${e.message}", e)
        }
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
