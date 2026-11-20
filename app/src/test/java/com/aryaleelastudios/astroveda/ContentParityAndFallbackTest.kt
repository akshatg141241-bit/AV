package com.aryaleelastudios.astroveda

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.aryaleelastudios.astroveda.astro.AstroEngine
import com.aryaleelastudios.astroveda.data.i18n.LanguageRegistry
import com.aryaleelastudios.astroveda.data.i18n.LocaleHelper
import com.aryaleelastudios.astroveda.data.local.PersonEntity
import com.aryaleelastudios.astroveda.data.model.AyanamsaType
import com.aryaleelastudios.astroveda.data.model.ReadingCategory
import com.aryaleelastudios.astroveda.data.repository.ContentRepository
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ContentParityAndFallbackTest {

    private lateinit var context: Context
    private lateinit var contentRepository: ContentRepository

    @Before
    fun setup() {
        context = ApplicationProvider.getApplicationContext()
        contentRepository = ContentRepository(context)
    }

    @Test
    fun languageRegistry_hasEnglishHindiEnabledAndGujaratiScaffolded() {
        val enabledCodes = LanguageRegistry.enabledLanguages().map { it.code }
        assertTrue(enabledCodes.containsAll(listOf("en", "hi")))

        val gu = LanguageRegistry.findByCode("gu")
        assertNotNull("Gujarati entry must exist in LanguageRegistry", gu)
        assertFalse("Gujarati must be disabled for v1", gu!!.enabled)
        assertTrue(gu.fontPath.endsWith(".ttf"))

        // Resolving disabled language falls back safely to English
        assertEquals("en", LanguageRegistry.resolveOrDefault("gu").code)
        assertEquals("en", LanguageRegistry.resolveOrDefault("unknown").code)
    }

    @Test
    fun predictionsJson_enAndHiHaveFullParityAndAtLeast20TaggedFragmentsPerPeriod() {
        val predEn = contentRepository.getPredictionsJson("en")
        val predHi = contentRepository.getPredictionsJson("hi")

        val readingSections = listOf("personality", "career", "relationships", "health")
        for (section in readingSections) {
            val enObj = predEn[section]?.jsonObject ?: error("Missing $section in en")
            val hiObj = predHi[section]?.jsonObject ?: error("Missing $section in hi")
            assertEquals("Sign keys mismatch in $section", AstroEngine.CANONICAL_RASHIS.toSet(), enObj.keys)
            assertEquals("Sign keys mismatch en vs hi in $section", enObj.keys, hiObj.keys)
            enObj.keys.forEach { sign ->
                assertTrue("Empty en text for $section.$sign", enObj[sign]!!.jsonPrimitive.content.isNotBlank())
                assertTrue("Empty hi text for $section.$sign", hiObj[sign]!!.jsonPrimitive.content.isNotBlank())
            }
        }

        // Lucky elements and Remedies parity
        listOf("lucky_elements", "remedies").forEach { section ->
            val enObj = predEn[section]?.jsonObject ?: error("Missing $section in en")
            val hiObj = predHi[section]?.jsonObject ?: error("Missing $section in hi")
            assertEquals(enObj.keys, hiObj.keys)
            enObj.keys.forEach { sign ->
                assertEquals(
                    "Nested keys mismatch in $section.$sign",
                    enObj[sign]!!.jsonObject.keys,
                    hiObj[sign]!!.jsonObject.keys
                )
            }
        }

        // 4 period insight arrays >= 20 tagged items each, matching IDs and non-blank text
        val periodKeys = listOf("daily_insights", "monthly_insights", "yearly_insights", "life_insights")
        for (pKey in periodKeys) {
            val enArr = predEn[pKey]?.jsonArray ?: error("Missing $pKey in en")
            val hiArr = predHi[pKey]?.jsonArray ?: error("Missing $pKey in hi")
            assertTrue("$pKey must have >= 20 fragments in en (found ${enArr.size})", enArr.size >= 20)
            assertEquals("$pKey length mismatch en vs hi", enArr.size, hiArr.size)

            val enIds = enArr.map { it.jsonObject["id"]!!.jsonPrimitive.content }
            val hiIds = hiArr.map { it.jsonObject["id"]!!.jsonPrimitive.content }
            assertEquals("Fragment IDs mismatch for $pKey", enIds, hiIds)

            enArr.forEach { item ->
                val obj = item.jsonObject
                assertTrue(obj["theme"]!!.jsonPrimitive.content.isNotBlank())
                assertTrue(obj["text"]!!.jsonPrimitive.content.isNotBlank())
                assertTrue("Fragment tags must be non-empty", obj["tags"]!!.jsonArray.isNotEmpty())
            }
            hiArr.forEach { item ->
                val obj = item.jsonObject
                assertTrue(obj["theme"]!!.jsonPrimitive.content.isNotBlank())
                assertTrue(obj["text"]!!.jsonPrimitive.content.isNotBlank())
            }
        }

        // astro_labels parity
        val astroEn = predEn["astro_labels"]!!.jsonObject
        val astroHi = predHi["astro_labels"]!!.jsonObject
        assertEquals("astro_labels keys mismatch", astroEn.keys, astroHi.keys)
        astroEn.keys.forEach { key ->
            val enVal = astroEn[key]!!
            val hiVal = astroHi[key]!!
            when (enVal) {
                is JsonObject -> assertEquals(
                    "astro_labels.$key keys mismatch",
                    enVal.keys,
                    hiVal.jsonObject.keys
                )
                is kotlinx.serialization.json.JsonArray -> assertEquals(
                    "astro_labels.$key array size mismatch",
                    enVal.size,
                    hiVal.jsonArray.size
                )
                else -> Unit
            }
        }
    }

    @Test
    fun categoryJsonNewSections_enAndHiHaveFullParityForAll15AbsorbedTopicsAndPaths() {
        val categories = mapOf(
            "career" to ReadingCategory.CAREER.absorbedTopicIds,
            "wealth" to ReadingCategory.WEALTH.absorbedTopicIds,
            "marriage" to ReadingCategory.MARRIAGE.absorbedTopicIds,
            "health" to ReadingCategory.HEALTH.absorbedTopicIds,
            "dharma" to ReadingCategory.DHARMA.absorbedTopicIds
        )
        val planets = listOf("Sun", "Moon", "Mars", "Mercury", "Jupiter", "Venus", "Saturn", "Rahu", "Ketu")
        val bannedRegex = Regex("""\b(weak|bad)\b|कमजोर|कमज़ोर|बुरा|खराब|ख़राब""", RegexOption.IGNORE_CASE)

        categories.forEach { (cat, topicIds) ->
            val en = contentRepository.getCategoryInsightsJson(cat, "en")
            val hi = contentRepository.getCategoryInsightsJson(cat, "hi")

            // cautious_zones_by_planet & safety_advice_by_planet
            listOf("cautious_zones_by_planet", "safety_advice_by_planet").forEach { sec ->
                val enObj = en[sec]?.jsonObject ?: error("Missing $sec in ${cat}_en")
                val hiObj = hi[sec]?.jsonObject ?: error("Missing $sec in ${cat}_hi")
                assertEquals("$sec planet keys mismatch in $cat", planets.toSet(), enObj.keys)
                assertEquals("$sec en vs hi mismatch in $cat", enObj.keys, hiObj.keys)
                planets.forEach { p ->
                    val enArr = enObj[p]!!.jsonArray
                    val hiArr = hiObj[p]!!.jsonArray
                    assertTrue("$cat.$sec.$p empty", enArr.isNotEmpty() && hiArr.isNotEmpty())
                    enArr.forEach { assertFalse("Banned word in $cat.$sec.$p", bannedRegex.containsMatchIn(it.jsonPrimitive.content)) }
                    hiArr.forEach { assertFalse("Banned word in $cat.$sec.$p hi", bannedRegex.containsMatchIn(it.jsonPrimitive.content)) }
                }
            }

            // diverse_paths_by_planet (plus employment/business/house_bonus on career)
            val pathSections = if (cat == "career") {
                listOf("diverse_paths_by_planet", "employment_paths_by_planet", "business_paths_by_planet")
            } else {
                listOf("diverse_paths_by_planet")
            }
            pathSections.forEach { sec ->
                val enObj = en[sec]?.jsonObject ?: error("Missing $sec in ${cat}_en")
                val hiObj = hi[sec]?.jsonObject ?: error("Missing $sec in ${cat}_hi")
                assertEquals("$sec planet keys mismatch in $cat", planets.toSet(), enObj.keys)
                assertEquals("$sec en vs hi mismatch in $cat", enObj.keys, hiObj.keys)
                planets.forEach { p ->
                    val enArr = enObj[p]!!.jsonArray
                    val hiArr = hiObj[p]!!.jsonArray
                    assertTrue("$cat.$sec.$p empty", enArr.isNotEmpty() && hiArr.isNotEmpty())
                    (enArr + hiArr).forEach { el ->
                        val obj = el.jsonObject
                        val t = obj["title"]!!.jsonPrimitive.content
                        val s = obj["sector"]!!.jsonPrimitive.content
                        val r = obj["reason"]!!.jsonPrimitive.content
                        assertTrue("$cat.$sec.$p missing fields", t.isNotBlank() && s.isNotBlank() && r.isNotBlank())
                        assertFalse("Banned word in $cat.$sec.$p: $t $r", bannedRegex.containsMatchIn("$t $s $r"))
                    }
                }
            }

            // absorbed_topics
            val absEn = en["absorbed_topics"]?.jsonObject ?: error("Missing absorbed_topics in ${cat}_en")
            val absHi = hi["absorbed_topics"]?.jsonObject ?: error("Missing absorbed_topics in ${cat}_hi")
            assertEquals("absorbed_topics keys mismatch in ${cat}_en", topicIds.toSet(), absEn.keys)
            assertEquals("absorbed_topics keys mismatch in ${cat}_hi", topicIds.toSet(), absHi.keys)
            topicIds.forEach { tId ->
                val objEn = absEn[tId]!!.jsonObject
                val objHi = absHi[tId]!!.jsonObject
                listOf("title", "strong", "balanced", "focus", "basis").forEach { field ->
                    val eVal = objEn[field]!!.jsonPrimitive.content
                    val hVal = objHi[field]!!.jsonPrimitive.content
                    assertTrue("Empty absorbed_topics.$tId.$field in $cat", eVal.isNotBlank() && hVal.isNotBlank())
                    assertFalse("Banned word in ${cat}_en absorbed.$tId.$field", bannedRegex.containsMatchIn(eVal))
                    assertFalse("Banned word in ${cat}_hi absorbed.$tId.$field", bannedRegex.containsMatchIn(hVal))
                }
            }

            // cross_references
            val crossEn = en["cross_references"]?.jsonObject ?: error("Missing cross_references in ${cat}_en")
            val crossHi = hi["cross_references"]?.jsonObject ?: error("Missing cross_references in ${cat}_hi")
            assertEquals("cross_references keys mismatch in $cat", 4, crossEn.size)
            assertEquals("cross_references en vs hi mismatch in $cat", crossEn.keys, crossHi.keys)
        }
    }

    @Test
    fun resolveStringWithFallback_usesEnglishWhenPrimaryKeyMissing() {
        val partialHi = Json.parseToJsonElement(
            """{"personality": {"Aries": ""}}"""
        ).jsonObject
        val fullEn = contentRepository.getFallbackPredictionsJson()

        val resolvedMissing = contentRepository.resolveStringWithFallback(
            primary = partialHi,
            fallback = fullEn,
            "career", "Aries"
        )
        assertTrue("Missing key should fall back to English", resolvedMissing.isNotBlank())

        val resolvedBlank = contentRepository.resolveStringWithFallback(
            primary = partialHi,
            fallback = fullEn,
            "personality", "Aries"
        )
        assertTrue("Blank key should fall back to English", resolvedBlank.isNotBlank())
    }

    @Test
    fun userFacingVerdictsAndAstroLabels_respectEmpoweringToneRule() {
        val bannedRegex = Regex("""\b(weak|bad)\b|कमजोर|कमज़ोर|बुरा|खराब|ख़राब""", RegexOption.IGNORE_CASE)

        listOf("en", "hi").forEach { lang ->
            val localizedContext = LocaleHelper.wrapWithLocale(context, lang)
            listOf(
                R.string.meter_excellent,
                R.string.meter_favorable,
                R.string.meter_growing,
                R.string.meter_needs_focus
            ).forEach { resId ->
                val label = localizedContext.getString(resId)
                assertFalse("Harsh tone word in $lang meter label: $label", bannedRegex.containsMatchIn(label))
            }

            val astroLabels = contentRepository.getPredictionsJson(lang)["astro_labels"]!!.jsonObject
            listOf("cosmic_highlights", "yogas_doshas").forEach { section ->
                val obj = astroLabels[section]!!.jsonObject
                obj.forEach { (k, v) ->
                    val text = v.jsonPrimitive.content
                    assertFalse(
                        "Harsh tone word in $lang astro_labels.$section.$k: $text",
                        bannedRegex.containsMatchIn(text)
                    )
                }
            }

            val samplePerson = PersonEntity(
                id = 1L,
                name = "Test Seeker",
                gender = "Female",
                birthDate = "1992-08-19",
                birthTime = "06:15",
                isTimeUnknown = false,
                birthPlace = "New Delhi, Delhi",
                latitude = 28.6139,
                longitude = 77.2090,
                timezoneId = "Asia/Kolkata",
                maritalStatus = "Single",
                occupation = "Service"
            )
            val kundali = AstroEngine.computeKundali(
                birthDateStr = samplePerson.birthDate,
                birthTimeStr = samplePerson.birthTime,
                isTimeUnknown = samplePerson.isTimeUnknown,
                lat = samplePerson.latitude,
                lon = samplePerson.longitude,
                tzId = samplePerson.timezoneId,
                ayanamsaType = AyanamsaType.LAHIRI,
                langCode = lang,
                contentRepo = contentRepository,
                person = samplePerson
            )

            kundali.cosmicHighlightBars.forEach { bar ->
                assertFalse("Harsh tone word in $lang cosmicHighlightBar ${bar.domainKey}: ${bar.summary}", bannedRegex.containsMatchIn(bar.summary))
            }
            assertFalse("Harsh tone word in $lang cosmicBlueprint: ${kundali.cosmicBlueprint}", bannedRegex.containsMatchIn(kundali.cosmicBlueprint))

            kundali.categoryDeepDives.forEach { (cat, dive) ->
                val texts = listOf(
                    dive.title,
                    dive.signatureTitle,
                    dive.archetype.giftTitle,
                    dive.archetype.derivationBasis,
                    dive.narrative,
                    dive.employmentVsBusinessGuidance,
                    dive.personalityParagraph,
                    dive.strengthsParagraph,
                    dive.challengesParagraph,
                    dive.superpower.title,
                    dive.superpower.placementTag,
                    dive.superpower.description,
                    dive.growthArea.title,
                    dive.growthArea.placementTag,
                    dive.growthArea.description,
                    dive.growthArea.guidance,
                    dive.remedy.mantra,
                    dive.remedy.charity,
                    dive.remedy.ritual,
                    dive.heroSummary,
                    dive.astroMathPill,
                    dive.tier2Badge,
                    dive.tier3SectionTitle,
                    dive.medicalDisclaimer,
                    dive.heroGlyph
                ) + dive.storyParagraphs +
                    dive.archetype.recommendedPaths +
                    dive.archetype.cautiousZones +
                    dive.diversePaths.flatMap { listOf(it.title, it.sector, it.astroReason) } +
                    dive.employmentPaths.flatMap { listOf(it.title, it.sector, it.astroReason) } +
                    dive.businessPaths.flatMap { listOf(it.title, it.sector, it.astroReason) } +
                    dive.absorbedReadings.flatMap { listOf(it.title, it.timingWindow, it.body, it.astroBasis) } +
                    dive.crossReferences.flatMap { listOf(it.targetLabel, it.title, it.insight) } +
                    dive.whatToDo +
                    dive.safetyAdvice +
                    dive.powerBars.flatMap { listOf(it.label, it.note) } +
                    dive.proofItems.flatMap { listOf(it.title, it.placementDetail, it.statusLabel) } +
                    dive.topJobMatches.flatMap { listOf(it.title, it.sector, it.description, it.chartReference) } +
                    dive.topBusinessMatches.flatMap { listOf(it.title, it.sector, it.description, it.chartReference) } +
                    dive.topWealthMatches.flatMap { listOf(it.title, it.sector, it.description, it.chartReference) } +
                    dive.broadScopes.flatMap { listOf(it.title, it.description, it.chartReference) } +
                    dive.tier1Cards.flatMap { listOf(it.title, it.sector, it.description, it.chartReference) } +
                    (dive.tier2Card?.let { listOf(it.title, it.description, it.chartReference) } ?: emptyList()) +
                    dive.tier3Cards.flatMap { listOf(it.title, it.body, it.chartReference) } +
                    dive.creativeFaqs.flatMap { listOf(it.question, it.badge) + it.paragraphs }

                texts.forEach { txt ->
                    assertFalse(
                        "Harsh tone word in $lang categoryDeepDive $cat: $txt",
                        bannedRegex.containsMatchIn(txt)
                    )
                }
            }

            kundali.dailyRemedies.forEach { rem ->
                listOf(rem.categoryLabel, rem.mantra, rem.why, rem.how).forEach { txt ->
                    assertFalse(
                        "Harsh tone word in $lang dailyRemedy ${rem.category}: $txt",
                        bannedRegex.containsMatchIn(txt)
                    )
                }
            }
        }
    }

    @Test
    fun massiveCatalogJson_enAndHiHaveFullParityAdequateCountsAndEmpoweringTone() {
        val bannedRegex = Regex("""\b(weak|bad)\b|कमजोर|कमज़ोर|बुरा|खराब|ख़राब""", RegexOption.IGNORE_CASE)
        val planets = listOf("Sun", "Moon", "Mars", "Mercury", "Jupiter", "Venus", "Saturn", "Rahu", "Ketu")
        val minCounts = mapOf(
            "jobs_by_planet" to 150,
            "business_models_by_planet" to 50,
            "wealth_sources_by_planet" to 30,
            "spouse_profiles_by_planet" to 30,
            "vulnerabilities_by_planet" to 30,
            "soul_missions_by_planet" to 20
        )

        val enCatalog = contentRepository.getMassiveCatalogJson("en")
        val hiCatalog = contentRepository.getMassiveCatalogJson("hi")
        assertEquals("massive_catalog top-level keys mismatch", minCounts.keys, enCatalog.keys)
        assertEquals("massive_catalog EN vs HI keys mismatch", enCatalog.keys, hiCatalog.keys)

        minCounts.forEach { (section, minRequired) ->
            val enSec = enCatalog[section]!!.jsonObject
            val hiSec = hiCatalog[section]!!.jsonObject
            assertEquals("Planet keys mismatch in $section EN", planets.toSet(), enSec.keys)
            assertEquals("Planet keys mismatch in $section HI", planets.toSet(), hiSec.keys)

            var totalCount = 0
            planets.forEach { planet ->
                val enArr = enSec[planet]!!.jsonArray
                val hiArr = hiSec[planet]!!.jsonArray
                assertEquals("Entry count mismatch EN vs HI for $section.$planet", enArr.size, hiArr.size)
                assertTrue("Expected entries for $section.$planet", enArr.isNotEmpty())
                totalCount += enArr.size

                for (i in enArr.indices) {
                    val enObj = enArr[i].jsonObject
                    val hiObj = hiArr[i].jsonObject
                    assertEquals(enObj["id"]!!.jsonPrimitive.content, hiObj["id"]!!.jsonPrimitive.content)
                    assertEquals(enObj["houses"]!!.jsonArray.size, hiObj["houses"]!!.jsonArray.size)
                    listOf("title", "sector", "desc").forEach { field ->
                        val eTxt = enObj[field]!!.jsonPrimitive.content
                        val hTxt = hiObj[field]!!.jsonPrimitive.content
                        assertTrue("Blank EN $section.$planet[$i].$field", eTxt.isNotBlank())
                        assertTrue("Blank HI $section.$planet[$i].$field", hTxt.isNotBlank())
                        assertFalse("Banned word in EN $section.$planet[$i].$field: $eTxt", bannedRegex.containsMatchIn(eTxt))
                        assertFalse("Banned word in HI $section.$planet[$i].$field: $hTxt", bannedRegex.containsMatchIn(hTxt))
                    }
                }
            }
            assertTrue("Expected >= $minRequired items in $section, found $totalCount", totalCount >= minRequired)
        }
    }

    @Test
    fun categoryAndBlueprintJson_enAndHiHaveCompleteKeysAndEmpoweringTone() {
        val bannedRegex = Regex("""\b(weak|bad)\b|कमजोर|कमज़ोर|बुरा|खराब|ख़राब""", RegexOption.IGNORE_CASE)
        val planets = listOf("Sun", "Moon", "Mars", "Mercury", "Jupiter", "Venus", "Saturn", "Rahu", "Ketu")
        val categories = listOf("career", "wealth", "marriage", "health", "dharma")

        // 1. Blueprint JSON parity
        val bpEn = contentRepository.getBlueprintJson("en")
        val bpHi = contentRepository.getBlueprintJson("hi")
        assertEquals(bpEn.keys, bpHi.keys)
        bpEn.keys.forEach { section ->
            val enObj = bpEn[section]!!.jsonObject
            val hiObj = bpHi[section]!!.jsonObject
            assertEquals("Blueprint section $section mismatch", enObj.keys, hiObj.keys)
            enObj.forEach { (k, v) ->
                val enStr = v.jsonPrimitive.content
                val hiStr = hiObj[k]!!.jsonPrimitive.content
                assertTrue("Empty EN blueprint $section.$k", enStr.isNotBlank())
                assertTrue("Empty HI blueprint $section.$k", hiStr.isNotBlank())
                assertFalse("Banned word in EN blueprint $section.$k: $enStr", bannedRegex.containsMatchIn(enStr))
                assertFalse("Banned word in HI blueprint $section.$k: $hiStr", bannedRegex.containsMatchIn(hiStr))
            }
        }

        // 2. Massive category JSON parity: 12 signs, 108 planet_in_house keys, 9 challenges/superpowers/growth_areas
        val expectedSigns = (0..11).map { it.toString() }.toSet()
        val expectedPlacements = planets.flatMap { p -> (1..12).map { h -> "${p}_$h" } }.toSet()

        categories.forEach { cat ->
            val en = contentRepository.getCategoryInsightsJson(cat, "en")
            val hi = contentRepository.getCategoryInsightsJson(cat, "hi")
            assertEquals("Top-level keys mismatch in $cat", en.keys, hi.keys)

            val pSignEn = en["personality_by_sign"]!!.jsonObject
            val pSignHi = hi["personality_by_sign"]!!.jsonObject
            assertEquals("Signs missing in ${cat}_en", expectedSigns, pSignEn.keys)
            assertEquals("Signs missing in ${cat}_hi", expectedSigns, pSignHi.keys)
            expectedSigns.forEach { s ->
                val enVal = pSignEn[s]!!.jsonPrimitive.content
                val hiVal = pSignHi[s]!!.jsonPrimitive.content
                assertTrue("Short/empty personality_by_sign $cat.$s", enVal.length > 40 && hiVal.length > 40)
                assertFalse("Banned word in ${cat}_en personality.$s", bannedRegex.containsMatchIn(enVal))
                assertFalse("Banned word in ${cat}_hi personality.$s", bannedRegex.containsMatchIn(hiVal))
            }

            val pihEn = en["planet_in_house"]!!.jsonObject
            val pihHi = hi["planet_in_house"]!!.jsonObject
            assertEquals("108 placements missing in ${cat}_en", expectedPlacements, pihEn.keys)
            assertEquals("108 placements missing in ${cat}_hi", expectedPlacements, pihHi.keys)
            expectedPlacements.forEach { k ->
                val enVal = pihEn[k]!!.jsonPrimitive.content
                val hiVal = pihHi[k]!!.jsonPrimitive.content
                assertTrue("Empty placement $cat.$k", enVal.isNotBlank() && hiVal.isNotBlank())
                assertFalse("Banned word in ${cat}_en $k: $enVal", bannedRegex.containsMatchIn(enVal))
                assertFalse("Banned word in ${cat}_hi $k: $hiVal", bannedRegex.containsMatchIn(hiVal))
            }

            val chEn = en["hidden_challenges"]!!.jsonObject
            val chHi = hi["hidden_challenges"]!!.jsonObject
            assertEquals(planets.toSet(), chEn.keys)
            assertEquals(planets.toSet(), chHi.keys)
            planets.forEach { p ->
                val enVal = chEn[p]!!.jsonPrimitive.content
                val hiVal = chHi[p]!!.jsonPrimitive.content
                assertTrue(enVal.isNotBlank() && hiVal.isNotBlank())
                assertFalse("Banned word in ${cat}_en hidden_challenges.$p", bannedRegex.containsMatchIn(enVal))
                assertFalse("Banned word in ${cat}_hi hidden_challenges.$p", bannedRegex.containsMatchIn(hiVal))
            }

            val spEn = en["superpowers"]!!.jsonObject
            val spHi = hi["superpowers"]!!.jsonObject
            assertEquals(planets.toSet(), spEn.keys)
            assertEquals(planets.toSet(), spHi.keys)
            planets.forEach { p ->
                val objEn = spEn[p]!!.jsonObject
                val objHi = spHi[p]!!.jsonObject
                listOf("title", "body").forEach { field ->
                    val enVal = objEn[field]!!.jsonPrimitive.content
                    val hiVal = objHi[field]!!.jsonPrimitive.content
                    assertTrue(enVal.isNotBlank() && hiVal.isNotBlank())
                    assertFalse("Banned word in ${cat}_en superpower.$p.$field", bannedRegex.containsMatchIn(enVal))
                    assertFalse("Banned word in ${cat}_hi superpower.$p.$field", bannedRegex.containsMatchIn(hiVal))
                }
            }

            val gaEn = en["growth_areas"]!!.jsonObject
            val gaHi = hi["growth_areas"]!!.jsonObject
            assertEquals(planets.toSet(), gaEn.keys)
            assertEquals(planets.toSet(), gaHi.keys)
            planets.forEach { p ->
                val objEn = gaEn[p]!!.jsonObject
                val objHi = gaHi[p]!!.jsonObject
                listOf("title", "body", "guidance").forEach { field ->
                    val enVal = objEn[field]!!.jsonPrimitive.content
                    val hiVal = objHi[field]!!.jsonPrimitive.content
                    assertTrue(enVal.isNotBlank() && hiVal.isNotBlank())
                    assertFalse("Banned word in ${cat}_en growth.$p.$field", bannedRegex.containsMatchIn(enVal))
                    assertFalse("Banned word in ${cat}_hi growth.$p.$field", bannedRegex.containsMatchIn(hiVal))
                }
            }
        }
    }

    @Test
    fun categoryStoryAndYear2027Json_enAndHiHaveFullParityAndEmpoweringTone() {
        val bannedRegex = Regex("""\b(weak|bad)\b|कमजोर|कमज़ोर|बुरा|खराब|ख़राब""", RegexOption.IGNORE_CASE)
        val planets = listOf("Sun", "Moon", "Mars", "Mercury", "Jupiter", "Venus", "Saturn", "Rahu", "Ketu")
        val categories = listOf("career", "wealth", "marriage", "health", "dharma")
        val houses = (1..12).map { it.toString() }.toSet()
        val buckets4 = setOf("excellent", "favorable", "growing", "extra_effort")

        // 1. All 10 Category Story JSONs
        categories.forEach { cat ->
            val en = contentRepository.getCategoryStoryJson(cat, "en")
            val hi = contentRepository.getCategoryStoryJson(cat, "hi")
            assertEquals("Story top-level keys mismatch in $cat", setOf("planet_p1", "house_p2", "dasha_p3", "synthesis_p4"), en.keys)
            assertEquals("Story EN vs HI top-level keys mismatch in $cat", en.keys, hi.keys)

            val mapChecks = listOf(
                "planet_p1" to planets.toSet(),
                "house_p2" to houses,
                "dasha_p3" to planets.toSet(),
                "synthesis_p4" to buckets4
            )
            mapChecks.forEach { (sec, expectedKeys) ->
                val enObj = en[sec]!!.jsonObject
                val hiObj = hi[sec]!!.jsonObject
                assertEquals("Story $cat.$sec keys mismatch", expectedKeys, enObj.keys)
                assertEquals("Story $cat.$sec EN vs HI mismatch", enObj.keys, hiObj.keys)
                expectedKeys.forEach { k ->
                    val enVal = enObj[k]!!.jsonPrimitive.content
                    val hiVal = hiObj[k]!!.jsonPrimitive.content
                    assertTrue("Empty EN story $cat.$sec.$k", enVal.isNotBlank())
                    assertTrue("Empty HI story $cat.$sec.$k", hiVal.isNotBlank())
                    assertFalse("Banned word in EN story $cat.$sec.$k: $enVal", bannedRegex.containsMatchIn(enVal))
                    assertFalse("Banned word in HI story $cat.$sec.$k: $hiVal", bannedRegex.containsMatchIn(hiVal))
                }
            }
        }

        // 2. year2027_en.json & year2027_hi.json
        val yEn = contentRepository.getYear2027Json("en")
        val yHi = contentRepository.getYear2027Json("hi")
        assertEquals("year2027 top-level keys mismatch", yEn.keys, yHi.keys)

        val enMonths = yEn["months"]!!.jsonArray
        val hiMonths = yHi["months"]!!.jsonArray
        assertEquals(12, enMonths.size)
        assertEquals(12, hiMonths.size)

        listOf("disclaimer").forEach { k ->
            val e = yEn[k]!!.jsonPrimitive.content
            val h = yHi[k]!!.jsonPrimitive.content
            assertTrue(e.isNotBlank() && h.isNotBlank())
            assertFalse("Banned word in year2027 EN $k", bannedRegex.containsMatchIn(e))
            assertFalse("Banned word in year2027 HI $k", bannedRegex.containsMatchIn(h))
        }

        val heroEn = yEn["hero_by_planet"]!!.jsonObject
        val heroHi = yHi["hero_by_planet"]!!.jsonObject
        assertEquals(planets.toSet(), heroEn.keys)
        assertEquals(planets.toSet(), heroHi.keys)
        planets.forEach { p ->
            val pEn = heroEn[p]!!.jsonObject
            val pHi = heroHi[p]!!.jsonObject
            assertEquals(setOf("headline", "glyph", "summary"), pEn.keys)
            assertEquals(pEn.keys, pHi.keys)
            pEn.keys.forEach { f ->
                val eVal = pEn[f]!!.jsonPrimitive.content
                val hVal = pHi[f]!!.jsonPrimitive.content
                assertTrue(eVal.isNotBlank() && hVal.isNotBlank())
                assertFalse("Banned word in year2027 EN hero.$p.$f", bannedRegex.containsMatchIn(eVal))
                assertFalse("Banned word in year2027 HI hero.$p.$f", bannedRegex.containsMatchIn(hVal))
            }
        }

        val glanceDomains = setOf("CAREER", "WEALTH", "HEALTH", "MARRIAGE", "GROWTH")
        val glanceEn = yEn["at_a_glance_notes"]!!.jsonObject
        val glanceHi = yHi["at_a_glance_notes"]!!.jsonObject
        assertEquals(glanceDomains, glanceEn.keys)
        assertEquals(glanceDomains, glanceHi.keys)
        glanceDomains.forEach { d ->
            val dEn = glanceEn[d]!!.jsonObject
            val dHi = glanceHi[d]!!.jsonObject
            assertEquals(buckets4, dEn.keys)
            assertEquals(buckets4, dHi.keys)
            buckets4.forEach { b ->
                val eVal = dEn[b]!!.jsonPrimitive.content
                val hVal = dHi[b]!!.jsonPrimitive.content
                assertTrue(eVal.isNotBlank() && hVal.isNotBlank())
                assertFalse("Banned word in year2027 EN at_a_glance.$d.$b", bannedRegex.containsMatchIn(eVal))
                assertFalse("Banned word in year2027 HI at_a_glance.$d.$b", bannedRegex.containsMatchIn(hVal))
            }
        }

        val catDomains = setOf("CAREER", "WEALTH", "HEALTH", "MARRIAGE", "DHARMA")
        listOf("golden_reasons", "effort_reasons").forEach { sec ->
            val sEn = yEn[sec]!!.jsonObject
            val sHi = yHi[sec]!!.jsonObject
            assertEquals(catDomains, sEn.keys)
            assertEquals(catDomains, sHi.keys)
            catDomains.forEach { d ->
                val eVal = sEn[d]!!.jsonPrimitive.content
                val hVal = sHi[d]!!.jsonPrimitive.content
                assertTrue(eVal.isNotBlank() && hVal.isNotBlank())
                assertFalse("Banned word in year2027 EN $sec.$d", bannedRegex.containsMatchIn(eVal))
                assertFalse("Banned word in year2027 HI $sec.$d", bannedRegex.containsMatchIn(hVal))
            }
        }

        val narrEn = yEn["category_narratives"]!!.jsonObject
        val narrHi = yHi["category_narratives"]!!.jsonObject
        assertEquals(catDomains, narrEn.keys)
        assertEquals(catDomains, narrHi.keys)
        val tiers = setOf("high", "balanced", "effort")
        catDomains.forEach { d ->
            val dEn = narrEn[d]!!.jsonObject
            val dHi = narrHi[d]!!.jsonObject
            assertEquals(tiers, dEn.keys)
            assertEquals(tiers, dHi.keys)
            tiers.forEach { t ->
                val tEn = dEn[t]!!.jsonObject
                val tHi = dHi[t]!!.jsonObject
                val pEn = tEn["paragraphs"]!!.jsonArray
                val pHi = tHi["paragraphs"]!!.jsonArray
                assertEquals(4, pEn.size)
                assertEquals(4, pHi.size)
                (pEn + pHi).forEach { el ->
                    val txt = el.jsonPrimitive.content
                    assertTrue(txt.isNotBlank())
                    assertFalse("Banned word in year2027 $d.$t paragraph: $txt", bannedRegex.containsMatchIn(txt))
                }
                val rEn = tEn["remedy"]!!.jsonPrimitive.content
                val rHi = tHi["remedy"]!!.jsonPrimitive.content
                assertTrue(rEn.isNotBlank() && rHi.isNotBlank())
                assertFalse("Banned word in year2027 EN $d.$t remedy", bannedRegex.containsMatchIn(rEn))
                assertFalse("Banned word in year2027 HI $d.$t remedy", bannedRegex.containsMatchIn(rHi))
            }
        }

        val monthlyEn = yEn["monthly_templates"]!!.jsonObject
        val monthlyHi = yHi["monthly_templates"]!!.jsonObject
        assertEquals(planets.toSet(), monthlyEn.keys)
        assertEquals(planets.toSet(), monthlyHi.keys)
        val templateArrays = setOf("moon_flavors", "body_variants", "lucky_tags", "effort_tags")
        planets.forEach { p ->
            val mEn = monthlyEn[p]!!.jsonObject
            val mHi = monthlyHi[p]!!.jsonObject
            assertEquals(templateArrays, mEn.keys)
            assertEquals(templateArrays, mHi.keys)
            templateArrays.forEach { arrKey ->
                val aEn = mEn[arrKey]!!.jsonArray
                val aHi = mHi[arrKey]!!.jsonArray
                assertEquals(3, aEn.size)
                assertEquals(3, aHi.size)
                (aEn + aHi).forEach { el ->
                    val txt = el.jsonPrimitive.content
                    assertTrue(txt.isNotBlank())
                    assertFalse("Banned word in year2027 monthly $p.$arrKey: $txt", bannedRegex.containsMatchIn(txt))
                }
            }
        }
    }

    @Test
    fun partAStoryBlocks_existForAll5CategoriesAndVaryAcrossChartsInEnAndHi() {
        val personA = PersonEntity(
            id = 101L,
            name = "Chart One",
            gender = "Male",
            birthDate = "1990-02-14",
            birthTime = "05:30",
            isTimeUnknown = false,
            birthPlace = "New Delhi, Delhi",
            latitude = 28.6139,
            longitude = 77.2090,
            timezoneId = "Asia/Kolkata",
            maritalStatus = "Single",
            occupation = "Engineer"
        )
        val personB = PersonEntity(
            id = 102L,
            name = "Chart Two",
            gender = "Female",
            birthDate = "1998-09-25",
            birthTime = "21:15",
            isTimeUnknown = false,
            birthPlace = "Mumbai, Maharashtra",
            latitude = 19.0760,
            longitude = 72.8777,
            timezoneId = "Asia/Kolkata",
            maritalStatus = "Married",
            occupation = "Business"
        )
        val refDate = java.time.LocalDate.of(2026, 6, 15)

        listOf("en", "hi").forEach { lang ->
            val kA = AstroEngine.computeKundali(
                birthDateStr = personA.birthDate,
                birthTimeStr = personA.birthTime,
                isTimeUnknown = personA.isTimeUnknown,
                lat = personA.latitude,
                lon = personA.longitude,
                tzId = personA.timezoneId,
                ayanamsaType = AyanamsaType.LAHIRI,
                langCode = lang,
                contentRepo = contentRepository,
                person = personA,
                referenceDate = refDate
            )
            val kB = AstroEngine.computeKundali(
                birthDateStr = personB.birthDate,
                birthTimeStr = personB.birthTime,
                isTimeUnknown = personB.isTimeUnknown,
                lat = personB.latitude,
                lon = personB.longitude,
                tzId = personB.timezoneId,
                ayanamsaType = AyanamsaType.LAHIRI,
                langCode = lang,
                contentRepo = contentRepository,
                person = personB,
                referenceDate = refDate
            )

            var differingCategories = 0
            ReadingCategory.entries.forEach { cat ->
                val storyA = kA.categoryDeepDives[cat]!!.storyParagraphs
                val storyB = kB.categoryDeepDives[cat]!!.storyParagraphs
                assertTrue("Expected 3..5 story paragraphs for $cat ($lang), found ${storyA.size}", storyA.size in 3..5)
                assertTrue("Expected 3..5 story paragraphs for $cat ($lang), found ${storyB.size}", storyB.size in 3..5)
                assertTrue(storyA.all { it.isNotBlank() && !it.contains("{planet}") && !it.contains("{maha}") })
                assertTrue(storyB.all { it.isNotBlank() && !it.contains("{planet}") && !it.contains("{maha}") })
                if (storyA != storyB) differingCategories++
            }
            assertTrue("Expected distinct charts to produce different story narratives ($lang)", differingCategories >= 3)
        }
    }
}
