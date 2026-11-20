package com.aryaleelastudios.astroveda.astro

import com.aryaleelastudios.astroveda.data.model.AbsorbedTopicReading
import com.aryaleelastudios.astroveda.data.model.AstroProofItem
import com.aryaleelastudios.astroveda.data.model.BroadScopeItem
import com.aryaleelastudios.astroveda.data.model.CategoryArchetype
import com.aryaleelastudios.astroveda.data.model.CategoryDeepDive
import com.aryaleelastudios.astroveda.data.model.CategoryRemedy
import com.aryaleelastudios.astroveda.data.model.CosmicHighlightBar
import com.aryaleelastudios.astroveda.data.model.CreativeFaqItem
import com.aryaleelastudios.astroveda.data.model.CrossCategoryReference
import com.aryaleelastudios.astroveda.data.model.DailyRemedyCard
import com.aryaleelastudios.astroveda.data.model.DashaPeriod
import com.aryaleelastudios.astroveda.data.model.DivisionalChart
import com.aryaleelastudios.astroveda.data.model.GrowthAreaInsight
import com.aryaleelastudios.astroveda.data.model.HouseCusp
import com.aryaleelastudios.astroveda.data.model.LagnaInfo
import com.aryaleelastudios.astroveda.data.model.MindsetRuleCard
import com.aryaleelastudios.astroveda.data.model.NormalizedMatchCard
import com.aryaleelastudios.astroveda.data.model.PlanetPosition
import com.aryaleelastudios.astroveda.data.model.PowerBarMetric
import com.aryaleelastudios.astroveda.data.model.ReadingCategory
import com.aryaleelastudios.astroveda.data.model.ReasonedPath
import com.aryaleelastudios.astroveda.data.model.SuperpowerInsight
import com.aryaleelastudios.astroveda.data.model.YogaDoshaStatus
import com.aryaleelastudios.astroveda.data.repository.ContentRepository
import kotlin.math.roundToInt

object CategoryReadingEngine {

    private val rashiLords = listOf(
        "Mars", "Venus", "Mercury", "Moon",
        "Sun", "Mercury", "Venus", "Mars",
        "Jupiter", "Saturn", "Saturn", "Jupiter"
    )

    private val exaltedSigns = mapOf(
        "Sun" to 0, "Moon" to 1, "Mars" to 9, "Mercury" to 5,
        "Jupiter" to 3, "Venus" to 11, "Saturn" to 6, "Rahu" to 1, "Ketu" to 7
    )

    private val debilitatedSigns = mapOf(
        "Sun" to 6, "Moon" to 7, "Mars" to 3, "Mercury" to 11,
        "Jupiter" to 9, "Venus" to 5, "Saturn" to 0, "Rahu" to 7, "Ketu" to 1
    )

    private val ownSigns = mapOf(
        "Sun" to setOf(4),
        "Moon" to setOf(3),
        "Mars" to setOf(0, 7),
        "Mercury" to setOf(2, 5),
        "Jupiter" to setOf(8, 11),
        "Venus" to setOf(1, 6),
        "Saturn" to setOf(9, 10)
    )

    private val benefics = setOf("Jupiter", "Venus", "Moon", "Mercury")
    private val kendraHouses = setOf(1, 4, 7, 10)
    private val trikonaHouses = setOf(1, 5, 9)
    private val dusthanaHouses = setOf(6, 8, 12)

    private class ChartContext(
        val lagna: LagnaInfo,
        val planets: List<PlanetPosition>,
        val houses: List<HouseCusp>,
        val yogasAndDoshas: List<YogaDoshaStatus>,
        val divisionalCharts: List<DivisionalChart>,
        val mahaLordCanonical: String,
        val antarLordCanonical: String,
        val langCode: String,
        val contentRepo: ContentRepository? = null,
        val vimshottariDasha: List<DashaPeriod> = emptyList(),
        val referenceYear: Int = 2026,
        val ageYears: Int = 28
    ) {
        val isHi: Boolean = langCode == "hi"
        private val planetByCanon = planets.associateBy { it.canonicalName }
        private val yogaByKey = yogasAndDoshas.associateBy { it.canonicalKey }

        fun ordinalHouse(houseNum: Int): String {
            contentRepo?.let { return it.getOrdinalHouseName(langCode, houseNum) }
            val n = houseNum.coerceIn(1, 12)
            return if (isHi) {
                val hiOrdinals = listOf(
                    "प्रथम भाव", "द्वितीय भाव", "तृतीय भाव", "चतुर्थ भाव",
                    "पंचम भाव", "षष्ठ भाव", "सप्तम भाव", "अष्टम भाव",
                    "नवम भाव", "दशम भाव", "एकादश भाव", "द्वादश भाव"
                )
                hiOrdinals[n - 1]
            } else {
                val suf = when {
                    n in 11..13 -> "th"
                    n % 10 == 1 -> "st"
                    n % 10 == 2 -> "nd"
                    n % 10 == 3 -> "rd"
                    else -> "th"
                }
                "$n$suf House"
            }
        }

        fun planet(canon: String): PlanetPosition? = planetByCanon[canon]

        fun planetName(canon: String): String = planetByCanon[canon]?.name ?: canon

        fun rashiName(rashiIdx: Int): String =
            houses.firstOrNull { it.rashiIndex == rashiIdx }?.rashiName
                ?: AstroEngine.CANONICAL_RASHIS.getOrElse(rashiIdx) { "Aries" }

        fun houseLordCanon(houseNum: Int): String {
            val signIdx = (lagna.rashiIndex + (houseNum - 1)) % 12
            return rashiLords[signIdx]
        }

        fun planetsInHouse(houseNum: Int): List<PlanetPosition> =
            planets.filter { it.houseNumber == houseNum }

        fun divLagnaSign(code: String): Int {
            val div = divisionalCharts.firstOrNull { it.code == code } ?: return lagna.rashiIndex
            return div.placements.firstOrNull { it.canonicalPlanet == "Lagna" }?.signIndex ?: lagna.rashiIndex
        }

        fun divSign(code: String, canonPlanet: String): Int {
            val div = divisionalCharts.firstOrNull { it.code == code }
            return div?.placements?.firstOrNull { it.canonicalPlanet == canonPlanet }?.signIndex
                ?: (planet(canonPlanet)?.rashiIndex ?: lagna.rashiIndex)
        }

        fun divHouse(code: String, canonPlanet: String): Int {
            val l = divLagnaSign(code)
            val s = divSign(code, canonPlanet)
            return ((s - l + 12) % 12) + 1
        }

        fun isExalted(canon: String, signIdx: Int): Boolean = exaltedSigns[canon] == signIdx
        fun isOwnSign(canon: String, signIdx: Int): Boolean = ownSigns[canon]?.contains(signIdx) == true
        fun isDebilitated(canon: String, signIdx: Int): Boolean = debilitatedSigns[canon] == signIdx

        fun planetScore(canon: String, divCode: String = "D10"): Int {
            val p = planet(canon) ?: return 52
            var s = 50
            when {
                isExalted(canon, p.rashiIndex) -> s += 26
                isOwnSign(canon, p.rashiIndex) -> s += 20
                isDebilitated(canon, p.rashiIndex) -> s -= 18
                else -> s += 4
            }
            when {
                p.houseNumber in trikonaHouses -> s += 14
                p.houseNumber in kendraHouses -> s += 12
                p.houseNumber in setOf(2, 11, 3) -> s += 8
                p.houseNumber == 6 && canon in setOf("Saturn", "Mars", "Rahu") -> s += 6
                p.houseNumber in dusthanaHouses -> s -= 11
            }
            if (divCode != "D1") {
                val dSign = divSign(divCode, canon)
                val dHouse = divHouse(divCode, canon)
                when {
                    isExalted(canon, dSign) || isOwnSign(canon, dSign) -> s += 10
                    dHouse in kendraHouses || dHouse in trikonaHouses -> s += 6
                    dHouse in dusthanaHouses -> s -= 5
                }
            }
            if (canon == mahaLordCanonical) s += 8
            if (canon == antarLordCanonical) s += 5
            return s.coerceIn(22, 96)
        }

        fun houseStrength(houseNum: Int, divCode: String = "D10"): Int {
            val lordScore = planetScore(houseLordCanon(houseNum), divCode)
            val occupants = planetsInHouse(houseNum)
            val beneficBoost = occupants.count { it.canonicalName in benefics } * 8
            val maleficAdjust = occupants.count { it.canonicalName !in benefics } *
                if (houseNum in setOf(3, 6, 10, 11)) 4 else -4
            return (lordScore + beneficBoost + maleficAdjust).coerceIn(24, 96)
        }

        fun yogaPresent(key: String): Boolean = yogaByKey[key]?.isPresent == true
        fun yoga(key: String): YogaDoshaStatus? = yogaByKey[key]

        fun dignityBadge(canon: String): Pair<String, Boolean> {
            val p = planet(canon) ?: return (if (isHi) "संतुलित" else "Balanced") to true
            return when {
                isExalted(canon, p.rashiIndex) -> (if (isHi) "उच्च (प्रबल)" else "Exalted (Strong)") to true
                isOwnSign(canon, p.rashiIndex) -> (if (isHi) "स्वराशि (अनुकूल)" else "Own Sign (Favorable)") to true
                p.houseNumber in kendraHouses || p.houseNumber in trikonaHouses || p.houseNumber in setOf(2, 11) ->
                    (if (isHi) "शुभ स्थान" else "Supportive House") to true
                isDebilitated(canon, p.rashiIndex) ->
                    (if (isHi) "अतिरिक्त प्रयास आवश्यक" else "Requires extra effort") to false
                p.houseNumber in dusthanaHouses ->
                    (if (isHi) "धैर्य व योजना आवश्यक" else "Needs patience") to false
                else -> (if (isHi) "स्थिर संयोग" else "Steady Alignment") to true
            }
        }

        fun placementTag(canon: String): String {
            val p = planet(canon)
            val house = p?.houseNumber ?: 1
            val sign = p?.rashiName ?: lagna.rashiName
            return if (isHi) {
                "${planetName(canon)} • भाव $house ($sign)"
            } else {
                "${planetName(canon)} • House $house ($sign)"
            }
        }

        fun whyChartClause(roleLabelEn: String, roleLabelHi: String, canon: String): String {
            val p = planet(canon)
            val h = ordinalHouse(p?.houseNumber ?: 1)
            val rashi = p?.rashiName ?: lagna.rashiName
            val pDisp = planetName(canon)
            return if (isHi) {
                "क्योंकि आपके $roleLabelHi $pDisp $h ($rashi) में स्थित हैं"
            } else {
                "Because your $roleLabelEn $pDisp is placed in your $h ($rashi)"
            }
        }
    }

    internal fun planetGlyph(canon: String): String = when (canon) {
        "Sun" -> "☉"
        "Moon" -> "☽"
        "Mars" -> "♂"
        "Mercury" -> "☿"
        "Jupiter" -> "♃"
        "Venus" -> "♀"
        "Saturn" -> "♄"
        "Rahu" -> "☊"
        "Ketu" -> "☋"
        else -> "✦"
    }

    internal fun universalSignatureHeadline(
        category: ReadingCategory,
        planetCanon: String,
        isHi: Boolean
    ): String {
        return when (category) {
            ReadingCategory.CAREER -> when (planetCanon) {
                "Sun" -> if (isHi) "तेजस्वी नेतृत्वकर्ता" else "The Radiant Sovereign"
                "Moon" -> if (isHi) "संवेदनशील मार्गदर्शक" else "The Intuitive Steward"
                "Mars" -> if (isHi) "निर्भीक अग्रदूत" else "The Fearless Pioneer"
                "Mercury" -> if (isHi) "तीक्ष्ण रणनीतिकार" else "The Sharp Strategist"
                "Jupiter" -> if (isHi) "विवेकशील गुरु" else "The Wise Mentor"
                "Venus" -> if (isHi) "सृजनात्मक दृष्टा" else "The Creative Visionary"
                "Saturn" -> if (isHi) "अडिग वास्तुकार" else "The Steadfast Architect"
                "Rahu" -> if (isHi) "नवोन्मेषी अन्वेषक" else "The Visionary Trailblazer"
                else -> if (isHi) "मौन पारखी साधक" else "The Quiet Master"
            }
            ReadingCategory.WEALTH -> when (planetCanon) {
                "Sun" -> if (isHi) "गौरवशाली कोषाध्यक्ष" else "The Dignified Steward"
                "Moon" -> if (isHi) "सौम्य संचयक" else "The Balanced Nurturer"
                "Mars" -> if (isHi) "ठोस संपत्ति निर्माता" else "The Bold Asset Builder"
                "Mercury" -> if (isHi) "चतुर मूल्य-निर्माता" else "The Nimble Multiplier"
                "Jupiter" -> if (isHi) "उदार समृद्धि-शिल्पी" else "The Generous Architect"
                "Venus" -> if (isHi) "सुरुचिपूर्ण समृद्ध" else "The Graceful Patron"
                "Saturn" -> if (isHi) "धैर्यवान संचय-शिल्पी" else "The Patient Compounder"
                "Rahu" -> if (isHi) "आधुनिक अवसर-दृष्टा" else "The Modern Catalyst"
                else -> if (isHi) "सचेत मूल्य-संरक्षक" else "The Discerning Keeper"
            }
            ReadingCategory.MARRIAGE -> when (planetCanon) {
                "Sun" -> if (isHi) "निष्ठावान गौरव साथी" else "The Noble Companion"
                "Moon" -> if (isHi) "गहन स्नेह-संरक्षक" else "The Tender Heart"
                "Mars" -> if (isHi) "उत्साही समर्पित संगी" else "The Passionate Loyalist"
                "Mercury" -> if (isHi) "सहज संवाद-मित्र" else "The Playful Confidant"
                "Jupiter" -> if (isHi) "उदार धर्म-संगी" else "The Gracious Anchor"
                "Venus" -> if (isHi) "सौहार्दपूर्ण हृदय-शिल्पी" else "The Devoted Harmonist"
                "Saturn" -> if (isHi) "अटूट आजीवन संगी" else "The Steadfast Partner"
                "Rahu" -> if (isHi) "जीवंत नव-क्षितिज संगी" else "The Magnetic Explorer"
                else -> if (isHi) "निश्छल आत्मिक साथी" else "The Soulful Kindred"
            }
            ReadingCategory.HEALTH -> when (planetCanon) {
                "Sun" -> if (isHi) "तेजस्वी प्राण-वाहक" else "The Radiant Vitalist"
                "Moon" -> if (isHi) "शांत लय-साधक" else "The Lunar Restorer"
                "Mars" -> if (isHi) "ऊर्जावान शक्ति-साधक" else "The Dynamic Dynamo"
                "Mercury" -> if (isHi) "फुर्तीला संतुलन-दृष्टा" else "The Agile Balancer"
                "Jupiter" -> if (isHi) "प्रफुल्ल आरोग्य-संरक्षक" else "The Thriving Optimist"
                "Venus" -> if (isHi) "सौम्य समरसता-वाहक" else "The Graceful Equilibrist"
                "Saturn" -> if (isHi) "दीर्घायु अनुशासन-शिल्पी" else "The Enduring Pillar"
                "Rahu" -> if (isHi) "अनुकूलनशील स्फूर्ति" else "The Adaptive Spark"
                else -> if (isHi) "निर्मल अंतर्ज्ञान-साधक" else "The Quiet Purist"
            }
            ReadingCategory.DHARMA -> when (planetCanon) {
                "Sun" -> if (isHi) "सत्य-ज्योति वाहक" else "The Luminous Guardian"
                "Moon" -> if (isHi) "करुणामय शरण-दाता" else "The Gentle Sanctuary"
                "Mars" -> if (isHi) "निडर धर्म-योद्धा" else "The Courageous Steward"
                "Mercury" -> if (isHi) "सचेत सत्य-संवादक" else "The Sacred Scribe"
                "Jupiter" -> if (isHi) "विवेकशील धर्म-रक्षक" else "The Wisdom Keeper"
                "Venus" -> if (isHi) "सौंदर्य-भक्ति साधक" else "The Devoted Harmonist"
                "Saturn" -> if (isHi) "अडिग कर्मयोगी" else "The Steadfast Yogi"
                "Rahu" -> if (isHi) "सीमातीत सत्य-खोजी" else "The Fearless Seeker"
                else -> if (isHi) "मौन ध्यान-साधक" else "The Silent Sage"
            }
        }
    }

    private fun buildStoryParagraphs(
        ctx: ChartContext,
        categoryKey: String,
        strongestPlanetCanon: String,
        keyLordCanon: String,
        keyHouseNum: Int,
        divCode: String,
        overallScore: Int
    ): List<String> {
        val repo = ctx.contentRepo ?: return emptyList()
        val keyLordObj = ctx.planet(keyLordCanon)
        val occupiedHouse = (keyLordObj?.houseNumber ?: keyHouseNum).coerceIn(1, 12)
        val signName = keyLordObj?.rashiName ?: ctx.lagna.rashiName
        val divSignIdx = ctx.divLagnaSign(divCode)
        val divSignName = ctx.rashiName(divSignIdx)
        val planetName = ctx.planetName(strongestPlanetCanon)
        val lordName = ctx.planetName(keyLordCanon)
        val houseName = ctx.ordinalHouse(occupiedHouse)
        val mahaName = ctx.planetName(ctx.mahaLordCanonical)
        val synthBucket = when {
            overallScore >= 80 -> "excellent"
            overallScore >= 60 -> "favorable"
            overallScore >= 40 -> "growing"
            else -> "extra_effort"
        }

        fun formatTemplate(raw: String): String = raw
            .replace("{planet}", planetName)
            .replace("{lord}", lordName)
            .replace("{house}", houseName)
            .replace("{sign}", signName)
            .replace("{div_sign}", divSignName)
            .replace("{maha}", mahaName)
            .trim()

        val p1 = formatTemplate(
            repo.resolveCategoryStoryParagraph(categoryKey, ctx.langCode, "planet_p1", strongestPlanetCanon)
        )
        val p2 = formatTemplate(
            repo.resolveCategoryStoryParagraph(categoryKey, ctx.langCode, "house_p2", occupiedHouse.toString())
        )
        val p3 = formatTemplate(
            repo.resolveCategoryStoryParagraph(categoryKey, ctx.langCode, "dasha_p3", ctx.mahaLordCanonical)
        )
        val p4 = formatTemplate(
            repo.resolveCategoryStoryParagraph(categoryKey, ctx.langCode, "synthesis_p4", synthBucket)
        )
        return listOf(p1, p2, p3, p4).filter { it.isNotBlank() }
    }

    private fun findAtmakaraka(planets: List<PlanetPosition>): PlanetPosition? {
        val classicalSeven = setOf("Sun", "Moon", "Mars", "Mercury", "Jupiter", "Venus", "Saturn")
        return planets
            .filter { it.canonicalName in classicalSeven }
            .maxByOrNull { it.degreeInRashi }
    }

    private fun rankAndNormalizeCatalogMatches(
        ctx: ChartContext,
        sectionKey: String,
        divCode: String,
        categoryBoostHouses: Set<Int>,
        minPercent: Int = 80,
        maxPercent: Int = 95,
        topLimit: Int = 5
    ): List<NormalizedMatchCard> {
        val entries = ctx.contentRepo?.resolveCatalogEntries(ctx.langCode, sectionKey).orEmpty()
        if (entries.isEmpty()) return emptyList()

        val scored = entries.map { entry ->
            val p = ctx.planet(entry.planet)
            var raw = ctx.planetScore(entry.planet, divCode).toDouble()
            if (p != null) {
                if (p.houseNumber in entry.preferredHouses) raw += 18.0
                if (p.houseNumber in categoryBoostHouses) raw += 8.0
                val dHouse = ctx.divHouse(divCode, entry.planet)
                if (dHouse in entry.preferredHouses) raw += 7.0
            }
            if (entry.planet == ctx.mahaLordCanonical) raw += 10.0
            if (entry.planet == ctx.antarLordCanonical) raw += 6.0
            val tieBreaker = ((entry.id.hashCode() and 0x7FFF) % 100) * 0.005
            entry to (raw + tieBreaker)
        }.sortedByDescending { it.second }
            .take(topLimit)

        if (scored.isEmpty()) return emptyList()
        val hiRaw = scored.first().second
        val loRaw = scored.last().second
        val span = (hiRaw - loRaw).coerceAtLeast(0.001)

        return scored.mapIndexed { idx, (entry, raw) ->
            val norm = if (hiRaw - loRaw < 0.01) {
                (maxPercent - idx * 3).coerceIn(minPercent, maxPercent)
            } else {
                val ratio = ((raw - loRaw) / span).coerceIn(0.0, 1.0)
                (minPercent + 2 + (ratio * (maxPercent - minPercent - 2))).roundToInt()
                    .coerceIn(minPercent, maxPercent)
            }
            val p = ctx.planet(entry.planet)
            val hNum = p?.houseNumber ?: entry.preferredHouses.firstOrNull() ?: 1
            val rashi = p?.rashiName ?: ctx.lagna.rashiName
            val pName = ctx.planetName(entry.planet)
            val houseOrd = ctx.ordinalHouse(hNum)
            val divTag = if (divCode == "D1") "" else if (ctx.isHi) " ($divCode संयोग सहित)" else " (with $divCode alignment)"
            val chartRef = if (ctx.isHi) {
                "क्योंकि आपका $pName $houseOrd ($rashi) में स्थित होकर ${entry.sector} को बल दे रहा है$divTag।"
            } else {
                "Because your $pName sits in your $houseOrd ($rashi) energizing ${entry.sector}$divTag."
            }
            NormalizedMatchCard(
                id = entry.id,
                title = entry.title,
                sector = entry.sector,
                matchPercent = norm,
                description = entry.description,
                chartReference = chartRef
            )
        }
    }

    private data class ResolvedCategoryLibraryInsights(
        val personalityParagraph: String,
        val strengthsParagraph: String,
        val challengesParagraph: String,
        val superpower: SuperpowerInsight,
        val growthArea: GrowthAreaInsight,
        val strongestPlanet: String,
        val growthPlanet: String,
        val rankedPlanets: List<String>
    )

    private fun resolveCategoryLibraryInsights(
        ctx: ChartContext,
        categoryKey: String,
        personalitySignIndex: Int,
        primaryHouse: Int,
        keyHouses: List<Int>,
        candidatePlanets: List<String>,
        divCode: String,
        fallbackNarrative: String
    ): ResolvedCategoryLibraryInsights {
        val isHi = ctx.isHi
        val repo = ctx.contentRepo

        // 1. Personality by relevant sign index (0..11)
        val personalityText = repo?.resolveCategoryPersonality(
            categoryKey = categoryKey,
            langCode = ctx.langCode,
            signIndex = personalitySignIndex.coerceIn(0, 11)
        )?.takeIf { it.isNotBlank() } ?: fallbackNarrative

        // Rank candidate planets + physical occupants of keyHouses dynamically
        val pool = (candidatePlanets + ctx.planetsInHouse(primaryHouse).map { it.canonicalName } +
            keyHouses.flatMap { h -> ctx.planetsInHouse(h).map { it.canonicalName } }).distinct()

        fun strengthRank(canon: String): Int {
            val p = ctx.planet(canon)
            val house = p?.houseNumber ?: 1
            var score = ctx.planetScore(canon, divCode)
            if (house == primaryHouse) score += 18
            else if (house in keyHouses) score += 10
            if (canon in candidatePlanets.take(2)) score += 6
            return score
        }

        fun growthRank(canon: String): Int {
            val p = ctx.planet(canon)
            val house = p?.houseNumber ?: 1
            var score = ctx.planetScore(canon, divCode)
            if (house in dusthanaHouses) score -= 12
            if (canon in candidatePlanets) score -= 4
            return score
        }

        val primaryOccupant = ctx.planetsInHouse(primaryHouse)
            .maxByOrNull { ctx.planetScore(it.canonicalName, divCode) }
            ?.canonicalName

        val strongestPlanet = pool.maxByOrNull { strengthRank(it) }
            ?: candidatePlanets.firstOrNull()
            ?: "Jupiter"

        val secondaryStrengthPlanet = pool
            .filter { it != strongestPlanet }
            .maxByOrNull { strengthRank(it) }
            ?: candidatePlanets.getOrElse(1) { strongestPlanet }

        val growthPlanet = candidatePlanets
            .filter { it != strongestPlanet }
            .minByOrNull { growthRank(it) }
            ?: candidatePlanets.lastOrNull()
            ?: "Saturn"

        val strongestHouse = ctx.planet(strongestPlanet)?.houseNumber ?: primaryHouse
        val secondaryHouse = ctx.planet(secondaryStrengthPlanet)?.houseNumber ?: primaryHouse
        val growthHouse = ctx.planet(growthPlanet)?.houseNumber ?: 1

        // 2. Strengths Paragraph: pulls exact planet_in_house placement(s), prioritizing primary house occupant (e.g. Moon_10)
        val strengthParts = mutableListOf<String>()
        if (primaryOccupant != null && repo != null) {
            repo.resolveCategoryPlacement(categoryKey, ctx.langCode, primaryOccupant, primaryHouse)
                .takeIf { it.isNotBlank() }?.let { strengthParts += it }
        }
        if (strongestPlanet != primaryOccupant && repo != null) {
            repo.resolveCategoryPlacement(categoryKey, ctx.langCode, strongestPlanet, strongestHouse)
                .takeIf { it.isNotBlank() }?.let { strengthParts += it }
        }
        if (secondaryStrengthPlanet != primaryOccupant && secondaryStrengthPlanet != strongestPlanet && repo != null) {
            repo.resolveCategoryPlacement(categoryKey, ctx.langCode, secondaryStrengthPlanet, secondaryHouse)
                .takeIf { it.isNotBlank() }?.let { strengthParts += it }
        }
        val strengthsText = if (strengthParts.isNotEmpty()) {
            strengthParts.joinToString(" ")
        } else {
            fallbackNarrative
        }

        // 3. Hidden Challenges Paragraph: pulls growth planet house placement + hidden challenge nuance
        val challengeParts = mutableListOf<String>()
        if (repo != null) {
            repo.resolveCategoryPlacement(categoryKey, ctx.langCode, growthPlanet, growthHouse)
                .takeIf { it.isNotBlank() }?.let { challengeParts += it }
            repo.resolveCategoryHiddenChallenge(categoryKey, ctx.langCode, growthPlanet)
                .takeIf { it.isNotBlank() }?.let { challengeParts += it }
        }
        val challengesText = if (challengeParts.isNotEmpty()) {
            challengeParts.joinToString(" ")
        } else {
            if (isHi) {
                "${ctx.placementTag(growthPlanet)} आपको धैर्य, स्पष्ट संवाद और निरंतर अभ्यास के माध्यम से दीर्घकालिक संतुलन विकसित करने की प्रेरणा देता है।"
            } else {
                "${ctx.placementTag(growthPlanet)} invites steady patience, thoughtful pacing, and clear boundaries to turn subtle pressure into lasting mastery."
            }
        }

        // 4. Superpower Card
        val superpower = repo?.resolveCategorySuperpower(
            categoryKey = categoryKey,
            langCode = ctx.langCode,
            planetCanonical = strongestPlanet,
            placementTag = ctx.placementTag(strongestPlanet)
        )?.takeIf { it.title.isNotBlank() && it.description.isNotBlank() }
            ?: SuperpowerInsight(
                title = if (isHi) "${ctx.planetName(strongestPlanet)} की विशिष्ट शक्ति" else "${ctx.planetName(strongestPlanet)} Signature Gift",
                placementTag = ctx.placementTag(strongestPlanet),
                description = strengthsText
            )

        // 5. Growth Area Card
        val growthArea = repo?.resolveCategoryGrowthArea(
            categoryKey = categoryKey,
            langCode = ctx.langCode,
            planetCanonical = growthPlanet,
            placementTag = ctx.placementTag(growthPlanet)
        )?.takeIf { it.title.isNotBlank() && it.description.isNotBlank() }
            ?: GrowthAreaInsight(
                title = if (isHi) "${ctx.planetName(growthPlanet)} का विकास क्षेत्र" else "${ctx.planetName(growthPlanet)} Growth Focus",
                placementTag = ctx.placementTag(growthPlanet),
                description = challengesText,
                guidance = if (isHi) "दैनिक छोटे अनुशासन और सचेत योजना से यह क्षेत्र सहज शक्ति में बदलता है।" else "Build small, consistent daily habits and clear milestones to unlock steady poise here."
            )

        val rankedPlanets = pool.sortedByDescending { strengthRank(it) }

        return ResolvedCategoryLibraryInsights(
            personalityParagraph = personalityText,
            strengthsParagraph = strengthsText,
            challengesParagraph = challengesText,
            superpower = superpower,
            growthArea = growthArea,
            strongestPlanet = strongestPlanet,
            growthPlanet = growthPlanet,
            rankedPlanets = rankedPlanets
        )
    }

