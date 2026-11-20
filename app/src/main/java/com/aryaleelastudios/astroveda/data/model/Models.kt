package com.aryaleelastudios.astroveda.data.model

import androidx.annotation.StringRes
import com.aryaleelastudios.astroveda.R

enum class Gender(val storageKey: String, @StringRes val labelResId: Int) {
    MALE("Male", R.string.gender_male),
    FEMALE("Female", R.string.gender_female),
    OTHER("Other", R.string.gender_other),
    PREFER_NOT_TO_SAY("Prefer not to say", R.string.gender_prefer_not);

    companion object {
        fun fromStorageKey(key: String): Gender =
            entries.find { it.storageKey.equals(key, ignoreCase = true) } ?: PREFER_NOT_TO_SAY
    }
}

enum class ChartStyle(
    @StringRes val labelResId: Int,
    @StringRes val shortLabelResId: Int
) {
    NORTH_INDIAN(R.string.chart_style_north_full, R.string.chart_style_north),
    SOUTH_INDIAN(R.string.chart_style_south_full, R.string.chart_style_south)
}

enum class AyanamsaType(@StringRes val labelResId: Int) {
    LAHIRI(R.string.ayanamsa_lahiri),
    RAMAN(R.string.ayanamsa_raman),
    KP(R.string.ayanamsa_kp)
}

enum class PredictionPeriod(@StringRes val labelResId: Int) {
    DAILY(R.string.period_daily),
    MONTHLY(R.string.period_monthly),
    YEARLY(R.string.period_yearly),
    LIFE(R.string.period_life)
}

data class PlanetPosition(
    val canonicalName: String, // Language-neutral identifier ("Sun", "Moon", etc.)
    val name: String,
    val glyph: String,
    val longitude: Double,
    val rashiIndex: Int, // 0 to 11
    val rashiName: String,
    val degreeInRashi: Double,
    val formattedDegree: String,
    val isRetrograde: Boolean,
    val nakshatraIndex: Int, // 0 to 26
    val nakshatraName: String,
    val nakshatraLord: String,
    val pada: Int, // 1 to 4
    var houseNumber: Int = 1 // 1 to 12
)

data class LagnaInfo(
    val longitude: Double,
    val rashiIndex: Int,
    val rashiName: String,
    val degreeInRashi: Double,
    val formattedDegree: String,
    val nakshatraIndex: Int,
    val nakshatraName: String,
    val pada: Int
)

data class HouseCusp(
    val houseNumber: Int,
    val rashiIndex: Int,
    val rashiName: String,
    val degree: Double,
    val formattedDegree: String
)

data class DashaPeriod(
    val canonicalLord: String, // Language-neutral ("Ketu", "Venus", ...)
    val planet: String,
    val startDate: String,
    val endDate: String,
    val startYear: Int,
    val endYear: Int,
    val subPeriods: List<DashaPeriod> = emptyList()
)

data class PanchangInfo(
    val tithi: String,
    val paksha: String,
    val vara: String,
    val nakshatra: String,
    val yoga: String,
    val karana: String,
    val sunrise: String,
    val sunset: String
)

data class YogaDoshaStatus(
    val canonicalKey: String, // "Gajakesari", "Mangal", "KaalSarp", "SadeSati", "Kemadruma"
    val name: String,
    val isPresent: Boolean,
    val isCancelled: Boolean = false,
    val statusText: String,
    val description: String,
    val remedy: String
)

data class DivisionalPlacement(
    val canonicalPlanet: String,
    val planet: String,
    val signIndex: Int,
    val signName: String
)

data class DivisionalChart(
    val code: String, // D9, D10, D7
    val title: String,
    val description: String,
    val placements: List<DivisionalPlacement>
)

data class LuckyElements(
    val number: Int,
    val color: String,
    val day: String,
    val direction: String,
    val gemstone: String
)

data class Remedies(
    val mantra: String,
    val charity: String,
    val fasting: String,
    val behavioral: String
)

data class PredictionSelection(
    val id: String,
    val theme: String,
    val text: String,
    val tags: List<String>
)

data class CosmicHighlight(
    val text: String,
    val isStrong: Boolean
)

data class CosmicHighlightBar(
    val domainKey: String,
    val label: String,
    val score: Int, // 0..100
    val summary: String,
    val isStrong: Boolean,
    val glyph: String = "✦"
)

