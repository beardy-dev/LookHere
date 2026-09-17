package com.beardydev.lookhere.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// Colors pulled from the app logo -- see lookhere-color-palette.md at the repo root.

private val LightColors = lightColorScheme(
    background = Color(0xFFFFF8F1),
    surface = Color(0xFFFFFFFF),
    surfaceVariant = Color(0xFFF2E4DC),
    primary = Color(0xFFFD594D),
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFFFFDAD4),
    onPrimaryContainer = Color(0xFF410002),
    secondary = Color(0xFFFBB748),
    onSecondary = Color(0xFF402800),
    secondaryContainer = Color(0xFFFFDFA6),
    onSecondaryContainer = Color(0xFF2A1800),
    tertiary = Color(0xFF2F8F87),
    onTertiary = Color(0xFFFFFFFF),
    tertiaryContainer = Color(0xFFB6EEE6),
    onTertiaryContainer = Color(0xFF00201D),
    error = Color(0xFFB23A48),
    onError = Color(0xFFFFFFFF),
    errorContainer = Color(0xFFFFDAD9),
    onErrorContainer = Color(0xFF410008),
    onBackground = Color(0xFF241512),
    onSurface = Color(0xFF241512),
    onSurfaceVariant = Color(0xFF7A655D),
    outline = Color(0xFF8C7A72),
    outlineVariant = Color(0xFFD6C4BB),
    scrim = Color(0xFF000000),
    inverseSurface = Color(0xFF3A2D28),
    inverseOnSurface = Color(0xFFFFEDE6),
    inversePrimary = Color(0xFFFF8A7B),
    surfaceTint = Color(0xFFFD594D),
)

private val DarkColors = darkColorScheme(
    background = Color(0xFF18120F),
    surface = Color(0xFF241B18),
    surfaceVariant = Color(0xFF52443D),
    primary = Color(0xFFFF8A7B),
    onPrimary = Color(0xFF3D0F08),
    primaryContainer = Color(0xFF5F1207),
    onPrimaryContainer = Color(0xFFFFDAD4),
    secondary = Color(0xFFFFCE7A),
    onSecondary = Color(0xFF402C00),
    secondaryContainer = Color(0xFF5C3F00),
    onSecondaryContainer = Color(0xFFFFDFA6),
    tertiary = Color(0xFF5FC2BA),
    onTertiary = Color(0xFF00332F),
    tertiaryContainer = Color(0xFF004D46),
    onTertiaryContainer = Color(0xFFB6EEE6),
    error = Color(0xFFFF9A9E),
    onError = Color(0xFF680009),
    errorContainer = Color(0xFF930012),
    onErrorContainer = Color(0xFFFFDAD9),
    onBackground = Color(0xFFF5E9E3),
    onSurface = Color(0xFFF5E9E3),
    onSurfaceVariant = Color(0xFFC2AFA7),
    outline = Color(0xFF8C7A72),
    outlineVariant = Color(0xFF52443D),
    scrim = Color(0xFF000000),
    inverseSurface = Color(0xFFF5E9E3),
    inverseOnSurface = Color(0xFF241512),
    inversePrimary = Color(0xFFFD594D),
    surfaceTint = Color(0xFFFF8A7B),
)

@Composable
fun LookHereTheme(content: @Composable () -> Unit) {
    val colorScheme = if (isSystemInDarkTheme()) DarkColors else LightColors
    MaterialTheme(colorScheme = colorScheme, content = content)
}
