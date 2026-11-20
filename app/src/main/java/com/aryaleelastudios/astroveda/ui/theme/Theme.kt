package com.aryaleelastudios.astroveda.ui.theme

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SelectableChipColors
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

private val AstroVedaLightColorScheme = lightColorScheme(
    primary = SaffronAccent,
    onPrimary = DeepIndigo,
    primaryContainer = SaffronLight.copy(alpha = 0.4f),
    onPrimaryContainer = DeepIndigo,
    secondary = DeepIndigo,
    onSecondary = Color.White,
    secondaryContainer = IndigoSurface,
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
    error = MutedRed,
    onError = Color.White
)

// Structured for clean dark-theme expansion in future versions
private val AstroVedaDarkColorScheme = darkColorScheme(
    primary = SaffronAccent,
    onPrimary = DeepIndigo,
    primaryContainer = IndigoContainer,
    onPrimaryContainer = Color.White,
    secondary = SaffronLight,
    onSecondary = TextPrimary,
    background = Color(0xFF14152B),
    onBackground = CreamBackground,
    surface = Color(0xFF1E203D),
    onSurface = CreamBackground,
    surfaceVariant = Color(0xFF2B2D5C),
    onSurfaceVariant = CreamBorder,
    outline = Color(0xFF3F427B),
    error = MutedRed,
    onError = Color.White
)

private val AstroVedaShapes = Shapes(
    extraSmall = RoundedCornerShape(6.dp),
    small = RoundedCornerShape(10.dp),
    medium = RoundedCornerShape(14.dp),
    large = RoundedCornerShape(18.dp),
    extraLarge = RoundedCornerShape(24.dp)
)

@Composable
fun astroPrimaryButtonColors(): ButtonColors = ButtonDefaults.buttonColors(
    containerColor = SaffronAccent,
    contentColor = DeepIndigo,
    disabledContainerColor = SaffronAccent.copy(alpha = 0.45f),
    disabledContentColor = DeepIndigo.copy(alpha = 0.5f)
)

@Composable
fun astroSecondaryButtonColors(): ButtonColors = ButtonDefaults.outlinedButtonColors(
    containerColor = Color.Transparent,
    contentColor = DeepIndigo
)

fun astroSecondaryButtonBorder(): BorderStroke = BorderStroke(1.25.dp, DeepIndigo)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun astroFilterChipColors(): SelectableChipColors = FilterChipDefaults.filterChipColors(
    containerColor = Color.Transparent,
    labelColor = TextPrimary,
    iconColor = TextPrimary,
    selectedContainerColor = SaffronAccent,
    selectedLabelColor = DeepIndigo,
    selectedLeadingIconColor = DeepIndigo
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun astroFilterChipBorder(
    enabled: Boolean = true,
    selected: Boolean
): BorderStroke = FilterChipDefaults.filterChipBorder(
    enabled = enabled,
    selected = selected,
    borderColor = CreamBorder,
    selectedBorderColor = SaffronDark,
    borderWidth = 1.dp,
    selectedBorderWidth = 1.5.dp
)

@Composable
fun AstroVedaTheme(
    languageCode: String = "en",
    darkTheme: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) AstroVedaDarkColorScheme else AstroVedaLightColorScheme
    val typography = remember(languageCode) { astroVedaTypography(languageCode) }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = typography,
        shapes = AstroVedaShapes,
        content = content
    )
}
