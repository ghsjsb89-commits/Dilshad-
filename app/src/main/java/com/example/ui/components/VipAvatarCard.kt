package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.R
import com.example.model.JarvisStatus
import com.example.ui.theme.*

@Composable
fun VipAvatarCard(
    status: JarvisStatus,
    userName: String,
    modifier: Modifier = Modifier
) {
    var showIconDialog by remember { mutableStateOf(false) }
    var selectedVisualMode by remember { mutableIntStateOf(0) } // 0: 3D Avatar, 1: App Icon

    val infiniteTransition = rememberInfiniteTransition(label = "avatar_pulse")
    val borderAlpha by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "avatar_border_pulse"
    )

    GlassCard(
        modifier = modifier
            .fillMaxWidth()
            .testTag("vip_avatar_card"),
        isHighlighted = status == JarvisStatus.LISTENING || status == JarvisStatus.SPEAKING
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth()
        ) {
            // Header Badges
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(JarvisGoldContainer)
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = null,
                        tint = JarvisGoldBright,
                        modifier = Modifier.size(14.dp)
                    )
                    Text(
                        text = "VIP EDITION",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = JarvisGoldBright,
                        letterSpacing = 1.sp
                    )
                }

                // Interactive Icon Showcase Button
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(JarvisEmeraldContainer)
                        .clickable { showIconDialog = true }
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                        .testTag("show_ai_icon_badge")
                ) {
                    Icon(
                        imageVector = Icons.Default.Visibility,
                        contentDescription = "Show AI Icon",
                        tint = JarvisEmerald,
                        modifier = Modifier.size(14.dp)
                    )
                    Text(
                        text = "VIEW AI ICON",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = JarvisEmerald
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Central Visual (Clickable to inspect in full HD)
            Box(
                modifier = Modifier
                    .size(118.dp)
                    .clip(CircleShape)
                    .border(
                        width = 2.5.dp,
                        brush = Brush.sweepGradient(
                            listOf(
                                JarvisGold.copy(alpha = borderAlpha),
                                JarvisEmerald.copy(alpha = borderAlpha),
                                JarvisCyan.copy(alpha = borderAlpha),
                                JarvisGold.copy(alpha = borderAlpha)
                            )
                        ),
                        shape = CircleShape
                    )
                    .clickable { showIconDialog = true },
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(
                        id = if (selectedVisualMode == 0) R.drawable.img_jarvis_avatar else R.drawable.img_jarvis_logo
                    ),
                    contentDescription = "Muslim Jarvis VIP AI Icon / Avatar",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Quick switcher between 3D Avatar and App Icon
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                FilterChip(
                    selected = selectedVisualMode == 0,
                    onClick = { selectedVisualMode = 0 },
                    label = { Text("3D Avatar", fontSize = 10.sp) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = JarvisGold,
                        selectedLabelColor = JarvisBlack
                    )
                )
                FilterChip(
                    selected = selectedVisualMode == 1,
                    onClick = { selectedVisualMode = 1 },
                    label = { Text("AI Launcher Icon", fontSize = 10.sp) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = JarvisGold,
                        selectedLabelColor = JarvisBlack
                    )
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Title & Status
            Text(
                text = "MUSLIM JARVIS",
                fontSize = 18.sp,
                fontWeight = FontWeight.ExtraBold,
                color = JarvisGoldBright,
                letterSpacing = 1.5.sp
            )

            Text(
                text = "Personal VIP Android Assistant for $userName",
                fontSize = 12.sp,
                color = JarvisTextSecondary
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Live Assistant State Pill
            val (statusColor, statusBg) = when (status) {
                JarvisStatus.LISTENING -> Pair(JarvisGoldBright, JarvisGoldContainer)
                JarvisStatus.SPEAKING -> Pair(JarvisCyan, Color(0xFF0C2433))
                JarvisStatus.REASONING -> Pair(JarvisEmerald, JarvisEmeraldContainer)
                JarvisStatus.EXECUTING -> Pair(JarvisOrange, Color(0xFF332005))
                JarvisStatus.ERROR -> Pair(JarvisRed, Color(0xFF330D0B))
                JarvisStatus.STANDBY -> Pair(JarvisTextSecondary, JarvisSurfaceHighlight)
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(statusBg)
                    .border(1.dp, statusColor.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                    .padding(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(statusColor)
                )
                Text(
                    text = "${status.label} • ${status.labelUrdu}",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = statusColor
                )
            }
        }
    }

    // Full HD AI Icon & Avatar Inspection Dialog
    if (showIconDialog) {
        Dialog(onDismissRequest = { showIconDialog = false }) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(24.dp))
                    .border(1.5.dp, JarvisGold, RoundedCornerShape(24.dp))
                    .testTag("ai_icon_inspection_dialog"),
                color = JarvisSurfaceDark,
                tonalElevation = 10.dp
            ) {
                Column(
                    modifier = Modifier
                        .background(
                            Brush.verticalGradient(
                                listOf(JarvisSurfaceCard, JarvisSurfaceDark, JarvisBlack)
                            )
                        )
                        .padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Dialog Header
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(Icons.Default.Palette, null, tint = JarvisGoldBright, modifier = Modifier.size(20.dp))
                            Text(
                                text = "VIP AI Icon Showcase",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = JarvisGoldBright
                            )
                        }

                        IconButton(
                            onClick = { showIconDialog = false },
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(Icons.Default.Close, "Close", tint = JarvisTextSecondary, modifier = Modifier.size(18.dp))
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Large HD Display of the Selected Asset
                    Box(
                        modifier = Modifier
                            .size(180.dp)
                            .clip(RoundedCornerShape(24.dp))
                            .border(2.dp, JarvisGold, RoundedCornerShape(24.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Image(
                            painter = painterResource(
                                id = if (selectedVisualMode == 1) R.drawable.img_jarvis_logo else R.drawable.img_jarvis_avatar
                            ),
                            contentDescription = "Muslim Jarvis VIP Asset",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Mode Toggle Buttons
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = { selectedVisualMode = 1 },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (selectedVisualMode == 1) JarvisGold else JarvisSurfaceHighlight,
                                contentColor = if (selectedVisualMode == 1) JarvisBlack else JarvisTextPrimary
                            ),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text("Launcher Icon", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }

                        Button(
                            onClick = { selectedVisualMode = 0 },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (selectedVisualMode == 0) JarvisGold else JarvisSurfaceHighlight,
                                contentColor = if (selectedVisualMode == 0) JarvisBlack else JarvisTextPrimary
                            ),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text("3D Hologram", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Asset Metadata & Description
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(JarvisSurfaceHighlight)
                            .padding(12.dp)
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text(
                                text = if (selectedVisualMode == 1) "VIP LAUNCHER EMBLEM (Adaptive)" else "HOLOGRAPHIC 3D ENTITY",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = JarvisEmerald
                            )
                            Text(
                                text = if (selectedVisualMode == 1)
                                    "Features a royal circular arc reactor with Islamic 8-fold geometric arabesque circuits, glowing emerald aura, and metallic gold framing centered on solid Obsidian #0B0E14."
                                else
                                    "A high-definition 3D cybernetic android entity with a luminescent visor, floating particles, and obsidian gold chassis reflecting live voice intelligence.",
                                fontSize = 11.sp,
                                color = JarvisTextSecondary,
                                lineHeight = 16.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Button(
                        onClick = { showIconDialog = false },
                        colors = ButtonDefaults.buttonColors(containerColor = JarvisGold, contentColor = JarvisBlack),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Done", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
