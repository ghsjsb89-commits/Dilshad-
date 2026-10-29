package com.example.ui.screens

import android.Manifest
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import com.example.model.LanguagePreference
import com.example.ui.components.GlassCard
import com.example.ui.theme.*
import com.example.viewmodel.JarvisViewModel

@Composable
fun SecurityPrivacyScreen(
    viewModel: JarvisViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val permissions by viewModel.permissions.collectAsState()
    val userName by viewModel.preferences.userName.collectAsState()
    val language by viewModel.preferences.language.collectAsState()
    val voiceSpeed by viewModel.preferences.voiceSpeed.collectAsState()
    val voicePitch by viewModel.preferences.voicePitch.collectAsState()

    var nameInput by remember { mutableStateOf(userName) }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) {
        viewModel.refreshPermissions()
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(top = 12.dp, bottom = 96.dp)
    ) {
        // 1. Emergency Killswitch Card
        item {
            GlassCard(
                modifier = Modifier.fillMaxWidth(),
                borderColor = JarvisRed
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
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF330D0B)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.StopCircle, null, tint = JarvisRed, modifier = Modifier.size(24.dp))
                        }
                        Column {
                            Text(
                                text = "Emergency Halt (Killswitch)",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = JarvisRed
                            )
                            Text(
                                text = "Instantly abort all speech & background actions",
                                fontSize = 11.sp,
                                color = JarvisTextSecondary
                            )
                        }
                    }

                    Button(
                        onClick = { viewModel.emergencyStop() },
                        colors = ButtonDefaults.buttonColors(containerColor = JarvisRed, contentColor = Color.White),
                        shape = RoundedCornerShape(10.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text("HALT", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // 2. Permission Dashboard Matrix
        item {
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "SECURITY & PERMISSION MATRIX",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = JarvisGoldBright,
                            letterSpacing = 1.sp
                        )
                        IconButton(
                            onClick = { viewModel.refreshPermissions() },
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(Icons.Default.Refresh, "Refresh", tint = JarvisGold, modifier = Modifier.size(16.dp))
                        }
                    }

                    // Granular Permissions
                    PermissionItem(
                        icon = Icons.Default.Mic,
                        title = "Microphone (RECORD_AUDIO)",
                        description = "Required to capture natural voice commands in Urdu and English.",
                        isGranted = permissions.hasMic,
                        onRequest = {
                            permissionLauncher.launch(arrayOf(Manifest.permission.RECORD_AUDIO))
                        }
                    )

                    PermissionItem(
                        icon = Icons.Default.Contacts,
                        title = "Contacts (READ_CONTACTS)",
                        description = "Enables resolving contact names for calling and messaging without error.",
                        isGranted = permissions.hasContacts,
                        onRequest = {
                            permissionLauncher.launch(arrayOf(Manifest.permission.READ_CONTACTS))
                        }
                    )

                    PermissionItem(
                        icon = Icons.Default.Phone,
                        title = "Phone Calling (CALL_PHONE)",
                        description = "Allows initiating calls directly by voice command.",
                        isGranted = permissions.hasPhoneCall,
                        onRequest = {
                            permissionLauncher.launch(arrayOf(Manifest.permission.CALL_PHONE))
                        }
                    )

                    PermissionItem(
                        icon = Icons.Default.Sms,
                        title = "Direct SMS (SEND_SMS)",
                        description = "Used to dispatch verified SMS messages upon user confirmation.",
                        isGranted = permissions.hasSms,
                        onRequest = {
                            permissionLauncher.launch(arrayOf(Manifest.permission.SEND_SMS))
                        }
                    )

                    PermissionItem(
                        icon = Icons.Default.LocationOn,
                        title = "Location (ACCESS_FINE_LOCATION)",
                        description = "Calculates accurate Qibla bearing to Mecca and astronomical prayer times.",
                        isGranted = permissions.hasLocation,
                        onRequest = {
                            permissionLauncher.launch(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION))
                        }
                    )

                    PermissionItem(
                        icon = Icons.Default.AccessibilityNew,
                        title = "Accessibility Service",
                        description = "Permits voice typing, scrolling, and system navigation across apps.",
                        isGranted = permissions.hasAccessibility,
                        onRequest = {
                            val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS).apply {
                                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                            }
                            context.startActivity(intent)
                        }
                    )

                    PermissionItem(
                        icon = Icons.Default.PictureInPicture,
                        title = "Draw Over Apps (Overlay HUD)",
                        description = "Displays the floating mini assistant panel over other apps.",
                        isGranted = permissions.hasOverlay,
                        onRequest = {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                                val intent = Intent(
                                    Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                                    Uri.parse("package:${context.packageName}")
                                ).apply { addFlags(Intent.FLAG_ACTIVITY_NEW_TASK) }
                                context.startActivity(intent)
                            }
                        }
                    )
                }
            }
        }

        // 3. User & Voice Preferences Card
        item {
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    Text(
                        text = "VIP ASSISTANT PREFERENCES",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = JarvisGoldBright,
                        letterSpacing = 1.sp
                    )

                    // User Name
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        OutlinedTextField(
                            value = nameInput,
                            onValueChange = { nameInput = it },
                            label = { Text("Your Preferred Name / Title", fontSize = 11.sp) },
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
                                if (nameInput.isNotBlank()) {
                                    viewModel.preferences.setUserName(nameInput)
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = JarvisGold, contentColor = JarvisBlack),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text("Save", fontWeight = FontWeight.Bold)
                        }
                    }

                    // Language Preference
                    Text(
                        text = "Voice Understanding Language:",
                        fontSize = 11.sp,
                        color = JarvisTextSecondary
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        LanguagePreference.values().forEach { pref ->
                            val isSelected = language == pref
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isSelected) JarvisGold else JarvisSurfaceHighlight)
                                    .clickable { viewModel.preferences.setLanguage(pref) }
                                    .padding(vertical = 8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = when (pref) {
                                        LanguagePreference.AUTO -> "Auto"
                                        LanguagePreference.URDU -> "اردو"
                                        LanguagePreference.ROMAN_URDU -> "Roman"
                                        LanguagePreference.ENGLISH -> "Eng"
                                    },
                                    fontSize = 11.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isSelected) JarvisBlack else JarvisTextSecondary
                                )
                            }
                        }
                    }

                    // TTS Voice Speed Slider
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Assistant Speech Speed", fontSize = 11.sp, color = JarvisTextSecondary)
                            Text("%.1fx".format(voiceSpeed), fontSize = 11.sp, color = JarvisGoldBright)
                        }
                        Slider(
                            value = voiceSpeed,
                            onValueChange = { viewModel.preferences.setVoiceSpeed(it) },
                            valueRange = 0.7f..1.4f,
                            colors = SliderDefaults.colors(thumbColor = JarvisGold, activeTrackColor = JarvisGold)
                        )
                    }

                    // TTS Voice Pitch Slider
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Voice Pitch Tuning", fontSize = 11.sp, color = JarvisTextSecondary)
                            Text("%.2fx".format(voicePitch), fontSize = 11.sp, color = JarvisGoldBright)
                        }
                        Slider(
                            value = voicePitch,
                            onValueChange = { viewModel.preferences.setVoicePitch(it) },
                            valueRange = 0.7f..1.3f,
                            colors = SliderDefaults.colors(thumbColor = JarvisEmerald, activeTrackColor = JarvisEmerald)
                        )
                    }
                }
            }
        }

        // 4. Privacy Policy & Zero-Eavesdropping Guarantee
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(JarvisSurfaceDark)
                    .border(1.dp, JarvisBorder, RoundedCornerShape(14.dp))
                    .padding(16.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(Icons.Default.Security, null, tint = JarvisEmerald, modifier = Modifier.size(18.dp))
                        Text(
                            text = "PRIVACY & SECURITY GUARANTEE",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = JarvisEmerald,
                            letterSpacing = 1.sp
                        )
                    }

                    Text(
                        text = "1. ZERO BACKGROUND RECORDING: Muslim Jarvis only listens when you tap the microphone or when wake word listening is actively enabled.\n\n" +
                               "2. CONTACT PRIVACY: Your phone book contacts are queried solely on-device to match names when you command Jarvis to call or message.\n\n" +
                               "3. USER CONFIRMATION: Consequential actions (such as sending an SMS) will always display a confirmation dialog before transmission.\n\n" +
                               "4. ISLAMIC INTEGRITY: Quran verses and Duas are stored from authenticated sources with verified Hadith references.",
                        fontSize = 11.sp,
                        color = JarvisTextSecondary,
                        lineHeight = 17.sp
                    )
                }
            }
        }
    }
}

