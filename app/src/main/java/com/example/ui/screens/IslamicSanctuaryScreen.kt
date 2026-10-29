package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
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
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.IslamicRepository
import com.example.model.DuaCategory
import com.example.model.IslamicDua
import com.example.ui.components.GlassCard
import com.example.ui.theme.*
import com.example.viewmodel.JarvisViewModel
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun IslamicSanctuaryScreen(
    viewModel: JarvisViewModel,
    modifier: Modifier = Modifier
) {
    val prayerTimes by viewModel.prayerTimes.collectAsState()
    val qiblaBearing by viewModel.qiblaBearing.collectAsState()
    val deviceAzimuth by viewModel.qiblaManager.azimuth.collectAsState()
    val isAligned by viewModel.qiblaManager.isAligned.collectAsState()

    var selectedTab by remember { mutableIntStateOf(0) } // 0: Prayer & Qibla, 1: Quran, 2: Duas
    var selectedCategory by remember { mutableStateOf<DuaCategory?>(null) }

    val displayedDuas = remember(selectedCategory) {
        if (selectedCategory == null) IslamicRepository.authenticDuas
        else IslamicRepository.authenticDuas.filter { it.category == selectedCategory }
    }

    DisposableEffect(Unit) {
        viewModel.qiblaManager.start(qiblaBearing)
        onDispose {
            viewModel.qiblaManager.stop()
        }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(top = 12.dp, bottom = 96.dp)
    ) {
        // 1. Tab Selector (Prayer & Qibla | Quran | Authentic Duas)
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(JarvisSurfaceHighlight)
                    .padding(4.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                TabButton(
                    label = "Prayer & Qibla",
                    isSelected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    modifier = Modifier.weight(1f)
                )
                TabButton(
                    label = "Holy Quran",
                    isSelected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    modifier = Modifier.weight(1f)
                )
                TabButton(
                    label = "Masnoon Duas",
                    isSelected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    modifier = Modifier.weight(1f)
                )
            }
        }

        if (selectedTab == 0) {
            // Qibla Compass + Prayer Times Tab
            item {
                GlassCard(
                    modifier = Modifier.fillMaxWidth(),
                    isHighlighted = isAligned
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "LIVE QIBLA COMPASS",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = JarvisGoldBright,
                                    letterSpacing = 1.sp
                                )
                                Text(
                                    text = "Kaaba Bearing: ${qiblaBearing.toInt()}° from North",
                                    fontSize = 12.sp,
                                    color = JarvisTextSecondary
                                )
                            }

                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isAligned) JarvisEmeraldContainer else JarvisSurfaceHighlight)
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = if (isAligned) "ALIGNED WITH QIBLA" else "ALIGN COMPASS",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isAligned) JarvisEmerald else JarvisTextSecondary
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Luxury Golden Qibla Compass Dial
                        Box(
                            modifier = Modifier
                                .size(200.dp)
                                .testTag("qibla_compass_dial"),
                            contentAlignment = Alignment.Center
                        ) {
                            // Dial Canvas
                            Canvas(modifier = Modifier.fillMaxSize()) {
                                val center = Offset(size.width / 2, size.height / 2)
                                val radius = size.width / 2 - 12.dp.toPx()

                                // Outer gold ring
                                drawCircle(
                                    color = JarvisGold.copy(alpha = 0.5f),
                                    radius = radius,
                                    center = center,
                                    style = Stroke(2.dp.toPx())
                                )

                                // Cardinal marks
                                val angles = listOf(0, 90, 180, 270)
                                val labels = listOf("N", "E", "S", "W")
                                angles.forEachIndexed { i, deg ->
                                    val rad = Math.toRadians((deg - deviceAzimuth).toDouble())
                                    val startX = center.x + ((radius - 10.dp.toPx()) * sin(rad)).toFloat()
                                    val startY = center.y - ((radius - 10.dp.toPx()) * cos(rad)).toFloat()
                                    val endX = center.x + (radius * sin(rad)).toFloat()
                                    val endY = center.y - (radius * cos(rad)).toFloat()
                                    drawLine(
                                        color = if (i == 0) JarvisEmerald else JarvisGold,
                                        start = Offset(startX, startY),
                                        end = Offset(endX, endY),
                                        strokeWidth = 2.dp.toPx()
                                    )
                                }
                            }

                            // Kaaba Direction Pointer Needle
                            val relativeAngle = (qiblaBearing - deviceAzimuth + 360f) % 360f
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .rotate(relativeAngle),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    modifier = Modifier.fillMaxHeight()
                                ) {
                                    // Pointer Arrow to Kaaba
                                    Box(
                                        modifier = Modifier
                                            .size(28.dp)
                                            .clip(CircleShape)
                                            .background(if (isAligned) JarvisEmerald else JarvisGold)
                                            .border(1.dp, JarvisGoldBright, CircleShape),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = "🕋",
                                            fontSize = 14.sp
                                        )
                                    }
                                    Spacer(modifier = Modifier.weight(1f))
                                }
                            }

                            // Center Pivot
                            Box(
                                modifier = Modifier
                                    .size(16.dp)
                                    .clip(CircleShape)
                                    .background(JarvisGoldBright)
                                    .border(2.dp, JarvisBlack, CircleShape)
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "Hold phone flat for precise sensor orientation",
                            fontSize = 11.sp,
                            color = JarvisTextMuted
                        )
                    }
                }
            }

            // Prayer Times Schedule
            item {
                GlassCard(modifier = Modifier.fillMaxWidth()) {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "PRAYER TIMES (اوقات نماز)",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = JarvisGoldBright,
                                letterSpacing = 1.sp
                            )
                            Text(
                                text = "Today",
                                fontSize = 11.sp,
                                color = JarvisTextSecondary
                            )
                        }

                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            prayerTimes.forEach { pt ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(if (pt.isNext) JarvisGoldContainer else JarvisSurfaceHighlight)
                                        .border(
                                            1.dp,
                                            if (pt.isNext) JarvisGold else JarvisBorder,
                                            RoundedCornerShape(10.dp)
                                        )
                                        .padding(horizontal = 12.dp, vertical = 8.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        if (pt.isNext) {
                                            Box(
                                                modifier = Modifier
                                                    .size(8.dp)
                                                    .clip(CircleShape)
                                                    .background(JarvisGoldBright)
                                            )
                                        }
                                        Text(
                                            text = "${pt.nameEnglish} • ${pt.nameUrdu}",
                                            fontSize = 13.sp,
                                            fontWeight = if (pt.isNext) FontWeight.Bold else FontWeight.Medium,
                                            color = if (pt.isNext) JarvisGoldBright else JarvisTextPrimary
                                        )
                                    }

                                    Column(horizontalAlignment = Alignment.End) {
                                        Text(
                                            text = pt.timeFormatted,
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (pt.isNext) JarvisGoldBright else JarvisTextPrimary
                                        )
                                        if (pt.isNext) {
                                            Text(
                                                text = "${pt.minutesRemaining} min left",
                                                fontSize = 10.sp,
                                                color = JarvisEmerald
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        } else if (selectedTab == 1) {
            // Holy Quran Tab
            item {
                Text(
                    text = "AUTHENTIC QURANIC VERSES",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = JarvisGoldBright,
                    letterSpacing = 1.sp
                )
            }

            items(IslamicRepository.quranVerses) { ayah ->
                GlassCard(modifier = Modifier.fillMaxWidth()) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Surah ${ayah.surahNameEnglish} (${ayah.surahNameArabic}) - ${ayah.surahNumber}:${ayah.ayahNumber}",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = JarvisGoldBright
                            )
                            IconButton(
                                onClick = {
                                    viewModel.speak("Surah ${ayah.surahNameEnglish}. ${ayah.urduTranslation}")
                                },
                                modifier = Modifier.size(28.dp)
                            ) {
                                Icon(Icons.Default.VolumeUp, "Recite", tint = JarvisGold, modifier = Modifier.size(18.dp))
                            }
                        }

                        // Arabic Calligraphy Text
                        Text(
                            text = ayah.arabicText,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFFFE082),
                            textAlign = TextAlign.End,
                            lineHeight = 28.sp,
                            modifier = Modifier.fillMaxWidth()
                        )

                        // Urdu Translation
                        Text(
                            text = "اردو: ${ayah.urduTranslation}",
                            fontSize = 13.sp,
                            color = JarvisTextPrimary,
                            textAlign = TextAlign.End,
                            lineHeight = 20.sp,
                            modifier = Modifier.fillMaxWidth()
                        )

                        // English Translation
                        Text(
                            text = ayah.englishTranslation,
                            fontSize = 12.sp,
                            color = JarvisTextSecondary,
                            lineHeight = 18.sp
                        )

                        Text(
                            text = "Theme: ${ayah.theme}",
                            fontSize = 10.sp,
                            color = JarvisEmerald
                        )
                    }
                }
            }
        } else {
            // Masnoon Duas Tab
            item {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "AUTHENTIC SUNNAH DUAS",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = JarvisGoldBright,
                        letterSpacing = 1.sp
                    )

                    // Category Filter Chips
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        item {
                            FilterChip(
                                selected = selectedCategory == null,
                                onClick = { selectedCategory = null },
                                label = { Text("All Duas", fontSize = 11.sp) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = JarvisGold,
                                    selectedLabelColor = JarvisBlack
                                )
                            )
                        }
                        items(DuaCategory.values().toList()) { cat ->
                            FilterChip(
                                selected = selectedCategory == cat,
                                onClick = { selectedCategory = cat },
                                label = { Text(cat.titleEnglish, fontSize = 11.sp) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = JarvisGold,
                                    selectedLabelColor = JarvisBlack
                                )
                            )
                        }
                    }
                }
            }

            items(displayedDuas) { dua ->
                GlassCard(modifier = Modifier.fillMaxWidth()) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = dua.titleEnglish,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = JarvisGoldBright
                                )
                                Text(
                                    text = dua.titleUrdu,
                                    fontSize = 11.sp,
                                    color = JarvisTextSecondary
                                )
                            }
                            IconButton(
                                onClick = {
                                    viewModel.speak("${dua.titleEnglish}. ${dua.urduTranslation}")
                                },
                                modifier = Modifier.size(28.dp)
                            ) {
                                Icon(Icons.Default.VolumeUp, "Recite", tint = JarvisGold, modifier = Modifier.size(18.dp))
                            }
                        }

                        // Arabic Calligraphy Text
                        Text(
                            text = dua.arabicText,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFFFE082),
                            textAlign = TextAlign.End,
                            lineHeight = 28.sp,
                            modifier = Modifier.fillMaxWidth()
                        )

                        // Transliteration
                        Text(
                            text = dua.transliteration,
                            fontSize = 11.sp,
                            color = JarvisTextSecondary
                        )

                        // Urdu
                        Text(
                            text = "ترجمہ: ${dua.urduTranslation}",
                            fontSize = 12.sp,
                            color = JarvisTextPrimary,
                            textAlign = TextAlign.End,
                            lineHeight = 18.sp,
                            modifier = Modifier.fillMaxWidth()
                        )

                        // English
                        Text(
                            text = dua.englishTranslation,
                            fontSize = 11.sp,
                            color = JarvisTextSecondary,
                            lineHeight = 16.sp
                        )

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(Icons.Default.Verified, null, tint = JarvisEmerald, modifier = Modifier.size(12.dp))
                            Text(
                                text = "Source: ${dua.authenticSource}",
                                fontSize = 10.sp,
                                color = JarvisEmerald
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun TabButton(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .background(if (isSelected) JarvisGold else Color.Transparent)
            .clickable { onClick() }
            .padding(vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            fontSize = 11.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
            color = if (isSelected) JarvisBlack else JarvisTextSecondary
        )
    }
}
