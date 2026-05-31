package com.example

import android.app.Application
import android.content.Context
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onRoot
import androidx.test.core.app.ApplicationProvider
import com.example.data.*
import com.example.ui.PrayerCompanionApp
import com.example.ui.theme.MyApplicationTheme
import com.example.viewmodel.PrayerViewModel
import com.github.takahirom.roborazzi.RobolectricDeviceQualifiers
import com.github.takahirom.roborazzi.captureRoboImage
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(qualifiers = RobolectricDeviceQualifiers.Pixel8, sdk = [36])
class GreetingScreenshotTest {

  @get:Rule val composeTestRule = createComposeRule()

  @Test
  fun greeting_screenshot() {
    composeTestRule.setContent { MyApplicationTheme { androidx.compose.material3.Text("Afah App") } }

    composeTestRule.onRoot().captureRoboImage(filePath = "src/test/screenshots/greeting.png")
  }

  @Test
  fun test_full_app_composition_and_idle() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    
    // Setup database and repos
    val database = AppDatabase.getDatabase(context)
    val prayerDao = database.prayerDao()
    val quranDao = database.quranDao()
    val bookmarkDao = database.bookmarkDao()
    val eventReminderDao = database.eventReminderDao()

    val prayerRepo = PrayerRepository(prayerDao)
    val quranRepo = QuranRepository(quranDao)
    val bookmarkRepo = BookmarkRepository(bookmarkDao)
    val eventReminderRepo = EventReminderRepository(eventReminderDao)

    val viewModel = PrayerViewModel(
        context as Application,
        prayerRepo,
        quranRepo,
        bookmarkRepo,
        eventReminderRepo
    )

    // Set Compose content with the active app 
    composeTestRule.setContent {
      MyApplicationTheme {
        PrayerCompanionApp(viewModel = viewModel)
      }
    }

    // Execute recompositions, animations, and LaunchedEffects
    composeTestRule.waitForIdle()
  }
}