enum class ReadingCategory(
    @StringRes val labelResId: Int,
    @StringRes val shortTabResId: Int,
    val divisionalChartCode: String,
    val absorbedTopicIds: List<String>
) {
    CAREER(
        labelResId = R.string.cat_career_title,
        shortTabResId = R.string.cat_career_tab,
        divisionalChartCode = "D10",
        absorbedTopicIds = listOf("career_field", "govt_job", "job_vs_business", "job_promotion")
    ),
    WEALTH(
        labelResId = R.string.cat_wealth_title,
        shortTabResId = R.string.cat_wealth_tab,
        divisionalChartCode = "D1",
        absorbedTopicIds = listOf("dhan_yog", "debt_clearance", "property_yog")
    ),
    MARRIAGE(
        labelResId = R.string.cat_marriage_title,
        shortTabResId = R.string.cat_marriage_tab,
        divisionalChartCode = "D9",
        absorbedTopicIds = listOf("marriage_timing", "marriage_type", "mangal_dosha", "children_timing")
    ),
    HEALTH(
        labelResId = R.string.cat_health_title,
        shortTabResId = R.string.cat_health_tab,
        divisionalChartCode = "D1",
        absorbedTopicIds = listOf("health_areas", "sade_sati")
    ),
    DHARMA(
        labelResId = R.string.cat_dharma_title,
        shortTabResId = R.string.cat_dharma_tab,
        divisionalChartCode = "D9",
        absorbedTopicIds = listOf("competitive_exams", "foreign_travel")
    )
}

data class PowerBarMetric(
    val label: String,
    val score: Int, // 0..100
    val note: String,
    val glyph: String = "✦"
)

data class ReasonedPath(
    val title: String,
    val sector: String,
    val astroReason: String
)

data class CategoryArchetype(
    val giftTitle: String,
    val derivationBasis: String,
    val recommendedPaths: List<String>,
    val cautiousZones: List<String>
)

data class AstroProofItem(
    val title: String,
    val placementDetail: String,
    val statusLabel: String,
    val isFavorable: Boolean
)

data class CategoryRemedy(
    val mantra: String,
    val charity: String,
    val ritual: String
)

data class SuperpowerInsight(
    val title: String,
    val placementTag: String,
    val description: String
)

data class GrowthAreaInsight(
    val title: String,
    val placementTag: String,
    val description: String,
    val guidance: String
)

data class AbsorbedTopicReading(
    val topicId: String,
    val title: String,
    val timingWindow: String,
    val body: String,
    val astroBasis: String
)

data class CrossCategoryReference(
    val targetCategory: ReadingCategory,
    val targetLabel: String,
    val title: String,
    val insight: String
)

data class NormalizedMatchCard(
    val id: String,
    val title: String,
    val sector: String,
    val matchPercent: Int, // Normalized 80..95 for top 5
    val description: String,
    val chartReference: String
)

data class BroadScopeItem(
    val title: String,
    val description: String,
    val chartReference: String
)

data class MindsetRuleCard(
    val title: String,
    val body: String,
    val chartReference: String
)

data class CreativeFaqItem(
    val question: String,
    val paragraphs: List<String>,
    val badge: String = ""
)

data class DailyRemedyCard(
    val category: ReadingCategory,
    val categoryLabel: String,
    val mantra: String,
    val why: String,
    val how: String
)

data class CategoryDeepDive(
    val category: ReadingCategory,
    val title: String,
    val overallScore: Int,
    val signatureTitle: String,
    val archetype: CategoryArchetype,
    val narrative: String,
    val diversePaths: List<ReasonedPath>,
    val employmentPaths: List<ReasonedPath>,
    val businessPaths: List<ReasonedPath>,
    val employmentVsBusinessGuidance: String,
    val powerBars: List<PowerBarMetric>,
    val proofItems: List<AstroProofItem>,
    val yogaNotes: List<YogaDoshaStatus>,
    val divisionalChartCode: String,
    val absorbedReadings: List<AbsorbedTopicReading>,
    val crossReferences: List<CrossCategoryReference>,
    val whatToDo: List<String>,
    val safetyAdvice: List<String>,
    val remedy: CategoryRemedy,
    val personalityParagraph: String = "",
    val strengthsParagraph: String = "",
    val challengesParagraph: String = "",
    val superpower: SuperpowerInsight = SuperpowerInsight("", "", ""),
    val growthArea: GrowthAreaInsight = GrowthAreaInsight("", "", "", ""),
    val heroSummary: String = "",
    val astroMathPill: String = "",
    val topJobMatches: List<NormalizedMatchCard> = emptyList(),
    val topBusinessMatches: List<NormalizedMatchCard> = emptyList(),
    val topWealthMatches: List<NormalizedMatchCard> = emptyList(),
    val broadScopes: List<BroadScopeItem> = emptyList(),
    val tier1Cards: List<NormalizedMatchCard> = emptyList(),
    val tier2Card: BroadScopeItem? = null,
    val tier2Badge: String = "",
    val tier3SectionTitle: String = "",
    val tier3Cards: List<MindsetRuleCard> = emptyList(),
    val creativeFaqs: List<CreativeFaqItem> = emptyList(),
    val isAgeGatedFutureFocus: Boolean = false,
    val medicalDisclaimer: String = "",
    val heroGlyph: String = "✦",
    val storyParagraphs: List<String> = emptyList()
)

