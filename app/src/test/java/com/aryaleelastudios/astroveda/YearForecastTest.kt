package com.aryaleelastudios.astroveda

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import androidx.test.core.app.ApplicationProvider
import com.aryaleelastudios.astroveda.astro.AstroEngine
import com.aryaleelastudios.astroveda.data.local.PersonEntity
import com.aryaleelastudios.astroveda.data.model.AyanamsaType
import com.aryaleelastudios.astroveda.data.model.ChartStyle
import com.aryaleelastudios.astroveda.data.model.ReadingCategory
import com.aryaleelastudios.astroveda.data.repository.ContentRepository
import com.aryaleelastudios.astroveda.pdf.PdfExporter
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
class YearForecastTest {

    private lateinit var context: Context
    private lateinit var contentRepository: ContentRepository

    private val samplePerson = PersonEntity(
        id = 10L,
        name = "Kavya Nair",
        gender = "Female",
        birthDate = "1994-07-08",
        birthTime = "10:40",
        isTimeUnknown = false,
        birthPlace = "Bengaluru, Karnataka",
        latitude = 12.9716,
        longitude = 77.5946,
        timezoneId = "Asia/Kolkata",
        maritalStatus = "Single",
        occupation = "Engineer"
    )

    @Before
    fun setup() {
        context = ApplicationProvider.getApplicationContext()
        contentRepository = ContentRepository(context)
    }

    @Test
    fun computeYearForecast2027_isDeterministicCompleteAndLocalized() {
        val enForecast1 = AstroEngine.computeYearForecast(
            person = samplePerson,
            year = 2027,
            ayanamsaType = AyanamsaType.LAHIRI,
            langCode = "en",
            contentRepo = contentRepository
        )
        val enForecast2 = AstroEngine.computeYearForecast(
            person = samplePerson,
            year = 2027,
            ayanamsaType = AyanamsaType.LAHIRI,
            langCode = "en",
            contentRepo = contentRepository
        )
        val hiForecast = AstroEngine.computeYearForecast(
            person = samplePerson,
            year = 2027,
            ayanamsaType = AyanamsaType.LAHIRI,
            langCode = "hi",
            contentRepo = contentRepository
        )

        // Determinism
        assertEquals(enForecast1, enForecast2)
        assertEquals(2027, enForecast1.year)
        assertEquals(samplePerson.name, enForecast1.personName)

        // Parity of numerical structure across EN and HI while text localizes cleanly
        assertEquals(enForecast1.overallScore, hiForecast.overallScore)
        assertTrue(enForecast1.overallScore in 0..100)
        assertEquals(5, enForecast1.atAGlanceMeters.size)
        assertEquals(enForecast1.atAGlanceMeters.map { it.score }, hiForecast.atAGlanceMeters.map { it.score })
        assertTrue(enForecast1.atAGlanceMeters.all { it.score in 0..100 && it.label.isNotBlank() && it.note.isNotBlank() })

        // Valid month ranges 1..12
        assertTrue(enForecast1.goldenWindows.isNotEmpty())
        assertTrue(enForecast1.effortWindows.isNotEmpty())
        (enForecast1.goldenWindows + enForecast1.effortWindows + hiForecast.goldenWindows + hiForecast.effortWindows).forEach { w ->
            assertTrue("Invalid startMonth ${w.startMonth}", w.startMonth in 1..12)
            assertTrue("Invalid endMonth ${w.endMonth}", w.endMonth in w.startMonth..12)
            assertTrue(w.label.isNotBlank())
            assertTrue(w.reason.isNotBlank())
        }

        // 5 category deep-dives
        assertEquals(ReadingCategory.entries.toSet(), enForecast1.categoryDeepDives.map { it.category }.toSet())
        assertEquals(ReadingCategory.entries.toSet(), hiForecast.categoryDeepDives.map { it.category }.toSet())
        (enForecast1.categoryDeepDives + hiForecast.categoryDeepDives).forEach { dd ->
            assertTrue(dd.score in 0..100)
            assertTrue(dd.categoryLabel.isNotBlank())
            assertEquals(4, dd.paragraphs.size)
            assertTrue(dd.paragraphs.all { it.isNotBlank() })
            assertTrue(dd.remedy2027.isNotBlank())
            dd.paragraphs.forEach { p ->
                assertFalse("Unresolved placeholder in category paragraph: $p", p.contains("{maha}") || p.contains("{planet}"))
            }
        }

        // 12 monthly forecasts Jan..Dec
        assertEquals((1..12).toList(), enForecast1.monthlyForecasts.map { it.monthIndex })
        assertEquals((1..12).toList(), hiForecast.monthlyForecasts.map { it.monthIndex })
        assertNotEquals(enForecast1.monthlyForecasts.first().monthName, hiForecast.monthlyForecasts.first().monthName)
        (enForecast1.monthlyForecasts + hiForecast.monthlyForecasts).forEach { mf ->
            assertTrue(mf.monthName.isNotBlank())
            assertTrue(mf.dashaSubPeriod.isNotBlank())
            assertTrue(mf.moonTransitFlavor.isNotBlank())
            assertTrue(mf.prediction.isNotBlank())
            assertTrue(mf.luckyFocus.isNotBlank())
            assertTrue(mf.effortFocus.isNotBlank())
            assertFalse(
                "Unresolved placeholder in month ${mf.monthIndex}",
                mf.prediction.contains("{month}") || mf.prediction.contains("{maha}") || mf.prediction.contains("{antar}")
            )
        }

        // Empowering tone verification (no banned words)
        val bannedRegex = Regex("""\b(weak|bad)\b|कमजोर|कमज़ोर|बुरा|खराब|ख़राब""", RegexOption.IGNORE_CASE)
        val allCopy = listOf(
            enForecast1.signatureHeadline, enForecast1.heroSummary, enForecast1.disclaimer,
            hiForecast.signatureHeadline, hiForecast.heroSummary, hiForecast.disclaimer
        ) + enForecast1.atAGlanceMeters.map { it.note } +
            hiForecast.atAGlanceMeters.map { it.note } +
            enForecast1.categoryDeepDives.flatMap { it.paragraphs + it.remedy2027 } +
            hiForecast.categoryDeepDives.flatMap { it.paragraphs + it.remedy2027 } +
            enForecast1.monthlyForecasts.flatMap { listOf(it.prediction, it.luckyFocus, it.effortFocus) } +
            hiForecast.monthlyForecasts.flatMap { listOf(it.prediction, it.luckyFocus, it.effortFocus) }

        allCopy.forEach { text ->
            assertFalse("Banned word found in 2027 forecast: $text", bannedRegex.containsMatchIn(text))
        }
    }

