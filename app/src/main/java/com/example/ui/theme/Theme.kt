package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val VipColorScheme = darkColorScheme(
    primary = JarvisGold,
    onPrimary = JarvisBlack,
    primaryContainer = JarvisGoldContainer,
    onPrimaryContainer = JarvisGoldBright,
    secondary = JarvisEmerald,
    onSecondary = JarvisBlack,
    secondaryContainer = JarvisEmeraldContainer,
    onSecondaryContainer = JarvisEmerald,
    tertiary = JarvisCyan,
    onTertiary = JarvisBlack,
    background = JarvisBlack,
    onBackground = JarvisTextPrimary,
    surface = JarvisSurfaceDark,
    onSurface = JarvisTextPrimary,
    surfaceVariant = JarvisSurfaceCard,
    onSurfaceVariant = JarvisTextSecondary,
    outline = JarvisGoldMuted,
    outlineVariant = JarvisBorder,
    error = JarvisRed,
    onError = JarvisBlack
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = true, // VIP Edition is always in stunning dark VIP mode
    dynamicColor: Boolean = false, // Keep the signature VIP Black & Gold aesthetic
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = VipColorScheme,
        typography = Typography,
        content = content
    )
}