    private fun interpolateTokens(
        ctx: ChartContext,
        template: String,
        planetCanon: String,
        fallbackHouse: Int,
        yearRange: String = ""
    ): String {
        val pName = ctx.planetName(planetCanon)
        val houseNum = ctx.planet(planetCanon)?.houseNumber ?: fallbackHouse
        val houseName = ctx.ordinalHouse(houseNum)
        return template
            .replace("{planet}", pName)
            .replace("{house}", houseName)
            .replace("{year_range}", yearRange)
    }

    private fun findTopicTimingWindow(
        ctx: ChartContext,
        timingLords: List<String>
    ): String {
        val refYear = ctx.referenceYear
        val targetSet = timingLords.toSet()

        // 1. Upcoming or active Antardasha within next 12 years matching timingLords
        val antarMatch = ctx.vimshottariDasha.asSequence()
            .filter { it.endYear >= refYear }
            .flatMap { it.subPeriods.asSequence() }
            .firstOrNull { sub ->
                sub.canonicalLord in targetSet &&
                    sub.endYear >= refYear &&
                    sub.startYear <= refYear + 12
            }
        if (antarMatch != null) {
            val s = maxOf(antarMatch.startYear, refYear)
            val e = maxOf(s, antarMatch.endYear)
            return if (s == e) "$s" else "$s–$e"
        }

        // 2. Active or upcoming Mahadasha matching timingLords
        val mahaMatch = ctx.vimshottariDasha.firstOrNull { maha ->
            maha.canonicalLord in targetSet &&
                maha.endYear >= refYear &&
                maha.startYear <= refYear + 18
        }
        if (mahaMatch != null) {
            val s = maxOf(mahaMatch.startYear, refYear)
            val e = minOf(s + 3, mahaMatch.endYear)
            return if (s == e) "$s" else "$s–$e"
        }

        // 3. Any nearest upcoming Antardasha matching timingLords
        val futureAntar = ctx.vimshottariDasha.asSequence()
            .flatMap { it.subPeriods.asSequence() }
            .firstOrNull { it.canonicalLord in targetSet && it.endYear >= refYear }
        if (futureAntar != null) {
            val s = maxOf(futureAntar.startYear, refYear)
            val e = maxOf(s, futureAntar.endYear)
            return if (s == e) "$s" else "$s–$e"
        }

        return "${refYear}–${refYear + 2}"
    }

    private fun computeDynamicPaths(
        ctx: ChartContext,
        categoryKey: String,
        jsonSectionKey: String,
        rankedPlanets: List<String>,
        fallbackHouse: Int,
        fallbackLabels: List<String>,
        careerTrackKey: String? = null
    ): List<ReasonedPath> {
        val repo = ctx.contentRepo
        val result = mutableListOf<ReasonedPath>()
        val seenTitles = mutableSetOf<String>()

        if (repo != null) {
            // Prioritize explicit chart house bonus placements (e.g. Jupiter_4 -> Agriculture & Land Ventures)
            if (categoryKey == "career" && careerTrackKey != null) {
                val triggerPlacements = listOf(
                    "Jupiter" to 4,
                    "Mars" to 4,
                    "Sun" to 10,
                    "Venus" to 10,
                    "Saturn" to 6
                )
                triggerPlacements.forEach { (pCanon, houseTarget) ->
                    val actualHouse = ctx.planet(pCanon)?.houseNumber
                    if (actualHouse == houseTarget) {
                        val tpl = repo.resolveCategoryHouseBonusPath(
                            categoryKey = "career",
                            langCode = ctx.langCode,
                            placementKey = "${pCanon}_$houseTarget",
                            trackKey = careerTrackKey
                        )
                        if (tpl != null && seenTitles.add(tpl.title)) {
                            result += ReasonedPath(
                                title = tpl.title,
                                sector = tpl.sector,
                                astroReason = interpolateTokens(ctx, tpl.reason, pCanon, houseTarget)
                            )
                        }
                    }
                }
            }

            // Collect top 3 ranked active planets' diverse reasoned paths
            rankedPlanets.take(3).forEach { planetCanon ->
                val list = repo.resolveCategoryReasonedPaths(
                    categoryKey = categoryKey,
                    langCode = ctx.langCode,
                    jsonSectionKey = jsonSectionKey,
                    planetCanonical = planetCanon
                )
                list.forEach { tpl ->
                    if (result.size < 6 && seenTitles.add(tpl.title)) {
                        result += ReasonedPath(
                            title = tpl.title,
                            sector = tpl.sector,
                            astroReason = interpolateTokens(ctx, tpl.reason, planetCanon, fallbackHouse)
                        )
                    }
                }
            }
        }

        if (result.isEmpty()) {
            val driver = rankedPlanets.firstOrNull() ?: "Jupiter"
            val houseNum = ctx.planet(driver)?.houseNumber ?: fallbackHouse
            fallbackLabels.take(4).forEach { title ->
                result += ReasonedPath(
                    title = title,
                    sector = if (ctx.isHi) "मुख्य क्षेत्र" else "Core Alignment",
                    astroReason = if (ctx.isHi) {
                        "${ctx.planetName(driver)} की ${ctx.ordinalHouse(houseNum)} में अनुकूल स्थिति के कारण विशेष रूप से लाभकारी।"
                    } else {
                        "Strongly aligned due to ${ctx.planetName(driver)} energizing your ${ctx.ordinalHouse(houseNum)}."
                    }
                )
            }
        }

        return result.take(6)
    }

    private fun computeCautiousZonesAndSafety(
        ctx: ChartContext,
        categoryKey: String,
        growthPlanet: String,
        extraCautious: List<String>,
        extraSafety: List<String>
    ): Pair<List<String>, List<String>> {
        val repo = ctx.contentRepo
        val fromLibCautious = repo?.resolveCategoryStringListByPlanet(
            categoryKey = categoryKey,
            langCode = ctx.langCode,
            sectionKey = "cautious_zones_by_planet",
            planetCanonical = growthPlanet
        ).orEmpty()
        val fromLibSafety = repo?.resolveCategoryStringListByPlanet(
            categoryKey = categoryKey,
            langCode = ctx.langCode,
            sectionKey = "safety_advice_by_planet",
            planetCanonical = growthPlanet
        ).orEmpty()
        val combinedCautious = (fromLibCautious + extraCautious).filter { it.isNotBlank() }.distinct().take(4)
        val combinedSafety = (fromLibSafety + extraSafety).filter { it.isNotBlank() }.distinct().take(4)
        return combinedCautious to combinedSafety
    }

    private data class TopicEvalSpec(
        val topicId: String,
        val score: Int,
        val driverPlanet: String,
        val fallbackHouse: Int,
        val timingLords: List<String>,
        val fallbackTitleEn: String,
        val fallbackTitleHi: String,
        val fallbackBodyEn: String,
        val fallbackBodyHi: String,
        val fallbackBasisEn: String,
        val fallbackBasisHi: String
    )

    private fun computeAbsorbedTopics(
        ctx: ChartContext,
        categoryKey: String,
        specs: List<TopicEvalSpec>
    ): List<AbsorbedTopicReading> {
        val repo = ctx.contentRepo
        return specs.map { spec ->
            val window = findTopicTimingWindow(ctx, spec.timingLords)
            val tpl = repo?.resolveAbsorbedTopic(categoryKey, ctx.langCode, spec.topicId)
            if (tpl != null) {
                val tierText = when {
                    spec.score >= 70 -> tpl.strong
                    spec.score >= 52 -> tpl.balanced
                    else -> tpl.focus
                }.ifBlank { tpl.balanced.ifBlank { tpl.strong } }

                AbsorbedTopicReading(
                    topicId = spec.topicId,
                    title = tpl.title,
                    timingWindow = window,
                    body = interpolateTokens(ctx, tierText, spec.driverPlanet, spec.fallbackHouse, window),
                    astroBasis = interpolateTokens(ctx, tpl.basis, spec.driverPlanet, spec.fallbackHouse, window)
                )
            } else {
                AbsorbedTopicReading(
                    topicId = spec.topicId,
                    title = if (ctx.isHi) spec.fallbackTitleHi else spec.fallbackTitleEn,
                    timingWindow = window,
                    body = interpolateTokens(
                        ctx,
                        if (ctx.isHi) spec.fallbackBodyHi else spec.fallbackBodyEn,
                        spec.driverPlanet,
                        spec.fallbackHouse,
                        window
                    ),
                    astroBasis = interpolateTokens(
                        ctx,
                        if (ctx.isHi) spec.fallbackBasisHi else spec.fallbackBasisEn,
                        spec.driverPlanet,
                        spec.fallbackHouse,
                        window
                    )
                )
            }
        }
    }

    private fun computeCrossReferences(
        ctx: ChartContext,
        categoryKey: String,
        primaryPlanet: String,
        fallbackHouse: Int
    ): List<CrossCategoryReference> {
        val repo = ctx.contentRepo
        val isHi = ctx.isHi
        fun shortLabel(cat: ReadingCategory): String = when (cat) {
            ReadingCategory.CAREER -> if (isHi) "करियर" else "Career"
            ReadingCategory.WEALTH -> if (isHi) "धन" else "Wealth"
            ReadingCategory.MARRIAGE -> if (isHi) "विवाह" else "Marriage"
            ReadingCategory.HEALTH -> if (isHi) "स्वास्थ्य" else "Health"
            ReadingCategory.DHARMA -> if (isHi) "धर्म" else "Dharma"
        }

        val currentCat = when (categoryKey) {
            "career" -> ReadingCategory.CAREER
            "wealth" -> ReadingCategory.WEALTH
            "marriage" -> ReadingCategory.MARRIAGE
            "health" -> ReadingCategory.HEALTH
            else -> ReadingCategory.DHARMA
        }

        return ReadingCategory.entries.filter { it != currentCat }.mapNotNull { targetCat ->
            val resolved = repo?.resolveCrossReference(categoryKey, ctx.langCode, targetCat.name)
            if (resolved != null) {
                CrossCategoryReference(
                    targetCategory = targetCat,
                    targetLabel = shortLabel(targetCat),
                    title = resolved.first,
                    insight = interpolateTokens(ctx, resolved.second, primaryPlanet, fallbackHouse)
                )
            } else null
        }
    }

    fun computeCosmicBlueprint(
        lagna: LagnaInfo,
        planets: List<PlanetPosition>,
        mahaLordCanonical: String,
        categoryDeepDives: Map<ReadingCategory, CategoryDeepDive>,
        langCode: String,
        contentRepo: ContentRepository? = null
    ): String {
        val isHi = langCode == "hi"
        val moon = planets.firstOrNull { it.canonicalName == "Moon" }
        val moonRashiIndex = (moon?.rashiIndex ?: lagna.rashiIndex).coerceIn(0, 11)
        val lagnaIndex = lagna.rashiIndex.coerceIn(0, 11)
        val topCategory = categoryDeepDives.values.maxByOrNull { it.overallScore }?.category ?: ReadingCategory.CAREER

        if (contentRepo != null) {
            val s1 = contentRepo.resolveBlueprintText(langCode, "lagna_themes", lagnaIndex.toString())
            val s2 = contentRepo.resolveBlueprintText(langCode, "moon_themes", moonRashiIndex.toString())
            val s3 = contentRepo.resolveBlueprintText(langCode, "dasha_themes", mahaLordCanonical)
            val s4 = contentRepo.resolveBlueprintText(langCode, "closings", topCategory.name)
            val combined = listOf(s1, s2, s3, s4).filter { it.isNotBlank() }.joinToString(" ")
            if (combined.isNotBlank()) return combined
        }

        val moonSign = moon?.rashiName ?: lagna.rashiName
        val mahaPlanet = planets.firstOrNull { it.canonicalName == mahaLordCanonical }?.name ?: mahaLordCanonical
        return if (isHi) {
            "${lagna.rashiName} लग्न आपके जीवन-पथ को स्पष्ट आत्मविश्वास, मौलिक दृष्टिकोण और आगे बढ़ने की सहज प्रेरणा प्रदान करता है। $moonSign राशि में स्थित चंद्रमा आपकी भावनात्मक सूझबूझ, अंतर्ज्ञान और अपनों के साथ मधुर जुड़ाव को समृद्ध बनाता है। वर्तमान में सक्रिय $mahaPlanet महादशा आपके दीर्घकालिक लक्ष्यों और सार्थक अनुभवों को परिपक्व दिशा दे रही है। समग्र रूप से आपकी कुंडली धैर्य, विवेक और सकारात्मक कर्म के माध्यम से उज्ज्वल उन्नति का सुंदर संदेश देती है।"
        } else {
            "With ${lagna.rashiName} rising as your Ascendant, your life path is illuminated by authentic initiative and quiet inner clarity. Your Moon in $moonSign enriches your emotional wisdom, intuitive perception, and steady rapport with people around you. Your active $mahaPlanet Maha Dasha opens meaningful opportunities to ripen core talents into lasting progress. Across your chart, thoughtful intention and steady perseverance turn everyday efforts into fulfilling milestones."
        }
    }

    fun computeCosmicHighlightBars(
        lagna: LagnaInfo,
        planets: List<PlanetPosition>,
        houses: List<HouseCusp>,
        yogasAndDoshas: List<YogaDoshaStatus>,
        divisionalCharts: List<DivisionalChart>,
        mahaLordCanonical: String,
        antarLordCanonical: String,
        langCode: String,
        categoryDeepDives: Map<ReadingCategory, CategoryDeepDive>
    ): List<CosmicHighlightBar> {
        val ctx = ChartContext(lagna, planets, houses, yogasAndDoshas, divisionalCharts, mahaLordCanonical, antarLordCanonical, langCode)
        val isHi = ctx.isHi

        fun barSummary(category: ReadingCategory, score: Int, deep: CategoryDeepDive): String {
            val gift = deep.archetype.giftTitle
            return if (isHi) {
                when {
                    score >= 76 -> "$gift — उन्नति के प्रबल संकेत और अनुकूल संयोग"
                    score >= 58 -> "$gift — निरंतर प्रयास से सहज प्रगति का समय"
                    else -> "$gift — धैर्य और सुव्यवस्थित योजना से स्थिर लाभ"
                }
            } else {
                when {
                    score >= 76 -> "Strong indication — $gift alignment fuels momentum"
                    score >= 58 -> "Favorable alignment — steady execution builds clear results"
                    else -> "Requires careful planning — patience unlocks steady rewards"
                }
            }
        }

        val specs = listOf(
            Triple("career", if (isHi) "करियर" else "Career", ReadingCategory.CAREER to "♃"),
            Triple("wealth", if (isHi) "धन" else "Wealth", ReadingCategory.WEALTH to "☿"),
            Triple("love", if (isHi) "प्रेम व विवाह" else "Love", ReadingCategory.MARRIAGE to "♀"),
            Triple("health", if (isHi) "स्वास्थ्य" else "Health", ReadingCategory.HEALTH to "☉")
        )

        return specs.map { (key, label, catAndGlyph) ->
            val (cat, glyph) = catAndGlyph
            val deep = categoryDeepDives[cat]!!
            CosmicHighlightBar(
                domainKey = key,
                label = label,
                score = deep.overallScore,
                summary = barSummary(cat, deep.overallScore, deep),
                isStrong = deep.overallScore >= 60,
                glyph = glyph
            )
        }
    }

    fun computeAllCategories(
        lagna: LagnaInfo,
        planets: List<PlanetPosition>,
        houses: List<HouseCusp>,
        yogasAndDoshas: List<YogaDoshaStatus>,
        divisionalCharts: List<DivisionalChart>,
        mahaLordCanonical: String,
        antarLordCanonical: String,
        langCode: String,
        signCareerReading: String,
        signRelationshipReading: String,
        signHealthReading: String,
        contentRepo: ContentRepository? = null,
        vimshottariDasha: List<DashaPeriod> = emptyList(),
        referenceYear: Int = 2026,
        ageYears: Int = 28
    ): Map<ReadingCategory, CategoryDeepDive> {
        val ctx = ChartContext(
            lagna = lagna,
            planets = planets,
            houses = houses,
            yogasAndDoshas = yogasAndDoshas,
            divisionalCharts = divisionalCharts,
            mahaLordCanonical = mahaLordCanonical,
            antarLordCanonical = antarLordCanonical,
            langCode = langCode,
            contentRepo = contentRepo,
            vimshottariDasha = vimshottariDasha,
            referenceYear = referenceYear,
            ageYears = ageYears
        )
        return mapOf(
            ReadingCategory.CAREER to buildCareer(ctx, signCareerReading),
            ReadingCategory.WEALTH to buildWealth(ctx, signCareerReading),
            ReadingCategory.MARRIAGE to buildMarriage(ctx, signRelationshipReading),
            ReadingCategory.HEALTH to buildHealth(ctx, signHealthReading),
            ReadingCategory.DHARMA to buildDharma(ctx)
        )
    }

    fun computeDailyRemedies(
        lagna: LagnaInfo,
        planets: List<PlanetPosition>,
        houses: List<HouseCusp>,
        yogasAndDoshas: List<YogaDoshaStatus>,
        divisionalCharts: List<DivisionalChart>,
        mahaLordCanonical: String,
        antarLordCanonical: String,
        langCode: String,
        categoryDeepDives: Map<ReadingCategory, CategoryDeepDive>,
        contentRepo: ContentRepository? = null
    ): List<DailyRemedyCard> {
        val ctx = ChartContext(
            lagna = lagna,
            planets = planets,
            houses = houses,
            yogasAndDoshas = yogasAndDoshas,
            divisionalCharts = divisionalCharts,
            mahaLordCanonical = mahaLordCanonical,
            antarLordCanonical = antarLordCanonical,
            langCode = langCode,
            contentRepo = contentRepo
        )
        val isHi = ctx.isHi
        val akCanon = findAtmakaraka(planets)?.canonicalName ?: ctx.houseLordCanon(9)

        val specs = listOf(
            Triple(ReadingCategory.CAREER, ctx.houseLordCanon(10), 10),
            Triple(ReadingCategory.WEALTH, ctx.houseLordCanon(2), 2),
            Triple(ReadingCategory.HEALTH, ctx.houseLordCanon(1), 1),
            Triple(ReadingCategory.MARRIAGE, ctx.houseLordCanon(7), 7),
            Triple(ReadingCategory.DHARMA, akCanon, ctx.planet(akCanon)?.houseNumber ?: 9)
        )

        return specs.map { (cat, planetCanon, roleHouse) ->
            val deep = categoryDeepDives[cat]
            val pObj = ctx.planet(planetCanon)
            val actualHouse = pObj?.houseNumber ?: roleHouse
            val houseOrd = ctx.ordinalHouse(actualHouse)
            val rashi = pObj?.rashiName ?: lagna.rashiName
            val pName = ctx.planetName(planetCanon)

            val catLabel = when (cat) {
                ReadingCategory.CAREER -> if (isHi) "करियर" else "Career"
                ReadingCategory.WEALTH -> if (isHi) "धन" else "Wealth"
                ReadingCategory.HEALTH -> if (isHi) "स्वास्थ्य" else "Health"
                ReadingCategory.MARRIAGE -> if (isHi) "विवाह" else "Marriage"
                ReadingCategory.DHARMA -> if (isHi) "धर्म" else "Dharma"
            }

            val roleEn = when (cat) {
                ReadingCategory.CAREER -> "10th Lord"
                ReadingCategory.WEALTH -> "2nd Lord"
                ReadingCategory.HEALTH -> "Ascendant Lord"
                ReadingCategory.MARRIAGE -> "7th Lord"
                ReadingCategory.DHARMA -> "Atmakaraka"
            }
            val roleHi = when (cat) {
                ReadingCategory.CAREER -> "दशमेश"
                ReadingCategory.WEALTH -> "द्वितीयेश"
                ReadingCategory.HEALTH -> "लग्नेश"
                ReadingCategory.MARRIAGE -> "सप्तमेश"
                ReadingCategory.DHARMA -> "आत्मकारक"
            }

            val remedy = deep?.remedy ?: planetRemedy(planetCanon, cat, isHi)
            val why = if (isHi) {
                "क्योंकि आपके $roleHi $pName $houseOrd ($rashi) में स्थित हैं और ${ctx.planetName(mahaLordCanonical)} दशा से जुड़े हैं, यह नियमित अभ्यास इस जीवन क्षेत्र को शुभ ऊर्जा देता है।"
            } else {
                "Because your $roleEn $pName sits in your $houseOrd ($rashi) alongside ${ctx.planetName(mahaLordCanonical)} Dasha, harmonizing this planetary rhythm elevates steady grace."
            }
            val how = "${remedy.ritual} ${remedy.charity}".trim()

            DailyRemedyCard(
                category = cat,
                categoryLabel = catLabel,
                mantra = remedy.mantra,
                why = why,
                how = how
            )
        }
    }

