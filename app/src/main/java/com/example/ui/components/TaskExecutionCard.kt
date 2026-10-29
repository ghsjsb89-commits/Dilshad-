package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.HourglassTop
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.JarvisTask
import com.example.model.StepState
import com.example.model.TaskExecutionStatus
import com.example.model.TaskStep
import com.example.ui.theme.*

@Composable
fun TaskExecutionCard(
    task: JarvisTask,
    modifier: Modifier = Modifier
) {
    GlassCard(
        modifier = modifier
            .fillMaxWidth()
            .testTag("task_execution_card"),
        isHighlighted = task.status == TaskExecutionStatus.EXECUTING
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // Task Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = task.title,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = JarvisGoldBright
                    )
                    Text(
                        text = task.titleUrdu,
                        fontSize = 12.sp,
                        color = JarvisTextSecondary
                    )
                }

                val (badgeText, badgeColor, badgeBg) = when (task.status) {
                    TaskExecutionStatus.COMPLETED -> Triple("COMPLETED", JarvisEmerald, JarvisEmeraldContainer)
                    TaskExecutionStatus.EXECUTING -> Triple("RUNNING", JarvisOrange, Color(0xFF332005))
                    TaskExecutionStatus.CONFIRMATION_REQUIRED -> Triple("CONFIRM", JarvisGoldBright, JarvisGoldContainer)
                    TaskExecutionStatus.FAILED -> Triple("FAILED", JarvisRed, Color(0xFF330D0B))
                    TaskExecutionStatus.PENDING -> Triple("PENDING", JarvisTextSecondary, JarvisSurfaceHighlight)
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(badgeBg)
                        .border(1.dp, badgeColor.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = badgeText,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = badgeColor
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Multi-step progress timeline
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                task.steps.forEachIndexed { idx, step ->
                    StepRow(step = step, stepNumber = idx + 1)
                }
            }

            // Result or error message
            if (task.resultMessage.isNotBlank()) {
                Spacer(modifier = Modifier.height(10.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(
                            if (task.status == TaskExecutionStatus.FAILED) Color(0xFF280B0B)
                            else JarvisSurfaceHighlight
                        )
                        .padding(10.dp)
                ) {
                    Text(
                        text = task.resultMessage,
                        fontSize = 12.sp,
                        color = if (task.status == TaskExecutionStatus.FAILED) JarvisRed else JarvisTextPrimary
                    )
                }
            }
        }
    }
}

@Composable
private fun StepRow(step: TaskStep, stepNumber: Int) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        // Step State Icon
        Box(
            modifier = Modifier
                .size(24.dp)
                .clip(CircleShape)
                .background(
                    when (step.state) {
                        StepState.DONE -> JarvisEmeraldContainer
                        StepState.RUNNING -> JarvisGoldContainer
                        StepState.FAILED -> Color(0xFF330D0B)
                        StepState.WAITING -> JarvisSurfaceHighlight
                    }
                )
                .border(
                    1.dp,
                    when (step.state) {
                        StepState.DONE -> JarvisEmerald
                        StepState.RUNNING -> JarvisGoldBright
                        StepState.FAILED -> JarvisRed
                        StepState.WAITING -> JarvisBorder
                    },
                    CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            when (step.state) {
                StepState.DONE -> Icon(Icons.Default.Check, null, tint = JarvisEmerald, modifier = Modifier.size(14.dp))
                StepState.RUNNING -> CircularProgressIndicator(modifier = Modifier.size(14.dp), strokeWidth = 2.dp, color = JarvisGoldBright)
                StepState.FAILED -> Icon(Icons.Default.Close, null, tint = JarvisRed, modifier = Modifier.size(14.dp))
                StepState.WAITING -> Text(text = "$stepNumber", fontSize = 10.sp, color = JarvisTextMuted)
            }
        }

        // Step Label & Detail
        Column(modifier = Modifier.weight(1f)) {
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = step.title,
                    fontSize = 12.sp,
                    fontWeight = if (step.state == StepState.RUNNING) FontWeight.Bold else FontWeight.Medium,
                    color = when (step.state) {
                        StepState.RUNNING -> JarvisGoldBright
                        StepState.DONE -> JarvisTextPrimary
                        StepState.FAILED -> JarvisRed
                        StepState.WAITING -> JarvisTextSecondary
                    }
                )
                Text(
                    text = step.titleUrdu,
                    fontSize = 11.sp,
                    color = JarvisTextMuted
                )
            }
            if (step.detail.isNotBlank()) {
                Text(
                    text = step.detail,
                    fontSize = 11.sp,
                    color = JarvisTextSecondary
                )
            }
        }
    }
}