    @Test
    fun masterPdfExporter_appends6PagesForYear2027WhenProvided() {
        val refDate = LocalDate.of(2026, 6, 15)
        val kundaliEn = AstroEngine.computeKundali(
            birthDateStr = samplePerson.birthDate,
            birthTimeStr = samplePerson.birthTime,
            isTimeUnknown = samplePerson.isTimeUnknown,
            lat = samplePerson.latitude,
            lon = samplePerson.longitude,
            tzId = samplePerson.timezoneId,
            ayanamsaType = AyanamsaType.LAHIRI,
            langCode = "en",
            contentRepo = contentRepository,
            person = samplePerson,
            referenceDate = refDate
        )
        val forecastEn = AstroEngine.computeYearForecast(
            person = samplePerson,
            year = 2027,
            ayanamsaType = AyanamsaType.LAHIRI,
            langCode = "en",
            contentRepo = contentRepository
        )

        val bitmap = Bitmap.createBitmap(PdfExporter.PAGE_WIDTH, PdfExporter.PAGE_HEIGHT, Bitmap.Config.ARGB_8888)
        val pagesWithout2027 = mutableListOf<Int>()
        val totalWithout = PdfExporter.renderMasterPages(
            context = context,
            person = samplePerson,
            kundali = kundaliEn,
            langCode = "en",
            chartStyle = ChartStyle.NORTH_INDIAN,
            yearForecast = null,
            startPage = { pageNum ->
                pagesWithout2027.add(pageNum)
                Canvas(bitmap)
            }
        )
        assertEquals(31, totalWithout)
        assertEquals((1..31).toList(), pagesWithout2027)

        val pagesWith2027 = mutableListOf<Int>()
        val totalWith = PdfExporter.renderMasterPages(
            context = context,
            person = samplePerson,
            kundali = kundaliEn,
            langCode = "en",
            chartStyle = ChartStyle.NORTH_INDIAN,
            yearForecast = forecastEn,
            startPage = { pageNum ->
                pagesWith2027.add(pageNum)
                Canvas(bitmap)
            }
        )
        assertEquals(40, totalWith)
        assertEquals((1..40).toList(), pagesWith2027)
        bitmap.recycle()
    }
}
