package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

val VoiceAssistantDarkColorScheme = darkColorScheme(
    primary = PrimaryIndigo,
    onPrimary = Color.White,
    primaryContainer = PrimaryIndigoContainer,
    onPrimaryContainer = TextPrimary,
    secondary = SecondaryCyan,
    onSecondary = Color(0xFF082F49),
    secondaryContainer = SecondaryCyanContainer,
    onSecondaryContainer = TextPrimary,
    tertiary = TertiaryViolet,
    onTertiary = Color(0xFF3B0764),
    tertiaryContainer = TertiaryVioletContainer,
    onTertiaryContainer = TextPrimary,
    background = DarkBg,
    onBackground = TextPrimary,
    surface = DarkSurface,
    onSurface = TextPrimary,
    surfaceVariant = DarkSurfaceElevated,
    onSurfaceVariant = TextSecondary,
    outline = BorderSubtle,
    error = AccentRed,
    onError = Color.White
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = true, // Clean dark UI priority
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = VoiceAssistantDarkColorScheme,
        typography = Typography,
        content = content
    )
}