    private fun buildCareer(ctx: ChartContext, baseSignReading: String): CategoryDeepDive {
        val isHi = ctx.isHi
        val lord10 = ctx.houseLordCanon(10)
        val lord2 = ctx.houseLordCanon(2)
        val lord3 = ctx.houseLordCanon(3)
        val lord6 = ctx.houseLordCanon(6)
        val lord7 = ctx.houseLordCanon(7)
        val lord11 = ctx.houseLordCanon(11)
        val d10LagnaSign = ctx.divLagnaSign("D10")
        val d10Sign10 = (d10LagnaSign + 9) % 12

        val candidates = listOf("Mercury", "Venus", "Sun", "Mars", "Jupiter", "Saturn")
        val weightedScores = candidates.associateWith { p ->
            var score = ctx.planetScore(p, "D10")
            if (p == lord10) score += 16
            if (ctx.planet(p)?.houseNumber == 10) score += 14
            if (ctx.planet(p)?.houseNumber == 1) score += 6
            val d10H = ctx.divHouse("D10", p)
            if (d10H == 10 || d10H == 1) score += 10
            if (p == ctx.mahaLordCanonical) score += 6
            score
        }

        val sunMarsCombined = maxOf(weightedScores["Sun"] ?: 0, weightedScores["Mars"] ?: 0)
        val topPlanet = weightedScores.maxByOrNull { it.value }?.key ?: lord10

        val archetypeKey = when {
            topPlanet == "Mercury" -> "ANALYTICAL"
            topPlanet == "Venus" -> "CREATIVE"
            topPlanet == "Jupiter" -> "MENTOR"
            topPlanet == "Saturn" -> "ARCHITECT"
            sunMarsCombined >= (weightedScores["Mercury"] ?: 0) -> "PIONEER"
            else -> "ANALYTICAL"
        }

        val house2Occ = ctx.planetsInHouse(2).joinToString(", ") { it.name }.ifBlank { ctx.planetName(lord2) }
        val house3Occ = ctx.planetsInHouse(3).joinToString(", ") { it.name }.ifBlank { ctx.planetName(lord3) }
        val lord10Planet = ctx.planet(lord10)

        val derivationBasis = if (isHi) {
            "दशमेश: ${ctx.planetName(lord10)} (भाव ${lord10Planet?.houseNumber ?: 10}, ${lord10Planet?.rashiName ?: ""}) • D10 लग्न: ${ctx.rashiName(d10LagnaSign)} • द्वितीय भाव: $house2Occ • तृतीय भाव: $house3Occ"
        } else {
            "10th Lord: ${ctx.planetName(lord10)} (House ${lord10Planet?.houseNumber ?: 10}, ${lord10Planet?.rashiName ?: ""}) • D10 Lagna: ${ctx.rashiName(d10LagnaSign)} (10th: ${ctx.rashiName(d10Sign10)}) • 2nd House: $house2Occ • 3rd House: $house3Occ"
        }

        val giftTitle: String
        val baseRecommendedPaths: List<String>
        val baseCautiousZones: List<String>
        when (archetypeKey) {
            "ANALYTICAL" -> if (isHi) {
                giftTitle = "विश्लेषणात्मक रणनीतिकार"
                baseRecommendedPaths = listOf("टेक्नोलॉजी व सॉफ्टवेयर", "वित्त एवं डेटा विश्लेषण", "अनुसंधान व नीति", "रणनीतिक परामर्श")
                baseCautiousZones = listOf("बिना डेटा के कोल्ड सेल्स", "अव्यवस्थित फील्ड कार्य")
            } else {
                giftTitle = "Analytical Strategist"
                baseRecommendedPaths = listOf("Technology & Architecture", "Finance & Quantitative Analytics", "Research & Deep Tech", "Strategy Consulting")
                baseCautiousZones = listOf("High-Churn Cold Sales", "Unstructured Field Operations")
            }

            "CREATIVE" -> if (isHi) {
                giftTitle = "सृजनात्मक दृष्टा"
                baseRecommendedPaths = listOf("डिज़ाइन व ब्रांड रणनीति", "मीडिया व स्टोरीटेलिंग", "आर्किटेक्चर व कला", "कूटनीति व जनसंपर्क")
                baseCautiousZones = listOf("कठोर क्लर्कियल ऑडिट", "दोहराव वाले नीरस कार्य")
            } else {
                giftTitle = "Creative Visionary"
                baseRecommendedPaths = listOf("Design & Product Storytelling", "Media & Creative Direction", "Architecture & Aesthetics", "Brand Diplomacy")
                baseCautiousZones = listOf("Rigid Routine Auditing", "Isolated Back-Office Clerical Roles")
            }

            "MENTOR" -> if (isHi) {
                giftTitle = "विवेकशील मार्गदर्शक"
                baseRecommendedPaths = listOf("शिक्षा व अकादमिक नेतृत्व", "कानून व प्रशासनिक सलाह", "वेल्थ गवर्नेंस", "एग्जीक्यूटिव कोचिंग")
                baseCautiousZones = listOf("अल्पकालिक सट्टेबाज़ी", "आक्रामक कमीशन-आधारित बिक्री")
            } else {
                giftTitle = "Wisdom Mentor & Advisor"
                baseRecommendedPaths = listOf("Education & Research Leadership", "Legal & Policy Advisory", "Wealth Governance", "Executive Coaching")
                baseCautiousZones = listOf("Impulsive Speculative Trading", "Short-Term Pressure Brokerage")
            }

            "ARCHITECT" -> if (isHi) {
                giftTitle = "व्यवस्थित तंत्र निर्माता"
                baseRecommendedPaths = listOf("इंफ्रास्ट्रक्चर व इंजीनियरिंग", "सप्लाई चेन व ऑपरेशंस", "अनुपालन व न्याय", "दीर्घकालिक डिजिटल उत्पाद")
                baseCautiousZones = listOf("बिना प्रक्रिया वाले अस्थिर कार्य", "जल्दबाज़ी के अल्पकालिक दांव")
            } else {
                giftTitle = "Systems Builder & Architect"
                baseRecommendedPaths = listOf("Core Engineering & Infrastructure", "Operations & Supply Chain", "Governance & Compliance", "Long-Horizon Platforms")
                baseCautiousZones = listOf("Unstructured Day-Trading", "Zero-Process Ad-Hoc Hustles")
            }

            else -> if (isHi) {
                giftTitle = "प्रभावशाली नेतृत्वकर्ता"
                baseRecommendedPaths = listOf("प्रशासन व सिविल सेवा", "उद्यम नेतृत्व", "प्रोजेक्ट ऑपरेशंस", "तकनीकी प्रबंधन")
                baseCautiousZones = listOf("निष्क्रिय पर्दे के पीछे की भूमिका", "सीमित दिनचर्या वाले क्लर्कियल पद")
            } else {
                giftTitle = "Executive Pioneer"
                baseRecommendedPaths = listOf("Leadership & Public Administration", "Entrepreneurship & Ventures", "Operations Management", "Applied Engineering")
                baseCautiousZones = listOf("Passive Siloed Support", "Low-Autonomy Clerical Routines")
            }
        }

        val leadershipBar = ((ctx.planetScore("Sun", "D10") + ctx.houseStrength(10, "D10")) / 2).coerceIn(25, 96)
        val commBar = ((ctx.planetScore("Mercury", "D10") + ctx.houseStrength(3, "D10")) / 2).coerceIn(25, 96)
        val creativityBar = ((ctx.planetScore("Venus", "D10") + ctx.houseStrength(5, "D10")) / 2).coerceIn(25, 96)
        val analyticalBar = ((ctx.planetScore("Jupiter", "D10") + ctx.houseStrength(11, "D10")) / 2).coerceIn(25, 96)
        val resilienceBar = ((ctx.planetScore("Saturn", "D10") + ctx.planetScore("Mars", "D10")) / 2).coerceIn(25, 96)
        val overallScore = ((leadershipBar + commBar + creativityBar + analyticalBar + resilienceBar) / 5).coerceIn(28, 95)

        val powerBars = if (isHi) listOf(
            PowerBarMetric(
                label = "नेतृत्व क्षमता (Leadership)",
                score = leadershipBar,
                note = "${ctx.whyChartClause("सूर्य", "सूर्य", "Sun")} — आप स्वाभाविक रूप से स्पष्ट निर्णय और जिम्मेदारी संभालने की क्षमता रखते हैं।",
                glyph = "☉"
            ),
            PowerBarMetric(
                label = "संवाद कौशल (Communication)",
                score = commBar,
                note = "${ctx.whyChartClause("बुध", "बुध", "Mercury")} — आपकी संवाद और लेखन शैली लोगों को सहज प्रभावित करती है।",
                glyph = "☿"
            ),
            PowerBarMetric(
                label = "रचनात्मकता (Creativity)",
                score = creativityBar,
                note = "${ctx.whyChartClause("शुक्र", "शुक्र", "Venus")} — आपके कार्यों में सौंदर्यबोध और मौलिक प्रस्तुति स्वाभाविक झलकती है।",
                glyph = "♀"
            ),
            PowerBarMetric(
                label = "विश्लेषण शक्ति (Analytical)",
                score = analyticalBar,
                note = "${ctx.whyChartClause("दशमेश", "दशमेश", lord10)} — जटिल विषयों को सुलझाकर दीर्घकालिक रणनीति बनाना आपकी प्रमुख ताकत है।",
                glyph = "♃"
            ),
            PowerBarMetric(
                label = "कर्मठ धैर्य (Resilience)",
                score = resilienceBar,
                note = "${ctx.whyChartClause("शनि", "शनि", "Saturn")} — निरंतर अनुशासन आपको बड़े लक्ष्य तक शांत भाव से पहुँचाता है।",
                glyph = "♄"
            )
        ) else listOf(
            PowerBarMetric(
                label = "Leadership",
                score = leadershipBar,
                note = "${ctx.whyChartClause("Sun", "सूर्य", "Sun")}, your executive poise inspires trust and clear team alignment.",
                glyph = "☉"
            ),
            PowerBarMetric(
                label = "Communication",
                score = commBar,
                note = "${ctx.whyChartClause("Mercury", "बुध", "Mercury")}, ideas translate cleanly into persuasive specs and dialogue.",
                glyph = "☿"
            ),
            PowerBarMetric(
                label = "Creativity",
                score = creativityBar,
                note = "${ctx.whyChartClause("Venus", "शुक्र", "Venus")}, inventive problem-solving and refined taste elevate your craft.",
                glyph = "♀"
            ),
            PowerBarMetric(
                label = "Analytical",
                score = analyticalBar,
                note = "${ctx.whyChartClause("10th Lord", "दशमेश", lord10)}, structured pattern discernment anchors sound decisions.",
                glyph = "♃"
            ),
            PowerBarMetric(
                label = "Resilience",
                score = resilienceBar,
                note = "${ctx.whyChartClause("Saturn", "शनि", "Saturn")}, steady perseverance turns demanding quarters into milestones.",
                glyph = "♄"
            )
        )

        val proofPlanets = listOf(lord10, "Sun", "Mercury", "Saturn", "Jupiter").distinct()
        val proofItems = proofPlanets.map { canon ->
            val p = ctx.planet(canon)
            val d10Sign = ctx.divSign("D10", canon)
            val d10House = ctx.divHouse("D10", canon)
            val (status, ok) = ctx.dignityBadge(canon)
            val roleTag = when {
                canon == lord10 && isHi -> " (दशमेश)"
                canon == lord10 -> " (10th Lord)"
                else -> ""
            }
            AstroProofItem(
                title = "${ctx.planetName(canon)}$roleTag",
                placementDetail = if (isHi) {
                    "भाव ${p?.houseNumber ?: 1} (${p?.rashiName ?: ""}) • D10 भाव $d10House (${ctx.rashiName(d10Sign)})"
                } else {
                    "House ${p?.houseNumber ?: 1} (${p?.rashiName ?: ""}) • D10 House $d10House (${ctx.rashiName(d10Sign)})"
                },
                statusLabel = status,
                isFavorable = ok
            )
        }

        val whatToDo = mutableListOf<String>()
        val extraSafety = mutableListOf<String>()
        if (ctx.planetScore("Mercury", "D10") >= 62) {
            whatToDo += if (isHi) "डेटा, सिस्टम डॉक्यूमेंटेशन और स्पष्ट तर्कों के आधार पर निर्णय नेतृत्व करें।" else "Lead decisions through structured systems, documentation, and analytical frameworks."
            extraSafety += if (isHi) "निर्णय लेते समय समयसीमा का ध्यान रखें और अति-विश्लेषण को संतुलित करें।" else "Balance deep analysis with practical milestone deadlines."
        } else {
            whatToDo += if (isHi) "प्रत्येक महत्वपूर्ण अनुबंध और ईमेल की लिखित जाँच सूची (checklist) बनाकर काम करें।" else "Use written checklists and double-check contracts before committing."
            extraSafety += if (isHi) "बड़े कार्यभार स्वीकार करने से पहले सभी शर्तों को लिखित रूप में स्पष्ट करें।" else "Document scope and deliverables clearly in writing rather than relying on verbal assumptions."
        }
        if (ctx.planetScore("Venus", "D10") >= 62) {
            whatToDo += if (isHi) "डिज़ाइन, प्रस्तुति और सौहार्दपूर्ण संबंधों से अपने प्रोजेक्ट का प्रभाव बढ़ाएँ।" else "Leverage presentation quality, aesthetics, and diplomatic stakeholder rapport."
        }
        if (ctx.planetScore("Saturn", "D10") >= 60) {
            whatToDo += if (isHi) "दीर्घकालिक प्रक्रियाओं, मेंटरशिप और निरंतर गुणवत्ता पर ध्यान केंद्रित करें।" else "Build compounding long-term workflows and own high-reliability deliverables."
        } else {
            whatToDo += if (isHi) "बड़े लक्ष्यों को साप्ताहिक छोटे मील के पत्थरों में बाँटकर नियमित गति बनाए रखें।" else "Break multi-quarter goals into structured weekly milestones for steady traction."
        }
        if (ctx.planetScore("Sun", "D10") < 58) {
            extraSafety += if (isHi) "वरिष्ठों के सामने प्रस्तुति देने से पहले तथ्यों व एजेंडा की पूर्व-तैयारी रखें।" else "Enter leadership reviews with concise written preparation and calm dialogue."
        } else {
            extraSafety += if (isHi) "ऐसी भूमिका चुनें जहाँ पहल और स्वायत्त निर्णय लेने का अवसर मिले।" else "Ensure your role includes clear ownership so your natural leadership initiative thrives."
        }
        extraSafety += if (isHi) "सक्रिय ${ctx.planetName(ctx.mahaLordCanonical)} दशा के दौरान बड़े करियर परिवर्तन के पहले 6 माह की वित्तीय तैयारी रखें।" else "Keep a thoughtful 6-month bridge plan before major transitions during ${ctx.planetName(ctx.mahaLordCanonical)} Dasha."

        val nurturePlanet = listOf(lord10, "Sun", "Mercury", "Saturn").minByOrNull { ctx.planetScore(it, "D10") } ?: lord10
        val remedy = planetRemedy(nurturePlanet, ReadingCategory.CAREER, isHi)

        val narrative = if (isHi) {
            "$baseSignReading\n\nआपकी दशांश (D10) कुंडली में ${ctx.rashiName(d10LagnaSign)} लग्न और दशमेश ${ctx.planetName(lord10)} का प्रभाव आपकी कार्यशैली को $giftTitle की दिशा देता है। वर्तमान ${ctx.planetName(ctx.mahaLordCanonical)} महादशा कर्म क्षेत्र में सार्थक कौशल निर्माण का समय है।"
        } else {
            "$baseSignReading\n\nWith ${ctx.planetName(lord10)} governing your 10th House and ${ctx.rashiName(d10LagnaSign)} rising in the D10 Dashamsha chart, your vocational signature aligns naturally with $giftTitle. Your active ${ctx.planetName(ctx.mahaLordCanonical)} Maha Dasha highlights purposeful skill compounding."
        }

        val relevantYogas = ctx.yogasAndDoshas.filter { it.canonicalKey in setOf("Gajakesari", "KaalSarp", "SadeSati") }
        val libInsights = resolveCategoryLibraryInsights(
            ctx = ctx,
            categoryKey = "career",
            personalitySignIndex = ctx.lagna.rashiIndex,
            primaryHouse = 10,
            keyHouses = listOf(10, 1, 11, 2, 3, 6),
            candidatePlanets = listOf(lord10, "Sun", "Mercury", "Jupiter", "Saturn", "Mars").distinct(),
            divCode = "D10",
            fallbackNarrative = narrative
        )

        val (cautiousZones, safetyAdvice) = computeCautiousZonesAndSafety(
            ctx = ctx,
            categoryKey = "career",
            growthPlanet = libInsights.growthPlanet,
            extraCautious = baseCautiousZones,
            extraSafety = extraSafety
        )

        val archetype = CategoryArchetype(
            giftTitle = giftTitle,
            derivationBasis = derivationBasis,
            recommendedPaths = baseRecommendedPaths,
            cautiousZones = cautiousZones
        )

        val employmentPaths = computeDynamicPaths(
            ctx = ctx,
            categoryKey = "career",
            jsonSectionKey = "employment_paths_by_planet",
            rankedPlanets = libInsights.rankedPlanets,
            fallbackHouse = 10,
            fallbackLabels = baseRecommendedPaths,
            careerTrackKey = "employment"
        )
        val businessPaths = computeDynamicPaths(
            ctx = ctx,
            categoryKey = "career",
            jsonSectionKey = "business_paths_by_planet",
            rankedPlanets = libInsights.rankedPlanets,
            fallbackHouse = 10,
            fallbackLabels = baseRecommendedPaths,
            careerTrackKey = "business"
        )
        val diversePaths = (employmentPaths.take(3) + businessPaths.take(3)).distinctBy { it.title }

        val salariedMetric = ((ctx.houseStrength(6, "D1") + ctx.planetScore("Saturn", "D10") + ctx.planetScore("Sun", "D10")) / 3).coerceIn(25, 96)
        val ventureMetric = ((ctx.planetScore("Mercury", "D10") + ctx.houseStrength(7, "D10") + ctx.houseStrength(11, "D10")) / 3).coerceIn(25, 96)
        val empVsBizGuidance = if (isHi) {
            when {
                ventureMetric >= salariedMetric + 5 ->
                    "आपकी कुंडली में बुध, सप्तम व एकादश भाव स्वतंत्र उद्यम, परामर्श और व्यवसाय-निर्माण को विशेष बल देते हैं; प्रारंभ में सुरक्षित अनुभव लेकर स्वायत्त व्यवसाय की ओर बढ़ना श्रेष्ठ रहेगा।"
                salariedMetric >= ventureMetric + 5 ->
                    "दशम, षष्ठ भाव तथा शनि-सूर्य का संतुलन प्रतिष्ठित नौकरी, प्रशासनिक भूमिका और संरचित कॉर्पोरेट नेतृत्व में त्वरित एवं स्थिर उन्नति का संकेत देता है।"
                else ->
                    "आपकी कुंडली नौकरी और स्वतंत्र उद्यम दोनों के लिए लचीली ऊर्जा रखती है—प्रारंभ में स्थापित संस्था से गहन कौशल व पूंजी अर्जित कर साइड-प्रोजेक्ट या परामर्श के रूप में व्यवसाय आरंभ करना सर्वोत्तम रहेगा।"
            }
        } else {
            when {
                ventureMetric >= salariedMetric + 5 ->
                    "Strong 7th, 11th, and Mercury alignments favor independent ventures, consulting, and scalable business creation—build early domain mastery and step confidently into ownership."
                salariedMetric >= ventureMetric + 5 ->
                    "Your 6th, 10th, Sun, and Saturn alignments favor structured executive roles, civil/public institutions, and leadership tracks with compounding promotions and high reliability."
                else ->
                    "Your chart blends both tracks smoothly: build deep domain authority and reserves inside an established organization while incubating an independent consulting or digital venture alongside."
            }
        }

        val careerTopicSpecs = listOf(
            TopicEvalSpec(
                topicId = "career_field",
                score = ((ctx.planetScore(lord10, "D10") + ctx.houseStrength(10, "D10")) / 2).coerceIn(25, 96),
                driverPlanet = topPlanet,
                fallbackHouse = 10,
                timingLords = listOf("Mercury", "Jupiter", "Sun", lord10),
                fallbackTitleEn = "Best-Suited Career Field & Vocational Flow",
                fallbackTitleHi = "सर्वश्रेष्ठ करियर क्षेत्र एवं कार्य दिशा",
                fallbackBodyEn = "Your 10th House and D10 Dashamsha point toward {planet} themes in your {house}, rewarding clarity, craftsmanship, and steady mastery through {year_range}.",
                fallbackBodyHi = "आपका दशम भाव और दशांश कुंडली {house} में {planet} की ऊर्जा से जुड़ा करियर मार्ग दर्शाते हैं, जो {year_range} के दौरान विशेष प्रगति देता है।",
                fallbackBasisEn = "10th Lord ${ctx.planetName(lord10)} • D10 Alignment",
                fallbackBasisHi = "दशमेश ${ctx.planetName(lord10)} • D10 स्थिति"
            ),
            TopicEvalSpec(
                topicId = "govt_job",
                score = ((ctx.planetScore("Sun", "D10") + ctx.houseStrength(10, "D10") + ctx.houseStrength(6, "D1")) / 3).coerceIn(25, 96),
                driverPlanet = "Sun",
                fallbackHouse = 10,
                timingLords = listOf("Sun", "Jupiter", "Saturn", lord10, lord6),
                fallbackTitleEn = "Government Service & Public Authority",
                fallbackTitleHi = "सरकारी सेवा व प्रशासनिक अवसर",
                fallbackBodyEn = "Sun in your {house} and 6th/10th coordination support disciplined administrative, regulatory, or public-institution roles, especially during {year_range}.",
                fallbackBodyHi = "{house} में सूर्य और दशम-षष्ठ भाव का संयोग प्रशासनिक, सार्वजनिक या नीति-आधारित भूमिकाओं में {year_range} के दौरान शुभ अवसर बनाता है।",
                fallbackBasisEn = "Sun Placement • 6th & 10th Houses",
                fallbackBasisHi = "सूर्य स्थिति • षष्ठ व दशम भाव"
            ),
            TopicEvalSpec(
                topicId = "job_vs_business",
                score = ventureMetric,
                driverPlanet = "Mercury",
                fallbackHouse = 7,
                timingLords = listOf("Mercury", "Saturn", "Sun", lord10, lord7),
                fallbackTitleEn = "Job Track vs Independent Business",
                fallbackTitleHi = "नौकरी बनाम स्वतंत्र व्यवसाय",
                fallbackBodyEn = "With {planet} influencing your {house}, balancing structured career foundations with calculated entrepreneurial milestones around {year_range} creates lasting freedom.",
                fallbackBodyHi = "{house} में {planet} के प्रभाव से {year_range} में अनुशासित नौकरी और सोचे-समझे व्यवसाय विस्तार के बीच संतुलन अत्यंत फलदायी रहेगा।",
                fallbackBasisEn = "7th, 10th & 11th House Synergy",
                fallbackBasisHi = "सप्तम, दशम व एकादश भाव संतुलन"
            ),
            TopicEvalSpec(
                topicId = "job_promotion",
                score = ((analyticalBar + ctx.planetScore(ctx.mahaLordCanonical, "D10")) / 2).coerceIn(25, 96),
                driverPlanet = lord10,
                fallbackHouse = 11,
                timingLords = listOf("Jupiter", "Sun", "Saturn", lord10, lord11),
                fallbackTitleEn = "Promotions, Role Elevation & Recognition",
                fallbackTitleHi = "पदोन्नति, सम्मान एवं कार्यक्षेत्र उन्नति",
                fallbackBodyEn = "Your active planetary periods highlight {year_range} as a high-traction elevation cycle led by {planet} in your {house}.",
                fallbackBodyHi = "{house} में {planet} और सक्रिय दशा के कारण {year_range} का समय पदोन्नति और नेतृत्व प्राप्ति के लिए अत्यंत उत्साहवर्धक है।",
                fallbackBasisEn = "Active Maha Dasha & D10 11th House",
                fallbackBasisHi = "सक्रिय महादशा व दशांश एकादश भाव"
            )
        )

        val absorbedReadings = computeAbsorbedTopics(ctx, "career", careerTopicSpecs)
        val crossReferences = computeCrossReferences(ctx, "career", libInsights.strongestPlanet, 10)

        val h10 = lord10Planet?.houseNumber ?: 10
        val careerHeadline = universalSignatureHeadline(ReadingCategory.CAREER, topPlanet, isHi)
        val careerGlyph = planetGlyph(topPlanet)
        val heroSummary = if (isHi) {
            "दशांश (D10) लग्न ${ctx.rashiName(d10LagnaSign)} में ${ctx.planetName(topPlanet)} की प्रबल उपस्थिति और ${ctx.ordinalHouse(h10)} में स्थित दशमेश ${ctx.planetName(lord10)} मिलकर आपके करियर को '$careerHeadline' का अद्वितीय स्वरूप देते हैं। जब आप अपनी स्वाभाविक तार्किक, सृजनात्मक व नेतृत्व क्षमता को अनुशासित समय-नियोजन से जोड़ते हैं, तब स्थायी प्रतिष्ठा व समृद्धि सहज प्राप्त होती है। सक्रिय ${ctx.planetName(ctx.mahaLordCanonical)} महादशा इस वर्ष आपके प्रमुख कौशल को विश्वसनीय मंच दिलाती है।"
        } else {
            "With ${ctx.planetName(topPlanet)} energizing your vocational core and 10th Lord ${ctx.planetName(lord10)} anchored in your ${ctx.ordinalHouse(h10)} alongside ${ctx.rashiName(d10LagnaSign)} in D10, you embody '$careerHeadline'. Meaningful autonomy, respected craft, and steady strategic execution turn everyday work into lasting reputation. Your active ${ctx.planetName(ctx.mahaLordCanonical)} Maha Dasha sharpens this signature into tangible milestones."
        }
        val astroMathPill = if (isHi) {
            "दशमेश: ${ctx.planetName(lord10)} (भाव $h10) • D10 लग्न: ${ctx.rashiName(d10LagnaSign)}"
        } else {
            "10th Lord: ${ctx.planetName(lord10)} (House $h10) • D10 Lagna: ${ctx.rashiName(d10LagnaSign)}"
        }

        val topJobMatches = rankAndNormalizeCatalogMatches(
            ctx = ctx,
            sectionKey = "jobs_by_planet",
            divCode = "D10",
            categoryBoostHouses = setOf(10, 11, 2, 6, 1)
        )
        val topBusinessMatches = rankAndNormalizeCatalogMatches(
            ctx = ctx,
            sectionKey = "business_models_by_planet",
            divCode = "D10",
            categoryBoostHouses = setOf(7, 10, 11, 3, 2)
        )

        val scopePlanets = libInsights.rankedPlanets.take(3).ifEmpty { listOf(lord10, "Mercury", "Jupiter") }
        val broadScopes = scopePlanets.mapIndexed { idx, pCanon ->
            val pObj = ctx.planet(pCanon)
            val pHouse = ctx.ordinalHouse(pObj?.houseNumber ?: 10)
            val pRashi = pObj?.rashiName ?: ctx.lagna.rashiName
            val pDisp = ctx.planetName(pCanon)
            val empSample = employmentPaths.getOrNull(idx)
            val bizSample = businessPaths.getOrNull(idx)
            if (isHi) {
                BroadScopeItem(
                    title = empSample?.sector ?: "${pDisp} कार्य-विस्तार क्षेत्र",
                    description = "${empSample?.title ?: "रणनीतिक नेतृत्व"} तथा ${bizSample?.title ?: "स्वतंत्र परामर्श"} जैसे व्यापक क्षेत्रों में आपकी स्वाभाविक पकड़ मजबूत है। दीर्घकालिक विश्वसनीयता आपको प्रमुख निर्णयकर्ताओं में स्थापित करती है।",
                    chartReference = "क्योंकि आपका $pDisp $pHouse ($pRashi) में बैठकर D10 कर्म भाव को सक्रिय कर रहा है।"
                )
            } else {
                BroadScopeItem(
                    title = empSample?.sector ?: "$pDisp Enterprise Scope",
                    description = "Broad arenas spanning ${empSample?.title ?: "strategic roles"} and ${bizSample?.title ?: "independent advisory"} fit your natural tempo. Compounding domain credibility puts you in high-trust rooms.",
                    chartReference = "Because your $pDisp occupies your $pHouse ($pRashi), reinforcing your 10th-house D10 career axis."
                )
            }
        }

        val tier3Cards = if (isHi) listOf(
            MindsetRuleCard(
                title = "सरकारी एवं प्रशासनिक सेवा दृष्टिकोण",
                body = "दैनिक प्रातः २ घंटे संरचित पाठ्यक्रम, मॉक-टेस्ट विश्लेषण और लिखित संक्षिप्त नोट्स के लिए आरक्षित रखें। नियमित पुनरावृत्ति और शांत अनुशासन बड़े कट-ऑफ को सहजता से पार कराता है।",
                chartReference = "${ctx.whyChartClause("सूर्य", "सूर्य", "Sun")} और षष्ठ-दशम भाव के साथ प्रशासनिक स्थिरता को बल देते हैं।"
            ),
            MindsetRuleCard(
                title = "निजी कॉर्पोरेट क्षेत्र में प्रगति रणनीति",
                body = "हर तिमाही अपने कार्य के मापनीय परिणाम लिखें और क्रॉस-टीम परियोजनाओं में पहल लें। वरिष्ठ मेंटर्स के साथ स्पष्ट संवाद आपकी पदोन्नति गति को दोगुना करता है।",
                chartReference = "${ctx.whyChartClause("दशमेश", "दशमेश", lord10)}, जो दशांश कुंडली में स्पष्ट दृश्यमानता प्रदान करते हैं।"
            ),
            MindsetRuleCard(
                title = "व्यवसाय, साझेदारी एवं जोखिम संतुलन",
                body = "किसी भी उद्यम या साझेदारी में भूमिकाएँ, लाभ-अनुपात और निकास नियम प्रारंभ में ही लिखित रूप में तय करें। छह माह का नकद कोष सुरक्षित रखकर ही नया विस्तार कदम उठाएँ।",
                chartReference = "${ctx.whyChartClause("सप्तमेश", "सप्तमेश", lord7)} और एकादश भाव के तालमेल से साझेदारी संतुलित रहती है।"
            )
        ) else listOf(
            MindsetRuleCard(
                title = "Public & Government Service Study Rhythm",
                body = "Dedicate fixed morning focus blocks to core syllabus mastery, timed mock papers, and weekly error-log revision. Steady consistency quietly outperforms erratic marathon cramming.",
                chartReference = "${ctx.whyChartClause("Sun", "सूर्य", "Sun")}, shaping disciplined 6th and 10th house public-service endurance."
            ),
            MindsetRuleCard(
                title = "Private Sector & Corporate Navigation",
                body = "Track measurable quarterly impact in writing, seek high-visibility cross-functional projects, and cultivate candid mentor feedback. Articulating business outcomes speeds up promotion cycles.",
                chartReference = "${ctx.whyChartClause("10th Lord", "दशमेश", lord10)}, anchoring recognition across your D10 professional axis."
            ),
            MindsetRuleCard(
                title = "Business Partnerships & Risk Stewardship",
                body = "Put equity splits, decision rights, and 6-month operating runway reserves in writing before scaling any venture. Complementary co-founders amplify strengths while protecting downside cleanly.",
                chartReference = "${ctx.whyChartClause("7th Lord", "सप्तमेश", lord7)}, defining how 7th and 11th house alliances prosper sustainably."
            )
        )

        val careerFaqs = if (isHi) listOf(
            CreativeFaqItem(
                question = "मेरे लिए कौन सा करियर क्षेत्र सबसे उपयुक्त है?",
                paragraphs = listOf(
                    "क्योंकि आपके दशमेश ${ctx.planetName(lord10)} ${ctx.ordinalHouse(h10)} में विराजमान हैं और D10 लग्न ${ctx.rashiName(d10LagnaSign)} है, आपकी पत्रिका '$giftTitle' कार्यशैली को सर्वोपरि समर्थन देती है। आप उन क्षेत्रों में शीघ्र उत्कृष्टता पाते हैं जहाँ तार्किक निर्णय, सृजन और सम्मानजनक नेतृत्व का संगम हो।",
                    "शीर्ष सामान्यीकृत मिलान में ${topJobMatches.take(2).joinToString(" व ") { it.title }} जैसे पथ आपके प्रमुख ग्रह ${ctx.planetName(topPlanet)} के साथ सर्वाधिक तालमेल रखते हैं। सक्रिय ${ctx.planetName(ctx.mahaLordCanonical)} महादशा के दौरान विशेष कौशल को निखारना दीर्घकालिक उन्नति का द्वार खोलता है।"
                )
            ),
            CreativeFaqItem(
                question = "नौकरी बनाम व्यवसाय — मेरे लिए क्या बेहतर है?",
                paragraphs = listOf(
                    empVsBizGuidance,
                    "क्योंकि आपका सप्तम भाव (${ctx.planetName(lord7)}) और एकादश भाव (${ctx.planetName(lord11)}) आय के बहु-माध्यमों को प्रेरित करते हैं, कौशल व वित्तीय संचय स्थापित संस्था में मजबूत करने के बाद परामर्श या स्वतंत्र उद्यम की ओर कदम बढ़ाना सर्वोत्तम संतुलन देता है।"
                )
            )
        ) else listOf(
            CreativeFaqItem(
                question = "Which career field suits me best?",
                paragraphs = listOf(
                    "Because your 10th Lord ${ctx.planetName(lord10)} occupies your ${ctx.ordinalHouse(h10)} and ${ctx.rashiName(d10LagnaSign)} rises in your D10 chart, your natural professional edge aligns with '$giftTitle'. Roles combining visible ownership, structured problem-solving, and credible expertise feel deeply energizing.",
                    "Specifically, top normalized matches such as ${topJobMatches.take(2).joinToString(" and ") { it.title }} resonate closely with ${ctx.planetName(topPlanet)} in your chart. Compounding specialized skill mastery during your active ${ctx.planetName(ctx.mahaLordCanonical)} Maha Dasha unlocks lasting authority."
                )
            ),
            CreativeFaqItem(
                question = "Job vs Business — what is better for me?",
                paragraphs = listOf(
                    empVsBizGuidance,
                    "Because your 7th Lord (${ctx.planetName(lord7)}) and 11th Lord (${ctx.planetName(lord11)}) govern alliances and scalable gains, building elite execution fluency and a 6-month financial cushion first ensures whichever track you lean into compounds smoothly."
                )
            )
        )

        val careerStory = buildStoryParagraphs(
            ctx = ctx,
            categoryKey = "career",
            strongestPlanetCanon = topPlanet,
            keyLordCanon = lord10,
            keyHouseNum = 10,
            divCode = "D10",
            overallScore = overallScore
        )

        return CategoryDeepDive(
            category = ReadingCategory.CAREER,
            title = if (isHi) "करियर एवं कर्म क्षेत्र" else "Career & Vocation",
            overallScore = overallScore,
            signatureTitle = careerHeadline,
            archetype = archetype,
            narrative = narrative,
            diversePaths = diversePaths,
            employmentPaths = employmentPaths,
            businessPaths = businessPaths,
            employmentVsBusinessGuidance = empVsBizGuidance,
            powerBars = powerBars,
            proofItems = proofItems,
            yogaNotes = relevantYogas,
            divisionalChartCode = ReadingCategory.CAREER.divisionalChartCode,
            absorbedReadings = absorbedReadings,
            crossReferences = crossReferences,
            whatToDo = whatToDo.distinct().take(4),
            safetyAdvice = safetyAdvice,
            remedy = remedy,
            personalityParagraph = libInsights.personalityParagraph,
            strengthsParagraph = libInsights.strengthsParagraph,
            challengesParagraph = libInsights.challengesParagraph,
            superpower = libInsights.superpower,
            growthArea = libInsights.growthArea,
            heroSummary = heroSummary,
            astroMathPill = astroMathPill,
            topJobMatches = topJobMatches,
            topBusinessMatches = topBusinessMatches,
            broadScopes = broadScopes,
            tier3SectionTitle = if (isHi) "कार्य के प्रति सही दृष्टिकोण" else "How to Approach Your Work",
            tier3Cards = tier3Cards,
            creativeFaqs = careerFaqs,
            heroGlyph = careerGlyph,
            storyParagraphs = careerStory
        )
    }