data class MonthRange(
    val startMonth: Int,
    val endMonth: Int,
    val label: String,
    val reason: String
)

data class MonthForecast(
    val monthIndex: Int,
    val monthName: String,
    val dashaSubPeriod: String,
    val moonTransitFlavor: String,
    val prediction: String,
    val luckyFocus: String,
    val effortFocus: String
)

data class YearCategoryDeepDive(
    val category: ReadingCategory,
    val categoryLabel: String,
    val score: Int,
    val chartCode: String,
    val goldenWindows: List<MonthRange>,
    val effortWindows: List<MonthRange>,
    val paragraphs: List<String>,
    val remedy2027: String
)

data class YearForecast(
    val year: Int,
    val personName: String,
    val signatureHeadline: String,
    val heroGlyph: String,
    val heroSummary: String,
    val mahaLord: String,
    val antarLords: List<String>,
    val dashaTimeline2027: List<DashaPeriod>,
    val overallScore: Int,
    val atAGlanceMeters: List<PowerBarMetric>,
    val goldenWindows: List<MonthRange>,
    val effortWindows: List<MonthRange>,
    val categoryDeepDives: List<YearCategoryDeepDive>,
    val monthlyForecasts: List<MonthForecast>,
    val remediesSummary: List<DailyRemedyCard>,
    val disclaimer: String
)

data class KundaliData(
    val lagna: LagnaInfo,
    val planets: List<PlanetPosition>,
    val houses: List<HouseCusp>,
    val panchang: PanchangInfo,
    val vimshottariDasha: List<DashaPeriod>,
    val currentDashaSummary: String,
    val currentMahaLordCanonical: String,
    val currentAntarLordCanonical: String,
    val yogasAndDoshas: List<YogaDoshaStatus>,
    val divisionalCharts: List<DivisionalChart>,
    val cosmicHighlights: List<CosmicHighlight> = emptyList(),
    val cosmicHighlightBars: List<CosmicHighlightBar> = emptyList(),
    val cosmicBlueprint: String = "",
    val categoryDeepDives: Map<ReadingCategory, CategoryDeepDive> = emptyMap(),
    val dailyRemedies: List<DailyRemedyCard> = emptyList(),
    val personalityReading: String,
    val careerReading: String,
    val relationshipReading: String,
    val healthReading: String,
    val luckyElements: LuckyElements,
    val remedies: Remedies,
    val dailyPrediction: PredictionSelection,
    val monthlyPrediction: PredictionSelection,
    val yearlyPrediction: PredictionSelection,
    val lifePrediction: PredictionSelection
) {
    fun predictionFor(period: PredictionPeriod): PredictionSelection = when (period) {
        PredictionPeriod.DAILY -> dailyPrediction
        PredictionPeriod.MONTHLY -> monthlyPrediction
        PredictionPeriod.YEARLY -> yearlyPrediction
        PredictionPeriod.LIFE -> lifePrediction
    }
}

data class KootaItem(
    val name: String,
    val score: Double,
    val maxScore: Double,
    val meaning: String
)

data class AshtaKootaResult(
    val groomName: String,
    val brideName: String,
    val groomRashi: String,
    val groomNakshatra: String,
    val brideRashi: String,
    val brideNakshatra: String,
    val totalScore: Double,
    val maxScore: Double = 36.0,
    val verdict: String,
    val verdictDetail: String,
    val kootas: List<KootaItem>,
    val mangalDoshaAnalysis: String
)
