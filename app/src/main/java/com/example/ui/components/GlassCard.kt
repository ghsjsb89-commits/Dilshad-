package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.ui.theme.JarvisBorder
import com.example.ui.theme.JarvisGold
import com.example.ui.theme.JarvisSurfaceCard
import com.example.ui.theme.JarvisSurfaceDark

@Composable
fun GlassCard(
    modifier: Modifier = Modifier,
    cornerRadius: Dp = 16.dp,
    borderColor: Color = JarvisBorder,
    isHighlighted: Boolean = false,
    content: @Composable BoxScope.() -> Unit
) {
    val effectiveBorder = if (isHighlighted) {
        BorderStroke(1.dp, Brush.linearGradient(listOf(JarvisGold, JarvisBorder)))
    } else {
        BorderStroke(1.dp, borderColor)
    }

    Surface(
        modifier = modifier.clip(RoundedCornerShape(cornerRadius)),
        shape = RoundedCornerShape(cornerRadius),
        color = JarvisSurfaceDark.copy(alpha = 0.85f),
        border = effectiveBorder,
        tonalElevation = 4.dp
    ) {
        Box(
            modifier = Modifier
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            JarvisSurfaceCard.copy(alpha = 0.5f),
                            JarvisSurfaceDark.copy(alpha = 0.9f)
                        )
                    )
                )
                .padding(16.dp),
            content = content
        )
    }
}
