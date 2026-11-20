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
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.AutoStories
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.HealthAndSafety
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Spa
import androidx.compose.material.icons.filled.Timeline
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.WorkOutline
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aryaleelastudios.astroveda.R
import com.aryaleelastudios.astroveda.data.local.PersonEntity
import com.aryaleelastudios.astroveda.data.model.BroadScopeItem
import com.aryaleelastudios.astroveda.data.model.ChartStyle
import com.aryaleelastudios.astroveda.data.model.CreativeFaqItem
import com.aryaleelastudios.astroveda.data.model.KundaliData
import com.aryaleelastudios.astroveda.data.model.MindsetRuleCard
import com.aryaleelastudios.astroveda.data.model.NormalizedMatchCard
import com.aryaleelastudios.astroveda.data.model.ReadingCategory
import com.aryaleelastudios.astroveda.ui.components.AstroMeter
import com.aryaleelastudios.astroveda.ui.components.AstroStatusPill
import com.aryaleelastudios.astroveda.ui.components.DivisionalChartCanvas
import com.aryaleelastudios.astroveda.ui.components.SacredSectionDivider
import com.aryaleelastudios.astroveda.ui.components.clearFocusOnTap
import com.aryaleelastudios.astroveda.ui.theme.AuspiciousGreen
import com.aryaleelastudios.astroveda.ui.theme.CreamBackground
import com.aryaleelastudios.astroveda.ui.theme.CreamBorder
import com.aryaleelastudios.astroveda.ui.theme.CreamSurface
import com.aryaleelastudios.astroveda.ui.theme.DeepIndigo
import com.aryaleelastudios.astroveda.ui.theme.IndigoContainer
import com.aryaleelastudios.astroveda.ui.theme.IndigoSurface
import com.aryaleelastudios.astroveda.ui.theme.MidnightNavy
import com.aryaleelastudios.astroveda.ui.theme.PaleCreamChipBg
import com.aryaleelastudios.astroveda.ui.theme.SaffronAccent
import com.aryaleelastudios.astroveda.ui.theme.SaffronDark
import com.aryaleelastudios.astroveda.ui.theme.SaffronLight
import com.aryaleelastudios.astroveda.ui.theme.SubtleCreamBorder
import com.aryaleelastudios.astroveda.ui.theme.TextMuted
import com.aryaleelastudios.astroveda.ui.theme.TextPrimary
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun DetailedCategoryScreen(
    person: PersonEntity,
    kundali: KundaliData,
    selectedCategory: ReadingCategory,
    chartStyle: ChartStyle,
    onSelectCategory: (ReadingCategory) -> Unit,
    onNavigateBack: () -> Unit
) {
    BackHandler { onNavigateBack() }

    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current
    val listState = rememberLazyListState()

    LaunchedEffect(selectedCategory) {
        listState.scrollToItem(0)
    }

    fun dismissKeyboard() {
        focusManager.clearFocus(force = true)
        keyboardController?.hide()
    }

    val deepDive = kundali.categoryDeepDives[selectedCategory]
        ?: kundali.categoryDeepDives.values.firstOrNull()
        ?: return

    var selectedCareerTrack by rememberSaveable(selectedCategory) { mutableStateOf("employment") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = stringResource(selectedCategory.labelResId),
                            style = MaterialTheme.typography.titleLarge,
                            color = DeepIndigo,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = person.name,
                            style = MaterialTheme.typography.labelMedium,
                            color = SaffronDark,
                            maxLines = 1
                        )
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = onNavigateBack,
                        modifier = Modifier.testTag("category_back_btn")
                    ) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.cd_back),
                            tint = DeepIndigo
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = CreamBackground)
            )
        },
        containerColor = CreamBackground,
        modifier = Modifier.testTag("detailed_category_screen")
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
                    .testTag("category_detail_list"),
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 36.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // NON-SCROLLING 2-COLUMN CATEGORY TAB GRID (All 5 visible without horizontal scrolling)
                item {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("category_tab_grid")
                    ) {
                        val rows = listOf(
                            listOf(ReadingCategory.CAREER, ReadingCategory.WEALTH),
                            listOf(ReadingCategory.MARRIAGE, ReadingCategory.HEALTH),
                            listOf(ReadingCategory.DHARMA)
                        )
                        rows.forEach { rowCats ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                rowCats.forEach { cat ->
                                    CategoryGridTab(
                                        category = cat,
                                        isSelected = cat == selectedCategory,
                                        onClick = {
                                            dismissKeyboard()
                                            onSelectCategory(cat)
                                        },
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                                if (rowCats.size == 1) {
                                    Spacer(Modifier.weight(1f))
                                }
                            }
                        }
                    }
                }

                // 1. HERO SECTION (Rich Indigo-Midnight Gradient + Planetary Glyph + Gold Border + Astro-Math Pill)
                item {
                    Card(
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = DeepIndigo),
                        border = BorderStroke(1.25.dp, SaffronAccent.copy(alpha = 0.65f)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .shadow(
                                elevation = 8.dp,
                                shape = RoundedCornerShape(20.dp),
                                ambientColor = DeepIndigo.copy(alpha = 0.35f),
                                spotColor = MidnightNavy.copy(alpha = 0.45f)
                            )
                            .testTag("category_archetype_hero")
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
                                    text = stringResource(R.string.cat_your_gift_label).uppercase(Locale.US),
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = SaffronAccent
                                )
                                AstroStatusPill(
                                    score = deepDive.overallScore,
                                    showStars = true,
                                    modifier = Modifier.testTag("category_hero_status_pill")
                                )
                            }

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(46.dp)
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
                                        text = deepDive.heroGlyph.ifBlank { "✦" },
                                        fontSize = 22.sp,
                                        color = SaffronLight,
                                        fontWeight = FontWeight.Bold
                                    )
                                }

                                Text(
                                    text = deepDive.signatureTitle,
                                    style = MaterialTheme.typography.headlineSmall,
                                    color = Color.White,
                                    modifier = Modifier
                                        .weight(1f)
                                        .testTag("category_archetype_title")
                                )
                            }

                            if (deepDive.heroSummary.isNotBlank()) {
                                Text(
                                    text = deepDive.heroSummary,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = CreamBackground.copy(alpha = 0.94f),
                                    modifier = Modifier.testTag("category_hero_summary")
                                )
                            }

                            if (deepDive.astroMathPill.isNotBlank()) {
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = Color.White.copy(alpha = 0.12f),
                                    border = BorderStroke(1.dp, SaffronAccent.copy(alpha = 0.65f)),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("category_astro_math_pill")
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
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
                                            text = deepDive.astroMathPill,
                                            style = MaterialTheme.typography.labelMedium,
                                            color = SaffronLight
                                        )
                                    }
                                }
                            }

                            Text(
                                text = deepDive.archetype.derivationBasis,
                                style = MaterialTheme.typography.bodySmall,
                                color = CreamBackground.copy(alpha = 0.82f)
                            )
                        }
                    }
                }

                item {
                    SacredSectionDivider(symbol = "ॐ")
                }

                // 2. DNA / SOUL ALIGNMENT METERS (Career/Financial/Relationship/Vitality DNA = 5 bars; Soul Alignment = 4 bars)
                item {
                    val dnaTitleRes = when (selectedCategory) {
                        ReadingCategory.CAREER -> R.string.cat_career_dna_title
                        ReadingCategory.WEALTH -> R.string.cat_financial_dna_title
                        ReadingCategory.MARRIAGE -> R.string.cat_relationship_dna_title
                        ReadingCategory.HEALTH -> R.string.cat_vitality_dna_title
                        ReadingCategory.DHARMA -> R.string.cat_soul_alignment_title
                    }
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        border = BorderStroke(1.dp, CreamBorder),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("category_power_bars_card")
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            Text(
                                text = stringResource(dnaTitleRes),
                                style = MaterialTheme.typography.titleMedium,
                                color = DeepIndigo
                            )
                            deepDive.powerBars.forEach { metric ->
                                AstroMeter(
                                    label = metric.label,
                                    glyph = metric.glyph,
                                    score = metric.score,
                                    summary = metric.note
                                )
                            }
                        }
                    }
                }

                item {
                    SacredSectionDivider(symbol = "✦")
                }

                // 3. TIER 1 SECTION (Normalized 80-95% Matches or Tier 1 Profile/Care/Mission Cards)
                if (selectedCategory == ReadingCategory.CAREER &&
                    (deepDive.topJobMatches.isNotEmpty() || deepDive.topBusinessMatches.isNotEmpty())
                ) {
                    item {
                        var showAllCareer by rememberSaveable(selectedCategory, selectedCareerTrack) {
                            mutableStateOf(false)
                        }
                        Card(
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            border = BorderStroke(1.dp, CreamBorder),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("category_tier1_card")
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Text(
                                    text = stringResource(R.string.cat_tier1_career_matches_title),
                                    style = MaterialTheme.typography.titleMedium,
                                    color = DeepIndigo
                                )

                                // Segmented [ Employment ] vs [ Business ] Toggle
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(CreamBackground, RoundedCornerShape(12.dp))
                                        .border(1.dp, CreamBorder, RoundedCornerShape(12.dp))
                                        .padding(4.dp),
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    val isEmp = selectedCareerTrack == "employment"
                                    Surface(
                                        shape = RoundedCornerShape(9.dp),
                                        color = if (isEmp) SaffronAccent else Color.Transparent,
                                        modifier = Modifier
                                            .weight(1f)
                                            .heightIn(min = 48.dp)
                                            .clickable {
                                                dismissKeyboard()
                                                selectedCareerTrack = "employment"
                                            }
                                            .testTag("career_track_employment")
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Text(
                                                text = stringResource(R.string.cat_employment_tab),
                                                style = MaterialTheme.typography.labelLarge,
                                                fontWeight = FontWeight.Bold,
                                                color = DeepIndigo
                                            )
                                        }
                                    }

                                    Surface(
                                        shape = RoundedCornerShape(9.dp),
                                        color = if (!isEmp) SaffronAccent else Color.Transparent,
                                        modifier = Modifier
                                            .weight(1f)
                                            .heightIn(min = 48.dp)
                                            .clickable {
                                                dismissKeyboard()
                                                selectedCareerTrack = "business"
                                            }
                                            .testTag("career_track_business")
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Text(
                                                text = stringResource(R.string.cat_business_tab),
                                                style = MaterialTheme.typography.labelLarge,
                                                fontWeight = FontWeight.Bold,
                                                color = DeepIndigo
                                            )
                                        }
                                    }
                                }

                                if (deepDive.employmentVsBusinessGuidance.isNotBlank()) {
                                    Surface(
                                        shape = RoundedCornerShape(12.dp),
                                        color = CreamSurface,
                                        border = BorderStroke(1.dp, SaffronAccent.copy(alpha = 0.55f))
                                    ) {
                                        Text(
                                            text = deepDive.employmentVsBusinessGuidance,
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = TextPrimary,
                                            modifier = Modifier.padding(12.dp)
                                        )
                                    }
                                }

                                Text(
                                    text = stringResource(
                                        if (selectedCareerTrack == "employment") R.string.cat_career_employment_sub
                                        else R.string.cat_career_business_sub
                                    ),
                                    style = MaterialTheme.typography.labelMedium,
                                    color = TextMuted
                                )

                                val fullList = if (selectedCareerTrack == "employment") {
                                    deepDive.topJobMatches
                                } else {
                                    deepDive.topBusinessMatches
                                }
                                val visibleList = if (showAllCareer) fullList else fullList.take(3)

                                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                    visibleList.forEachIndexed { idx, match ->
                                        NormalizedMatchCardView(
                                            match = match,
                                            showMatchPercent = true,
                                            modifier = Modifier.testTag("career_tier1_match_$idx")
                                        )
                                    }
                                }

                                if (fullList.size > 3) {
                                    TextButton(
                                        onClick = { showAllCareer = !showAllCareer },
                                        modifier = Modifier
                                            .align(Alignment.End)
                                            .heightIn(min = 48.dp)
                                            .testTag("tier1_expand_toggle")
                                    ) {
                                        Text(
                                            text = stringResource(
                                                if (showAllCareer) R.string.cat_tier1_show_less else R.string.cat_tier1_show_more
                                            ),
                                            color = SaffronDark,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                        }
                    }
                } else {
                    val tier1TitleRes = when (selectedCategory) {
                        ReadingCategory.WEALTH -> R.string.cat_tier1_wealth_sources_title
                        ReadingCategory.MARRIAGE -> R.string.cat_tier1_spouse_profile_title
                        ReadingCategory.HEALTH -> R.string.cat_tier1_health_focus_title
                        ReadingCategory.DHARMA -> R.string.cat_tier1_soul_mission_title
                        ReadingCategory.CAREER -> R.string.cat_tier1_career_matches_title
                    }
                    val fullTier1Items = when (selectedCategory) {
                        ReadingCategory.WEALTH -> deepDive.topWealthMatches
                        else -> deepDive.tier1Cards
                    }
                    if (fullTier1Items.isNotEmpty()) {
                        item {
                            var showAllItems by rememberSaveable(selectedCategory) { mutableStateOf(false) }
                            val visibleItems = if (showAllItems) fullTier1Items else fullTier1Items.take(3)
                            Card(
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(containerColor = Color.White),
                                border = BorderStroke(1.dp, CreamBorder),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("category_tier1_card")
                            ) {
                                Column(
                                    modifier = Modifier.padding(16.dp),
                                    verticalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    Text(
                                        text = stringResource(tier1TitleRes),
                                        style = MaterialTheme.typography.titleMedium,
                                        color = DeepIndigo
                                    )
                                    visibleItems.forEachIndexed { idx, itemCard ->
                                        NormalizedMatchCardView(
                                            match = itemCard,
                                            showMatchPercent = selectedCategory == ReadingCategory.WEALTH,
                                            modifier = Modifier.testTag("category_tier1_item_$idx")
                                        )
                                    }
                                    if (fullTier1Items.size > 3) {
                                        TextButton(
                                            onClick = { showAllItems = !showAllItems },
                                            modifier = Modifier
                                                .align(Alignment.End)
                                                .heightIn(min = 48.dp)
                                                .testTag("tier1_expand_toggle")
                                        ) {
                                            Text(
                                                text = stringResource(
                                                    if (showAllItems) R.string.cat_tier1_show_less else R.string.cat_tier1_show_more
                                                ),
                                                color = SaffronDark,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                item {
                    SacredSectionDivider(symbol = "ॐ")
                }

                // 4. TIER 2 SECTION (Broad Industry Scopes / Money Mindset / Love vs Arranged / Vitality Plan / Life Themes)
                if (selectedCategory == ReadingCategory.CAREER && deepDive.broadScopes.isNotEmpty()) {
                    item {
                        Card(
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            border = BorderStroke(1.dp, CreamBorder),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("category_tier2_card")
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Text(
                                    text = stringResource(R.string.cat_tier2_broad_scopes_title),
                                    style = MaterialTheme.typography.titleMedium,
                                    color = DeepIndigo
                                )
                                deepDive.broadScopes.forEachIndexed { idx, scope ->
                                    BroadScopeCardView(
                                        item = scope,
                                        badgeText = "",
                                        modifier = Modifier.testTag("career_broad_scope_$idx")
                                    )
                                }
                            }
                        }
                    }
                } else if (deepDive.tier2Card != null) {
                    val tier2TitleRes = when (selectedCategory) {
                        ReadingCategory.WEALTH -> R.string.cat_tier2_money_mindset_title
                        ReadingCategory.MARRIAGE -> R.string.cat_tier2_love_vs_arranged_title
                        ReadingCategory.HEALTH -> R.string.cat_tier2_health_action_title
                        ReadingCategory.DHARMA -> R.string.cat_tier2_life_themes_title
                        ReadingCategory.CAREER -> R.string.cat_tier2_broad_scopes_title
                    }
                    item {
                        Card(
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            border = BorderStroke(1.dp, CreamBorder),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("category_tier2_card")
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Text(
                                    text = stringResource(tier2TitleRes),
                                    style = MaterialTheme.typography.titleMedium,
                                    color = DeepIndigo
                                )
                                BroadScopeCardView(
                                    item = deepDive.tier2Card,
                                    badgeText = deepDive.tier2Badge
                                )
                            }
                        }
                    }
                }

                // 5. TIER 3 SECTION (Expandable Attitude / Universal Rules / Psychology / Health Mindset / Spiritual Attitude)
                if (deepDive.tier3Cards.isNotEmpty()) {
                    val fallbackTier3TitleRes = when (selectedCategory) {
                        ReadingCategory.CAREER -> R.string.cat_tier3_career_attitude_title
                        ReadingCategory.WEALTH -> R.string.cat_tier3_wealth_rules_title
                        ReadingCategory.MARRIAGE -> R.string.cat_tier3_rel_psychology_title
                        ReadingCategory.HEALTH -> R.string.cat_tier3_health_mindset_title
                        ReadingCategory.DHARMA -> R.string.cat_tier3_spiritual_attitude_title
                    }
                    item {
                        val tier3Heading = deepDive.tier3SectionTitle.ifBlank { stringResource(fallbackTier3TitleRes) }
                        Card(
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            border = BorderStroke(1.dp, CreamBorder),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("category_tier3_card")
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Psychology,
                                        contentDescription = null,
                                        tint = SaffronDark,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(Modifier.width(8.dp))
                                    Text(
                                        text = tier3Heading,
                                        style = MaterialTheme.typography.titleMedium,
                                        color = DeepIndigo
                                    )
                                }
                                deepDive.tier3Cards.forEachIndexed { idx, card ->
                                    ExpandableMindsetCard(
                                        card = card,
                                        initiallyExpanded = idx == 0,
                                        modifier = Modifier.testTag("tier3_expandable_$idx")
                                    )
                                }
                            }
                        }
                    }
                }

                // 5.5 COLLAPSIBLE CATEGORY STORY SECTION (4 large cohesive paragraphs directly above Creative FAQs)
                if (deepDive.storyParagraphs.isNotEmpty()) {
                    val storyTitleRes = when (selectedCategory) {
                        ReadingCategory.CAREER -> R.string.cat_story_career_title
                        ReadingCategory.WEALTH -> R.string.cat_story_wealth_title
                        ReadingCategory.MARRIAGE -> R.string.cat_story_marriage_title
                        ReadingCategory.HEALTH -> R.string.cat_story_health_title
                        ReadingCategory.DHARMA -> R.string.cat_story_dharma_title
                    }
                    item {
                        SacredSectionDivider(symbol = "✦")
                    }
                    item {
                        var storyExpanded by rememberSaveable(selectedCategory) { mutableStateOf(true) }
                        Card(
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            border = BorderStroke(1.25.dp, SaffronAccent.copy(alpha = 0.65f)),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("category_story_card")
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(
                                        Brush.verticalGradient(
                                            colors = listOf(CreamSurface, PaleCreamChipBg.copy(alpha = 0.45f))
                                        )
                                    )
                                    .padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .heightIn(min = 48.dp)
                                        .clickable { storyExpanded = !storyExpanded }
                                        .testTag("category_story_toggle"),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(32.dp)
                                                .background(SaffronAccent.copy(alpha = 0.22f), CircleShape),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.AutoStories,
                                                contentDescription = null,
                                                tint = SaffronDark,
                                                modifier = Modifier.size(18.dp)
                                            )
                                        }
                                        Spacer(Modifier.width(10.dp))
                                        Column {
                                            Text(
                                                text = stringResource(storyTitleRes),
                                                style = MaterialTheme.typography.titleMedium,
                                                color = DeepIndigo
                                            )
                                            Text(
                                                text = stringResource(R.string.cat_story_subtitle),
                                                style = MaterialTheme.typography.labelSmall,
                                                color = TextMuted
                                            )
                                        }
                                    }
                                    Icon(
                                        imageVector = if (storyExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                                        contentDescription = if (storyExpanded) stringResource(R.string.cd_collapse) else stringResource(R.string.cd_expand),
                                        tint = DeepIndigo
                                    )
                                }

                                AnimatedVisibility(visible = storyExpanded) {
                                    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                                        HorizontalDivider(color = SaffronAccent.copy(alpha = 0.45f))
                                        deepDive.storyParagraphs.forEachIndexed { idx, para ->
                                            Text(
                                                text = para,
                                                style = MaterialTheme.typography.bodyMedium,
                                                color = TextPrimary,
                                                modifier = Modifier.testTag("category_story_para_$idx")
                                            )
                                            if (idx < deepDive.storyParagraphs.lastIndex) {
                                                HorizontalDivider(color = CreamBorder.copy(alpha = 0.6f))
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // 6. CREATIVE FAQS SECTION (Includes Marriage <21 Age-Gated Future Focus)
                if (deepDive.creativeFaqs.isNotEmpty()) {
                    val faqSectionTitleRes = if (deepDive.isAgeGatedFutureFocus) {
                        R.string.cat_future_focus_title
                    } else {
                        R.string.cat_creative_faqs_title
                    }
                    item {
                        Card(
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            border = BorderStroke(1.dp, CreamBorder),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("category_creative_faqs_card")
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Timeline,
                                        contentDescription = null,
                                        tint = SaffronDark,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(Modifier.width(8.dp))
                                    Text(
                                        text = stringResource(faqSectionTitleRes),
                                        style = MaterialTheme.typography.titleMedium,
                                        color = DeepIndigo
                                    )
                                }
                                deepDive.creativeFaqs.forEachIndexed { idx, faq ->
                                    ExpandableFaqCard(
                                        faq = faq,
                                        initiallyExpanded = idx == 0,
                                        modifier = Modifier.testTag("creative_faq_$idx")
                                    )
                                }
                            }
                        }
                    }
                }

                // 7. MANDATORY MEDICAL DISCLAIMER (Health Page)
                if (deepDive.medicalDisclaimer.isNotBlank()) {
                    item {
                        Card(
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = IndigoSurface),
                            border = BorderStroke(1.dp, DeepIndigo.copy(alpha = 0.25f)),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("category_medical_disclaimer_card")
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.HealthAndSafety,
                                        contentDescription = null,
                                        tint = DeepIndigo,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(Modifier.width(8.dp))
                                    Text(
                                        text = stringResource(R.string.cat_health_medical_disclaimer_title),
                                        style = MaterialTheme.typography.titleMedium,
                                        color = DeepIndigo
                                    )
                                }
                                Text(
                                    text = deepDive.medicalDisclaimer,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = TextPrimary
                                )
                            }
                        }
                    }
                }

                // SUPPORTING SECTION: Superpower & Area for Growth
                if (deepDive.superpower.title.isNotBlank()) {
                    item {
                        Card(
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = CreamSurface),
                            border = BorderStroke(1.2.dp, SaffronAccent.copy(alpha = 0.7f)),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("category_superpower_card")
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(
                                        Brush.verticalGradient(
                                            colors = listOf(SaffronLight.copy(alpha = 0.22f), CreamSurface)
                                        )
                                    )
                                    .padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                                        Box(
                                            modifier = Modifier
                                                .size(32.dp)
                                                .background(SaffronAccent.copy(alpha = 0.28f), CircleShape),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Bolt,
                                                contentDescription = null,
                                                tint = SaffronDark,
                                                modifier = Modifier.size(18.dp)
                                            )
                                        }
                                        Spacer(Modifier.width(10.dp))
                                        Text(
                                            text = stringResource(R.string.cat_superpower_title),
                                            style = MaterialTheme.typography.titleMedium,
                                            color = DeepIndigo
                                        )
                                    }
                                    if (deepDive.superpower.placementTag.isNotBlank()) {
                                        Surface(
                                            shape = RoundedCornerShape(12.dp),
                                            color = SaffronAccent.copy(alpha = 0.24f),
                                            border = BorderStroke(1.dp, SaffronAccent)
                                        ) {
                                            Text(
                                                text = deepDive.superpower.placementTag,
                                                style = MaterialTheme.typography.labelSmall,
                                                fontWeight = FontWeight.Bold,
                                                color = DeepIndigo,
                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                            )
                                        }
                                    }
                                }

                                Text(
                                    text = deepDive.superpower.title,
                                    style = MaterialTheme.typography.titleSmall,
                                    color = SaffronDark
                                )
                                Text(
                                    text = deepDive.superpower.description,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = TextPrimary
                                )
                            }
                        }
                    }
                }

                if (deepDive.growthArea.title.isNotBlank()) {
                    item {
                        Card(
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            border = BorderStroke(1.dp, DeepIndigo.copy(alpha = 0.25f)),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("category_growth_area_card")
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                                        Box(
                                            modifier = Modifier
                                                .size(32.dp)
                                                .background(IndigoSurface, CircleShape),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.TrendingUp,
                                                contentDescription = null,
                                                tint = DeepIndigo,
                                                modifier = Modifier.size(18.dp)
                                            )
                                        }
                                        Spacer(Modifier.width(10.dp))
                                        Text(
                                            text = stringResource(R.string.cat_growth_area_title),
                                            style = MaterialTheme.typography.titleMedium,
                                            color = DeepIndigo
                                        )
                                    }
                                    if (deepDive.growthArea.placementTag.isNotBlank()) {
                                        Surface(
                                            shape = RoundedCornerShape(12.dp),
                                            color = IndigoSurface,
                                            border = BorderStroke(1.dp, DeepIndigo.copy(alpha = 0.25f))
                                        ) {
                                            Text(
                                                text = deepDive.growthArea.placementTag,
                                                style = MaterialTheme.typography.labelSmall,
                                                fontWeight = FontWeight.Bold,
                                                color = DeepIndigo,
                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                            )
                                        }
                                    }
                                }

                                Text(
                                    text = deepDive.growthArea.title,
                                    style = MaterialTheme.typography.titleSmall,
                                    color = DeepIndigo
                                )
                                Text(
                                    text = deepDive.growthArea.description,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = TextPrimary
                                )
                                if (deepDive.growthArea.guidance.isNotBlank()) {
                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .background(IndigoSurface.copy(alpha = 0.65f), RoundedCornerShape(10.dp))
                                            .border(1.dp, DeepIndigo.copy(alpha = 0.15f), RoundedCornerShape(10.dp))
                                            .padding(10.dp)
                                    ) {
                                        Text(
                                            text = stringResource(R.string.cat_growth_guidance_label),
                                            style = MaterialTheme.typography.labelMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = DeepIndigo
                                        )
                                        Spacer(Modifier.height(2.dp))
                                        Text(
                                            text = deepDive.growthArea.guidance,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = TextPrimary
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // SUPPORTING SECTION: Detailed Narrative (Personality, Strengths, Subtle Challenges)
                item {
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        border = BorderStroke(1.dp, CreamBorder),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("category_detailed_insights_card")
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = SaffronDark, modifier = Modifier.size(20.dp))
                                Spacer(Modifier.width(8.dp))
                                Text(
                                    text = stringResource(R.string.cat_narrative_title),
                                    style = MaterialTheme.typography.titleMedium,
                                    color = DeepIndigo
                                )
                            }

                            if (deepDive.personalityParagraph.isNotBlank()) {
                                Text(
                                    text = stringResource(R.string.cat_personality_heading),
                                    style = MaterialTheme.typography.titleSmall,
                                    color = SaffronDark
                                )
                                Text(
                                    text = deepDive.personalityParagraph,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = TextPrimary,
                                    modifier = Modifier.testTag("category_personality_text")
                                )
                            }

                            if (deepDive.strengthsParagraph.isNotBlank()) {
                                HorizontalDivider(color = CreamBorder)
                                Text(
                                    text = stringResource(R.string.cat_strengths_heading),
                                    style = MaterialTheme.typography.titleSmall,
                                    color = AuspiciousGreen
                                )
                                Text(
                                    text = deepDive.strengthsParagraph,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = TextPrimary,
                                    modifier = Modifier.testTag("category_strengths_text")
                                )
                            }

                            if (deepDive.challengesParagraph.isNotBlank()) {
                                HorizontalDivider(color = CreamBorder)
                                Text(
                                    text = stringResource(R.string.cat_challenges_heading),
                                    style = MaterialTheme.typography.titleSmall,
                                    color = DeepIndigo
                                )
                                Text(
                                    text = deepDive.challengesParagraph,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = TextPrimary,
                                    modifier = Modifier.testTag("category_challenges_text")
                                )
                            }

                            if (deepDive.personalityParagraph.isBlank() && deepDive.strengthsParagraph.isBlank()) {
                                Text(
                                    text = deepDive.narrative,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = TextPrimary
                                )
                            }
                        }
                    }
                }

                // SUPPORTING SECTION: Recommended Themes & Cautious Zones Overview
                item {
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        border = BorderStroke(1.dp, CreamBorder),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("category_paths_overview_card")
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Text(
                                text = stringResource(R.string.cat_recommended_paths),
                                style = MaterialTheme.typography.titleSmall,
                                color = AuspiciousGreen
                            )
                            FlowRow(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                deepDive.archetype.recommendedPaths.forEach { path ->
                                    Surface(
                                        shape = RoundedCornerShape(10.dp),
                                        color = AuspiciousGreen.copy(alpha = 0.1f),
                                        border = BorderStroke(1.dp, AuspiciousGreen.copy(alpha = 0.35f))
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Icon(
                                                Icons.Default.CheckCircle,
                                                contentDescription = null,
                                                tint = AuspiciousGreen,
                                                modifier = Modifier.size(14.dp)
                                            )
                                            Spacer(Modifier.width(5.dp))
                                            Text(
                                                text = path,
                                                style = MaterialTheme.typography.labelMedium,
                                                color = DeepIndigo
                                            )
                                        }
                                    }
                                }
                            }

                            HorizontalDivider(color = CreamBorder)

                            Text(
                                text = stringResource(R.string.cat_cautious_zones),
                                style = MaterialTheme.typography.titleSmall,
                                color = SaffronDark
                            )
                            FlowRow(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                deepDive.archetype.cautiousZones.forEach { zone ->
                                    Surface(
                                        shape = RoundedCornerShape(10.dp),
                                        color = PaleCreamChipBg,
                                        border = BorderStroke(1.dp, SubtleCreamBorder)
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Icon(
                                                Icons.Default.Info,
                                                contentDescription = null,
                                                tint = SaffronDark,
                                                modifier = Modifier.size(14.dp)
                                            )
                                            Spacer(Modifier.width(5.dp))
                                            Text(
                                                text = zone,
                                                style = MaterialTheme.typography.bodySmall,
                                                color = TextPrimary
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // SUPPORTING SECTION: In-Depth Timing Windows (unless age-gated on Marriage)
                if (deepDive.absorbedReadings.isNotEmpty() && !deepDive.isAgeGatedFutureFocus) {
                    item {
                        Card(
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            border = BorderStroke(1.dp, CreamBorder),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("category_absorbed_topics_card")
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Text(
                                    text = stringResource(R.string.cat_absorbed_topics_title),
                                    style = MaterialTheme.typography.titleMedium,
                                    color = DeepIndigo
                                )

                                deepDive.absorbedReadings.forEach { topic ->
                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .background(CreamBackground, RoundedCornerShape(12.dp))
                                            .border(1.dp, CreamBorder, RoundedCornerShape(12.dp))
                                            .padding(14.dp)
                                            .testTag("absorbed_topic_${topic.topicId}"),
                                        verticalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.Top
                                        ) {
                                            Text(
                                                text = topic.title,
                                                style = MaterialTheme.typography.titleSmall,
                                                color = DeepIndigo,
                                                modifier = Modifier
                                                    .weight(1f)
                                                    .padding(end = 8.dp)
                                            )
                                            if (topic.timingWindow.isNotBlank()) {
                                                Surface(
                                                    shape = RoundedCornerShape(10.dp),
                                                    color = SaffronAccent.copy(alpha = 0.24f),
                                                    border = BorderStroke(1.dp, SaffronAccent)
                                                ) {
                                                    Row(
                                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                                        verticalAlignment = Alignment.CenterVertically
                                                    ) {
                                                        Icon(
                                                            Icons.Default.DateRange,
                                                            contentDescription = null,
                                                            tint = SaffronDark,
                                                            modifier = Modifier.size(12.dp)
                                                        )
                                                        Spacer(Modifier.width(4.dp))
                                                        Text(
                                                            text = stringResource(R.string.cat_timing_window_badge, topic.timingWindow),
                                                            style = MaterialTheme.typography.labelSmall,
                                                            fontWeight = FontWeight.Bold,
                                                            color = DeepIndigo
                                                        )
                                                    }
                                                }
                                            }
                                        }

                                        Text(
                                            text = topic.body,
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = TextPrimary
                                        )

                                        if (topic.astroBasis.isNotBlank()) {
                                            Text(
                                                text = topic.astroBasis,
                                                style = MaterialTheme.typography.labelMedium,
                                                color = SaffronDark
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // SUPPORTING SECTION: Cross-Chart Life Connections
                if (deepDive.crossReferences.isNotEmpty()) {
                    item {
                        Card(
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            border = BorderStroke(1.dp, CreamBorder),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("category_cross_references_card")
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Text(
                                    text = stringResource(R.string.cat_cross_reference_title),
                                    style = MaterialTheme.typography.titleMedium,
                                    color = DeepIndigo
                                )

                                deepDive.crossReferences.forEach { ref ->
                                    Surface(
                                        shape = RoundedCornerShape(12.dp),
                                        color = IndigoSurface.copy(alpha = 0.5f),
                                        border = BorderStroke(1.dp, DeepIndigo.copy(alpha = 0.18f)),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .heightIn(min = 56.dp)
                                            .clickable {
                                                dismissKeyboard()
                                                onSelectCategory(ref.targetCategory)
                                            }
                                            .testTag("cross_ref_${ref.targetCategory.name.lowercase(Locale.US)}")
                                    ) {
                                        Column(
                                            modifier = Modifier.padding(12.dp),
                                            verticalArrangement = Arrangement.spacedBy(6.dp)
                                        ) {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Surface(
                                                    shape = RoundedCornerShape(8.dp),
                                                    color = SaffronAccent.copy(alpha = 0.25f)
                                                ) {
                                                    Text(
                                                        text = ref.targetLabel,
                                                        style = MaterialTheme.typography.labelSmall,
                                                        fontWeight = FontWeight.Bold,
                                                        color = DeepIndigo,
                                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                                    )
                                                }
                                                Row(verticalAlignment = Alignment.CenterVertically) {
                                                    Text(
                                                        text = stringResource(R.string.cat_tap_to_open),
                                                        style = MaterialTheme.typography.labelSmall,
                                                        fontWeight = FontWeight.Bold,
                                                        color = SaffronDark
                                                    )
                                                    Icon(
                                                        Icons.AutoMirrored.Filled.KeyboardArrowRight,
                                                        contentDescription = null,
                                                        tint = SaffronDark,
                                                        modifier = Modifier.size(16.dp)
                                                    )
                                                }
                                            }
                                            Text(
                                                text = ref.title,
                                                style = MaterialTheme.typography.titleSmall,
                                                color = DeepIndigo
                                            )
                                            Text(
                                                text = ref.insight,
                                                style = MaterialTheme.typography.bodySmall,
                                                color = TextPrimary
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // SUPPORTING SECTION: Astrological Proof & Divisional Chart
                item {
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        border = BorderStroke(1.dp, CreamBorder),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("category_astro_proof_card")
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Text(
                                text = stringResource(R.string.cat_astro_proof_title),
                                style = MaterialTheme.typography.titleMedium,
                                color = DeepIndigo
                            )

                            deepDive.proofItems.forEach { proof ->
                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = CreamBackground,
                                    border = BorderStroke(1.dp, CreamBorder),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(10.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = proof.title,
                                                style = MaterialTheme.typography.labelLarge,
                                                fontWeight = FontWeight.Bold,
                                                color = DeepIndigo
                                            )
                                            Text(
                                                text = proof.placementDetail,
                                                style = MaterialTheme.typography.bodySmall,
                                                color = TextMuted
                                            )
                                        }
                                        Spacer(Modifier.width(8.dp))
                                        Surface(
                                            shape = RoundedCornerShape(8.dp),
                                            color = PaleCreamChipBg,
                                            border = BorderStroke(1.dp, SubtleCreamBorder)
                                        ) {
                                            Text(
                                                text = proof.statusLabel,
                                                style = MaterialTheme.typography.labelSmall,
                                                fontWeight = FontWeight.Bold,
                                                color = if (proof.isFavorable) AuspiciousGreen else SaffronDark,
                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                            )
                                        }
                                    }
                                }
                            }

                            if (deepDive.yogaNotes.isNotEmpty()) {
                                HorizontalDivider(color = CreamBorder)
                                Text(
                                    text = stringResource(R.string.cat_yogas_proof_title),
                                    style = MaterialTheme.typography.titleSmall,
                                    color = DeepIndigo
                                )
                                deepDive.yogaNotes.forEach { yd ->
                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .background(CreamBackground, RoundedCornerShape(10.dp))
                                            .padding(10.dp)
                                    ) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Text(
                                                text = yd.name,
                                                style = MaterialTheme.typography.labelLarge,
                                                fontWeight = FontWeight.Bold,
                                                color = DeepIndigo
                                            )
                                            Text(
                                                text = yd.statusText,
                                                style = MaterialTheme.typography.labelSmall,
                                                fontWeight = FontWeight.Bold,
                                                color = if (yd.isPresent) SaffronDark else AuspiciousGreen
                                            )
                                        }
                                        Spacer(Modifier.height(3.dp))
                                        Text(
                                            text = yd.description,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = TextPrimary
                                        )
                                    }
                                }
                            }

                            HorizontalDivider(color = CreamBorder)

                            Text(
                                text = stringResource(R.string.cat_divisional_chart_title, deepDive.divisionalChartCode),
                                style = MaterialTheme.typography.titleSmall,
                                color = DeepIndigo
                            )
                            DivisionalChartCanvas(
                                chartCode = deepDive.divisionalChartCode,
                                kundali = kundali,
                                chartStyle = chartStyle
                            )
                        }
                    }
                }

                // SUPPORTING SECTION: Actionable Guidance & Category Remedies
                item {
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        border = BorderStroke(1.dp, CreamBorder),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("category_actionable_advice_card")
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Text(
                                text = stringResource(R.string.cat_actionable_advice_title),
                                style = MaterialTheme.typography.titleMedium,
                                color = DeepIndigo
                            )

                            Text(
                                text = stringResource(R.string.cat_what_to_do),
                                style = MaterialTheme.typography.titleSmall,
                                color = AuspiciousGreen
                            )
                            deepDive.whatToDo.forEach { tip ->
                                Row(verticalAlignment = Alignment.Top) {
                                    Icon(
                                        Icons.Default.CheckCircle,
                                        contentDescription = null,
                                        tint = AuspiciousGreen,
                                        modifier = Modifier
                                            .size(18.dp)
                                            .padding(top = 2.dp)
                                    )
                                    Spacer(Modifier.width(8.dp))
                                    Text(
                                        text = tip,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = TextPrimary
                                    )
                                }
                            }

                            HorizontalDivider(color = CreamBorder)

                            Text(
                                text = stringResource(R.string.cat_safety_advice),
                                style = MaterialTheme.typography.titleSmall,
                                color = SaffronDark
                            )
                            deepDive.safetyAdvice.forEach { tip ->
                                Row(verticalAlignment = Alignment.Top) {
                                    Icon(
                                        Icons.Default.Shield,
                                        contentDescription = null,
                                        tint = SaffronDark,
                                        modifier = Modifier
                                            .size(18.dp)
                                            .padding(top = 2.dp)
                                    )
                                    Spacer(Modifier.width(8.dp))
                                    Text(
                                        text = tip,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = TextPrimary
                                    )
                                }
                            }
                        }
                    }
                }

                item {
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        border = BorderStroke(1.dp, CreamBorder),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("category_remedies_card")
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(32.dp)
                                        .background(SaffronAccent.copy(alpha = 0.22f), CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(Icons.Default.Spa, contentDescription = null, tint = SaffronDark, modifier = Modifier.size(18.dp))
                                }
                                Spacer(Modifier.width(10.dp))
                                Text(
                                    text = stringResource(R.string.cat_remedies_title),
                                    style = MaterialTheme.typography.titleMedium,
                                    color = DeepIndigo
                                )
                            }

                            CategoryRemedyBlock(
                                title = stringResource(R.string.cat_remedy_mantra),
                                body = deepDive.remedy.mantra
                            )
                            CategoryRemedyBlock(
                                title = stringResource(R.string.cat_remedy_charity),
                                body = deepDive.remedy.charity
                            )
                            CategoryRemedyBlock(
                                title = stringResource(R.string.cat_remedy_ritual),
                                body = deepDive.remedy.ritual
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CategoryGridTab(
    category: ReadingCategory,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(14.dp),
        color = if (isSelected) SaffronAccent else Color.White,
        border = BorderStroke(
            width = if (isSelected) 1.5.dp else 1.dp,
            color = if (isSelected) SaffronDark else CreamBorder
        ),
        modifier = modifier
            .heightIn(min = 48.dp)
            .clickable(onClick = onClick)
            .testTag("cat_pill_${category.name.lowercase(Locale.US)}")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = categoryIcon(category),
                contentDescription = null,
                tint = DeepIndigo,
                modifier = Modifier.size(17.dp)
            )
            Spacer(Modifier.width(7.dp))
            Text(
                text = stringResource(category.shortTabResId),
                style = MaterialTheme.typography.labelLarge,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.SemiBold,
                color = DeepIndigo,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun NormalizedMatchCardView(
    match: NormalizedMatchCard,
    showMatchPercent: Boolean,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(
                Brush.verticalGradient(
                    colors = listOf(CreamSurface, PaleCreamChipBg)
                ),
                RoundedCornerShape(12.dp)
            )
            .border(1.dp, SaffronAccent.copy(alpha = 0.7f), RoundedCornerShape(12.dp))
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top
        ) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(end = 8.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = match.title,
                    style = MaterialTheme.typography.titleSmall,
                    color = DeepIndigo
                )
                if (match.sector.isNotBlank()) {
                    Text(
                        text = match.sector,
                        style = MaterialTheme.typography.labelSmall,
                        color = SaffronDark
                    )
                }
            }
            if (showMatchPercent && match.matchPercent > 0) {
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = AuspiciousGreen.copy(alpha = 0.14f),
                    border = BorderStroke(1.dp, AuspiciousGreen.copy(alpha = 0.55f))
                ) {
                    Text(
                        text = stringResource(R.string.cat_match_percent_format, match.matchPercent),
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = AuspiciousGreen,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }
            }
        }

        Text(
            text = match.description,
            style = MaterialTheme.typography.bodyMedium,
            color = TextPrimary
        )

        if (match.chartReference.isNotBlank()) {
            ChartReferencePill(match.chartReference)
        }
    }
}

@Composable
private fun BroadScopeCardView(
    item: BroadScopeItem,
    badgeText: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(CreamBackground, RoundedCornerShape(12.dp))
            .border(1.dp, CreamBorder, RoundedCornerShape(12.dp))
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = item.title,
                style = MaterialTheme.typography.titleSmall,
                color = DeepIndigo,
                modifier = Modifier
                    .weight(1f)
                    .padding(end = 8.dp)
            )
            if (badgeText.isNotBlank()) {
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = SaffronAccent.copy(alpha = 0.25f),
                    border = BorderStroke(1.dp, SaffronAccent)
                ) {
                    Text(
                        text = badgeText,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = DeepIndigo,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }
            }
        }

        Text(
            text = item.description,
            style = MaterialTheme.typography.bodyMedium,
            color = TextPrimary
        )

        if (item.chartReference.isNotBlank()) {
            ChartReferencePill(item.chartReference)
        }
    }
}

