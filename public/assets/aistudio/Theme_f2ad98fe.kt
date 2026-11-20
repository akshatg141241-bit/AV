package com.aryaleelastudios.kundaliguru.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

private val LightColorScheme = lightColorScheme(
    primary = DeepIndigo,
    onPrimary = Color.White,
    primaryContainer = SaffronLight,
    onPrimaryContainer = DeepIndigo,
    secondary = SaffronAccent,
    onSecondary = DeepIndigo,
    secondaryContainer = CreamSurfaceVariant,
    onSecondaryContainer = DeepIndigo,
    tertiary = SaffronDark,
    onTertiary = Color.White,
    background = CreamBackground,
    onBackground = TextPrimary,
    surface = CreamSurface,
    onSurface = TextPrimary,
    surfaceVariant = CreamSurfaceVariant,
    onSurfaceVariant = TextSecondary,
    outline = CreamBorder,
    outlineVariant = Color(0xFFEDE2CF)
)

val KundaliShapes = Shapes(
    small = RoundedCornerShape(8.dp),
    medium = RoundedCornerShape(16.dp),
    large = RoundedCornerShape(24.dp),
    extraLarge = RoundedCornerShape(32.dp)
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = false, // Light theme only per specification
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = LightColorScheme,
        typography = Typography,
        shapes = KundaliShapes,
        content = content
    )
}