    private fun buildWealth(ctx: ChartContext, baseCareerReading: String): CategoryDeepDive {
        val isHi = ctx.isHi
        val lord2 = ctx.houseLordCanon(2)
        val lord11 = ctx.houseLordCanon(11)
        val lord4 = ctx.houseLordCanon(4)
        val lord9 = ctx.houseLordCanon(9)

        val dhanaFlow = ((ctx.houseStrength(2) + ctx.planetScore("Jupiter")) / 2).coerceIn(25, 96)
        val gainsFlow = ((ctx.houseStrength(11) + ctx.houseStrength(9)) / 2).coerceIn(25, 96)
        val assetStability = ((ctx.houseStrength(4) + ctx.planetScore("Venus") + ctx.planetScore("Mars")) / 3).coerceIn(25, 96)
        val debtResilience = ((ctx.planetScore("Saturn") + ctx.houseStrength(11) + if (ctx.yogaPresent("Gajakesari")) 78 else 52) / 3).coerceIn(25, 96)

        val primaryDriver = listOf(lord2, lord11, "Jupiter", "Venus", "Mercury", "Mars")
            .maxByOrNull { ctx.planetScore(it) } ?: "Jupiter"

        val basis = if (isHi) {
            "द्वितीयेश (धन): ${ctx.planetName(lord2)} • एकादशेश (लाभ): ${ctx.planetName(lord11)} • चतुर्थेश (संपत्ति): ${ctx.planetName(lord4)} • गुरु भाव ${ctx.planet("Jupiter")?.houseNumber ?: 1}"
        } else {
            "2nd Lord (Savings): ${ctx.planetName(lord2)} • 11th Lord (Gains): ${ctx.planetName(lord11)} • 4th Lord (Assets): ${ctx.planetName(lord4)} • Jupiter in House ${ctx.planet("Jupiter")?.houseNumber ?: 1}"
        }

        val giftTitle: String
        val baseRecommendedPaths: List<String>
        val baseCautiousZones: List<String>
        when (primaryDriver) {
            "Jupiter" -> if (isHi) {
                giftTitle = "विवेकशील संचयकर्ता"
                baseRecommendedPaths = listOf("इंडेक्स व दीर्घकालिक निवेश", "ज्ञान-आधारित आय", "सलाहकार व ट्रस्ट संपत्तियाँ", "पारिवारिक कोष वृद्धि")
                baseCautiousZones = listOf("उतावले सट्टा व्यापार", "असुरक्षित बिना दस्तावेज उधार")
            } else {
                giftTitle = "Compound Wealth Architect"
                baseRecommendedPaths = listOf("Long-Horizon Compounding", "Advisory & Intellectual Equity", "Diversified Income Assets", "Stewarded Family Reserves")
                baseCautiousZones = listOf("Leveraged Short-Term Speculation", "Undocumented Informal Lending")
            }

            "Mercury" -> if (isHi) {
                giftTitle = "व्यापारिक मूल्य निर्माता"
                baseRecommendedPaths = listOf("व्यवसाय व ई-कॉमर्स", "डेटा व वित्तीय प्रबंधन", "बौद्धिक संपदा", "बहु-आय स्रोत")
                baseCautiousZones = listOf("बिना बजट के आवेगी खर्च", "अस्पष्ट साझेदारी समझौते")
            } else {
                giftTitle = "Value-Creation Strategist"
                baseRecommendedPaths = listOf("Scalable Digital Commerce", "Portfolio & Cashflow Analytics", "Intellectual Property", "Multi-Stream Advisory")
                baseCautiousZones = listOf("Untracked Impulse Liquidity Drain", "Vague Revenue-Share Agreements")
            }

            "Mars", lord4 -> if (isHi) {
                giftTitle = "अचल संपत्ति निर्माता"
                baseRecommendedPaths = listOf("भूमि व रियल एस्टेट", "मूर्त परिसंपत्तियाँ", "इंजीनियरिंग उद्यम", "ऋण-मुक्त गृह निर्माण")
                baseCautiousZones = listOf("उच्च ब्याज वाले त्वरित कर्ज", "भावावेश में संपत्ति सौदे")
            } else {
                giftTitle = "Asset & Real-Estate Builder"
                baseRecommendedPaths = listOf("Real Estate & Land Equity", "Tangible Infrastructure", "Fixed-Asset Accumulation", "Disciplined Mortgage Paydown")
                baseCautiousZones = listOf("High-Interest Short-Term Debt", "Rushed Property Closings Without Due Diligence")
            }

            else -> if (isHi) {
                giftTitle = "संतुलित समृद्धि साधक"
                baseRecommendedPaths = listOf("नियमित बचत योजना", "ब्रांड व रचनात्मक मूल्य", "स्थिर लाभांश निवेश", "कौशल-आधारित आय")
                baseCautiousZones = listOf("दिखावे पर अतिरिक्त व्यय", "त्वरित लाभ वाली अनजान योजनाएँ")
            } else {
                giftTitle = "Steady Prosperity Steward"
                baseRecommendedPaths = listOf("Automated Cashflow Reserves", "Creative & Lifestyle Equity", "Quality Dividend Assets", "Skill-Anchored Retainers")
                baseCautiousZones = listOf("Lifestyle Inflation Beyond Core Surplus", "Opaque High-Yield Promises")
            }
        }

        val earningScore = ((ctx.houseStrength(10) + ctx.planetScore("Sun")) / 2).coerceIn(25, 96)
        val savingScore = ((ctx.houseStrength(2) + ctx.planetScore("Moon")) / 2).coerceIn(25, 96)
        val investmentScore = ((ctx.houseStrength(5) + ctx.planetScore("Jupiter")) / 2).coerceIn(25, 96)
        val debtMgmtScore = ((ctx.planetScore("Saturn") + ctx.houseStrength(11) + if (ctx.yogaPresent("Gajakesari")) 78 else 54) / 3).coerceIn(25, 96)
        val suddenGainsScore = ((ctx.houseStrength(11) + ctx.planetScore("Rahu") + ctx.planetScore("Mercury")) / 3).coerceIn(25, 96)
        val overallScore = ((earningScore + savingScore + investmentScore + debtMgmtScore + suddenGainsScore) / 5).coerceIn(28, 95)

        val powerBars = if (isHi) listOf(
            PowerBarMetric(
                label = "अर्जन क्षमता (Earning Capacity)",
                score = earningScore,
                note = "${ctx.whyChartClause("द्वितीयेश", "द्वितीयेश", lord2)}, आपकी आय-सृजन क्षमता निरंतर सशक्त होती है।",
                glyph = "☉"
            ),
            PowerBarMetric(
                label = "बचत संतुलन (Saving)",
                score = savingScore,
                note = "${ctx.whyChartClause("चंद्र", "चंद्र", "Moon")}, नियमित स्वचालित बचत आपके कोष को सुरक्षित रखती है।",
                glyph = "☽"
            ),
            PowerBarMetric(
                label = "निवेश बुद्धि (Investment)",
                score = investmentScore,
                note = "${ctx.whyChartClause("बृहस्पति", "बृहस्पति", "Jupiter")}, दीर्घकालिक और नैतिक निवेश सर्वाधिक फलदायी रहते हैं।",
                glyph = "♃"
            ),
            PowerBarMetric(
                label = "ऋण प्रबंधन (Debt Management)",
                score = debtMgmtScore,
                note = "${ctx.whyChartClause("शनि", "शनि", "Saturn")}, स्पष्ट बजट और धैर्य से सभी दायित्व सुगमता से चुकते हैं।",
                glyph = "♄"
            ),
            PowerBarMetric(
                label = "आकस्मिक लाभ (Sudden Gains)",
                score = suddenGainsScore,
                note = "${ctx.whyChartClause("एकादशेश", "एकादशेश", lord11)}, सही समय पर अवसर-लाभ और बोनस योग सक्रिय रहते हैं।",
                glyph = "☊"
            )
        ) else listOf(
            PowerBarMetric(
                label = "Earning Capacity",
                score = earningScore,
                note = "${ctx.whyChartClause("2nd Lord", "द्वितीयेश", lord2)}, your core income engine grows steadily with skill mastery.",
                glyph = "☉"
            ),
            PowerBarMetric(
                label = "Saving",
                score = savingScore,
                note = "${ctx.whyChartClause("Moon", "चंद्र", "Moon")}, automated monthly reserves protect peaceful household liquidity.",
                glyph = "☽"
            ),
            PowerBarMetric(
                label = "Investment",
                score = investmentScore,
                note = "${ctx.whyChartClause("Jupiter", "बृहस्पति", "Jupiter")}, patient quality compounding outweighs hasty speculation.",
                glyph = "♃"
            ),
            PowerBarMetric(
                label = "Debt Management",
                score = debtMgmtScore,
                note = "${ctx.whyChartClause("Saturn", "शनि", "Saturn")}, structured repayment keeps balance sheets light and agile.",
                glyph = "♄"
            ),
            PowerBarMetric(
                label = "Sudden Gains",
                score = suddenGainsScore,
                note = "${ctx.whyChartClause("11th Lord", "एकादशेश", lord11)}, equity grants and timely windfalls reward readiness.",
                glyph = "☊"
            )
        )

        val proofItems = listOf(lord2, lord11, "Jupiter", "Venus", lord4).distinct().map { canon ->
            val p = ctx.planet(canon)
            val (status, ok) = ctx.dignityBadge(canon)
            val tag = when {
                canon == lord2 && isHi -> " (द्वितीयेश)"
                canon == lord2 -> " (2nd Lord)"
                canon == lord11 && isHi -> " (एकादशेश)"
                canon == lord11 -> " (11th Lord)"
                else -> ""
            }
            AstroProofItem(
                title = "${ctx.planetName(canon)}$tag",
                placementDetail = if (isHi) "भाव ${p?.houseNumber ?: 1} • ${p?.rashiName ?: ""} (${p?.formattedDegree ?: ""})"
                else "House ${p?.houseNumber ?: 1} • ${p?.rashiName ?: ""} (${p?.formattedDegree ?: ""})",
                statusLabel = status,
                isFavorable = ok
            )
        }

        val whatToDo = mutableListOf<String>()
        val extraSafety = mutableListOf<String>()
        if (dhanaFlow >= 62) {
            whatToDo += if (isHi) "मासिक आय का कम से कम 25% स्वचालित दीर्घकालिक बचत में लगाएँ।" else "Automate at least 25% of monthly inflows into compounding long-term instruments."
        } else {
            whatToDo += if (isHi) "साप्ताहिक व्यय डायरी रखें और छह माह का आपातकालीन कोष प्राथमिकता से बनाएँ।" else "Maintain a tight weekly cashflow ledger and build a 6-month liquid reserve first."
        }
        if (assetStability >= 62) {
            whatToDo += if (isHi) "गृह या ठोस अचल संपत्ति के अवसरों का मूल्यांकन स्पष्ट कागजात के साथ करें।" else "Evaluate residential or real-asset acquisitions when documentation is verified."
        } else {
            whatToDo += if (isHi) "बड़े अचल सौदों से पहले तरल नकदी और ऋण-मुक्त आधार मजबूत करें।" else "Strengthen liquid investments before locking capital into illiquid real estate."
        }
        whatToDo += if (isHi) "प्रत्येक माह अपने वित्तीय लक्ष्यों और कर-योजना की शांत समीक्षा करें।" else "Review tax strategy, budgets, and fee structures systematically every month."

        if (ctx.planetScore("Venus") < 58) {
            extraSafety += if (isHi) "आराम व विलासिता की वस्तुएँ नकद बचत के आधार पर ही खरीदें, अल्पकालिक कर्ज से नहीं।" else "Fund lifestyle upgrades out of accumulated surplus rather than short-term credit."
        } else {
            extraSafety += if (isHi) "सुविधा-खर्च की तुलना अपने निष्क्रिय निवेश रिटर्न से अवश्य करते रहें।" else "Keep recurring lifestyle growth paced below your passive investment growth."
        }
        if (ctx.yogaPresent("SadeSati")) {
            extraSafety += if (isHi) "शनि साढ़े साती के दौरान जमानती गारंटर बनने या उच्च जोखिम ऋण से दूरी रखें।" else "Keep debt conservative and verify every legal signature carefully during Sade Sati."
        } else {
            extraSafety += if (isHi) "आपातकालीन कोष को हमेशा सुरक्षित और पारदर्शी विकल्पों में रखें।" else "Keep emergency reserves strictly inside transparent, verified instruments."
        }
        extraSafety += if (isHi) "पारिवारिक धन निर्णयों में स्पष्ट लिखित रिकॉर्ड बनाए रखें।" else "Document shared family financial agreements in writing with clear timelines."

        val nurturePlanet = listOf(lord2, lord11, "Jupiter", "Venus").minByOrNull { ctx.planetScore(it) } ?: "Jupiter"
        val remedy = planetRemedy(nurturePlanet, ReadingCategory.WEALTH, isHi)
        val relevantYogas = ctx.yogasAndDoshas.filter { it.canonicalKey in setOf("Gajakesari", "SadeSati", "Kemadruma") }

        val narrative = if (isHi) {
            "$baseCareerReading\n\nधन भाव (द्वितीय) के स्वामी ${ctx.planetName(lord2)} और लाभ भाव (एकादश) के स्वामी ${ctx.planetName(lord11)} मिलकर आपके आर्थिक प्रवाह को दिशा देते हैं। आपकी कुंडली अनुशासित संचय और सही समय पर लिए गए संपत्ति निर्णयों से दीर्घकालिक समृद्धि का समर्थन करती है।"
        } else {
            "$baseCareerReading\n\nFinancial flow in your chart is anchored by 2nd Lord ${ctx.planetName(lord2)} and 11th Lord ${ctx.planetName(lord11)} alongside Jupiter. Consistent compounding, clean cashflow boundaries, and measured timing convert effort into durable wealth."
        }

        val wealthSignIndex = ctx.planet("Moon")?.rashiIndex ?: ctx.lagna.rashiIndex
        val libInsights = resolveCategoryLibraryInsights(
            ctx = ctx,
            categoryKey = "wealth",
            personalitySignIndex = wealthSignIndex,
            primaryHouse = 2,
            keyHouses = listOf(2, 11, 4, 5, 9),
            candidatePlanets = listOf(lord2, lord11, "Jupiter", "Venus", lord4, "Mercury").distinct(),
            divCode = "D1",
            fallbackNarrative = narrative
        )

        val (cautiousZones, safetyAdvice) = computeCautiousZonesAndSafety(
            ctx = ctx,
            categoryKey = "wealth",
            growthPlanet = libInsights.growthPlanet,
            extraCautious = baseCautiousZones,
            extraSafety = extraSafety
        )

        val archetype = CategoryArchetype(
            giftTitle = giftTitle,
            derivationBasis = basis,
            recommendedPaths = baseRecommendedPaths,
            cautiousZones = cautiousZones
        )

        val diversePaths = computeDynamicPaths(
            ctx = ctx,
            categoryKey = "wealth",
            jsonSectionKey = "diverse_paths_by_planet",
            rankedPlanets = libInsights.rankedPlanets,
            fallbackHouse = 2,
            fallbackLabels = baseRecommendedPaths
        )

        val wealthTopicSpecs = listOf(
            TopicEvalSpec(
                topicId = "dhan_yog",
                score = ((dhanaFlow + gainsFlow) / 2).coerceIn(25, 96),
                driverPlanet = lord2,
                fallbackHouse = 2,
                timingLords = listOf("Jupiter", "Venus", lord2, lord11, lord9),
                fallbackTitleEn = "Dhan Yog & Wealth Multiplication Windows",
                fallbackTitleHi = "धन योग एवं समृद्धि वृद्धि का समय",
                fallbackBodyEn = "{planet} energizing your {house} activates compounding savings and income breakthroughs during {year_range}.",
                fallbackBodyHi = "{house} में {planet} का प्रभाव {year_range} के दौरान संचय और आय विस्तार के विशेष अवसर लाता है।",
                fallbackBasisEn = "2nd Lord ${ctx.planetName(lord2)} • 11th Lord ${ctx.planetName(lord11)}",
                fallbackBasisHi = "द्वितीयेश ${ctx.planetName(lord2)} • एकादशेश ${ctx.planetName(lord11)}"
            ),
            TopicEvalSpec(
                topicId = "debt_clearance",
                score = debtResilience,
                driverPlanet = lord11,
                fallbackHouse = 11,
                timingLords = listOf("Jupiter", "Mercury", "Venus", lord11, lord2),
                fallbackTitleEn = "Cashflow Surplus & Debt Freedom Timeline",
                fallbackTitleHi = "ऋण-मुक्ति एवं आर्थिक संतुलन समय",
                fallbackBodyEn = "Structured budgeting guided by {planet} strengthens surplus accumulation, making {year_range} highly favorable for clearing obligations.",
                fallbackBodyHi = "{planet} के मार्गदर्शन में अनुशासित वित्तीय योजना {year_range} के भीतर दायित्व कम करने और अधिशेष बढ़ाने में सहायक है।",
                fallbackBasisEn = "6th–11th Balance & Saturn Discipline",
                fallbackBasisHi = "षष्ठ-एकादश संतुलन व शनि अनुशासन"
            ),
            TopicEvalSpec(
                topicId = "property_yog",
                score = assetStability,
                driverPlanet = lord4,
                fallbackHouse = 4,
                timingLords = listOf("Mars", "Venus", "Moon", lord4, lord11),
                fallbackTitleEn = "Home, Land & Fixed-Asset Acquisition",
                fallbackTitleHi = "भवन, भूमि एवं वाहन सुख योग",
                fallbackBodyEn = "With 4th House themes shaped by {planet} in your {house}, tangible property or vehicle upgrades gain momentum in {year_range}.",
                fallbackBodyHi = "{house} में {planet} और चतुर्थ भाव के प्रभाव से {year_range} के दौरान घर, भूमि या वाहन से जुड़े निर्णय शुभ फल देते हैं।",
                fallbackBasisEn = "4th Lord ${ctx.planetName(lord4)} • Venus & Mars",
                fallbackBasisHi = "चतुर्थेश ${ctx.planetName(lord4)} • शुक्र व मंगल"
            )
        )

        val absorbedReadings = computeAbsorbedTopics(ctx, "wealth", wealthTopicSpecs)
        val crossReferences = computeCrossReferences(ctx, "wealth", libInsights.strongestPlanet, 2)

        val h2 = ctx.planet(lord2)?.houseNumber ?: 2
        val h11 = ctx.planet(lord11)?.houseNumber ?: 11
        val wealthHeadline = universalSignatureHeadline(ReadingCategory.WEALTH, primaryDriver, isHi)
        val wealthGlyph = planetGlyph(primaryDriver)
        val heroSummary = if (isHi) {
            "द्वितीयेश ${ctx.planetName(lord2)} (भाव $h2) और एकादशेश ${ctx.planetName(lord11)} (भाव $h11) के साथ प्रमुख धन-कारक ${ctx.planetName(primaryDriver)} आपकी आर्थिक यात्रा को '$wealthHeadline' की सुदृढ़ दिशा देते हैं। स्वचालित बचत और सोचे-समझे दीर्घकालिक निवेश आपकी संपत्ति को पीढ़ियों तक सुरक्षित व समृद्ध बनाते हैं। सक्रिय ${ctx.planetName(ctx.mahaLordCanonical)} काल में स्पष्ट वित्तीय अनुशासन बहुआयामी आय बढ़ाता है।"
        } else {
            "Anchored by ${ctx.planetName(primaryDriver)}, your 2nd Lord ${ctx.planetName(lord2)} in the ${ctx.ordinalHouse(h2)}, and 11th Lord ${ctx.planetName(lord11)} in the ${ctx.ordinalHouse(h11)}, your prosperity path carries the '$wealthHeadline' signature. Steady savings discipline paired with calm long-term compounding turns reliable income into lasting peace. Your active ${ctx.planetName(ctx.mahaLordCanonical)} Maha Dasha brings clear focus to asset stewardship."
        }
        val astroMathPill = if (isHi) {
            "द्वितीयेश: ${ctx.planetName(lord2)} (भाव $h2) • एकादशेश: ${ctx.planetName(lord11)} (भाव $h11)"
        } else {
            "2nd Lord: ${ctx.planetName(lord2)} (House $h2) • 11th Lord: ${ctx.planetName(lord11)} (House $h11)"
        }

        val topWealthMatches = rankAndNormalizeCatalogMatches(
            ctx = ctx,
            sectionKey = "wealth_sources_by_planet",
            divCode = "D1",
            categoryBoostHouses = setOf(2, 11, 4, 5, 9)
        )

        val wealthMindsetCard = if (isHi) {
            BroadScopeItem(
                title = "$giftTitle — धन मनोविज्ञान",
                description = "आप धन को क्षणिक दिखावे के बजाय सुरक्षा, परिवार की शांति और सार्थक स्वतंत्रता के साधन के रूप में देखते हैं। जब आय के विभिन्न स्रोत स्वचालित बचत व्यवस्था से जुड़ते हैं, तब आपका आत्मविश्वास और निर्णय-क्षमता दोनों कई गुना बढ़ जाते हैं। छोटे नियमित निवेश आपके लिए बड़े जोखिम वाले दांवों से कहीं अधिक सुकून व परिणाम देते हैं।",
                chartReference = "${ctx.whyChartClause("द्वितीयेश", "द्वितीयेश", lord2)} और बृहस्पति के प्रभाव से दीर्घकालिक संचय को बल देते हैं।"
            )
        } else {
            BroadScopeItem(
                title = "$giftTitle — Money Psychology",
                description = "You instinctively view money as peace of mind, family security, and creative freedom rather than mere display. When monthly inflows automatically route into diversified reserves before discretionary spending, financial decisions feel effortless and calm. Steady conviction consistently beats reactive market chasing in your chart.",
                chartReference = "${ctx.whyChartClause("2nd Lord", "द्वितीयेश", lord2)} alongside Jupiter's influence in your ${ctx.ordinalHouse(ctx.planet("Jupiter")?.houseNumber ?: 5)}."
            )
        }

        val tier3WealthRules = if (isHi) listOf(
            MindsetRuleCard(
                title = "सकारात्मक धन-प्रवाह नियम (The Circulation Mindset)",
                body = "आय का एक निश्चित अंश बचत व निवेश में रखने के साथ एक छोटा अंश ज्ञान, परिवार और सेवा में उदारता से प्रवाहित करें। कृतज्ञता के साथ धन का सही उपयोग नए अवसरों को स्वाभाविक आकर्षित करता है।",
                chartReference = "${ctx.whyChartClause("एकादशेश", "एकादशेश", lord11)} और द्वितीय भाव के शुभ आदान-प्रदान पर आधारित।"
            ),
            MindsetRuleCard(
                title = "खरीद से पूर्व २४ घंटे का संयम नियम (The Cooling-Off Period)",
                body = "किसी भी गैर-अनिवार्य बड़े खर्च, विलासिता या अटकलबाजी निवेश से पहले पूरे २४ घंटे प्रतीक्षा करें। यह छोटा ठहराव अनावश्यक व्यय रोककर आपके दीर्घकालिक निवेश को तेज़ी से बढ़ाता है।",
                chartReference = "${ctx.whyChartClause("शुक्र", "शुक्र", "Venus")} और शनि का अनुशासन आवेगी व्यय को शांत समृद्धि में बदलता है।"
            )
        ) else listOf(
            MindsetRuleCard(
                title = "The Circulation Mindset",
                body = "Pair automated long-term saving with intentional generosity toward learning, craft, and meaningful service. Keeping value circulating cleanly prevents scarcity anxiety and draws collaborative opportunities.",
                chartReference = "${ctx.whyChartClause("11th Lord", "एकादशेश", lord11)}, linking ethical generosity with expanding 2nd and 11th house gains."
            ),
            MindsetRuleCard(
                title = "The Cooling-Off Period",
                body = "Wait a calm 24 hours before any non-essential purchase or speculative trade above your weekly discretionary cap. That single pause filters impulse noise out while compounding reserves automatically.",
                chartReference = "${ctx.whyChartClause("Venus", "शुक्र", "Venus")}, where Saturnian pacing preserves surplus across market cycles."
            )
        )

        val wealthDebtWindow = absorbedReadings.firstOrNull { it.topicId == "debt_clearance" }?.timingWindow ?: "${ctx.referenceYear}–${ctx.referenceYear + 2}"
        val wealthDhanWindow = absorbedReadings.firstOrNull { it.topicId == "dhan_yog" }?.timingWindow ?: "${ctx.referenceYear}–${ctx.referenceYear + 2}"

        val wealthFaqs = if (isHi) listOf(
            CreativeFaqItem(
                question = "मैं अपने ऋण और वित्तीय दायित्व कब तक समाप्त कर पाऊंगा/पाऊंगी?",
                paragraphs = listOf(
                    "क्योंकि आपके षष्ठ एवं एकादश भाव का संतुलन तथा शनि की स्थिति अनुशासित भुगतान का समर्थन करती है, $wealthDebtWindow की अवधि पुराने दायित्वों को क्रमशः समाप्त कर नकदी अधिशेष बढ़ाने के लिए अत्यंत अनुकूल है।",
                    "उच्च-ब्याज वाले दायित्वों को पहले समेटना और द्वितीयेश ${ctx.planetName(lord2)} की दिशा में नियमित मासिक कोष बनाना आपको शीघ्र पूर्ण वित्तीय निश्चिंतता दिलाएगा।"
                )
            ),
            CreativeFaqItem(
                question = "क्या मेरी कुंडली में आकस्मिक धन लाभ के योग हैं?",
                paragraphs = listOf(
                    "क्योंकि आपके एकादशेश ${ctx.planetName(lord11)} ${ctx.ordinalHouse(h11)} में हैं और $wealthDhanWindow के दौरान शुभ दशा-प्रवाह बन रहा है, आपको इक्विटी, बोनस, संपत्ति मूल्य वृद्धि या विशेष प्रोजेक्ट्स से अप्रत्याशित लाभ के सुंदर अवसर मिल सकते हैं।",
                    "राहु और बुध के प्रभाव से आधुनिक तकनीक, परामर्श या दीर्घकालिक निवेश होल्डिंग्स में अचानक उछाल संभव है—सुनिश्चित करें कि आप लाभ का बड़ा हिस्सा सुरक्षित परिसंपत्तियों में लॉक करें।"
                )
            )
        ) else listOf(
            CreativeFaqItem(
                question = "When will I clear my loans?",
                paragraphs = listOf(
                    "Because your 6th–11th house balance works alongside ${ctx.planetName("Saturn")} in your ${ctx.ordinalHouse(ctx.planet("Saturn")?.houseNumber ?: 6)}, the $wealthDebtWindow cycle stands out as a decisive window for reducing liabilities and lifting monthly free cashflow.",
                    "Applying the Cooling-Off rule and routing bonuses directly into highest-rate principal alongside your 2nd Lord ${ctx.planetName(lord2)} rhythm builds rapid debt-free momentum."
                )
            ),
            CreativeFaqItem(
                question = "Will I have sudden financial windfall?",
                paragraphs = listOf(
                    "Because your 11th Lord ${ctx.planetName(lord11)} activates your ${ctx.ordinalHouse(h11)} and ${wealthDhanWindow} aligns with supportive Dasha lords, asymmetric upside can arrive through equity vesting, performance bonuses, property appreciation, or smart IP royalties.",
                    "To turn windfalls into permanent security, pre-commit 70% of any unexpected lump sum directly into diversified long-horizon vehicles highlighted by ${topWealthMatches.firstOrNull()?.title ?: "quality compounders"}."
                )
            )
        )

        val wealthStory = buildStoryParagraphs(
            ctx = ctx,
            categoryKey = "wealth",
            strongestPlanetCanon = primaryDriver,
            keyLordCanon = lord2,
            keyHouseNum = 2,
            divCode = "D1",
            overallScore = overallScore
        )

        return CategoryDeepDive(
            category = ReadingCategory.WEALTH,
            title = if (isHi) "धन, संपत्ति एवं समृद्धि" else "Wealth & Prosperity",
            overallScore = overallScore,
            signatureTitle = wealthHeadline,
            archetype = archetype,
            narrative = narrative,
            diversePaths = diversePaths,
            employmentPaths = emptyList(),
            businessPaths = emptyList(),
            employmentVsBusinessGuidance = "",
            powerBars = powerBars,
            proofItems = proofItems,
            yogaNotes = relevantYogas,
            divisionalChartCode = ReadingCategory.WEALTH.divisionalChartCode,
            absorbedReadings = absorbedReadings,
            crossReferences = crossReferences,
            whatToDo = whatToDo.distinct().take(4),
            safetyAdvice = safetyAdvice,
            remedy = remedy,
            personalityParagraph = libInsights.personalityParagraph,
            strengthsParagraph = libInsights.strengthsParagraph,
            challengesParagraph = libInsights.challengesParagraph,
            superpower = libInsights.superpower,
            growthArea = libInsights.growthArea,
            heroSummary = heroSummary,
            astroMathPill = astroMathPill,
            topWealthMatches = topWealthMatches,
            tier2Card = wealthMindsetCard,
            tier3SectionTitle = if (isHi) "सार्वभौमिक समृद्धि नियम" else "Universal Wealth Rules",
            tier3Cards = tier3WealthRules,
            creativeFaqs = wealthFaqs,
            heroGlyph = wealthGlyph,
            storyParagraphs = wealthStory
        )
    }

