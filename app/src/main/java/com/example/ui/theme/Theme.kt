package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// A unified, dark luxury color scheme embodying "Al Nasr Attar & Fragrance"
private val LuxuryColorScheme = darkColorScheme(
    primary = RoyalGold,
    onPrimary = Color.Black,
    primaryContainer = RichBronze,
    onPrimaryContainer = ChampagneIvory,
    secondary = WarmGold,
    onSecondary = Color.Black,
    secondaryContainer = VelvetSlate,
    onSecondaryContainer = ChampagneIvory,
    tertiary = RichBronze,
    onTertiary = ChampagneIvory,
    background = DeepObsidian,
    onBackground = ChampagneIvory,
    surface = SatinCharcoal,
    onSurface = ChampagneIvory,
    surfaceVariant = VelvetSlate,
    onSurfaceVariant = ChampagneIvory,
    outline = RoyalGold,
    error = WarmCoral
)

@Composable
fun MyApplicationTheme(
    content: @Composable () -> Unit,
) {
    // We enforce the custom luxury dark palette to keep the exclusive gold-on-black brand look
    MaterialTheme(
        colorScheme = LuxuryColorScheme,
        typography = Typography,
        content = content
    )
}
