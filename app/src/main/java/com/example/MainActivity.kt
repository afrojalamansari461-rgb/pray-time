package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
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
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

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
            MyApplicationTheme {
                PrayerCompanionApp(viewModel = viewModel)
            }
        }
    }
}