    private fun buildMarriage(ctx: ChartContext, baseRelReading: String): CategoryDeepDive {
        val isHi = ctx.isHi
        val lord7 = ctx.houseLordCanon(7)
        val lord5 = ctx.houseLordCanon(5)
        val lord9 = ctx.houseLordCanon(9)
        val lord11 = ctx.houseLordCanon(11)
        val mangalActive = ctx.yogaPresent("Mangal")
        val d9LagnaSign = ctx.divLagnaSign("D9")

        val familyGrace = ((ctx.planetScore("Jupiter", "D9") + ctx.houseStrength(2, "D9")) / 2).coerceIn(25, 96)

        val topRel = listOf("Venus", "Jupiter", "Moon", "Mercury", "Mars").maxByOrNull { ctx.planetScore(it, "D9") } ?: "Venus"
        val basis = if (isHi) {
            "सप्तमेश: ${ctx.planetName(lord7)} • पंचमेश: ${ctx.planetName(lord5)} • नवांश (D9) लग्न: ${ctx.rashiName(d9LagnaSign)} • शुक्र D9 भाव ${ctx.divHouse("D9", "Venus")}"
        } else {
            "7th Lord: ${ctx.planetName(lord7)} • 5th Lord: ${ctx.planetName(lord5)} • D9 Navamsha Lagna: ${ctx.rashiName(d9LagnaSign)} • Venus in D9 House ${ctx.divHouse("D9", "Venus")}"
        }

        val giftTitle: String
        val baseRecommendedPaths: List<String>
        val baseCautiousZones: List<String>
        when (topRel) {
            "Venus" -> if (isHi) {
                giftTitle = "स्नेही जीवनसाथी"
                baseRecommendedPaths = listOf("परस्पर सम्मान पर आधारित संबंध", "कला व साझा जीवनशैली", "प्रेम व पारिवारिक स्वीकृति का संगम")
                baseCautiousZones = listOf("अनकही भावनात्मक अपेक्षाएँ", "तुलना व दिखावे का दबाव")
            } else {
                giftTitle = "Devoted Harmony Partner"
                baseRecommendedPaths = listOf("Warm Shared Lifestyle", "Aesthetic & Emotional Reciprocity", "Harmonious Family Integration")
                baseCautiousZones = listOf("Unspoken Expectations", "External Social Comparison")
            }

            "Jupiter" -> if (isHi) {
                giftTitle = "धर्म व परिवार आधार"
                baseRecommendedPaths = listOf("साझा जीवन मूल्यों वाला विवाह", "वरिष्ठों के आशीर्वाद से जुड़ाव", "दीर्घकालिक पारिवारिक स्थिरता")
                baseCautiousZones = listOf("अत्यधिक उपदेशात्मक संवाद", "व्यावहारिक लचीलेपन की कमी")
            } else {
                giftTitle = "Dharma & Family Anchor"
                baseRecommendedPaths = listOf("Values-Aligned Partnership", "Wisdom-Guided Family Union", "Long-Term Domestic Prosperity")
                baseCautiousZones = listOf("Overly Rigid Ideals", "Neglecting Playful Affection")
            }

            "Mercury" -> if (isHi) {
                giftTitle = "बौद्धिक संवाद साथी"
                baseRecommendedPaths = listOf("खुले संवाद और मित्रता से जुड़ाव", "साझा बौद्धिक व यात्रा रुचियाँ", "पारदर्शी साझेदारी")
                baseCautiousZones = listOf("हर भावना का अति-तर्क विश्लेषण", "अधूरी बात पर निष्कर्ष")
            } else {
                giftTitle = "Intellectual Companion"
                baseRecommendedPaths = listOf("Friendship-First Marriage", "Shared Curiosity & Dialogue", "Transparent Co-Planning")
                baseCautiousZones = listOf("Over-Intellectualizing Feelings", "Distracted Listening")
            }

            else -> if (isHi) {
                giftTitle = "सशक्त निष्ठावान सहभागी"
                baseRecommendedPaths = listOf("पारस्परिक स्वतंत्रता व विश्वास", "समान लक्ष्यों की साझेदारी", "धैर्यपूर्ण संवाद")
                baseCautiousZones = listOf("जल्दबाज़ी में तीखी प्रतिक्रिया", "अकेले निर्णय थोपना")
            } else {
                giftTitle = "Loyal & Passionate Equal"
                baseRecommendedPaths = listOf("Balanced Autonomy & Loyalty", "Shared Purpose & Mutual Grit", "Honest Direct Communication")
                baseCautiousZones = listOf("Heated Reactive Exchanges", "Unilateral Decision-Making")
            }
        }

        val intimacyBar = ((ctx.planetScore("Venus", "D9") + ctx.houseStrength(7, "D9")) / 2).coerceIn(25, 96)
        val relCommBar = ((ctx.planetScore("Mercury", "D9") + ctx.houseStrength(3, "D9")) / 2).coerceIn(25, 96)
        val independenceBar = ((ctx.planetScore("Mars", "D9") + if (mangalActive) 58 else 78) / 2).coerceIn(25, 96)
        val loyaltyBar = ((ctx.planetScore("Saturn", "D9") + ctx.planetScore(lord7, "D9")) / 2).coerceIn(25, 96)
        val compromiseBar = ((ctx.planetScore("Moon", "D9") + ctx.planetScore("Jupiter", "D9")) / 2).coerceIn(25, 96)
        val overallScore = ((intimacyBar + relCommBar + independenceBar + loyaltyBar + compromiseBar) / 5).coerceIn(28, 95)

        val powerBars = if (isHi) listOf(
            PowerBarMetric(
                label = "आत्मीयता (Intimacy)",
                score = intimacyBar,
                note = "${ctx.whyChartClause("शुक्र", "शुक्र", "Venus")}, आपके संबंधों में स्नेह, कोमलता और गहरा भावनात्मक जुड़ाव रहता है।",
                glyph = "♀"
            ),
            PowerBarMetric(
                label = "संवाद तालमेल (Communication)",
                score = relCommBar,
                note = "${ctx.whyChartClause("बुध", "बुध", "Mercury")}, खुली मित्रता और शांत बातचीत हर उलझन को सहज सुलझाती है।",
                glyph = "☿"
            ),
            PowerBarMetric(
                label = "व्यक्तिगत स्वायत्तता (Independence)",
                score = independenceBar,
                note = "${ctx.whyChartClause("मंगल", "मंगल", "Mars")}, आप दोनों के व्यक्तिगत विकास और सम्मानजनक स्थान को महत्व देते हैं।",
                glyph = "♂"
            ),
            PowerBarMetric(
                label = "दीर्घकालिक निष्ठा (Loyalty)",
                score = loyaltyBar,
                note = "${ctx.whyChartClause("सप्तमेश", "सप्तमेश", lord7)}, आपका समर्पण और वचनबद्धता समय के साथ और मजबूत होती है।",
                glyph = "♄"
            ),
            PowerBarMetric(
                label = "सहज सामंजस्य (Compromise)",
                score = compromiseBar,
                note = "${ctx.whyChartClause("चंद्र", "चंद्र", "Moon")}, करुणा और सुनने की कला घर में मधुर संतुलन बनाए रखती है।",
                glyph = "☽"
            )
        ) else listOf(
            PowerBarMetric(
                label = "Intimacy",
                score = intimacyBar,
                note = "${ctx.whyChartClause("Venus", "शुक्र", "Venus")}, warmth and affectionate devotion form the heart of your bond.",
                glyph = "♀"
            ),
            PowerBarMetric(
                label = "Communication",
                score = relCommBar,
                note = "${ctx.whyChartClause("Mercury", "बुध", "Mercury")}, candid friendship and playful dialogue keep closeness clear.",
                glyph = "☿"
            ),
            PowerBarMetric(
                label = "Independence",
                score = independenceBar,
                note = "${ctx.whyChartClause("Mars", "मंगल", "Mars")}, healthy individuality makes mutual partnership stronger.",
                glyph = "♂"
            ),
            PowerBarMetric(
                label = "Loyalty",
                score = loyaltyBar,
                note = "${ctx.whyChartClause("7th Lord", "सप्तमेश", lord7)}, steadfast commitment deepens reliably across seasons.",
                glyph = "♄"
            ),
            PowerBarMetric(
                label = "Compromise",
                score = compromiseBar,
                note = "${ctx.whyChartClause("Moon", "चंद्र", "Moon")}, empathetic listening turns small differences into grace.",
                glyph = "☽"
            )
        )

        val d9Resonance = intimacyBar
        val harmony = loyaltyBar
        val emotionalBond = compromiseBar

        val proofItems = listOf(lord7, "Venus", "Jupiter", "Moon", "Mars").distinct().map { canon ->
            val p = ctx.planet(canon)
            val d9Sign = ctx.divSign("D9", canon)
            val d9House = ctx.divHouse("D9", canon)
            val (status, ok) = ctx.dignityBadge(canon)
            val tag = when {
                canon == lord7 && isHi -> " (सप्तमेश)"
                canon == lord7 -> " (7th Lord)"
                else -> ""
            }
            AstroProofItem(
                title = "${ctx.planetName(canon)}$tag",
                placementDetail = if (isHi) "भाव ${p?.houseNumber ?: 1} (${p?.rashiName ?: ""}) • D9 भाव $d9House (${ctx.rashiName(d9Sign)})"
                else "House ${p?.houseNumber ?: 1} (${p?.rashiName ?: ""}) • D9 House $d9House (${ctx.rashiName(d9Sign)})",
                statusLabel = status,
                isFavorable = ok
            )
        }

        val whatToDo = mutableListOf<String>()
        val extraSafety = mutableListOf<String>()
        if (mangalActive) {
            whatToDo += if (isHi) "विवाह से पूर्व कुंडली मिलान (अष्टकूट व मांगलिक साम्य) अवश्य करवाएँ और संवाद में धैर्य रखें।" else "Prioritize full horoscope compatibility matching and practice calm, unhurried dialogue."
            extraSafety += if (isHi) "तीव्र भावनात्मक क्षणों में 24 घंटे रुककर शांत मन से महत्वपूर्ण निर्णय लें।" else "Give heated moments a gentle 24-hour pause before making lasting relationship decisions."
        } else {
            whatToDo += if (isHi) "संबंधों में नियमित समय, आभार और स्पष्ट पारिवारिक परिचय को प्राथमिकता दें।" else "Invest in shared rituals, transparent expectations, and warm family introductions."
        }
        if (ctx.planetScore("Venus", "D9") >= 62) {
            whatToDo += if (isHi) "रचनात्मक साथ और परस्पर सम्मान के माध्यम से आत्मीयता को सुदृढ़ करें।" else "Nurture shared joy, mutual courtesy, and thoughtful small gestures daily."
        } else {
            whatToDo += if (isHi) "विश्वास बनाने के लिए अपेक्षाओं को सरल और स्पष्ट रखें।" else "Keep partnership expectations grounded and express appreciation explicitly."
        }
        extraSafety += if (isHi) "निजी वैवाहिक चर्चाओं में साथी से सीधे व सम्मानपूर्ण संवाद को प्राथमिकता दें।" else "Prioritize direct partner-to-partner clarity before involving outside opinions."
        extraSafety += if (isHi) "जीवन-मूल्यों, वित्त और पारिवारिक लक्ष्यों पर आरंभ से ही खुला संवाद रखें।" else "Align early on shared values, finances, and family goals with mutual kindness."

        val nurturePlanet = listOf(lord7, "Venus", "Jupiter", "Moon").minByOrNull { ctx.planetScore(it, "D9") } ?: "Venus"
        val remedy = planetRemedy(nurturePlanet, ReadingCategory.MARRIAGE, isHi)
        val relevantYogas = ctx.yogasAndDoshas.filter { it.canonicalKey in setOf("Mangal", "Gajakesari", "Kemadruma") }

        val narrative = if (isHi) {
            "$baseRelReading\n\nनवांश (D9) कुंडली में ${ctx.rashiName(d9LagnaSign)} लग्न तथा सप्तमेश ${ctx.planetName(lord7)} संबंधों की दीर्घकालिक परिपक्वता दर्शाते हैं। परस्पर सम्मान और ${ctx.planetName(ctx.mahaLordCanonical)} दशा में धैर्यपूर्ण संवाद से दांपत्य सुख खिलता है।"
        } else {
            "$baseRelReading\n\nYour D9 Navamsha Lagna in ${ctx.rashiName(d9LagnaSign)} and 7th Lord ${ctx.planetName(lord7)} shape your deeper partnership rhythm. Empathy, shared values, and mindful communication cultivate enduring harmony."
        }

        val marriageSignIndex = ctx.planet("Venus")?.rashiIndex ?: ctx.planet("Moon")?.rashiIndex ?: ctx.lagna.rashiIndex
        val libInsights = resolveCategoryLibraryInsights(
            ctx = ctx,
            categoryKey = "marriage",
            personalitySignIndex = marriageSignIndex,
            primaryHouse = 7,
            keyHouses = listOf(7, 5, 2, 4, 1),
            candidatePlanets = listOf(lord7, "Venus", "Jupiter", "Moon", lord5).distinct(),
            divCode = "D9",
            fallbackNarrative = narrative
        )

        val (cautiousZones, safetyAdvice) = computeCautiousZonesAndSafety(
            ctx = ctx,
            categoryKey = "marriage",
            growthPlanet = libInsights.growthPlanet,
            extraCautious = baseCautiousZones,
            extraSafety = extraSafety
        )

        val archetype = CategoryArchetype(
            giftTitle = giftTitle,
            derivationBasis = basis,
            recommendedPaths = baseRecommendedPaths,
            cautiousZones = cautiousZones
        )

        val diversePaths = computeDynamicPaths(
            ctx = ctx,
            categoryKey = "marriage",
            jsonSectionKey = "diverse_paths_by_planet",
            rankedPlanets = libInsights.rankedPlanets,
            fallbackHouse = 7,
            fallbackLabels = baseRecommendedPaths
        )

        val marriageTopicSpecs = listOf(
            TopicEvalSpec(
                topicId = "marriage_timing",
                score = ((harmony + d9Resonance) / 2).coerceIn(25, 96),
                driverPlanet = lord7,
                fallbackHouse = 7,
                timingLords = listOf("Venus", "Jupiter", "Moon", lord7, lord11),
                fallbackTitleEn = "Marriage Timing & Union Windows",
                fallbackTitleHi = "विवाह योग एवं शुभ समय",
                fallbackBodyEn = "Active planetary cycles around {year_range} activate your 7th House and {planet} themes in your {house}, inviting committed partnership milestones.",
                fallbackBodyHi = "{house} में {planet} और दशा चक्र के प्रभाव से {year_range} की अवधि विवाह और सुखद साझेदारी के लिए अत्यंत अनुकूल है।",
                fallbackBasisEn = "7th Lord ${ctx.planetName(lord7)} • D9 Navamsha",
                fallbackBasisHi = "सप्तमेश ${ctx.planetName(lord7)} • नवांश कुंडली"
            ),
            TopicEvalSpec(
                topicId = "marriage_type",
                score = ((emotionalBond + ctx.planetScore("Venus", "D9")) / 2).coerceIn(25, 96),
                driverPlanet = "Venus",
                fallbackHouse = 5,
                timingLords = listOf("Venus", "Rahu", "Moon", lord5, lord7),
                fallbackTitleEn = "Love vs Arranged Resonance & Partner Vibe",
                fallbackTitleHi = "प्रेम बनाम पारिवारिक विवाह व जीवनसाथी स्वभाव",
                fallbackBodyEn = "Venus and 5th/7th synergy shape how rapport forms naturally; {year_range} brings meaningful emotional alignment through shared values.",
                fallbackBodyHi = "शुक्र तथा पंचम-सप्तम भाव का तालमेल जीवनसाथी के चयन की शैली बताता है; {year_range} में सहज भावनात्मक जुड़ाव बढ़ेगा।",
                fallbackBasisEn = "Venus, 5th & 7th House Balance",
                fallbackBasisHi = "शुक्र व पंचम-सप्तम भाव संयोग"
            ),
            TopicEvalSpec(
                topicId = "mangal_dosha",
                score = if (mangalActive) 50 else 84,
                driverPlanet = "Mars",
                fallbackHouse = 7,
                timingLords = listOf("Mars", "Jupiter", "Venus", lord7),
                fallbackTitleEn = "Mangal Alignment & Partnership Temperament",
                fallbackTitleHi = "मांगलिक प्रभाव एवं ऊर्जा संतुलन",
                fallbackBodyEn = if (mangalActive) {
                    "Mars energy highlights passionate devotion and drive; Shaadi Milan matching and calm pacing keep harmony radiant across {year_range}."
                } else {
                    "Your chart carries peaceful Mars balance without active Mangal Dosha pressure, supporting smooth domestic rapport through {year_range}."
                },
                fallbackBodyHi = if (mangalActive) {
                    "मंगल की प्रबल ऊर्जा संबंधों में निष्ठा व उत्साह लाती है; गुण मिलान और मधुर संवाद {year_range} में दांपत्य संतुलन को और गहरा बनाते हैं।"
                } else {
                    "आपकी कुंडली में मंगल ऊर्जा संतुलित व सौम्य है, जिससे {year_range} में वैवाहिक सामंजस्य सहज बना रहता है।"
                },
                fallbackBasisEn = "Mars Placement • 1st/4th/7th/8th/12th Check",
                fallbackBasisHi = "मंगल भाव परीक्षण • नवांश संतुलन"
            ),
            TopicEvalSpec(
                topicId = "children_timing",
                score = ((ctx.houseStrength(5, "D7") + ctx.planetScore("Jupiter", "D7")) / 2).coerceIn(25, 96),
                driverPlanet = "Jupiter",
                fallbackHouse = 5,
                timingLords = listOf("Jupiter", "Moon", "Venus", lord5, lord9),
                fallbackTitleEn = "Family Expansion & Children Blessings (D7)",
                fallbackTitleHi = "संतान सुख एवं पारिवारिक विस्तार (D7)",
                fallbackBodyEn = "Your 5th House and D7 Saptamsha chart highlight Jupiterian grace and nurturing family milestones blossoming around {year_range}.",
                fallbackBodyHi = "पंचम भाव तथा सप्तांश (D7) कुंडली में बृहस्पति की कृपा से {year_range} के दौरान संतान व पारिवारिक आनंद के शुभ संकेत हैं।",
                fallbackBasisEn = "D7 Saptamsha • 5th Lord & Jupiter",
                fallbackBasisHi = "सप्तांश (D7) • पंचमेश व गुरु"
            )
        )

        val absorbedReadings = computeAbsorbedTopics(ctx, "marriage", marriageTopicSpecs)
        val crossReferences = computeCrossReferences(ctx, "marriage", libInsights.strongestPlanet, 7)

        val h7 = ctx.planet(lord7)?.houseNumber ?: 7
        val isUnder21 = ctx.ageYears < 21
        val goldenWindow = absorbedReadings.firstOrNull { it.topicId == "marriage_timing" }?.timingWindow
            ?: "${ctx.referenceYear}–${ctx.referenceYear + 2}"

        val heroSummary = if (isHi) {
            "सप्तमेश ${ctx.planetName(lord7)} (भाव $h7) और नवांश (D9) लग्न ${ctx.rashiName(d9LagnaSign)} आपके जीवन में '$giftTitle' जैसी परिपक्व साझेदारी का संकेत देते हैं। परस्पर संवाद, सम्मान और साझा मूल्य आपके संबंधों को दीर्घकालिक आनंद देते हैं।"
        } else {
            "Because your 7th Lord ${ctx.planetName(lord7)} resides in your ${ctx.ordinalHouse(h7)} and ${ctx.rashiName(d9LagnaSign)} rises in your D9 Navamsha chart, partnership reflects '$giftTitle'. Mutual respect, warmth, and honest dialogue create enduring grace."
        }
        val astroMathPill = if (isHi) {
            "सप्तमेश: ${ctx.planetName(lord7)} (भाव $h7) • D9 लग्न: ${ctx.rashiName(d9LagnaSign)}"
        } else {
            "7th Lord: ${ctx.planetName(lord7)} (House $h7) • D9 Lagna: ${ctx.rashiName(d9LagnaSign)}"
        }

        val spouseProfiles = rankAndNormalizeCatalogMatches(
            ctx = ctx,
            sectionKey = "spouse_profiles_by_planet",
            divCode = "D9",
            categoryBoostHouses = setOf(7, 5, 2, 4, 11),
            topLimit = 3
        )

        val loveScore = ctx.planetScore("Venus", "D9") + ctx.houseStrength(5, "D9") + (if (ctx.planet("Rahu")?.houseNumber in setOf(5, 7)) 12 else 0)
        val arrangedScore = ctx.planetScore("Jupiter", "D9") + ctx.houseStrength(2, "D9") + ctx.houseStrength(9, "D9")
        val loveVsArrangedBadge = when {
            loveScore > arrangedScore + 10 -> if (isHi) "प्रेम विवाह प्रबल संकेत" else "Love-Led Resonance"
            arrangedScore > loveScore + 10 -> if (isHi) "पारिवारिक सहमति विवाह" else "Family-Aligned Union"
            else -> if (isHi) "प्रेम व परिवार का सुंदर संगम" else "Love + Family Blend"
        }
        val loveVsArrangedCard = if (isHi) {
            BroadScopeItem(
                title = loveVsArrangedBadge,
                description = "आपकी पत्रिका में पंचम भाव (${ctx.planetName(lord5)}) और सप्तम भाव (${ctx.planetName(lord7)}) का संबंध दर्शाता है कि आत्मीय मित्रता और परिवार के आशीर्वाद दोनों का संतुलन आपके लिए सबसे सुखद रहता है। जब दोनों परिवारों में विचारों की पारदर्शिता होती है, तब बंधन सहजता से पक्का होता है। सही परिचय मित्रों, कार्यक्षेत्र या सम्माननीय पारिवारिक माध्यम से मिलना अत्यंत शुभ रहेगा।",
                chartReference = "क्योंकि आपके पंचमेश ${ctx.planetName(lord5)}, सप्तमेश ${ctx.planetName(lord7)} और D9 शुक्र साथ मिलकर मार्ग प्रशस्त करते हैं।"
            )
        } else {
            BroadScopeItem(
                title = loveVsArrangedBadge,
                description = "Your 5th Lord (${ctx.planetName(lord5)}) and 7th Lord (${ctx.planetName(lord7)}) in Navamsha show how affection and family harmony reinforce each other. Meaningful connections grow best through shared values, mutual curiosity, and warm introductions respected by loved ones. Transparent expectations from the start ensure joy whether meeting organically or through trusted elders.",
                chartReference = "Because your 5th Lord ${ctx.planetName(lord5)} and 7th Lord ${ctx.planetName(lord7)} coordinate cleanly across D1 and D9 Navamsha."
            )
        }

        val tier3MarriageCards = if (isHi) listOf(
            MindsetRuleCard(
                title = "सहज विनोद व स्नेही दृष्टिकोण (The Playful Approach)",
                body = "दैनिक जिम्मेदारियों के बीच हल्की मुस्कान, छोटी सैर और बिना एजेंडे की बातचीत को नियमित स्थान दें। हँसी और कृतज्ञता संबंधों की ऊर्जा को सदैव तरोताजा रखते हैं।",
                chartReference = "${ctx.whyChartClause("शुक्र", "शुक्र", "Venus")} और पंचम भाव आनंद व स्नेह का पोषण करते हैं।"
            ),
            MindsetRuleCard(
                title = "संवाद एवं सम्मानजनक निजी स्थान (Communicating Space)",
                body = "व्यस्त या भावुक क्षणों में शांत श्वास के साथ अपनी आवश्यकता स्पष्ट कहें—'मुझे सोचने के लिए थोड़ी शांति चाहिए, फिर हम मिलकर बात करेंगे।' यह स्पष्टता गलतफहमी से बचाती है।",
                chartReference = "${ctx.whyChartClause("बुध", "बुध", "Mercury")} और चंद्र संवाद में मधुर मर्यादा लाते हैं।"
            )
        ) else listOf(
            MindsetRuleCard(
                title = "The Playful Approach",
                body = "Protect unhurried moments for shared humor, spontaneous walks, and lighthearted curiosity outside logistics. Playfulness dissolves minor friction far faster than endless analysis.",
                chartReference = "${ctx.whyChartClause("Venus", "शुक्र", "Venus")}, energizing your 5th-house joy and romantic warmth."
            ),
            MindsetRuleCard(
                title = "Communicating Space",
                body = "Name personal recharge needs gently without pulling away silently—let your partner know when a calm 20-minute reset helps you show up more present. Clear boundaries deepen emotional safety.",
                chartReference = "${ctx.whyChartClause("Mercury", "बुध", "Mercury")}, guiding respectful dialogue alongside your Moon."
            )
        )

        val topSpouseTitle = spouseProfiles.firstOrNull()?.title ?: if (isHi) "समझदार एवं सहयोगी साथी" else "Warm, Principled Partner"
        val marriageFaqs = if (isUnder21) {
            if (isHi) listOf(
                CreativeFaqItem(
                    question = "भविष्य की तैयारी: अभी मैं अपने संबंधों व आत्म-विश्वास को कैसे निखारूं?",
                    badge = "Future Focus (< 21)",
                    paragraphs = listOf(
                        "क्योंकि आपकी आयु अभी २१ वर्ष से कम है, आपकी कुंडली विवाह की समय-गणना के बजाय आत्म-विकास, स्वस्थ मित्रता और भावनात्मक परिपक्वता के निर्माण को प्राथमिकता देती है। सप्तमेश ${ctx.planetName(lord7)} इस समय आपको स्वयं के जीवन मूल्यों और लक्ष्यों को स्पष्ट करने का सुंदर अवसर दे रहे हैं।",
                        "अच्छी आदतें, सम्मानजनक संवाद और अध्ययन-कौशल पर ध्यान देने से भविष्य की हर साझेदारी स्वाभाविक रूप से सुखी और सशक्त बनेगी।"
                    )
                ),
                CreativeFaqItem(
                    question = "मेरी पत्रिका में भविष्य के लिए कौन से संबंध गुण विकसित हो रहे हैं?",
                    badge = "Future Focus",
                    paragraphs = listOf(
                        "क्योंकि नवांश लग्न ${ctx.rashiName(d9LagnaSign)} और शुक्र आपके स्नेह व सृजन को दिशा देते हैं, आपमें निष्ठा, सहानुभूति और संतुलित सीमाएँ तय करने की कला तेजी से विकसित हो रही है।",
                        "मित्रों और परिवार के साथ धैर्यपूर्ण श्रोता बनना अभी से आपके आने वाले वर्षों को अत्यंत सौहार्दपूर्ण बनाता है।"
                    )
                )
            ) else listOf(
                CreativeFaqItem(
                    question = "Future Focus: How do I build emotional poise and healthy bonds now?",
                    badge = "Future Focus (< 21)",
                    paragraphs = listOf(
                        "Because you are under 21, AstroVeda prioritizes personal growth, authentic friendships, and emotional maturity ahead of marriage timing calculations. Your 7th Lord ${ctx.planetName(lord7)} in your ${ctx.ordinalHouse(h7)} currently encourages clarity about your own values, education, and self-respect.",
                        "Developing honest communication, patience, and self-confidence today lays the happiest possible foundation for all future partnerships."
                    )
                ),
                CreativeFaqItem(
                    question = "Which relationship strengths are blooming in my chart for the future?",
                    badge = "Future Focus",
                    paragraphs = listOf(
                        "Because ${ctx.rashiName(d9LagnaSign)} rises in your D9 Navamsha alongside ${ctx.planetName("Venus")} in your ${ctx.ordinalHouse(ctx.planet("Venus")?.houseNumber ?: 7)}, natural warmth, loyalty, and creative empathy are steadily maturing.",
                        "Surrounding yourself with encouraging peers who honor boundaries helps these gifts flourish effortlessly."
                    )
                )
            )
        } else {
            if (isHi) listOf(
                CreativeFaqItem(
                    question = "क्या मेरा वैवाहिक जीवन सुखमय रहेगा?",
                    badge = "अवधि: $goldenWindow",
                    paragraphs = listOf(
                        "क्योंकि आपके सप्तमेश ${ctx.planetName(lord7)} ${ctx.ordinalHouse(h7)} में स्थित हैं और नवांश (D9) लग्न ${ctx.rashiName(d9LagnaSign)} है, आपकी पत्रिका में परस्पर सम्मान और गहरे स्नेह के सुंदर योग हैं। $goldenWindow का समय वैवाहिक जीवन और सौहार्द को नई ऊँचाई देने के लिए विशेष शुभ है।",
                        "खुला संवाद, छोटे दैनिक आभार और विवाह से पूर्व शांत गुण-मिलान (अष्टकूट) आपके दांपत्य को दीर्घकालिक सुख और स्थिरता प्रदान करते हैं।"
                    )
                ),
                CreativeFaqItem(
                    question = "मेरे भावी जीवनसाथी का स्वभाव और व्यक्तित्व कैसा होगा?",
                    paragraphs = listOf(
                        "क्योंकि D9 कुंडली और सप्तम भाव में ${ctx.planetName(topRel)} की ऊर्जा प्रमुख है, आपके साथी में '$topSpouseTitle' जैसे गुण प्रमुखता से दिखाई देते हैं। वे भरोसेमंद, संवेदनशील और आपके लक्ष्यों के प्रति सहयोगी दृष्टिकोण रखेंगे।",
                        "जब आप दोनों पारिवारिक मूल्यों और व्यक्तिगत रचनात्मकता के बीच सम्मानजनक संतुलन रखते हैं, तब घर में सहज आनंद और प्रगति बनी रहती है।"
                    )
                )
            ) else listOf(
                CreativeFaqItem(
                    question = "Will my marriage be happy?",
                    badge = "Golden Window: $goldenWindow",
                    paragraphs = listOf(
                        "Because your 7th Lord ${ctx.planetName(lord7)} sits in your ${ctx.ordinalHouse(h7)} while ${ctx.rashiName(d9LagnaSign)} anchors your D9 Navamsha chart, your chart supports loyal companionship and heartfelt mutual care. The $goldenWindow period illuminates especially joyful partnership milestones.",
                        "Combining transparent dialogue with Shaadi Milan compatibility mindfulness keeps trust luminous and peaceful across decades."
                    )
                ),
                CreativeFaqItem(
                    question = "What is the personality of my future spouse?",
                    paragraphs = listOf(
                        "Because ${ctx.planetName(topRel)} shapes your 7th-house and D9 Navamsha resonance, your partner strongly reflects the '$topSpouseTitle' signature—bringing genuine integrity, warm attentiveness, and steady companionship.",
                        "They appreciate emotional honesty and shared growth, thriving alongside you when playful affection balances everyday responsibilities."
                    )
                )
            )
        }

        val finalTier1Cards = spouseProfiles.mapIndexed { idx, card ->
            if (!isUnder21 && idx == 0) {
                card.copy(sector = "${card.sector} • $goldenWindow")
            } else card
        }

        val marriageHeadline = universalSignatureHeadline(ReadingCategory.MARRIAGE, topRel, isHi)
        val marriageGlyph = planetGlyph(topRel)
        val marriageStory = buildStoryParagraphs(
            ctx = ctx,
            categoryKey = "marriage",
            strongestPlanetCanon = topRel,
            keyLordCanon = lord7,
            keyHouseNum = 7,
            divCode = "D9",
            overallScore = overallScore
        )
        val enrichedMarriageHero = if (isHi) {
            "नवांश (D9) लग्न ${ctx.rashiName(d9LagnaSign)} में ${ctx.planetName(topRel)} के सान्निध्य और ${ctx.ordinalHouse(h7)} में स्थित सप्तमेश ${ctx.planetName(lord7)} से आपका दांपत्य-भाव '$marriageHeadline' का स्वरूप धारण करता है। स्नेही संवाद, पारस्परिक सम्मान और धैर्यपूर्ण विश्वास आपके घर में स्थायी सौहार्द रचते हैं। सक्रिय ${ctx.planetName(ctx.mahaLordCanonical)} दशा आत्मीय समझ को और गहरा बनाती है।"
        } else {
            "With ${ctx.planetName(topRel)} illuminating your D9 Navamsha (${ctx.rashiName(d9LagnaSign)}) alongside 7th Lord ${ctx.planetName(lord7)} in your ${ctx.ordinalHouse(h7)}, your relational energy expresses '$marriageHeadline'. Honest affection, shared warmth, and loyal reciprocity nurture lifelong security. Under your active ${ctx.planetName(ctx.mahaLordCanonical)} period, patient closeness deepens smoothly."
        }

        return CategoryDeepDive(
            category = ReadingCategory.MARRIAGE,
            title = if (isHi) "विवाह एवं संबंध" else "Relationships & Marriage",
            overallScore = overallScore,
            signatureTitle = marriageHeadline,
            archetype = archetype,
            narrative = narrative,
            diversePaths = diversePaths,
            employmentPaths = emptyList(),
            businessPaths = emptyList(),
            employmentVsBusinessGuidance = "",
            powerBars = powerBars,
            proofItems = proofItems,
            yogaNotes = relevantYogas,
            divisionalChartCode = ReadingCategory.MARRIAGE.divisionalChartCode,
            absorbedReadings = absorbedReadings,
            crossReferences = crossReferences,
            whatToDo = whatToDo.distinct().take(4),
            safetyAdvice = safetyAdvice,
            remedy = remedy,
            personalityParagraph = libInsights.personalityParagraph,
            strengthsParagraph = libInsights.strengthsParagraph,
            challengesParagraph = libInsights.challengesParagraph,
            superpower = libInsights.superpower,
            growthArea = libInsights.growthArea,
            heroSummary = enrichedMarriageHero,
            astroMathPill = astroMathPill,
            tier1Cards = finalTier1Cards,
            tier2Card = loveVsArrangedCard,
            tier2Badge = loveVsArrangedBadge,
            tier3SectionTitle = if (isHi) "संबंध मनोविज्ञान" else "Relationship Psychology",
            tier3Cards = tier3MarriageCards,
            creativeFaqs = marriageFaqs,
            isAgeGatedFutureFocus = isUnder21,
            heroGlyph = marriageGlyph,
            storyParagraphs = marriageStory
        )
    }