@Composable
private fun PermissionItem(
    icon: ImageVector,
    title: String,
    description: String,
    isGranted: Boolean,
    onRequest: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(JarvisSurfaceHighlight)
            .border(1.dp, if (isGranted) JarvisBorder else JarvisRed.copy(alpha = 0.3f), RoundedCornerShape(10.dp))
            .padding(10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.Top,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.weight(1f)
        ) {
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .clip(CircleShape)
                    .background(if (isGranted) JarvisEmeraldContainer else Color(0xFF330D0B)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = if (isGranted) JarvisEmerald else JarvisRed,
                    modifier = Modifier.size(16.dp)
                )
            }

            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    text = title,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = JarvisTextPrimary
                )
                Text(
                    text = description,
                    fontSize = 10.sp,
                    color = JarvisTextSecondary,
                    lineHeight = 14.sp
                )
            }
        }

        Spacer(modifier = Modifier.width(8.dp))

        if (isGranted) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .background(JarvisEmeraldContainer)
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Text(text = "GRANTED", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = JarvisEmerald)
            }
        } else {
            Button(
                onClick = onRequest,
                colors = ButtonDefaults.buttonColors(containerColor = JarvisGold, contentColor = JarvisBlack),
                shape = RoundedCornerShape(8.dp),
                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
            ) {
                Text("Grant", fontSize = 10.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}