@Composable
private fun ExpandableMindsetCard(
    card: MindsetRuleCard,
    initiallyExpanded: Boolean,
    modifier: Modifier = Modifier
) {
    var expanded by remember(card.title) { mutableStateOf(initiallyExpanded) }
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(CreamBackground, RoundedCornerShape(12.dp))
            .border(1.dp, CreamBorder, RoundedCornerShape(12.dp))
            .clickable { expanded = !expanded }
            .padding(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 48.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = card.title,
                style = MaterialTheme.typography.titleSmall,
                color = DeepIndigo,
                modifier = Modifier
                    .weight(1f)
                    .padding(end = 8.dp)
            )
            Icon(
                imageVector = if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                contentDescription = if (expanded) stringResource(R.string.cd_collapse) else stringResource(R.string.cd_expand),
                tint = DeepIndigo
            )
        }

        AnimatedVisibility(visible = expanded) {
            Column(
                modifier = Modifier.padding(top = 6.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = card.body,
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextPrimary
                )
                if (card.chartReference.isNotBlank()) {
                    ChartReferencePill(card.chartReference)
                }
            }
        }
    }
}

@Composable
private fun ExpandableFaqCard(
    faq: CreativeFaqItem,
    initiallyExpanded: Boolean,
    modifier: Modifier = Modifier
) {
    var expanded by remember(faq.question) { mutableStateOf(initiallyExpanded) }
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(CreamBackground, RoundedCornerShape(12.dp))
            .border(1.dp, CreamBorder, RoundedCornerShape(12.dp))
            .clickable { expanded = !expanded }
            .padding(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 48.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(end = 8.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                if (faq.badge.isNotBlank()) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = SaffronAccent.copy(alpha = 0.24f),
                        border = BorderStroke(1.dp, SaffronAccent)
                    ) {
                        Text(
                            text = faq.badge,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = DeepIndigo,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                        )
                    }
                }
                Text(
                    text = faq.question,
                    style = MaterialTheme.typography.titleSmall,
                    color = DeepIndigo
                )
            }
            Icon(
                imageVector = if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                contentDescription = if (expanded) stringResource(R.string.cd_collapse) else stringResource(R.string.cd_expand),
                tint = DeepIndigo
            )
        }

        AnimatedVisibility(visible = expanded) {
            Column(
                modifier = Modifier.padding(top = 8.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                faq.paragraphs.forEach { para ->
                    Text(
                        text = para,
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextPrimary
                    )
                }
            }
        }
    }
}

