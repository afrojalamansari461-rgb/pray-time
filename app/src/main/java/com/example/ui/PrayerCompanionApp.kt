package com.example.ui

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import com.example.data.PrayerLog
import com.example.data.QuranProgress
import com.example.ui.theme.animatedGradientBackground
import com.example.utils.PrayerTimeCalculator
import com.example.viewmodel.PrayerViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import androidx.compose.runtime.rememberCoroutineScope
import java.io.File
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

object Tabs {
    const val PRAYERS = "Prayers"
    const val QURAN = "Quran"
    const val WISDOM = "Wisdom"
    const val CALENDAR = "Calendar"
    const val QIBLA = "Qibla"
    const val STATS = "Statistics"
    const val SETTINGS = "Settings"
}

private val SURAH_LIST = listOf(
    "Al-Fatihah", "Al-Baqarah", "Ali 'Imran", "An-Nisa'", "Al-Ma'idah", "Al-An'am", "Al-A'raf", "Al-Anfal", "At-Tawbah", "Yunus", "Hud", "Yusuf", "Ar-Ra'd", "Ibrahim", "Al-Hijr", "An-Nahl", "Al-Isra'", "Al-Kahf", "Maryam", "Ta-Ha", "Al-Anbiya'", "Al-Hajj", "Al-Mu'minun", "An-Nur", "Al-Furqan", "Ash-Shu'ara'", "An-Naml", "Al-Qacas", "Al-Ankabut", "Ar-Rum", "Luqman", "As-Sajdah", "Al-Ahzab", "Saba'", "Fatir", "Ya-Sin", "As-Saffat", "Sad", "Az-Zumar", "Ghafir", "Fussilat", "Ash-Shura", "Az-Zukhruf", "Ad-Dukhan", "Al-Jathiyah", "Al-Ahqaf", "Muhammad", "Al-Fath", "Al-Hujurat", "Qaf", "Adh-Dhariyat", "At-Tur", "An-Najm", "Al-Qamar", "Ar-Rahman", "Al-Waqi'ah", "Al-Hadid", "Al-Mujadilah", "Al-Hashr", "Al-Mumtahanah", "As-Saff", "Al-Jumu'ah", "Al-Munafiqun", "At-Taghabun", "At-Talaq", "At-Tahrim", "Al-Mulk", "Al-Qalam", "Al-Haqqah", "Al-Ma'arij", "Nuh", "Al-Jinn", "Al-Muzzammil", "Al-Muddaththir", "Al-Qiyamah", "Al-Insan", "Al-Mursalat", "An-Naba'", "An-Nazi'at", "'Abasa", "At-Takwir", "Al-Infitar", "Al-Mutaffifin", "Al-Inshiqaq", "Al-Buruj", "At-Tariq", "Al-A'la", "Al-Ghashiyah", "Al-Fajr", "Al-Balad", "Ash-Shams", "Al-Layl", "Ad-Duha", "Ash-Sharh", "At-Tin", "Al-'Alaq", "Al-Qadr", "Al-Bayyinah", "Az-Zalzalah", "Al-'Adiyat", "Al-Qari'ah", "At-Takathur", "Al-'Asr", "Al-Humazah", "Al-Fil", "Quraysh", "Al-Ma'un", "Al-Kawthar", "Al-Kafirun", "An-Nasr", "Al-Masad", "Al-Ikhlas", "Al-Falaq", "An-Nas"
)

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun PrayerCompanionApp(viewModel: PrayerViewModel) {
    val context = LocalContext.current
    var currentTab by remember { mutableStateOf(Tabs.PRAYERS) }
    
    var showSplash by remember { mutableStateOf(true) }
    val isLoggedIn by viewModel.isLoggedIn.collectAsState()

    // Launcher for Location permissions
    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val fineGranted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] ?: false
        val coarseGranted = permissions[Manifest.permission.ACCESS_COARSE_LOCATION] ?: false
        if (fineGranted || coarseGranted) {
            viewModel.fetchGPSLocation(context)
            Toast.makeText(context, "Location sync active", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(context, "GPS permission denied. Using customizable coordinates.", Toast.LENGTH_LONG).show()
        }
    }

    // Trigger permission requests safely once user is logged in and splash layout has finished
    LaunchedEffect(isLoggedIn, showSplash) {
        if (!showSplash && isLoggedIn) {
            val fineCheck = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION)
            val coarseCheck = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION)
            if (fineCheck != PackageManager.PERMISSION_GRANTED && coarseCheck != PackageManager.PERMISSION_GRANTED) {
                locationPermissionLauncher.launch(
                    arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION)
                )
            } else {
                viewModel.fetchGPSLocation(context)
            }
        }
    }

    if (showSplash) {
        SplashScreen(onTimeout = { showSplash = false })
    } else if (!isLoggedIn) {
        LoginSignupScreen(viewModel = viewModel, onAuthSuccess = {})
    } else {
        val isLoggingEnabled by viewModel.isLoggingEnabled.collectAsState()
        LaunchedEffect(isLoggingEnabled) {
            if (!isLoggingEnabled && currentTab == Tabs.STATS) {
                currentTab = Tabs.PRAYERS
            }
        }

        Scaffold(
            bottomBar = {
            NavigationBar(
                modifier = Modifier
                    .windowInsetsPadding(WindowInsets.navigationBars)
                    .testTag("app_bottom_navigation"),
                tonalElevation = 8.dp
            ) {
                val tabs = mutableListOf(
                    Tabs.PRAYERS to Icons.Default.Schedule,
                    Tabs.QURAN to Icons.Default.Book,
                    Tabs.WISDOM to Icons.Default.AutoAwesome,
                    Tabs.CALENDAR to Icons.Default.CalendarMonth,
                    Tabs.QIBLA to Icons.Default.Explore
                ).apply {
                    if (isLoggingEnabled) {
                        add(Tabs.STATS to Icons.Default.BarChart)
                    }
                    add(Tabs.SETTINGS to Icons.Default.Settings)
                }

                tabs.forEach { (tabName, icon) ->
                    NavigationBarItem(
                        selected = currentTab == tabName,
                        onClick = { currentTab = tabName },
                        icon = { Icon(icon, contentDescription = tabName) },
                        label = { Text(tabName, fontSize = 9.sp, fontWeight = FontWeight.SemiBold, maxLines = 1) },
                        modifier = Modifier.testTag("nav_item_${tabName.lowercase()}")
                    )
                }
            }
        },
        contentWindowInsets = WindowInsets.safeDrawing
    ) { paddingValues ->
        val currentTheme by viewModel.currentThemeName.collectAsState()
        Crossfade(
            targetState = currentTab,
            animationSpec = tween(250),
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .animatedGradientBackground(currentTheme)
                .background(if (currentTheme == "aurora_live" || currentTheme == "nebula_live") Color.Transparent else MaterialTheme.colorScheme.background)
        ) { tab ->
            when (tab) {
                Tabs.PRAYERS -> PrayersScreen(
                    viewModel = viewModel,
                    onNavigateToQibla = { currentTab = Tabs.QIBLA },
                    onNavigateToQuran = { currentTab = Tabs.QURAN },
                    onNavigateToStats = { currentTab = Tabs.STATS },
                    onNavigateToSettings = { currentTab = Tabs.SETTINGS }
                )
                Tabs.QURAN -> QuranScreen(viewModel)
                Tabs.WISDOM -> HadithDuaScreen(viewModel)
                Tabs.CALENDAR -> CalendarScreen(viewModel)
                Tabs.QIBLA -> QiblaScreen(viewModel)
                Tabs.STATS -> StatsScreen(viewModel)
                Tabs.SETTINGS -> SettingsScreen(viewModel, onBack = { currentTab = Tabs.PRAYERS })
            }
        }
    }
}
}

