package com.example.ui.components

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.PanTool
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.JarvisStatus
import com.example.ui.theme.*
import kotlin.math.roundToInt

@Composable
fun VipFloatingHud(
    status: JarvisStatus,
    transcript: String,
    isExpanded: Boolean,
    onToggleExpand: () -> Unit,
    onMicClick: () -> Unit,
    onQuickAction: (String) -> Unit,
    onEmergencyHalt: () -> Unit,
    modifier: Modifier = Modifier
) {
    var offsetX by remember { mutableFloatStateOf(0f) }
    var offsetY by remember { mutableFloatStateOf(0f) }

    Box(
        modifier = modifier
            .offset { IntOffset(offsetX.roundToInt(), offsetY.roundToInt()) }
            .pointerInput(Unit) {
                detectDragGestures { change, dragAmount ->
                    change.consume()
                    offsetX += dragAmount.x
                    offsetY += dragAmount.y
                }
            }
            .testTag("vip_floating_hud"),
        contentAlignment = Alignment.BottomEnd
    ) {
        if (!isExpanded) {
            // Minimized Floating Gold Orb
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.radialGradient(
                            listOf(JarvisGoldBright, JarvisGoldDark, JarvisBlack)
                        )
                    )
                    .border(2.dp, JarvisGoldBright, CircleShape)
                    .clickable { onToggleExpand() }
                    .testTag("floating_hud_minimized_orb"),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Mic,
                    contentDescription = "Expand Floating HUD",
                    tint = JarvisBlack,
                    modifier = Modifier.size(28.dp)
                )
            }
        } else {
            // Expanded VIP Mini Panel
            Surface(
                modifier = Modifier
                    .width(300.dp)
                    .clip(RoundedCornerShape(18.dp))
                    .border(1.5.dp, JarvisGold, RoundedCornerShape(18.dp)),
                color = JarvisSurfaceDark.copy(alpha = 0.95f),
                tonalElevation = 10.dp
            ) {
                Column(
                    modifier = Modifier
                        .background(
                            Brush.verticalGradient(
                                listOf(JarvisSurfaceCard, JarvisSurfaceDark)
                            )
                        )
                        .padding(14.dp)
                ) {
                    // Header
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(if (status == JarvisStatus.LISTENING) JarvisGoldBright else JarvisEmerald)
                            )
                            Text(
                                text = "VIP FLOATING HUD",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = JarvisGoldBright,
                                letterSpacing = 1.sp
                            )
                        }

                        IconButton(
                            onClick = onToggleExpand,
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(Icons.Default.Close, "Collapse HUD", tint = JarvisTextSecondary, modifier = Modifier.size(16.dp))
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Transcript Preview
                    Text(
                        text = transcript.take(90),
                        fontSize = 12.sp,
                        color = JarvisTextPrimary,
                        maxLines = 2
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Quick Command Chips
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        QuickChip(label = "YouTube", onClick = { onQuickAction("YouTube kholo") })
                        QuickChip(label = "Call", onClick = { onQuickAction("Call Mama") })
                        QuickChip(label = "Dua", onClick = { onQuickAction("Safar ki dua sunao") })
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Action Controls
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = onMicClick,
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (status == JarvisStatus.LISTENING) JarvisRed else JarvisGold,
                                contentColor = JarvisBlack
                            )
                        ) {
                            Icon(Icons.Default.Mic, null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = if (status == JarvisStatus.LISTENING) "Stop" else "Listen",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        OutlinedButton(
                            onClick = onEmergencyHalt,
                            modifier = Modifier.weight(0.8f),
                            border = ButtonDefaults.outlinedButtonBorder.copy(brush = androidx.compose.ui.graphics.SolidColor(JarvisRed)),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = JarvisRed)
                        ) {
                            Text("HALT", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun QuickChip(label: String, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(JarvisSurfaceHighlight)
            .border(1.dp, JarvisBorder, RoundedCornerShape(8.dp))
            .clickable { onClick() }
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Text(text = label, fontSize = 10.sp, color = JarvisGoldBright)
    }
}
