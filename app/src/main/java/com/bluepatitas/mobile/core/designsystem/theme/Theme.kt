package com.bluepatitas.mobile.core.designsystem.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val LightColorScheme: ColorScheme = lightColorScheme(
    primary = BluePrimary,
    onPrimary = White,
    primaryContainer = BlueSurfaceVariant,
    onPrimaryContainer = BlueDark,
    secondary = GreenSuccess,
    onSecondary = White,
    error = RedCritical,
    onError = White,
    background = White,
    onBackground = Ink,
    surface = White,
    onSurface = Ink,
    surfaceVariant = BlueSurface,
    onSurfaceVariant = MutedInk,
    outline = BlueSurfaceVariant
)

@Composable
fun BluePatitasTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = LightColorScheme,
        typography = BluePatitasTypography,
        shapes = BluePatitasShapes,
        content = content
    )
}
