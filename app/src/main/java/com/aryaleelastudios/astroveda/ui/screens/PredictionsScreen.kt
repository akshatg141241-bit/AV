package com.aryaleelastudios.astroveda.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Tag
import androidx.compose.material.icons.filled.Today
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aryaleelastudios.astroveda.R
import com.aryaleelastudios.astroveda.data.local.PersonEntity
import com.aryaleelastudios.astroveda.data.model.KundaliData
import com.aryaleelastudios.astroveda.data.model.PredictionPeriod
import com.aryaleelastudios.astroveda.ui.components.clearFocusOnTap
import com.aryaleelastudios.astroveda.ui.theme.CreamBackground
import com.aryaleelastudios.astroveda.ui.theme.CreamBorder
import com.aryaleelastudios.astroveda.ui.theme.CreamSurfaceVariant
import com.aryaleelastudios.astroveda.ui.theme.DeepIndigo
import com.aryaleelastudios.astroveda.ui.theme.IndigoContainer
import com.aryaleelastudios.astroveda.ui.theme.SaffronAccent
import com.aryaleelastudios.astroveda.ui.theme.SaffronDark
import com.aryaleelastudios.astroveda.ui.theme.TextMuted
import com.aryaleelastudios.astroveda.ui.theme.TextPrimary
import com.aryaleelastudios.astroveda.ui.theme.astroPrimaryButtonColors
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun PredictionsScreen(
    person: PersonEntity?,
    kundali: KundaliData?,
    selectedPeriod: PredictionPeriod,
    onSelectPeriod: (PredictionPeriod) -> Unit,
    languageCode: String,
    onNavigateToAddPerson: () -> Unit
) {
    if (person == null || kundali == null) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clearFocusOnTap()
                .padding(24.dp)
                .testTag("predictions_empty_state"),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier.widthIn(max = 460.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(88.dp)
                        .background(SaffronAccent.copy(alpha = 0.2f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.WbSunny,
                        contentDescription = null,
                        tint = DeepIndigo,
                        modifier = Modifier.size(44.dp)
                    )
                }

                Spacer(Modifier.height(20.dp))

                Text(
                    text = stringResource(R.string.empty_kundali_title),
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = DeepIndigo,
                    textAlign = TextAlign.Center
                )

                Spacer(Modifier.height(8.dp))

                Text(
                    text = stringResource(R.string.empty_kundali_subtitle),
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextMuted,
                    textAlign = TextAlign.Center,
                    lineHeight = 20.sp
                )

                Spacer(Modifier.height(24.dp))

                Button(
                    onClick = onNavigateToAddPerson,
                    colors = astroPrimaryButtonColors(),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .height(52.dp)
                        .testTag("predictions_add_person_cta")
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(20.dp))
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = stringResource(R.string.cta_add_first_person),
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                }
            }
        }
        return
    }

    val locale = remember(languageCode) { Locale(languageCode) }
    val todayFormatted = remember(languageCode) {
        SimpleDateFormat("EEEE, dd MMMM yyyy", locale).format(Date())
    }
    val moonSign = kundali.planets.firstOrNull { it.canonicalName == "Moon" }?.rashiName ?: kundali.lagna.rashiName
    val currentSelection = kundali.predictionFor(selectedPeriod)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .clearFocusOnTap()
            .testTag("predictions_screen"),
        contentAlignment = Alignment.TopCenter
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = 600.dp)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Top Segmented Control: Daily | Monthly | Yearly | Life
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White, RoundedCornerShape(14.dp))
                    .border(1.dp, CreamBorder, RoundedCornerShape(14.dp))
                    .padding(4.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                PredictionPeriod.entries.forEach { period ->
                    val isSelected = selectedPeriod == period
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .heightIn(min = 44.dp)
                            .background(
                                if (isSelected) SaffronAccent else Color.Transparent,
                                RoundedCornerShape(10.dp)
                            )
                            .clickable { onSelectPeriod(period) }
                            .padding(horizontal = 6.dp, vertical = 10.dp)
                            .testTag("period_tab_${period.name.lowercase(Locale.US)}"),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = stringResource(period.labelResId),
                            fontSize = 13.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            color = DeepIndigo,
                            maxLines = 1
                        )
                    }
                }
            }

            // Hero Card
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = DeepIndigo),
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            brush = Brush.verticalGradient(
                                colors = listOf(DeepIndigo, IndigoContainer)
                            )
                        )
                        .padding(20.dp)
                ) {
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = person.name,
                                    style = MaterialTheme.typography.headlineSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                                Text(
                                    text = stringResource(R.string.prediction_sign_label, moonSign),
                                    fontSize = 14.sp,
                                    color = CreamBackground.copy(alpha = 0.9f)
                                )
                            }
                            Box(
                                modifier = Modifier
                                    .size(48.dp)
                                    .background(SaffronAccent, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(text = "☉", fontSize = 24.sp, color = DeepIndigo)
                            }
                        }

                        Spacer(Modifier.height(14.dp))
                        HorizontalDivider(color = Color.White.copy(alpha = 0.2f))
                        Spacer(Modifier.height(14.dp))

                        Text(
                            text = todayFormatted,
                            fontSize = 12.sp,
                            color = CreamBackground.copy(alpha = 0.85f),
                            fontWeight = FontWeight.Medium
                        )
                        Spacer(Modifier.height(6.dp))
                        Text(
                            text = "${stringResource(R.string.predictions_theme_header)}: ${currentSelection.theme}",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = Color.White
                        )
                    }
                }
            }

            // Prediction Body Card
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = BorderStroke(1.dp, CreamBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = SaffronDark, modifier = Modifier.size(22.dp))
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text = stringResource(R.string.prediction_guidance_title),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = DeepIndigo
                        )
                    }
                    Spacer(Modifier.height(12.dp))
                    Text(
                        text = currentSelection.text,
                        fontSize = 15.sp,
                        lineHeight = 23.sp,
                        color = TextPrimary,
                        modifier = Modifier.testTag("prediction_body_text")
                    )

                    Spacer(Modifier.height(14.dp))
                    HorizontalDivider(color = CreamBorder)
                    Spacer(Modifier.height(12.dp))

                    Row(verticalAlignment = Alignment.Top) {
                        Icon(Icons.Default.Lightbulb, contentDescription = null, tint = SaffronDark, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text = stringResource(R.string.predictions_action_tip),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            color = DeepIndigo
                        )
                    }
                }
            }

            // Lucky Elements
            Text(
                text = stringResource(R.string.prediction_lucky_title),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = DeepIndigo
            )

            val l = kundali.luckyElements
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                LuckyElementBox(
                    label = stringResource(R.string.lucky_number),
                    value = "${l.number}",
                    icon = Icons.Default.Tag,
                    modifier = Modifier.weight(1f)
                )
                LuckyElementBox(
                    label = stringResource(R.string.lucky_color),
                    value = l.color.split(",").firstOrNull()?.trim() ?: l.color,
                    icon = Icons.Default.Palette,
                    modifier = Modifier.weight(1f)
                )
                LuckyElementBox(
                    label = stringResource(R.string.lucky_day),
                    value = l.day.split(" ").firstOrNull() ?: l.day,
                    icon = Icons.Default.Today,
                    modifier = Modifier.weight(1f)
                )
            }

            // Disclaimer
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = CreamSurfaceVariant),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = stringResource(R.string.disclaimer_text),
                    fontSize = 12.sp,
                    color = TextMuted,
                    modifier = Modifier.padding(14.dp)
                )
            }

            Spacer(Modifier.height(12.dp))
        }
    }
}

@Composable
private fun LuckyElementBox(
    label: String,
    value: String,
    icon: ImageVector,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, CreamBorder),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(icon, contentDescription = null, tint = SaffronDark, modifier = Modifier.size(20.dp))
            Spacer(Modifier.height(6.dp))
            Text(label, fontSize = 11.sp, color = TextMuted)
            Spacer(Modifier.height(2.dp))
            Text(value, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = DeepIndigo, maxLines = 1)
        }
    }
}