    private fun buildHealth(ctx: ChartContext, baseHealthReading: String): CategoryDeepDive {
        val isHi = ctx.isHi
        val lord1 = ctx.houseLordCanon(1)
        val lord6 = ctx.houseLordCanon(6)
        val lord8 = ctx.houseLordCanon(8)
        val sadeSati = ctx.yogaPresent("SadeSati")
        val kemadruma = ctx.yogaPresent("Kemadruma")

        val staminaBar = ((ctx.planetScore(lord1, "D1") + ctx.planetScore("Sun", "D1")) / 2).coerceIn(28, 96)
        val mentalResilienceBar = ((ctx.planetScore("Moon", "D1") + (if (kemadruma) 48 else 76) + (if (sadeSati) 52 else 80)) / 3).coerceIn(28, 96)
        val digestiveFireBar = ((ctx.planetScore("Mars", "D1") + ctx.houseStrength(5, "D1")) / 2).coerceIn(28, 96)
        val sleepQualityBar = ((ctx.planetScore("Moon", "D1") + ctx.planetScore("Venus", "D1")) / 2).coerceIn(28, 96)
        val immunityBar = ((ctx.planetScore("Saturn", "D1") + ctx.planetScore("Jupiter", "D1") + ctx.houseStrength(6, "D1")) / 3).coerceIn(28, 96)
        val overallScore = ((staminaBar + mentalResilienceBar + digestiveFireBar + sleepQualityBar + immunityBar) / 5).coerceIn(30, 95)

        val elementIdx = ctx.lagna.rashiIndex % 4
        val basis = if (isHi) {
            "लग्न: ${ctx.lagna.rashiName} • लग्नेश: ${ctx.planetName(lord1)} • सूर्य भाव ${ctx.planet("Sun")?.houseNumber ?: 1} • चंद्र भाव ${ctx.planet("Moon")?.houseNumber ?: 1}"
        } else {
            "Lagna: ${ctx.lagna.rashiName} • Lagna Lord: ${ctx.planetName(lord1)} • Sun in House ${ctx.planet("Sun")?.houseNumber ?: 1} • Moon in House ${ctx.planet("Moon")?.houseNumber ?: 1}"
        }

        val giftTitle: String
        val baseRecommendedPaths: List<String>
        val baseCautiousZones: List<String>
        when (elementIdx) {
            0 -> if (isHi) {
                giftTitle = "तेजस्वी अग्नि प्रकृति"
                baseRecommendedPaths = listOf("प्रातःकालीन सूर्य-प्रकाश व योग", "शीतल जल व संतुलित आहार", "मध्यम कार्डियो व्यायाम")
                baseCautiousZones = listOf("देर रात तक जागना", "अत्यधिक तीखा व तला भोजन")
            } else {
                giftTitle = "Radiant Pitta Vitality"
                baseRecommendedPaths = listOf("Morning Sunlight & Breathwork", "Cooling Hydration & Whole Foods", "Consistent Moderate Cardio")
                baseCautiousZones = listOf("Overheating Late-Night Work", "Skipping Meals Under Deadline Stress")
            }

            1 -> if (isHi) {
                giftTitle = "स्थिर पृथ्वी प्रकृति"
                baseRecommendedPaths = listOf("दैनिक तेज चाल व शक्ति-अभ्यास", "हल्का व ताज़ा सात्विक भोजन", "नियमित प्रातः जागरण")
                baseCautiousZones = listOf("दिन में लंबी नींद व आलस्य", "अत्यधिक भारी मीठा भोजन")
            } else {
                giftTitle = "Grounded Kapha Endurance"
                baseRecommendedPaths = listOf("Brisk Daily Movement & Strength", "Light Warm Fresh Meals", "Early Rising Routine")
                baseCautiousZones = listOf("Prolonged Sedentary Hours", "Heavy Late-Evening Dinners")
            }

            2 -> if (isHi) {
                giftTitle = "चपल वायु प्रकृति"
                baseRecommendedPaths = listOf("नियमित निद्रा समय-सारणी", "गुनगुना व पोषक आहार", "ध्यान व अनुलोम-विलोम प्राणायाम")
                baseCautiousZones = listOf("अनियमित भोजन व अनिद्रा", "लगातार स्क्रीन और अति-विचार")
            } else {
                giftTitle = "Agile Vata Rhythm"
                baseRecommendedPaths = listOf("Fixed Sleep-Wake Schedule", "Warm Grounding Meals", "Pranayama & Nervous System Rest")
                baseCautiousZones = listOf("Erratic Fasting or Irregular Sleep", "Continuous Screen Overstimulation")
            }

            else -> if (isHi) {
                giftTitle = "सौम्य जल प्रकृति"
                baseRecommendedPaths = listOf("जल-संतुलन व शांत संध्या", "तैराकी या सौम्य स्ट्रेचिंग", "सकारात्मक भावनात्मक विश्राम")
                baseCautiousZones = listOf("भावनात्मक तनाव में भोजन छोड़ना", "ठंडा व बासी भोजन")
            } else {
                giftTitle = "Intuitive Lunar Vitality"
                baseRecommendedPaths = listOf("Mindful Hydration & Calm Evenings", "Gentle Mobility & Swimming", "Restorative Emotional Boundaries")
                baseCautiousZones = listOf("Absorbing Work Stress Into Digestion", "Cold Heavy Late Snacks")
            }
        }

        val powerBars = if (isHi) listOf(
            PowerBarMetric(
                label = "शारीरिक ऊर्जा (Physical Stamina)",
                score = staminaBar,
                note = "${ctx.whyChartClause("लग्नेश", "लग्नेश", lord1)}, नियमित प्रातःकालीन सक्रियता आपकी स्फूर्ति को दोगुना करती है।",
                glyph = "☉"
            ),
            PowerBarMetric(
                label = "मानसिक सुदृढ़ता (Mental Resilience)",
                score = mentalResilienceBar,
                note = "${ctx.whyChartClause("चंद्र", "चंद्र", "Moon")}, शांत श्वास अभ्यास भावनात्मक संतुलन को दृढ़ बनाता है।",
                glyph = "☽"
            ),
            PowerBarMetric(
                label = "पाचन अग्नि (Digestive Fire)",
                score = digestiveFireBar,
                note = "${ctx.whyChartClause("मंगल", "मंगल", "Mars")}, समय पर ताज़ा भोजन आपकी जठराग्नि को संतुलित रखता है।",
                glyph = "♂"
            ),
            PowerBarMetric(
                label = "निद्रा गुणवत्ता (Sleep Quality)",
                score = sleepQualityBar,
                note = "${ctx.whyChartClause("शुक्र", "शुक्र", "Venus")}, रात्रि शांत दिनचर्या गहरी व ताज़गीभरी नींद प्रदान करती है।",
                glyph = "♀"
            ),
            PowerBarMetric(
                label = "रोग प्रतिरोध (Immunity)",
                score = immunityBar,
                note = "${ctx.whyChartClause("शनि", "शनि", "Saturn")}, अनुशासित जलपान व विश्राम आपकी प्रतिरक्षा क्षमता को मजबूत बनाते हैं।",
                glyph = "♄"
            )
        ) else listOf(
            PowerBarMetric(
                label = "Physical Stamina",
                score = staminaBar,
                note = "${ctx.whyChartClause("Ascendant Lord", "लग्नेश", lord1)}, morning movement builds clean, enduring physical energy.",
                glyph = "☉"
            ),
            PowerBarMetric(
                label = "Mental Resilience",
                score = mentalResilienceBar,
                note = "${ctx.whyChartClause("Moon", "चंद्र", "Moon")}, calm breathwork steadies emotional clarity through busy weeks.",
                glyph = "☽"
            ),
            PowerBarMetric(
                label = "Digestive Fire",
                score = digestiveFireBar,
                note = "${ctx.whyChartClause("Mars", "मंगल", "Mars")}, warm whole foods at regular hours keep Agni bright.",
                glyph = "♂"
            ),
            PowerBarMetric(
                label = "Sleep Quality",
                score = sleepQualityBar,
                note = "${ctx.whyChartClause("Venus", "शुक्र", "Venus")}, serene pre-bed habits invite deeply restorative rest.",
                glyph = "♀"
            ),
            PowerBarMetric(
                label = "Immunity",
                score = immunityBar,
                note = "${ctx.whyChartClause("Saturn", "शनि", "Saturn")}, consistent pacing and hydration strengthen long-term vitality.",
                glyph = "♄"
            )
        )

        val vitality = staminaBar
        val immunity = immunityBar
        val mentalCalm = mentalResilienceBar

        val proofItems = listOf(lord1, "Sun", "Moon", lord6, "Saturn").distinct().map { canon ->
            val p = ctx.planet(canon)
            val (status, ok) = ctx.dignityBadge(canon)
            val tag = if (canon == lord1) (if (isHi) " (लग्नेश)" else " (Lagna Lord)") else ""
            AstroProofItem(
                title = "${ctx.planetName(canon)}$tag",
                placementDetail = if (isHi) "भाव ${p?.houseNumber ?: 1} • ${p?.rashiName ?: ""} (${p?.formattedDegree ?: ""})"
                else "House ${p?.houseNumber ?: 1} • ${p?.rashiName ?: ""} (${p?.formattedDegree ?: ""})",
                statusLabel = status,
                isFavorable = ok
            )
        }

        val whatToDo = mutableListOf<String>()
        val extraSafety = mutableListOf<String>()
        if (mentalCalm < 62 || sadeSati) {
            whatToDo += if (isHi) "सोने से 45 मिनट पहले स्क्रीन बंद कर शांत श्वास अभ्यास करें।" else "Keep a strict 45-minute screen-free wind-down with slow breathwork before bed."
            extraSafety += if (isHi) "संध्या के बाद कैफीन और तनावपूर्ण चर्चाओं से दूरी रखकर गहरी नींद सुनिश्चित करें।" else "Protect evening nervous-system calm by pausing caffeine and tense debates after sunset."
        } else {
            whatToDo += if (isHi) "प्रतिदिन प्रातः कम से कम 25 मिनट ताज़ी हवा में टहलें या योग करें।" else "Anchor mornings with 25+ minutes of outdoor movement or Surya Namaskar."
        }
        if (vitality < 62) {
            whatToDo += if (isHi) "ताज़ा गर्म भोजन व समय पर जलपान को प्राथमिकता दें तथा थकान के संकेतों का सम्मान करें।" else "Eat warm, freshly prepared meals on fixed hours and honor early fatigue cues."
            extraSafety += if (isHi) "भोजन समय पर लें और काम के बीच छोटे विश्राम अंतराल जोड़ें।" else "Honor regular meal times and schedule short recovery pauses between deep tasks."
        } else {
            whatToDo += if (isHi) "वार्षिक स्वास्थ्य जाँच और संतुलित जल-पोषण द्वारा अपनी प्राकृतिक ऊर्जा सुरक्षित रखें।" else "Protect your strong baseline through consistent hydration and preventive checkups."
            extraSafety += if (isHi) "अति-उत्साह में भी 7–8 घंटे की नियमित निद्रा को प्राथमिकता बनाए रखें।" else "Even with lively stamina, guard consistent 7–8 hour sleep recovery every night."
        }

        val nurturePlanet = listOf(lord1, "Sun", "Moon", "Saturn").minByOrNull { ctx.planetScore(it, "D1") } ?: lord1
        val remedy = planetRemedy(nurturePlanet, ReadingCategory.HEALTH, isHi)
        val relevantYogas = ctx.yogasAndDoshas.filter { it.canonicalKey in setOf("SadeSati", "Kemadruma", "Gajakesari") }

        val narrative = if (isHi) {
            "$baseHealthReading\n\nलग्नेश ${ctx.planetName(lord1)} और चंद्र की स्थिति यह सिखाती है कि आपका शरीर नियमित दिनचर्या, संतुलित पाचन और शांत मानसिक विश्राम से सबसे अधिक खिलता है।"
        } else {
            "$baseHealthReading\n\nWith ${ctx.planetName(lord1)} as Lagna Lord alongside Sun and Moon vitality, your body responds swiftly to rhythmic sleep, clean nutrition, and steady daily pacing."
        }

        val libInsights = resolveCategoryLibraryInsights(
            ctx = ctx,
            categoryKey = "health",
            personalitySignIndex = ctx.lagna.rashiIndex,
            primaryHouse = 1,
            keyHouses = listOf(1, 6, 4, 8),
            candidatePlanets = listOf(lord1, "Sun", "Moon", "Mars", "Saturn").distinct(),
            divCode = "D1",
            fallbackNarrative = narrative
        )

        val (cautiousZones, safetyAdvice) = computeCautiousZonesAndSafety(
            ctx = ctx,
            categoryKey = "health",
            growthPlanet = libInsights.growthPlanet,
            extraCautious = baseCautiousZones,
            extraSafety = extraSafety
        )

        val archetype = CategoryArchetype(
            giftTitle = giftTitle,
            derivationBasis = basis,
            recommendedPaths = baseRecommendedPaths,
            cautiousZones = cautiousZones
        )

        val diversePaths = computeDynamicPaths(
            ctx = ctx,
            categoryKey = "health",
            jsonSectionKey = "diverse_paths_by_planet",
            rankedPlanets = libInsights.rankedPlanets,
            fallbackHouse = 1,
            fallbackLabels = baseRecommendedPaths
        )

        val healthTopicSpecs = listOf(
            TopicEvalSpec(
                topicId = "health_areas",
                score = ((vitality + immunity) / 2).coerceIn(28, 96),
                driverPlanet = lord1,
                fallbackHouse = 1,
                timingLords = listOf("Saturn", "Rahu", "Mars", lord6, lord8),
                fallbackTitleEn = "Constitutional Wellness & Mindfulness Zones",
                fallbackTitleHi = "शारीरिक प्रकृति एवं विशेष स्वास्थ्य देखभाल क्षेत्र",
                fallbackBodyEn = "Your Lagna element and {planet} in your {house} favor rhythmic breathwork, clean digestion, and preventive balance through {year_range}.",
                fallbackBodyHi = "{house} में {planet} और आपकी शारीरिक प्रकृति के अनुसार {year_range} में संतुलित पाचन, प्राणायाम और नियमित विश्राम उत्तम स्वास्थ्य देंगे।",
                fallbackBasisEn = "Lagna Lord ${ctx.planetName(lord1)} • 6th House Routine",
                fallbackBasisHi = "लग्नेश ${ctx.planetName(lord1)} • षष्ठ भाव दिनचर्या"
            ),
            TopicEvalSpec(
                topicId = "sade_sati",
                score = if (sadeSati) 52 else 84,
                driverPlanet = "Saturn",
                fallbackHouse = 1,
                timingLords = listOf("Saturn", "Jupiter", "Moon"),
                fallbackTitleEn = "Saturn Transit & Sade Sati Steadiness",
                fallbackTitleHi = "शनि गोचर एवं साढ़े साती प्रभाव मार्गदर्शन",
                fallbackBodyEn = if (sadeSati) {
                    "Current Saturn transit teaches grounded patience, clean ethics, and restorative sleep; steady habits carry you triumphantly across {year_range}."
                } else {
                    "You are free from active Sade Sati transit pressure, enjoying clear lunar momentum and resilient emotional calm during {year_range}."
                },
                fallbackBodyHi = if (sadeSati) {
                    "वर्तमान शनि गोचर अनुशासन, धैर्य और सेवा-भाव से जीवन को निखारने का समय है; शांत दिनचर्या से {year_range} अत्यंत सुगम रहेगा।"
                } else {
                    "आप वर्तमान में साढ़े साती के दबाव से मुक्त हैं, जिससे {year_range} के दौरान मानसिक शांति और ऊर्जा का उत्तम प्रवाह बना रहेगा।"
                },
                fallbackBasisEn = "Moon Sign Transit • Saturn Cycle",
                fallbackBasisHi = "चंद्र राशि गोचर • शनि चक्र"
            )
        )

        val absorbedReadings = computeAbsorbedTopics(ctx, "health", healthTopicSpecs)
        val crossReferences = computeCrossReferences(ctx, "health", libInsights.strongestPlanet, 1)

        val h1 = ctx.planet(lord1)?.houseNumber ?: 1
        val h6 = ctx.planet(lord6)?.houseNumber ?: 6
        val heroSummary = if (isHi) {
            "लग्नेश ${ctx.planetName(lord1)} (भाव $h1) और षष्ठेश ${ctx.planetName(lord6)} (भाव $h6) आपकी '$giftTitle' शारीरिक प्रकृति का निर्माण करते हैं। लयबद्ध निद्रा, ताज़ा सात्विक भोजन और सचेत विश्राम आपकी जीवन शक्ति को सदैव उज्ज्वल रखते हैं।"
        } else {
            "Because your Ascendant Lord ${ctx.planetName(lord1)} occupies your ${ctx.ordinalHouse(h1)} while 6th Lord ${ctx.planetName(lord6)} guides your ${ctx.ordinalHouse(h6)}, your constitution reflects '$giftTitle'. Rhythmic sleep, warm nourishment, and daily movement preserve radiant vitality."
        }
        val astroMathPill = if (isHi) {
            "लग्नेश: ${ctx.planetName(lord1)} (भाव $h1) • षष्ठेश: ${ctx.planetName(lord6)} (भाव $h6)"
        } else {
            "Ascendant Lord: ${ctx.planetName(lord1)} (House $h1) • 6th Lord: ${ctx.planetName(lord6)} (House $h6)"
        }

        val healthCareAreas = rankAndNormalizeCatalogMatches(
            ctx = ctx,
            sectionKey = "vulnerabilities_by_planet",
            divCode = "D1",
            categoryBoostHouses = setOf(1, 6, 8, 12),
            topLimit = 3
        )

        val healthActionPlan = if (isHi) {
            BroadScopeItem(
                title = "दैनिक स्वास्थ्य एवं आहार कार्य योजना",
                description = "आहार: '${giftTitle}' प्रकृति के अनुकूल ताज़ा, गर्म और सुपाच्य घर का भोजन समय पर लें। जल-संतुलन: प्रातः उठते ही एक गिलास गुनगुना जल पिएँ और दिन भर नियमित सिप लेते रहें। दिनचर्या: सूर्योदय के साथ २५ मिनट पैदल चलें या योग करें और सोने से ४५ मिनट पहले रोशनी हल्की रखें।",
                chartReference = "${ctx.whyChartClause("लग्नेश", "लग्नेश", lord1)} और चंद्र आपकी दैनिक लय व पाचन से सीधे जुड़े हैं।"
            )
        } else {
            BroadScopeItem(
                title = "Diet, Hydration & Daily Vitality Routine",
                description = "Diet: Favor warm, freshly prepared whole meals tailored to your '$giftTitle' constitution at steady meal times. Hydration: Begin mornings with room-temperature water and sip consistently between meals. Daily Routine: Anchor 25 minutes of morning sunlight movement and protect a calm 7.5-hour sleep window.",
                chartReference = "${ctx.whyChartClause("Ascendant Lord", "लग्नेश", lord1)} alongside Moon in your ${ctx.ordinalHouse(ctx.planet("Moon")?.houseNumber ?: 4)}."
            )
        }

        val tier3HealthCards = if (isHi) listOf(
            MindsetRuleCard(
                title = "सचेत निवारक दृष्टिकोण (The Prevention Mindset)",
                body = "शरीर के सूक्ष्म थकान संकेतों को टालने के बजाय तुरंत हल्का विश्राम, स्ट्रेचिंग और नियमित स्वास्थ्य जाँच अपनाएँ। प्रारंभिक सजगता छोटे असंतुलन को आसानी से दूर रखती है।",
                chartReference = "${ctx.whyChartClause("षष्ठेश", "षष्ठेश", lord6)} दैनिक स्वास्थ्य अनुशासन से अद्भुत सुरक्षा देता है।"
            ),
            MindsetRuleCard(
                title = "संध्याकालीन डिजिटल विश्राम (The Digital Detox)",
                body = "रात्रि सोने से ४५ मिनट पूर्व फोन व तेज स्क्रीन से दूरी बनाकर धीमी श्वास या शांत संगीत का आनंद लें। यह छोटा अभ्यास तंत्रिका तंत्र को गहरी शांति और ताज़गी देता है।",
                chartReference = "${ctx.whyChartClause("चंद्र", "चंद्र", "Moon")} और बुध के आराम से मन और निद्रा निर्मल रहती है।"
            )
        ) else listOf(
            MindsetRuleCard(
                title = "The Prevention Mindset",
                body = "Treat subtle posture tightness, thirst, or early fatigue as friendly dashboard signals rather than waiting for burnout. Routine checkups and micro-breaks keep baseline resilience consistently high.",
                chartReference = "${ctx.whyChartClause("6th Lord", "षष्ठेश", lord6)}, rewarding steady preventive routines in daily life."
            ),
            MindsetRuleCard(
                title = "The Digital Detox",
                body = "Park screens 45 minutes before bedtime and switch to dim lighting, journaling, or calm nasal breathing. Protecting evening nervous-system quiet multiplies morning stamina effortlessly.",
                chartReference = "${ctx.whyChartClause("Moon", "चंद्र", "Moon")}, governing restorative sleep rhythm and emotional calm."
            )
        )

        val careTitles = healthCareAreas.take(2).joinToString(if (isHi) " तथा " else " and ") { it.title }
        val healthFaqs = if (isHi) listOf(
            CreativeFaqItem(
                question = "शरीर के किन अंगों को विशेष देखभाल की आवश्यकता है?",
                paragraphs = listOf(
                    "क्योंकि आपके लग्नेश ${ctx.planetName(lord1)} ${ctx.ordinalHouse(h1)} में और षष्ठेश ${ctx.planetName(lord6)} ${ctx.ordinalHouse(h6)} में स्थित हैं, आपकी पत्रिका में ${careTitles.ifBlank { "पाचन एवं नेत्र-विश्राम" }} जैसे क्षेत्रों को प्रेमपूर्वक व नियमित देखभाल देना सर्वोत्तम रहता है।",
                    "सचेत मुद्रा (posture), आंखों को हर घंटे छोटे विश्राम और हल्के सुपाच्य रात्रि भोजन से ये अंग सदैव सहज और स्वस्थ बने रहते हैं।"
                )
            ),
            CreativeFaqItem(
                question = "मैं अपनी शारीरिक ऊर्जा और सहनशक्ति कैसे बढ़ा सकता/सकती हूँ?",
                paragraphs = listOf(
                    "क्योंकि आपका सूर्य ${ctx.ordinalHouse(ctx.planet("Sun")?.houseNumber ?: 1)} में और मंगल ${ctx.ordinalHouse(ctx.planet("Mars")?.houseNumber ?: 3)} में प्राण-ऊर्जा संचालित करते हैं, प्रातःकाल का सूर्य प्रकाश, नियमित मध्यम कार्डियो या सूर्य नमस्कार आपकी सहनशक्ति को तीव्र गति से बढ़ाते हैं।",
                    "दोपहर का संतुलित भोजन और रात्रि डिजिटल विश्राम मिलकर आपकी ऊर्जा को पूरे दिन समान और स्फूर्तिवान बनाए रखते हैं।"
                )
            )
        ) else listOf(
            CreativeFaqItem(
                question = "Which body areas need extra care?",
                paragraphs = listOf(
                    "Because your Ascendant Lord ${ctx.planetName(lord1)} sits in your ${ctx.ordinalHouse(h1)} and 6th Lord ${ctx.planetName(lord6)} guides your ${ctx.ordinalHouse(h6)}, giving mindful daily attention to ${careTitles.ifBlank { "digestion, eyes, and posture" }} supports lifelong comfort.",
                    "Simple ergonomic resets, warm hydration, and pacing intense workloads smoothly keep these sensitive systems flourishing."
                )
            ),
            CreativeFaqItem(
                question = "How can I improve my stamina?",
                paragraphs = listOf(
                    "Because your Sun activates your ${ctx.ordinalHouse(ctx.planet("Sun")?.houseNumber ?: 1)} alongside Mars in your ${ctx.ordinalHouse(ctx.planet("Mars")?.houseNumber ?: 3)}, pairing 15 minutes of morning sunlight with consistent brisk movement builds clean aerobic endurance.",
                    "Protecting 7–8 hours of regular sleep through the evening Digital Detox turns steady daily habits into vibrant physical stamina."
                )
            )
        )

        val medicalDisclaimer = if (isHi) {
            "चिकित्सीय अस्वीकरण: यहाँ प्रस्तुत ज्योतिषीय विश्लेषण केवल पारंपरिक जीवनशैली व आध्यात्मिक मार्गदर्शन के लिए है। यह किसी भी प्रकार की चिकित्सीय जांच, निदान या उपचार का विकल्प नहीं है—किसी भी स्वास्थ्य समस्या हेतु योग्य चिकित्सक से परामर्श अवश्य लें।"
        } else {
            "Medical Disclaimer: Astrological wellness insights provided here are strictly for traditional lifestyle reflection and personal mindfulness. They do not constitute medical diagnosis or treatment—always consult a qualified healthcare professional for medical advice."
        }

        val strongestHealthPlanet = listOf(lord1, "Sun", "Moon", "Jupiter", "Mars", "Venus")
            .maxByOrNull { ctx.planetScore(it, "D1") } ?: lord1
        val healthHeadline = universalSignatureHeadline(ReadingCategory.HEALTH, strongestHealthPlanet, isHi)
        val healthGlyph = planetGlyph(strongestHealthPlanet)
        val healthStory = buildStoryParagraphs(
            ctx = ctx,
            categoryKey = "health",
            strongestPlanetCanon = strongestHealthPlanet,
            keyLordCanon = lord1,
            keyHouseNum = 1,
            divCode = "D1",
            overallScore = overallScore
        )
        val enrichedHealthHero = if (isHi) {
            "${ctx.lagna.rashiName} लग्न में लग्नेश ${ctx.planetName(lord1)} और प्रमुख प्राण-कारक ${ctx.planetName(strongestHealthPlanet)} आपकी स्वास्थ्य-प्रकृति को '$healthHeadline' की सशक्त ऊर्जा प्रदान करते हैं। प्रातःकालीन धूप, ताज़ा सुपाच्य भोजन और नियमित विश्राम आपकी प्रतिरक्षा को सहज सुदृढ़ रखते हैं। सक्रिय ${ctx.planetName(ctx.mahaLordCanonical)} दशा में संतुलित दिनचर्या दीर्घकालिक स्फूर्ति देती है।"
        } else {
            "Led by ${ctx.planetName(strongestHealthPlanet)} and Ascendant Lord ${ctx.planetName(lord1)} guiding your ${ctx.lagna.rashiName} Lagna, your constitution radiates '$healthHeadline'. Consistent morning sunlight, clean hydration, and restful sleep rhythms multiply energy naturally. Your active ${ctx.planetName(ctx.mahaLordCanonical)} period rewards calm daily pacing."
        }

        return CategoryDeepDive(
            category = ReadingCategory.HEALTH,
            title = if (isHi) "स्वास्थ्य एवं प्राण ऊर्जा" else "Health & Vitality",
            overallScore = overallScore,
            signatureTitle = healthHeadline,
            archetype = archetype,
            narrative = narrative,
            diversePaths = diversePaths,
            employmentPaths = emptyList(),
            businessPaths = emptyList(),
            employmentVsBusinessGuidance = "",
            powerBars = powerBars,
            proofItems = proofItems,
            yogaNotes = relevantYogas,
            divisionalChartCode = ReadingCategory.HEALTH.divisionalChartCode,
            absorbedReadings = absorbedReadings,
            crossReferences = crossReferences,
            whatToDo = whatToDo.distinct().take(4),
            safetyAdvice = safetyAdvice,
            remedy = remedy,
            personalityParagraph = libInsights.personalityParagraph,
            strengthsParagraph = libInsights.strengthsParagraph,
            challengesParagraph = libInsights.challengesParagraph,
            superpower = libInsights.superpower,
            growthArea = libInsights.growthArea,
            heroSummary = enrichedHealthHero,
            astroMathPill = astroMathPill,
            tier1Cards = healthCareAreas,
            tier2Card = healthActionPlan,
            tier3SectionTitle = if (isHi) "समग्र स्वास्थ्य दृष्टिकोण" else "Health Mindset",
            tier3Cards = tier3HealthCards,
            creativeFaqs = healthFaqs,
            medicalDisclaimer = medicalDisclaimer,
            heroGlyph = healthGlyph,
            storyParagraphs = healthStory
        )
    }

