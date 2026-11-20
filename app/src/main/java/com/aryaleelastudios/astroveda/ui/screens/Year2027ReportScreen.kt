package com.aryaleelastudios.astroveda.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.Spa
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Timeline
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
import com.aryaleelastudios.astroveda.data.local.PersonEntity
import com.aryaleelastudios.astroveda.data.model.ChartStyle
import com.aryaleelastudios.astroveda.data.model.KundaliData
import com.aryaleelastudios.astroveda.data.model.MonthForecast
import com.aryaleelastudios.astroveda.data.model.YearCategoryDeepDive
import com.aryaleelastudios.astroveda.data.model.YearForecast
import com.aryaleelastudios.astroveda.ui.components.AstroMeter
import com.aryaleelastudios.astroveda.ui.components.AstroStatusPill
import com.aryaleelastudios.astroveda.ui.components.DivisionalChartCanvas
import com.aryaleelastudios.astroveda.ui.components.KundaliChartCanvas
import com.aryaleelastudios.astroveda.ui.components.SacredSectionDivider
import com.aryaleelastudios.astroveda.ui.components.clearFocusOnTap
import com.aryaleelastudios.astroveda.ui.theme.AuspiciousGreen
import com.aryaleelastudios.astroveda.ui.theme.CreamBackground
import com.aryaleelastudios.astroveda.ui.theme.CreamBorder
import com.aryaleelastudios.astroveda.ui.theme.CreamSurface
import com.aryaleelastudios.astroveda.ui.theme.CreamSurfaceVariant
import com.aryaleelastudios.astroveda.ui.theme.DeepIndigo
import com.aryaleelastudios.astroveda.ui.theme.IndigoSurface
import com.aryaleelastudios.astroveda.ui.theme.MidnightNavy
import com.aryaleelastudios.astroveda.ui.theme.PaleCreamChipBg
import com.aryaleelastudios.astroveda.ui.theme.SaffronAccent
import com.aryaleelastudios.astroveda.ui.theme.SaffronDark
import com.aryaleelastudios.astroveda.ui.theme.SaffronLight
import com.aryaleelastudios.astroveda.ui.theme.SubtleCreamBorder
import com.aryaleelastudios.astroveda.ui.theme.TextMuted
import com.aryaleelastudios.astroveda.ui.theme.TextPrimary
import com.aryaleelastudios.astroveda.ui.theme.astroFilterChipBorder
import com.aryaleelastudios.astroveda.ui.theme.astroFilterChipColors
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun Year2027ReportScreen(
    person: PersonEntity,
    kundali: KundaliData,
    forecast: YearForecast,
    chartStyle: ChartStyle,
    onNavigateBack: () -> Unit
) {
    BackHandler { onNavigateBack() }

    val listState = rememberLazyListState()
    var selectedAppendixChart by rememberSaveable { mutableStateOf("D1") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = stringResource(R.string.year_2027_screen_title),
                            style = MaterialTheme.typography.titleLarge,
                            color = DeepIndigo,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = person.name,
                            style = MaterialTheme.typography.labelMedium,
                            color = SaffronDark,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = onNavigateBack,
                        modifier = Modifier.testTag("year_2027_back_btn")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.cd_back),
                            tint = DeepIndigo
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = CreamBackground)
            )
        },
        containerColor = CreamBackground,
        modifier = Modifier.testTag("year_2027_screen")
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clearFocusOnTap()
                .padding(padding),
            contentAlignment = Alignment.TopCenter
        ) {
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 600.dp)
                    .testTag("year_2027_list"),
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 36.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // 1. HERO CARD
                item {
                    Card(
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = DeepIndigo),
                        border = BorderStroke(1.25.dp, SaffronAccent.copy(alpha = 0.7f)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .shadow(
                                elevation = 8.dp,
                                shape = RoundedCornerShape(20.dp),
                                ambientColor = DeepIndigo.copy(alpha = 0.35f),
                                spotColor = MidnightNavy.copy(alpha = 0.45f)
                            )
                            .testTag("year_2027_hero_card")
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(
                                    Brush.verticalGradient(
                                        colors = listOf(DeepIndigo, MidnightNavy)
                                    )
                                )
                                .padding(18.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = stringResource(R.string.year_2027_hero_label),
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = SaffronAccent
                                )
                                AstroStatusPill(
                                    score = forecast.overallScore,
                                    showStars = true,
                                    modifier = Modifier.testTag("year_2027_hero_status_pill")
                                )
                            }

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(48.dp)
                                        .shadow(
                                            elevation = 6.dp,
                                            shape = CircleShape,
                                            ambientColor = SaffronAccent.copy(alpha = 0.55f),
                                            spotColor = SaffronAccent.copy(alpha = 0.55f)
                                        )
                                        .background(
                                            Brush.radialGradient(
                                                colors = listOf(
                                                    SaffronAccent.copy(alpha = 0.35f),
                                                    Color.White.copy(alpha = 0.12f)
                                                )
                                            ),
                                            CircleShape
                                        )
                                        .border(1.25.dp, SaffronAccent.copy(alpha = 0.85f), CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = forecast.heroGlyph.ifBlank { "✦" },
                                        fontSize = 22.sp,
                                        color = SaffronLight,
                                        fontWeight = FontWeight.Bold
                                    )
                                }

                                Text(
                                    text = forecast.signatureHeadline,
                                    style = MaterialTheme.typography.headlineSmall,
                                    color = Color.White,
                                    modifier = Modifier
                                        .weight(1f)
                                        .testTag("year_2027_hero_headline")
                                )
                            }

                            if (forecast.heroSummary.isNotBlank()) {
                                Text(
                                    text = forecast.heroSummary,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = CreamBackground.copy(alpha = 0.94f),
                                    lineHeight = 21.sp,
                                    modifier = Modifier.testTag("year_2027_hero_summary")
                                )
                            }

                            val activeDashaText = buildString {
                                append(forecast.mahaLord)
                                if (forecast.antarLords.isNotEmpty()) {
                                    append(" (")
                                    append(forecast.antarLords.joinToString(", "))
                                    append(")")
                                }
                            }
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = Color.White.copy(alpha = 0.12f),
                                border = BorderStroke(1.dp, SaffronAccent.copy(alpha = 0.65f)),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("year_2027_dasha_badge")
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 9.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.AutoAwesome,
                                        contentDescription = null,
                                        tint = SaffronAccent,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Text(
                                        text = stringResource(R.string.year_2027_dasha_badge, activeDashaText),
                                        style = MaterialTheme.typography.labelMedium,
                                        color = SaffronLight
                                    )
                                }
                            }
                        }
                    }
                }

                item { SacredSectionDivider(symbol = "ॐ") }

                // 2. 2027 AT A GLANCE (5 ASTRO METERS)
                item {
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        border = BorderStroke(1.dp, CreamBorder),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("year_2027_at_a_glance_card")
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            Text(
                                text = stringResource(R.string.year_2027_at_a_glance),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = DeepIndigo
                            )
                            forecast.atAGlanceMeters.forEachIndexed { idx, meter ->
                                AstroMeter(
                                    label = meter.label,
                                    glyph = meter.glyph,
                                    score = meter.score,
                                    summary = meter.note,
                                    badgeTestTag = "year_2027_meter_$idx"
                                )
                            }
                        }
                    }
                }

                // 3. GOLDEN WINDOWS & EFFORT WINDOWS
                item {
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        border = BorderStroke(1.dp, CreamBorder),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("year_2027_windows_card")
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Star,
                                    contentDescription = null,
                                    tint = AuspiciousGreen,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(Modifier.width(8.dp))
                                Text(
                                    text = stringResource(R.string.year_2027_golden_windows),
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = DeepIndigo
                                )
                            }

                            forecast.goldenWindows.forEach { win ->
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = AuspiciousGreen.copy(alpha = 0.08f),
                                    border = BorderStroke(1.dp, AuspiciousGreen.copy(alpha = 0.4f)),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(
                                        modifier = Modifier.padding(12.dp),
                                        verticalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        Text(
                                            text = win.label,
                                            style = MaterialTheme.typography.labelLarge,
                                            fontWeight = FontWeight.Bold,
                                            color = AuspiciousGreen
                                        )
                                        Text(
                                            text = win.reason,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = TextPrimary,
                                            lineHeight = 18.sp
                                        )
                                    }
                                }
                            }

                            HorizontalDivider(color = SubtleCreamBorder)

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Tune,
                                    contentDescription = null,
                                    tint = SaffronDark,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(Modifier.width(8.dp))
                                Text(
                                    text = stringResource(R.string.year_2027_effort_windows),
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = DeepIndigo
                                )
                            }

                            forecast.effortWindows.forEach { win ->
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = PaleCreamChipBg,
                                    border = BorderStroke(1.dp, SaffronAccent.copy(alpha = 0.65f)),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(
                                        modifier = Modifier.padding(12.dp),
                                        verticalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        Text(
                                            text = win.label,
                                            style = MaterialTheme.typography.labelLarge,
                                            fontWeight = FontWeight.Bold,
                                            color = SaffronDark
                                        )
                                        Text(
                                            text = win.reason,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = TextPrimary,
                                            lineHeight = 18.sp
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                item { SacredSectionDivider(symbol = "✦") }

                // 3. 2027 CATEGORY DEEP-DIVES
                item {
                    Text(
                        text = stringResource(R.string.year_2027_deep_dives_title),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = DeepIndigo
                    )
                }

                itemsIndexed(
                    items = forecast.categoryDeepDives,
                    key = { _, item -> "dd_${item.category.name}" }
                ) { idx, deepDive ->
                    YearCategoryDeepDiveCard(
                        index = idx,
                        deepDive = deepDive,
                        kundali = kundali,
                        chartStyle = chartStyle
                    )
                }

                item { SacredSectionDivider(symbol = "ॐ") }

                // 6. MONTH-BY-MONTH 2027 TIMELINE (JAN - DEC)
                item {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.CalendarMonth,
                            contentDescription = null,
                            tint = SaffronDark,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text = stringResource(R.string.year_2027_monthly_title),
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = DeepIndigo
                        )
                    }
                }

                itemsIndexed(
                    items = forecast.monthlyForecasts,
                    key = { _, item -> "month_${item.monthIndex}" }
                ) { _, mf ->
                    MonthForecastCard(mf = mf)
                }

                item { SacredSectionDivider(symbol = "✦") }

                // 7. 2027 CHARTS & SIGN MATRIX APPENDIX
                item {
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        border = BorderStroke(1.dp, CreamBorder),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("year_2027_appendix_card")
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.GridView,
                                    contentDescription = null,
                                    tint = DeepIndigo,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(Modifier.width(8.dp))
                                Text(
                                    text = stringResource(R.string.year_2027_charts_appendix_title),
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = DeepIndigo
                                )
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                listOf("D1", "D9", "D10").forEach { code ->
                                    FilterChip(
                                        selected = selectedAppendixChart == code,
                                        onClick = { selectedAppendixChart = code },
                                        label = {
                                            Text(
                                                text = code,
                                                fontWeight = FontWeight.Bold
                                            )
                                        },
                                        colors = astroFilterChipColors(),
                                        border = astroFilterChipBorder(selected = selectedAppendixChart == code),
                                        modifier = Modifier
                                            .heightIn(min = 48.dp)
                                            .testTag("year_2027_chart_tab_${code.lowercase(Locale.US)}")
                                    )
                                }
                            }

                            if (selectedAppendixChart == "D1") {
                                KundaliChartCanvas(
                                    kundali = kundali,
                                    chartStyle = chartStyle
                                )
                            } else {
                                DivisionalChartCanvas(
                                    chartCode = selectedAppendixChart,
                                    kundali = kundali,
                                    chartStyle = chartStyle
                                )
                            }

                            HorizontalDivider(color = SubtleCreamBorder)

                            Text(
                                text = stringResource(R.string.year_2027_sign_matrix_title),
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = DeepIndigo
                            )

                            val d9Map = kundali.divisionalCharts
                                .firstOrNull { it.code == "D9" }
                                ?.placements
                                ?.associateBy { it.canonicalPlanet }
                                ?: emptyMap()
                            val d10Map = kundali.divisionalCharts
                                .firstOrNull { it.code == "D10" }
                                ?.placements
                                ?.associateBy { it.canonicalPlanet }
                                ?: emptyMap()

                            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(IndigoSurface, RoundedCornerShape(8.dp))
                                        .padding(horizontal = 10.dp, vertical = 7.dp)
                                ) {
                                    Text(
                                        text = stringResource(R.string.col_planet),
                                        fontSize = 11.5.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = DeepIndigo,
                                        modifier = Modifier.weight(1.1f)
                                    )
                                    Text(
                                        text = "D1",
                                        fontSize = 11.5.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = DeepIndigo,
                                        modifier = Modifier.weight(1f)
                                    )
                                    Text(
                                        text = "D9",
                                        fontSize = 11.5.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = DeepIndigo,
                                        modifier = Modifier.weight(1f)
                                    )
                                    Text(
                                        text = "D10",
                                        fontSize = 11.5.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = DeepIndigo,
                                        modifier = Modifier.weight(1f)
                                    )
                                }

                                kundali.planets.forEachIndexed { rowIdx, planet ->
                                    val rowBg = if (rowIdx % 2 == 0) CreamBackground else Color.White
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .background(rowBg, RoundedCornerShape(6.dp))
                                            .padding(horizontal = 10.dp, vertical = 6.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = "${planet.glyph} ${planet.name}",
                                            fontSize = 11.5.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = DeepIndigo,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis,
                                            modifier = Modifier.weight(1.1f)
                                        )
                                        Text(
                                            text = planet.rashiName,
                                            fontSize = 11.sp,
                                            color = TextPrimary,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis,
                                            modifier = Modifier.weight(1f)
                                        )
                                        Text(
                                            text = d9Map[planet.canonicalName]?.signName ?: "-",
                                            fontSize = 11.sp,
                                            color = TextPrimary,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis,
                                            modifier = Modifier.weight(1f)
                                        )
                                        Text(
                                            text = d10Map[planet.canonicalName]?.signName ?: "-",
                                            fontSize = 11.sp,
                                            color = TextPrimary,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis,
                                            modifier = Modifier.weight(1f)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // 2027 VIMSHOTTARI SUB-PERIODS TIMELINE TABLE
                if (forecast.dashaTimeline2027.isNotEmpty()) {
                    item {
                        Card(
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            border = BorderStroke(1.dp, CreamBorder),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("year_2027_dasha_card")
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Timeline,
                                        contentDescription = null,
                                        tint = DeepIndigo,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(Modifier.width(8.dp))
                                    Text(
                                        text = stringResource(R.string.year_2027_dasha_timeline),
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = DeepIndigo
                                    )
                                }

                                forecast.dashaTimeline2027.forEach { dasha ->
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .background(CreamBackground, RoundedCornerShape(10.dp))
                                            .border(1.dp, SubtleCreamBorder, RoundedCornerShape(10.dp))
                                            .padding(horizontal = 12.dp, vertical = 10.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = dasha.planet,
                                            style = MaterialTheme.typography.titleSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = DeepIndigo,
                                            modifier = Modifier.weight(1f)
                                        )
                                        Text(
                                            text = "${dasha.startDate} – ${dasha.endDate}",
                                            style = MaterialTheme.typography.labelMedium,
                                            color = SaffronDark,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // 8. 2027 REMEDIES SUMMARY
                if (forecast.remediesSummary.isNotEmpty()) {
                    item {
                        Card(
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            border = BorderStroke(1.dp, CreamBorder),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("year_2027_remedies_card")
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(32.dp)
                                            .background(SaffronAccent.copy(alpha = 0.22f), CircleShape),
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
                                        text = stringResource(R.string.year_2027_remedies_title),
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = DeepIndigo
                                    )
                                }

                                forecast.remediesSummary.forEach { card ->
                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .background(CreamBackground, RoundedCornerShape(12.dp))
                                            .border(1.dp, SubtleCreamBorder, RoundedCornerShape(12.dp))
                                            .padding(12.dp),
                                        verticalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Text(
                                            text = card.categoryLabel,
                                            style = MaterialTheme.typography.titleSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = DeepIndigo
                                        )
                                        Text(
                                            text = "${stringResource(R.string.remedy_label_mantra)}: ${card.mantra}",
                                            style = MaterialTheme.typography.bodySmall,
                                            fontWeight = FontWeight.SemiBold,
                                            color = SaffronDark
                                        )
                                        Text(
                                            text = "${stringResource(R.string.remedy_label_why)}: ${card.why}",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = TextPrimary,
                                            lineHeight = 18.sp
                                        )
                                        Text(
                                            text = "${stringResource(R.string.remedy_label_how)}: ${card.how}",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = TextPrimary,
                                            lineHeight = 18.sp
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // 9. DISCLAIMER
                item {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = CreamSurfaceVariant.copy(alpha = 0.65f),
                        border = BorderStroke(1.dp, CreamBorder),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("year_2027_disclaimer")
                    ) {
                        Text(
                            text = forecast.disclaimer,
                            style = MaterialTheme.typography.bodySmall,
                            color = TextMuted,
                            lineHeight = 18.sp,
                            modifier = Modifier.padding(14.dp)
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun YearCategoryDeepDiveCard(
    index: Int,
    deepDive: YearCategoryDeepDive,
    kundali: KundaliData,
    chartStyle: ChartStyle
) {
    var showChart by rememberSaveable(deepDive.category.name) { mutableStateOf(true) }

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, CreamBorder),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("year_2027_deep_dive_$index")
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = deepDive.categoryLabel,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = DeepIndigo,
                    modifier = Modifier
                        .weight(1f)
                        .padding(end = 8.dp)
                )
                AstroStatusPill(score = deepDive.score, showStars = false)
            }

            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                deepDive.goldenWindows.forEach { win ->
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = AuspiciousGreen.copy(alpha = 0.12f),
                        border = BorderStroke(1.dp, AuspiciousGreen.copy(alpha = 0.45f))
                    ) {
                        Text(
                            text = stringResource(R.string.year_2027_golden_chip, win.label),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = AuspiciousGreen,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                        )
                    }
                }
                deepDive.effortWindows.forEach { win ->
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = PaleCreamChipBg,
                        border = BorderStroke(1.dp, SaffronAccent)
                    ) {
                        Text(
                            text = stringResource(R.string.year_2027_effort_chip, win.label),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = SaffronDark,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                        )
                    }
                }
            }

            deepDive.paragraphs.forEach { para ->
                Text(
                    text = para,
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextPrimary,
                    lineHeight = 21.sp
                )
            }

            if (deepDive.remedy2027.isNotBlank()) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.verticalGradient(listOf(CreamSurface, PaleCreamChipBg)),
                            RoundedCornerShape(12.dp)
                        )
                        .border(1.dp, SaffronAccent.copy(alpha = 0.65f), RoundedCornerShape(12.dp))
                        .padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = stringResource(R.string.year_2027_remedy_note_label),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = SaffronDark
                    )
                    Text(
                        text = deepDive.remedy2027,
                        style = MaterialTheme.typography.bodySmall,
                        color = DeepIndigo,
                        lineHeight = 18.sp
                    )
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 48.dp)
                    .background(CreamBackground, RoundedCornerShape(10.dp))
                    .border(1.dp, SubtleCreamBorder, RoundedCornerShape(10.dp))
                    .clickable { showChart = !showChart }
                    .padding(horizontal = 12.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.cat_divisional_chart_title, deepDive.chartCode),
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = DeepIndigo
                )
                Icon(
                    imageVector = if (showChart) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = null,
                    tint = SaffronDark
                )
            }

            AnimatedVisibility(visible = showChart) {
                if (deepDive.chartCode == "D1") {
                    KundaliChartCanvas(
                        kundali = kundali,
                        chartStyle = chartStyle
                    )
                } else {
                    DivisionalChartCanvas(
                        chartCode = deepDive.chartCode,
                        kundali = kundali,
                        chartStyle = chartStyle
                    )
                }
            }
        }
    }
}

