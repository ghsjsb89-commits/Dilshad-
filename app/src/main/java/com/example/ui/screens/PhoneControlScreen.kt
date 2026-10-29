package com.example.ui.screens

import android.content.Intent
import android.provider.Settings
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.InstalledAppItem
import com.example.service.JarvisAccessibilityService
import com.example.ui.components.GlassCard
import com.example.ui.theme.*
import com.example.viewmodel.JarvisViewModel

@Composable
fun PhoneControlScreen(
    viewModel: JarvisViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val installedApps = remember { viewModel.executor.getInstalledApps() }
    val isAccessibilityActive by JarvisAccessibilityService.isServiceActive.collectAsState()

    var textToType by remember { mutableStateOf("Assalamu Alaikum! Dictated by Muslim Jarvis VIP.") }
    var appSearchQuery by remember { mutableStateOf("") }

    val filteredApps = remember(appSearchQuery, installedApps) {
        if (appSearchQuery.isBlank()) installedApps.take(12)
        else installedApps.filter { it.name.contains(appSearchQuery, ignoreCase = true) }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(top = 12.dp, bottom = 96.dp)
    ) {
        // 1. Accessibility Service Automation Engine Card
        item {
            GlassCard(
                modifier = Modifier.fillMaxWidth(),
                isHighlighted = isAccessibilityActive
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(if (isAccessibilityActive) JarvisEmeraldContainer else JarvisGoldContainer),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.AccessibilityNew,
                                    contentDescription = null,
                                    tint = if (isAccessibilityActive) JarvisEmerald else JarvisGoldBright,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Column {
                                Text(
                                    text = "Accessibility Service Controller",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = JarvisGoldBright
                                )
                                Text(
                                    text = if (isAccessibilityActive) "Active • System Control Enabled" else "Service Disabled in Android Settings",
                                    fontSize = 11.sp,
                                    color = if (isAccessibilityActive) JarvisEmerald else JarvisTextSecondary
                                )
                            }
                        }

                        Button(
                            onClick = {
                                val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS).apply {
                                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                }
                                context.startActivity(intent)
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isAccessibilityActive) JarvisSurfaceHighlight else JarvisGold,
                                contentColor = if (isAccessibilityActive) JarvisTextPrimary else JarvisBlack
                            ),
                            shape = RoundedCornerShape(10.dp),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Text(if (isAccessibilityActive) "Settings" else "Enable", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    Divider(color = JarvisBorder)

                    // Navigation Actions
                    Text(
                        text = "VOICE-TRIGGERED SYSTEM GESTURES",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = JarvisTextSecondary
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        GestureActionButton(
                            icon = Icons.Default.ArrowBack,
                            label = "Back",
                            onClick = { viewModel.processSpokenCommand("back jao") },
                            modifier = Modifier.weight(1f)
                        )
                        GestureActionButton(
                            icon = Icons.Default.Home,
                            label = "Home",
                            onClick = { viewModel.processSpokenCommand("home screen par jao") },
                            modifier = Modifier.weight(1f)
                        )
                        GestureActionButton(
                            icon = Icons.Default.Notifications,
                            label = "Notifs",
                            onClick = { viewModel.processSpokenCommand("notifications kholo") },
                            modifier = Modifier.weight(1f)
                        )
                        GestureActionButton(
                            icon = Icons.Default.ArrowDownward,
                            label = "Scroll",
                            onClick = { viewModel.processSpokenCommand("scroll down") },
                            modifier = Modifier.weight(1f)
                        )
                    }

                    // Auto-typing test
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "VOICE-DICTATED AUTO TYPING (ACTION_SET_TEXT)",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = JarvisTextSecondary
                    )

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        OutlinedTextField(
                            value = textToType,
                            onValueChange = { textToType = it },
                            modifier = Modifier.weight(1f),
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = JarvisGold,
                                unfocusedBorderColor = JarvisBorder,
                                focusedTextColor = JarvisTextPrimary,
                                unfocusedTextColor = JarvisTextPrimary
                            )
                        )
                        Button(
                            onClick = {
                                viewModel.processSpokenCommand("type $textToType")
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = JarvisGold, contentColor = JarvisBlack),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text("Type", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // 2. Android System Settings Quick Dials
        item {
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "SYSTEM SETTINGS LAUNCHERS",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = JarvisGoldBright,
                        letterSpacing = 1.sp
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        SettingDialItem(
                            icon = Icons.Default.Wifi,
                            label = "Wi-Fi",
                            onClick = { viewModel.processSpokenCommand("wifi settings") },
                            modifier = Modifier.weight(1f)
                        )
                        SettingDialItem(
                            icon = Icons.Default.Bluetooth,
                            label = "Bluetooth",
                            onClick = { viewModel.processSpokenCommand("bluetooth settings") },
                            modifier = Modifier.weight(1f)
                        )
                        SettingDialItem(
                            icon = Icons.Default.PhotoCamera,
                            label = "Camera",
                            onClick = { viewModel.processSpokenCommand("camera kholo") },
                            modifier = Modifier.weight(1f)
                        )
                        SettingDialItem(
                            icon = Icons.Default.Settings,
                            label = "Settings",
                            onClick = { viewModel.processSpokenCommand("settings kholo") },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }

        // 3. Alarms & Timers Quick Controls
        item {
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "ALARMS & COUNTDOWN TIMERS",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = JarvisGoldBright,
                        letterSpacing = 1.sp
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        QuickTimerButton(
                            label = "5 Min Timer",
                            seconds = 300,
                            onClick = { viewModel.processSpokenCommand("5 minute ka timer lagao") },
                            modifier = Modifier.weight(1f)
                        )
                        QuickTimerButton(
                            label = "10 Min Timer",
                            seconds = 600,
                            onClick = { viewModel.processSpokenCommand("10 minute ka timer lagao") },
                            modifier = Modifier.weight(1f)
                        )
                        QuickTimerButton(
                            label = "Fajr Alarm (05:00)",
                            seconds = 0,
                            onClick = { viewModel.processSpokenCommand("set alarm for 5 am") },
                            modifier = Modifier.weight(1.2f)
                        )
                    }
                }
            }
        }

        // 4. Installed Apps Browser & Voice Launcher
        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "INSTALLED APPS (${installedApps.size})",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = JarvisGoldBright
                    )
                    Text(
                        text = "Tap or say 'Open [App]'",
                        fontSize = 11.sp,
                        color = JarvisTextSecondary
                    )
                }

                OutlinedTextField(
                    value = appSearchQuery,
                    onValueChange = { appSearchQuery = it },
                    placeholder = { Text("Filter apps (e.g. YouTube, Maps)...", fontSize = 12.sp, color = JarvisTextMuted) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    leadingIcon = { Icon(Icons.Default.Search, null, tint = JarvisGold) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = JarvisGold,
                        unfocusedBorderColor = JarvisBorder,
                        focusedTextColor = JarvisTextPrimary,
                        unfocusedTextColor = JarvisTextPrimary
                    )
                )

                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    filteredApps.forEach { appItem ->
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(JarvisSurfaceCard)
                                .border(1.dp, JarvisBorder, RoundedCornerShape(12.dp))
                                .clickable {
                                    viewModel.processSpokenCommand("open ${appItem.name}")
                                }
                                .padding(horizontal = 14.dp, vertical = 10.dp)
                                .testTag("app_item_${appItem.packageName}")
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(32.dp)
                                            .clip(CircleShape)
                                            .background(JarvisSurfaceHighlight),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Apps,
                                            contentDescription = null,
                                            tint = JarvisGold,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                    Column {
                                        Text(
                                            text = appItem.name,
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = JarvisTextPrimary
                                        )
                                        Text(
                                            text = appItem.packageName,
                                            fontSize = 10.sp,
                                            color = JarvisTextMuted
                                        )
                                    }
                                }

                                Icon(
                                    imageVector = Icons.Default.Launch,
                                    contentDescription = "Launch",
                                    tint = JarvisGold,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun GestureActionButton(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .background(JarvisSurfaceHighlight)
            .border(1.dp, JarvisBorder, RoundedCornerShape(10.dp))
            .clickable { onClick() }
            .padding(vertical = 10.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(icon, contentDescription = label, tint = JarvisGoldBright, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.height(2.dp))
            Text(label, fontSize = 10.sp, color = JarvisTextPrimary)
        }
    }
}

@Composable
private fun SettingDialItem(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(JarvisSurfaceHighlight)
            .border(1.dp, JarvisBorder, RoundedCornerShape(12.dp))
            .clickable { onClick() }
            .padding(vertical = 12.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(icon, contentDescription = label, tint = JarvisEmerald, modifier = Modifier.size(22.dp))
            Spacer(modifier = Modifier.height(4.dp))
            Text(label, fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = JarvisTextPrimary)
        }
    }
}

@Composable
private fun QuickTimerButton(
    label: String,
    seconds: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .background(JarvisSurfaceHighlight)
            .border(1.dp, JarvisBorder, RoundedCornerShape(10.dp))
            .clickable { onClick() }
            .padding(vertical = 10.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(text = label, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = JarvisGoldBright)
    }
}
