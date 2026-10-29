package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.model.ConfirmationPayload
import com.example.ui.theme.*

@Composable
fun ConfirmationDialog(
    payload: ConfirmationPayload,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .border(1.5.dp, JarvisGold, RoundedCornerShape(20.dp))
                .testTag("vip_confirmation_dialog"),
            color = JarvisSurfaceDark,
            tonalElevation = 8.dp
        ) {
            Column(
                modifier = Modifier
                    .background(JarvisSurfaceDark)
                    .padding(20.dp)
            ) {
                // Header
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(JarvisGoldContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Security,
                            contentDescription = null,
                            tint = JarvisGoldBright,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Column {
                        Text(
                            text = "VIP Authorization Required",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = JarvisGoldBright
                        )
                        Text(
                            text = "اجازت درکار ہے",
                            fontSize = 12.sp,
                            color = JarvisTextSecondary
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Detail Box
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(JarvisSurfaceHighlight)
                        .border(1.dp, JarvisBorder, RoundedCornerShape(12.dp))
                        .padding(14.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(
                            text = "ACTION: ${payload.actionType.uppercase()}",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = JarvisEmerald
                        )
                        Text(
                            text = "Recipient: ${payload.targetName} (${payload.targetContactOrPhone})",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = JarvisTextPrimary
                        )
                        Divider(color = JarvisBorder, modifier = Modifier.padding(vertical = 4.dp))
                        Text(
                            text = "Message Payload:",
                            fontSize = 11.sp,
                            color = JarvisTextMuted
                        )
                        Text(
                            text = "\"${payload.content}\"",
                            fontSize = 13.sp,
                            color = JarvisGoldBright
                        )
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                // Actions
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("dialog_cancel_button"),
                        border = ButtonDefaults.outlinedButtonBorder.copy(brush = androidx.compose.ui.graphics.SolidColor(JarvisBorder)),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = JarvisTextSecondary)
                    ) {
                        Text("Cancel / منسوخ")
                    }

                    Button(
                        onClick = {
                            payload.onConfirmAction()
                        },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("dialog_confirm_button"),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = JarvisGold,
                            contentColor = JarvisBlack
                        )
                    ) {
                        Text("Confirm / بھیجیں", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
