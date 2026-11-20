package com.aryaleelastudios.astroveda.data.repository

import android.content.Context
import com.aryaleelastudios.astroveda.data.i18n.LanguageRegistry
import com.aryaleelastudios.astroveda.data.local.PersonEntity
import com.aryaleelastudios.astroveda.data.model.GrowthAreaInsight
import com.aryaleelastudios.astroveda.data.model.LuckyElements
import com.aryaleelastudios.astroveda.data.model.PredictionPeriod
import com.aryaleelastudios.astroveda.data.model.PredictionSelection
import com.aryaleelastudios.astroveda.data.model.Remedies
import com.aryaleelastudios.astroveda.data.model.SuperpowerInsight
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.intOrNull
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import java.time.LocalDate
import java.time.Period
import java.time.format.DateTimeFormatter
import java.util.concurrent.ConcurrentHashMap
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ContentRepository @Inject constructor(
    private val context: Context
) {

    private val jsonParser = Json { ignoreUnknownKeys = true }
    private val predictionsCache = ConcurrentHashMap<String, JsonObject>()
    private val blueprintCache = ConcurrentHashMap<String, JsonObject>()
    private val categoryInsightsCache = ConcurrentHashMap<String, JsonObject>()
    private val massiveCatalogCache = ConcurrentHashMap<String, JsonObject>()
    private val storyCache = ConcurrentHashMap<String, JsonObject>()
    private val year2027Cache = ConcurrentHashMap<String, JsonObject>()

    private fun loadAssetJson(path: String): JsonObject? {
        return try {
            val content = context.assets.open(path).bufferedReader().use { it.readText() }
            jsonParser.parseToJsonElement(content).jsonObject
        } catch (_: Exception) {
            null
        }
    }

    fun getPredictionsJson(langCode: String): JsonObject {
        val resolved = LanguageRegistry.resolveOrDefault(langCode).code
        return predictionsCache.getOrPut(resolved) {
            loadAssetJson("predictions/predictions_${resolved}.json")
                ?: getFallbackPredictionsJson()
        }
    }

    fun getFallbackPredictionsJson(): JsonObject {
        return predictionsCache.getOrPut("en") {
            loadAssetJson("predictions/predictions_en.json") ?: JsonObject(emptyMap())
        }
    }

    fun getBlueprintJson(langCode: String): JsonObject {
        val resolved = LanguageRegistry.resolveOrDefault(langCode).code
        return blueprintCache.getOrPut(resolved) {
            loadAssetJson("categories/blueprint_${resolved}.json")
                ?: getFallbackBlueprintJson()
        }
    }

    fun getFallbackBlueprintJson(): JsonObject {
        return blueprintCache.getOrPut("en") {
            loadAssetJson("categories/blueprint_en.json") ?: JsonObject(emptyMap())
        }
    }

    fun getCategoryInsightsJson(categoryKey: String, langCode: String): JsonObject {
        val resolved = LanguageRegistry.resolveOrDefault(langCode).code
        val cacheKey = "${categoryKey}_${resolved}"
        return categoryInsightsCache.getOrPut(cacheKey) {
            loadAssetJson("categories/${categoryKey}_${resolved}.json")
                ?: getFallbackCategoryInsightsJson(categoryKey)
        }
    }

    fun getFallbackCategoryInsightsJson(categoryKey: String): JsonObject {
        return categoryInsightsCache.getOrPut("${categoryKey}_en") {
            loadAssetJson("categories/${categoryKey}_en.json") ?: JsonObject(emptyMap())
        }
    }

    fun getMassiveCatalogJson(langCode: String): JsonObject {
        val resolved = LanguageRegistry.resolveOrDefault(langCode).code
        return massiveCatalogCache.getOrPut(resolved) {
            loadAssetJson("categories/massive_catalog_${resolved}.json")
                ?: getFallbackMassiveCatalogJson()
        }
    }

    fun getFallbackMassiveCatalogJson(): JsonObject {
        return massiveCatalogCache.getOrPut("en") {
            loadAssetJson("categories/massive_catalog_en.json") ?: JsonObject(emptyMap())
        }
    }

    fun getCategoryStoryJson(categoryKey: String, langCode: String): JsonObject {
        val resolved = LanguageRegistry.resolveOrDefault(langCode).code
        val cacheKey = "${categoryKey}_story_${resolved}"
        return storyCache.getOrPut(cacheKey) {
            loadAssetJson("categories/${categoryKey}_story_${resolved}.json")
                ?: getFallbackCategoryStoryJson(categoryKey)
        }
    }

    fun getFallbackCategoryStoryJson(categoryKey: String): JsonObject {
        return storyCache.getOrPut("${categoryKey}_story_en") {
            loadAssetJson("categories/${categoryKey}_story_en.json") ?: JsonObject(emptyMap())
        }
    }

    fun getYear2027Json(langCode: String): JsonObject {
        val resolved = LanguageRegistry.resolveOrDefault(langCode).code
        return year2027Cache.getOrPut(resolved) {
            loadAssetJson("year2027/year2027_${resolved}.json")
                ?: getFallbackYear2027Json()
        }
    }

    fun getFallbackYear2027Json(): JsonObject {
        return year2027Cache.getOrPut("en") {
            loadAssetJson("year2027/year2027_en.json") ?: JsonObject(emptyMap())
        }
    }

    fun getRawAssetString(path: String): String {
        return context.assets.open(path).bufferedReader().use { it.readText() }
    }

    /**
     * Key-by-key lookup from target JsonObject, falling back to English JsonObject if missing or blank.
     */
    fun resolveStringWithFallback(
        primary: JsonObject,
        fallback: JsonObject,
        vararg keys: String
    ): String {
        val fromPrimary = navigateString(primary, *keys)
        if (!fromPrimary.isNullOrBlank()) return fromPrimary
        return navigateString(fallback, *keys) ?: ""
    }

    private fun navigateString(root: JsonObject, vararg keys: String): String? {
        var current: JsonElement = root
        for (k in keys) {
            val obj = current as? JsonObject ?: return null
            current = obj[k] ?: return null
        }
        return (current as? kotlinx.serialization.json.JsonPrimitive)?.contentOrNull
    }

    private fun navigateInt(root: JsonObject, vararg keys: String): Int? {
        var current: JsonElement = root
        for (k in keys) {
            val obj = current as? JsonObject ?: return null
            current = obj[k] ?: return null
        }
        return (current as? kotlinx.serialization.json.JsonPrimitive)?.intOrNull
    }

    private fun navigateArray(root: JsonObject, vararg keys: String): JsonArray? {
        var current: JsonElement = root
        for (k in keys) {
            val obj = current as? JsonObject ?: return null
            current = obj[k] ?: return null
        }
        return current as? JsonArray
    }

    fun resolveStringListWithFallback(
        primary: JsonObject,
        fallback: JsonObject,
        key: String
    ): List<String> {
        val pArr = navigateArray(primary, key)
        val fArr = navigateArray(fallback, key) ?: return emptyList()
        return fArr.indices.map { idx ->
            val pVal = pArr?.getOrNull(idx)?.jsonPrimitive?.contentOrNull
            if (!pVal.isNullOrBlank()) pVal else fArr[idx].jsonPrimitive.contentOrNull.orEmpty()
        }
    }

    fun resolveAstroStringList(langCode: String, listKey: String): List<String> {
        val primary = getPredictionsJson(langCode)
        val fallback = getFallbackPredictionsJson()
        val pArr = navigateArray(primary, "astro_labels", listKey)
        val fArr = navigateArray(fallback, "astro_labels", listKey) ?: return emptyList()
        return fArr.indices.map { idx ->
            val pVal = pArr?.getOrNull(idx)?.jsonPrimitive?.contentOrNull
            if (!pVal.isNullOrBlank()) pVal else fArr[idx].jsonPrimitive.contentOrNull.orEmpty()
        }
    }

    fun resolveAstroLabel(langCode: String, vararg keys: String): String {
        val primary = getPredictionsJson(langCode)
        val fallback = getFallbackPredictionsJson()
        return resolveStringWithFallback(primary, fallback, "astro_labels", *keys)
    }

    fun getPlanetDisplayName(langCode: String, canonicalPlanet: String): String {
        val fromPred = resolveAstroLabel(langCode, "planets", canonicalPlanet)
        if (fromPred.isNotBlank()) return fromPred
        return canonicalPlanet
    }

    fun getOrdinalHouseName(langCode: String, houseNum: Int): String {
        val normalized = houseNum.coerceIn(1, 12)
        return if (langCode == "hi") {
            val hindiOrdinals = listOf(
                "प्रथम भाव", "द्वितीय भाव", "तृतीय भाव", "चतुर्थ भाव",
                "पंचम भाव", "षष्ठ भाव", "सप्तम भाव", "अष्टम भाव",
                "नवम भाव", "दशम भाव", "एकादश भाव", "द्वादश भाव"
            )
            hindiOrdinals[normalized - 1]
        } else {
            val suffix = when {
                normalized in 11..13 -> "th"
                normalized % 10 == 1 -> "st"
                normalized % 10 == 2 -> "nd"
                normalized % 10 == 3 -> "rd"
                else -> "th"
            }
            "$normalized$suffix House"
        }
    }

    data class SignReadings(
        val personality: String,
        val career: String,
        val relationships: String,
        val health: String,
        val luckyElements: LuckyElements,
        val remedies: Remedies
    )

    fun getSignReadings(langCode: String, moonSignCanonical: String): SignReadings {
        val primary = getPredictionsJson(langCode)
        val fallback = getFallbackPredictionsJson()
        val sign = if (fallback["personality"]?.jsonObject?.containsKey(moonSignCanonical) == true) {
            moonSignCanonical
        } else {
            "Aries"
        }

        val personality = resolveStringWithFallback(primary, fallback, "personality", sign)
        val career = resolveStringWithFallback(primary, fallback, "career", sign)
        val relationships = resolveStringWithFallback(primary, fallback, "relationships", sign)
        val health = resolveStringWithFallback(primary, fallback, "health", sign)

        val number = navigateInt(primary, "lucky_elements", sign, "number")
            ?: navigateInt(fallback, "lucky_elements", sign, "number")
            ?: 7
        val lucky = LuckyElements(
            number = number,
            color = resolveStringWithFallback(primary, fallback, "lucky_elements", sign, "color"),
            day = resolveStringWithFallback(primary, fallback, "lucky_elements", sign, "day"),
            direction = resolveStringWithFallback(primary, fallback, "lucky_elements", sign, "direction"),
            gemstone = resolveStringWithFallback(primary, fallback, "lucky_elements", sign, "gemstone")
        )

        val remedies = Remedies(
            mantra = resolveStringWithFallback(primary, fallback, "remedies", sign, "mantra"),
            charity = resolveStringWithFallback(primary, fallback, "remedies", sign, "charity"),
            fasting = resolveStringWithFallback(primary, fallback, "remedies", sign, "fasting"),
            behavioral = resolveStringWithFallback(primary, fallback, "remedies", sign, "behavioral")
        )

        return SignReadings(
            personality = personality,
            career = career,
            relationships = relationships,
            health = health,
            luckyElements = lucky,
            remedies = remedies
        )
    }

    enum class LifeStageBucket {
        YOUTH,        // <= 22
        EARLY_ADULT,  // 23..32
        ESTABLISHED,  // 33..45
        MID_LIFE,     // 46..58
        SENIOR        // 59+
    }

    fun computeAgeYears(birthDateStr: String, referenceDate: LocalDate = LocalDate.now()): Int {
        return try {
            val dob = LocalDate.parse(birthDateStr, DateTimeFormatter.ISO_LOCAL_DATE)
            Period.between(dob, referenceDate).years.coerceAtLeast(0)
        } catch (_: Exception) {
            28
        }
    }

    fun lifeStageForAge(ageYears: Int): LifeStageBucket = when {
        ageYears <= 22 -> LifeStageBucket.YOUTH
        ageYears <= 32 -> LifeStageBucket.EARLY_ADULT
        ageYears <= 45 -> LifeStageBucket.ESTABLISHED
        ageYears <= 58 -> LifeStageBucket.MID_LIFE
        else -> LifeStageBucket.SENIOR
    }

    /**
     * Soft-weights a prediction fragment's tags against age bucket, marital status, and occupation.
     * Off-bucket fragments are down-weighted relative to boosted fragments, never excluded (>= 1.0).
     */
    fun computeFragmentWeight(
        tags: List<String>,
        ageYears: Int,
        maritalStatus: String,
        occupation: String
    ): Double {
        var weight = 1.0
        val bucketTags = when (lifeStageForAge(ageYears)) {
            LifeStageBucket.YOUTH -> setOf("study", "career-start", "marriage-early", "growth")
            LifeStageBucket.EARLY_ADULT -> setOf("career", "marriage", "money-start", "property-start")
            LifeStageBucket.ESTABLISHED -> setOf("money", "property", "children", "family", "career-growth")
            LifeStageBucket.MID_LIFE -> setOf("money", "health", "family", "career-peak", "property")
            LifeStageBucket.SENIOR -> setOf("health", "family", "spirituality", "legacy", "money-security")
        }

        val bucketMatches = tags.count { it in bucketTags }
        if (bucketMatches > 0) {
            weight += 2.0 * bucketMatches
        }

        if (maritalStatus.equals("Married", ignoreCase = true)) {
            val marriedTags = setOf("family", "children", "property")
            if (tags.any { it in marriedTags }) {
                weight += 1.5
            }
        }

        if (occupation.equals("Student", ignoreCase = true)) {
            val studentTags = setOf("study", "career-start")
            if (tags.any { it in studentTags }) {
                weight += 1.5
            }
        }

        return weight
    }

    fun selectPrediction(
        langCode: String,
        period: PredictionPeriod,
        person: PersonEntity?,
        dashaLordLocalized: String,
        dashaLordCanonical: String,
        moonSignLocalized: String,
        moonSignCanonical: String,
        referenceDate: LocalDate = LocalDate.now()
    ): PredictionSelection {
        val primary = getPredictionsJson(langCode)
        val fallback = getFallbackPredictionsJson()
        val arrayKey = when (period) {
            PredictionPeriod.DAILY -> "daily_insights"
            PredictionPeriod.MONTHLY -> "monthly_insights"
            PredictionPeriod.YEARLY -> "yearly_insights"
            PredictionPeriod.LIFE -> "life_insights"
        }

        val fallbackArr = navigateArray(fallback, arrayKey) ?: JsonArray(emptyList())
        val primaryArr = navigateArray(primary, arrayKey)

        // Build unified list with key-by-key fallback by matching fragment id or index
        val primaryById = primaryArr?.mapNotNull { el ->
            val obj = el as? JsonObject ?: return@mapNotNull null
            val id = obj["id"]?.jsonPrimitive?.contentOrNull ?: return@mapNotNull null
            id to obj
        }?.toMap().orEmpty()

        val fragments = fallbackArr.mapIndexedNotNull { idx, el ->
            val fbObj = el as? JsonObject ?: return@mapIndexedNotNull null
            val id = fbObj["id"]?.jsonPrimitive?.contentOrNull ?: "item_$idx"
            val pObj = primaryById[id] ?: (primaryArr?.getOrNull(idx) as? JsonObject)
            val theme = pObj?.get("theme")?.jsonPrimitive?.contentOrNull?.takeIf { it.isNotBlank() }
                ?: fbObj["theme"]?.jsonPrimitive?.contentOrNull.orEmpty()
            val text = pObj?.get("text")?.jsonPrimitive?.contentOrNull?.takeIf { it.isNotBlank() }
                ?: fbObj["text"]?.jsonPrimitive?.contentOrNull.orEmpty()
            val tags = fbObj["tags"]?.jsonArray?.mapNotNull { it.jsonPrimitive.contentOrNull }.orEmpty()
            PredictionSelection(id = id, theme = theme, text = text, tags = tags)
        }

        if (fragments.isEmpty()) {
            return PredictionSelection("default", "", "", emptyList())
        }

        val age = if (person != null) computeAgeYears(person.birthDate, referenceDate) else 28
        val marital = person?.maritalStatus ?: "Single"
        val occupation = person?.occupation ?: "Other"

        val weights = fragments.map { frag ->
            computeFragmentWeight(frag.tags, age, marital, occupation)
        }
        val totalWeight = weights.sum()

        val periodDateKey = when (period) {
            PredictionPeriod.DAILY -> referenceDate.format(DateTimeFormatter.BASIC_ISO_DATE)
            PredictionPeriod.MONTHLY -> "${referenceDate.year}-${referenceDate.monthValue}"
            PredictionPeriod.YEARLY -> "${referenceDate.year}"
            PredictionPeriod.LIFE -> "life"
        }

        val personKey = person?.let { "${it.name}|${it.birthDate}|${it.birthTime}" } ?: "guest"
        val seedStr = "$personKey|${period.name}|$periodDateKey|$dashaLordCanonical|$moonSignCanonical"
        val unitRand = deterministicUnitDouble(seedStr)
        val target = unitRand * totalWeight

        var cumulative = 0.0
        var chosen = fragments.first()
        for (i in fragments.indices) {
            cumulative += weights[i]
            if (target <= cumulative) {
                chosen = fragments[i]
                break
            }
        }

        val renderedText = chosen.text
            .replace("{dasha}", dashaLordLocalized)
            .replace("{moon_sign}", moonSignLocalized)

        return chosen.copy(text = renderedText)
    }

    private fun deterministicUnitDouble(seed: String): Double {
        // 64-bit FNV-1a hash mixed cleanly for uniform [0.0, 1.0)
        var hash = -3750763034362895579L // 0xcbf29ce484222325
        for (ch in seed) {
            hash = hash xor ch.code.toLong()
            hash *= 1099511628211L // 0x100000001b3
        }
        // Murmur-style finalizer
        hash = hash xor (hash ushr 33)
        hash *= -49064778989728563L
        hash = hash xor (hash ushr 33)
        val positive = hash and Long.MAX_VALUE
        return (positive % 1_000_000L).toDouble() / 1_000_000.0
    }

    data class ReasonedPathTemplate(
        val title: String,
        val sector: String,
        val reason: String
    )

    data class AbsorbedTopicTemplate(
        val title: String,
        val strong: String,
        val balanced: String,
        val focus: String,
        val basis: String
    )

    fun resolveCategoryReasonedPaths(
        categoryKey: String,
        langCode: String,
        jsonSectionKey: String,
        planetCanonical: String
    ): List<ReasonedPathTemplate> {
        val primary = getCategoryInsightsJson(categoryKey, langCode)
        val fallback = getFallbackCategoryInsightsJson(categoryKey)
        val pArr = navigateArray(primary, jsonSectionKey, planetCanonical)
        val fArr = navigateArray(fallback, jsonSectionKey, planetCanonical) ?: return emptyList()
        return fArr.indices.mapNotNull { idx ->
            val fbObj = fArr[idx] as? JsonObject ?: return@mapNotNull null
            val pObj = pArr?.getOrNull(idx) as? JsonObject
            val title = pObj?.get("title")?.jsonPrimitive?.contentOrNull?.takeIf { it.isNotBlank() }
                ?: fbObj["title"]?.jsonPrimitive?.contentOrNull.orEmpty()
            val sector = pObj?.get("sector")?.jsonPrimitive?.contentOrNull?.takeIf { it.isNotBlank() }
                ?: fbObj["sector"]?.jsonPrimitive?.contentOrNull.orEmpty()
            val reason = pObj?.get("reason")?.jsonPrimitive?.contentOrNull?.takeIf { it.isNotBlank() }
                ?: fbObj["reason"]?.jsonPrimitive?.contentOrNull.orEmpty()
            if (title.isBlank()) null else ReasonedPathTemplate(title, sector, reason)
        }
    }

    fun resolveCategoryHouseBonusPath(
        categoryKey: String,
        langCode: String,
        placementKey: String,
        trackKey: String
    ): ReasonedPathTemplate? {
        val primary = getCategoryInsightsJson(categoryKey, langCode)
        val fallback = getFallbackCategoryInsightsJson(categoryKey)
        val title = resolveStringWithFallback(primary, fallback, "house_bonus_paths", placementKey, trackKey, "title")
        if (title.isBlank()) return null
        val sector = resolveStringWithFallback(primary, fallback, "house_bonus_paths", placementKey, trackKey, "sector")
        val reason = resolveStringWithFallback(primary, fallback, "house_bonus_paths", placementKey, trackKey, "reason")
        return ReasonedPathTemplate(title = title, sector = sector, reason = reason)
    }

    fun resolveCategoryStringListByPlanet(
        categoryKey: String,
        langCode: String,
        sectionKey: String,
        planetCanonical: String
    ): List<String> {
        val primary = getCategoryInsightsJson(categoryKey, langCode)
        val fallback = getFallbackCategoryInsightsJson(categoryKey)
        val pArr = navigateArray(primary, sectionKey, planetCanonical)
        val fArr = navigateArray(fallback, sectionKey, planetCanonical) ?: return emptyList()
        return fArr.indices.mapNotNull { idx ->
            val pVal = pArr?.getOrNull(idx)?.jsonPrimitive?.contentOrNull
            val resolved = if (!pVal.isNullOrBlank()) pVal else fArr[idx].jsonPrimitive.contentOrNull.orEmpty()
            resolved.takeIf { it.isNotBlank() }
        }
    }

    fun resolveAbsorbedTopic(
        categoryKey: String,
        langCode: String,
        topicId: String
    ): AbsorbedTopicTemplate? {
        val primary = getCategoryInsightsJson(categoryKey, langCode)
        val fallback = getFallbackCategoryInsightsJson(categoryKey)
        val title = resolveStringWithFallback(primary, fallback, "absorbed_topics", topicId, "title")
        if (title.isBlank()) return null
        return AbsorbedTopicTemplate(
            title = title,
            strong = resolveStringWithFallback(primary, fallback, "absorbed_topics", topicId, "strong"),
            balanced = resolveStringWithFallback(primary, fallback, "absorbed_topics", topicId, "balanced"),
            focus = resolveStringWithFallback(primary, fallback, "absorbed_topics", topicId, "focus"),
            basis = resolveStringWithFallback(primary, fallback, "absorbed_topics", topicId, "basis")
        )
    }

    fun resolveCrossReference(
        categoryKey: String,
        langCode: String,
        targetCategoryName: String
    ): Pair<String, String>? {
        val primary = getCategoryInsightsJson(categoryKey, langCode)
        val fallback = getFallbackCategoryInsightsJson(categoryKey)
        val title = resolveStringWithFallback(primary, fallback, "cross_references", targetCategoryName, "title")
        val insight = resolveStringWithFallback(primary, fallback, "cross_references", targetCategoryName, "insight")
        if (title.isBlank() || insight.isBlank()) return null
        return title to insight
    }

    // Cosmic Blueprint & Massive Category Library Helpers
    fun resolveBlueprintText(langCode: String, sectionKey: String, itemKey: String): String {
        val primary = getBlueprintJson(langCode)
        val fallback = getFallbackBlueprintJson()
        return resolveStringWithFallback(primary, fallback, sectionKey, itemKey)
    }

    fun resolveCategoryPersonality(categoryKey: String, langCode: String, signIndex: Int): String {
        val primary = getCategoryInsightsJson(categoryKey, langCode)
        val fallback = getFallbackCategoryInsightsJson(categoryKey)
        return resolveStringWithFallback(primary, fallback, "personality_by_sign", signIndex.toString())
    }

    fun resolveCategoryPlacement(
        categoryKey: String,
        langCode: String,
        planetCanonical: String,
        houseNumber: Int
    ): String {
        val primary = getCategoryInsightsJson(categoryKey, langCode)
        val fallback = getFallbackCategoryInsightsJson(categoryKey)
        return resolveStringWithFallback(primary, fallback, "planet_in_house", "${planetCanonical}_${houseNumber}")
    }

    fun resolveCategoryHiddenChallenge(
        categoryKey: String,
        langCode: String,
        planetCanonical: String
    ): String {
        val primary = getCategoryInsightsJson(categoryKey, langCode)
        val fallback = getFallbackCategoryInsightsJson(categoryKey)
        return resolveStringWithFallback(primary, fallback, "hidden_challenges", planetCanonical)
    }

    fun resolveCategorySuperpower(
        categoryKey: String,
        langCode: String,
        planetCanonical: String,
        placementTag: String
    ): SuperpowerInsight {
        val primary = getCategoryInsightsJson(categoryKey, langCode)
        val fallback = getFallbackCategoryInsightsJson(categoryKey)
        val title = resolveStringWithFallback(primary, fallback, "superpowers", planetCanonical, "title")
        val body = resolveStringWithFallback(primary, fallback, "superpowers", planetCanonical, "body")
        return SuperpowerInsight(
            title = title,
            placementTag = placementTag,
            description = body
        )
    }

    fun resolveCategoryGrowthArea(
        categoryKey: String,
        langCode: String,
        planetCanonical: String,
        placementTag: String
    ): GrowthAreaInsight {
        val primary = getCategoryInsightsJson(categoryKey, langCode)
        val fallback = getFallbackCategoryInsightsJson(categoryKey)
        val title = resolveStringWithFallback(primary, fallback, "growth_areas", planetCanonical, "title")
        val body = resolveStringWithFallback(primary, fallback, "growth_areas", planetCanonical, "body")
        val guidance = resolveStringWithFallback(primary, fallback, "growth_areas", planetCanonical, "guidance")
        return GrowthAreaInsight(
            title = title,
            placementTag = placementTag,
            description = body,
            guidance = guidance
        )
    }

    data class CatalogEntry(
        val id: String,
        val planet: String,
        val title: String,
        val sector: String,
        val preferredHouses: List<Int>,
        val description: String
    )

    fun resolveCatalogEntries(
        langCode: String,
        sectionKey: String
    ): List<CatalogEntry> {
        val primary = getMassiveCatalogJson(langCode)
        val fallback = getFallbackMassiveCatalogJson()
        val fSection = fallback[sectionKey] as? JsonObject ?: return emptyList()
        val pSection = primary[sectionKey] as? JsonObject

        val result = mutableListOf<CatalogEntry>()
        for ((planetName, fElement) in fSection) {
            val fArr = fElement as? JsonArray ?: continue
            val pArr = pSection?.get(planetName) as? JsonArray
            val primaryById = pArr?.mapNotNull { el ->
                val obj = el as? JsonObject ?: return@mapNotNull null
                val id = obj["id"]?.jsonPrimitive?.contentOrNull ?: return@mapNotNull null
                id to obj
            }?.toMap().orEmpty()

            fArr.forEachIndexed { idx, el ->
                val fObj = el as? JsonObject ?: return@forEachIndexed
                val id = fObj["id"]?.jsonPrimitive?.contentOrNull ?: "${planetName}_$idx"
                val pObj = primaryById[id] ?: (pArr?.getOrNull(idx) as? JsonObject)
                val title = pObj?.get("title")?.jsonPrimitive?.contentOrNull?.takeIf { it.isNotBlank() }
                    ?: fObj["title"]?.jsonPrimitive?.contentOrNull.orEmpty()
                val sector = pObj?.get("sector")?.jsonPrimitive?.contentOrNull?.takeIf { it.isNotBlank() }
                    ?: fObj["sector"]?.jsonPrimitive?.contentOrNull.orEmpty()
                val desc = pObj?.get("desc")?.jsonPrimitive?.contentOrNull?.takeIf { it.isNotBlank() }
                    ?: fObj["desc"]?.jsonPrimitive?.contentOrNull.orEmpty()
                val houses = (fObj["houses"] as? JsonArray)?.mapNotNull { h ->
                    h.jsonPrimitive.intOrNull
                }.orEmpty()
                if (title.isNotBlank()) {
                    result += CatalogEntry(
                        id = id,
                        planet = planetName,
                        title = title,
                        sector = sector,
                        preferredHouses = houses,
                        description = desc
                    )
                }
            }
        }
        return result
    }

    fun resolveCategoryStoryParagraph(
        categoryKey: String,
        langCode: String,
        sectionKey: String,
        itemKey: String
    ): String {
        val primary = getCategoryStoryJson(categoryKey, langCode)
        val fallback = getFallbackCategoryStoryJson(categoryKey)
        return resolveStringWithFallback(primary, fallback, sectionKey, itemKey)
    }

    fun resolveYear2027String(langCode: String, vararg keys: String): String {
        val primary = getYear2027Json(langCode)
        val fallback = getFallbackYear2027Json()
        return resolveStringWithFallback(primary, fallback, *keys)
    }

    fun resolveYear2027StringList(langCode: String, vararg keys: String): List<String> {
        val primary = getYear2027Json(langCode)
        val fallback = getFallbackYear2027Json()
        val pArr = navigateArray(primary, *keys)
        val fArr = navigateArray(fallback, *keys) ?: return emptyList()
        return fArr.indices.mapNotNull { idx ->
            val pVal = pArr?.getOrNull(idx)?.jsonPrimitive?.contentOrNull
            val resolved = if (!pVal.isNullOrBlank()) pVal else fArr[idx].jsonPrimitive.contentOrNull.orEmpty()
            resolved.takeIf { it.isNotBlank() }
        }
    }
}
