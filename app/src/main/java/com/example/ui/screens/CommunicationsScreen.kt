package com.example.ui.screens

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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.GlassCard
import com.example.ui.theme.*
import com.example.viewmodel.JarvisViewModel

@Composable
fun CommunicationsScreen(
    viewModel: JarvisViewModel,
    modifier: Modifier = Modifier
) {
    var searchContactName by remember { mutableStateOf("") }
    var recipientName by remember { mutableStateOf("") }
    var messageBody by remember { mutableStateOf("Assalamu Alaikum! (Sent via Muslim Jarvis VIP)") }

    val matchedContacts = remember(searchContactName) {
        if (searchContactName.isNotBlank()) viewModel.executor.findContacts(searchContactName)
        else emptyList()
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(top = 12.dp, bottom = 96.dp)
    ) {
        // 1. VIP Voice Call Center
        item {
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
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
                                    .background(JarvisGoldContainer),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.Phone, null, tint = JarvisGoldBright, modifier = Modifier.size(20.dp))
                            }
                            Column {
                                Text(
                                    text = "VIP Voice Calling",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = JarvisGoldBright
                                )
                                Text(
                                    text = "\"JARVIS, call [Contact Name]\"",
                                    fontSize = 11.sp,
                                    color = JarvisTextSecondary
                                )
                            }
                        }

                        Button(
                            onClick = { viewModel.processSpokenCommand("cut the call") },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = JarvisRed,
                                contentColor = Color.White
                            ),
                            shape = RoundedCornerShape(10.dp),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Icon(Icons.Default.CallEnd, null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Cut Call", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    OutlinedTextField(
                        value = searchContactName,
                        onValueChange = { searchContactName = it },
                        placeholder = { Text("Find contact (e.g. Mama, Ali, Dad)...", fontSize = 12.sp, color = JarvisTextMuted) },
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

                    if (matchedContacts.isNotEmpty()) {
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            matchedContacts.take(5).forEach { (name, phone) ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(JarvisSurfaceHighlight)
                                        .border(1.dp, JarvisBorder, RoundedCornerShape(10.dp))
                                        .padding(horizontal = 12.dp, vertical = 8.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text(name, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = JarvisTextPrimary)
                                        Text(phone, fontSize = 11.sp, color = JarvisTextMuted)
                                    }

                                    Button(
                                        onClick = { viewModel.processSpokenCommand("call $name") },
                                        colors = ButtonDefaults.buttonColors(containerColor = JarvisEmerald, contentColor = JarvisBlack),
                                        shape = RoundedCornerShape(8.dp),
                                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)
                                    ) {
                                        Icon(Icons.Default.Call, null, modifier = Modifier.size(14.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Call", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    } else if (searchContactName.isNotBlank()) {
                        Text(
                            text = "No contact matching '$searchContactName' found in phone book.",
                            fontSize = 11.sp,
                            color = JarvisTextMuted
                        )
                    }
                }
            }
        }

        // 2. Safe VIP Messaging Center (Requires User Confirmation)
        item {
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(JarvisEmeraldContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.ChatBubble, null, tint = JarvisEmerald, modifier = Modifier.size(20.dp))
                        }
                        Column {
                            Text(
                                text = "Safe VIP SMS Dispatch",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = JarvisGoldBright
                            )
                            Text(
                                text = "Always confirms recipient & text before sending",
                                fontSize = 11.sp,
                                color = JarvisEmerald
                            )
                        }
                    }

                    OutlinedTextField(
                        value = recipientName,
                        onValueChange = { recipientName = it },
                        label = { Text("Recipient Name or Phone Number", fontSize = 11.sp) },
                        placeholder = { Text("e.g. Ali or 03001234567", fontSize = 12.sp, color = JarvisTextMuted) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = JarvisGold,
                            unfocusedBorderColor = JarvisBorder,
                            focusedTextColor = JarvisTextPrimary,
                            unfocusedTextColor = JarvisTextPrimary
                        )
                    )

                    OutlinedTextField(
                        value = messageBody,
                        onValueChange = { messageBody = it },
                        label = { Text("Message Payload", fontSize = 11.sp) },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 2,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = JarvisGold,
                            unfocusedBorderColor = JarvisBorder,
                            focusedTextColor = JarvisTextPrimary,
                            unfocusedTextColor = JarvisTextPrimary
                        )
                    )

                    Button(
                        onClick = {
                            val target = recipientName.ifBlank { "Contact" }
                            viewModel.processSpokenCommand("send message to $target $messageBody")
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = JarvisGold,
                            contentColor = JarvisBlack
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.Send, null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Draft & Request Confirmation", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // 3. Hands-Free Safety Protocol Notice
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(JarvisSurfaceCard)
                    .border(1.dp, JarvisBorder, RoundedCornerShape(12.dp))
                    .padding(14.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.Top,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Icon(Icons.Default.VerifiedUser, null, tint = JarvisGoldBright, modifier = Modifier.size(20.dp))
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(
                            text = "ANTI-MISTAKE SECURITY PROTOCOL",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = JarvisGoldBright
                        )
                        Text(
                            text = "Muslim Jarvis VIP strictly validates contact numbers and displays a full-screen confirmation modal before transmitting SMS messages. Hands-free calls use authorized Android Telecom APIs.",
                            fontSize = 11.sp,
                            color = JarvisTextSecondary,
                            lineHeight = 16.sp
                        )
                    }
                }
            }
        }
    }
}
