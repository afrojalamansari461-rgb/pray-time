package com.example.notifications

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.media.RingtoneManager
import android.os.Build
import androidx.core.app.NotificationCompat
import com.example.MainActivity
import com.example.data.AppDatabase
import com.example.data.PrayerLog
import com.example.utils.PrayerTimeCalculator
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class PrayerReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val prayerName = intent.getStringExtra("prayer_name") ?: "Upcoming Prayer"
        val isReminder = intent.getBooleanExtra("is_reminder", false)

        val channelId = "prayer_companion_alerts"
        val channelName = "Prayer Alerts & Reminders"
        
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(channelId, channelName, NotificationManager.IMPORTANCE_HIGH).apply {
                description = "Islamic notifications for Adhan and daily tracking reminders."
                enableVibration(true)
            }
            notificationManager.createNotificationChannel(channel)
        }

        // Tap notification opens MainActivity
        val openIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            context, 
            0, 
            openIntent, 
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val title = if (isReminder) "Daily Spiritual Progress" else "Time for $prayerName"
        val text = if (isReminder) {
            "Don't forget to track your daily Quran reading and log your completed prayers today!"
        } else {
            "Adhan: It is time for $prayerName in your current location. Tap to log."
        }

        val defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)

        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(android.R.drawable.ic_lock_idle_alarm) // System build fallback icon
            .setContentTitle(title)
            .setContentText(text)
            .setAutoCancel(true)
            .setSound(defaultSoundUri)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .build()

        notificationManager.notify(if (isReminder) 999 else prayerName.hashCode(), notification)

        // Automatically reschedule next alarm
        val sharedPrefs = context.getSharedPreferences("PrayerPrefs", Context.MODE_PRIVATE)
        val lat = sharedPrefs.getFloat("latitude", 0.0f).toDouble()
        val lon = sharedPrefs.getFloat("longitude", 0.0f).toDouble()
        
        if (lat != 0.0 && lon != 0.0) {
            rescheduleNextAlarm(context, lat, lon)
        }
    }

    private fun rescheduleNextAlarm(context: Context, lat: Double, lon: Double) {
        val sharedPrefs = context.getSharedPreferences("PrayerPrefs", Context.MODE_PRIVATE)
        val asrSchoolStr = sharedPrefs.getString("asr_school", "STANDARD") ?: "STANDARD"
        val school = if (asrSchoolStr == "HANAFI") PrayerTimeCalculator.AsrSchool.HANAFI else PrayerTimeCalculator.AsrSchool.STANDARD
        
        AlarmScheduler.scheduleNextPrayer(context, lat, lon, school)
    }
}
