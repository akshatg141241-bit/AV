package com.aryaleelastudios.astroveda.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.HealthAndSafety
import androidx.compose.material.icons.filled.HomeWork
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Spa
import androidx.compose.material.icons.filled.Timeline
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material.icons.filled.WorkOutline
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aryaleelastudios.astroveda.R
import com.aryaleelastudios.astroveda.data.local.PersonEntity
import com.aryaleelastudios.astroveda.data.model.ChartStyle
import com.aryaleelastudios.astroveda.data.model.KundaliData
import com.aryaleelastudios.astroveda.data.model.PredictionPeriod
import com.aryaleelastudios.astroveda.data.model.ReadingCategory
import com.aryaleelastudios.astroveda.ui.components.AstroMeter
import com.aryaleelastudios.astroveda.ui.components.DivisionalChartCanvas
import com.aryaleelastudios.astroveda.ui.components.KundaliChartCanvas
import com.aryaleelastudios.astroveda.ui.components.clearFocusOnTap
import com.aryaleelastudios.astroveda.ui.components.computeAstroMeterMeta
import com.aryaleelastudios.astroveda.ui.theme.AuspiciousGreen
import com.aryaleelastudios.astroveda.ui.theme.CreamBackground
import com.aryaleelastudios.astroveda.ui.theme.CreamBorder
import com.aryaleelastudios.astroveda.ui.theme.CreamSurface
import com.aryaleelastudios.astroveda.ui.theme.DeepIndigo
import com.aryaleelastudios.astroveda.ui.theme.MidnightNavy
import com.aryaleelastudios.astroveda.ui.theme.MutedRed
import com.aryaleelastudios.astroveda.ui.theme.SaffronAccent
import com.aryaleelastudios.astroveda.ui.theme.SaffronDark
import com.aryaleelastudios.astroveda.ui.theme.SaffronLight
import com.aryaleelastudios.astroveda.ui.theme.TextMuted
import com.aryaleelastudios.astroveda.ui.theme.TextPrimary
import com.aryaleelastudios.astroveda.ui.theme.astroPrimaryButtonColors
import com.aryaleelastudios.astroveda.ui.theme.astroSecondaryButtonBorder
import com.aryaleelastudios.astroveda.ui.theme.astroSecondaryButtonColors

