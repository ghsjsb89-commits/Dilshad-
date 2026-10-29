package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.JarvisStatus
import com.example.ui.components.GlassCard
import com.example.ui.components.TaskExecutionCard
import com.example.ui.components.VipAvatarCard
import com.example.ui.components.VipGlowingCore
import com.example.ui.theme.*
import com.example.viewmodel.JarvisViewModel

@Composable
fun CommandCenterScreen(
    viewModel: JarvisViewModel,
    modifier: Modifier = Modifier
) {
    val status by viewModel.status.collectAsState()
    val transcript by viewModel.transcript.collectAsState()
    val currentTask by viewModel.currentTask.collectAsState()
    val userName by viewModel.preferences.userName.collectAsState()
    val wakeWordEnabled by viewModel.preferences.wakeWordEnabled.collectAsState()
    val soundWaveLevel by viewModel.voiceEngine.soundWaveLevel.collectAsState()
    val floatingHudExpanded by viewModel.floatingHudExpanded.collectAsState()

    var textInput by remember { mutableStateOf("") }
    val focusManager = LocalFocusManager.current

    val quickCommands = listOf(
        Pair("YouTube Kholo", "یوٹیوب کھولیں"),
        Pair("Call Mama", "امی کو کال کریں"),
        Pair("Safar Ki Dua", "سفر کی دعا"),
        Pair("Wi-Fi Settings", "وائی فائی سیٹنگز"),
        Pair("5 Min Timer", "پانچ منٹ ٹائمر"),
        Pair("Fajr Time", "فجر کا وقت"),
        Pair("Qibla Compass", "قبلہ رخ")
    )

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(top = 12.dp, bottom = 96.dp)
    ) {
        // 1. VIP Holographic 3D Avatar Card
        item {
            VipAvatarCard(status = status, userName = userName)
        }

        // 2. Glowing Circular AI Arc Reactor Core with Sound-Wave Animation
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                VipGlowingCore(
                    status = status,
                    soundWaveLevel = soundWaveLevel,
                    onMicClick = {
                        if (status == JarvisStatus.LISTENING) {
                            viewModel.stopListening()
                        } else {
                            viewModel.startListening()
                        }
                    }
                )
            }
        }

        // 3. Wake Word & Voice HUD Toggle Bar
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Wake Word Toggle
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (wakeWordEnabled) JarvisGoldContainer else JarvisSurfaceHighlight)
                        .border(1.dp, if (wakeWordEnabled) JarvisGold else JarvisBorder, RoundedCornerShape(12.dp))
                        .clickable { viewModel.preferences.setWakeWordEnabled(!wakeWordEnabled) }
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                        .testTag("wake_word_toggle")
                ) {
                    Icon(
                        imageVector = Icons.Default.Hearing,
                        contentDescription = null,
                        tint = if (wakeWordEnabled) JarvisGoldBright else JarvisTextSecondary,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = if (wakeWordEnabled) "\"Hey JARVIS\": ON" else "\"Hey JARVIS\": OFF",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (wakeWordEnabled) JarvisGoldBright else JarvisTextSecondary
                    )
                }

                // Emergency Stop Button
                Button(
                    onClick = { viewModel.emergencyStop() },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = JarvisRed,
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(12.dp),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                    modifier = Modifier.testTag("emergency_stop_button")
                ) {
                    Icon(Icons.Default.StopCircle, null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = "STOP", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        // 4. Live Speech / Assistant Thought Transcript Card
        item {
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "LIVE SPEECH & INTELLIGENCE HUD",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = JarvisGoldBright,
                            letterSpacing = 1.sp
                        )
                        if (status == JarvisStatus.LISTENING || status == JarvisStatus.SPEAKING) {
                            Text(
                                text = "ACTIVE STREAM",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = JarvisEmerald
                            )
                        }
                    }

                    Text(
                        text = transcript,
                        fontSize = 14.sp,
                        color = JarvisTextPrimary,
                        lineHeight = 20.sp
                    )
                }
            }
        }

        // 5. Instant Voice Suggestion Chips (Urdu / Roman / English)
        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "QUICK VOICE COMMANDS (اردو / ROMAN URDU / ENGLISH)",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = JarvisTextSecondary,
                    letterSpacing = 0.5.sp
                )

                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(quickCommands) { (cmd, cmdUrdu) ->
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(JarvisSurfaceCard)
                                .border(1.dp, JarvisBorder, RoundedCornerShape(12.dp))
                                .clickable {
                                    viewModel.processSpokenCommand(cmd)
                                }
                                .padding(horizontal = 12.dp, vertical = 8.dp)
                                .testTag("quick_command_$cmd")
                        ) {
                            Column {
                                Text(
                                    text = cmd,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = JarvisGoldBright
                                )
                                Text(
                                    text = cmdUrdu,
                                    fontSize = 10.sp,
                                    color = JarvisTextMuted
                                )
                            }
                        }
                    }
                }
            }
        }

        // 6. Live Multi-step Task Timeline
        if (currentTask != null) {
            item {
                TaskExecutionCard(task = currentTask!!)
            }
        }

        // 7. Manual Text Command Input for Quiet Environments
        item {
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value = textInput,
                        onValueChange = { textInput = it },
                        placeholder = {
                            Text("Type in Urdu or English...", fontSize = 12.sp, color = JarvisTextMuted)
                        },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("manual_command_input"),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                        keyboardActions = KeyboardActions(onSend = {
                            if (textInput.isNotBlank()) {
                                viewModel.processSpokenCommand(textInput)
                                textInput = ""
                                focusManager.clearFocus()
                            }
                        }),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = JarvisGold,
                            unfocusedBorderColor = JarvisBorder,
                            focusedTextColor = JarvisTextPrimary,
                            unfocusedTextColor = JarvisTextPrimary
                        )
                    )

                    IconButton(
                        onClick = {
                            if (textInput.isNotBlank()) {
                                viewModel.processSpokenCommand(textInput)
                                textInput = ""
                                focusManager.clearFocus()
                            }
                        },
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(JarvisGold)
                            .testTag("send_command_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Send,
                            contentDescription = "Send Command",
                            tint = JarvisBlack,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }
    }
}
