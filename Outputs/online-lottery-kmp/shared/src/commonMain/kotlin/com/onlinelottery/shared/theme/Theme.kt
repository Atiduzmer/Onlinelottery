package com.onlinelottery.shared.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

// Apple-inspired semantic palette: system blue on a grouped light background.
val BrandBlue = Color(0xFF007AFF)
val BrandNavy = Color(0xFF1C1C1E)
val BrandBlueSoft = Color(0xFFEAF3FF)
val LiveGreen = Color(0xFF34C759)
val MintSoft = Color(0xFFE9F9EE)
val AlertRed = Color(0xFFFF3B30)
val WarmAmber = Color(0xFFFF9500)
val PageBackground = Color(0xFFF2F2F7)
val PrimaryText = Color(0xFF1C1C1E)
val SecondaryText = Color(0xFF8E8E93)
val CardBorder = Color(0xFFE5E5EA)
val DarkPageBackground = Color(0xFF05070C)
val DarkSurface = Color(0xFF111722)
val DarkSecondary = Color(0xFF9AA7B8)

enum class AppThemeMode { System, Light, Dark }

private val LightColors = lightColorScheme(
    primary = BrandBlue,
    onPrimary = Color.White,
    primaryContainer = BrandBlueSoft,
    onPrimaryContainer = BrandBlue,
    secondary = LiveGreen,
    error = AlertRed,
    background = PageBackground,
    onBackground = PrimaryText,
    surface = Color.White,
    onSurface = PrimaryText,
    surfaceVariant = Color(0xFFE5E5EA),
    onSurfaceVariant = SecondaryText,
    outline = CardBorder,
)

private val DarkColors = darkColorScheme(
    primary = Color(0xFF4F8CFF),
    onPrimary = Color.White,
    primaryContainer = Color(0xFF17356B),
    onPrimaryContainer = Color(0xFFD9E6FF),
    secondary = Color(0xFF63D98A),
    error = Color(0xFFFF6961),
    background = DarkPageBackground,
    onBackground = Color(0xFFF5F5F7),
    surface = DarkSurface,
    onSurface = Color(0xFFF5F5F7),
    surfaceVariant = Color(0xFF1C2432),
    onSurfaceVariant = DarkSecondary,
    outline = Color(0xFF394354),
)

private val AppTypography = Typography(
    headlineSmall = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Bold,
        fontSize = 26.sp,
        lineHeight = 32.sp,
    ),
    titleLarge = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.SemiBold,
        fontSize = 20.sp,
        lineHeight = 25.sp,
    ),
    titleMedium = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Medium,
        fontSize = 17.sp,
        lineHeight = 22.sp,
    ),
    bodyLarge = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontSize = 16.sp,
        lineHeight = 22.sp,
    ),
    bodyMedium = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontSize = 15.sp,
        lineHeight = 20.sp,
    ),
    bodySmall = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontSize = 13.sp,
        lineHeight = 18.sp,
    ),
    labelMedium = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Medium,
        fontSize = 13.sp,
        lineHeight = 18.sp,
    ),
)

@Composable
fun OnlineLotteryTheme(
    mode: AppThemeMode = AppThemeMode.System,
    content: @Composable () -> Unit,
) {
    val dark = when (mode) {
        AppThemeMode.System -> isSystemInDarkTheme()
        AppThemeMode.Light -> false
        AppThemeMode.Dark -> true
    }
    MaterialTheme(
        colorScheme = if (dark) DarkColors else LightColors,
        typography = AppTypography,
        content = content,
    )
}

@Composable
fun isDarkTheme(): Boolean = MaterialTheme.colorScheme.background == DarkPageBackground