    private fun buildDharma(ctx: ChartContext): CategoryDeepDive {
        val isHi = ctx.isHi
        val lord5 = ctx.houseLordCanon(5)
        val lord9 = ctx.houseLordCanon(9)
        val lord12 = ctx.houseLordCanon(12)
        val atmakaraka = findAtmakaraka(ctx.planets) ?: ctx.planet(lord9)
        val akCanon = atmakaraka?.canonicalName ?: "Jupiter"
        val akName = ctx.planetName(akCanon)
        val akDeg = atmakaraka?.formattedDegree ?: "15° 00'"
        val akHouse = atmakaraka?.houseNumber ?: 9
        val ketu = ctx.planet("Ketu")
        val ketuSign = ketu?.rashiName ?: ctx.lagna.rashiName
        val ketuHouse = ketu?.houseNumber ?: 12

        val karmicScore = ((ctx.planetScore("Saturn", "D9") + ctx.planetScore(akCanon, "D9")) / 2).coerceIn(28, 96)
        val intuitionScore = ((ctx.planetScore("Ketu", "D9") + ctx.planetScore("Moon", "D9")) / 2).coerceIn(28, 96)
        val detachmentScore = ((ctx.houseStrength(12, "D9") + ctx.planetScore("Ketu", "D9")) / 2).coerceIn(28, 96)
        val serviceScore = ((ctx.planetScore("Jupiter", "D9") + ctx.houseStrength(9, "D9")) / 2).coerceIn(28, 96)
        val overallScore = ((karmicScore + intuitionScore + detachmentScore + serviceScore) / 4).coerceIn(30, 95)

        val basis = if (isHi) {
            "आत्मकारक: $akName ($akDeg, भाव $akHouse) • केतु: $ketuSign (भाव $ketuHouse) • नवमेश: ${ctx.planetName(lord9)}"
        } else {
            "Atmakaraka: $akName ($akDeg, House $akHouse) • Ketu: $ketuSign (House $ketuHouse) • 9th Lord: ${ctx.planetName(lord9)}"
        }

        val giftTitle: String
        val baseRecommendedPaths: List<String>
        val baseCautiousZones: List<String>
        when (akCanon) {
            "Jupiter" -> if (isHi) {
                giftTitle = "विवेकशील धर्म साधक"
                baseRecommendedPaths = listOf("गुरु-शिष्य ज्ञान परंपरा", "नैतिक मार्गदर्शन व शिक्षण", "तीर्थ एवं चिंतन", "निःस्वार्थ सेवा")
                baseCautiousZones = listOf("अत्यधिक सैद्धांतिक उपदेश", "दैनिक कर्म की उपेक्षा")
            } else {
                giftTitle = "Wisdom Dharma Keeper"
                baseRecommendedPaths = listOf("Mentorship & Living Wisdom", "Ethical Stewardship", "Contemplative Study", "Generous Seva")
                baseCautiousZones = listOf("Moral Rigidity Over Empathy", "Neglecting Grounded Daily Duties")
            }

            "Saturn" -> if (isHi) {
                giftTitle = "कर्मयोगी पथ-निर्माता"
                baseRecommendedPaths = listOf("निःस्वार्थ सेवा एवं श्रम सम्मान", "दीर्घकालिक अनुशासन", "सत्यनिष्ठ नेतृत्व")
                baseCautiousZones = listOf("स्वयं पर अत्यधिक कठोरता", "अतीत की अनावश्यक चिंता")
            } else {
                giftTitle = "Steadfast Karma Yogi"
                baseRecommendedPaths = listOf("Quiet Selfless Service", "Enduring Craft & Duty", "Compassionate Fairness")
                baseCautiousZones = listOf("Harsh Self-Judgment", "Carrying Everyone Else's Burdens Alone")
            }

            "Sun" -> if (isHi) {
                giftTitle = "सत्य-प्रकाश वाहक"
                baseRecommendedPaths = listOf("निस्वार्थ नेतृत्व", "सत्य और स्पष्टता का आचरण", "प्रातःकालीन सूर्य-ध्यान")
                baseCautiousZones = listOf("अहंकार से जुड़ाव", "अकेले नियंत्रण रखने की प्रवृत्ति")
            } else {
                giftTitle = "Sovereign Truth Seeker"
                baseRecommendedPaths = listOf("Servant Leadership", "Authentic Integrity", "Dawn Contemplation")
                baseCautiousZones = listOf("Attaching Worth to Applause", "Impatient Pride")
            }

            "Moon" -> if (isHi) {
                giftTitle = "करुणामय अंतर्ज्ञानी"
                baseRecommendedPaths = listOf("भावनात्मक उपचार व सहानुभूति", "भक्ति एवं संगीत ध्यान", "परिवार-समुदाय सेवा")
                baseCautiousZones = listOf("दूसरों के दुख में अत्यधिक डूबना", "स्व-देखभाल भूल जाना")
            } else {
                giftTitle = "Compassionate Sanctuary Maker"
                baseRecommendedPaths = listOf("Empathic Healing Presence", "Devotional Reflection", "Nurturing Community Service")
                baseCautiousZones = listOf("Absorbing Ambient Stress", "Forgetting Quiet Solitude")
            }

            else -> if (isHi) {
                giftTitle = "जाग्रत जीवन-अन्वेषक"
                baseRecommendedPaths = listOf("ज्ञान व सत्य की खोज", "सचेत स्वाध्याय", "रचनात्मक सेवा एवं ध्यान")
                baseCautiousZones = listOf("विचारों का भटकाव", "सहज सरलता से दूरी")
            } else {
                giftTitle = "Awakened Truth Explorer"
                baseRecommendedPaths = listOf("Inquiry & Mindfulness", "Creative Stewardship", "Balanced Detachment in Action")
                baseCautiousZones = listOf("Restless Over-Analysis", "Mistaking Complexity for Depth")
            }
        }

        // Exact 4 Soul Alignment Bars for Page 5 (Dharma)
        val powerBars = if (isHi) listOf(
            PowerBarMetric(
                label = "कर्म पाठ (Karmic Lessons)",
                score = karmicScore,
                note = "${ctx.whyChartClause("आत्मकारक", "आत्मकारक", akCanon)}, आपके जीवन अनुभव परिपक्व विवेक और सहनशीलता गढ़ते हैं।",
                glyph = "♄"
            ),
            PowerBarMetric(
                label = "अंतर्ज्ञान (Intuition)",
                score = intuitionScore,
                note = "${ctx.whyChartClause("केतु", "केतु", "Ketu")}, आपकी सूक्ष्म अंतर्दृष्टि सत्य को शब्दों से पहले पहचान लेती है।",
                glyph = "☋"
            ),
            PowerBarMetric(
                label = "सहज अनासक्ति (Detachment)",
                score = detachmentScore,
                note = "${ctx.whyChartClause("द्वादशेश", "द्वादशेश", lord12)}, आप परिणाम की चिंता से मुक्त होकर शांत कर्म कर पाते हैं।",
                glyph = "☊"
            ),
            PowerBarMetric(
                label = "निःस्वार्थ सेवा (Service)",
                score = serviceScore,
                note = "${ctx.whyChartClause("नवमेश", "नवमेश", lord9)}, दूसरों के उत्थान में सहभागी बनना आपको गहरी आत्मिक तृप्ति देता है।",
                glyph = "♃"
            )
        ) else listOf(
            PowerBarMetric(
                label = "Karmic Lessons",
                score = karmicScore,
                note = "${ctx.whyChartClause("Atmakaraka", "आत्मकारक", akCanon)}, life milestones refine deep humility and enduring inner clarity.",
                glyph = "♄"
            ),
            PowerBarMetric(
                label = "Intuition",
                score = intuitionScore,
                note = "${ctx.whyChartClause("Ketu", "केतु", "Ketu")}, quiet inner discernment senses truth beneath surface noise.",
                glyph = "☋"
            ),
            PowerBarMetric(
                label = "Detachment",
                score = detachmentScore,
                note = "${ctx.whyChartClause("12th Lord", "द्वादशेश", lord12)}, letting go of anxious control invites serene grace.",
                glyph = "☊"
            ),
            PowerBarMetric(
                label = "Service",
                score = serviceScore,
                note = "${ctx.whyChartClause("9th Lord", "नवमेश", lord9)}, uplifting others transforms everyday craft into sacred Dharma.",
                glyph = "♃"
            )
        )

        val proofItems = listOf(akCanon, "Ketu", lord9, lord5, "Jupiter").distinct().map { canon ->
            val p = ctx.planet(canon)
            val (status, ok) = ctx.dignityBadge(canon)
            val tag = when {
                canon == akCanon && isHi -> " (आत्मकारक)"
                canon == akCanon -> " (Atmakaraka)"
                canon == lord9 && isHi -> " (नवमेश)"
                canon == lord9 -> " (9th Lord)"
                else -> ""
            }
            AstroProofItem(
                title = "${ctx.planetName(canon)}$tag",
                placementDetail = if (isHi) "भाव ${p?.houseNumber ?: 1} • ${p?.rashiName ?: ""} (${p?.formattedDegree ?: ""})"
                else "House ${p?.houseNumber ?: 1} • ${p?.rashiName ?: ""} (${p?.formattedDegree ?: ""})",
                statusLabel = status,
                isFavorable = ok
            )
        }

        val whatToDo = if (isHi) listOf(
            "प्रातः १० मिनट शांत ध्यान, प्राणायाम या कृतज्ञता लेखन से अपने अंतःकरण को केंद्रित करें।",
            "सप्ताह में एक बार ज्ञान, भोजन या मार्गदर्शन के माध्यम से निःस्वार्थ सेवा (Seva) अवश्य करें।",
            "अपने आत्मकारक $akName की उच्च शिक्षाओं को रोज़मर्रा के व्यवहार व विनम्रता में उतारें।"
        ) else listOf(
            "Spend 10 quiet morning minutes in meditation, pranayama, or reflective journaling.",
            "Practice consistent selfless seva weekly—sharing wisdom, meals, or patient kindness.",
            "Anchor your Atmakaraka $akName lessons into everyday humility and honest craft."
        )
        val extraSafety = if (isHi) listOf(
            "आध्यात्मिक अभ्यास को दैनिक जिम्मेदारियों से पलायन नहीं, बल्कि कर्म की गुणवत्ता बढ़ाने का माध्यम बनाएँ।",
            "नए गुरु या मार्ग चुनते समय विवेक, सरलता और पारदर्शी आचरण की परीक्षा अवश्य करें।"
        ) else listOf(
            "Let spiritual insight deepen everyday reliability rather than avoiding practical commitments.",
            "Favor grounded teachers and transparent practices over dramatic claims."
        )

        val remedy = planetRemedy(akCanon, ReadingCategory.DHARMA, isHi)
        val relevantYogas = ctx.yogasAndDoshas.filter { it.canonicalKey in setOf("Gajakesari", "KaalSarp", "Kemadruma") }

        val narrative = if (isHi) {
            "जैमिनी ज्योतिष के अनुसार आपकी कुंडली में सर्वाधिक अंश वाला ग्रह $akName आपका आत्मकारक है (भाव $akHouse) और मोक्ष-कारक केतु $ketuSign (भाव $ketuHouse) में स्थित हैं। ये दोनों मिलकर आपके जीवन को '$giftTitle' की आध्यात्मिक दिशा और सार्थक सेवा का उद्देश्य प्रदान करते हैं।"
        } else {
            "In Jaimini Jyotish, $akName at $akDeg is your chart's Atmakaraka (Soul Planet) in your ${ctx.ordinalHouse(akHouse)}, paired with Ketu in $ketuSign (House $ketuHouse). Together they awaken the '$giftTitle' path—uniting inner stillness with purposeful service."
        }

        val dharmaSignIndex = atmakaraka?.rashiIndex ?: ctx.planet("Jupiter")?.rashiIndex ?: ctx.lagna.rashiIndex
        val libInsights = resolveCategoryLibraryInsights(
            ctx = ctx,
            categoryKey = "dharma",
            personalitySignIndex = dharmaSignIndex,
            primaryHouse = 9,
            keyHouses = listOf(9, 5, 12, 1, akHouse),
            candidatePlanets = listOf(akCanon, "Ketu", lord9, "Jupiter", lord5, "Saturn").distinct(),
            divCode = "D9",
            fallbackNarrative = narrative
        )

        val (cautiousZones, safetyAdvice) = computeCautiousZonesAndSafety(
            ctx = ctx,
            categoryKey = "dharma",
            growthPlanet = libInsights.growthPlanet,
            extraCautious = baseCautiousZones,
            extraSafety = extraSafety
        )

        val archetype = CategoryArchetype(
            giftTitle = giftTitle,
            derivationBasis = basis,
            recommendedPaths = baseRecommendedPaths,
            cautiousZones = cautiousZones
        )

        val diversePaths = computeDynamicPaths(
            ctx = ctx,
            categoryKey = "dharma",
            jsonSectionKey = "diverse_paths_by_planet",
            rankedPlanets = libInsights.rankedPlanets,
            fallbackHouse = 9,
            fallbackLabels = baseRecommendedPaths
        )

        val dharmaTopicSpecs = listOf(
            TopicEvalSpec(
                topicId = "competitive_exams",
                score = ((karmicScore + serviceScore) / 2).coerceIn(28, 96),
                driverPlanet = akCanon,
                fallbackHouse = 9,
                timingLords = listOf(akCanon, "Jupiter", "Mercury", lord9, lord5),
                fallbackTitleEn = "Soul Clarity, Mastery & Dharma Milestones",
                fallbackTitleHi = "आत्मिक स्पष्टता एवं ज्ञान सिद्धि समय",
                fallbackBodyEn = "Structured discipline led by {planet} in your {house} deepens inner wisdom and meaningful credentials across {year_range}.",
                fallbackBodyHi = "{house} में {planet} और सार्थक साधना {year_range} के दौरान उच्च ज्ञान एवं उद्देश्य सिद्धि प्रदान करती है।",
                fallbackBasisEn = "Atmakaraka $akName • 5th & 9th Houses",
                fallbackBasisHi = "आत्मकारक $akName • पंचम व नवम भाव"
            ),
            TopicEvalSpec(
                topicId = "foreign_travel",
                score = ((intuitionScore + detachmentScore) / 2).coerceIn(28, 96),
                driverPlanet = "Ketu",
                fallbackHouse = 12,
                timingLords = listOf("Ketu", "Rahu", "Jupiter", lord9, lord12),
                fallbackTitleEn = "Pilgrimage, Retreats & Global Horizons",
                fallbackTitleHi = "तीर्थयात्रा, एकांत चिंतन एवं वैश्विक दृष्टि",
                fallbackBodyEn = "9th and 12th House alignments with {planet} near your {house} inspire transformative journeys and broader horizons during {year_range}.",
                fallbackBodyHi = "नवम-द्वादश भाव और {house} में {planet} का संयोग {year_range} में प्रेरणादायक यात्राएँ और व्यापक जीवन दृष्टि देता है।",
                fallbackBasisEn = "Ketu in House $ketuHouse • 9th & 12th Axis",
                fallbackBasisHi = "केतु भाव $ketuHouse • नवम-द्वादश भाव"
            )
        )

        val absorbedReadings = computeAbsorbedTopics(ctx, "dharma", dharmaTopicSpecs)
        val crossReferences = computeCrossReferences(ctx, "dharma", akCanon, akHouse)

        val heroSummary = if (isHi) {
            "आपके आत्मकारक ग्रह $akName (भाव $akHouse) और $ketuSign राशि (भाव $ketuHouse) में केतु मिलकर '$giftTitle' रूपी जीवन-उद्देश्य को प्रकाशित करते हैं। ज्ञान, सत्य और करुणामय सेवा का संगम आपके भीतर गहरी शांति व पूर्णता जगाता है।"
        } else {
            "Because your Jaimini Atmakaraka $akName ($akDeg) aligns with Ketu in $ketuSign (House $ketuHouse), your soul compass points toward '$giftTitle'. Truthful inquiry paired with quiet service turns ordinary days into lasting fulfillment."
        }
        val astroMathPill = if (isHi) {
            "आत्मकारक: $akName ($akDeg) • केतु: $ketuSign (भाव $ketuHouse)"
        } else {
            "Atmakaraka: $akName ($akDeg) • Ketu: $ketuSign (House $ketuHouse)"
        }

        val soulMissions = rankAndNormalizeCatalogMatches(
            ctx = ctx,
            sectionKey = "soul_missions_by_planet",
            divCode = "D9",
            categoryBoostHouses = setOf(9, 12, 5, 1, akHouse),
            topLimit = 3
        )

        val lifeThemesCard = if (isHi) {
            BroadScopeItem(
                title = "मुख्य जीवन सूत्र: $giftTitle",
                description = "आपके जीवन का मुख्य विषय बाहरी दिखावे से आगे बढ़कर प्रामाणिक ज्ञान, आंतरिक संतुलन और दूसरों के लिए उपयोगी निर्माण करना है। जब आप अपने कार्य को सेवा-भाव से जोड़ते हैं, तब सफलता के साथ गहरी मानसिक शांति स्वयं चली आती है। प्रत्येक चुनौती आपको अधिक दयालु और स्थिर बनाने के लिए आती है।",
                chartReference = "क्योंकि आपके आत्मकारक $akName (${ctx.ordinalHouse(akHouse)}), केतु (भाव $ketuHouse) और नवमेश ${ctx.planetName(lord9)} धर्म-अक्ष को सशक्त बनाते हैं।"
            )
        } else {
            BroadScopeItem(
                title = "Overarching Theme: $giftTitle",
                description = "Your overarching life arc moves from external approval toward authentic integrity, compassionate wisdom, and purposeful stewardship. Whenever you treat everyday skills as an offering of service, quiet inner peace accompanies outer achievement naturally. Difficult crossroads consistently sculpt deeper empathy and calm resilience.",
                chartReference = "Because your Atmakaraka $akName sits in your ${ctx.ordinalHouse(akHouse)} while Ketu occupies your ${ctx.ordinalHouse(ketuHouse)} and 9th Lord ${ctx.planetName(lord9)} anchors higher meaning."
            )
        }

        val tier3DharmaCards = if (isHi) listOf(
            MindsetRuleCard(
                title = "सत्य की निरंतर खोज (Seeking Truth)",
                body = "किसी भी परंपरा, पुस्तक या विचार को आँख मूँदकर स्वीकार करने के बजाय शांत स्वाध्याय और प्रत्यक्ष अनुभव से परखें। ईमानदार प्रश्न पूछना ही वास्तविक आध्यात्मिक परिपक्वता का लक्षण है।",
                chartReference = "${ctx.whyChartClause("नवमेश", "नवमेश", lord9)} और गुरु प्रामाणिक विवेक का पोषण करते हैं।"
            ),
            MindsetRuleCard(
                title = "आध्यात्मिक अहंकार से सहज मुक्ति (Avoiding Spiritual Ego)",
                body = "ध्यान, ज्ञान या सदाचार से भीतर विनम्रता और करुणा बढ़नी चाहिए—किसी की आलोचना या स्वयं को श्रेष्ठ मानने का भाव नहीं। सरल दैनिक दयालुता ही सबसे ऊँचा योग है।",
                chartReference = "${ctx.whyChartClause("केतु", "केतु", "Ketu")} अहंकार-मुक्त सहजता और समर्पण सिखाते हैं।"
            )
        ) else listOf(
            MindsetRuleCard(
                title = "Seeking Truth",
                body = "Test insights gently through quiet reflection, verified classics, and lived kindness rather than blind dogma. Honest curiosity grounded in everyday ethics reveals timeless clarity.",
                chartReference = "${ctx.whyChartClause("9th Lord", "नवमेश", lord9)}, illuminating higher inquiry alongside Atmakaraka $akName."
            ),
            MindsetRuleCard(
                title = "Avoiding Spiritual Ego",
                body = "True practice softens judgment and deepens ordinary warmth toward family, colleagues, and strangers alike. Whenever knowledge feels isolating, return immediately to humble hands-on service.",
                chartReference = "${ctx.whyChartClause("Ketu", "केतु", "Ketu")} in your ${ctx.ordinalHouse(ketuHouse)}, teaching quiet humility over display."
            )
        )

        val topMissionTitle = soulMissions.firstOrNull()?.title ?: giftTitle
        val dharmaFaqs = if (isHi) listOf(
            CreativeFaqItem(
                question = "मेरे जीवन का मुख्य उद्देश्य और धर्म क्या है?",
                paragraphs = listOf(
                    "क्योंकि आपके आत्मकारक ग्रह $akName ${ctx.ordinalHouse(akHouse)} में हैं और केतु भाव $ketuHouse में विराजमान हैं, आपका आत्मिक उद्देश्य '$topMissionTitle' तथा '$giftTitle' के मार्ग पर चलते हुए विश्व में स्पष्टता व सद्भाव बढ़ाना है।",
                    "जब आप अपने स्वाभाविक कौशल को विनम्र सेवा और सत्यनिष्ठा से जोड़ते हैं, तब आपका प्रत्येक कर्म ही धर्म बन जाता है।"
                )
            ),
            CreativeFaqItem(
                question = "इस जीवन में मुझे कौन से प्रमुख कर्म-पाठ सीखने हैं?",
                paragraphs = listOf(
                    "क्योंकि शनि और आत्मकारक $akName आपके नवम-द्वादश अक्ष के साथ संवाद करते हैं, आपका प्रमुख कर्म-पाठ धैर्य, परिणाम की अनावश्यक चिंता छोड़ना और करुणामय सीमाएँ बनाए रखना है।",
                    "नियमित एकांत चिंतन और अतीत के प्रति क्षमा-भाव अपनाने से जीवन में अद्भुत हल्कापन और सहज उन्नति प्राप्त होती है।"
                )
            )
        ) else listOf(
            CreativeFaqItem(
                question = "What is my life purpose?",
                paragraphs = listOf(
                    "Because your Atmakaraka $akName occupies your ${ctx.ordinalHouse(akHouse)} while Ketu resides in $ketuSign (House $ketuHouse), your soul purpose resonates with '$topMissionTitle' and '$giftTitle'—bringing honest clarity and uplifting stewardship wherever you serve.",
                    "Fulfillment blossoms fastest when you apply practical gifts generously without waiting for grand titles or external applause."
                )
            ),
            CreativeFaqItem(
                question = "What karmic lessons am I here to learn?",
                paragraphs = listOf(
                    "Because ${ctx.planetName("Saturn")} and your Atmakaraka $akName shape your Karmic axis, your core lessons center on patient trust, releasing rigid control over outcomes, and practicing joyful service without burnout.",
                    "Honoring quiet daily stillness and forgiving past missteps frees tremendous creative energy for your present chapter."
                )
            )
        )

        val dharmaHeadline = universalSignatureHeadline(ReadingCategory.DHARMA, akCanon, isHi)
        val dharmaGlyph = planetGlyph(akCanon)
        val dharmaStory = buildStoryParagraphs(
            ctx = ctx,
            categoryKey = "dharma",
            strongestPlanetCanon = akCanon,
            keyLordCanon = lord9,
            keyHouseNum = 9,
            divCode = "D9",
            overallScore = overallScore
        )
        val enrichedDharmaHero = if (isHi) {
            "आपके आत्मकारक ग्रह $akName (${ctx.ordinalHouse(akHouse)}) और $ketuSign राशि में केतु मिलकर जीवन-उद्देश्य को '$dharmaHeadline' का दिव्य स्वरूप देते हैं। स्वाध्याय, सत्यनिष्ठा और करुणामय सेवा साधारण दैनिक कर्मों को भी आध्यात्मिक शांति में बदल देते हैं। सक्रिय ${ctx.planetName(ctx.mahaLordCanonical)} महादशा आंतरिक स्पष्टता को और प्रगाढ़ बनाती है।"
        } else {
            "With Jaimini Atmakaraka $akName guiding your ${ctx.ordinalHouse(akHouse)} alongside Ketu in $ketuSign (House $ketuHouse), your higher calling reflects '$dharmaHeadline'. Thoughtful contemplation paired with generous everyday service turns quiet integrity into lasting peace. Under your active ${ctx.planetName(ctx.mahaLordCanonical)} period, spiritual clarity deepens naturally."
        }

        return CategoryDeepDive(
            category = ReadingCategory.DHARMA,
            title = if (isHi) "धर्म एवं जीवन उद्देश्य" else "Dharma & Life Purpose",
            overallScore = overallScore,
            signatureTitle = dharmaHeadline,
            archetype = archetype,
            narrative = narrative,
            diversePaths = diversePaths,
            employmentPaths = emptyList(),
            businessPaths = emptyList(),
            employmentVsBusinessGuidance = "",
            powerBars = powerBars,
            proofItems = proofItems,
            yogaNotes = relevantYogas,
            divisionalChartCode = ReadingCategory.DHARMA.divisionalChartCode,
            absorbedReadings = absorbedReadings,
            crossReferences = crossReferences,
            whatToDo = whatToDo.distinct().take(4),
            safetyAdvice = safetyAdvice,
            remedy = remedy,
            personalityParagraph = libInsights.personalityParagraph,
            strengthsParagraph = libInsights.strengthsParagraph,
            challengesParagraph = libInsights.challengesParagraph,
            superpower = libInsights.superpower,
            growthArea = libInsights.growthArea,
            heroSummary = enrichedDharmaHero,
            astroMathPill = astroMathPill,
            tier1Cards = soulMissions,
            tier2Card = lifeThemesCard,
            tier3SectionTitle = if (isHi) "आध्यात्मिक दृष्टिकोण" else "Spiritual Attitude",
            tier3Cards = tier3DharmaCards,
            creativeFaqs = dharmaFaqs,
            heroGlyph = dharmaGlyph,
            storyParagraphs = dharmaStory
        )
    }

