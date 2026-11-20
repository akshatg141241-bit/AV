package com.aryaleelastudios.kundaliguru.data.model

enum class Gender(val displayNameEn: String, val displayNameHi: String) {
    MALE("Male", "पुरुष"),
    FEMALE("Female", "महिला"),
    OTHER("Other", "अन्य"),
    PREFER_NOT_TO_SAY("Prefer not to say", "नहीं बताना चाहते")
}

enum class ChartStyle(val displayNameEn: String, val displayNameHi: String) {
    NORTH_INDIAN("North Indian", "उत्तर भारतीय"),
    SOUTH_INDIAN("South Indian", "दक्षिण भारतीय")
}

enum class AyanamsaType(val displayNameEn: String, val displayNameHi: String) {
    LAHIRI("Lahiri (Chitrapaksha)", "लाहिड़ी (चित्रापक्ष)"),
    RAMAN("B.V. Raman", "बी.वी. रमण"),
    KP("Krishnamurti (KP)", "कृष्णमूर्ति (के.पी.)")
}

data class PlanetPosition(
    val nameEn: String,
    val nameHi: String,
    val glyph: String,
    val longitude: Double,
    val rashiIndex: Int, // 0 to 11
    val rashiNameEn: String,
    val rashiNameHi: String,
    val degreeInRashi: Double,
    val formattedDegree: String,
    val isRetrograde: Boolean,
    val nakshatraIndex: Int, // 0 to 26
    val nakshatraNameEn: String,
    val nakshatraNameHi: String,
    val nakshatraLordEn: String,
    val nakshatraLordHi: String,
    val pada: Int, // 1 to 4
    var houseNumber: Int = 1 // 1 to 12
)

data class LagnaInfo(
    val longitude: Double,
    val rashiIndex: Int,
    val rashiNameEn: String,
    val rashiNameHi: String,
    val degreeInRashi: Double,
    val formattedDegree: String,
    val nakshatraIndex: Int,
    val nakshatraNameEn: String,
    val nakshatraNameHi: String,
    val pada: Int
)

data class HouseCusp(
    val houseNumber: Int,
    val rashiIndex: Int,
    val rashiNameEn: String,
    val rashiNameHi: String,
    val degree: Double,
    val formattedDegree: String
)

data class DashaPeriod(
    val planetEn: String,
    val planetHi: String,
    val startDate: String,
    val endDate: String,
    val subPeriods: List<DashaPeriod> = emptyList()
)

data class PanchangInfo(
    val tithiEn: String,
    val tithiHi: String,
    val pakshaEn: String,
    val pakshaHi: String,
    val varaEn: String,
    val varaHi: String,
    val nakshatraEn: String,
    val nakshatraHi: String,
    val yogaEn: String,
    val yogaHi: String,
    val karanaEn: String,
    val karanaHi: String,
    val sunrise: String,
    val sunset: String
)

data class YogaDoshaStatus(
    val nameEn: String,
    val nameHi: String,
    val isPresent: Boolean,
    val statusTextEn: String,
    val statusTextHi: String,
    val descriptionEn: String,
    val descriptionHi: String,
    val remedyEn: String,
    val remedyHi: String
)

data class DivisionalPlacement(
    val planetEn: String,
    val planetHi: String,
    val signIndex: Int,
    val signNameEn: String,
    val signNameHi: String
)

data class DivisionalChart(
    val code: String, // D9, D10, D7
    val titleEn: String,
    val titleHi: String,
    val descriptionEn: String,
    val descriptionHi: String,
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

data class KundaliData(
    val lagna: LagnaInfo,
    val planets: List<PlanetPosition>,
    val houses: List<HouseCusp>,
    val panchang: PanchangInfo,
    val vimshottariDasha: List<DashaPeriod>,
    val currentDashaSummaryEn: String,
    val currentDashaSummaryHi: String,
    val yogasAndDoshas: List<YogaDoshaStatus>,
    val divisionalCharts: List<DivisionalChart>,
    val personalityReadingEn: String,
    val personalityReadingHi: String,
    val careerReadingEn: String,
    val careerReadingHi: String,
    val relationshipReadingEn: String,
    val relationshipReadingHi: String,
    val healthReadingEn: String,
    val healthReadingHi: String,
    val luckyElements: LuckyElements,
    val remedies: Remedies,
    val dailyPredictionEn: String,
    val dailyPredictionHi: String,
    val monthlyPredictionEn: String,
    val monthlyPredictionHi: String,
    val yearlyPredictionEn: String,
    val yearlyPredictionHi: String,
    val lifePredictionEn: String,
    val lifePredictionHi: String
)

data class KootaItem(
    val nameEn: String,
    val nameHi: String,
    val score: Double,
    val maxScore: Double,
    val meaningEn: String,
    val meaningHi: String
)

data class AshtaKootaResult(
    val groomName: String,
    val brideName: String,
    val groomRashiEn: String,
    val groomRashiHi: String,
    val groomNakshatraEn: String,
    val groomNakshatraHi: String,
    val brideRashiEn: String,
    val brideRashiHi: String,
    val brideNakshatraEn: String,
    val brideNakshatraHi: String,
    val totalScore: Double,
    val maxScore: Double = 36.0,
    val verdictEn: String,
    val verdictHi: String,
    val verdictDetailEn: String,
    val verdictDetailHi: String,
    val kootas: List<KootaItem>,
    val mangalDoshaAnalysisEn: String,
    val mangalDoshaAnalysisHi: String
)
