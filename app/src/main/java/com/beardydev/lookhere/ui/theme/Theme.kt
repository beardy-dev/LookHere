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
    primary = Color(0xFFFD594D),
    onPrimary = Color(0xFFFFFFFF),
    secondary = Color(0xFFFBB748),
    onSecondary = Color(0xFF402800),
    tertiary = Color(0xFF2F8F87),
    onTertiary = Color(0xFFFFFFFF),
    error = Color(0xFFB23A48),
    onBackground = Color(0xFF241512),
    onSurface = Color(0xFF241512),
    onSurfaceVariant = Color(0xFF7A655D),
)

private val DarkColors = darkColorScheme(
    background = Color(0xFF18120F),
    surface = Color(0xFF241B18),
    primary = Color(0xFFFF8A7B),
    onPrimary = Color(0xFF3D0F08),
    secondary = Color(0xFFFFCE7A),
    onSecondary = Color(0xFF402C00),
    tertiary = Color(0xFF5FC2BA),
    onTertiary = Color(0xFF00332F),
    error = Color(0xFFFF9A9E),
    onBackground = Color(0xFFF5E9E3),
    onSurface = Color(0xFFF5E9E3),
    onSurfaceVariant = Color(0xFFC2AFA7),
)

@Composable
fun LookHereTheme(content: @Composable () -> Unit) {
    val colorScheme = if (isSystemInDarkTheme()) DarkColors else LightColors
    MaterialTheme(colorScheme = colorScheme, content = content)
}
