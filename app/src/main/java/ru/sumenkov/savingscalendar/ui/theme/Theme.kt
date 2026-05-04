package ru.sumenkov.savingscalendar.ui.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val SavingsColorScheme: ColorScheme = lightColorScheme(
    primary = Emerald,
    onPrimary = Color.White,
    primaryContainer = Mint,
    onPrimaryContainer = Graphite,
    secondary = Gold,
    onSecondary = Graphite,
    background = Background,
    onBackground = Graphite,
    surface = Color.White,
    onSurface = Graphite,
    surfaceVariant = Mint,
    onSurfaceVariant = SecondaryText,
    error = SoftRed,
    errorContainer = Color(0xFFF9DEDE),
    onErrorContainer = Graphite
)

@Composable
fun SavingsTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = SavingsColorScheme,
        typography = androidx.compose.material3.Typography(),
        content = content
    )
}
