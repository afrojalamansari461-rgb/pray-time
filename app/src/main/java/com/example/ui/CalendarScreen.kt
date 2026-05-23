package com.example.ui

import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.IslamicEvent
import com.example.data.IslamicEventData
import com.example.viewmodel.PrayerViewModel
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun CalendarScreen(viewModel: PrayerViewModel) {
    val context = LocalContext.current
    val reminders by viewModel.eventReminders.collectAsState()

    // Browse states defaulting to current year & month
    val today = Calendar.getInstance()
    var selectedYear by remember { mutableStateOf(today.get(Calendar.YEAR)) }
    var selectedMonthIndex by remember { mutableStateOf(today.get(Calendar.MONTH)) } // 0 = Jan, 11 = Dec

    // Clamp browsing to years 2025 to 2027
    if (selectedYear < 2025) selectedYear = 2025
    if (selectedYear > 2027) selectedYear = 2027

    // Mathematical grid calculation for Gregorian calendars
    val cal = Calendar.getInstance().apply {
        set(Calendar.YEAR, selectedYear)
        set(Calendar.MONTH, selectedMonthIndex)
        set(Calendar.DAY_OF_MONTH, 1)
    }

    val maxDays = cal.getActualMaximum(Calendar.DAY_OF_MONTH)
    val startDayOfWeek = cal.get(Calendar.DAY_OF_WEEK) // 1 = Sunday, 2 = Monday, etc.

    // Month name formatter
    val monthName = SimpleDateFormat("MMMM", Locale.getDefault()).format(cal.time)

    // Filter events occurring in this specific month/year
    val eventsThisMonth = IslamicEventData.EVENTS.filter { event ->
        try {
            val dateParts = event.dateStr.split("-")
            val evYear = dateParts[0].toInt()
            val evMonth = dateParts[1].toInt() - 1 // 0-based
            evYear == selectedYear && evMonth == selectedMonthIndex
        } catch (e: Exception) {
            false
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // --- SCREEN HEADER ---
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.CalendarMonth,
                contentDescription = "Calendar icon",
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(28.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Column {
                Text(
                    text = "Islamic Calendar",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.primary,
                    letterSpacing = (-0.5).sp
                )
                Text(
                    text = "Observe global events pageants, lunar observances, and fast schedules",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.62f)
                )
            }
        }

        // --- NAVIGATION CONTROLLERS ---
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF2B2930)),
            border = BorderStroke(1.dp, Color(0xFF49454F).copy(alpha = 0.5f))
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Prev button
                IconButton(
                    onClick = {
                        if (selectedMonthIndex == 0) {
                            if (selectedYear > 2025) {
                                selectedYear -= 1
                                selectedMonthIndex = 11
                            }
                        } else {
                            selectedMonthIndex -= 1
                        }
                    },
                    modifier = Modifier.testTag("calendar_prev_month_btn"),
                    enabled = !(selectedYear == 2025 && selectedMonthIndex == 0)
                ) {
                    Icon(
                        imageVector = Icons.Default.ChevronLeft,
                        contentDescription = "Previous Month",
                        tint = if (selectedYear == 2025 && selectedMonthIndex == 0) Color(0xFF938F99).copy(alpha = 0.3f) else MaterialTheme.colorScheme.primary
                    )
                }

                // Month/Year Display
                Text(
                    text = "$monthName $selectedYear",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.testTag("calendar_month_year_title")
                )

                // Next button
                IconButton(
                    onClick = {
                        if (selectedMonthIndex == 11) {
                            if (selectedYear < 2027) {
                                selectedYear += 1
                                selectedMonthIndex = 0
                            }
                        } else {
                            selectedMonthIndex += 1
                        }
                    },
                    modifier = Modifier.testTag("calendar_next_month_btn"),
                    enabled = !(selectedYear == 2027 && selectedMonthIndex == 11)
                ) {
                    Icon(
                        imageVector = Icons.Default.ChevronRight,
                        contentDescription = "Next Month",
                        tint = if (selectedYear == 2027 && selectedMonthIndex == 11) Color(0xFF938F99).copy(alpha = 0.3f) else MaterialTheme.colorScheme.primary
                    )
                }
            }
        }

        // --- WEEKDAY LABELS ---
        Row(
            modifier = Modifier.fillMaxWidth().padding(bottom = 6.dp),
            horizontalArrangement = Arrangement.SpaceAround
        ) {
            val checkWeekDays = listOf("Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat")
            checkWeekDays.forEach { day ->
                Text(
                    text = day,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (day == "Fri") MaterialTheme.colorScheme.primary else Color(0xFFCAC4D0),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.width(36.dp)
                )
            }
        }

        // --- CALENDAR GRID ---
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF2B2930)),
            border = BorderStroke(1.dp, Color(0xFF49454F).copy(alpha = 0.3f))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                val totalCells = startDayOfWeek - 1 + maxDays
                val rowCount = (totalCells + 6) / 7

                for (row in 0 until rowCount) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        for (col in 0 until 7) {
                            val cellIndex = row * 7 + col
                            val dayNumber = cellIndex - (startDayOfWeek - 1) + 1

                            if (dayNumber in 1..maxDays) {
                                // Find if there's an event on this day
                                val dayStr = if (dayNumber < 10) "0$dayNumber" else dayNumber.toString()
                                val monthStr = if (selectedMonthIndex + 1 < 10) "0${selectedMonthIndex + 1}" else (selectedMonthIndex + 1).toString()
                                val searchDate = "$selectedYear-$monthStr-$dayStr"
                                val eventOnDay = IslamicEventData.EVENTS.find { it.dateStr == searchDate }

                                val isToday = today.get(Calendar.YEAR) == selectedYear &&
                                              today.get(Calendar.MONTH) == selectedMonthIndex &&
                                              today.get(Calendar.DAY_OF_MONTH) == dayNumber

                                DayCell(
                                    day = dayNumber,
                                    isToday = isToday,
                                    event = eventOnDay,
                                    modifier = Modifier.testTag("day_cell_$dayNumber")
                                )
                            } else {
                                // Spacer for padding cells
                                Box(modifier = Modifier.size(36.dp))
                            }
                        }
                    }
                }
            }
        }

        // --- EVENTS LIST HEADER ---
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Events in $monthName",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFF49454F))
                    .padding(horizontal = 8.dp, vertical = 2.dp)
            ) {
                Text(
                    text = "${eventsThisMonth.size} OBSERVANCES",
                    fontSize = 9.sp,
                    color = Color(0xFFD0BCFF),
                    fontWeight = FontWeight.Bold
                )
            }
        }

        // --- MONTHLY EVENTS DETAIL LIST ---
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {
            if (eventsThisMonth.isEmpty()) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.CalendarToday,
                        contentDescription = "Empty calendar",
                        tint = Color(0xFFCAC4D0).copy(alpha = 0.4f),
                        modifier = Modifier.size(40.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "No major Islamic events on this Gregorian month.",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFFCAC4D0),
                        textAlign = TextAlign.Center
                    )
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(eventsThisMonth) { event ->
                        val isReminderEnabled = reminders.any { it.eventKey == event.id && it.reminderEnabled }
                        EventCard(
                            event = event,
                            isReminderSet = isReminderEnabled,
                            onToggleReminder = { enabled ->
                                viewModel.toggleEventReminder(event.id, enabled)
                                Toast.makeText(
                                    context,
                                    if (enabled) "Reminder enabled for ${event.title}" else "Reminder removed",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun DayCell(
    day: Int,
    isToday: Boolean,
    event: IslamicEvent?,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .size(36.dp)
            .clip(CircleShape)
            .background(
                when {
                    isToday -> MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f)
                    event != null && event.isMajor -> MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                    event != null -> Color(0xFF49454F)
                    else -> Color.Transparent
                }
            )
            .border(
                width = 1.dp,
                color = when {
                    isToday -> MaterialTheme.colorScheme.primary
                    event != null -> MaterialTheme.colorScheme.primary.copy(alpha = 0.4f)
                    else -> Color.Transparent
                },
                shape = CircleShape
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = day.toString(),
                fontSize = 13.sp,
                color = when {
                    isToday -> MaterialTheme.colorScheme.primary
                    event != null -> Color.White
                    else -> Color(0xFFE6E1E5)
                },
                fontWeight = if (isToday || event != null) FontWeight.Bold else FontWeight.Normal
            )
            // Little indicator dot for events
            if (event != null) {
                Box(
                    modifier = Modifier
                        .size(4.dp)
                        .clip(CircleShape)
                        .background(if (event.isMajor) MaterialTheme.colorScheme.primary else Color(0xFF938F99))
                )
            }
        }
    }
}

@Composable
fun EventCard(
    event: IslamicEvent,
    isReminderSet: Boolean,
    onToggleReminder: (Boolean) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("event_card_${event.id}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF2B2930)),
        border = BorderStroke(1.dp, Color(0xFF49454F).copy(alpha = 0.5f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = event.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = event.hijriDate,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Box(
                            modifier = Modifier
                                .size(3.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFCAC4D0))
                        )
                        Text(
                            text = event.dateStr,
                            fontSize = 11.sp,
                            color = Color(0xFFCAC4D0)
                        )
                    }
                }

                // Notification Bell icon toggle with custom color highlight when enabled!
                IconButton(
                    onClick = { onToggleReminder(!isReminderSet) },
                    modifier = Modifier.testTag("event_notification_button_${event.id}")
                ) {
                    Icon(
                        imageVector = if (isReminderSet) Icons.Default.NotificationsActive else Icons.Default.Notifications,
                        contentDescription = "Toggle Event Reminder Notification",
                        tint = if (isReminderSet) MaterialTheme.colorScheme.primary else Color(0xFFCAC4D0).copy(alpha = 0.8f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = event.description,
                style = MaterialTheme.typography.bodySmall,
                color = Color(0xFFCAC4D0).copy(alpha = 0.82f),
                lineHeight = 16.sp
            )
        }
    }
}
