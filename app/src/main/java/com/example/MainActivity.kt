package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.ViewModelProvider
import com.example.data.AppDatabase
import com.example.data.PrayerRepository
import com.example.data.QuranRepository
import com.example.ui.PrayerCompanionApp
import com.example.ui.theme.MyApplicationTheme
import com.example.viewmodel.PrayerViewModel
import com.example.viewmodel.PrayerViewModelFactory

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        // Intercept and cleanly print/store any fatal exceptions to avoid silent broken UI channels
        val originalHandler = Thread.getDefaultUncaughtExceptionHandler()
        Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
            android.util.Log.e("FATAL_CRASH", "CRASH DETECTED on thread ${thread?.name}: ${throwable?.message}", throwable)
            try {
                val sharedPrefs = getSharedPreferences("crash_reports", android.content.Context.MODE_PRIVATE)
                sharedPrefs.edit()
                    .putString("last_crash_msg", throwable?.message ?: "Unknown error")
                    .putString("last_crash_trace", android.util.Log.getStackTraceString(throwable))
                    .putLong("last_crash_time", System.currentTimeMillis())
                    .apply()
            } catch (t: Throwable) {
                // Secondary fallback protection
            }
            originalHandler?.uncaughtException(thread, throwable)
        }

        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Log previous crash report on launch if there was any
        try {
            val sharedPrefs = getSharedPreferences("crash_reports", android.content.Context.MODE_PRIVATE)
            val lastCrashMsg = sharedPrefs.getString("last_crash_msg", null)
            val lastCrashTime = sharedPrefs.getLong("last_crash_time", 0)
            if (lastCrashMsg != null && System.currentTimeMillis() - lastCrashTime < 60_000 * 5) {
                android.util.Log.w("FATAL_CRASH", "Detected crash on previous run: $lastCrashMsg")
                // Clear so we don't alert on subsequent sessions
                sharedPrefs.edit().remove("last_crash_msg").apply()
            }
        } catch (t: Throwable) {}

        // Initialize Room local database singletons
        val database = AppDatabase.getDatabase(applicationContext)
        val prayerDao = database.prayerDao()
                val quranDao = database.quranDao()
        val bookmarkDao = database.bookmarkDao()
        val eventReminderDao = database.eventReminderDao()

        // Create decoupled business repositories
        val prayerRepo = PrayerRepository(prayerDao)
        val quranRepo = QuranRepository(quranDao)
        val bookmarkRepo = com.example.data.BookmarkRepository(bookmarkDao)
        val eventReminderRepo = com.example.data.EventReminderRepository(eventReminderDao)

        // Bind dependencies into PrayerViewModel using custom Factory
        val factory = PrayerViewModelFactory(application, prayerRepo, quranRepo, bookmarkRepo, eventReminderRepo)
        val viewModel = ViewModelProvider(this, factory)[PrayerViewModel::class.java]

        setContent {
            val currentTheme by viewModel.currentThemeName.collectAsState()
            MyApplicationTheme(themeName = currentTheme) {
                PrayerCompanionApp(viewModel = viewModel)
            }
        }
    }
}
