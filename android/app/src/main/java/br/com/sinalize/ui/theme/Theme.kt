package br.com.sinalize.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val SinalizeColors = lightColorScheme(
    primary = Teal,
    onPrimary = Color.White,
    primaryContainer = PaleTeal,
    onPrimaryContainer = Navy,
    secondary = Navy,
    onSecondary = Color.White,
    background = Background,
    onBackground = Navy,
    surface = Color.White,
    onSurface = Navy,
    surfaceVariant = PaleTeal,
    onSurfaceVariant = Muted,
    outline = OutlineSoft
)

@Composable
fun SinalizeTheme(content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = SinalizeColors, typography = Typography, content = content)
}
