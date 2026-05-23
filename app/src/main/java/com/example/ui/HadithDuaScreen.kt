package com.example.ui

import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.BookmarkBorder
import android.widget.Toast
import androidx.compose.ui.platform.LocalContext
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.Bookmark
import com.example.data.DuaItem
import com.example.data.HadithDuaData
import com.example.data.HadithItem
import com.example.viewmodel.PrayerViewModel

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun HadithDuaScreen(viewModel: PrayerViewModel) {
    val bookmarks by viewModel.bookmarks.collectAsState()
    
    var activeSubTab by remember { mutableStateOf("Hadiths") } // "Hadiths", "Duas", "Bookmarks"
    var searchQuery by remember { mutableStateOf("") }
    
    // Hadith filtering state
    var selectedHadithTopic by remember { mutableStateOf("All") }
    // Dua filtering state
    var selectedDuaOccasion by remember { mutableStateOf("All") }

    val clipboardManager = LocalClipboardManager.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // --- SCREEN TITLE ---
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.AutoAwesome,
                contentDescription = "Wisdom Icon",
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(28.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Column {
                Text(
                    text = "Islamic Wisdom",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.primary,
                    letterSpacing = (-0.5).sp
                )
                Text(
                    text = "Amplify your daily spirit with authentic Hadith and Duas",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.62f)
                )
            }
        }

        // --- SUB TABS ROW ---
        TabRow(
            selectedTabIndex = when (activeSubTab) {
                "Hadiths" -> 0
                "Duas" -> 1
                else -> 2
            },
            containerColor = Color.Transparent,
            contentColor = MaterialTheme.colorScheme.primary,
            indicator = { tabPositions ->
                TabRowDefaults.SecondaryIndicator(
                    modifier = Modifier.tabIndicatorOffset(tabPositions[
                        when (activeSubTab) {
                            "Hadiths" -> 0
                            "Duas" -> 1
                            else -> 2
                        }
                    ]),
                    color = MaterialTheme.colorScheme.primary
                )
            },
            modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)
        ) {
            Tab(
                selected = activeSubTab == "Hadiths",
                onClick = { activeSubTab = "Hadiths" },
                modifier = Modifier.testTag("wisdom_tab_hadiths")
            ) {
                Text("Hadith", modifier = Modifier.padding(12.dp), fontWeight = FontWeight.Bold, fontSize = 14.sp)
            }
            Tab(
                selected = activeSubTab == "Duas",
                onClick = { activeSubTab = "Duas" },
                modifier = Modifier.testTag("wisdom_tab_duas")
            ) {
                Text("Dua Library", modifier = Modifier.padding(12.dp), fontWeight = FontWeight.Bold, fontSize = 14.sp)
            }
            Tab(
                selected = activeSubTab == "Bookmarks",
                onClick = { activeSubTab = "Bookmarks" },
                modifier = Modifier.testTag("wisdom_tab_bookmarks")
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(12.dp)
                ) {
                    Icon(
                        imageVector = if (bookmarks.isNotEmpty()) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                        contentDescription = "Bookmarks count",
                        modifier = Modifier.size(16.dp),
                        tint = if (activeSubTab == "Bookmarks") MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Bookmarks (${bookmarks.size})", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                }
            }
        }

        // --- SEARCH INPUT BAR ---
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp)
                .testTag("wisdom_search_field"),
            placeholder = { Text("Search by keywords...") },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search icon") },
            trailingIcon = if (searchQuery.isNotEmpty()) {
                {
                    IconButton(onClick = { searchQuery = "" }) {
                        Icon(Icons.Default.Close, contentDescription = "Clear search")
                    }
                }
            } else null,
            shape = RoundedCornerShape(16.dp),
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
            )
        )

        // --- CHIP FILTERS (Only show if not in bookmarks tab) ---
        if (activeSubTab != "Bookmarks") {
            val chips = if (activeSubTab == "Hadiths") HadithDuaData.HADITH_TOPICS else HadithDuaData.DUA_OCCASIONS
            val activeSelected = if (activeSubTab == "Hadiths") selectedHadithTopic else selectedDuaOccasion

            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(chips) { chip ->
                    val isSelected = chip == activeSelected
                    FilterChip(
                        selected = isSelected,
                        onClick = {
                            if (activeSubTab == "Hadiths") {
                                selectedHadithTopic = chip
                            } else {
                                selectedDuaOccasion = chip
                            }
                        },
                        label = { Text(chip, fontSize = 12.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primary,
                            selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                        ),
                        border = BorderStroke(
                            width = 1.dp,
                            color = if (isSelected) Color.Transparent else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                        ),
                        shape = RoundedCornerShape(20.dp),
                        modifier = Modifier.testTag("wisdom_chip_$chip")
                    )
                }
            }
        }

        // --- CONTENT AREA ---
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {
            when (activeSubTab) {
                "Hadiths" -> {
                    val filteredHadiths = HadithDuaData.HADITHS.filter {
                        (selectedHadithTopic == "All" || it.topic == selectedHadithTopic) &&
                        (searchQuery.isEmpty() || it.text.contains(searchQuery, ignoreCase = true) || it.narration.contains(searchQuery, ignoreCase = true))
                    }

                    if (filteredHadiths.isEmpty()) {
                        NoResultsState("No authentic Hadiths match your filters.")
                    } else {
                        LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            items(filteredHadiths) { hadith ->
                                val isStarred = bookmarks.any { it.type == "hadith" && it.itemId == hadith.id }
                                HadithCard(
                                    hadith = hadith,
                                    isBookmarked = isStarred,
                                    onBookmarkToggle = { viewModel.toggleBookmark("hadith", hadith.id) },
                                    onCopy = {
                                        clipboardManager.setText(AnnotatedString("${hadith.text}\n— ${hadith.narration}"))
                                    }
                                )
                            }
                        }
                    }
                }
                "Duas" -> {
                    val filteredDuas = HadithDuaData.DUAS.filter {
                        (selectedDuaOccasion == "All" || it.occasion == selectedDuaOccasion) &&
                        (searchQuery.isEmpty() || it.title.contains(searchQuery, ignoreCase = true) || it.translation.contains(searchQuery, ignoreCase = true) || it.transliteration.contains(searchQuery, ignoreCase = true))
                    }

                    if (filteredDuas.isEmpty()) {
                        NoResultsState("No commonly recited Duas match your filters.")
                    } else {
                        LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            items(filteredDuas) { dua ->
                                val isStarred = bookmarks.any { it.type == "dua" && it.itemId == dua.id }
                                DuaCard(
                                    dua = dua,
                                    isBookmarked = isStarred,
                                    onBookmarkToggle = { viewModel.toggleBookmark("dua", dua.id) },
                                    onCopy = {
                                        clipboardManager.setText(AnnotatedString("${dua.title}\n${dua.arabic}\n${dua.transliteration}\n${dua.translation}"))
                                    }
                                )
                            }
                        }
                    }
                }
                "Bookmarks" -> {
                    val bookmarkedHadiths = HadithDuaData.HADITHS.filter { item ->
                        bookmarks.any { it.type == "hadith" && it.itemId == item.id }
                    }
                    val bookmarkedDuas = HadithDuaData.DUAS.filter { item ->
                        bookmarks.any { it.type == "dua" && it.itemId == item.id }
                    }

                    val totalBookmarked = bookmarkedHadiths.size + bookmarkedDuas.size

                    if (totalBookmarked == 0) {
                        NoResultsState("Your bookmarks library is empty. Star Hadith and Duas for fast reference.")
                    } else {
                        LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            if (bookmarkedHadiths.isNotEmpty()) {
                                item {
                                    Text(
                                        text = "Saved Hadiths",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.padding(vertical = 4.dp)
                                    )
                                }
                                items(bookmarkedHadiths) { hadith ->
                                    HadithCard(
                                        hadith = hadith,
                                        isBookmarked = true,
                                        onBookmarkToggle = { viewModel.toggleBookmark("hadith", hadith.id) },
                                        onCopy = {
                                            clipboardManager.setText(AnnotatedString("${hadith.text}\n— ${hadith.narration}"))
                                        }
                                    )
                                }
                            }
                            if (bookmarkedDuas.isNotEmpty()) {
                                item {
                                    Text(
                                        text = "Saved Supplications (Duas)",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.padding(top = 12.dp, bottom = 4.dp)
                                    )
                                }
                                items(bookmarkedDuas) { dua ->
                                    DuaCard(
                                        dua = dua,
                                        isBookmarked = true,
                                        onBookmarkToggle = { viewModel.toggleBookmark("dua", dua.id) },
                                        onCopy = {
                                            clipboardManager.setText(AnnotatedString("${dua.title}\n${dua.arabic}\n${dua.transliteration}\n${dua.translation}"))
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun HadithCard(
    hadith: HadithItem,
    isBookmarked: Boolean,
    onBookmarkToggle: () -> Unit,
    onCopy: () -> Unit
) {
    val context = LocalContext.current
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("hadith_card_${hadith.id}"),
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
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.2f))
                        .padding(horizontal = 8.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = hadith.topic.uppercase(),
                        color = MaterialTheme.colorScheme.primary,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                }

                Row {
                    IconButton(onClick = onCopy) {
                        Icon(
                            imageVector = Icons.Default.ContentCopy,
                            contentDescription = "Copy Hadith",
                            tint = Color(0xFFCAC4D0),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    IconButton(
                        onClick = {
                            onBookmarkToggle()
                            Toast.makeText(context, if (isBookmarked) "Unbookmarked" else "Bookmarked!", Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier.testTag("hadith_bookmark_btn_${hadith.id}")
                    ) {
                        Icon(
                            imageVector = if (isBookmarked) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                            contentDescription = "Bookmark toggle",
                            tint = if (isBookmarked) MaterialTheme.colorScheme.primary else Color(0xFFCAC4D0),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "\"${hadith.text}\"",
                style = MaterialTheme.typography.bodyLarge,
                lineHeight = 22.sp,
                fontWeight = FontWeight.Normal,
                color = Color.White
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "Narration: ${hadith.narration}",
                style = MaterialTheme.typography.labelSmall,
                color = Color(0xFFCAC4D0),
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun DuaCard(
    dua: DuaItem,
    isBookmarked: Boolean,
    onBookmarkToggle: () -> Unit,
    onCopy: () -> Unit
) {
    val context = LocalContext.current
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("dua_card_${dua.id}"),
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
                Text(
                    text = dua.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )

                Row {
                    IconButton(onClick = onCopy) {
                        Icon(
                            imageVector = Icons.Default.ContentCopy,
                            contentDescription = "Copy Dua",
                            tint = Color(0xFFCAC4D0),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    IconButton(
                        onClick = {
                            onBookmarkToggle()
                            Toast.makeText(context, if (isBookmarked) "Unbookmarked" else "Bookmarked!", Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier.testTag("dua_bookmark_btn_${dua.id}")
                    ) {
                        Icon(
                            imageVector = if (isBookmarked) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                            contentDescription = "Bookmark toggle",
                            tint = if (isBookmarked) MaterialTheme.colorScheme.primary else Color(0xFFCAC4D0),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Beautiful Large Right-to-Left Arabic text script
            Text(
                text = dua.arabic,
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontFamily = FontFamily.Serif
                ),
                color = Color.White,
                textAlign = TextAlign.Right,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                lineHeight = 36.sp
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Transliteration
            Text(
                text = dua.transliteration,
                style = MaterialTheme.typography.bodyMedium,
                fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.85f),
                lineHeight = 18.sp
            )

            Spacer(modifier = Modifier.height(8.dp))

            // English Translation
            Text(
                text = dua.translation,
                style = MaterialTheme.typography.bodySmall,
                color = Color(0xFFCAC4D0),
                lineHeight = 16.sp
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Tag/Occasion indicator hook
            Box(
                modifier = Modifier
                    .align(Alignment.End)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFF49454F))
                    .padding(horizontal = 8.dp, vertical = 2.dp)
            ) {
                Text(
                    text = dua.occasion,
                    color = Color(0xFFE6E1E5).copy(alpha = 0.82f),
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
fun NoResultsState(text: String) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.Info,
            contentDescription = "Info icon",
            tint = Color(0xFFCAC4D0).copy(alpha = 0.5f),
            modifier = Modifier.size(48.dp)
        )
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            color = Color(0xFFCAC4D0),
            textAlign = TextAlign.Center
        )
    }
}
