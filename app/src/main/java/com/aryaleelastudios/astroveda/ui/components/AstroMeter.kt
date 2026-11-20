package com.aryaleelastudios.astroveda.ui.components

import androidx.annotation.StringRes
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.StarOutline
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aryaleelastudios.astroveda.R
import com.aryaleelastudios.astroveda.ui.theme.AuspiciousGreen
import com.aryaleelastudios.astroveda.ui.theme.CreamBorder
import com.aryaleelastudios.astroveda.ui.theme.CreamSurfaceVariant
import com.aryaleelastudios.astroveda.ui.theme.DeepIndigo
import com.aryaleelastudios.astroveda.ui.theme.MutedRed
import com.aryaleelastudios.astroveda.ui.theme.SaffronAccent
import com.aryaleelastudios.astroveda.ui.theme.SaffronDark
import com.aryaleelastudios.astroveda.ui.theme.SaffronLight
import com.aryaleelastudios.astroveda.ui.theme.TextPrimary

data class AstroMeterMeta(
    @StringRes val statusResId: Int,
    val starCount: Int,
    val badgeColor: Color,
    val badgeBgColor: Color,
    val glowColor: Color
)

fun computeAstroMeterMeta(score: Int): AstroMeterMeta {
    val clamped = score.coerceIn(0, 100)
    return when {
        clamped >= 80 -> AstroMeterMeta(
            statusResId = R.string.meter_excellent,
            starCount = 5,
            badgeColor = AuspiciousGreen,
            badgeBgColor = AuspiciousGreen.copy(alpha = 0.14f),
            glowColor = AuspiciousGreen.copy(alpha = 0.45f)
        )
        clamped >= 60 -> AstroMeterMeta(
            statusResId = R.string.meter_favorable,
            starCount = 4,
            badgeColor = SaffronDark,
            badgeBgColor = SaffronAccent.copy(alpha = 0.24f),
            glowColor = SaffronAccent.copy(alpha = 0.55f)
        )
        clamped >= 40 -> AstroMeterMeta(
            statusResId = R.string.meter_growing,
            starCount = if (clamped >= 50) 3 else 2,
            badgeColor = DeepIndigo,
            badgeBgColor = DeepIndigo.copy(alpha = 0.10f),
            glowColor = DeepIndigo.copy(alpha = 0.30f)
        )
        else -> AstroMeterMeta(
            statusResId = R.string.meter_needs_focus,
            starCount = if (clamped >= 25) 2 else 1,
            badgeColor = MutedRed,
            badgeBgColor = MutedRed.copy(alpha = 0.12f),
            glowColor = MutedRed.copy(alpha = 0.35f)
        )
    }
}

@Composable
fun AstroMeter(
    label: String,
    glyph: String,
    score: Int,
    summary: String,
    modifier: Modifier = Modifier,
    badgeTestTag: String? = null
) {
    val meta = computeAstroMeterMeta(score)
    val fraction = (score / 100f).coerceIn(0.06f, 1f)
    val statusText = stringResource(meta.statusResId)

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    modifier = Modifier
                        .size(26.dp)
                        .background(SaffronLight.copy(alpha = 0.45f), CircleShape)
                        .border(1.dp, SaffronAccent, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = glyph,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = DeepIndigo
                    )
                }
                Spacer(Modifier.width(8.dp))
                Text(
                    text = label,
                    fontSize = 13.5.sp,
                    fontWeight = FontWeight.Bold,
                    color = DeepIndigo,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(Modifier.width(8.dp))

            Surface(
                shape = RoundedCornerShape(20.dp),
                color = meta.badgeBgColor,
                border = BorderStroke(1.dp, meta.badgeColor.copy(alpha = 0.45f)),
                modifier = Modifier
                    .shadow(
                        elevation = 4.dp,
                        shape = RoundedCornerShape(20.dp),
                        ambientColor = meta.glowColor,
                        spotColor = meta.glowColor
                    )
                    .then(if (badgeTestTag != null) Modifier.testTag(badgeTestTag) else Modifier)
            ) {
                Text(
                    text = statusText,
                    fontSize = 11.5.sp,
                    fontWeight = FontWeight.Bold,
                    color = meta.badgeColor,
                    maxLines = 1,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                )
            }
        }

        // Gradient Progress Track (Light Saffron -> Saffron Accent -> Deep Gold)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(14.dp)
                .shadow(
                    elevation = 4.dp,
                    shape = RoundedCornerShape(7.dp),
                    ambientColor = SaffronAccent.copy(alpha = 0.35f),
                    spotColor = SaffronDark.copy(alpha = 0.30f)
                )
                .clip(RoundedCornerShape(7.dp))
                .background(CreamSurfaceVariant)
                .border(0.75.dp, CreamBorder, RoundedCornerShape(7.dp))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(fraction)
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(7.dp))
                    .background(
                        Brush.horizontalGradient(
                            colors = listOf(SaffronLight, SaffronAccent, SaffronDark)
                        )
                    )
            )
        }

        // 1 to 5 Golden Stars + 1-line empowering summary below the bar
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.Top,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(top = 1.dp)
            ) {
                for (i in 1..5) {
                    Icon(
                        imageVector = if (i <= meta.starCount) Icons.Filled.Star else Icons.Outlined.StarOutline,
                        contentDescription = null,
                        tint = if (i <= meta.starCount) SaffronDark else CreamBorder,
                        modifier = Modifier.size(14.dp)
                    )
                }
            }

            Text(
                text = summary,
                fontSize = 12.sp,
                lineHeight = 17.sp,
                color = TextPrimary,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
fun AstroStatusPill(
    score: Int,
    modifier: Modifier = Modifier,
    showStars: Boolean = false
) {
    val meta = computeAstroMeterMeta(score)
    val statusText = stringResource(meta.statusResId)
    val stars = "★".repeat(meta.starCount)

    Surface(
        shape = RoundedCornerShape(16.dp),
        color = SaffronAccent,
        modifier = modifier.shadow(
            elevation = 6.dp,
            shape = RoundedCornerShape(16.dp),
            ambientColor = SaffronAccent.copy(alpha = 0.55f),
            spotColor = SaffronDark.copy(alpha = 0.55f)
        )
    ) {
        Text(
            text = if (showStars) "$statusText • $stars" else statusText,
            fontSize = 11.5.sp,
            fontWeight = FontWeight.Bold,
            color = DeepIndigo,
            maxLines = 1,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
        )
    }
}

@Composable
fun SacredSectionDivider(
    symbol: String = "ॐ",
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        HorizontalDivider(
            modifier = Modifier.weight(1f),
            thickness = 1.dp,
            color = SaffronAccent.copy(alpha = 0.45f)
        )
        Text(
            text = symbol,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = SaffronDark
        )
        HorizontalDivider(
            modifier = Modifier.weight(1f),
            thickness = 1.dp,
            color = SaffronAccent.copy(alpha = 0.45f)
        )
    }
}