@Composable
private fun ChartReferencePill(chartReference: String) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = IndigoSurface.copy(alpha = 0.72f),
        border = BorderStroke(1.dp, DeepIndigo.copy(alpha = 0.16f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(horizontal = 10.dp, vertical = 7.dp)) {
            Text(
                text = stringResource(R.string.cat_chart_reference_label),
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = SaffronDark
            )
            Spacer(Modifier.height(2.dp))
            Text(
                text = chartReference,
                style = MaterialTheme.typography.bodySmall,
                color = DeepIndigo
            )
        }
    }
}

@Composable
private fun CategoryRemedyBlock(title: String, body: String) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(CreamBackground, RoundedCornerShape(10.dp))
            .border(1.dp, CreamBorder, RoundedCornerShape(10.dp))
            .padding(12.dp)
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
            color = SaffronDark
        )
        Spacer(Modifier.height(3.dp))
        Text(
            text = body,
            style = MaterialTheme.typography.bodyMedium,
            color = TextPrimary
        )
    }
}

private fun categoryIcon(cat: ReadingCategory): ImageVector = when (cat) {
    ReadingCategory.CAREER -> Icons.Default.WorkOutline
    ReadingCategory.WEALTH -> Icons.Default.AccountBalanceWallet
    ReadingCategory.MARRIAGE -> Icons.Default.Favorite
    ReadingCategory.HEALTH -> Icons.Default.HealthAndSafety
    ReadingCategory.DHARMA -> Icons.Default.AutoAwesome
}