    private fun planetRemedy(planetCanon: String, category: ReadingCategory, isHi: Boolean): CategoryRemedy {
        return when (planetCanon) {
            "Sun" -> if (isHi) CategoryRemedy(
                mantra = "प्रातः सूर्य को अर्घ्य देते हुए 'ॐ घृणि सूर्याय नमः' का 11 बार जप करें।",
                charity = "रविवार को गेहूँ, गुड़ या ताँबे का पात्र किसी जरूरतमंद को दान करें।",
                ritual = "प्रातः सूर्योदय के समय 10 मिनट अनुशासन व स्पष्ट संकल्प के साथ दिन आरंभ करें।"
            ) else CategoryRemedy(
                mantra = "Chant 'Om Ghrini Suryaya Namah' 11 times facing the morning Sun.",
                charity = "Donate wheat, jaggery, or wholesome grains on Sundays.",
                ritual = "Begin mornings with 10 minutes of sunlight and calm posture before checking screens."
            )

            "Moon" -> if (isHi) CategoryRemedy(
                mantra = "सोमवार को शांत मन से 'ॐ सोमाय नमः' या 'ॐ नमः शिवाय' का जप करें।",
                charity = "सोमवार को चावल, दूध या श्वेत वस्त्र का दान करें।",
                ritual = "जल का पर्याप्त सेवन करें और संध्या समय 10 मिनट मौन ध्यान करें।"
            ) else CategoryRemedy(
                mantra = "Chant 'Om Somaya Namah' or 'Om Namah Shivaya' calmly on Mondays.",
                charity = "Offer rice, milk, or white clothing to elders or community kitchens on Mondays.",
                ritual = "Practice 10 minutes of silent lunar evening gratitude and mindful breathing."
            )

            "Mars" -> if (isHi) CategoryRemedy(
                mantra = "मंगलवार को 'ॐ अं अंगारकाय नमः' या हनुमान चालीसा का पाठ करें।",
                charity = "मंगलवार को लाल मसूर दाल या गुड़ का दान करें।",
                ritual = "नियमित शारीरिक व्यायाम से ऊर्जा को सकारात्मक दिशा दें और धैर्यपूर्ण संवाद रखें।"
            ) else CategoryRemedy(
                mantra = "Chant 'Om Ang Angarakaya Namah' or Hanuman Chalisa on Tuesdays.",
                charity = "Donate red lentils or jaggery on Tuesdays with goodwill.",
                ritual = "Channel extra drive into structured morning exercise and patient speech."
            )

            "Mercury" -> if (isHi) CategoryRemedy(
                mantra = "बुधवार को 'ॐ बुं बुधाय नमः' का 21 बार स्पष्ट उच्चारण से जप करें।",
                charity = "बुधवार को हरी मूंग दाल, पुस्तकें या विद्यार्थियों को लेखन सामग्री दान करें।",
                ritual = "अपने कार्य-डेस्क को स्वच्छ रखें और प्रतिदिन मुख्य विचारों का लिखित सार बनाएँ।"
            ) else CategoryRemedy(
                mantra = "Chant 'Om Bum Budhaya Namah' 21 times on Wednesday mornings.",
                charity = "Donate green moong, notebooks, or learning supplies to students on Wednesdays.",
                ritual = "Keep your workspace uncluttered and write clear daily action summaries."
            )

            "Jupiter" -> if (isHi) CategoryRemedy(
                mantra = "गुरुवार को 'ॐ बृं बृहस्पतये नमः' का 11 बार जप करें।",
                charity = "गुरुवार को चने की दाल, हल्दी, केले या धार्मिक/शैक्षिक पुस्तकें भेंट करें।",
                ritual = "गुरुजनों व वरिष्ठों का सम्मान करें और प्रतिदिन कुछ सार्थक ज्ञान पढ़ें।"
            ) else CategoryRemedy(
                mantra = "Chant 'Om Brim Brihaspataye Namah' 11 times on Thursday mornings.",
                charity = "Offer yellow lentils, turmeric, bananas, or educational books on Thursdays.",
                ritual = "Express gratitude to teachers and dedicate 15 minutes daily to wisdom reading."
            )

            "Venus" -> if (isHi) CategoryRemedy(
                mantra = "शुक्रवार को 'ॐ शुं शुक्राय नमः' का सौम्य भाव से जप करें।",
                charity = "शुक्रवार को मिश्री, चावल या सुगंधित श्वेत पुष्प अर्पित व दान करें।",
                ritual = "घर-कार्यस्थल में स्वच्छता, सौंदर्य और स्नेही वाणी बनाए रखें।"
            ) else CategoryRemedy(
                mantra = "Chant 'Om Shum Shukraya Namah' gently on Friday mornings.",
                charity = "Donate rice, rock sugar, or white flowers cleanly on Fridays.",
                ritual = "Keep your surroundings harmonious, tidy, and speak with warmth."
            )

            else -> if (isHi) CategoryRemedy(
                mantra = "शनिवार को 'ॐ शं शनैश्चराय नमः' का 11 बार शांत जप करें।",
                charity = "शनिवार को काले तिल, सरसों का तेल या श्रमिकों को भोजन भेंट करें।",
                ritual = "शनिवार संध्या को तिल के तेल का दीपक जलाएँ और समयबद्ध अनुशासन निभाएँ।"
            ) else CategoryRemedy(
                mantra = "Chant 'Om Sham Shanicharaya Namah' 11 times on Saturdays.",
                charity = "Donate black sesame, mustard oil, or meals to hardworking laborers on Saturdays.",
                ritual = "Light a sesame oil lamp on Saturday evenings and honor steady commitments."
            )
        }
    }
}
