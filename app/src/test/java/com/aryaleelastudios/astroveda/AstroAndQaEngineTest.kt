package com.aryaleelastudios.astroveda

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import androidx.test.core.app.ApplicationProvider
import com.aryaleelastudios.astroveda.astro.AshtaKootaEngine
import com.aryaleelastudios.astroveda.astro.AstroEngine
import com.aryaleelastudios.astroveda.astro.CategoryReadingEngine
import com.aryaleelastudios.astroveda.data.local.PersonEntity
import com.aryaleelastudios.astroveda.data.model.AyanamsaType
import com.aryaleelastudios.astroveda.data.model.ChartStyle
import com.aryaleelastudios.astroveda.data.model.PredictionPeriod
import com.aryaleelastudios.astroveda.data.model.ReadingCategory
import com.aryaleelastudios.astroveda.data.repository.ContentRepository
import com.aryaleelastudios.astroveda.pdf.PdfExporter
import com.aryaleelastudios.astroveda.ui.components.computeAstroMeterMeta
import com.aryaleelastudios.astroveda.ui.components.toChartSpec
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.time.LocalDate

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class AstroAndQaEngineTest {

    private lateinit var context: Context
    private lateinit var contentRepository: ContentRepository

    private val samplePersonYoung = PersonEntity(
        id = 1L,
        name = "Aarav Sharma",
        gender = "Male",
        birthDate = "2004-05-14",
        birthTime = "08:30",
        isTimeUnknown = false,
        birthPlace = "New Delhi, Delhi",
        latitude = 28.6139,
        longitude = 77.2090,
        timezoneId = "Asia/Kolkata",
        maritalStatus = "Single",
        occupation = "Student"
    )

    private val samplePersonAdult = PersonEntity(
        id = 2L,
        name = "Meera Verma",
        gender = "Female",
        birthDate = "1986-11-22",
        birthTime = "19:45",
        isTimeUnknown = false,
        birthPlace = "Mumbai, Maharashtra",
        latitude = 19.0760,
        longitude = 72.8777,
        timezoneId = "Asia/Kolkata",
        maritalStatus = "Married",
        occupation = "Homemaker"
    )

    @Before
    fun setup() {
        context = ApplicationProvider.getApplicationContext()
        contentRepository = ContentRepository(context)
    }

    private fun computeForPerson(
        person: PersonEntity,
        langCode: String,
        refDate: LocalDate
    ) = AstroEngine.computeKundali(
        birthDateStr = person.birthDate,
        birthTimeStr = person.birthTime,
        isTimeUnknown = person.isTimeUnknown,
        lat = person.latitude,
        lon = person.longitude,
        tzId = person.timezoneId,
        ayanamsaType = AyanamsaType.LAHIRI,
        langCode = langCode,
        contentRepo = contentRepository,
        person = person,
        referenceDate = refDate
    )

    @Test
    fun computeKundali_producesLocalizedDataInEnglishAndHindi() {
        val refDate = LocalDate.of(2026, 6, 15)
        val kundaliEn = computeForPerson(samplePersonYoung, "en", refDate)
        val kundaliHi = computeForPerson(samplePersonYoung, "hi", refDate)

        assertEquals(9, kundaliEn.planets.size)
        assertEquals(12, kundaliEn.houses.size)
        assertEquals(9, kundaliEn.vimshottariDasha.size)
        assertEquals(5, kundaliEn.yogasAndDoshas.size)
        assertEquals(listOf("D9", "D10", "D7"), kundaliEn.divisionalCharts.map { it.code })

        // Same astronomical Rashi index across languages, localized labels differ
        assertEquals(kundaliEn.lagna.rashiIndex, kundaliHi.lagna.rashiIndex)
        assertNotEquals(kundaliEn.lagna.rashiName, kundaliHi.lagna.rashiName)
        assertNotEquals(kundaliEn.dailyPrediction.text, kundaliHi.dailyPrediction.text)

        // Cosmic Highlights & Mini Chart Specs (D10, D9, D1)
        assertTrue("Expected 3..4 highlights in EN, got ${kundaliEn.cosmicHighlights.size}", kundaliEn.cosmicHighlights.size in 3..4)
        assertTrue("Expected 3..4 highlights in HI, got ${kundaliHi.cosmicHighlights.size}", kundaliHi.cosmicHighlights.size in 3..4)
        listOf("D10", "D9", "D1").forEach { code ->
            val spec = kundaliEn.toChartSpec(code)
            assertTrue("Lagna rashi index out of range for $code", spec.lagnaRashiIndex in 0..11)
            assertEquals("Expected 9 planets in $code spec", 9, spec.planets.size)
            assertTrue("House numbers out of 1..12 for $code", spec.planets.all { it.houseNumber in 1..12 })
        }
    }

    @Test
    fun predictions_areDeterministicAndSoftWeighted() {
        val refDate = LocalDate.of(2026, 6, 15)
        val kundali1 = computeForPerson(samplePersonYoung, "en", refDate)
        val kundali2 = computeForPerson(samplePersonYoung, "en", refDate)

        // Same person + same date -> exact same prediction text
        assertEquals(kundali1.dailyPrediction.text, kundali2.dailyPrediction.text)
        assertEquals(kundali1.monthlyPrediction.text, kundali2.monthlyPrediction.text)
        assertEquals(kundali1.yearlyPrediction.text, kundali2.yearlyPrediction.text)
        assertEquals(kundali1.lifePrediction.text, kundali2.lifePrediction.text)

        // Check soft-weighting boosts matching bucket & lifestyle without zeroing off-bucket
        val studentWeight = contentRepository.computeFragmentWeight(
            tags = listOf("study", "career-start"),
            ageYears = 21,
            maritalStatus = "Single",
            occupation = "Student"
        )
        val offBucketWeight = contentRepository.computeFragmentWeight(
            tags = listOf("spirituality", "legacy"),
            ageYears = 21,
            maritalStatus = "Single",
            occupation = "Student"
        )
        assertTrue("Matching tags must boost weight", studentWeight > offBucketWeight)
        assertTrue("Off-bucket fragments must never be excluded (>= 1.0)", offBucketWeight >= 1.0)

        // Verify placeholders are resolved
        PredictionPeriod.entries.forEach { period ->
            val text = kundali1.predictionFor(period).text
            assertTrue(text.isNotBlank())
            assertTrue("Unresolved {dasha} token", !text.contains("{dasha}"))
            assertTrue("Unresolved {moon_sign} token", !text.contains("{moon_sign}"))
        }
    }

    @Test
    fun absorbedLifeTopics_all15TopicsAreIntegratedAcrossCategoriesWithRealTiming() {
        val refDate = LocalDate.of(2026, 6, 15)
        val kundaliEn = computeForPerson(samplePersonYoung, "en", refDate)
        val kundaliHi = computeForPerson(samplePersonYoung, "hi", refDate)

        val expectedIds = setOf(
            "career_field", "govt_job", "job_vs_business", "job_promotion",
            "dhan_yog", "debt_clearance", "property_yog",
            "marriage_timing", "marriage_type", "mangal_dosha", "children_timing",
            "health_areas", "sade_sati",
            "competitive_exams", "foreign_travel"
        )

        val enTopics = kundaliEn.categoryDeepDives.values.flatMap { it.absorbedReadings }
        val hiTopics = kundaliHi.categoryDeepDives.values.flatMap { it.absorbedReadings }

        assertEquals(expectedIds, enTopics.map { it.topicId }.toSet())
        assertEquals(expectedIds, hiTopics.map { it.topicId }.toSet())
        assertEquals(15, enTopics.size)
        assertEquals(15, hiTopics.size)

        (enTopics + hiTopics).forEach { topic ->
            assertTrue("Blank title for ${topic.topicId}", topic.title.isNotBlank())
            assertTrue("Blank timingWindow for ${topic.topicId}", topic.timingWindow.isNotBlank())
            assertTrue("Blank body for ${topic.topicId}", topic.body.isNotBlank())
            assertTrue("Blank astroBasis for ${topic.topicId}", topic.astroBasis.isNotBlank())
            assertFalse("Unresolved {planet} in ${topic.topicId}", topic.body.contains("{planet}"))
            assertFalse("Unresolved {house} in ${topic.topicId}", topic.body.contains("{house}"))
            assertFalse("Unresolved {year_range} in ${topic.topicId}", topic.body.contains("{year_range}"))
        }
    }

    @Test
    fun ashtaKootaEngine_computesValid36PointMatch() {
        val refDate = LocalDate.of(2026, 6, 15)
        val k1 = computeForPerson(samplePersonYoung, "en", refDate)
        val k2 = computeForPerson(samplePersonAdult, "en", refDate)

        val moon1 = k1.planets.first { it.canonicalName == "Moon" }
        val moon2 = k2.planets.first { it.canonicalName == "Moon" }
        val mangal1 = k1.yogasAndDoshas.find { it.canonicalKey == "Mangal" }?.isPresent == true
        val mangal2 = k2.yogasAndDoshas.find { it.canonicalKey == "Mangal" }?.isPresent == true

        val result = AshtaKootaEngine.matchHoroscopes(
            groomName = samplePersonYoung.name,
            groomRashiIdx = moon1.rashiIndex,
            groomNakshatraIdx = moon1.nakshatraIndex,
            groomIsManglik = mangal1,
            brideName = samplePersonAdult.name,
            brideRashiIdx = moon2.rashiIndex,
            brideNakshatraIdx = moon2.nakshatraIndex,
            brideIsManglik = mangal2,
            langCode = "en",
            contentRepo = contentRepository
        )

        assertTrue("Total score out of range: ${result.totalScore}", result.totalScore in 0.0..36.0)
        assertEquals(8, result.kootas.size)
        assertEquals(36.0, result.kootas.sumOf { it.maxScore }, 0.01)
        assertTrue(result.verdict.isNotBlank())
    }

    @Test
    fun careerTracks_separateEmploymentAndBusinessAndTriggerJupiter4thLandPath() {
        val refDate = LocalDate.of(2026, 6, 15)
        val base = computeForPerson(samplePersonYoung, "en", refDate)

        val jup4Planets = base.planets.map {
            when (it.canonicalName) {
                "Jupiter" -> it.copy(houseNumber = 4)
                else -> if (it.houseNumber == 4) it.copy(houseNumber = 2) else it
            }
        }

        val dives = CategoryReadingEngine.computeAllCategories(
            lagna = base.lagna,
            planets = jup4Planets,
            houses = base.houses,
            yogasAndDoshas = base.yogasAndDoshas,
            divisionalCharts = base.divisionalCharts,
            mahaLordCanonical = base.currentMahaLordCanonical,
            antarLordCanonical = base.currentAntarLordCanonical,
            langCode = "en",
            signCareerReading = base.careerReading,
            signRelationshipReading = base.relationshipReading,
            signHealthReading = base.healthReading,
            contentRepo = contentRepository,
            vimshottariDasha = base.vimshottariDasha,
            referenceYear = refDate.year
        )

        val career = dives[ReadingCategory.CAREER]!!
        assertTrue(career.employmentPaths.isNotEmpty())
        assertTrue(career.businessPaths.isNotEmpty())
        assertTrue(career.employmentVsBusinessGuidance.isNotBlank())
        assertTrue(
            "Expected Agriculture / Land Ventures path when Jupiter occupies 4th house",
            career.businessPaths.any {
                it.title.contains("Agriculture", ignoreCase = true) ||
                    it.title.contains("Land", ignoreCase = true)
            }
        )
    }

    @Test
    fun cosmicHighlightBarsAndCategoryDeepDives_coverAll5CategoriesDeterministically() {
        val refDate = LocalDate.of(2026, 6, 15)
        val kundaliEn = computeForPerson(samplePersonYoung, "en", refDate)
        val kundaliHi = computeForPerson(samplePersonYoung, "hi", refDate)

        // 4 Cosmic Highlight Progress Bars (Career, Wealth, Love, Health)
        assertEquals(listOf("career", "wealth", "love", "health"), kundaliEn.cosmicHighlightBars.map { it.domainKey })
        assertEquals(kundaliEn.cosmicHighlightBars.map { it.score }, kundaliHi.cosmicHighlightBars.map { it.score })
        kundaliEn.cosmicHighlightBars.forEach { bar ->
            assertTrue("Score out of range for ${bar.domainKey}: ${bar.score}", bar.score in 0..100)
            assertTrue("Empty label for ${bar.domainKey}", bar.label.isNotBlank())
            assertTrue("Empty summary for ${bar.domainKey}", bar.summary.isNotBlank())
        }

        // All 5 Detailed Category Deep Dives
        assertEquals(ReadingCategory.entries.toSet(), kundaliEn.categoryDeepDives.keys)
        assertEquals(ReadingCategory.entries.toSet(), kundaliHi.categoryDeepDives.keys)

        val sentenceCount = kundaliEn.cosmicBlueprint.split(Regex("[.!?।]+")).count { it.isNotBlank() }
        assertTrue("Expected at least 3 sentences in cosmicBlueprint, got $sentenceCount", sentenceCount >= 3)
        val sentenceCountHi = kundaliHi.cosmicBlueprint.split(Regex("[.!?।]+")).count { it.isNotBlank() }
        assertTrue("Expected at least 3 sentences in Hindi cosmicBlueprint, got $sentenceCountHi", sentenceCountHi >= 3)

        ReadingCategory.entries.forEach { cat ->
            val en = kundaliEn.categoryDeepDives[cat]!!
            val hi = kundaliHi.categoryDeepDives[cat]!!
            assertEquals("Overall score must match across languages for $cat", en.overallScore, hi.overallScore)
            assertTrue(en.overallScore in 0..100)
            assertTrue(en.signatureTitle.isNotBlank())
            assertTrue(hi.signatureTitle.isNotBlank())
            assertTrue(en.archetype.giftTitle.isNotBlank())
            assertTrue(hi.archetype.giftTitle.isNotBlank())
            assertTrue(en.archetype.recommendedPaths.isNotEmpty())
            assertTrue(en.archetype.cautiousZones.isNotEmpty())
            assertTrue(en.diversePaths.isNotEmpty())
            assertEquals(4, en.crossReferences.size)
            assertFalse(en.crossReferences.any { it.targetCategory == cat })
            assertTrue(en.powerBars.size >= 3)
            assertTrue(en.powerBars.all { it.score in 0..100 && it.glyph.isNotBlank() })
            assertTrue(en.proofItems.isNotEmpty())
            assertTrue(en.whatToDo.isNotEmpty())
            assertTrue(en.safetyAdvice.isNotEmpty())
            assertTrue(en.remedy.mantra.isNotBlank())
            assertTrue(en.remedy.charity.isNotBlank())
            assertTrue(en.remedy.ritual.isNotBlank())
            assertTrue("Empty personalityParagraph for $cat", en.personalityParagraph.isNotBlank())
            assertTrue("Empty strengthsParagraph for $cat", en.strengthsParagraph.isNotBlank())
            assertTrue("Empty challengesParagraph for $cat", en.challengesParagraph.isNotBlank())
            assertTrue("Empty superpower title for $cat", en.superpower.title.isNotBlank())
            assertTrue("Empty growthArea title for $cat", en.growthArea.title.isNotBlank())
            assertTrue("Empty growthArea guidance for $cat", en.growthArea.guidance.isNotBlank())
        }
    }

    @Test
    fun masterPdfExporter_generates31PagesInEnglishAndHindi() {
        val refDate = LocalDate.of(2026, 6, 15)
        val kundaliEn = computeForPerson(samplePersonYoung, "en", refDate)
        val kundaliHi = computeForPerson(samplePersonAdult, "hi", refDate)

        val bitmap = Bitmap.createBitmap(PdfExporter.PAGE_WIDTH, PdfExporter.PAGE_HEIGHT, Bitmap.Config.ARGB_8888)
        val renderedPagesEn = mutableListOf<Int>()
        val totalEn = PdfExporter.renderMasterPages(
            context = context,
            person = samplePersonYoung,
            kundali = kundaliEn,
            langCode = "en",
            chartStyle = ChartStyle.NORTH_INDIAN,
            startPage = { pageNum ->
                renderedPagesEn.add(pageNum)
                Canvas(bitmap)
            }
        )
        assertEquals(31, PdfExporter.TOTAL_PAGES)
        assertEquals(PdfExporter.TOTAL_PAGES, totalEn)
        assertEquals((1..PdfExporter.TOTAL_PAGES).toList(), renderedPagesEn)

        val renderedPagesHi = mutableListOf<Int>()
        val totalHi = PdfExporter.renderMasterPages(
            context = context,
            person = samplePersonAdult,
            kundali = kundaliHi,
            langCode = "hi",
            chartStyle = ChartStyle.SOUTH_INDIAN,
            startPage = { pageNum ->
                renderedPagesHi.add(pageNum)
                Canvas(bitmap)
            }
        )
        assertEquals(PdfExporter.TOTAL_PAGES, totalHi)
        assertEquals((1..PdfExporter.TOTAL_PAGES).toList(), renderedPagesHi)
        bitmap.recycle()
    }

    @Test
    fun categoryReadingEngine_producesDistinctArchetypesAcrossCharts() {
        val refDate = LocalDate.of(2026, 6, 15)
        val p1 = samplePersonYoung
        val p2 = samplePersonAdult.copy(birthDate = "1991-03-10", birthTime = "14:20")
        val k1 = computeForPerson(p1, "en", refDate)
        val k2 = computeForPerson(p2, "en", refDate)

        val mercPlanets = k1.planets.map {
            when (it.canonicalName) {
                "Mercury" -> it.copy(rashiIndex = 5, houseNumber = 10) // Exalted in Virgo & 10th house
                "Venus" -> it.copy(rashiIndex = 5, houseNumber = 6)    // Debilitated in 6th
                else -> it.copy(houseNumber = 6)
            }
        }
        val venusPlanets = k1.planets.map {
            when (it.canonicalName) {
                "Venus" -> it.copy(rashiIndex = 11, houseNumber = 10)  // Exalted in Pisces & 10th house
                "Mercury" -> it.copy(rashiIndex = 11, houseNumber = 6) // Debilitated in 6th
                else -> it.copy(houseNumber = 6)
            }
        }
        val mercCareer = CategoryReadingEngine.computeAllCategories(
            lagna = k1.lagna,
            planets = mercPlanets,
            houses = k1.houses,
            yogasAndDoshas = k1.yogasAndDoshas,
            divisionalCharts = k1.divisionalCharts,
            mahaLordCanonical = k1.currentMahaLordCanonical,
            antarLordCanonical = k1.currentAntarLordCanonical,
            langCode = "en",
            signCareerReading = k1.careerReading,
            signRelationshipReading = k1.relationshipReading,
            signHealthReading = k1.healthReading
        )[ReadingCategory.CAREER]!!
        val venusCareer = CategoryReadingEngine.computeAllCategories(
            lagna = k1.lagna,
            planets = venusPlanets,
            houses = k1.houses,
            yogasAndDoshas = k1.yogasAndDoshas,
            divisionalCharts = k1.divisionalCharts,
            mahaLordCanonical = k1.currentMahaLordCanonical,
            antarLordCanonical = k1.currentAntarLordCanonical,
            langCode = "en",
            signCareerReading = k1.careerReading,
            signRelationshipReading = k1.relationshipReading,
            signHealthReading = k1.healthReading
        )[ReadingCategory.CAREER]!!
        assertNotEquals(mercCareer.archetype.giftTitle, venusCareer.archetype.giftTitle)
        assertEquals("Analytical Strategist", mercCareer.archetype.giftTitle)
        assertEquals("Creative Visionary", venusCareer.archetype.giftTitle)
        assertTrue(k2.categoryDeepDives[ReadingCategory.CAREER]!!.archetype.giftTitle.isNotBlank())
    }

    @Test
    fun astroMeterMeta_mapsThresholdsAndStarCountsAccurately() {
        val exc = computeAstroMeterMeta(88)
        assertEquals(R.string.meter_excellent, exc.statusResId)
        assertEquals(5, exc.starCount)

        val fav = computeAstroMeterMeta(65)
        assertEquals(R.string.meter_favorable, fav.statusResId)
        assertEquals(4, fav.starCount)

        val growHigh = computeAstroMeterMeta(54)
        assertEquals(R.string.meter_growing, growHigh.statusResId)
        assertEquals(3, growHigh.starCount)

        val growLow = computeAstroMeterMeta(42)
        assertEquals(R.string.meter_growing, growLow.statusResId)
        assertEquals(2, growLow.starCount)

        val focusMid = computeAstroMeterMeta(32)
        assertEquals(R.string.meter_needs_focus, focusMid.statusResId)
        assertEquals(2, focusMid.starCount)

        val focusLow = computeAstroMeterMeta(18)
        assertEquals(R.string.meter_needs_focus, focusLow.statusResId)
        assertEquals(1, focusLow.starCount)
    }

    @Test
    fun categoryLibrary_pullsActualPlanetInHousePlacementTextDynamically() {
        val refDate = LocalDate.of(2026, 6, 15)
        val base = computeForPerson(samplePersonYoung, "en", refDate)
        val moon10Planets = base.planets.map {
            when (it.canonicalName) {
                "Moon" -> it.copy(houseNumber = 10)
                else -> if (it.houseNumber == 10) it.copy(houseNumber = 3) else it
            }
        }
        val moon10Career = CategoryReadingEngine.computeAllCategories(
            lagna = base.lagna,
            planets = moon10Planets,
            houses = base.houses,
            yogasAndDoshas = base.yogasAndDoshas,
            divisionalCharts = base.divisionalCharts,
            mahaLordCanonical = base.currentMahaLordCanonical,
            antarLordCanonical = base.currentAntarLordCanonical,
            langCode = "en",
            signCareerReading = base.careerReading,
            signRelationshipReading = base.relationshipReading,
            signHealthReading = base.healthReading,
            contentRepo = contentRepository
        )[ReadingCategory.CAREER]!!

        val expectedMoon10 = contentRepository.resolveCategoryPlacement("career", "en", "Moon", 10)
        assertTrue(expectedMoon10.isNotBlank())
        assertTrue(
            "Expected Career strengthsParagraph to include Moon_10 placement insight",
            moon10Career.strengthsParagraph.contains(expectedMoon10)
        )
    }

    @Test
    fun categoryBlueprintOverhaul_verifiesNormalizationDnaCountsAgeGateAndDailyRemedies() {
        val refDate = LocalDate.of(2026, 6, 15)
        val adultEn = computeForPerson(samplePersonAdult, "en", refDate)
        val adultHi = computeForPerson(samplePersonAdult, "hi", refDate)

        // Teen seeker (< 21 on refDate)
        val teenPerson = samplePersonYoung.copy(birthDate = "2010-05-14")
        val teenEn = computeForPerson(teenPerson, "en", refDate)

        // 1. DNA / Soul Alignment bar counts
        assertEquals(5, adultEn.categoryDeepDives[ReadingCategory.CAREER]!!.powerBars.size)
        assertEquals(5, adultEn.categoryDeepDives[ReadingCategory.WEALTH]!!.powerBars.size)
        assertEquals(5, adultEn.categoryDeepDives[ReadingCategory.MARRIAGE]!!.powerBars.size)
        assertEquals(5, adultEn.categoryDeepDives[ReadingCategory.HEALTH]!!.powerBars.size)
        assertEquals(4, adultEn.categoryDeepDives[ReadingCategory.DHARMA]!!.powerBars.size)

        // 2. Top 5 normalized matches in Career (Employment + Business) and Wealth must be 80..95 descending
        val career = adultEn.categoryDeepDives[ReadingCategory.CAREER]!!
        val wealth = adultEn.categoryDeepDives[ReadingCategory.WEALTH]!!
        listOf(career.topJobMatches, career.topBusinessMatches, wealth.topWealthMatches).forEach { matches ->
            assertEquals(5, matches.size)
            matches.forEach { card ->
                assertTrue("Match % out of 80..95 range: ${card.matchPercent}", card.matchPercent in 80..95)
                assertTrue("Blank title in match card", card.title.isNotBlank())
                assertTrue("Blank description in match card", card.description.isNotBlank())
                assertTrue("Blank chartReference in match card", card.chartReference.isNotBlank())
            }
            val percents = matches.map { it.matchPercent }
            assertEquals("Normalized matches should be descending", percents.sortedDescending(), percents)
        }
        assertEquals(3, career.broadScopes.size)

        // 3. Marriage < 21 Age-Gate
        val teenMarriage = teenEn.categoryDeepDives[ReadingCategory.MARRIAGE]!!
        val adultMarriage = adultEn.categoryDeepDives[ReadingCategory.MARRIAGE]!!
        assertTrue("Expected age < 21 Future Focus gate", teenMarriage.isAgeGatedFutureFocus)
        assertFalse("Adult >= 21 should not be age-gated", adultMarriage.isAgeGatedFutureFocus)
        assertTrue(teenMarriage.creativeFaqs.isNotEmpty())
        assertTrue(adultMarriage.creativeFaqs.isNotEmpty())

        // 4. Health mandatory medical disclaimer
        val healthEn = adultEn.categoryDeepDives[ReadingCategory.HEALTH]!!
        val healthHi = adultHi.categoryDeepDives[ReadingCategory.HEALTH]!!
        assertTrue(healthEn.medicalDisclaimer.isNotBlank())
        assertTrue(healthHi.medicalDisclaimer.isNotBlank())
        assertTrue(healthEn.tier1Cards.size == 3)

        // 5. Dharma Atmakaraka + Ketu in astroMathPill
        val dharmaEn = adultEn.categoryDeepDives[ReadingCategory.DHARMA]!!
        val dharmaHi = adultHi.categoryDeepDives[ReadingCategory.DHARMA]!!
        assertTrue(dharmaEn.astroMathPill.contains("Atmakaraka") && dharmaEn.astroMathPill.contains("Ketu"))
        assertTrue(dharmaHi.astroMathPill.contains("आत्मकारक") && dharmaHi.astroMathPill.contains("केतु"))

        // 6. All 5 pages have heroSummary, astroMathPill, tier3Cards, creativeFaqs
        ReadingCategory.entries.forEach { cat ->
            val dive = adultEn.categoryDeepDives[cat]!!
            assertTrue("Blank heroSummary for $cat", dive.heroSummary.isNotBlank())
            assertTrue("Blank astroMathPill for $cat", dive.astroMathPill.isNotBlank())
            assertTrue("Missing tier3Cards for $cat", dive.tier3Cards.isNotEmpty())
            assertTrue("Missing creativeFaqs for $cat", dive.creativeFaqs.isNotEmpty())
        }

        // 7. Daily Remedies Footer: all 5 categories with Mantra, Why, How
        assertEquals(5, adultEn.dailyRemedies.size)
        assertEquals(5, adultHi.dailyRemedies.size)
        assertEquals(ReadingCategory.entries.toSet(), adultEn.dailyRemedies.map { it.category }.toSet())
        (adultEn.dailyRemedies + adultHi.dailyRemedies).forEach { rem ->
            assertTrue(rem.categoryLabel.isNotBlank())
            assertTrue(rem.mantra.isNotBlank())
            assertTrue(rem.why.isNotBlank())
            assertTrue(rem.how.isNotBlank())
        }
    }
}