@Composable
private fun MonthForecastCard(mf: MonthForecast) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, CreamBorder),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("year_2027_month_${mf.monthIndex}")
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = mf.monthName,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = DeepIndigo
                )
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = IndigoSurface,
                    border = BorderStroke(1.dp, DeepIndigo.copy(alpha = 0.25f))
                ) {
                    Text(
                        text = mf.dashaSubPeriod,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = DeepIndigo,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Text(
                text = stringResource(R.string.year_2027_moon_transit_prefix, mf.moonTransitFlavor),
                style = MaterialTheme.typography.labelMedium,
                color = SaffronDark,
                fontWeight = FontWeight.SemiBold
            )

            Text(
                text = mf.prediction,
                style = MaterialTheme.typography.bodyMedium,
                color = TextPrimary,
                lineHeight = 20.sp
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = AuspiciousGreen.copy(alpha = 0.10f),
                    border = BorderStroke(1.dp, AuspiciousGreen.copy(alpha = 0.35f)),
                    modifier = Modifier.weight(1f)
                ) {
                    Column(modifier = Modifier.padding(8.dp)) {
                        Text(
                            text = stringResource(R.string.year_2027_lucky_focus),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = AuspiciousGreen
                        )
                        Text(
                            text = mf.luckyFocus,
                            style = MaterialTheme.typography.bodySmall,
                            color = TextPrimary
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = PaleCreamChipBg,
                    border = BorderStroke(1.dp, SaffronAccent.copy(alpha = 0.55f)),
                    modifier = Modifier.weight(1f)
                ) {
                    Column(modifier = Modifier.padding(8.dp)) {
                        Text(
                            text = stringResource(R.string.year_2027_effort_focus),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = SaffronDark
                        )
                        Text(
                            text = mf.effortFocus,
                            style = MaterialTheme.typography.bodySmall,
                            color = TextPrimary
                        )
                    }
                }
            }
        }
    }
}