@Composable
fun KundaliScreen(
    person: PersonEntity?,
    kundali: KundaliData?,
    chartStyle: ChartStyle,
    onChartStyleChanged: (ChartStyle) -> Unit,
    onOpenCategory: (ReadingCategory) -> Unit,
    onOpenPredictions: (PredictionPeriod) -> Unit,
    onNavigateToAddPerson: () -> Unit,
    onEditPerson: (PersonEntity) -> Unit,
    onDeletePerson: (PersonEntity) -> Unit,
    onExportPdf: () -> Unit,
    onShareJson: () -> Unit,
    onOpenYear2027: () -> Unit = {},
    listState: LazyListState = rememberSaveable(saver = LazyListState.Saver) { LazyListState() }
) {
    if (person == null || kundali == null) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clearFocusOnTap()
                .padding(24.dp)
                .testTag("kundali_empty_state"),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier.widthIn(max = 460.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(100.dp)
                        .background(SaffronAccent.copy(alpha = 0.2f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = "🕉", fontSize = 48.sp, color = DeepIndigo)
                }

                Spacer(Modifier.height(24.dp))

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

                Spacer(Modifier.height(28.dp))

                Button(
                    onClick = onNavigateToAddPerson,
                    colors = astroPrimaryButtonColors(),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .height(52.dp)
                        .testTag("add_first_person_cta")
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

    var expandedMahaIndex by remember { mutableStateOf<Int?>(0) }
    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current

    fun dismissKeyboard() {
        focusManager.clearFocus(force = true)
        keyboardController?.hide()
    }

    val moonSign = kundali.planets.firstOrNull { it.canonicalName == "Moon" }?.rashiName ?: kundali.lagna.rashiName

    Box(
        modifier = Modifier
            .fillMaxSize()
            .clearFocusOnTap(),
        contentAlignment = Alignment.TopCenter
    ) {
        LazyColumn(
            state = listState,
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = 600.dp)
                .testTag("kundali_content_list")
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(top = 12.dp, bottom = 32.dp)
        ) {
            // SECTION 1: Today's Prediction (Teaser Card)
            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = BorderStroke(1.dp, CreamBorder),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("section_today_prediction_card")
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.WbSunny, contentDescription = null, tint = SaffronDark, modifier = Modifier.size(20.dp))
                                Spacer(Modifier.width(8.dp))
                                Text(
                                    text = stringResource(R.string.todays_prediction_title),
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = DeepIndigo
                                )
                            }
                            Text(
                                text = moonSign,
                                fontWeight = FontWeight.SemiBold,
                                color = SaffronDark,
                                fontSize = 13.sp
                            )
                        }

                        Spacer(Modifier.height(10.dp))

                        Text(
                            text = kundali.dailyPrediction.text,
                            fontSize = 14.sp,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                            color = TextPrimary,
                            lineHeight = 20.sp
                        )

                        Spacer(Modifier.height(6.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End
                        ) {
                            TextButton(
                                onClick = {
                                    dismissKeyboard()
                                    onOpenPredictions(PredictionPeriod.DAILY)
                                },
                                modifier = Modifier
                                    .heightIn(min = 48.dp)
                                    .testTag("today_read_more_btn")
                            ) {
                                Text(
                                    text = stringResource(R.string.read_more_cta),
                                    color = DeepIndigo,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }

            // SECTION 1.5: Your 2027 Forecast Promo Card
            item {
                Card(
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = DeepIndigo),
                    border = BorderStroke(1.25.dp, SaffronAccent.copy(alpha = 0.7f)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            dismissKeyboard()
                            onOpenYear2027()
                        }
                        .testTag("section_year_2027_card")
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(DeepIndigo, MidnightNavy)
                                )
                            )
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
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
                                Icon(
                                    imageVector = Icons.Default.AutoAwesome,
                                    contentDescription = null,
                                    tint = SaffronAccent,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(Modifier.width(8.dp))
                                Text(
                                    text = stringResource(R.string.kundali_year_2027_title),
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = SaffronAccent
                            ) {
                                Text(
                                    text = stringResource(R.string.kundali_year_2027_badge),
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = DeepIndigo,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                )
                            }
                        }

                        Text(
                            text = stringResource(R.string.kundali_year_2027_subtitle),
                            style = MaterialTheme.typography.bodySmall,
                            color = CreamBackground.copy(alpha = 0.92f),
                            lineHeight = 19.sp
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End
                        ) {
                            Button(
                                onClick = {
                                    dismissKeyboard()
                                    onOpenYear2027()
                                },
                                colors = astroPrimaryButtonColors(),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier
                                    .heightIn(min = 48.dp)
                                    .testTag("year_2027_open_btn")
                            ) {
                                Text(
                                    text = stringResource(R.string.kundali_year_2027_cta),
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(Modifier.width(4.dp))
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }
            }

            // SECTION 2: Kundali Birth Chart (North / South Indian toggle)
            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = BorderStroke(1.dp, CreamBorder),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("section_chart_card")
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = stringResource(R.string.chart_section_title),
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = DeepIndigo
                                )
                                Text(
                                    text = stringResource(R.string.chart_lagna_label, kundali.lagna.rashiName, kundali.lagna.formattedDegree),
                                    fontSize = 12.sp,
                                    color = TextMuted
                                )
                            }

                            Row(
                                modifier = Modifier
                                    .background(CreamBackground, RoundedCornerShape(10.dp))
                                    .border(1.dp, CreamBorder, RoundedCornerShape(10.dp))
                                    .padding(2.dp)
                            ) {
                                ChartStyle.entries.forEach { style ->
                                    val isSelected = chartStyle == style
                                    Box(
                                        modifier = Modifier
                                            .heightIn(min = 40.dp)
                                            .background(
                                                if (isSelected) SaffronAccent else Color.Transparent,
                                                RoundedCornerShape(8.dp)
                                            )
                                            .clickable {
                                                dismissKeyboard()
                                                onChartStyleChanged(style)
                                            }
                                            .padding(horizontal = 10.dp, vertical = 8.dp)
                                            .testTag(
                                                if (style == ChartStyle.NORTH_INDIAN) "chart_toggle_north" else "chart_toggle_south"
                                            ),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = stringResource(style.shortLabelResId),
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = DeepIndigo
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(Modifier.height(14.dp))

                        KundaliChartCanvas(
                            kundali = kundali,
                            chartStyle = chartStyle
                        )
                    }
                }
            }

            // SECTION 3: Your Cosmic Blueprint (Dynamic 3-4 sentence Ascendant + Moon + Dasha overview — ABOVE Power Bars)
            if (kundali.cosmicBlueprint.isNotBlank()) {
                item {
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = CreamSurface),
                        border = BorderStroke(1.2.dp, SaffronAccent.copy(alpha = 0.65f)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("section_cosmic_blueprint")
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(
                                    Brush.verticalGradient(
                                        colors = listOf(
                                            SaffronLight.copy(alpha = 0.18f),
                                            CreamSurface
                                        )
                                    )
                                )
                                .padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(30.dp)
                                        .background(SaffronAccent.copy(alpha = 0.25f), CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.AutoAwesome,
                                        contentDescription = null,
                                        tint = SaffronDark,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                                Spacer(Modifier.width(10.dp))
                                Text(
                                    text = stringResource(R.string.section_cosmic_blueprint),
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = DeepIndigo
                                )
                            }
                            Text(
                                text = kundali.cosmicBlueprint,
                                fontSize = 13.5.sp,
                                lineHeight = 21.sp,
                                color = TextPrimary
                            )
                        }
                    }
                }
            }

            // SECTION 4: Cosmic Highlights / Power Bars (Career, Wealth, Love, Health using Saffron->Gold gradients & golden stars)
            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = BorderStroke(1.dp, CreamBorder),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("section_cosmic_highlights")
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Text(
                            text = stringResource(R.string.section_cosmic_highlights),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = DeepIndigo
                        )

                        kundali.cosmicHighlightBars.forEach { bar ->
                            AstroMeter(
                                label = bar.label,
                                glyph = bar.glyph,
                                score = bar.score,
                                summary = bar.summary,
                                badgeTestTag = "astro_badge_${bar.domainKey}",
                                modifier = Modifier.testTag("cosmic_highlight_bar_${bar.domainKey}")
                            )
                        }
                    }
                }
            }

            // SECTION 5: Your Reading (Clickable Category Grid opening Detailed Category Screens)
            item {
                Column(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.testTag("section_your_reading")
                ) {
                    Text(
                        text = stringResource(R.string.section_your_reading),
                        style = MaterialTheme.typography.titleLarge,
                        color = DeepIndigo
                    )

                    val careerScore = kundali.categoryDeepDives[ReadingCategory.CAREER]?.overallScore ?: 75
                    val wealthScore = kundali.categoryDeepDives[ReadingCategory.WEALTH]?.overallScore ?: 75
                    val marriageScore = kundali.categoryDeepDives[ReadingCategory.MARRIAGE]?.overallScore ?: 75
                    val healthScore = kundali.categoryDeepDives[ReadingCategory.HEALTH]?.overallScore ?: 75
                    val dharmaScore = kundali.categoryDeepDives[ReadingCategory.DHARMA]?.overallScore ?: 75

                    val careerMeta = computeAstroMeterMeta(careerScore)
                    val wealthMeta = computeAstroMeterMeta(wealthScore)
                    val marriageMeta = computeAstroMeterMeta(marriageScore)
                    val healthMeta = computeAstroMeterMeta(healthScore)
                    val dharmaMeta = computeAstroMeterMeta(dharmaScore)

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        ReadingCategoryGridButton(
                            title = stringResource(R.string.cat_career_title),
                            subtitle = "${stringResource(careerMeta.statusResId)} • ${"★".repeat(careerMeta.starCount)}",
                            icon = Icons.Default.WorkOutline,
                            onClick = {
                                dismissKeyboard()
                                onOpenCategory(ReadingCategory.CAREER)
                            },
                            testTag = "reading_btn_career",
                            modifier = Modifier.weight(1f)
                        )
                        ReadingCategoryGridButton(
                            title = stringResource(R.string.cat_wealth_title),
                            subtitle = "${stringResource(wealthMeta.statusResId)} • ${"★".repeat(wealthMeta.starCount)}",
                            icon = Icons.Default.AccountBalanceWallet,
                            onClick = {
                                dismissKeyboard()
                                onOpenCategory(ReadingCategory.WEALTH)
                            },
                            testTag = "reading_btn_wealth",
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        ReadingCategoryGridButton(
                            title = stringResource(R.string.cat_marriage_title),
                            subtitle = "${stringResource(marriageMeta.statusResId)} • ${"★".repeat(marriageMeta.starCount)}",
                            icon = Icons.Default.FavoriteBorder,
                            onClick = {
                                dismissKeyboard()
                                onOpenCategory(ReadingCategory.MARRIAGE)
                            },
                            testTag = "reading_btn_marriage",
                            modifier = Modifier.weight(1f)
                        )
                        ReadingCategoryGridButton(
                            title = stringResource(R.string.cat_health_title),
                            subtitle = "${stringResource(healthMeta.statusResId)} • ${"★".repeat(healthMeta.starCount)}",
                            icon = Icons.Default.HealthAndSafety,
                            onClick = {
                                dismissKeyboard()
                                onOpenCategory(ReadingCategory.HEALTH)
                            },
                            testTag = "reading_btn_health",
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        ReadingCategoryGridButton(
                            title = stringResource(R.string.reading_dharma),
                            subtitle = "${stringResource(dharmaMeta.statusResId)} • ${"★".repeat(dharmaMeta.starCount)}",
                            icon = Icons.Default.AutoAwesome,
                            onClick = {
                                dismissKeyboard()
                                onOpenCategory(ReadingCategory.DHARMA)
                            },
                            testTag = "reading_btn_dharma",
                            modifier = Modifier.weight(1f)
                        )
                        Spacer(Modifier.weight(1f))
                    }
                }
            }

            // SECTION 6: Detailed Astrological Charts (Collapsible: Planets, Bhava, Dasha, Panchang, Yogas, D9/D10/D7 full-size charts)
            item {
                Text(
                    text = stringResource(R.string.section_detailed_charts),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = DeepIndigo,
                    modifier = Modifier.testTag("section_detailed_charts")
                )
            }

            // Planets Table
            item {
                CollapsibleCard(
                    title = stringResource(R.string.detail_planets_title),
                    icon = Icons.Default.Public
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(CreamBackground)
                                .padding(6.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(stringResource(R.string.col_planet), fontWeight = FontWeight.Bold, fontSize = 11.sp, modifier = Modifier.weight(1.2f))
                            Text(stringResource(R.string.col_sign), fontWeight = FontWeight.Bold, fontSize = 11.sp, modifier = Modifier.weight(1.2f))
                            Text(stringResource(R.string.col_deg), fontWeight = FontWeight.Bold, fontSize = 11.sp, modifier = Modifier.weight(1.2f))
                            Text(stringResource(R.string.col_nakshatra), fontWeight = FontWeight.Bold, fontSize = 11.sp, modifier = Modifier.weight(1.4f))
                        }

                        kundali.planets.forEach { p ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 6.dp, vertical = 3.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "${p.glyph} ${p.name}${if (p.isRetrograde) "[R]" else ""}",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = DeepIndigo,
                                    modifier = Modifier.weight(1.2f)
                                )
                                Text(
                                    text = p.rashiName,
                                    fontSize = 12.sp,
                                    color = TextPrimary,
                                    modifier = Modifier.weight(1.2f)
                                )
                                Text(
                                    text = p.formattedDegree,
                                    fontSize = 11.sp,
                                    color = TextMuted,
                                    modifier = Modifier.weight(1.2f)
                                )
                                Text(
                                    text = "${p.nakshatraName} (${p.pada})",
                                    fontSize = 11.sp,
                                    color = SaffronDark,
                                    modifier = Modifier.weight(1.4f)
                                )
                            }
                        }
                    }
                }
            }

            // Bhava Cusps
            item {
                CollapsibleCard(
                    title = stringResource(R.string.detail_bhava_title),
                    icon = Icons.Default.HomeWork
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        kundali.houses.forEach { h ->
                            DetailRow(
                                label = stringResource(R.string.house_label, h.houseNumber),
                                value = "${h.rashiName} (${h.formattedDegree})"
                            )
                        }
                    }
                }
            }

            // Vimshottari Dasha (Maha -> Antar)
            item {
                CollapsibleCard(
                    title = stringResource(R.string.detail_dasha_title),
                    icon = Icons.Default.Timeline
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        kundali.vimshottariDasha.forEachIndexed { idx, dasha ->
                            val isSubExpanded = expandedMahaIndex == idx
                            Card(
                                shape = RoundedCornerShape(10.dp),
                                colors = CardDefaults.cardColors(containerColor = CreamBackground),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        expandedMahaIndex = if (isSubExpanded) null else idx
                                    }
                            ) {
                                Column(modifier = Modifier.padding(10.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = stringResource(R.string.maha_dasha_suffix, dasha.planet),
                                            fontWeight = FontWeight.Bold,
                                            color = DeepIndigo,
                                            fontSize = 13.sp
                                        )
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text(
                                                text = "${dasha.startDate} – ${dasha.endDate}",
                                                fontSize = 11.sp,
                                                color = TextMuted
                                            )
                                            Spacer(Modifier.width(4.dp))
                                            Icon(
                                                imageVector = if (isSubExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                                                contentDescription = null,
                                                tint = DeepIndigo,
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }
                                    }

                                    if (isSubExpanded && dasha.subPeriods.isNotEmpty()) {
                                        Spacer(Modifier.height(6.dp))
                                        HorizontalDivider(color = CreamBorder)
                                        Spacer(Modifier.height(6.dp))
                                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                            dasha.subPeriods.forEach { antar ->
                                                Row(
                                                    modifier = Modifier.fillMaxWidth(),
                                                    horizontalArrangement = Arrangement.SpaceBetween
                                                ) {
                                                    Text(
                                                        text = "• ${dasha.planet} / ${antar.planet}",
                                                        fontSize = 11.sp,
                                                        color = SaffronDark,
                                                        fontWeight = FontWeight.Medium
                                                    )
                                                    Text(
                                                        text = "${antar.startDate} – ${antar.endDate}",
                                                        fontSize = 10.sp,
                                                        color = TextMuted
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Panchang at Birth
            item {
                CollapsibleCard(
                    title = stringResource(R.string.detail_panchang_title),
                    icon = Icons.Default.CalendarToday
                ) {
                    val p = kundali.panchang
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        DetailRow(stringResource(R.string.panchang_tithi), "${p.tithi} (${p.paksha})")
                        DetailRow(stringResource(R.string.panchang_vara), p.vara)
                        DetailRow(stringResource(R.string.panchang_nakshatra), p.nakshatra)
                        DetailRow(stringResource(R.string.panchang_yoga), p.yoga)
                        DetailRow(stringResource(R.string.panchang_karana), p.karana)
                        DetailRow(stringResource(R.string.panchang_sun_times), "${p.sunrise} / ${p.sunset}")
                    }
                }
            }

            // Yogas & Doshas
            item {
                CollapsibleCard(
                    title = stringResource(R.string.detail_yogas_doshas_title),
                    icon = Icons.Default.Shield
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        kundali.yogasAndDoshas.forEach { yd ->
                            Card(
                                shape = RoundedCornerShape(10.dp),
                                colors = CardDefaults.cardColors(containerColor = CreamBackground),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(10.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = yd.name,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 13.sp,
                                            color = DeepIndigo
                                        )
                                        Text(
                                            text = yd.statusText,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 11.sp,
                                            color = if (yd.isPresent) SaffronDark else AuspiciousGreen
                                        )
                                    }
                                    Spacer(Modifier.height(4.dp))
                                    Text(
                                        text = yd.description,
                                        fontSize = 12.sp,
                                        color = TextPrimary,
                                        lineHeight = 17.sp
                                    )
                                    Spacer(Modifier.height(4.dp))
                                    Text(
                                        text = stringResource(R.string.remedy_prefix, yd.remedy),
                                        fontSize = 11.sp,
                                        color = DeepIndigo,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Full-Size Divisional Charts (D9 / D10 / D7)
            item {
                CollapsibleCard(
                    title = stringResource(R.string.detail_divisional_title),
                    icon = Icons.Default.GridView
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        kundali.divisionalCharts.forEach { div ->
                            Card(
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(containerColor = CreamBackground),
                                border = BorderStroke(1.dp, CreamBorder),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(
                                    modifier = Modifier.padding(12.dp),
                                    verticalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    Column {
                                        Text(
                                            text = div.title,
                                            style = MaterialTheme.typography.titleSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = DeepIndigo
                                        )
                                        Text(
                                            text = div.description,
                                            fontSize = 11.5.sp,
                                            color = TextMuted,
                                            lineHeight = 16.sp
                                        )
                                    }

                                    DivisionalChartCanvas(
                                        chartCode = div.code,
                                        kundali = kundali,
                                        chartStyle = chartStyle
                                    )

                                    Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                                        div.placements.chunked(2).forEach { chunk ->
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween
                                            ) {
                                                chunk.forEach { p ->
                                                    Text(
                                                        text = "${p.planet}: ${p.signName}",
                                                        fontSize = 11.sp,
                                                        color = DeepIndigo,
                                                        modifier = Modifier.weight(1f)
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // SECTION 8: Actions & Export
            item {
                Text(
                    text = stringResource(R.string.section_actions),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = DeepIndigo,
                    modifier = Modifier.testTag("section_actions")
                )
            }

            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = BorderStroke(1.dp, CreamBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = onExportPdf,
                            colors = astroPrimaryButtonColors(),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                                .testTag("action_export_pdf_btn")
                        ) {
                            Icon(Icons.Default.PictureAsPdf, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(8.dp))
                            Text(stringResource(R.string.action_export_pdf), fontWeight = FontWeight.Bold)
                        }

                        OutlinedButton(
                            onClick = onShareJson,
                            colors = astroSecondaryButtonColors(),
                            border = astroSecondaryButtonBorder(),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                                .testTag("action_share_json_btn")
                        ) {
                            Icon(Icons.Default.Share, contentDescription = null, tint = DeepIndigo, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(8.dp))
                            Text(
                                text = stringResource(R.string.action_share_json),
                                color = DeepIndigo,
                                fontWeight = FontWeight.SemiBold
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedButton(
                                onClick = { onEditPerson(person) },
                                colors = astroSecondaryButtonColors(),
                                border = astroSecondaryButtonBorder(),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(48.dp)
                                    .testTag("action_edit_person_btn")
                            ) {
                                Icon(Icons.Default.Edit, contentDescription = null, tint = DeepIndigo, modifier = Modifier.size(16.dp))
                                Spacer(Modifier.width(6.dp))
                                Text(
                                    text = stringResource(R.string.action_edit),
                                    color = DeepIndigo,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }

                            OutlinedButton(
                                onClick = { onDeletePerson(person) },
                                shape = RoundedCornerShape(12.dp),
                                border = BorderStroke(1.25.dp, MutedRed),
                                colors = ButtonDefaults.outlinedButtonColors(
                                    containerColor = Color.Transparent,
                                    contentColor = MutedRed
                                ),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(48.dp)
                                    .testTag("action_delete_person_btn")
                            ) {
                                Icon(Icons.Default.Delete, contentDescription = null, tint = MutedRed, modifier = Modifier.size(16.dp))
                                Spacer(Modifier.width(6.dp))
                                Text(
                                    text = stringResource(R.string.action_delete),
                                    color = MutedRed,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }
                }
            }

            // SECTION 9: Your Daily Remedies (All 5 Domains with Mantra, Why [Chart Ref], How)
            if (kundali.dailyRemedies.isNotEmpty()) {
                item {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.testTag("section_daily_remedies")
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(30.dp)
                                    .background(SaffronAccent.copy(alpha = 0.25f), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Spa,
                                    contentDescription = null,
                                    tint = SaffronDark,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            Spacer(Modifier.width(10.dp))
                            Text(
                                text = stringResource(R.string.section_daily_remedies),
                                style = MaterialTheme.typography.titleLarge,
                                color = DeepIndigo
                            )
                        }

                        kundali.dailyRemedies.forEach { remedyCard ->
                            Card(
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(containerColor = Color.White),
                                border = BorderStroke(1.dp, CreamBorder),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("daily_remedy_card_${remedyCard.category.name.lowercase(java.util.Locale.US)}")
                            ) {
                                Column(
                                    modifier = Modifier.padding(16.dp),
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Text(
                                        text = remedyCard.categoryLabel,
                                        style = MaterialTheme.typography.titleMedium,
                                        color = DeepIndigo
                                    )

                                    Surface(
                                        shape = RoundedCornerShape(10.dp),
                                        color = CreamSurface,
                                        border = BorderStroke(1.dp, SaffronAccent.copy(alpha = 0.55f)),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Column(modifier = Modifier.padding(10.dp)) {
                                            Text(
                                                text = stringResource(R.string.remedy_label_mantra),
                                                style = MaterialTheme.typography.labelSmall,
                                                fontWeight = FontWeight.Bold,
                                                color = SaffronDark
                                            )
                                            Spacer(Modifier.height(2.dp))
                                            Text(
                                                text = remedyCard.mantra,
                                                style = MaterialTheme.typography.bodyMedium,
                                                fontWeight = FontWeight.SemiBold,
                                                color = DeepIndigo
                                            )
                                        }
                                    }

                                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                        Text(
                                            text = stringResource(R.string.remedy_label_why),
                                            style = MaterialTheme.typography.labelMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = DeepIndigo
                                        )
                                        Text(
                                            text = remedyCard.why,
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = TextPrimary
                                        )
                                    }

                                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                        Text(
                                            text = stringResource(R.string.remedy_label_how),
                                            style = MaterialTheme.typography.labelMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = AuspiciousGreen
                                        )
                                        Text(
                                            text = remedyCard.how,
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = TextPrimary
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Disclaimer
            item {
                Text(
                    text = stringResource(R.string.disclaimer_text),
                    style = MaterialTheme.typography.bodySmall,
                    color = TextMuted,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp)
                )
            }
        }
    }
}

@Composable
private fun ReadingCategoryGridButton(
    title: String,
    subtitle: String,
    icon: ImageVector,
    onClick: () -> Unit,
    testTag: String,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, CreamBorder),
        modifier = modifier
            .heightIn(min = 116.dp)
            .clickable(onClick = onClick)
            .testTag(testTag)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .background(SaffronAccent.copy(alpha = 0.22f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = SaffronDark,
                        modifier = Modifier.size(18.dp)
                    )
                }
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                    contentDescription = null,
                    tint = DeepIndigo,
                    modifier = Modifier.size(18.dp)
                )
            }

            Spacer(Modifier.height(10.dp))

            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = DeepIndigo,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(Modifier.height(3.dp))

            Text(
                text = subtitle,
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
                color = SaffronDark,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
fun CollapsibleCard(
    title: String,
    icon: ImageVector,
    initiallyExpanded: Boolean = false,
    content: @Composable () -> Unit
) {
    var isExpanded by remember { mutableStateOf(initiallyExpanded) }

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, CreamBorder),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 48.dp)
                    .clickable { isExpanded = !isExpanded },
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(icon, contentDescription = null, tint = SaffronDark, modifier = Modifier.size(20.dp))
                    Spacer(Modifier.width(10.dp))
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = DeepIndigo
                    )
                }
                Icon(
                    imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = if (isExpanded) stringResource(R.string.cd_collapse) else stringResource(R.string.cd_expand),
                    tint = DeepIndigo
                )
            }

            AnimatedVisibility(visible = isExpanded) {
                Column(modifier = Modifier.padding(top = 8.dp)) {
                    content()
                }
            }
        }
    }
}

@Composable
private fun DetailRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, fontSize = 12.sp, color = TextMuted)
        Text(value, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = DeepIndigo)
    }
}
