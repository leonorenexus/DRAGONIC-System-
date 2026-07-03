package com.dragonic.system.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight

// Cyberpunk color palette
val CyanNeon = Color(0xFF00FFFF)
val PurpleNeon = Color(0xFF7B2FFF)
val MagentaGlow = Color(0xFFFF00FF)
val DeepBlack = Color(0xFF0A0A0F)
val DarkSurface = Color(0xFF0F0F1A)
val CardSurface = Color(0xFF14141F)
val BorderCyan = Color(0xFF00FFFF).copy(alpha = 0.3f)
val TextPrimary = Color(0xFFE0E0FF)
val TextSecondary = Color(0xFF8080AA)
val DangerRed = Color(0xFFFF3366)
val SuccessGreen = Color(0xFF00FF88)
val WarnYellow = Color(0xFFFFCC00)

private val DragonicColorScheme = darkColorScheme(
    primary = CyanNeon,
    secondary = PurpleNeon,
    tertiary = MagentaGlow,
    background = DeepBlack,
    surface = DarkSurface,
    onPrimary = DeepBlack,
    onSecondary = Color.White,
    onBackground = TextPrimary,
    onSurface = TextPrimary,
    error = DangerRed
)

@Composable
fun DragonicTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = DragonicColorScheme,
        content = content
    )
}
