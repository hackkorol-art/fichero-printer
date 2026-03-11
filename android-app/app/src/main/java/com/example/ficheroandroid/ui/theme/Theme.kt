package com.example.ficheroandroid.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp

private val DarkColorScheme = darkColorScheme(
    primary = FicheroColors.Fichero,
    onPrimary = FicheroColors.InkPrimary,
    secondary = FicheroColors.InkSecondary,
    onSecondary = FicheroColors.InkPrimary,
    tertiary = FicheroColors.InkTertiary,
    background = FicheroColors.Surface0,
    onBackground = FicheroColors.InkPrimary,
    surface = FicheroColors.Surface1,
    onSurface = FicheroColors.InkPrimary,
    surfaceVariant = FicheroColors.Surface2,
    outline = FicheroColors.BorderEmphasis,
    error = FicheroColors.StatusDanger,
    onError = FicheroColors.InkPrimary,
)

private val FicheroShapes = Shapes(
    small = RoundedCornerShape(6.dp),
    medium = RoundedCornerShape(10.dp),
    large = RoundedCornerShape(14.dp),
)

@Composable
fun FicheroTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = DarkColorScheme,
        typography = FicheroTypography,
        shapes = FicheroShapes,
        content = content,
    )
}