@Composable
fun PrayersScreen(
    viewModel: PrayerViewModel,
    onNavigateToQibla: () -> Unit,
    onNavigateToQuran: () -> Unit,
    onNavigateToStats: () -> Unit,
    onNavigateToSettings: () -> Unit
) {
    val context = LocalContext.current
    val isLoggingEnabled by viewModel.isLoggingEnabled.collectAsState()
    val times by viewModel.prayerTimes.collectAsState()
    val todayLogs by viewModel.todayLogs.collectAsState()
    val nextInfo by viewModel.nextPrayerInfo.collectAsState()
    val activeSchool by viewModel.asrSchool.collectAsState()
    val selectedDateStr by viewModel.selectedDate.collectAsState()
    val label by viewModel.locationLabel.collectAsState()
    val quranEntries by viewModel.quranEntries.collectAsState()
    val allLogs by viewModel.allPrayerLogs.collectAsState()
    val loggedInUser by viewModel.loggedInUser.collectAsState()
    val dateQueueTrigger by viewModel.dateQueueTrigger.collectAsState()

    var showConfigDialog by remember { mutableStateOf(false) }
    var tempLat by remember { mutableStateOf(viewModel.latitude.value.toString()) }
    var tempLon by remember { mutableStateOf(viewModel.longitude.value.toString()) }
    var tempFajrAngle by remember { mutableStateOf(viewModel.fajrAngle.value.toString()) }
    var tempIshaAngle by remember { mutableStateOf(viewModel.ishaAngle.value.toString()) }

    var logDialogState by remember { mutableStateOf<String?>(null) } // holds name of prayer being detailed

    var tasbihCount by rememberSaveable { mutableStateOf(0) }
    var tasbihTarget by rememberSaveable { mutableStateOf(33) } // 33, 99, 100, or 0 (Infinite)
    val tasbihPhrases = listOf(
        Triple("Subhanallah", "سُبْحَانَ ٱللَّهِ", "Glory be to Allah"),
        Triple("Alhamdulillah", "ٱلْحَمْدُ لِلَّهِ", "Praise be to Allah"),
        Triple("Allahu Akbar", "ٱللَّهُ أَكْبَرُ", "Allah is the Greatest"),
        Triple("Astaghfirullah", "أَسْتَغْفِرُ ٱللَّهَ", "I seek forgiveness from Allah"),
        Triple("Subhanallahi wa bihamdihi", "سُبْحَانَ اللَّهِ وَبِحَمْدِهِ", "Glory and praise be to Allah"),
        Triple("La ilaha illallah", "لَا إِلَٰهَ إِلَّا ٱللَّهُ", "There is no deity but Allah")
    )
    var selectedPhraseIndex by rememberSaveable { mutableStateOf(0) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // --- AFAH BRANDING HEADER ROW (Elegant Dark style) ---
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Afah",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary, // #D0BCFF
                    letterSpacing = (-0.5).sp
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF4CAF50)) // bg-green-500
                    )
                    Text(
                        text = "$label",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                }
            }
            
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Beautiful Settings Quick Action Button
                IconButton(
                    onClick = onNavigateToSettings,
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f))
                ) {
                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = "Open Settings",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                }

                // User Avatar Box with border matching theme configuration
                val initials = if (loggedInUser.isNotEmpty()) loggedInUser.take(2).uppercase() else "JS"
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.secondaryContainer)
                        .border(1.dp, MaterialTheme.colorScheme.outline, CircleShape)
                        .clickable { onNavigateToSettings() }
                        .testTag("location_config_btn"),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = initials,
                        color = MaterialTheme.colorScheme.onSecondaryContainer,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                }
            }
        }

        if (loggedInUser.isNotEmpty()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 4.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Default.Person,
                    contentDescription = null,
                    tint = Color(0xFFD4AF37),
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "As-salamu alaykum, $loggedInUser",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.weight(1f))
                TextButton(
                    onClick = { viewModel.logout() },
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                ) {
                    Text("Sign Out", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.error)
                }
            }
            Spacer(modifier = Modifier.height(10.dp))
        }

        Spacer(modifier = Modifier.height(6.dp))

        // --- COUNTDOWN / LIVE TIMER BANNER (Elegant Dark Custom Card) ---
        val nextPrayerName = nextInfo.first
        val nextPrayerTime = remember(nextPrayerName, times) {
            val nameClean = nextPrayerName.replace(" (Tomorrow)", "").trim()
            when (nameClean) {
                "Fajr Adhan" -> times.fajr
                "Fajr Namaj" -> {
                    if (viewModel.fajrOverrideNamaj.value.isNotEmpty()) viewModel.fajrOverrideNamaj.value
                    else {
                        val adhan = if (viewModel.fajrOverrideAdhan.value.isNotEmpty()) viewModel.fajrOverrideAdhan.value else times.fajr
                        viewModel.adjustTime(adhan, viewModel.fajrIqamahDelay.value)
                    }
                }
                "Ishraq Namaj" -> viewModel.adjustTime(times.sunrise, 15)
                "Duha Namaj" -> "08:30"
                "Dhuhr Adhan" -> times.dhuhr
                "Dhuhr Namaj" -> {
                    if (viewModel.dhuhrOverrideNamaj.value.isNotEmpty()) viewModel.dhuhrOverrideNamaj.value
                    else {
                        val adhan = if (viewModel.dhuhrOverrideAdhan.value.isNotEmpty()) viewModel.dhuhrOverrideAdhan.value else times.dhuhr
                        viewModel.adjustTime(adhan, viewModel.dhuhrIqamahDelay.value)
                    }
                }
                "Asr Adhan" -> times.asr
                "Asr Namaj" -> {
                    if (viewModel.asrOverrideNamaj.value.isNotEmpty()) viewModel.asrOverrideNamaj.value
                    else {
                        val adhan = if (viewModel.asrOverrideAdhan.value.isNotEmpty()) viewModel.asrOverrideAdhan.value else times.asr
                        viewModel.adjustTime(adhan, viewModel.asrIqamahDelay.value)
                    }
                }
                "Maghrib Adhan" -> times.maghrib
                "Maghrib Namaj" -> {
                    if (viewModel.maghribOverrideNamaj.value.isNotEmpty()) viewModel.maghribOverrideNamaj.value
                    else {
                        val adhan = if (viewModel.maghribOverrideAdhan.value.isNotEmpty()) viewModel.maghribOverrideAdhan.value else times.maghrib
                        viewModel.adjustTime(adhan, viewModel.maghribIqamahDelay.value)
                    }
                }
                "Awabin Namaj" -> viewModel.adjustTime(times.maghrib, 15)
                "Isha Adhan" -> times.isha
                "Isha Namaj" -> {
                    if (viewModel.ishaOverrideNamaj.value.isNotEmpty()) viewModel.ishaOverrideNamaj.value
                    else {
                        val adhan = if (viewModel.ishaOverrideAdhan.value.isNotEmpty()) viewModel.ishaOverrideAdhan.value else times.isha
                        viewModel.adjustTime(adhan, viewModel.ishaIqamahDelay.value)
                    }
                }
                "Tahajjud Namaj" -> "02:00"
                "Fajr" -> times.fajr
                "Sunrise" -> times.sunrise
                "Dhuhr" -> times.dhuhr
                "Asr" -> times.asr
                "Maghrib" -> times.maghrib
                "Isha" -> times.isha
                else -> "--:--"
            }
        }

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.25f))
        ) {
            Box(modifier = Modifier.fillMaxWidth()) {
                // Background Mosque Skyline & Celestial ornaments
                MosqueDomeAndMinaretBackground(
                    modifier = Modifier.matchParentSize(),
                    primaryColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.16f),
                    secondaryColor = MaterialTheme.colorScheme.secondary.copy(alpha = 0.08f),
                    prayerPhase = nextPrayerName
                )

                Column(modifier = Modifier.padding(24.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "NEXT PRAYER",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.5.sp
                        )
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .background(MaterialTheme.colorScheme.primary)
                                .padding(horizontal = 8.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "Live",
                                color = MaterialTheme.colorScheme.onPrimary,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = nextPrayerName,
                        style = MaterialTheme.typography.headlineLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = nextInfo.second,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.90f),
                        modifier = Modifier.testTag("prayer_countdown_timer")
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(
                                text = if (nextPrayerTime.isNotEmpty()) nextPrayerTime else "--:--",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "ADHAN TIME",
                                fontSize = 10.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Box(
                            modifier = Modifier
                                .height(36.dp)
                                .width(1.dp)
                                .background(MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                        )

                        Button(
                            onClick = onNavigateToQibla,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary,
                                contentColor = MaterialTheme.colorScheme.onPrimary
                            ),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("Qibla Finder", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                        }
                    }
                }
            }
        }

        // --- DASHBOARD GRID CARDS (Daily Quran & Prayer Log / Daily Supplication) ---
        val lastReadSurah = quranEntries.firstOrNull()?.surahName ?: "Surah Al-Fatihah"
        val quranProgressFrac = if (quranEntries.isNotEmpty()) 0.65f else 0.1f
        val completionRate = if (allLogs.isNotEmpty()) (allLogs.count { it.completed }.toFloat() / allLogs.size.toFloat() * 100).toInt() else 0
        val displayPct = if (allLogs.isNotEmpty()) "$completionRate%" else "0%"

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Daily Quran Card
            Card(
                modifier = Modifier
                    .weight(1f)
                    .height(112.dp)
                    .clickable { onNavigateToQuran() },
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF2B2930)),
                border = BorderStroke(1.dp, Color(0xFF49454F))
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Daily Quran",
                        fontSize = 12.sp,
                        color = Color(0xFFCAC4D0),
                        fontWeight = FontWeight.Medium
                    )
                    Column {
                        Text(
                            text = lastReadSurah,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color.White
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        // Mini progress indicator
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(4.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF49454F))
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxHeight()
                                    .fillMaxWidth(quranProgressFrac)
                                    .clip(CircleShape)
                                    .background(Color(0xFFD0BCFF))
                            )
                        }
                    }
                }
            }

            if (isLoggingEnabled) {
                // Prayer Log Card (Visible only when logging is enabled)
                Card(
                    modifier = Modifier
                        .weight(1f)
                        .height(112.dp)
                        .clickable { onNavigateToStats() },
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF2B2930)),
                    border = BorderStroke(1.dp, Color(0xFF49454F))
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Prayer Log",
                            fontSize = 12.sp,
                            color = Color(0xFFCAC4D0),
                            fontWeight = FontWeight.Medium
                        )
                        Column {
                            Text(
                                text = "$displayPct Rate",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color.White
                            )
                            Text(
                                text = if (completionRate > 50) "+5% from yesterday" else "Consistent spirit",
                                fontSize = 11.sp,
                                color = Color(0xFF4CAF50),
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            } else {
                // Tailored Morning/Evening Supplication or Daily Wisdom Card
                val isMorningDua = java.util.Calendar.getInstance().get(java.util.Calendar.HOUR_OF_DAY) in 4..11
                val isAfternoonDua = java.util.Calendar.getInstance().get(java.util.Calendar.HOUR_OF_DAY) in 12..17

                val selectedDuaOrHadith = if (isMorningDua) {
                    Triple(
                        "Morning Supplication",
                        "الْحَمْدُ لِلَّهِ الَّذِي أَحْيَانَا بَعْدَ مَا أَمَاتَنَا",
                        "Praise be to Allah who gave us life after death."
                    )
                } else if (isAfternoonDua) {
                    Triple(
                        "Knowledge & Wisdom",
                        "Actions are judged by intentions...",
                        "Sahih al-Bukhari"
                    )
                } else {
                    Triple(
                        "Night Supplication",
                        "بِاسْمِكَ اللَّهُمَّ أَمُوتُ وَأَحْيَا",
                        "In Your name, O Allah, I die and I live."
                    )
                }

                Card(
                    modifier = Modifier
                        .weight(1f)
                        .height(112.dp)
                        .clickable { /* No-op or dynamic detail action */ },
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.25f)),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.35f))
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = if (isMorningDua) Icons.Default.WbSunny else Icons.Default.WbTwilight,
                                contentDescription = null,
                                tint = if (isMorningDua) Color(0xFFECC27E) else Color(0xFFFF8A65),
                                modifier = Modifier.size(14.dp)
                            )
                            Text(
                                text = selectedDuaOrHadith.first,
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Column {
                            Text(
                                text = selectedDuaOrHadith.second,
                                fontSize = 11.sp,
                                maxLines = 2,
                                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface,
                                lineHeight = 13.sp
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = selectedDuaOrHadith.third,
                                fontSize = 9.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                                maxLines = 1,
                                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            }
        }

        // --- HORIZONTAL CALENDAR DATE SELECTOR ---
        HorizontalDateSelector(selectedDateStr, viewModel) { dateStr ->
            viewModel.selectDate(dateStr)
        }

        Spacer(modifier = Modifier.height(16.dp))

        // --- PRAYER TIMES LIST WITH CHECKLOGGERS ---
        val fDelay by viewModel.fajrIqamahDelay.collectAsState()
        val dDelay by viewModel.dhuhrIqamahDelay.collectAsState()
        val aDelay by viewModel.asrIqamahDelay.collectAsState()
        val mDelay by viewModel.maghribIqamahDelay.collectAsState()
        val iDelay by viewModel.ishaIqamahDelay.collectAsState()

        val delayForPrayer = mapOf(
            "Fajr" to fDelay,
            "Dhuhr" to dDelay,
            "Asr" to aDelay,
            "Maghrib" to mDelay,
            "Isha" to iDelay
        )

        fun getIqamahTime(timeStr: String, delayMinutes: Int): String {
            if (timeStr.isEmpty()) return ""
            try {
                val parts = timeStr.trim().split(":")
                if (parts.size != 2) return ""
                val hrs = parts[0].toIntOrNull() ?: return ""
                val mins = parts[1].toIntOrNull() ?: return ""
                val totalMinutes = (hrs * 60 + mins + delayMinutes + 1440) % 1440
                val newHrs = totalMinutes / 60
                val newMins = totalMinutes % 60
                return String.format("%02d:%02d", newHrs, newMins)
            } catch (e: Exception) {
                return ""
            }
        }

        val expandedSubPrayers = remember { mutableStateMapOf<String, Boolean>() }

        val prayers = listOf(
            "Fajr" to times.fajr,
            "Sunrise" to times.sunrise,
            "Dhuhr" to times.dhuhr,
            "Asr" to times.asr,
            "Sunset" to times.sunset,
            "Maghrib" to times.maghrib,
            "Isha" to times.isha
        )

        prayers.forEach { (name, time) ->
            val log = todayLogs.find { it.prayerName == name }
            val completed = log?.completed ?: false
            val isMissed = log != null && !log.completed
            val isCelestial = name == "Sunrise" || name == "Sunset"

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 6.dp)
                    .testTag("prayer_card_${name.lowercase()}"),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (completed) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.40f)
                    else if (isMissed) MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.22f)
                    else MaterialTheme.colorScheme.surface
                ),
                border = BorderStroke(
                    1.dp,
                    if (completed) MaterialTheme.colorScheme.primary.copy(alpha = 0.60f)
                    else if (isMissed) MaterialTheme.colorScheme.error.copy(alpha = 0.60f)
                    else MaterialTheme.colorScheme.outlineVariant
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                name,
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = if (isCelestial) MaterialTheme.colorScheme.onSurface.copy(alpha = 0.60f) else MaterialTheme.colorScheme.onSurface
                            )
                            if (log?.prayedInCongregation == true) {
                                Spacer(modifier = Modifier.width(6.dp))
                                SuggestionChip(
                                    onClick = {},
                                    label = { Text("Jama'at", fontSize = 10.sp) },
                                    colors = SuggestionChipDefaults.suggestionChipColors(
                                        containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                                    )
                                )
                            }
                            if (log?.isQada == true) {
                                Spacer(modifier = Modifier.width(6.dp))
                                SuggestionChip(
                                    onClick = {},
                                    label = { Text("Qada", fontSize = 10.sp) },
                                    colors = SuggestionChipDefaults.suggestionChipColors(
                                        containerColor = MaterialTheme.colorScheme.error.copy(alpha = 0.15f)
                                    )
                                )
                            }
                            if (isMissed) {
                                Spacer(modifier = Modifier.width(6.dp))
                                SuggestionChip(
                                    onClick = {},
                                    label = { Text("Missed", fontSize = 10.sp) },
                                    colors = SuggestionChipDefaults.suggestionChipColors(
                                        containerColor = MaterialTheme.colorScheme.error.copy(alpha = 0.20f),
                                        labelColor = MaterialTheme.colorScheme.error
                                    )
                                )
                            }
                        }
                        val subPrayersStr = when (name) {
                            "Fajr" -> "2 Sunnah (Muakkadah) • 2 Fard"
                            "Dhuhr" -> "4 Sunnah • 4 Fard • 2 Sunnah • 2 Nafl"
                            "Asr" -> "4 Sunnah (G. Muakkadah) • 4 Fard"
                            "Maghrib" -> "3 Fard • 2 Sunnah • 2 Nafl"
                            "Isha" -> "4 Fard • 2 Sunnah • 2 Nafl • 3 Witr (Wajib) • 2 Nafl"
                            else -> ""
                        }
                        if (subPrayersStr.isNotEmpty()) {
                            val isExpanded = expandedSubPrayers[name] == true
                            Row(
                                modifier = Modifier
                                    .padding(top = 2.dp, bottom = 4.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.25f))
                                    .clickable { expandedSubPrayers[name] = !isExpanded }
                                    .padding(horizontal = 8.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Text(
                                    text = "Sunnah & Nafl",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.secondary,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = if (isExpanded) "⌃" else "⌄",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.secondary,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            AnimatedVisibility(
                                visible = isExpanded,
                                enter = expandVertically() + fadeIn(),
                                exit = shrinkVertically() + fadeOut()
                            ) {
                                Column(
                                    modifier = Modifier
                                        .padding(top = 6.dp, bottom = 8.dp)
                                        .fillMaxWidth(),
                                    verticalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    val parts = subPrayersStr.split(" • ")
                                    parts.forEach { part ->
                                        val isFard = part.contains("Fard")
                                        val isWitr = part.contains("Witr")
                                        val isSunnah = part.contains("Sunnah")
                                        
                                        val badgeColor = when {
                                            isFard -> MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.65f)
                                            isWitr -> MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.65f)
                                            isSunnah -> MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.55f)
                                            else -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.65f)
                                        }
                                        val onBadgeColor = when {
                                            isFard -> MaterialTheme.colorScheme.onPrimaryContainer
                                            isWitr -> MaterialTheme.colorScheme.onTertiaryContainer
                                            isSunnah -> MaterialTheme.colorScheme.onSecondaryContainer
                                            else -> MaterialTheme.colorScheme.onSurfaceVariant
                                        }

                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .size(6.dp)
                                                    .clip(CircleShape)
                                                    .background(if (isFard) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary)
                                            )
                                            
                                            Box(
                                                modifier = Modifier
                                                    .clip(RoundedCornerShape(8.dp))
                                                    .background(badgeColor)
                                                    .padding(horizontal = 8.dp, vertical = 3.dp)
                                            ) {
                                                Text(
                                                    text = part,
                                                    style = MaterialTheme.typography.bodySmall,
                                                    color = onBadgeColor,
                                                    fontWeight = if (isFard) FontWeight.Bold else FontWeight.Medium
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                        Text(
                            text = if (time.isNotEmpty()) time else "--:--",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        if (!isCelestial && time.isNotEmpty()) {
                            // Automatically fetch the dynamic delay (either specific date delay or custom global standard delay)
                            val delayMin = remember(name, selectedDateStr, dateQueueTrigger) {
                                viewModel.getQueueDelayForPrayer(name, selectedDateStr)
                            }
                            val iqamahTime = getIqamahTime(time, delayMin)
                            Text(
                                text = "Queue/Jama'at: $iqamahTime (+$delayMin mins)",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }

                    if (!isCelestial) {
                        if (isLoggingEnabled) {
                            val timePassed = isPrayerTimePassed(time, selectedDateStr)
                            IconButton(
                                onClick = {
                                    if (log != null) {
                                        // Reset log state if clicked again when already logged
                                        viewModel.togglePrayer(name, false)
                                    } else {
                                        if (timePassed) {
                                            // Open options log dialog immediately
                                            logDialogState = name
                                        } else {
                                            Toast.makeText(context, "Cannot log $name in advance! Locked until real time ($time).", Toast.LENGTH_SHORT).show()
                                        }
                                    }
                                },
                                modifier = Modifier
                                    .size(48.dp)
                                    .testTag("checkbox_${name.lowercase()}")
                            ) {
                                Icon(
                                    imageVector = if (completed) {
                                        Icons.Default.CheckCircle
                                    } else if (isMissed) {
                                        Icons.Default.Cancel
                                    } else if (!timePassed) {
                                        Icons.Default.Lock
                                    } else {
                                        Icons.Default.RadioButtonUnchecked
                                    },
                                    contentDescription = "Log $name",
                                    tint = if (completed) {
                                        MaterialTheme.colorScheme.primary
                                    } else if (isMissed) {
                                        MaterialTheme.colorScheme.error
                                    } else if (!timePassed) {
                                        MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                                    } else {
                                        MaterialTheme.colorScheme.outline
                                    },
                                    modifier = Modifier.size(32.dp)
                                )
                            }
                        } else {
                            Icon(
                                imageVector = Icons.Default.NotificationsActive,
                                contentDescription = "Active Notifications Alert",
                                tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.75f),
                                modifier = Modifier
                                    .size(32.dp)
                                    .padding(4.dp)
                            )
                        }
                    } else {
                        Icon(
                            imageVector = if (name == "Sunrise") Icons.Default.WbSunny else Icons.Default.WbTwilight,
                            contentDescription = name,
                            tint = if (name == "Sunrise") Color(0xFFECC27E) else Color(0xFFFF8A65),
                            modifier = Modifier.size(28.dp)
                        )
                    }
                }
            }
        }

        // --- DIGITAL TASBIH CARD ---
        Spacer(modifier = Modifier.height(16.dp))
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("digital_tasbih_card"),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f))
        ) {
            val haptic = LocalHapticFeedback.current
            val currentPhrase = tasbihPhrases[selectedPhraseIndex]

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Header of Tasbih
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Adjust,
                            contentDescription = "Tasbih Icon",
                            tint = Color(0xFFD4AF37),
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Interactive Digital Tasbih",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    
                    // Reset Button
                    IconButton(
                        onClick = {
                            tasbihCount = 0
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        },
                        modifier = Modifier
                            .size(32.dp)
                            .background(MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f), CircleShape)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Reset Tasbih",
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Phase Phrase Selector/Display
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.6f)),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                ) {
                    var expandedMenu by remember { mutableStateOf(false) }

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { expandedMenu = true }
                            .padding(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = currentPhrase.first,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Text(
                                    text = currentPhrase.third,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = currentPhrase.second,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp,
                                    color = Color(0xFFD4AF37)
                                )
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = "Change",
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.secondary,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                    Icon(
                                        imageVector = Icons.Default.ArrowDropDown,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.secondary,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }

                        DropdownMenu(
                            expanded = expandedMenu,
                            onDismissRequest = { expandedMenu = false },
                            modifier = Modifier.background(MaterialTheme.colorScheme.surface)
                        ) {
                            tasbihPhrases.forEachIndexed { idx, item ->
                                DropdownMenuItem(
                                    text = {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Column {
                                                Text(item.first, fontWeight = FontWeight.Bold)
                                                Text(item.third, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                            }
                                            Spacer(modifier = Modifier.width(24.dp))
                                            Text(item.second, color = Color(0xFFD4AF37), fontWeight = FontWeight.Bold)
                                        }
                                    },
                                    onClick = {
                                        selectedPhraseIndex = idx
                                        expandedMenu = false
                                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                    }
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Target Limit Selector (33, 99, 100, Free Mode)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val listTargets = listOf(
                        33 to "33 Limit",
                        99 to "99 Limit",
                        100 to "100 Limit",
                        0 to "Free Mode"
                    )
                    listTargets.forEach { (target, label) ->
                        val isSelected = tasbihTarget == target
                        OutlinedButton(
                            onClick = {
                                tasbihTarget = target
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            },
                            modifier = Modifier.height(32.dp),
                            colors = ButtonDefaults.outlinedButtonColors(
                                containerColor = if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent,
                                contentColor = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                            ),
                            border = BorderStroke(1.dp, if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)),
                            contentPadding = PaddingValues(horizontal = 8.dp)
                        ) {
                            Text(text = label, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Radial Progress Medallion + Big Tap Finger Button
                Box(
                    modifier = Modifier
                        .size(160.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.8f))
                        .border(
                            2.dp,
                            Brush.linearGradient(
                                listOf(
                                    MaterialTheme.colorScheme.primary,
                                    Color(0xFFD4AF37)
                                )
                            ),
                            CircleShape
                        )
                        .clickable {
                            val nextCount = tasbihCount + 1
                            if (tasbihTarget > 0 && nextCount >= tasbihTarget) {
                                tasbihCount = tasbihTarget
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                Toast.makeText(context, "Completed! Praise be to Allah.", Toast.LENGTH_SHORT).show()
                            } else {
                                tasbihCount = nextCount
                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            }
                        }
                        .testTag("tasbih_counter_button"),
                    contentAlignment = Alignment.Center
                ) {
                    // Draw a subtle animated progress arc
                    Canvas(modifier = Modifier.fillMaxSize().padding(8.dp)) {
                        val angle = if (tasbihTarget > 0) {
                            (tasbihCount.toFloat() / tasbihTarget.toFloat()) * 360f
                        } else {
                            (tasbihCount % 33 / 33f) * 360f
                        }
                        drawArc(
                            color = Color(0xFFD4AF37).copy(alpha = 0.15f),
                            startAngle = -90f,
                            sweepAngle = 360f,
                            useCenter = false,
                            style = Stroke(width = 4.dp.toPx())
                        )
                        drawArc(
                            color = Color(0xFFD4AF37),
                            startAngle = -90f,
                            sweepAngle = angle,
                            useCenter = false,
                            style = Stroke(width = 6.dp.toPx())
                        )
                    }

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = if (tasbihTarget > 0) "$tasbihCount / $tasbihTarget" else "$tasbihCount",
                            style = MaterialTheme.typography.displaySmall,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.onSurface,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.TouchApp,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.secondary.copy(alpha = 0.8f),
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = "TAP",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.8f),
                                letterSpacing = 1.sp
                            )
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(8.dp))
            }
        }

        // --- OPTIONAL / SUNNAH NAFILAH PRAYERS SECTION ---
        Spacer(modifier = Modifier.height(16.dp))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Default.Star, contentDescription = "Sunnah Icon", tint = Color(0xFFD4AF37), modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Optional & Sunnah Prayers (Nafilah)",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
        }
        Spacer(modifier = Modifier.height(8.dp))

        val optionalPrayers = listOf(
            Triple("Tahajjud", "Tahajjud (Night Vigil): High spiritual prayer in the last third of night before Fajr", "02:00 - 04:30 AM"),
            Triple("Tahiyyatul Wudu", "Tahiyyatul Wudu (Ablution Prayer): 2 Rakahs offered immediately after performing Wudu", "Right after Wudu"),
            Triple("Tahiyyatul Masjid", "Tahiyyatul Masjid (Mosque Prayer): 2 Rakahs on entering Masjid before sitting down", "On entering Masjid"),
            Triple("Ishraq", "Ishraq (Post-Sunrise Sunnah): 15-20 minutes after Sunrise for spiritual sunrise reward", "15m after Sunrise"),
            Triple("Duha", "Duha / Chasht (Forenoon Namaj): Highly rewarded morning voluntary prayer of light", "08:30 - 11:30 AM"),
            Triple("Chasht", "Chasht (Mid-Morning Namaj): Prayed as the sun climbs high in the sky", "09:30 - 11:30 AM"),
            Triple("Salatul Tasbih", "Salatul Tasbih (Prayer of Glorification): Special 4 Rakahs with repeated Tasbihs", "Universal recommended times"),
            Triple("Salatul Hajat", "Salatul Hajat (Prayer of Need): Prayed to ask special help/wish from Allah in times of need", "Anytime in need"),
            Triple("Salatul Istikhara", "Salatul Istikhara (Prayer of Guidance): Prayed when seeking path / decision advice from Allah", "Anytime, ideally night"),
            Triple("Salatul Tawbah", "Salatul Tawbah (Prayer of Repentance): Offered to seek sincere remorse and forgiveness for sins", "Immediately after sin"),
            Triple("Awabin", "Awabin (Post-Maghrib): 6 Rakahs of sunnah prayer between Maghrib and Isha", "Between Maghrib & Isha"),
            Triple("Salatul Kusuf", "Salatul Kusuf / Khusuf (Eclipse Prayer): Prayed during Solar or Lunar Eclipse", "During Eclipse")
        )

        optionalPrayers.forEach { (optName, optDesc, optRecommendedTime) ->
            val optLog = todayLogs.find { it.prayerName == optName }
            val optCompleted = optLog?.completed ?: false

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 5.dp)
                    .testTag("optional_prayer_card_${optName.lowercase()}"),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (optCompleted) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f)
                    else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.20f)
                ),
                border = BorderStroke(
                    1.dp,
                    if (optCompleted) MaterialTheme.colorScheme.primary.copy(alpha = 0.50f)
                    else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.30f)
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            optName,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            optDesc,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Recommended: $optRecommendedTime",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFFD4AF37)
                        )
                    }

                    if (isLoggingEnabled) {
                        IconButton(
                            onClick = {
                                if (optCompleted) {
                                    viewModel.togglePrayer(optName, false)
                                } else {
                                    // Instantly log optional prayers
                                    viewModel.togglePrayer(optName, true)
                                    Toast.makeText(context, "$optName Logged successfully!", Toast.LENGTH_SHORT).show()
                                }
                            },
                            modifier = Modifier.size(40.dp)
                        ) {
                            Icon(
                                imageVector = if (optCompleted) Icons.Default.CheckCircle else Icons.Default.AddCircleOutline,
                                contentDescription = "Log $optName",
                                tint = if (optCompleted) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline,
                                modifier = Modifier.size(28.dp)
                            )
                        }
                    } else {
                        Icon(
                            imageVector = Icons.Default.NotificationsActive,
                            contentDescription = "Active Notification Alert",
                            tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.75f),
                            modifier = Modifier
                                .size(32.dp)
                                .padding(4.dp)
                        )
                    }
                }
            }
        }
    }

    // --- DETAILED LOG OPTIONS DIALOG ---
    logDialogState?.let { name ->
        Dialog(onDismissRequest = { logDialogState = null }) {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                var selectedOption by remember { mutableStateOf<String?>(null) }
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Log $name",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Please select the prayer status to submit",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    val options = listOf(
                        Triple("alone", "Prayed Individually (Alone)", "Offered prayer on-time by yourself"),
                        Triple("jamaat", "Prayed in Congregation (Jama'at)", "Offered prayer with others in congregation"),
                        Triple("qada", "Prayed Late (Qada)", "Offered late after the prayer time elapsed"),
                        Triple("missed", "Missed / Not Prayed", "Register this prayer as skipped or missed")
                    )

                    options.forEach { (optionKey, title, desc) ->
                        val isSelected = selectedOption == optionKey
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                                .clickable { selectedOption = optionKey }
                                .testTag("log_option_$optionKey"),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f)
                                else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.25f)
                            ),
                            border = BorderStroke(
                                width = if (isSelected) 1.8.dp else 1.dp,
                                color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f)
                            )
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 12.dp, vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RadioButton(
                                    selected = isSelected,
                                    onClick = { selectedOption = optionKey },
                                    colors = RadioButtonDefaults.colors(selectedColor = MaterialTheme.colorScheme.primary)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text(
                                        text = title,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = desc,
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = {
                                val opt = selectedOption ?: return@Button
                                when (opt) {
                                    "alone" -> {
                                        viewModel.togglePrayer(prayerName = name, completed = true, isCongregation = false, isQada = false)
                                    }
                                    "jamaat" -> {
                                        viewModel.togglePrayer(prayerName = name, completed = true, isCongregation = true, isQada = false)
                                    }
                                    "qada" -> {
                                        viewModel.togglePrayer(prayerName = name, completed = true, isCongregation = false, isQada = true)
                                    }
                                    "missed" -> {
                                        viewModel.logMissedPrayer(name)
                                    }
                                }
                                logDialogState = null
                            },
                            enabled = selectedOption != null,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(46.dp)
                                .testTag("btn_save_detailed_log")
                        ) {
                            Text("Submit Log", fontWeight = FontWeight.Bold)
                        }

                        TextButton(
                            onClick = { logDialogState = null },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Dismiss", color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun HorizontalDateSelector(
    selectedDateStr: String,
    viewModel: PrayerViewModel,
    onDateSelected: (String) -> Unit
) {
    val format = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    val formatDay = SimpleDateFormat("EEE", Locale.getDefault())
    val formatDateOnly = SimpleDateFormat("dd", Locale.getDefault())
    val formatMonthYear = SimpleDateFormat("MMMM yyyy", Locale.getDefault())

    val todayStr = remember { format.format(Date()) }
    val allLogs by viewModel.allPrayerLogs.collectAsState()

    var focusDate by remember {
        val d = try { format.parse(selectedDateStr) ?: Date() } catch(e: Exception) { Date() }
        mutableStateOf(d)
    }

    LaunchedEffect(selectedDateStr) {
        try {
            val d = format.parse(selectedDateStr)
            if (d != null) {
                focusDate = d
            }
        } catch (_: Exception) {}
    }

    val dates = remember(focusDate) {
        val list = mutableListOf<Date>()
        val cal = Calendar.getInstance()
        cal.time = focusDate
        cal.add(Calendar.DATE, -3)
        for (i in 0..6) {
            list.add(cal.time)
            cal.add(Calendar.DATE, 1)
        }
        list
    }

    var showMonthDialog by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Habit Calendar Tracker",
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = try { formatMonthYear.format(focusDate) } catch (e: Exception) { "" },
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                )
            }

            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                TextButton(
                    onClick = {
                        val tStr = format.format(Date())
                        onDateSelected(tStr)
                        focusDate = Date()
                    },
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                    modifier = Modifier.height(32.dp)
                ) {
                    Text("Today", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }

                IconButton(
                    onClick = { showMonthDialog = true },
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.CalendarMonth,
                        contentDescription = "Interactive Monthly Habit Calendar",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = {
                    val cal = Calendar.getInstance()
                    cal.time = focusDate
                    cal.add(Calendar.DATE, -3)
                    focusDate = cal.time
                },
                modifier = Modifier.size(32.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.ChevronLeft,
                    contentDescription = "Previous Days",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(20.dp)
                )
            }

            LazyRow(
                modifier = Modifier.weight(1f),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                items(dates) { date ->
                    val dateStr = format.format(date)
                    val dayStr = formatDay.format(date)
                    val dayNum = formatDateOnly.format(date)
                    val isSelected = dateStr == selectedDateStr
                    val isToday = dateStr == todayStr

                    val completedCount = allLogs.filter { it.date == dateStr && it.completed }.size

                    Box(
                        modifier = Modifier
                            .padding(horizontal = 3.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(
                                if (isSelected) MaterialTheme.colorScheme.primary
                                else if (isToday) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
                                else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
                            )
                            .border(
                                1.dp,
                                if (isSelected) MaterialTheme.colorScheme.primary
                                else if (isToday) MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
                                else Color.Transparent,
                                RoundedCornerShape(12.dp)
                            )
                            .clickable {
                                onDateSelected(dateStr)
                                focusDate = date
                            }
                            .padding(vertical = 8.dp, horizontal = 10.dp)
                            .widthIn(min = 34.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = dayStr.uppercase(),
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isSelected) MaterialTheme.colorScheme.onPrimary 
                                        else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(3.dp))
                            Text(
                                text = dayNum,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Black,
                                color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                if (completedCount > 0) {
                                    val dotColor = if (completedCount >= 5) Color(0xFF2E7D32) else Color(0xFFEF6C00)
                                    Box(
                                        modifier = Modifier
                                            .size(5.dp)
                                            .clip(CircleShape)
                                            .background(if (isSelected) MaterialTheme.colorScheme.onPrimary else dotColor)
                                    )
                                } else {
                                    Box(
                                        modifier = Modifier
                                            .size(5.dp)
                                            .clip(CircleShape)
                                            .background(Color.Transparent)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            IconButton(
                onClick = {
                    val cal = Calendar.getInstance()
                    cal.time = focusDate
                    cal.add(Calendar.DATE, 3)
                    focusDate = cal.time
                },
                modifier = Modifier.size(32.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.ChevronRight,
                    contentDescription = "Next Days",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }

    if (showMonthDialog) {
        MonthlyHabitCalendarDialog(
            selectedDateStr = selectedDateStr,
            allLogs = allLogs,
            onDismiss = { showMonthDialog = false },
            onDateSelected = { dateStr ->
                onDateSelected(dateStr)
                try {
                    format.parse(dateStr)?.let { focusDate = it }
                } catch(_: Exception) {}
                showMonthDialog = false
            }
        )
    }
}

@Composable
fun MonthlyHabitCalendarDialog(
    selectedDateStr: String,
    allLogs: List<PrayerLog>,
    onDismiss: () -> Unit,
    onDateSelected: (String) -> Unit
) {
    val format = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    val formatMonthYear = SimpleDateFormat("MMMM yyyy", Locale.getDefault())
    val todayStr = remember { format.format(Date()) }

    var monthCal by remember {
        val cal = Calendar.getInstance()
        try {
            format.parse(selectedDateStr)?.let { cal.time = it }
        } catch (_: Exception) {}
        mutableStateOf(cal)
    }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = {
                            val newCal = Calendar.getInstance().apply {
                                time = monthCal.time
                                add(Calendar.MONTH, -1)
                            }
                            monthCal = newCal
                        }
                    ) {
                        Icon(Icons.Default.ChevronLeft, contentDescription = "Previous Month")
                    }

                    Text(
                        text = formatMonthYear.format(monthCal.time),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )

                    IconButton(
                        onClick = {
                            val newCal = Calendar.getInstance().apply {
                                time = monthCal.time
                                add(Calendar.MONTH, 1)
                            }
                            monthCal = newCal
                        }
                    ) {
                        Icon(Icons.Default.ChevronRight, contentDescription = "Next Month")
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                val daysOfWeekNames = listOf("M", "T", "W", "T", "F", "S", "S")
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    daysOfWeekNames.forEach { name ->
                        Text(
                            text = name,
                            modifier = Modifier.weight(1f),
                            textAlign = TextAlign.Center,
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                val daysGrid = remember(monthCal) {
                    val gridList = mutableListOf<Date?>()
                    val cal = Calendar.getInstance()
                    cal.time = monthCal.time
                    cal.set(Calendar.DAY_OF_MONTH, 1)
                    
                    val firstDayOfWeek = cal.get(Calendar.DAY_OF_WEEK)
                    val prefixEmptyCells = if (firstDayOfWeek == Calendar.SUNDAY) 6 else firstDayOfWeek - 2

                    for (i in 0 until prefixEmptyCells) {
                        gridList.add(null)
                    }

                    val totalDays = cal.getActualMaximum(Calendar.DAY_OF_MONTH)
                    for (i in 1..totalDays) {
                        cal.set(Calendar.DAY_OF_MONTH, i)
                        gridList.add(cal.time)
                    }
                    
                    while (gridList.size % 7 != 0) {
                        gridList.add(null)
                    }
                    gridList
                }

                val rows = daysGrid.chunked(7)
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    rows.forEach { rowDays ->
                        Row(modifier = Modifier.fillMaxWidth()) {
                            rowDays.forEach { date ->
                                if (date != null) {
                                    val dateStr = format.format(date)
                                    val isSelected = dateStr == selectedDateStr
                                    val isToday = dateStr == todayStr

                                    val cal = Calendar.getInstance()
                                    cal.time = date
                                    val dayNum = cal.get(Calendar.DAY_OF_MONTH).toString()

                                    val completedCount = allLogs.filter { it.date == dateStr && it.completed }.size

                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .aspectRatio(1f)
                                            .padding(2.dp)
                                            .clip(CircleShape)
                                            .background(
                                                if (isSelected) MaterialTheme.colorScheme.primary
                                                else if (isToday) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                                                else Color.Transparent
                                            )
                                            .border(
                                                1.dp,
                                                if (isSelected) MaterialTheme.colorScheme.primary
                                                else if (isToday) MaterialTheme.colorScheme.primary
                                                else Color.Transparent,
                                                CircleShape
                                            )
                                            .clickable { onDateSelected(dateStr) },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Column(
                                            horizontalAlignment = Alignment.CenterHorizontally,
                                            verticalArrangement = Arrangement.Center
                                        ) {
                                            Text(
                                                text = dayNum,
                                                style = MaterialTheme.typography.bodyMedium,
                                                fontWeight = if (isSelected || isToday) FontWeight.Black else FontWeight.Medium,
                                                color = if (isSelected) MaterialTheme.colorScheme.onPrimary
                                                        else if (isToday) MaterialTheme.colorScheme.onPrimaryContainer
                                                        else MaterialTheme.colorScheme.onSurface,
                                                fontSize = 12.sp
                                            )
                                            
                                            if (completedCount > 0) {
                                                val dotColor = if (completedCount >= 5) Color(0xFF2E7D32) else Color(0xFFEF6C00)
                                                Box(
                                                    modifier = Modifier
                                                        .padding(top = 2.dp)
                                                        .size(4.dp)
                                                        .clip(CircleShape)
                                                        .background(if (isSelected) MaterialTheme.colorScheme.onPrimary else dotColor)
                                                )
                                            }
                                        }
                                    }
                                } else {
                                    Spacer(modifier = Modifier.weight(1f).aspectRatio(1f))
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                Text(
                    text = "Habit Tracking Index",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                    modifier = Modifier.padding(bottom = 6.dp)
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(Color(0xFF2E7D32)))
                        Text("5/5 Logged (Fard completed)", fontSize = 9.sp, color = MaterialTheme.colorScheme.onSurface)
                    }
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(Color(0xFFEF6C00)))
                        Text("1-4 Logged", fontSize = 9.sp, color = MaterialTheme.colorScheme.onSurface)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Close", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuranScreen(viewModel: PrayerViewModel) {
    val context = LocalContext.current
    val progressEntries by viewModel.quranEntries.collectAsState()
    
    var showAddDialog by remember { mutableStateOf(false) }

    // Dialog state
    var selectedSurah by remember { mutableStateOf("Al-Fatihah") }
    var surahNumber by remember { mutableStateOf(1) }
    var startAyah by remember { mutableStateOf("") }
    var endAyah by remember { mutableStateOf("") }
    var readDuration by remember { mutableStateOf("") }
    var surahExpanded by remember { mutableStateOf(false) }
    var isManualLog by remember { mutableStateOf(true) }

    // Live Reading Session Timer States
    var timerSeconds by remember { mutableStateOf(0) }
    var timerRunning by remember { mutableStateOf(false) }

    LaunchedEffect(timerRunning) {
        if (timerRunning) {
            while (true) {
                delay(1000L)
                timerSeconds += 1
            }
        }
    }

    // Calculator for statistics
    val quranCount = progressEntries.size
    val totalSecondsSum = progressEntries.sumOf { it.durationSeconds }
    val totalMinutesSum = progressEntries.sumOf { it.durationMinutes } + (totalSecondsSum / 60)
    val remainingSeconds = totalSecondsSum % 60

    val totalReadString = remember(totalMinutesSum, remainingSeconds) {
        buildString {
            if (totalMinutesSum > 0) {
                append(totalMinutesSum)
                append(" min")
                if (totalMinutesSum > 1) append("s")
                append(" ")
            }
            if (remainingSeconds > 0 || totalMinutesSum == 0) {
                append(remainingSeconds)
                append(" sec")
                if (remainingSeconds > 1 || remainingSeconds == 0) append("s")
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // --- MOTIVATIONAL STATS CARD ---
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
            shape = RoundedCornerShape(16.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Qur'an Tracker",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Black,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Read $quranCount sessions • Total: $totalReadString reading",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                    )
                }
                Box(
                    modifier = Modifier
                        .size(54.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.MenuBook,
                        contentDescription = "Quran Books",
                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.size(28.dp)
                    )
                }
            }
        }

        // --- LIVE READING TIMER STOPWATCH CARD (THE "NEAT TIMER" THING) ---
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
        ) {
            Column(
                modifier = Modifier.padding(14.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Inline Surah Selection directly in the timer card
                Row(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Active Surah:",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Box {
                        OutlinedButton(
                            onClick = { surahExpanded = true },
                            modifier = Modifier.testTag("timer_surah_dropdown_btn"),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Text(text = "$surahNumber. $selectedSurah", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface)
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(Icons.Default.ArrowDropDown, contentDescription = "Dropdown", modifier = Modifier.size(16.dp))
                        }
                        DropdownMenu(
                            expanded = surahExpanded,
                            onDismissRequest = { surahExpanded = false },
                            modifier = Modifier.heightIn(max = 240.dp)
                        ) {
                            SURAH_LIST.forEachIndexed { index, name ->
                                DropdownMenuItem(
                                    text = { Text("${index + 1}. $name") },
                                    onClick = {
                                        selectedSurah = name
                                        surahNumber = index + 1
                                        surahExpanded = false
                                    }
                                )
                            }
                        }
                    }
                }

                HorizontalDivider(
                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f),
                    thickness = 1.dp,
                    modifier = Modifier.padding(bottom = 10.dp)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Live Quran Session",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = if (timerRunning) "Timer is ticking. Keep reading!" else "Start timer before you read.",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.outline
                        )
                    }

                    // Compute clean stopwatch format (H:M:S)
                    val hrs = timerSeconds / 3600
                    val mins = (timerSeconds % 3600) / 60
                    val secs = timerSeconds % 60
                    val formattedDuration = if (hrs > 0) {
                        String.format("%02d:%02d:%02d", hrs, mins, secs)
                    } else {
                        String.format("%02d:%02d", mins, secs)
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        if (timerRunning) {
                            // Pulsing color dot to indicate progression state
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFFE91E63))
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                        }
                        Text(
                            text = formattedDuration,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Black,
                            fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                            color = if (timerRunning) Color(0xFFD4AF37) else MaterialTheme.colorScheme.onSurface
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Play/Pause Button
                    Button(
                        onClick = { timerRunning = !timerRunning },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (timerRunning) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                        ),
                        modifier = Modifier.weight(1f).height(40.dp),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 4.dp)
                    ) {
                        Icon(
                            imageVector = if (timerRunning) Icons.Default.Pause else Icons.Default.PlayArrow,
                            contentDescription = if (timerRunning) "Pause" else "Start",
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(text = if (timerRunning) "Pause" else "Start", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                    }

                    // Reset button
                    OutlinedButton(
                        onClick = {
                            timerRunning = false
                            timerSeconds = 0
                        },
                        modifier = Modifier.weight(0.9f).height(40.dp),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Reset",
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(text = "Reset", fontSize = 11.sp)
                    }

                    // Auto-Log and Save button
                    Button(
                        onClick = {
                            val elapsedMins = timerSeconds / 60
                            val elapsedSecs = timerSeconds % 60
                            viewModel.addQuranProgress(
                                surahName = selectedSurah,
                                surahNum = surahNumber,
                                start = 1,
                                end = 1,
                                duration = elapsedMins,
                                seconds = elapsedSecs
                            )
                            timerRunning = false
                            timerSeconds = 0
                            val durationToastStr = if (elapsedMins > 0) {
                                "$elapsedMins mins $elapsedSecs secs"
                            } else {
                                "$elapsedSecs secs"
                            }
                            Toast.makeText(context, "Logged $durationToastStr of Surah $selectedSurah automatically!", Toast.LENGTH_LONG).show()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD4AF37)),
                        modifier = Modifier.weight(1.2f).height(40.dp).testTag("autolog_timer_btn"),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 4.dp),
                        enabled = timerSeconds > 0
                    ) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = "Auto log",
                            tint = Color.Black,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Auto-Log",
                            fontSize = 11.sp,
                            color = Color.Black,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        // --- BUTTON TO LOG NEW READING ---
        Button(
            onClick = { 
                isManualLog = true
                readDuration = ""
                showAddDialog = true 
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .testTag("log_quran_btn"),
            shape = RoundedCornerShape(12.dp)
        ) {
            Icon(Icons.Default.Add, contentDescription = "Add")
            Spacer(modifier = Modifier.width(8.dp))
            Text("Log Quran Progress manually", fontWeight = FontWeight.Bold)
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "READING HISTORY",
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        if (progressEntries.isEmpty()) {
            // Friendly Empty State
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        Icons.Default.Book,
                        contentDescription = "Empty Quran Tracker",
                        modifier = Modifier.size(64.dp),
                        tint = MaterialTheme.colorScheme.outlineVariant
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "No Quran reading logged offline yet.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.outline
                    )
                    Text(
                        text = "Start logs to build a consistent spiritual habit!",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.outline
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                items(progressEntries) { entry ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 6.dp)
                            .testTag("quran_progress_card_${entry.id}"),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "Surah ${entry.surahNumber}. ${entry.surahName}",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                val durationText = remember(entry.durationMinutes, entry.durationSeconds, entry.startAyah, entry.endAyah) {
                                    buildString {
                                        append("Ayah ")
                                        append(entry.startAyah)
                                        append(" to ")
                                        append(entry.endAyah)
                                        append(" • Read: ")
                                        if (entry.durationMinutes > 0 || entry.durationSeconds > 0) {
                                            if (entry.durationMinutes > 0) {
                                                append(entry.durationMinutes)
                                                append(" min")
                                                if (entry.durationMinutes > 1) append("s")
                                                append(" ")
                                            }
                                            if (entry.durationSeconds > 0) {
                                                append(entry.durationSeconds)
                                                append(" sec")
                                                if (entry.durationSeconds > 1) append("s")
                                            }
                                        } else {
                                            append("0 secs")
                                        }
                                    }
                                }
                                Text(
                                    text = durationText,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.outline
                                )
                                val formattedLogTime = remember(entry.timestamp, entry.date) {
                                    try {
                                        SimpleDateFormat("yyyy-MM-dd 'at' h:mm a", Locale.getDefault()).format(Date(entry.timestamp))
                                    } catch (e: Exception) {
                                        entry.date
                                    }
                                }
                                Text(
                                    text = "Logged: $formattedLogTime",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.secondary
                                )
                            }
                            IconButton(
                                onClick = { viewModel.deleteQuranProgress(entry.id) },
                                modifier = Modifier.testTag("delete_progress_btn_${entry.id}")
                            ) {
                                Icon(
                                    Icons.Default.Delete,
                                    contentDescription = "Delete log entry",
                                    tint = MaterialTheme.colorScheme.error.copy(alpha = 0.8f)
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // --- ADD DIALOG ---
    if (showAddDialog) {
        Dialog(onDismissRequest = { showAddDialog = false }) {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Column(
                    modifier = Modifier
                        .verticalScroll(rememberScrollState())
                        .padding(20.dp)
                ) {
                    Text(
                        text = "Log Quran Reading",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Black,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )

                    // Surah Dropdown selector
                    Text("Select Surah", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    Spacer(modifier = Modifier.height(4.dp))
                    Box(modifier = Modifier.fillMaxWidth()) {
                        OutlinedButton(
                            onClick = { surahExpanded = true },
                            modifier = Modifier.fillMaxWidth().testTag("surah_dropdown_btn")
                        ) {
                            Text("$surahNumber. $selectedSurah")
                            Icon(Icons.Default.ArrowDropDown, contentDescription = "Dropdown")
                        }
                        DropdownMenu(
                            expanded = surahExpanded,
                            onDismissRequest = { surahExpanded = false },
                            modifier = Modifier.heightIn(max = 240.dp)
                        ) {
                            SURAH_LIST.forEachIndexed { index, name ->
                                DropdownMenuItem(
                                    text = { Text("${index + 1}. $name") },
                                    onClick = {
                                        selectedSurah = name
                                        surahNumber = index + 1
                                        surahExpanded = false
                                    }
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = startAyah,
                        onValueChange = { startAyah = it },
                        label = { Text("Starting Ayah") },
                        modifier = Modifier.fillMaxWidth().testTag("input_start_ayah")
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = endAyah,
                        onValueChange = { endAyah = it },
                        label = { Text("Ending Ayah") },
                        modifier = Modifier.fillMaxWidth().testTag("input_end_ayah")
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    if (isManualLog) {
                        OutlinedTextField(
                            value = readDuration,
                            onValueChange = { readDuration = it },
                            label = { Text("Read Duration (Minutes)") },
                            modifier = Modifier.fillMaxWidth().testTag("input_duration")
                        )
                        Spacer(modifier = Modifier.height(24.dp))
                    } else {
                        Spacer(modifier = Modifier.height(6.dp))
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f))
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    Icons.Default.Timer,
                                    contentDescription = "Timer Icon",
                                    tint = MaterialTheme.colorScheme.primary
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Auto logged session: $readDuration min(s)",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(20.dp))
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        TextButton(onClick = { showAddDialog = false }) {
                            Text("Cancel", color = MaterialTheme.colorScheme.error)
                        }
                        Button(
                            onClick = {
                                val sAyah = startAyah.toIntOrNull() ?: 1
                                val eAyah = endAyah.toIntOrNull() ?: 1
                                val duration = readDuration.toIntOrNull() ?: 0

                                viewModel.addQuranProgress(
                                    surahName = selectedSurah,
                                    surahNum = surahNumber,
                                    start = sAyah,
                                    end = eAyah,
                                    duration = duration
                                )
                                if (!isManualLog) {
                                    timerSeconds = 0
                                    timerRunning = false
                                }
                                showAddDialog = false
                            },
                            modifier = Modifier.testTag("submit_log_quran_btn")
                        ) {
                            Text("Save Entry")
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun QiblaScreen(viewModel: PrayerViewModel) {
    val context = LocalContext.current
    DisposableEffect(Unit) {
        viewModel.registerCompassSensors(context)
        onDispose {
            viewModel.unregisterCompassSensors()
        }
    }

    val bearing by viewModel.kaabaBearing.collectAsState()
    val heading by viewModel.deviceHeading.collectAsState()

    // Calculate dynamic shortest angular difference between device heading and Mecca bearing
    val angleDiff = remember(heading, bearing) {
        val diff = (bearing - heading + 180) % 360 - 180
        if (diff < -180) diff + 360 else if (diff > 180) diff - 360 else diff
    }
    // Deemed aligned if within 5.0 degrees of accuracy
    val isAligned = kotlin.math.abs(angleDiff) < 5.0

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top
    ) {
        // --- EXPLANATORY QIBLA MODULE ---
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            colors = CardDefaults.cardColors(
                containerColor = if (isAligned) Color(0xFF1E3A1E) else MaterialTheme.colorScheme.surfaceVariant
            ),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Qibla Direction",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = if (isAligned) Color(0xFF81C784) else MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Real-time spherical bearing is computed relative to True North based on your precise GPS coordinates.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (isAligned) Color(0xFFE8F5E9) else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // --- ALIGNMENT BADGE STATUS ---
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(bottom = 12.dp)
        ) {
            if (isAligned) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = "Aligned",
                    tint = Color(0xFF4CAF50),
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "✦ ALIGNED WITH QIBLA ✦",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Black,
                    color = Color(0xFF4CAF50),
                    letterSpacing = 1.2.sp
                )
            } else {
                Text(
                    text = "Slowly rotate your phone to align",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.outline
                )
            }
        }

        // --- COMPASS GRAPHIC DRAW BLOCK ---
        val borderColor = if (isAligned) Color(0xFF4CAF50) else MaterialTheme.colorScheme.outlineVariant
        val borderThickness = if (isAligned) 4.dp else 2.dp

        Box(
            modifier = Modifier
                .size(280.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.surface)
                .border(borderThickness, borderColor, CircleShape)
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            // Compass Dial Canvas Drawing
            Canvas(modifier = Modifier.fillMaxSize()) {
                val center = Offset(size.width / 2, size.height / 2)
                val radius = size.width / 2

                // Draw outer ticks
                for (i in 0 until 360 step 30) {
                    val angleRad = (i - 90) * PI / 180
                    val start = Offset(
                        (center.x + (radius - 12) * cos(angleRad)).toFloat(),
                        (center.y + (radius - 12) * sin(angleRad)).toFloat()
                    )
                    val end = Offset(
                        (center.x + radius * cos(angleRad)).toFloat(),
                        (center.y + radius * sin(angleRad)).toFloat()
                    )
                    drawLine(
                        color = if (isAligned) Color(0xFF4CAF50).copy(alpha = 0.5f) else Color.Gray.copy(alpha = 0.5f),
                        start = start,
                        end = end,
                        strokeWidth = if (i % 90 == 0) 3.dp.toPx() else 1.5.dp.toPx()
                    )
                }

                // Draw Compass ring
                drawCircle(
                    color = if (isAligned) Color(0xFF4CAF50).copy(alpha = 0.25f) else Color.Green.copy(alpha = 0.1f),
                    radius = radius - 20,
                    style = Stroke(width = if (isAligned) 3.dp.toPx() else 2.dp.toPx())
                )
            }

            // Direction arrow rotates cleanly based on orientation and bearings
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer { rotationZ = -heading } // Rotate entire card relative to north optimized
                    .padding(12.dp)
                    .testTag("qibla_compass_dial"),
                contentAlignment = Alignment.Center
            ) {
                // North Marker
                Text(
                    text = "N",
                    fontWeight = FontWeight.Black,
                    fontSize = 20.sp,
                    color = if (isAligned) Color(0xFF4CAF50) else MaterialTheme.colorScheme.primary,
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .padding(top = 4.dp)
                )

                // East/West/South Indicators
                Text("E", style = MaterialTheme.typography.labelSmall, modifier = Modifier.align(Alignment.CenterEnd))
                Text("W", style = MaterialTheme.typography.labelSmall, modifier = Modifier.align(Alignment.CenterStart))
                Text("S", style = MaterialTheme.typography.labelSmall, modifier = Modifier.align(Alignment.BottomCenter))

                // 1. Circumference indicator representing Mecca (Kaaba) at the exact bearing
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .graphicsLayer { rotationZ = bearing.toFloat() }
                ) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopCenter)
                            .padding(top = 10.dp)
                            .size(34.dp)
                            .clip(RoundedCornerShape(6.dp))
                            .background(Color(0xFF1E1E1E))
                            .border(1.5.dp, Color(0xFFD4AF37), RoundedCornerShape(6.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(4.dp)
                                    .background(Color(0xFFD4AF37))
                            )
                            Spacer(modifier = Modifier.weight(1f))
                            Text("كعبة", fontSize = 8.sp, color = Color.White, fontWeight = FontWeight.Bold, maxLines = 1)
                        }
                    }
                }

                // 2. Central High-Fidelity 3D-Shaded Diamond Pointer pointing with absolute mathematical precision
                Canvas(
                    modifier = Modifier
                        .size(110.dp)
                        .graphicsLayer { rotationZ = bearing.toFloat() }
                        .testTag("qibla_compass_needle")
                ) {
                    val w = size.width
                    val h = size.height
                    val needleWidth = 14.dp.toPx()
                    val centerNotchOffset = 6.dp.toPx()

                    // Left half of the North indicator (Golden or Emerald green)
                    val pathNorthLeft = androidx.compose.ui.graphics.Path().apply {
                        moveTo(w / 2, 0f)
                        lineTo(w / 2 - needleWidth, h / 2)
                        lineTo(w / 2, h / 2 - centerNotchOffset)
                        close()
                    }
                    // Right half of the North indicator (slightly brighter for 3D/bevel lighting effect)
                    val pathNorthRight = androidx.compose.ui.graphics.Path().apply {
                        moveTo(w / 2, 0f)
                        lineTo(w / 2 + needleWidth, h / 2)
                        lineTo(w / 2, h / 2 - centerNotchOffset)
                        close()
                    }
                    // Left half of the South indicator (Silver-grey)
                    val pathSouthLeft = androidx.compose.ui.graphics.Path().apply {
                        moveTo(w / 2, h)
                        lineTo(w / 2 - needleWidth, h / 2)
                        lineTo(w / 2, h / 2 + centerNotchOffset)
                        close()
                    }
                    // Right half of the South indicator (slightly lighter Silver-grey)
                    val pathSouthRight = androidx.compose.ui.graphics.Path().apply {
                        moveTo(w / 2, h)
                        lineTo(w / 2 + needleWidth, h / 2)
                        lineTo(w / 2, h / 2 + centerNotchOffset)
                        close()
                    }

                    // Render with beautiful dynamic active colors
                    drawPath(
                        path = pathNorthLeft,
                        color = if (isAligned) Color(0xFF2E7D32) else Color(0xFFC5A059) // Darker shade of emerald/gold
                    )
                    drawPath(
                        path = pathNorthRight,
                        color = if (isAligned) Color(0xFF4CAF50) else Color(0xFFFFD54F) // Lighter shade profile
                    )
                    drawPath(
                        path = pathSouthLeft,
                        color = Color(0xFF5A5A5A)
                    )
                    drawPath(
                        path = pathSouthRight,
                        color = Color(0xFF8E8E8E)
                    )

                    // Draw golden/emerald glowing pivot cap in center
                    drawCircle(
                        color = if (isAligned) Color(0xFFB9F6CA) else Color(0xFFFFF9C4),
                        radius = 4.dp.toPx()
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(28.dp))

        // --- BEARING TEXT FEEDBACKS ---
        Card(
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(
                containerColor = if (isAligned) Color(0xFF1B3821) else MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
            ),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Device Heading", fontSize = 11.sp, color = MaterialTheme.colorScheme.outline)
                    Text(
                        "${heading.toInt()}°",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                }
                Divider(
                    modifier = Modifier
                        .height(38.dp)
                        .width(1.dp)
                )
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Qibla Bearing", fontSize = 11.sp, color = MaterialTheme.colorScheme.outline)
                    Text(
                        "${bearing.toInt()}° North",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFD4AF37)
                    )
                }
            }
        }
    }
}

@Composable
fun StatsScreen(viewModel: PrayerViewModel) {
    val context = LocalContext.current
    val allLogs by viewModel.allPrayerLogs.collectAsState()
    val cloudStatus by viewModel.cloudBackupStatus.collectAsState()
    val backupTime by viewModel.lastBackupTime.collectAsState()

    // Math statistics
    val totalLogs = allLogs.size
    val totalCompleted = allLogs.count { it.completed }
    val congregationCount = allLogs.count { it.prayedInCongregation }
    val qadaCount = allLogs.count { it.isQada }

    val completionRate = if (totalLogs > 0) (totalCompleted.toFloat() / totalLogs.toFloat() * 100).toInt() else 0

    // Calculates consecutive days of complete prayers (5 daily logged complete)
    val logsGroupedByDate = allLogs.groupBy { it.date }
    var prayerStreak = 0
    val todayDateStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
    var checkCal = Calendar.getInstance()
    var checkDateStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(checkCal.time)

    // Crawl backwards to count streak
    while (logsGroupedByDate.containsKey(checkDateStr)) {
        val completedToday = logsGroupedByDate[checkDateStr]?.count { it.completed } ?: 0
        if (completedToday >= 5) {
            prayerStreak++
            checkCal.add(Calendar.DATE, -1)
            checkDateStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(checkCal.time)
        } else {
            break
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        // --- STATISTICS HERO STRIPS ---
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Card(
                modifier = Modifier
                    .weight(1f)
                    .height(130.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(14.dp),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    Icon(Icons.Default.TrendingUp, contentDescription = "Rate", tint = MaterialTheme.colorScheme.onTertiaryContainer)
                    Column {
                        Text("Completion", fontSize = 11.sp, color = MaterialTheme.colorScheme.onTertiaryContainer.copy(alpha = 0.7f))
                        Text("$completionRate%", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.onTertiaryContainer)
                    }
                }
            }

            Card(
                modifier = Modifier
                    .weight(1f)
                    .height(130.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(14.dp),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    Icon(Icons.Default.Whatshot, contentDescription = "Streak", tint = Color(0xFFD4AF37))
                    Column {
                        Text("Logged Streak", fontSize = 11.sp, color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f))
                        Text("$prayerStreak Days", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.onPrimaryContainer)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // --- INDIVIDUAL PRAYER LOG METRICS ---
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Spiritual Analytics",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(12.dp))

                listOf(
                    Pair("Fajr", Color(0xFF33C08E)),
                    Pair("Dhuhr", Color(0xFFECC27E)),
                    Pair("Asr", Color(0xFFC5A059)),
                    Pair("Maghrib", Color(0xFF0F9D58)),
                    Pair("Isha", Color(0xFF1B6A9F))
                ).forEach { (pName, barColor) ->
                    val pCount = allLogs.count { it.prayerName == pName && it.completed }
                    val pMissed = allLogs.count { it.prayerName == pName && !it.completed }
                    Column(modifier = Modifier.padding(vertical = 4.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(pName, fontWeight = FontWeight.Bold)
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Text("$pCount prayed", fontWeight = FontWeight.SemiBold, fontSize = 13.sp, color = MaterialTheme.colorScheme.primary)
                                if (pMissed > 0) {
                                    Text("$pMissed missed", fontWeight = FontWeight.SemiBold, fontSize = 13.sp, color = MaterialTheme.colorScheme.error)
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        // Progress bar representation
                        val barFraction = if (totalLogs > 0) pCount.toFloat() / (totalLogs / 5.0f).coerceAtLeast(1.0f) else 0.0f
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.outlineVariant)
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxHeight()
                                    .fillMaxWidth(fraction = barFraction.coerceIn(0.0f, 1.0f))
                                    .clip(CircleShape)
                                    .background(barColor)
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Key stats details
        Card(
            modifier = Modifier.fillMaxWidth(),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Congregational", fontSize = 11.sp, color = MaterialTheme.colorScheme.outline)
                    Text("$congregationCount Jama'at", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                }
                Divider(modifier = Modifier.height(34.dp).width(1.dp))
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Adjusted Logs", fontSize = 11.sp, color = MaterialTheme.colorScheme.outline)
                    Text("$qadaCount Qada", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                }
                Divider(modifier = Modifier.height(34.dp).width(1.dp))
                val missedTotal = allLogs.count { !it.completed }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Not Prayed", fontSize = 11.sp, color = MaterialTheme.colorScheme.outline)
                    Text("$missedTotal Missed", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.error)
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // --- BACKUP & SYNCHRONIZATION BAR ---
        Text(
            text = "SECURE CLOUD BACKUP & SYNCHRONIZATION",
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
            shape = RoundedCornerShape(16.dp),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "End-to-End Encrypted Cloud Sync",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Sync your offline prayer statistics, streaks, and Quran progress to a secured private cloud node. This helps keep long term consistency.",
                    style = MaterialTheme.typography.bodySmall
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "Last Cloud Backup: $backupTime",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.secondary,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = { viewModel.syncBackupToSecureCloud() },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("backup_cloud_btn")
                    ) {
                        Icon(Icons.Default.CloudUpload, contentDescription = "Cloud Upload")
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Cloud Sync")
                    }

                    OutlinedButton(
                        onClick = {
                            val f = viewModel.exportBackupToFile(context)
                            if (f != null) {
                                triggerFileShare(context, f)
                            } else {
                                Toast.makeText(context, "Export error", Toast.LENGTH_SHORT).show()
                            }
                        },
                        modifier = Modifier
                            .weight(1.5f)
                            .testTag("backup_export_btn")
                    ) {
                        Icon(Icons.Default.FileDownload, contentDescription = "Export")
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Export Backup JSON")
                    }
                }

                // Display backup status dynamically
                when (val status = cloudStatus) {
                    is PrayerViewModel.BackupState.Syncing -> {
                        Spacer(modifier = Modifier.height(14.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Securing handshake, packaging database schemas...", fontSize = 12.sp)
                        }
                    }
                    is PrayerViewModel.BackupState.Success -> {
                        Spacer(modifier = Modifier.height(14.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Check, contentDescription = "OK", tint = Color.Green, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(status.message, fontSize = 11.sp, color = Color.Green)
                        }
                    }
                    is PrayerViewModel.BackupState.Error -> {
                        Spacer(modifier = Modifier.height(14.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Warning, contentDescription = "Error", tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(status.error, fontSize = 11.sp, color = MaterialTheme.colorScheme.error)
                        }
                    }
                    else -> {}
                }
            }
        }
    }
}

// Share backup JSON file through system intent
private fun triggerFileShare(context: Context, backupFile: File) {
    try {
        val uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            backupFile
        )
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "application/json"
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(shareIntent, "Save Prayer Backup JSON File"))
    } catch (e: Exception) {
        Toast.makeText(context, "Failure preparing secure export: ${e.message}", Toast.LENGTH_LONG).show()
    }
}

@Composable
fun AfahAppLogo(modifier: Modifier = Modifier, size: androidx.compose.ui.unit.Dp = 100.dp) {
    Box(
        modifier = modifier.size(size),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.toPx()
            val h = size.toPx()
            val cx = w / 2f
            val cy = h / 2f
            
            // Outer glowing ring
            drawCircle(
                color = Color(0xFFD4AF37).copy(alpha = 0.08f),
                radius = w * 0.48f,
                center = androidx.compose.ui.geometry.Offset(cx, cy)
            )
            
            // Solid inner background medallion
            drawCircle(
                color = Color(0xFF1E1C18),
                radius = w * 0.40f,
                center = androidx.compose.ui.geometry.Offset(cx, cy)
            )
            
            // Medallion double border (double concentric gold rings)
            drawCircle(
                color = Color(0xFFD4AF37).copy(alpha = 0.8f),
                radius = w * 0.40f,
                center = androidx.compose.ui.geometry.Offset(cx, cy),
                style = androidx.compose.ui.graphics.drawscope.Stroke(width = w * 0.02f)
            )
            drawCircle(
                color = Color(0xFFD4AF37).copy(alpha = 0.3f),
                radius = w * 0.36f,
                center = androidx.compose.ui.geometry.Offset(cx, cy),
                style = androidx.compose.ui.graphics.drawscope.Stroke(width = w * 0.008f)
            )
            
            // Islamic geometry: subtle 8-pointed boundary star inside the border
            val numPoints = 8
            val starOuterR = w * 0.40f
            val starInnerR = w * 0.35f
            val boundaryPath = androidx.compose.ui.graphics.Path().apply {
                for (i in 0 until numPoints * 2) {
                    val angleRad = (i * (360.0 / (numPoints * 2)) - 90.0) * Math.PI / 180.0
                    val radius = if (i % 2 == 0) starOuterR else starInnerR
                    val x = cx + radius * Math.cos(angleRad)
                    val y = cy + radius * Math.sin(angleRad)
                    if (i == 0) moveTo(x.toFloat(), y.toFloat()) else lineTo(x.toFloat(), y.toFloat())
                }
                close()
            }
            drawPath(
                path = boundaryPath,
                color = Color(0xFFD4AF37).copy(alpha = 0.4f),
                style = androidx.compose.ui.graphics.drawscope.Stroke(width = w * 0.01f)
            )

            // Dynamic Crescent Moon
            val crescentPath = androidx.compose.ui.graphics.Path().apply {
                val moonR = w * 0.22f
                val moonCx = cx - w * 0.04f
                val moonCy = cy
                addArc(
                    androidx.compose.ui.geometry.Rect(moonCx - moonR, moonCy - moonR, moonCx + moonR, moonCy + moonR),
                    -110f,
                    220f
                )
                // Inner clip sphere
                val clipR = moonR * 0.95f
                val clipCx = moonCx + moonR * 0.42f
                addArc(
                    androidx.compose.ui.geometry.Rect(clipCx - clipR, moonCy - clipR, clipCx + clipR, moonCy + clipR),
                    110f,
                    -220f
                )
                close()
            }
            drawPath(
                path = crescentPath,
                color = Color(0xFFFFD54F) // Golden Yellow filled moon
            )

            // Glowing core 8-point star nesting inside the crescent point
            val starCx = cx + w * 0.12f
            val starCy = cy - w * 0.08f
            val coreStarR1 = w * 0.08f
            val coreStarR2 = w * 0.035f
            val coreStarPath = androidx.compose.ui.graphics.Path().apply {
                for (step in 0 until 8) {
                    val angleRad1 = (step * 45 - 90) * Math.PI / 180.0
                    val angleRad2 = (step * 45 + 22.5 - 90) * Math.PI / 180.0
                    val p1x = starCx + coreStarR1 * Math.cos(angleRad1)
                    val p1y = starCy + coreStarR1 * Math.sin(angleRad1)
                    val p2x = starCx + coreStarR2 * Math.cos(angleRad2)
                    val p2y = starCy + coreStarR2 * Math.sin(angleRad2)
                    if (step == 0) {
                        moveTo(p1x.toFloat(), p1y.toFloat())
                    } else {
                        lineTo(p1x.toFloat(), p1y.toFloat())
                    }
                    lineTo(p2x.toFloat(), p2y.toFloat())
                }
                close()
            }
            drawPath(
                path = coreStarPath,
                color = Color(0xFFFFF59D) // Bright star center
            )
            
            // Elegant dome baseline in the center bottom
            val domePath = androidx.compose.ui.graphics.Path().apply {
                val dx = cx - w * 0.15f
                val dy = cy + w * 0.28f
                moveTo(dx, dy)
                // Dome arch
                cubicTo(
                    cx - w * 0.12f, cy + w * 0.12f,
                    cx + w * 0.12f, cy + w * 0.12f,
                    cx + w * 0.15f, dy
                )
                lineTo(dx, dy)
                close()
            }
            drawPath(
                path = domePath,
                color = Color(0xFFD4AF37).copy(alpha = 0.5f)
            )
        }
    }
}

@Composable
fun SplashScreen(onTimeout: () -> Unit) {
    var progress by remember { mutableStateOf(0f) }
    LaunchedEffect(Unit) {
        val animationSteps = 100
        for (i in 1..animationSteps) {
            delay(20) // 20ms * 100 = 2 seconds
            progress = i / 100f
        }
        delay(300)
        onTimeout()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF0F0B1E), // Vibrant deep space indigo
                        Color(0xFF04060C), // Deep starry black backdrop
                        Color(0xFF140D24)  // Mystic dawn accent glow
                    )
                )
            )
            .animatedGradientBackground("nebula_live"), // Fluid celestial transition
        contentAlignment = Alignment.Center
    ) {
        // Grand decorative mosque silhouette standing tall at the bottom of the splash screen
        MosqueDomeAndMinaretBackground(
            modifier = Modifier
                .fillMaxWidth()
                .height(220.dp)
                .align(Alignment.BottomCenter),
            primaryColor = Color(0xFFD4AF37).copy(alpha = 0.22f), // Beautiful glowing gold arch
            secondaryColor = Color(0xFF1DE9B6).copy(alpha = 0.12f), // Soft cyan minaret tip aura
            prayerPhase = "isha",
            drawSkyBackground = false // Transparent overlay for a seamless full background
        )

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(24.dp)
        ) {
            AfahAppLogo(size = 140.dp)
            Spacer(modifier = Modifier.height(24.dp))
            Text(
                text = "AFAH",
                style = MaterialTheme.typography.displayMedium,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.primary,
                letterSpacing = 4.sp
            )
            Text(
                text = "Your Islamic Prayer Companion",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.Light,
                letterSpacing = 1.sp
            )
            Spacer(modifier = Modifier.height(48.dp))
            Box(
                modifier = Modifier
                    .width(180.dp)
                    .height(6.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .fillMaxWidth(progress)
                        .background(MaterialTheme.colorScheme.primary)
                )
            }
            Spacer(modifier = Modifier.height(48.dp))
            Text(
                text = "Developed by Afroj",
                style = MaterialTheme.typography.bodySmall,
                color = Color(0xFFD4AF37),
                fontWeight = FontWeight.SemiBold,
                letterSpacing = 1.5.sp
            )
            Text(
                text = "Based in India Edition",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.outline,
                fontWeight = FontWeight.Normal
            )
        }
    }
}

@Composable
fun LoginSignupScreen(
    viewModel: PrayerViewModel,
    onAuthSuccess: () -> Unit
) {
    var username by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var trackingModeSelection by remember { mutableStateOf(true) } // true = Log Prayers & stats, false = Notification only
    var isSignUpMode by remember { mutableStateOf(false) }
    var errorMsg by remember { mutableStateOf<String?>(null) }

    val coroutineScope = rememberCoroutineScope()
    var isLoading by remember { mutableStateOf(false) }

    val keyboardController = LocalSoftwareKeyboardController.current
    val focusManager = LocalFocusManager.current

    val currentTheme by viewModel.currentThemeName.collectAsState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF0F0B1E), // Deep luxury spiritual indigo
                        Color(0xFF04060C), // Deep space velvet black
                        Color(0xFF140D24)  // Mystic dawn amethyst aura
                    )
                )
            )
            .animatedGradientBackground(currentTheme),
        contentAlignment = Alignment.Center
    ) {
        // Celestial mosque skyline spanning the entire screen background
        MosqueDomeAndMinaretBackground(
            modifier = Modifier.fillMaxSize(),
            primaryColor = Color(0xFFD4AF37).copy(alpha = 0.15f), // Glowing gold arch
            secondaryColor = Color(0xFF1DE9B6).copy(alpha = 0.08f), // Soft cyan minaret tip aura
            prayerPhase = "isha",
            drawSkyBackground = false // Transparent overlay for seamless background flow
        )

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            AfahAppLogo(size = 90.dp)
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = if (isSignUpMode) "Create Account" else "Sign In to Afah",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = "Keep your spiritual journey aligned",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 24.dp)
            )

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = MihrabArchShape(0.12f),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.25f))
            ) {
                Column(
                    modifier = Modifier.padding(start = 24.dp, end = 24.dp, bottom = 24.dp, top = 48.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    if (isSignUpMode) {
                        OutlinedTextField(
                            value = username,
                            onValueChange = { username = it },
                            label = { Text("Believer's Name") },
                            leadingIcon = { Icon(Icons.Default.Person, contentDescription = "Name") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            enabled = !isLoading,
                            keyboardOptions = KeyboardOptions(
                                imeAction = ImeAction.Next
                            )
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                    }

                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it },
                        label = { Text("Email Address") },
                        leadingIcon = { Icon(Icons.Default.Email, contentDescription = "Email") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        enabled = !isLoading,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Email,
                            imeAction = ImeAction.Next
                        )
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it },
                        label = { Text("Password") },
                        leadingIcon = { Icon(Icons.Default.Lock, contentDescription = "Password") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        enabled = !isLoading,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Password,
                            imeAction = if (isSignUpMode) ImeAction.Done else ImeAction.Done
                        ),
                        keyboardActions = KeyboardActions(
                            onDone = {
                                keyboardController?.hide()
                                focusManager.clearFocus()
                            }
                        )
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    if (isSignUpMode) {
                        Text(
                            text = "Choose App Experience:",
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.align(Alignment.Start).padding(bottom = 6.dp)
                        )
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            // Card A: Log Prayers
                            Card(
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable(enabled = !isLoading) { trackingModeSelection = true }
                                    .border(
                                        1.5.dp,
                                        if (trackingModeSelection) MaterialTheme.colorScheme.primary else Color.Transparent,
                                        RoundedCornerShape(12.dp)
                                    ),
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = if (trackingModeSelection) MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                                                     else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.15f)
                                )
                            ) {
                                Column(
                                    modifier = Modifier.padding(10.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Icon(
                                        Icons.Default.CheckCircle,
                                        contentDescription = "Log Prayers Option",
                                        tint = if (trackingModeSelection) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        "Log Prayers",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        textAlign = TextAlign.Center
                                    )
                                    Text(
                                        "Record status & view stats charts",
                                        style = MaterialTheme.typography.bodySmall,
                                        fontSize = 8.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        textAlign = TextAlign.Center,
                                        lineHeight = 10.sp
                                    )
                                }
                            }

                            // Card B: Notifications Only
                            Card(
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable(enabled = !isLoading) { trackingModeSelection = false }
                                    .border(
                                        1.5.dp,
                                        if (!trackingModeSelection) MaterialTheme.colorScheme.primary else Color.Transparent,
                                        RoundedCornerShape(12.dp)
                                    ),
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = if (!trackingModeSelection) MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                                                     else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.15f)
                                )
                            ) {
                                Column(
                                    modifier = Modifier.padding(10.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Icon(
                                        Icons.Default.NotificationsActive,
                                        contentDescription = "Notifications Only Option",
                                        tint = if (!trackingModeSelection) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        "Only Alerts",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        textAlign = TextAlign.Center
                                    )
                                    Text(
                                        "Receive calendar & adhan alerts only",
                                        style = MaterialTheme.typography.bodySmall,
                                        fontSize = 8.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        textAlign = TextAlign.Center,
                                        lineHeight = 10.sp
                                    )
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                    }

                    errorMsg?.let { msg ->
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(msg, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                        Spacer(modifier = Modifier.height(8.dp))
                    }

                    Button(
                        onClick = {
                            if (isLoading) return@Button
                            keyboardController?.hide()
                            focusManager.clearFocus()
                            if (email.isEmpty() || password.isEmpty() || (isSignUpMode && username.isEmpty())) {
                                errorMsg = "Please fill in all fields."
                                return@Button
                            }
                            if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                                errorMsg = "Please enter a valid email."
                                return@Button
                            }
                            if (password.length < 4) {
                                errorMsg = "Password must be at least 4 characters."
                                return@Button
                            }

                            coroutineScope.launch {
                                isLoading = true
                                errorMsg = null
                                try {
                                    if (isSignUpMode) {
                                        val signupSuccess = viewModel.signUpUser(username, email, password, trackingModeSelection)
                                        if (signupSuccess) {
                                            onAuthSuccess()
                                        } else {
                                            errorMsg = "An account with this email already exists."
                                        }
                                    } else {
                                        val loginError = viewModel.loginUser(email, password)
                                        if (loginError == null) {
                                            onAuthSuccess()
                                        } else {
                                            errorMsg = loginError
                                        }
                                    }
                                } catch (e: Throwable) {
                                    errorMsg = "Sync failure: ${e.localizedMessage ?: "Network or internal issue"}"
                                } finally {
                                    isLoading = false
                                }
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp),
                        shape = RoundedCornerShape(12.dp),
                        enabled = !isLoading
                    ) {
                        if (isLoading) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(20.dp),
                                    color = MaterialTheme.colorScheme.onPrimary,
                                    strokeWidth = 2.dp
                                )
                                Text(
                                    text = if (isSignUpMode) "Registering & Syncing..." else "Syncing Afah Cloud...",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp
                                )
                            }
                        } else {
                            Text(if (isSignUpMode) "Register" else "Enter", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    TextButton(
                        onClick = {
                            isSignUpMode = !isSignUpMode
                            errorMsg = null
                        },
                        enabled = !isLoading
                    ) {
                        Text(
                            text = if (isSignUpMode) "Already have an account? Sign In" else "New to Afah? Create Account",
                            color = if (isLoading) MaterialTheme.colorScheme.primary.copy(alpha = 0.5f) else MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
            Text(
                text = "Crafted with dedication by Afroj",
                style = MaterialTheme.typography.bodySmall,
                color = Color(0xFFD4AF37),
                fontWeight = FontWeight.Medium
            )
        }
    }
}

private fun isPrayerTimePassed(prayerTimeStr: String, selectedDateStr: String): Boolean {
    val todayStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
    if (selectedDateStr < todayStr) return true
    if (selectedDateStr > todayStr) return false
    
    try {
        val cleanTime = prayerTimeStr.trim()
        val parts = cleanTime.split(":")
        if (parts.size >= 2) {
            val hour = parts[0].toIntOrNull() ?: return true
            val minute = parts[1].substringBefore(" ").toIntOrNull() ?: return true
            
            val now = Calendar.getInstance()
            val currentHour = now.get(Calendar.HOUR_OF_DAY)
            val currentMinute = now.get(Calendar.MINUTE)
            
            if (currentHour > hour) return true
            if (currentHour == hour && currentMinute >= minute) return true
            return false
        }
    } catch (e: Exception) {
        return true
    }
    return true
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(viewModel: PrayerViewModel, onBack: () -> Unit) {
    val context = LocalContext.current
    val currentTheme by viewModel.currentThemeName.collectAsState()
    val activeSchool by viewModel.asrSchool.collectAsState()
    val latitude by viewModel.latitude.collectAsState()
    val longitude by viewModel.longitude.collectAsState()
    val fajrAngle by viewModel.fajrAngle.collectAsState()
    val ishaAngle by viewModel.ishaAngle.collectAsState()

    var tempLat by remember(latitude) { mutableStateOf(latitude.toString()) }
    var tempLon by remember(longitude) { mutableStateOf(longitude.toString()) }
    var tempFajrAngle by remember(fajrAngle) { mutableStateOf(fajrAngle.toString()) }
    var tempIshaAngle by remember(ishaAngle) { mutableStateOf(ishaAngle.toString()) }

    val fDelay by viewModel.fajrIqamahDelay.collectAsState()
    val dDelay by viewModel.dhuhrIqamahDelay.collectAsState()
    val aDelay by viewModel.asrIqamahDelay.collectAsState()
    val mDelay by viewModel.maghribIqamahDelay.collectAsState()
    val iDelay by viewModel.ishaIqamahDelay.collectAsState()

    val fAdhanOver by viewModel.fajrOverrideAdhan.collectAsState()
    val dAdhanOver by viewModel.dhuhrOverrideAdhan.collectAsState()
    val aAdhanOver by viewModel.asrOverrideAdhan.collectAsState()
    val mAdhanOver by viewModel.maghribOverrideAdhan.collectAsState()
    val iAdhanOver by viewModel.ishaOverrideAdhan.collectAsState()

    val fNamajOver by viewModel.fajrOverrideNamaj.collectAsState()
    val dNamajOver by viewModel.dhuhrOverrideNamaj.collectAsState()
    val aNamajOver by viewModel.asrOverrideNamaj.collectAsState()
    val mNamajOver by viewModel.maghribOverrideNamaj.collectAsState()
    val iNamajOver by viewModel.ishaOverrideNamaj.collectAsState()
    val dateQueueTrigger by viewModel.dateQueueTrigger.collectAsState()

    val fOff by viewModel.fajrOffset.collectAsState()
    val sOff by viewModel.sunriseOffset.collectAsState()
    val dOff by viewModel.dhuhrOffset.collectAsState()
    val aOff by viewModel.asrOffset.collectAsState()
    val mOff by viewModel.maghribOffset.collectAsState()
    val iOff by viewModel.ishaOffset.collectAsState()

    val loggedInUser by viewModel.loggedInUser.collectAsState()
    val loggedInEmail by viewModel.loggedInEmail.collectAsState()
    val loggedInAddress by viewModel.loggedInAddress.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // --- HEADER ROW ---
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onBack,
                modifier = Modifier
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Back",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(
                    text = "Afah Settings",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "Configure calculations, theme accents & timings",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // --- 1. THEME CARD ---
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(Icons.Default.Palette, contentDescription = "Theme Icon", tint = MaterialTheme.colorScheme.primary)
                    Text(
                        text = "App Color Accents",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf(
                        Triple("royal_purple", "Royal Purple", Color(0xFFBB86FC)),
                        Triple("emerald_dusk", "Islamic Emerald", Color(0xFF00E676)),
                        Triple("midnight_sapphire", "Midnight Sapphire", Color(0xFF29B6F6)),
                        Triple("crimson_velvet", "Crimson Velvet", Color(0xFFFF5252)),
                        Triple("golden_oasis", "Golden Oasis", Color(0xFFFFB74D)),
                        Triple("rose_quartz", "Rose Quartz", Color(0xFFF06292)),
                        Triple("amber_glow", "Amber Glow", Color(0xFFFFB300)),
                        Triple("aurora_live", "Aurora Live", Color(0xFF1DE9B6)),
                        Triple("nebula_live", "Nebula Live", Color(0xFFE040FB)),
                        Triple("ivory_glow", "Ivory Glow", Color(0xFFFFFDD0)),
                        Triple("celestial_dusk", "Celestial Dusk", Color(0xFFB388FF))
                    ).forEach { (themeId, labelStr, themeColor) ->
                        Box(
                            modifier = Modifier
                                .size(width = 115.dp, height = 50.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(
                                    if (currentTheme == themeId) themeColor.copy(alpha = 0.25f)
                                    else MaterialTheme.colorScheme.surface.copy(alpha = 0.6f)
                                )
                                .border(
                                    2.dp,
                                    if (currentTheme == themeId) themeColor else Color.Transparent,
                                    RoundedCornerShape(12.dp)
                                )
                                .clickable { viewModel.updateThemeName(themeId) }
                                .padding(8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(10.dp)
                                        .clip(CircleShape)
                                        .background(themeColor)
                                )
                                Text(
                                    text = labelStr.substringBefore(" "),
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = if (currentTheme == themeId) themeColor else MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                }
            }
        }

        // --- CUSTOM ADHAN & NAMAJ OVERRIDES CARD ---
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(Icons.Default.Edit, contentDescription = "Edit Overrides", tint = MaterialTheme.colorScheme.primary)
                    Column {
                        Text(
                            text = "Self Namaj & Adhan Times",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Override automatic astronomical timings",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))

                listOf(
                    Triple("Fajr", fAdhanOver, fNamajOver),
                    Triple("Dhuhr", dAdhanOver, dNamajOver),
                    Triple("Asr", aAdhanOver, aNamajOver),
                    Triple("Maghrib", mAdhanOver, mNamajOver),
                    Triple("Isha", iAdhanOver, iNamajOver)
                ).forEach { (prayerName, adhanVal, namajVal) ->
                    var adhanText by remember(adhanVal) { mutableStateOf(adhanVal) }
                    var namajText by remember(namajVal) { mutableStateOf(namajVal) }

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp)
                    ) {
                        Text(prayerName, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedTextField(
                                value = adhanText,
                                onValueChange = {
                                    adhanText = it
                                    viewModel.updateAdhanOverride(prayerName, it)
                                },
                                label = { Text("Adhan (HH:mm)") },
                                textStyle = MaterialTheme.typography.bodyMedium,
                                singleLine = true,
                                modifier = Modifier.weight(1f)
                            )
                            OutlinedTextField(
                                value = namajText,
                                onValueChange = {
                                    namajText = it
                                    viewModel.updateNamajOverride(prayerName, it)
                                },
                                label = { Text("Namaj (HH:mm)") },
                                textStyle = MaterialTheme.typography.bodyMedium,
                                singleLine = true,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }
        }

        // --- SPECIFIC DATE QUEUE TIME CARD ---
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(Icons.Default.DateRange, contentDescription = "Date Queue Delay", tint = MaterialTheme.colorScheme.secondary)
                    Column {
                        Text(
                            text = "Queue Delay at Specific Date",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Configure custom Iqamah delays for special occasions",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))

                var targetDateText by remember { mutableStateOf(SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())) }
                var selectedPrayerName by remember { mutableStateOf("Fajr") }
                var customDelayText by remember { mutableStateOf("15") }

                val datePickerCalendar = java.util.Calendar.getInstance()
                try {
                    val parsedDate = SimpleDateFormat("yyyy-MM-dd", java.util.Locale.US).parse(targetDateText)
                    if (parsedDate != null) {
                        datePickerCalendar.time = parsedDate
                    }
                } catch (e: Exception) {}

                val datePickerDialog = android.app.DatePickerDialog(
                    context,
                    { _, year, month, dayOfMonth ->
                        val mStr = if (month + 1 < 10) "0${month + 1}" else (month + 1).toString()
                        val dStr = if (dayOfMonth < 10) "0$dayOfMonth" else dayOfMonth.toString()
                        targetDateText = "$year-$mStr-$dStr"
                    },
                    datePickerCalendar.get(java.util.Calendar.YEAR),
                    datePickerCalendar.get(java.util.Calendar.MONTH),
                    datePickerCalendar.get(java.util.Calendar.DAY_OF_MONTH)
                )

                OutlinedTextField(
                    value = targetDateText,
                    onValueChange = { targetDateText = it },
                    label = { Text("Target Date (yyyy-MM-dd)") },
                    singleLine = true,
                    readOnly = true,
                    modifier = Modifier.fillMaxWidth(),
                    trailingIcon = {
                        IconButton(onClick = { datePickerDialog.show() }) {
                            Icon(Icons.Default.CalendarMonth, contentDescription = "Choose target date from calendar")
                        }
                    }
                )
                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Prayer Selector Chips
                    listOf("Fajr", "Dhuhr", "Asr", "Maghrib", "Isha").forEach { pName ->
                        FilterChip(
                            selected = selectedPrayerName == pName,
                            onClick = { selectedPrayerName = pName },
                            label = { Text(pName) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = customDelayText,
                    onValueChange = { customDelayText = it },
                    label = { Text("Queue Delay (Minutes)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(12.dp))

                Button(
                    onClick = {
                        val minutes = customDelayText.toIntOrNull()
                        if (minutes == null || minutes < 0) {
                            Toast.makeText(context, "Please enter a valid positive number of minutes", Toast.LENGTH_SHORT).show()
                        } else {
                            viewModel.setQueueDelayForPrayer(selectedPrayerName, targetDateText, minutes)
                            Toast.makeText(context, "[Success] Applied $selectedPrayerName queue delay of $minutes minutes for $targetDateText", Toast.LENGTH_SHORT).show()
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
                ) {
                    Icon(Icons.Default.Check, contentDescription = "Apply specific date delay")
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Apply Specific Date Queue Delay")
                }
            }
        }

        // --- 4. GEO CONFIG CARD ---
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(Icons.Default.MyLocation, contentDescription = "Location Settings", tint = MaterialTheme.colorScheme.primary)
                    Text(
                        text = "Georeferences & Calculation Method",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
                Spacer(modifier = Modifier.height(12.dp))

                // Indian Cities Quick Links
                Text(text = "Quick India Georefs", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf(
                        Triple(28.6139, 77.2090, "New Delhi"),
                        Triple(19.0760, 72.8777, "Mumbai"),
                        Triple(22.5726, 88.3639, "Kolkata"),
                        Triple(13.0827, 80.2707, "Chennai"),
                        Triple(17.3850, 78.4867, "Hyderabad"),
                        Triple(12.9716, 77.5946, "Bangalore")
                    ).forEach { (lat, lon, cityName) ->
                        FilterChip(
                            selected = latitude == lat,
                            onClick = {
                                tempLat = lat.toString()
                                tempLon = lon.toString()
                                viewModel.updateLocation(lat, lon, "$cityName, India")
                                Toast.makeText(context, "$cityName Georef Applied", Toast.LENGTH_SHORT).show()
                            },
                            label = { Text(cityName) }
                        )
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))

                // Asr Shadow School Type selector
                Text(text = "Asr Shadow School", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = MaterialTheme.colorScheme.primary)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf(
                        PrayerTimeCalculator.AsrSchool.STANDARD to "Shafi'i/Standard",
                        PrayerTimeCalculator.AsrSchool.HANAFI to "Hanafi"
                    ).forEach { (school, labelStr) ->
                        FilterChip(
                            selected = activeSchool == school,
                            onClick = { viewModel.updateAsrSchool(school) },
                            label = { Text(labelStr) }
                        )
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))

                // Manual Coordinates Inputs
                Text(text = "Manual Coordinate Override", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = MaterialTheme.colorScheme.secondary)
                Spacer(modifier = Modifier.height(6.dp))
                OutlinedTextField(
                    value = tempLat,
                    onValueChange = { tempLat = it },
                    label = { Text("Latitude") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = tempLon,
                    onValueChange = { tempLon = it },
                    label = { Text("Longitude") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(12.dp))

                // Astronomical shadow calculation angles
                Text(text = "Astronomical Angles", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = MaterialTheme.colorScheme.secondary)
                Spacer(modifier = Modifier.height(6.dp))
                OutlinedTextField(
                    value = tempFajrAngle,
                    onValueChange = { tempFajrAngle = it },
                    label = { Text("Fajr Angle (default 18°)") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = tempIshaAngle,
                    onValueChange = { tempIshaAngle = it },
                    label = { Text("Isha Angle (default 17°)") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Detect current GPS button
                    Button(
                        onClick = {
                            viewModel.fetchGPSLocation(context)
                            tempLat = viewModel.latitude.value.toString()
                            tempLon = viewModel.longitude.value.toString()
                        },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surfaceVariant, contentColor = MaterialTheme.colorScheme.onSurfaceVariant)
                    ) {
                        Icon(Icons.Default.MyLocation, contentDescription = "GPS icon", modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Detect GPS", fontSize = 12.sp)
                    }

                    // Save / Apply manual references button
                    Button(
                        onClick = {
                            val latDouble = tempLat.toDoubleOrNull() ?: latitude
                            val lonDouble = tempLon.toDoubleOrNull() ?: longitude
                            val fAngle = tempFajrAngle.toDoubleOrNull() ?: fajrAngle
                            val iAngle = tempIshaAngle.toDoubleOrNull() ?: ishaAngle

                            viewModel.updateCalculationAngles(fAngle, iAngle)
                            viewModel.updateLocation(latDouble, lonDouble, "Manual Geo Override")
                            Toast.makeText(context, "Calculation coordinates override success!", Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                    ) {
                        Icon(Icons.Default.Save, contentDescription = "Save icon", modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Save & Apply", fontSize = 12.sp)
                    }
                }
            }
        }

        // --- 5. APP MODE & TRACKING ---
        val isLoggingEnabledSetting by viewModel.isLoggingEnabled.collectAsState()
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Tune,
                        contentDescription = "Tracking Icon",
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "App Mode & Tracking Preference",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1.5f)) {
                        Text(
                            text = "Log Prayers & Statistics",
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Record prayer checklist status, missed prayers, and spiritual progress charts. Turn off to receive Adhan alerts only.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            lineHeight = 14.sp
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Switch(
                        checked = isLoggingEnabledSetting,
                        onCheckedChange = { viewModel.updateLoggingPreference(it) },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = MaterialTheme.colorScheme.primary,
                            checkedTrackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
                        )
                    )
                }
            }
        }

        // --- 6. LOGOUT / ACCOUNT CARD ---
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(Icons.Default.AccountCircle, contentDescription = "Profile Settings", tint = MaterialTheme.colorScheme.primary)
                    Text(
                        text = "User Profile Account",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))

                val initials = if (loggedInUser.isNotEmpty()) loggedInUser.take(2).uppercase() else "JS"
                Box(
                    modifier = Modifier
                        .size(60.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primaryContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = initials,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = if (loggedInUser.isNotEmpty()) loggedInUser else "Unregistered User",
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                if (loggedInEmail.isNotEmpty()) {
                    Text(
                        text = loggedInEmail,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Medium
                    )
                }
                if (loggedInAddress.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Address: $loggedInAddress",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Aptitude profile synchronization ready with offline log buffers",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(16.dp))

                if (loggedInUser.isNotEmpty()) {
                    Button(
                        onClick = {
                            viewModel.logout()
                            Toast.makeText(context, "Logged out successfully", Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                    ) {
                        Icon(Icons.Default.ExitToApp, contentDescription = "Log out icon")
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Disconnect Account ($loggedInUser)")
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))
        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
        Text(
            text = "Islamic Companion • Afah v1.3\nDevised with absolute reverence, 2026",
            style = MaterialTheme.typography.bodySmall,
            color = Color(0xFFD4AF37),
            fontWeight = FontWeight.Medium,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(bottom = 24.dp)
        )
    }
}

// --- ISLAMIC GEOMETRIC AND ARCHITECTURAL MOTIFS ---

/**
 * Extension function to draw standard symmetrical onion mosque dome paths mathematically.
 */
fun androidx.compose.ui.graphics.Path.addOnionDome(
    xStart: Float,
    xEnd: Float,
    yGround: Float,
    yPeak: Float
) {
    val cx = (xStart + xEnd) / 2f
    val domeW = xEnd - xStart
    
    // Bottom flare control points: starts curving inwards then bulge outwards
    val yBulge = yPeak + (yGround - yPeak) * 0.45f
    val xLeftBulge = xStart - domeW * 0.08f
    val xRightBulge = xEnd + domeW * 0.08f
    
    // Begin path
    moveTo(xStart, yGround)
    // Bullet/onion curve up to left bulge
    cubicTo(
        xStart + domeW * 0.05f, yGround - (yGround - yBulge) * 0.4f,
        xLeftBulge - domeW * 0.03f, yBulge + (yGround - yBulge) * 0.15f,
        xLeftBulge, yBulge
    )
    // Flare inward and upward to the pointed crown
    cubicTo(
        xLeftBulge + domeW * 0.03f, yBulge - (yBulge - yPeak) * 0.45f,
        cx - domeW * 0.12f, yPeak + (yBulge - yPeak) * 0.15f,
        cx, yPeak
    )
    // Symmetrically do the right side back down to ground
    cubicTo(
        cx + domeW * 0.12f, yPeak + (yBulge - yPeak) * 0.15f,
        xRightBulge - domeW * 0.03f, yBulge - (yBulge - yPeak) * 0.45f,
        xRightBulge, yBulge
    )
    cubicTo(
        xRightBulge + domeW * 0.03f, yBulge + (yGround - yBulge) * 0.15f,
        xEnd - domeW * 0.05f, yGround - (yGround - yBulge) * 0.4f,
        xEnd, yGround
    )
}

@Composable
fun MosqueDomeAndMinaretBackground(
    modifier: Modifier = Modifier,
    primaryColor: Color = MaterialTheme.colorScheme.primary.copy(alpha = 0.22f),
    secondaryColor: Color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.12f),
    prayerPhase: String = "isha",
    drawSkyBackground: Boolean = true
) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        if (w <= 0f || h <= 0f) return@Canvas

        val phase = prayerPhase.lowercase().trim()
        
        if (drawSkyBackground) {
            // 1. Draw glowing gorgeous sky background based on active prayer time
            val gradientBrush = when {
                phase.contains("fajr") -> androidx.compose.ui.graphics.Brush.verticalGradient(
                    colors = listOf(Color(0xFF2E1A47), Color(0xFFF07B3F)),
                    startY = 0f,
                    endY = h
                )
                phase.contains("dhuhr") -> androidx.compose.ui.graphics.Brush.verticalGradient(
                    colors = listOf(Color(0xFF1E88E5), Color(0xFF90CAF9)),
                    startY = 0f,
                    endY = h
                )
                phase.contains("asr") -> androidx.compose.ui.graphics.Brush.verticalGradient(
                    colors = listOf(Color(0xFFFB8C00), Color(0xFFFFD54F)),
                    startY = 0f,
                    endY = h
                )
                phase.contains("maghrib") -> androidx.compose.ui.graphics.Brush.verticalGradient(
                    colors = listOf(Color(0xFF9C27B0), Color(0xFFE91E63)),
                    startY = 0f,
                    endY = h
                )
                else -> androidx.compose.ui.graphics.Brush.verticalGradient(
                    colors = listOf(Color(0xFF0F2027), Color(0xFF203A43)),
                    startY = 0f,
                    endY = h
                )
            }
            drawRect(brush = gradientBrush, size = size)

            // 2. Draw corresponding astronomical/celestial bodies
            if (phase.contains("fajr")) {
                // Draw sunrise: soft glowing sun rising from bottom left
                val scx = w * 0.20f
                val scy = h * 0.70f
                drawCircle(
                    color = Color(0xFFFFF176).copy(alpha = 0.85f),
                    radius = 24f,
                    center = androidx.compose.ui.geometry.Offset(scx, scy)
                )
                drawCircle(
                    color = Color(0xFFFFB74D).copy(alpha = 0.40f),
                    radius = 42f,
                    center = androidx.compose.ui.geometry.Offset(scx, scy)
                )
            } else if (phase.contains("dhuhr")) {
                // High noon sun at the absolute head (top center)
                val scx = w * 0.5f
                val scy = h * 0.22f
                drawCircle(
                    color = Color(0xFFFFEB3B),
                    radius = 22f,
                    center = androidx.compose.ui.geometry.Offset(scx, scy)
                )
                drawCircle(
                    color = Color(0xFFFFF59D).copy(alpha = 0.40f),
                    radius = 45f,
                    center = androidx.compose.ui.geometry.Offset(scx, scy)
                )
                // Draw shining high sunrays radiating symmetrically
                for (i in 0 until 8) {
                    val angle = i * (Math.PI / 4)
                    val startX = scx + Math.cos(angle).toFloat() * 28f
                    val startY = scy + Math.sin(angle).toFloat() * 28f
                    val endX = scx + Math.cos(angle).toFloat() * 46f
                    val endY = scy + Math.sin(angle).toFloat() * 46f
                    drawLine(
                        color = Color(0xFFFFEB3B).copy(alpha = 0.7f),
                        start = androidx.compose.ui.geometry.Offset(startX, startY),
                        end = androidx.compose.ui.geometry.Offset(endX, endY),
                        strokeWidth = 3.5f
                    )
                }
            } else if (phase.contains("asr")) {
                // Late afternoon golden hour sun settling mid-way towards the right
                val scx = w * 0.78f
                val scy = h * 0.40f
                drawCircle(
                    color = Color(0xFFFFB300),
                    radius = 20f,
                    center = androidx.compose.ui.geometry.Offset(scx, scy)
                )
                drawCircle(
                    color = Color(0xFFFFE082).copy(alpha = 0.35f),
                    radius = 34f,
                    center = androidx.compose.ui.geometry.Offset(scx, scy)
                )
                // Golden warm rays
                for (i in 0 until 6) {
                    val angle = i * (Math.PI / 3) + (Math.PI / 6)
                    val startX = scx + Math.cos(angle).toFloat() * 25f
                    val startY = scy + Math.sin(angle).toFloat() * 25f
                    val endX = scx + Math.cos(angle).toFloat() * 38f
                    val endY = scy + Math.sin(angle).toFloat() * 38f
                    drawLine(
                        color = Color(0xFFFFB300).copy(alpha = 0.6f),
                        start = androidx.compose.ui.geometry.Offset(startX, startY),
                        end = androidx.compose.ui.geometry.Offset(endX, endY),
                        strokeWidth = 2.5f
                    )
                }
            } else if (phase.contains("maghrib")) {
                // Sunset: sun half submerged behind the horizon
                val scx = w * 0.72f
                val scy = h * 0.75f
                drawCircle(
                    color = Color(0xFFFF3D00).copy(alpha = 0.95f),
                    radius = 18f,
                    center = androidx.compose.ui.geometry.Offset(scx, scy)
                )
                drawCircle(
                    color = Color(0xFFFF8A65).copy(alpha = 0.45f),
                    radius = 30f,
                    center = androidx.compose.ui.geometry.Offset(scx, scy)
                )
            } else {
                // Isha - starry deep night with twinkling stars and glowing crescent moon
                val stars = listOf(
                    Pair(w * 0.12f, h * 0.18f),
                    Pair(w * 0.22f, h * 0.12f),
                    Pair(w * 0.35f, h * 0.22f),
                    Pair(w * 0.60f, h * 0.10f),
                    Pair(w * 0.76f, h * 0.20f),
                    Pair(w * 0.88f, h * 0.14f)
                )
                for ((sx, sy) in stars) {
                    val starPath = androidx.compose.ui.graphics.Path().apply {
                        moveTo(sx, sy - 3.5f)
                        quadraticTo(sx, sy, sx + 3.5f, sy)
                        quadraticTo(sx, sy, sx, sy + 3.5f)
                        quadraticTo(sx, sy, sx - 3.5f, sy)
                        quadraticTo(sx, sy, sx, sy - 3.5f)
                        close()
                    }
                    drawPath(starPath, color = Color.White.copy(alpha = 0.60f))
                }

                // High-contrast golden crescent moon
                val mcx = w * 0.82f
                val mcy = h * 0.28f
                val mr = 15f
                val moonPath = androidx.compose.ui.graphics.Path().apply {
                    addArc(
                        androidx.compose.ui.geometry.Rect(mcx - mr, mcy - mr, mcx + mr, mcy + mr),
                        -120f,
                        240f
                    )
                    addArc(
                        androidx.compose.ui.geometry.Rect(mcx - mr * 0.5f, mcy - mr, mcx + mr * 1.5f, mcy + mr),
                        120f,
                        -240f
                    )
                    close()
                }
                drawPath(moonPath, color = Color(0xFFFFF9C4).copy(alpha = 0.75f))
            }
        }

        // 3. Draw Mosque Skyline over the sky background & elements
        val bgDomePath = androidx.compose.ui.graphics.Path()
        val bgCx = w * 0.5f
        val bgYGround = h
        val bgYPeak = h * 0.54f
        val bgDomeW = w * 0.38f
        
        bgDomePath.addOnionDome(bgCx - bgDomeW / 2, bgCx + bgDomeW / 2, bgYGround, bgYPeak)
        drawPath(bgDomePath, color = secondaryColor)

        val fgDomePath = androidx.compose.ui.graphics.Path()
        
        // Left Minaret Tower
        val mx1 = w * 0.08f
        val mw1 = w * 0.055f
        val mYGround = h
        val mYPeak = h * 0.45f
        fgDomePath.moveTo(mx1, mYGround)
        fgDomePath.lineTo(mx1, mYPeak)
        fgDomePath.lineTo(mx1 + mw1, mYPeak)
        fgDomePath.lineTo(mx1 + mw1, mYGround)
        fgDomePath.addOnionDome(mx1, mx1 + mw1, mYPeak, mYPeak - 12f)
        
        // Right Minaret Tower
        val mx2 = w * 0.865f
        fgDomePath.moveTo(mx2, mYGround)
        fgDomePath.lineTo(mx2, mYPeak)
        fgDomePath.lineTo(mx2 + mw1, mYPeak)
        fgDomePath.lineTo(mx2 + mw1, mYGround)
        fgDomePath.addOnionDome(mx2, mx2 + mw1, mYPeak, mYPeak - 12f)

        // Main Pointed foreground dome
        val fgCx = w * 0.5f
        val fgYPeak = h * 0.60f
        val fgDomeW = w * 0.33f
        fgDomePath.addOnionDome(fgCx - fgDomeW / 2, fgCx + fgDomeW / 2, mYGround, fgYPeak)
        
        // Left small dome
        val leftSmallCx = w * 0.28f
        val leftSmallYPeak = h * 0.70f
        val leftSmallDomeW = w * 0.18f
        fgDomePath.addOnionDome(leftSmallCx - leftSmallDomeW/2, leftSmallCx + leftSmallDomeW/2, mYGround, leftSmallYPeak)

        // Right small dome
        val rightSmallCx = w * 0.72f
        val rightSmallYPeak = h * 0.70f
        val rightSmallDomeW = w * 0.18f
        fgDomePath.addOnionDome(rightSmallCx - rightSmallDomeW/2, rightSmallCx + rightSmallDomeW/2, mYGround, rightSmallYPeak)

        drawPath(fgDomePath, color = primaryColor)
    }
}

/**
 * Beautiful Mosque Mihrab pointed frame arch shape.
 */
class MihrabArchShape(private val archStartHeightPercent: Float = 0.28f) : androidx.compose.ui.graphics.Shape {
    override fun createOutline(
        size: androidx.compose.ui.geometry.Size,
        layoutDirection: androidx.compose.ui.unit.LayoutDirection,
        density: androidx.compose.ui.unit.Density
    ): androidx.compose.ui.graphics.Outline {
        if (size.width <= 0f || size.height <= 0f) {
            return androidx.compose.ui.graphics.Outline.Rectangle(androidx.compose.ui.geometry.Rect(0f, 0f, size.width, size.height))
        }
        val path = androidx.compose.ui.graphics.Path().apply {
            val w = size.width
            val h = size.height
            val archY = h * archStartHeightPercent
            
            moveTo(0f, h)
            lineTo(0f, archY)
            
            val cx = w / 2f
            // Inward-to-outward curves meeting with pointed pinnacle at the apex
            cubicTo(
                0f, archY * 0.45f,
                cx - w * 0.18f, 0f,
                cx, 0f
            )
            cubicTo(
                cx + w * 0.18f, 0f,
                w, archY * 0.45f,
                w, archY
            )
            lineTo(w, h)
            close()
        }
        return androidx.compose.ui.graphics.Outline.Generic(path)
    }
}


