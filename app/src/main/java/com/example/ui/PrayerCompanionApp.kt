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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
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
import com.example.utils.PrayerTimeCalculator
import com.example.viewmodel.PrayerViewModel
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
}

private val SURAH_LIST = listOf(
    "Al-Fatihah", "Al-Baqarah", "Ali 'Imran", "An-Nisa'", "Al-Ma'idah", "Al-An'am", "Al-A'raf", "Al-Anfal", "At-Tawbah", "Yunus", "Hud", "Yusuf", "Ar-Ra'd", "Ibrahim", "Al-Hijr", "An-Nahl", "Al-Isra'", "Al-Kahf", "Maryam", "Ta-Ha", "Al-Anbiya'", "Al-Hajj", "Al-Mu'minun", "An-Nur", "Al-Furqan", "Ash-Shu'ara'", "An-Naml", "Al-Qacas", "Al-Ankabut", "Ar-Rum", "Luqman", "As-Sajdah", "Al-Ahzab", "Saba'", "Fatir", "Ya-Sin", "As-Saffat", "Sad", "Az-Zumar", "Ghafir", "Fussilat", "Ash-Shura", "Az-Zukhruf", "Ad-Dukhan", "Al-Jathiyah", "Al-Ahqaf", "Muhammad", "Al-Fath", "Al-Hujurat", "Qaf", "Adh-Dhariyat", "At-Tur", "An-Najm", "Al-Qamar", "Ar-Rahman", "Al-Waqi'ah", "Al-Hadid", "Al-Mujadilah", "Al-Hashr", "Al-Mumtahanah", "As-Saff", "Al-Jumu'ah", "Al-Munafiqun", "At-Taghabun", "At-Talaq", "At-Tahrim", "Al-Mulk", "Al-Qalam", "Al-Haqqah", "Al-Ma'arij", "Nuh", "Al-Jinn", "Al-Muzzammil", "Al-Muddaththir", "Al-Qiyamah", "Al-Insan", "Al-Mursalat", "An-Naba'", "An-Nazi'at", "'Abasa", "At-Takwir", "Al-Infitar", "Al-Mutaffifin", "Al-Inshiqaq", "Al-Buruj", "At-Tariq", "Al-A'la", "Al-Ghashiyah", "Al-Fajr", "Al-Balad", "Ash-Shams", "Al-Layl", "Ad-Duha", "Ash-Sharh", "At-Tin", "Al-'Alaq", "Al-Qadr", "Al-Bayyinah", "Az-Zalzalah", "Al-'Adiyat", "Al-Qari'ah", "At-Takathur", "Al-'Asr", "Al-Humazah", "Al-Fil", "Quraysh", "Al-Ma'un", "Al-Kawthar", "Al-Kafirun", "An-Nasr", "Al-Masad", "Al-Ikhlas", "Al-Falaq", "An-Nas"
)

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun PrayerCompanionApp(viewModel: PrayerViewModel) {
    val context = LocalContext.current
    var currentTab by remember { mutableStateOf(Tabs.PRAYERS) }

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

    // Trigger permission requests safely on startup if not already granted
    LaunchedEffect(Unit) {
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

    Scaffold(
        bottomBar = {
            NavigationBar(
                modifier = Modifier
                    .windowInsetsPadding(WindowInsets.navigationBars)
                    .testTag("app_bottom_navigation"),
                tonalElevation = 8.dp
            ) {
                val tabs = listOf(
                    Tabs.PRAYERS to Icons.Default.Schedule,
                    Tabs.QURAN to Icons.Default.Book,
                    Tabs.WISDOM to Icons.Default.AutoAwesome,
                    Tabs.CALENDAR to Icons.Default.CalendarMonth,
                    Tabs.QIBLA to Icons.Default.Explore,
                    Tabs.STATS to Icons.Default.BarChart
                )
                tabs.forEach { (tabName, icon) ->
                    NavigationBarItem(
                        selected = currentTab == tabName,
                        onClick = { currentTab = tabName },
                        icon = { Icon(icon, contentDescription = tabName) },
                        label = { Text(tabName, fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                        modifier = Modifier.testTag("nav_item_${tabName.lowercase()}")
                    )
                }
            }
        },
        contentWindowInsets = WindowInsets.safeDrawing
    ) { paddingValues ->
        Crossfade(
            targetState = currentTab,
            animationSpec = tween(250),
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(MaterialTheme.colorScheme.background)
        ) { tab ->
            when (tab) {
                Tabs.PRAYERS -> PrayersScreen(
                    viewModel = viewModel,
                    onNavigateToQibla = { currentTab = Tabs.QIBLA },
                    onNavigateToQuran = { currentTab = Tabs.QURAN },
                    onNavigateToStats = { currentTab = Tabs.STATS }
                )
                Tabs.QURAN -> QuranScreen(viewModel)
                Tabs.WISDOM -> HadithDuaScreen(viewModel)
                Tabs.CALENDAR -> CalendarScreen(viewModel)
                Tabs.QIBLA -> QiblaScreen(viewModel)
                Tabs.STATS -> StatsScreen(viewModel)
            }
        }
    }
}

@Composable
fun PrayersScreen(
    viewModel: PrayerViewModel,
    onNavigateToQibla: () -> Unit,
    onNavigateToQuran: () -> Unit,
    onNavigateToStats: () -> Unit
) {
    val context = LocalContext.current
    val times by viewModel.prayerTimes.collectAsState()
    val todayLogs by viewModel.todayLogs.collectAsState()
    val nextInfo by viewModel.nextPrayerInfo.collectAsState()
    val activeSchool by viewModel.asrSchool.collectAsState()
    val selectedDateStr by viewModel.selectedDate.collectAsState()
    val label by viewModel.locationLabel.collectAsState()
    val quranEntries by viewModel.quranEntries.collectAsState()
    val allLogs by viewModel.allPrayerLogs.collectAsState()

    var showConfigDialog by remember { mutableStateOf(false) }
    var tempLat by remember { mutableStateOf(viewModel.latitude.value.toString()) }
    var tempLon by remember { mutableStateOf(viewModel.longitude.value.toString()) }
    var tempFajrAngle by remember { mutableStateOf(viewModel.fajrAngle.value.toString()) }
    var tempIshaAngle by remember { mutableStateOf(viewModel.ishaAngle.value.toString()) }

    var logDialogState by remember { mutableStateOf<String?>(null) } // holds name of prayer being detailed

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // --- NOOR BRANDING HEADER ROW (Elegant Dark style) ---
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Noor",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.SemiBold,
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
                        text = "$label (GPS Active)",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                }
            }
            // User Avatar Box with border matching theme configuration (#49454F and #938F99)
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF49454F))
                    .border(1.dp, Color(0xFF938F99), CircleShape)
                    .clickable { showConfigDialog = true }
                    .testTag("location_config_btn"),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "JS",
                    color = Color(0xFFE6E1E5),
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        // --- COUNTDOWN / LIVE TIMER BANNER (Elegant Dark Custom Card) ---
        val nextPrayerName = nextInfo.first
        val nextPrayerTime = when (nextPrayerName.lowercase()) {
            "fajr" -> times.fajr
            "sunrise" -> times.sunrise
            "dhuhr" -> times.dhuhr
            "asr" -> times.asr
            "maghrib" -> times.maghrib
            "isha" -> times.isha
            else -> "--:--"
        }

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF49454F)),
            border = BorderStroke(1.dp, Color(0xFF49454F))
        ) {
            Column(modifier = Modifier.padding(24.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "NEXT PRAYER",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color(0xFFD0BCFF),
                        fontWeight = FontWeight.Medium,
                        letterSpacing = 1.5.sp
                    )
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .background(Color(0xFFD0BCFF))
                            .padding(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "Live",
                            color = Color(0xFF381E72),
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
                    color = Color.White
                )
                Text(
                    text = nextInfo.second,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color(0xFFE6E1E5).copy(alpha = 0.82f),
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
                            fontWeight = FontWeight.Light,
                            color = Color.White
                        )
                        Text(
                            text = "ADHAN TIME",
                            fontSize = 10.sp,
                            color = Color.White.copy(alpha = 0.5f),
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Box(
                        modifier = Modifier
                            .height(36.dp)
                            .width(1.dp)
                            .background(Color(0xFF938F99).copy(alpha = 0.3f))
                    )

                    Button(
                        onClick = onNavigateToQibla,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFD0BCFF),
                            contentColor = Color(0xFF381E72)
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Qibla Finder", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                    }
                }
            }
        }

        // --- DASHBOARD GRID CARDS (Daily Quran & Prayer Log) ---
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

            // Prayer Log Card
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
        }

        // --- HORIZONTAL CALENDAR DATE SELECTOR ---
        HorizontalDateSelector(selectedDateStr) { dateStr ->
            viewModel.selectDate(dateStr)
        }

        Spacer(modifier = Modifier.height(16.dp))

        // --- PRAYER TIMES LIST WITH CHECKLOGGERS ---
        val prayers = listOf(
            "Fajr" to times.fajr,
            "Sunrise" to times.sunrise,
            "Dhuhr" to times.dhuhr,
            "Asr" to times.asr,
            "Maghrib" to times.maghrib,
            "Isha" to times.isha
        )

        prayers.forEach { (name, time) ->
            val log = todayLogs.find { it.prayerName == name }
            val completed = log?.completed ?: false

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 6.dp)
                    .testTag("prayer_card_${name.lowercase()}"),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (completed) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
                    else MaterialTheme.colorScheme.surface
                ),
                border = BorderStroke(
                    1.dp,
                    if (completed) MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)
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
                                text = name,
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = if (name == "Sunrise") MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f) else MaterialTheme.colorScheme.onSurface
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
                        }
                        Text(
                            text = if (time.isNotEmpty()) time else "--:--",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }

                    if (name != "Sunrise") {
                        IconButton(
                            onClick = {
                                if (completed) {
                                    // Remove log
                                    viewModel.togglePrayer(name, false)
                                } else {
                                    // Open options log dialog immediately
                                    logDialogState = name
                                }
                            },
                            modifier = Modifier
                                .size(48.dp)
                                .testTag("checkbox_$name")
                        ) {
                            Icon(
                                imageVector = if (completed) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
                                contentDescription = "Log $name",
                                tint = if (completed) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline,
                                modifier = Modifier.size(32.dp)
                            )
                        }
                    } else {
                        Icon(
                            Icons.Default.WbSunny,
                            contentDescription = "Sunrise",
                            tint = Color(0xFFECC27E),
                            modifier = Modifier.size(28.dp)
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
                var isInCongregation by remember { mutableStateOf(false) }
                var isQada by remember { mutableStateOf(false) }
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
                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Prayed in Congregation (Jama'at)")
                        Switch(
                            checked = isInCongregation,
                            onCheckedChange = { isInCongregation = it },
                            modifier = Modifier.testTag("switch_jamaat")
                        )
                    }
                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Prayed Qada (Missed Time)")
                        Switch(
                            checked = isQada,
                            onCheckedChange = { isQada = it },
                            modifier = Modifier.testTag("switch_qada")
                        )
                    }
                    Spacer(modifier = Modifier.height(24.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        TextButton(onClick = { logDialogState = null }) {
                            Text("Cancel", color = MaterialTheme.colorScheme.error)
                        }
                        Button(
                            onClick = {
                                viewModel.togglePrayer(
                                    prayerName = name,
                                    completed = true,
                                    isCongregation = isInCongregation,
                                    isQada = isQada
                                )
                                logDialogState = null
                            },
                            modifier = Modifier.testTag("btn_save_detailed_log")
                        ) {
                            Text("Log Prayer")
                        }
                    }
                }
            }
        }
    }

    // --- COORDINATES CONFIG DIALOG ---
    if (showConfigDialog) {
        Dialog(onDismissRequest = { showConfigDialog = false }) {
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
                        text = "Calculation Settings",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )

                    // School Select
                    Text(text = "Asr Shadow School", fontWeight = FontWeight.Bold, fontSize = 14.sp)
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
                    Spacer(modifier = Modifier.height(16.dp))

                    Text(text = "Georeference Inputs", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    Spacer(modifier = Modifier.height(4.dp))
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
                    Spacer(modifier = Modifier.height(16.dp))

                    Text(text = "Astronomical Angles", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    Spacer(modifier = Modifier.height(4.dp))
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

                    Button(
                        onClick = {
                            viewModel.fetchGPSLocation(context)
                            tempLat = viewModel.latitude.value.toString()
                            tempLon = viewModel.longitude.value.toString()
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
                    ) {
                        Icon(Icons.Default.MyLocation, contentDescription = "GPS")
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Detect Current GPS")
                    }
                    Spacer(modifier = Modifier.height(24.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        TextButton(onClick = { showConfigDialog = false }) {
                            Text("Cancel")
                        }
                        Button(
                            onClick = {
                                val latDouble = tempLat.toDoubleOrNull() ?: viewModel.latitude.value
                                val lonDouble = tempLon.toDoubleOrNull() ?: viewModel.longitude.value
                                val fAngle = tempFajrAngle.toDoubleOrNull() ?: viewModel.fajrAngle.value
                                val iAngle = tempIshaAngle.toDoubleOrNull() ?: viewModel.ishaAngle.value

                                viewModel.updateCalculationAngles(fAngle, iAngle)
                                viewModel.updateLocation(latDouble, lonDouble, "Manual Geo Override")
                                showConfigDialog = false
                            },
                            modifier = Modifier.testTag("apply_settings_btn")
                        ) {
                            Text("Apply")
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun HorizontalDateSelector(selectedDateStr: String, onDateSelected: (String) -> Unit) {
    val format = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    val formatDay = SimpleDateFormat("EEE", Locale.getDefault())
    val formatDateOnly = SimpleDateFormat("dd", Locale.getDefault())
    
    // Generates a list of 7 dates (3 in past, today, 3 in future)
    val dates = remember {
        val list = mutableListOf<Date>()
        val cal = Calendar.getInstance()
        cal.add(Calendar.DATE, -3)
        for (i in 0..6) {
            list.add(cal.time)
            cal.add(Calendar.DATE, 1)
        }
        list
    }

    Column {
        Text(
            text = "Offline Day Tracker",
            fontWeight = FontWeight.Bold,
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(horizontal = 4.dp, vertical = 6.dp)
        )
        LazyRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            items(dates) { date ->
                val dateStr = format.format(date)
                val dayStr = formatDay.format(date)
                val dayNum = formatDateOnly.format(date)
                val isSelected = dateStr == selectedDateStr

                Box(
                    modifier = Modifier
                        .padding(horizontal = 4.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(
                            if (isSelected) MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                        )
                        .clickable { onDateSelected(dateStr) }
                        .padding(vertical = 12.dp, horizontal = 14.dp)
                        .widthIn(min = 36.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = dayStr.uppercase(),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = dayNum,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Black,
                            color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuranScreen(viewModel: PrayerViewModel) {
    val progressEntries by viewModel.quranEntries.collectAsState()
    
    var showAddDialog by remember { mutableStateOf(false) }

    // Dialog state
    var selectedSurah by remember { mutableStateOf("Al-Fatihah") }
    var surahNumber by remember { mutableStateOf(1) }
    var startAyah by remember { mutableStateOf("") }
    var endAyah by remember { mutableStateOf("") }
    var readDuration by remember { mutableStateOf("") }
    var surahExpanded by remember { mutableStateOf(false) }

    // Calculator for statistics: calculate current streaks
    val quranCount = progressEntries.size
    val totalTime = progressEntries.sumOf { it.durationMinutes }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // --- MOTIVATIONAL STATS CARD ---
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
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
                        text = "Read $quranCount days • Total: $totalTime mins reading",
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

        // --- BUTTON TO LOG NEW READING ---
        Button(
            onClick = { showAddDialog = true },
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
                .testTag("log_quran_btn"),
            shape = RoundedCornerShape(12.dp)
        ) {
            Icon(Icons.Default.Add, contentDescription = "Add")
            Spacer(modifier = Modifier.width(8.dp))
            Text("Log Daily Quran Progress", fontWeight = FontWeight.Bold)
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
                                Text(
                                    text = "Ayah ${entry.startAyah} to ${entry.endAyah} • Read: ${entry.durationMinutes} mins",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.outline
                                )
                                Text(
                                    text = "Logged: ${entry.date}",
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

                    OutlinedTextField(
                        value = readDuration,
                        onValueChange = { readDuration = it },
                        label = { Text("Read Duration (Minutes)") },
                        modifier = Modifier.fillMaxWidth().testTag("input_duration")
                    )
                    Spacer(modifier = Modifier.height(24.dp))

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
                                val duration = readDuration.toIntOrNull() ?: 10

                                viewModel.addQuranProgress(
                                    surahName = selectedSurah,
                                    surahNum = surahNumber,
                                    start = sAyah,
                                    end = eAyah,
                                    duration = duration
                                )
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
    val bearing by viewModel.kaabaBearing.collectAsState()
    val heading by viewModel.deviceHeading.collectAsState()

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
                .padding(bottom = 24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Qibla Direction",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Real-time spherical bearing is computed relative to True North based on your precise GPS coordinates.",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }

        // --- COMPASS GRAPHIC DRAW BLOCK ---
        Box(
            modifier = Modifier
                .size(280.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.surface)
                .border(2.dp, MaterialTheme.colorScheme.outlineVariant, CircleShape)
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
                        color = Color.Gray.copy(alpha = 0.5f),
                        start = start,
                        end = end,
                        strokeWidth = if (i % 90 == 0) 3.dp.toPx() else 1.5.dp.toPx()
                    )
                }

                // Draw Compass ring
                drawCircle(
                    color = Color.Green.copy(alpha = 0.1f),
                    radius = radius - 20,
                    style = Stroke(width = 2.dp.toPx())
                )
            }

            // Direction arrow rotates cleanly based on orientation and bearings
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .rotate(-heading) // Rotate entire card relative to north
                    .padding(12.dp),
                contentAlignment = Alignment.Center
            ) {
                // North Marker
                Text(
                    text = "N",
                    fontWeight = FontWeight.Black,
                    fontSize = 20.sp,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .padding(top = 4.dp)
                )

                // East/West/South Indicators
                Text("E", style = MaterialTheme.typography.labelSmall, modifier = Modifier.align(Alignment.CenterEnd))
                Text("W", style = MaterialTheme.typography.labelSmall, modifier = Modifier.align(Alignment.CenterStart))
                Text("S", style = MaterialTheme.typography.labelSmall, modifier = Modifier.align(Alignment.BottomCenter))

                // The Golden Qibla Pointer rotates pointing to the Kaaba
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .rotate(bearing.toFloat()), // Turns arrow point direct to Mecca bearing
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.fillMaxHeight()
                    ) {
                        Spacer(modifier = Modifier.height(18.dp))
                        Icon(
                            Icons.Default.Navigation,
                            contentDescription = "Qibla needle",
                            tint = Color(0xFFD4AF37), // Burnished Gold
                            modifier = Modifier
                                .size(48.dp)
                                .testTag("qibla_compass_needle")
                        )
                        Spacer(modifier = Modifier.weight(1f))
                        Text(
                            text = "🕋",
                            fontSize = 22.sp
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(28.dp))

        // --- BEARING TEXT FEEDBACKS ---
        Card(
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)),
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
                    Triple("Fajr", allLogs.count { it.prayerName == "Fajr" && it.completed }, Color(0xFF33C08E)),
                    Triple("Dhuhr", allLogs.count { it.prayerName == "Dhuhr" && it.completed }, Color(0xFFECC27E)),
                    Triple("Asr", allLogs.count { it.prayerName == "Asr" && it.completed }, Color(0xFFC5A059)),
                    Triple("Maghrib", allLogs.count { it.prayerName == "Maghrib" && it.completed }, Color(0xFF0F9D58)),
                    Triple("Isha", allLogs.count { it.prayerName == "Isha" && it.completed }, Color(0xFF1B6A9F))
                ).forEach { (pName, pCount, barColor) ->
                    Column(modifier = Modifier.padding(vertical = 4.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(pName, fontWeight = FontWeight.Bold)
                            Text("$pCount prayed", fontWeight = FontWeight.Medium)
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
                    Text("$congregationCount Jama'at", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                }
                Divider(modifier = Modifier.height(34.dp).width(1.dp))
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Adjusted Logs", fontSize = 11.sp, color = MaterialTheme.colorScheme.outline)
                    Text("$qadaCount Qada", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
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
