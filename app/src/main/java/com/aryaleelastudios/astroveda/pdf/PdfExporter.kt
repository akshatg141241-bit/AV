package com.aryaleelastudios.astroveda.pdf

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import android.graphics.Shader
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.net.Uri
import android.text.Layout
import android.text.StaticLayout
import android.text.TextPaint
import com.aryaleelastudios.astroveda.R
import com.aryaleelastudios.astroveda.data.i18n.LocaleHelper
import com.aryaleelastudios.astroveda.data.local.PersonEntity
import com.aryaleelastudios.astroveda.data.model.CategoryDeepDive
import com.aryaleelastudios.astroveda.data.model.ChartStyle
import com.aryaleelastudios.astroveda.data.model.DivisionalChart
import com.aryaleelastudios.astroveda.data.model.Gender
import com.aryaleelastudios.astroveda.data.model.KundaliData
import com.aryaleelastudios.astroveda.data.model.ReadingCategory
import com.aryaleelastudios.astroveda.data.model.YearForecast
import com.aryaleelastudios.astroveda.ui.components.AstroMeterMeta
import com.aryaleelastudios.astroveda.ui.components.KundaliChartSpec
import com.aryaleelastudios.astroveda.ui.components.computeAstroMeterMeta
import com.aryaleelastudios.astroveda.ui.components.toChartSpec
import java.util.Locale

object PdfExporter {

    const val PAGE_WIDTH = 595
    const val PAGE_HEIGHT = 842
    const val MARGIN_X = 36f
    const val CONTENT_WIDTH = PAGE_WIDTH - (MARGIN_X * 2f)
    const val TOTAL_PAGES = 31
    private val activeTotalPages = ThreadLocal<Int>()

    private val COLOR_INDIGO = Color.rgb(0x2B, 0x2D, 0x5C)
    private val COLOR_INDIGO_DARK = Color.rgb(0x1D, 0x1F, 0x45)
    private val COLOR_SAFFRON = Color.rgb(0xE8, 0xA3, 0x3D)
    private val COLOR_SAFFRON_DARK = Color.rgb(0xB8, 0x6E, 0x14)
    private val COLOR_CREAM = Color.rgb(0xFF, 0xF8, 0xEC)
    private val COLOR_CREAM_BORDER = Color.rgb(0xE8, 0xDE, 0xCA)
    private val COLOR_TEXT = Color.rgb(0x1F, 0x20, 0x41)
    private val COLOR_MUTED = Color.rgb(0x61, 0x61, 0x76)
    private val COLOR_GREEN = Color.rgb(0x2E, 0x7D, 0x32)
    private val COLOR_RED_MUTED = Color.rgb(0xA8, 0x38, 0x3B)
    private val COLOR_ROW_ALT = Color.rgb(0xFA, 0xF6, 0xEE)
    private val COLOR_TABLE_HEAD = Color.rgb(0xEE, 0xE9, 0xDD)

    fun exportToUri(
        context: Context,
        uri: Uri,
        person: PersonEntity,
        kundali: KundaliData,
        langCode: String,
        chartStyle: ChartStyle = ChartStyle.NORTH_INDIAN,
        yearForecast: YearForecast? = null
    ): Boolean {
        var document: PdfDocument? = null
        return try {
            document = buildMasterDocument(
                context = context,
                person = person,
                kundali = kundali,
                langCode = langCode,
                chartStyle = chartStyle,
                yearForecast = yearForecast
            )
            val stream = context.contentResolver.openOutputStream(uri) ?: return false
            stream.use { out ->
                document.writeTo(out)
            }
            true
        } catch (_: Exception) {
            false
        } finally {
            try {
                document?.close()
            } catch (_: Exception) {
            }
        }
    }

    fun buildMasterDocument(
        context: Context,
        person: PersonEntity,
        kundali: KundaliData,
        langCode: String,
        chartStyle: ChartStyle = ChartStyle.NORTH_INDIAN,
        yearForecast: YearForecast? = null
    ): PdfDocument {
        val document = PdfDocument()
        var currentPage: PdfDocument.Page? = null
        renderMasterPages(
            context = context,
            person = person,
            kundali = kundali,
            langCode = langCode,
            chartStyle = chartStyle,
            yearForecast = yearForecast,
            startPage = { pageNumber ->
                val pageInfo = PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, pageNumber).create()
                val page = document.startPage(pageInfo)
                currentPage = page
                val bgPaint = Paint().apply { color = Color.WHITE }
                page.canvas.drawRect(0f, 0f, PAGE_WIDTH.toFloat(), PAGE_HEIGHT.toFloat(), bgPaint)
                page.canvas
            },
            finishPage = {
                currentPage?.let { document.finishPage(it) }
                currentPage = null
            }
        )
        return document
    }

    fun renderMasterPages(
        context: Context,
        person: PersonEntity,
        kundali: KundaliData,
        langCode: String,
        chartStyle: ChartStyle = ChartStyle.NORTH_INDIAN,
        yearForecast: YearForecast? = null,
        startPage: (Int) -> Canvas,
        finishPage: () -> Unit = {}
    ): Int {
        val lCtx = LocaleHelper.localizedContext(context, langCode)
        val isHi = langCode == "hi"
        var pageNum = 1
        val effectiveTotalPages = if (yearForecast != null) TOTAL_PAGES + 9 else TOTAL_PAGES
        activeTotalPages.set(effectiveTotalPages)

        fun withPage(block: (Canvas, Int) -> Unit) {
            val number = pageNum++
            val canvas = startPage(number)
            val bgPaint = Paint().apply { color = Color.WHITE }
            canvas.drawRect(0f, 0f, PAGE_WIDTH.toFloat(), PAGE_HEIGHT.toFloat(), bgPaint)
            block(canvas, number)
            finishPage()
        }

        return try {
            // Page 1: Cover & Table of Contents
            withPage { canvas, _ ->
                drawCoverPage(canvas, lCtx, person, kundali, isHi, effectiveTotalPages, yearForecast != null)
            }

        // Page 2: Cosmic Blueprint, Cosmic Highlights & Predictions Overview
        withPage { canvas, num ->
            drawCosmicAndPredictionsPage(canvas, lCtx, person, kundali, isHi, num)
        }

        // Page 3: Main Natal Birth Chart (D1) & Panchang
        withPage { canvas, num ->
            drawNatalChartAndPanchangPage(canvas, lCtx, person, kundali, chartStyle, isHi, num)
        }

        // Pages 4..23: 5 Detailed Categories x 4 pages each = 20 pages
        ReadingCategory.entries.forEach { category ->
            val dive = kundali.categoryDeepDives[category]
            if (dive != null) {
                withPage { canvas, num ->
                    drawCategoryPageOne(canvas, lCtx, person, dive, isHi, num)
                }
                withPage { canvas, num ->
                    drawCategoryPageTwo(canvas, lCtx, person, dive, isHi, num)
                }
                withPage { canvas, num ->
                    drawCategoryPageThree(canvas, lCtx, person, dive, isHi, num)
                }
                withPage { canvas, num ->
                    drawCategoryPageFour(canvas, lCtx, person, kundali, dive, chartStyle, isHi, num)
                }
            }
        }

        // Page 19: Planetary Ephemeris Table
        withPage { canvas, num ->
            drawPlanetaryTablePage(canvas, lCtx, person, kundali, isHi, num)
        }

        // Page 20: 12 Bhava House Cusps
        withPage { canvas, num ->
            drawBhavaCuspsPage(canvas, lCtx, person, kundali, isHi, num)
        }

        // Pages 21..22: Full Vimshottari Dasha Timeline (Maha + Antar)
        withPage { canvas, num ->
            drawDashaTimelinePage(canvas, lCtx, person, kundali, 0, 5, isHi, num)
        }
        withPage { canvas, num ->
            drawDashaTimelinePage(canvas, lCtx, person, kundali, 5, kundali.vimshottariDasha.size, isHi, num)
        }

        // Page 23: Complete Yogas & Doshas Analysis
        withPage { canvas, num ->
            drawYogasAndDoshasPage(canvas, lCtx, person, kundali, isHi, num)
        }

        // Page 24: Divisional Charts D9 & D10 Visual + Placements
        withPage { canvas, num ->
            drawDivisionalChartsPairPage(canvas, lCtx, person, kundali, chartStyle, isHi, num)
        }

        // Page 25: Divisional Chart D7 + Complete D1/D9/D10/D7 Sign Matrix
        withPage { canvas, num ->
            drawD7AndMatrixPage(canvas, lCtx, person, kundali, chartStyle, isHi, num)
        }

        // Page 31: Master Consolidated Remedies, Mantras & Weekly Practice
        withPage { canvas, num ->
            drawMasterRemediesPage(canvas, lCtx, person, kundali, isHi, num)
        }

        // Pages 32..40: Optional 2027 Annual Edition Appendix
        if (yearForecast != null) {
            withPage { canvas, num ->
                draw2027OverviewPage(canvas, lCtx, person, yearForecast, isHi, num)
            }
            withPage { canvas, num ->
                draw2027DeepDivesPage(canvas, lCtx, person, yearForecast, 0, 2, isHi, num)
            }
            withPage { canvas, num ->
                draw2027DeepDivesPage(canvas, lCtx, person, yearForecast, 2, 4, isHi, num)
            }
            withPage { canvas, num ->
                draw2027DeepDivesWithDashaPage(canvas, lCtx, person, yearForecast, isHi, num)
            }
            withPage { canvas, num ->
                draw2027MonthlyTimelinePage(canvas, lCtx, person, yearForecast, 0, 4, isHi, num)
            }
            withPage { canvas, num ->
                draw2027MonthlyTimelinePage(canvas, lCtx, person, yearForecast, 4, 8, isHi, num)
            }
            withPage { canvas, num ->
                draw2027MonthlyTimelinePage(canvas, lCtx, person, yearForecast, 8, 12, isHi, num)
            }
            withPage { canvas, num ->
                draw2027ChartsAppendixPage(canvas, lCtx, person, kundali, chartStyle, isHi, num)
            }
            withPage { canvas, num ->
                draw2027RemediesClosingPage(canvas, lCtx, person, yearForecast, isHi, num)
            }
        }

        pageNum - 1
        } finally {
            activeTotalPages.remove()
        }
    }

    private fun drawPageHeaderFooter(
        canvas: Canvas,
        lCtx: Context,
        sectionTitle: String,
        personName: String,
        pageNumber: Int
    ) {
        val paint = Paint().apply { isAntiAlias = true }
        paint.color = COLOR_INDIGO
        canvas.drawRect(0f, 0f, PAGE_WIDTH.toFloat(), 58f, paint)
        paint.color = COLOR_SAFFRON
        canvas.drawRect(0f, 58f, PAGE_WIDTH.toFloat(), 61f, paint)

        val titlePaint = TextPaint().apply {
            isAntiAlias = true
            color = COLOR_SAFFRON
            textSize = 15f
            typeface = Typeface.create(Typeface.SERIF, Typeface.BOLD)
        }
        canvas.drawText(sectionTitle, MARGIN_X, 27f, titlePaint)

        val subPaint = TextPaint().apply {
            isAntiAlias = true
            color = Color.WHITE
            textSize = 9.5f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        }
        canvas.drawText("AstroVeda • $personName", MARGIN_X, 45f, subPaint)

        // Footer divider and page count
        paint.color = COLOR_CREAM_BORDER
        paint.strokeWidth = 1f
        canvas.drawLine(MARGIN_X, PAGE_HEIGHT - 40f, PAGE_WIDTH - MARGIN_X, PAGE_HEIGHT - 40f, paint)

        val footPaint = TextPaint().apply {
            isAntiAlias = true
            color = COLOR_MUTED
            textSize = 8.5f
        }
        canvas.drawText(lCtx.getString(R.string.pdf_footer), MARGIN_X, PAGE_HEIGHT - 24f, footPaint)

        val totalPages = activeTotalPages.get() ?: TOTAL_PAGES
        val pageStr = "Page $pageNumber / $totalPages"
        val pw = footPaint.measureText(pageStr)
        canvas.drawText(pageStr, PAGE_WIDTH - MARGIN_X - pw, PAGE_HEIGHT - 24f, footPaint)
    }

    private fun drawCoverPage(
        canvas: Canvas,
        lCtx: Context,
        person: PersonEntity,
        kundali: KundaliData,
        isHi: Boolean,
        totalPages: Int = TOTAL_PAGES,
        include2027Appendix: Boolean = false
    ) {
        val paint = Paint().apply { isAntiAlias = true }

        // Deep Indigo Royal Header Band
        paint.color = COLOR_INDIGO
        canvas.drawRect(0f, 0f, PAGE_WIDTH.toFloat(), 230f, paint)
        paint.color = COLOR_SAFFRON
        canvas.drawRect(0f, 230f, PAGE_WIDTH.toFloat(), 236f, paint)

        // Decorative border on page
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 1.5f
        paint.color = COLOR_SAFFRON
        canvas.drawRect(18f, 18f, PAGE_WIDTH - 18f, PAGE_HEIGHT - 18f, paint)
        paint.style = Paint.Style.FILL

        val badgePaint = TextPaint().apply {
            isAntiAlias = true
            color = COLOR_SAFFRON
            textSize = 11f
            letterSpacing = 0.14f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }
        canvas.drawText(
            if (isHi) "संपूर्ण वैदिक ज्योतिष एवं कुंडली महाग्रंथ" else "COMPREHENSIVE VEDIC ASTROLOGY REPORT",
            MARGIN_X,
            68f,
            badgePaint
        )

        val heroPaint = TextPaint().apply {
            isAntiAlias = true
            color = Color.WHITE
            textSize = 30f
            typeface = Typeface.create(Typeface.SERIF, Typeface.BOLD)
        }
        canvas.drawText("AstroVeda Master Report", MARGIN_X, 112f, heroPaint)

        val subHeroPaint = TextPaint().apply {
            isAntiAlias = true
            color = Color.rgb(0xFF, 0xF0, 0xD2)
            textSize = 13f
        }
        drawWrappedText(
            canvas,
            lCtx.getString(R.string.pdf_header_subtitle),
            MARGIN_X,
            130f,
            CONTENT_WIDTH,
            subHeroPaint
        )

        val personBannerPaint = TextPaint().apply {
            isAntiAlias = true
            color = COLOR_SAFFRON
            textSize = 20f
            typeface = Typeface.create(Typeface.SERIF, Typeface.BOLD)
        }
        val genderLabel = lCtx.getString(Gender.fromStorageKey(person.gender).labelResId)
        canvas.drawText("${person.name} ($genderLabel)", MARGIN_X, 198f, personBannerPaint)

        var y = 262f

        // Birth Profile Card
        y = drawCardBox(canvas, MARGIN_X, y, CONTENT_WIDTH, 116f)
        val cardY = y - 116f
        val boldDark = TextPaint().apply {
            isAntiAlias = true
            color = COLOR_INDIGO
            textSize = 12.5f
            typeface = Typeface.create(Typeface.SERIF, Typeface.BOLD)
        }
        val bodyDark = TextPaint().apply {
            isAntiAlias = true
            color = COLOR_TEXT
            textSize = 10.5f
        }
        val timeStr = if (person.isTimeUnknown) lCtx.getString(R.string.time_unknown_display) else person.birthTime
        canvas.drawText(if (isHi) "जन्म विवरण" else "Birth Profile & Coordinates", MARGIN_X + 14f, cardY + 22f, boldDark)
        canvas.drawText("${lCtx.getString(R.string.pdf_date_label)} ${person.birthDate}   |   ${lCtx.getString(R.string.pdf_time_label)} $timeStr", MARGIN_X + 14f, cardY + 44f, bodyDark)
        canvas.drawText("${lCtx.getString(R.string.pdf_place_label)} ${person.birthPlace}", MARGIN_X + 14f, cardY + 63f, bodyDark)
        canvas.drawText(
            String.format(Locale.US, "Coordinates: %.4f° N/S, %.4f° E/W   |   Timezone: %s", person.latitude, person.longitude, person.timezoneId),
            MARGIN_X + 14f,
            cardY + 82f,
            bodyDark
        )
        canvas.drawText(
            if (isHi) "वर्तमान दशा: ${kundali.currentDashaSummary}" else "Active Vimshottari Dasha: ${kundali.currentDashaSummary}",
            MARGIN_X + 14f,
            cardY + 101f,
            bodyDark
        )

        y += 18f

        // Snapshot 3-Column Metrics
        val moon = kundali.planets.firstOrNull { it.canonicalName == "Moon" }
        val sun = kundali.planets.firstOrNull { it.canonicalName == "Sun" }
        val boxW = (CONTENT_WIDTH - 20f) / 3f
        drawMiniStatCard(canvas, MARGIN_X, y, boxW, if (isHi) "लग्न राशि" else "Lagna (Ascendant)", kundali.lagna.rashiName, kundali.lagna.formattedDegree)
        drawMiniStatCard(canvas, MARGIN_X + boxW + 10f, y, boxW, if (isHi) "चन्द्र राशि" else "Moon Sign (Rashi)", moon?.rashiName ?: "-", moon?.nakshatraName ?: "-")
        drawMiniStatCard(canvas, MARGIN_X + (boxW + 10f) * 2f, y, boxW, if (isHi) "सूर्य राशि" else "Sun Sign (Surya)", sun?.rashiName ?: "-", sun?.formattedDegree ?: "-")
        y += 78f

        // Table of Contents
        canvas.drawText(
            if (isHi) "विषय सूची ($totalPages पृष्ठ)" else "Report Contents ($totalPages Pages)",
            MARGIN_X,
            y,
            boldDark
        )
        y += 10f

        val baseTocEntries = if (isHi) {
            listOf(
                "पृष्ठ 1" to "मुख्य पृष्ठ एवं जन्म विवरण",
                "पृष्ठ 2" to "कॉस्मिक ब्लूप्रिंट, प्रमुख जीवन सूचकांक एवं भविष्यफल",
                "पृष्ठ 3" to "लग्न कुंडली (D1) एवं जन्म पंचांग",
                "पृष्ठ 4–7" to "करियर एवं कर्म क्षेत्र (नौकरी/व्यवसाय, समय, D10, सुरक्षा सलाह)",
                "पृष्ठ 8–11" to "धन एवं समृद्धि (धन योग, संपत्ति समय, उपाय, सुरक्षा सलाह)",
                "पृष्ठ 12–15" to "विवाह एवं संबंध (नवांश D9, विवाह समय, संतान, उपाय)",
                "पृष्ठ 16–19" to "स्वास्थ्य एवं प्राण ऊर्जा (साढ़े साती, जीवनशैली, उपाय)",
                "पृष्ठ 20–23" to "धर्म एवं जीवन उद्देश्य (आत्मकारक, कर्म-पाठ, उपाय)",
                "पृष्ठ 24–25" to "नवग्रह स्पष्ट तालिका एवं द्वादश भाव संधि",
                "पृष्ठ 26–27" to "संपूर्ण विंशोत्तरी महादशा एवं अंतर्दशा समय सारिणी",
                "पृष्ठ 28" to "प्रमुख योग एवं दोष विवेचना",
                "पृष्ठ 29–30" to "षोडशवर्ग कुंडलियां (D9 नवमांश, D10 दशमांश, D7 सप्तमांश)",
                "पृष्ठ 31" to "समेकित मंत्र, रत्न, दान एवं साप्ताहिक अभ्यास"
            )
        } else {
            listOf(
                "Page 1" to "Cover Page & Birth Profile Snapshot",
                "Page 2" to "Cosmic Blueprint, Highlights & Multi-Horizon Forecasts",
                "Page 3" to "Main Natal Birth Chart (D1) & Birth Panchang",
                "Pages 4–7" to "Deep Dive: Career & Vocation (Employment/Business, Timing, D10)",
                "Pages 8–11" to "Deep Dive: Wealth & Prosperity (Asset & Dhan Windows, Proof)",
                "Pages 12–15" to "Deep Dive: Relationships & Marriage (D9, Timing, Safety, Remedies)",
                "Pages 16–19" to "Deep Dive: Health & Vitality (Wellness, Sade Sati, Remedies)",
                "Pages 20–23" to "Deep Dive: Dharma & Life Purpose (Atmakaraka, Soul Mission, Remedies)",
                "Pages 24–25" to "Complete Planetary Ephemeris & 12 Bhava House Cusps",
                "Pages 26–27" to "Full Vimshottari Dasha Timeline (9 Maha & 81 Antar Periods)",
                "Page 28" to "Comprehensive Yogas & Doshas Analysis",
                "Pages 29–30" to "Visual Divisional Charts (D9, D10, D7) & Sign Comparison Matrix",
                "Page 31" to "Master Consolidated Remedies, Mantras & Weekly Practice"
            )
        }
        val tocEntries = if (include2027Appendix) {
            baseTocEntries + (
                if (isHi) "पृष्ठ 32–40" to "2027 वार्षिक विशेष परिशिष्ट (5 क्षेत्र, 12 माह, चक्र एवं उपाय)"
                else "Pages 32–40" to "2027 Annual Edition (Deep-Dives, 12 Months, Charts & Remedies)"
                )
        } else {
            baseTocEntries
        }

        val tocRowPaint = TextPaint().apply {
            isAntiAlias = true
            color = COLOR_TEXT
            textSize = if (include2027Appendix) 9.0f else 9.5f
        }
        val tocPagePaint = TextPaint().apply {
            isAntiAlias = true
            color = COLOR_SAFFRON_DARK
            textSize = if (include2027Appendix) 9.0f else 9.5f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }
        val rowStep = if (include2027Appendix) 19f else 21f
        val rowBgH = if (include2027Appendix) 18f else 20f

        tocEntries.forEachIndexed { idx, (pg, desc) ->
            if (idx % 2 == 0) {
                paint.color = COLOR_ROW_ALT
                canvas.drawRect(MARGIN_X, y, PAGE_WIDTH - MARGIN_X, y + rowBgH, paint)
            }
            canvas.drawText(pg, MARGIN_X + 8f, y + (rowBgH * 0.68f), tocPagePaint)
            canvas.drawText(desc, MARGIN_X + 86f, y + (rowBgH * 0.68f), tocRowPaint)
            y += rowStep
        }

        // Bottom Disclaimer
        val footPaint = TextPaint().apply {
            isAntiAlias = true
            color = COLOR_MUTED
            textSize = 8.5f
        }
        canvas.drawText(lCtx.getString(R.string.pdf_footer), MARGIN_X, PAGE_HEIGHT - 28f, footPaint)
    }

    private fun drawCosmicAndPredictionsPage(
        canvas: Canvas,
        lCtx: Context,
        person: PersonEntity,
        kundali: KundaliData,
        isHi: Boolean,
        pageNum: Int
    ) {
        drawPageHeaderFooter(
            canvas,
            lCtx,
            if (isHi) "कॉस्मिक ब्लूप्रिंट, प्रमुख सूचकांक एवं भविष्यफल" else "Cosmic Blueprint, Highlights & Forecasts",
            person.name,
            pageNum
        )

        var y = 78f
        val bodyPaint = TextPaint().apply {
            isAntiAlias = true
            color = COLOR_TEXT
            textSize = 9.2f
        }

        if (kundali.cosmicBlueprint.isNotBlank()) {
            y = drawSectionHeading(canvas, lCtx.getString(R.string.section_cosmic_blueprint), MARGIN_X, y)
            val blueprintH = measureTextHeight(kundali.cosmicBlueprint, CONTENT_WIDTH - 24f, bodyPaint)
            val cardH = blueprintH + 18f
            if (y + cardH < PAGE_HEIGHT - 60f) {
                drawCardBox(canvas, MARGIN_X, y, CONTENT_WIDTH, cardH)
                drawWrappedText(canvas, kundali.cosmicBlueprint, MARGIN_X + 12f, y + 10f, CONTENT_WIDTH - 24f, bodyPaint)
                y += cardH + 10f
            }
        }

        y = drawSectionHeading(canvas, lCtx.getString(R.string.section_cosmic_highlights), MARGIN_X, y)

        kundali.cosmicHighlightBars.forEach { bar ->
            y = drawProgressBarRow(
                canvas = canvas,
                lCtx = lCtx,
                label = "${bar.glyph}  ${bar.label}",
                score = bar.score,
                summary = bar.summary,
                x = MARGIN_X,
                y = y,
                width = CONTENT_WIDTH
            )
            y += 5f
        }

        y += 4f
        y = drawSectionHeading(canvas, lCtx.getString(R.string.section_predictions), MARGIN_X, y)

        val forecasts = listOf(
            Triple(lCtx.getString(R.string.prediction_daily_card), kundali.dailyPrediction.theme, kundali.dailyPrediction.text),
            Triple(lCtx.getString(R.string.prediction_monthly_card), kundali.monthlyPrediction.theme, kundali.monthlyPrediction.text),
            Triple(lCtx.getString(R.string.prediction_yearly_card), kundali.yearlyPrediction.theme, kundali.yearlyPrediction.text),
            Triple(lCtx.getString(R.string.prediction_life_card), kundali.lifePrediction.theme, kundali.lifePrediction.text)
        )

        val titlePaint = TextPaint().apply {
            isAntiAlias = true
            color = COLOR_INDIGO
            textSize = 10.5f
            typeface = Typeface.create(Typeface.SERIF, Typeface.BOLD)
        }
        val themePaint = TextPaint().apply {
            isAntiAlias = true
            color = COLOR_SAFFRON_DARK
            textSize = 9f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }

        forecasts.forEach { (heading, theme, text) ->
            val measuredBody = measureTextHeight(text, CONTENT_WIDTH - 24f, bodyPaint)
            val cardH = 36f + measuredBody + 8f
            if (y + cardH < PAGE_HEIGHT - 55f) {
                drawCardBox(canvas, MARGIN_X, y, CONTENT_WIDTH, cardH)
                canvas.drawText(heading, MARGIN_X + 12f, y + 16f, titlePaint)
                canvas.drawText(theme, MARGIN_X + 12f, y + 29f, themePaint)
                drawWrappedText(canvas, text, MARGIN_X + 12f, y + 35f, CONTENT_WIDTH - 24f, bodyPaint)
                y += cardH + 8f
            }
        }
    }

    private fun drawNatalChartAndPanchangPage(
        canvas: Canvas,
        lCtx: Context,
        person: PersonEntity,
        kundali: KundaliData,
        chartStyle: ChartStyle,
        isHi: Boolean,
        pageNum: Int
    ) {
        drawPageHeaderFooter(
            canvas,
            lCtx,
            if (isHi) "मुख्य जन्म कुंडली (D1) एवं पंचांग" else "Natal Birth Chart (D1) & Birth Panchang",
            person.name,
            pageNum
        )

        var y = 80f
        val spec = kundali.toChartSpec("D1")
        val chartSize = 330f
        val chartX = (PAGE_WIDTH - chartSize) / 2f
        val lagnaBadge = lCtx.getString(R.string.chart_lagna_short)
        val ascBadge = lCtx.getString(R.string.chart_asc_short)
        val subtitle = lCtx.getString(R.string.chart_lagna_label, kundali.lagna.rashiName, kundali.lagna.formattedDegree)

        drawChartToPdfCanvas(
            canvas = canvas,
            left = chartX,
            top = y,
            size = chartSize,
            spec = spec,
            chartStyle = chartStyle,
            lagnaBadge = lagnaBadge,
            ascBadge = ascBadge,
            centerTitle = "D1",
            centerSubtitle = subtitle
        )
        y += chartSize + 20f

        y = drawSectionHeading(canvas, lCtx.getString(R.string.pdf_panchang_title), MARGIN_X, y)
        val p = kundali.panchang
        val rows = listOf(
            lCtx.getString(R.string.panchang_tithi) to "${p.tithi} (${p.paksha})",
            lCtx.getString(R.string.panchang_vara) to p.vara,
            lCtx.getString(R.string.panchang_nakshatra) to p.nakshatra,
            lCtx.getString(R.string.panchang_yoga) to p.yoga,
            lCtx.getString(R.string.panchang_karana) to p.karana,
            lCtx.getString(R.string.panchang_sun_times) to "${p.sunrise} / ${p.sunset}",
            lCtx.getString(R.string.pdf_lagna_row) to "${kundali.lagna.rashiName} (${kundali.lagna.formattedDegree}) • ${kundali.lagna.nakshatraName} (${kundali.lagna.pada})"
        )
        y = drawKeyValueTable(canvas, rows, MARGIN_X, y, CONTENT_WIDTH)

        y += 14f
        y = drawSectionHeading(canvas, lCtx.getString(R.string.reading_personality), MARGIN_X, y)
        val bodyPaint = TextPaint().apply {
            isAntiAlias = true
            color = COLOR_TEXT
            textSize = 10f
        }
        drawWrappedText(canvas, kundali.personalityReading, MARGIN_X, y, CONTENT_WIDTH, bodyPaint)
    }

    private fun drawCategoryPageOne(
        canvas: Canvas,
        lCtx: Context,
        person: PersonEntity,
        dive: CategoryDeepDive,
        isHi: Boolean,
        pageNum: Int
    ) {
        drawPageHeaderFooter(
            canvas,
            lCtx,
            "${dive.title} — ${if (isHi) "हस्ताक्षर प्रवृत्ति एवं उपयुक्त मार्ग" else "Signature Profile & Aligned Paths"}",
            person.name,
            pageNum
        )

        var y = 78f

        // Cosmic Signature Hero Box
        val paint = Paint().apply { isAntiAlias = true; color = COLOR_INDIGO }
        val basisPaint = TextPaint().apply {
            isAntiAlias = true
            color = Color.rgb(0xFF, 0xF3, 0xDB)
            textSize = 9.5f
        }
        val basisHeight = measureTextHeight(dive.archetype.derivationBasis, CONTENT_WIDTH - 28f, basisPaint)
        val heroHeight = 64f + basisHeight
        canvas.drawRoundRect(MARGIN_X, y, MARGIN_X + CONTENT_WIDTH, y + heroHeight, 12f, 12f, paint)

        val giftLabelPaint = TextPaint().apply {
            isAntiAlias = true
            color = COLOR_SAFFRON
            textSize = 9.5f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }
        val heroMeta = computeAstroMeterMeta(dive.overallScore)
        val heroStatus = "${lCtx.getString(heroMeta.statusResId)} • ${"★".repeat(heroMeta.starCount)}"
        canvas.drawText(
            "${lCtx.getString(R.string.cat_your_gift_label).uppercase(Locale.US)}  •  $heroStatus",
            MARGIN_X + 14f,
            y + 20f,
            giftLabelPaint
        )

        val giftTitlePaint = TextPaint().apply {
            isAntiAlias = true
            color = Color.WHITE
            textSize = 16f
            typeface = Typeface.create(Typeface.SERIF, Typeface.BOLD)
        }
        canvas.drawText(dive.signatureTitle, MARGIN_X + 14f, y + 43f, giftTitlePaint)
        drawWrappedText(canvas, dive.archetype.derivationBasis, MARGIN_X + 14f, y + 51f, CONTENT_WIDTH - 28f, basisPaint)
        y += heroHeight + 12f

        val bodyText = TextPaint().apply { isAntiAlias = true; color = COLOR_TEXT; textSize = 9.2f }
        val pathTitlePaint = TextPaint().apply {
            isAntiAlias = true
            color = COLOR_INDIGO
            textSize = 9.8f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }
        val sectorPaint = TextPaint().apply {
            isAntiAlias = true
            color = COLOR_SAFFRON_DARK
            textSize = 8.5f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }

        if (dive.category == ReadingCategory.CAREER &&
            (dive.employmentPaths.isNotEmpty() || dive.businessPaths.isNotEmpty())
        ) {
            y = drawSectionHeading(canvas, lCtx.getString(R.string.cat_career_track_title), MARGIN_X, y)
            if (dive.employmentVsBusinessGuidance.isNotBlank()) {
                val gh = measureTextHeight(dive.employmentVsBusinessGuidance, CONTENT_WIDTH - 20f, bodyText)
                drawCardBox(canvas, MARGIN_X, y, CONTENT_WIDTH, gh + 12f)
                drawWrappedText(canvas, dive.employmentVsBusinessGuidance, MARGIN_X + 10f, y + 6f, CONTENT_WIDTH - 20f, bodyText)
                y += gh + 18f
            }

            val trackPairs = listOf(
                lCtx.getString(R.string.cat_employment_tab) to dive.employmentPaths.take(3),
                lCtx.getString(R.string.cat_business_tab) to dive.businessPaths.take(3)
            )
            trackPairs.forEach { (trackLabel, paths) ->
                if (paths.isNotEmpty() && y < PAGE_HEIGHT - 190f) {
                    canvas.drawText(trackLabel, MARGIN_X, y + 10f, pathTitlePaint)
                    y += 14f
                    paths.forEach { path ->
                        val rh = measureTextHeight(path.astroReason, CONTENT_WIDTH - 20f, bodyText)
                        val bh = rh + 24f
                        if (y + bh < PAGE_HEIGHT - 170f) {
                            drawCardBox(canvas, MARGIN_X, y, CONTENT_WIDTH, bh)
                            canvas.drawText(path.title, MARGIN_X + 10f, y + 13f, pathTitlePaint)
                            val sw = sectorPaint.measureText(path.sector)
                            canvas.drawText(path.sector, MARGIN_X + CONTENT_WIDTH - 10f - sw, y + 13f, sectorPaint)
                            drawWrappedText(canvas, path.astroReason, MARGIN_X + 10f, y + 18f, CONTENT_WIDTH - 20f, bodyText)
                            y += bh + 5f
                        }
                    }
                    y += 4f
                }
            }
        } else if (dive.diversePaths.isNotEmpty()) {
            y = drawSectionHeading(canvas, lCtx.getString(R.string.cat_diverse_paths_title), MARGIN_X, y)
            dive.diversePaths.take(5).forEach { path ->
                val rh = measureTextHeight(path.astroReason, CONTENT_WIDTH - 20f, bodyText)
                val bh = rh + 26f
                if (y + bh < PAGE_HEIGHT - 170f) {
                    drawCardBox(canvas, MARGIN_X, y, CONTENT_WIDTH, bh)
                    canvas.drawText(path.title, MARGIN_X + 10f, y + 14f, pathTitlePaint)
                    val sw = sectorPaint.measureText(path.sector)
                    canvas.drawText(path.sector, MARGIN_X + CONTENT_WIDTH - 10f - sw, y + 14f, sectorPaint)
                    drawWrappedText(canvas, path.astroReason, MARGIN_X + 10f, y + 20f, CONTENT_WIDTH - 20f, bodyText)
                    y += bh + 6f
                }
            }
        }

        // Recommended & Cautious Zones
        if (y < PAGE_HEIGHT - 120f) {
            y += 6f
            y = drawSectionHeading(canvas, lCtx.getString(R.string.cat_recommended_paths), MARGIN_X, y)
            dive.archetype.recommendedPaths.take(4).forEach { path ->
                if (y < PAGE_HEIGHT - 85f) {
                    drawBulletItem(canvas, "✓", path, COLOR_GREEN, bodyText, MARGIN_X + 4f, y, CONTENT_WIDTH - 8f)
                    y += measureTextHeight(path, CONTENT_WIDTH - 26f, bodyText) + 4f
                }
            }
        }

        if (y < PAGE_HEIGHT - 85f) {
            y += 6f
            y = drawSectionHeading(canvas, lCtx.getString(R.string.cat_cautious_zones), MARGIN_X, y)
            dive.archetype.cautiousZones.take(4).forEach { zone ->
                if (y < PAGE_HEIGHT - 50f) {
                    drawBulletItem(canvas, "•", zone, COLOR_SAFFRON_DARK, bodyText, MARGIN_X + 4f, y, CONTENT_WIDTH - 8f)
                    y += measureTextHeight(zone, CONTENT_WIDTH - 26f, bodyText) + 4f
                }
            }
        }
    }

    private fun drawCategoryPageTwo(
        canvas: Canvas,
        lCtx: Context,
        person: PersonEntity,
        dive: CategoryDeepDive,
        isHi: Boolean,
        pageNum: Int
    ) {
        drawPageHeaderFooter(
            canvas,
            lCtx,
            "${dive.title} — ${if (isHi) "शक्ति मानक एवं वैदिक विवेचना" else "Power Bars, Narrative & Superpower"}",
            person.name,
            pageNum
        )

        var y = 78f
        y = drawSectionHeading(canvas, lCtx.getString(R.string.cat_power_bars_title), MARGIN_X, y)
        dive.powerBars.forEach { metric ->
            y = drawProgressBarRow(
                canvas = canvas,
                lCtx = lCtx,
                label = "${metric.glyph}  ${metric.label}",
                score = metric.score,
                summary = metric.note,
                x = MARGIN_X,
                y = y,
                width = CONTENT_WIDTH
            )
            y += 5f
        }

        y += 6f
        y = drawSectionHeading(canvas, lCtx.getString(R.string.cat_narrative_title), MARGIN_X, y)
        val bodyPaint = TextPaint().apply {
            isAntiAlias = true
            color = COLOR_TEXT
            textSize = 9.2f
        }
        val combinedNarrative = listOf(
            dive.personalityParagraph,
            dive.strengthsParagraph,
            dive.challengesParagraph
        ).filter { it.isNotBlank() }.joinToString("\n\n").ifBlank { dive.narrative }

        val narrativeH = drawWrappedText(canvas, combinedNarrative, MARGIN_X, y, CONTENT_WIDTH, bodyPaint)
        y += narrativeH + 10f

        if (dive.superpower.title.isNotBlank() && y < PAGE_HEIGHT - 125f) {
            val spLine = "⚡ ${lCtx.getString(R.string.cat_superpower_title)}: ${dive.superpower.title} — ${dive.superpower.description}"
            val spH = measureTextHeight(spLine, CONTENT_WIDTH - 20f, bodyPaint)
            if (y + spH + 14f < PAGE_HEIGHT - 52f) {
                drawCardBox(canvas, MARGIN_X, y, CONTENT_WIDTH, spH + 14f)
                drawWrappedText(canvas, spLine, MARGIN_X + 10f, y + 7f, CONTENT_WIDTH - 20f, bodyPaint)
                y += spH + 20f
            }
        }
        if (dive.growthArea.title.isNotBlank() && y < PAGE_HEIGHT - 90f) {
            val gaLine = "✦ ${lCtx.getString(R.string.cat_growth_area_title)}: ${dive.growthArea.title} — ${dive.growthArea.guidance}"
            val gaH = measureTextHeight(gaLine, CONTENT_WIDTH - 20f, bodyPaint)
            if (y + gaH + 14f < PAGE_HEIGHT - 50f) {
                drawCardBox(canvas, MARGIN_X, y, CONTENT_WIDTH, gaH + 14f)
                drawWrappedText(canvas, gaLine, MARGIN_X + 10f, y + 7f, CONTENT_WIDTH - 20f, bodyPaint)
            }
        }
    }

    private fun drawCategoryPageThree(
        canvas: Canvas,
        lCtx: Context,
        person: PersonEntity,
        dive: CategoryDeepDive,
        isHi: Boolean,
        pageNum: Int
    ) {
        drawPageHeaderFooter(
            canvas,
            lCtx,
            "${dive.title} — ${if (isHi) "जीवन प्रश्न, समय एवं अंतर्संबंध" else "Life Questions, Timing & Cross-Chart Links"}",
            person.name,
            pageNum
        )

        var y = 78f
        val bodyPaint = TextPaint().apply {
            isAntiAlias = true
            color = COLOR_TEXT
            textSize = 9.2f
        }
        val titlePaint = TextPaint().apply {
            isAntiAlias = true
            color = COLOR_INDIGO
            textSize = 10f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }
        val metaPaint = TextPaint().apply {
            isAntiAlias = true
            color = COLOR_SAFFRON_DARK
            textSize = 8.5f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }

        if (dive.absorbedReadings.isNotEmpty()) {
            y = drawSectionHeading(canvas, lCtx.getString(R.string.cat_absorbed_topics_title), MARGIN_X, y)
            dive.absorbedReadings.forEach { topic ->
                val titleH = measureTextHeight(topic.title, CONTENT_WIDTH - 140f, titlePaint)
                val bodyH = measureTextHeight(topic.body, CONTENT_WIDTH - 20f, bodyPaint)
                val boxH = maxOf(titleH, 14f) + bodyH + 26f
                if (y + boxH < PAGE_HEIGHT - 52f) {
                    drawCardBox(canvas, MARGIN_X, y, CONTENT_WIDTH, boxH)
                    drawWrappedText(canvas, topic.title, MARGIN_X + 10f, y + 7f, CONTENT_WIDTH - 140f, titlePaint)
                    if (topic.timingWindow.isNotBlank()) {
                        val badgeStr = lCtx.getString(R.string.cat_timing_window_badge, topic.timingWindow)
                        val bw = metaPaint.measureText(badgeStr)
                        canvas.drawText(badgeStr, MARGIN_X + CONTENT_WIDTH - 10f - bw, y + 17f, metaPaint)
                    }
                    val bodyStartY = y + maxOf(titleH, 14f) + 10f
                    drawWrappedText(canvas, topic.body, MARGIN_X + 10f, bodyStartY, CONTENT_WIDTH - 20f, bodyPaint)
                    if (topic.astroBasis.isNotBlank()) {
                        canvas.drawText(topic.astroBasis, MARGIN_X + 10f, bodyStartY + bodyH + 10f, metaPaint)
                    }
                    y += boxH + 7f
                }
            }
        }

        if (dive.crossReferences.isNotEmpty() && y < PAGE_HEIGHT - 120f) {
            y += 6f
            y = drawSectionHeading(canvas, lCtx.getString(R.string.cat_cross_reference_title), MARGIN_X, y)
            dive.crossReferences.forEach { ref ->
                val insightH = measureTextHeight(ref.insight, CONTENT_WIDTH - 20f, bodyPaint)
                val boxH = insightH + 26f
                if (y + boxH < PAGE_HEIGHT - 50f) {
                    drawCardBox(canvas, MARGIN_X, y, CONTENT_WIDTH, boxH)
                    canvas.drawText("${ref.targetLabel} • ${ref.title}", MARGIN_X + 10f, y + 14f, titlePaint)
                    drawWrappedText(canvas, ref.insight, MARGIN_X + 10f, y + 20f, CONTENT_WIDTH - 20f, bodyPaint)
                    y += boxH + 6f
                }
            }
        }
    }

    private fun drawCategoryPageFour(
        canvas: Canvas,
        lCtx: Context,
        person: PersonEntity,
        kundali: KundaliData,
        dive: CategoryDeepDive,
        chartStyle: ChartStyle,
        isHi: Boolean,
        pageNum: Int
    ) {
        drawPageHeaderFooter(
            canvas,
            lCtx,
            "${dive.title} — ${if (isHi) "ग्रह प्रमाण, मार्गदर्शन एवं उपाय" else "Astrological Proof, Guidance & Remedies"}",
            person.name,
            pageNum
        )

        var y = 78f
        val bodyPaint = TextPaint().apply {
            isAntiAlias = true
            color = COLOR_TEXT
            textSize = 9.2f
        }

        y = drawSectionHeading(canvas, lCtx.getString(R.string.cat_astro_proof_title), MARGIN_X, y)
        val proofRows = dive.proofItems.map { "${it.title}: ${it.placementDetail}" to it.statusLabel }
        y = drawKeyValueTable(canvas, proofRows, MARGIN_X, y, CONTENT_WIDTH)

        if (dive.yogaNotes.isNotEmpty() && y < PAGE_HEIGHT - 330f) {
            y += 8f
            y = drawSectionHeading(canvas, lCtx.getString(R.string.cat_yogas_proof_title), MARGIN_X, y)
            dive.yogaNotes.forEach { yd ->
                val line = "${yd.name} (${yd.statusText}): ${yd.description}"
                val h = drawWrappedText(canvas, line, MARGIN_X + 4f, y, CONTENT_WIDTH - 8f, bodyPaint)
                y += h + 5f
            }
        }

        y += 8f
        if (y < PAGE_HEIGHT - 290f) {
            y = drawSectionHeading(
                canvas,
                lCtx.getString(R.string.cat_divisional_chart_title, dive.divisionalChartCode),
                MARGIN_X,
                y
            )
            val chartSize = 148f
            val spec = kundali.toChartSpec(dive.divisionalChartCode)
            drawChartToPdfCanvas(
                canvas = canvas,
                left = (PAGE_WIDTH - chartSize) / 2f,
                top = y,
                size = chartSize,
                spec = spec,
                chartStyle = chartStyle,
                lagnaBadge = lCtx.getString(R.string.chart_lagna_short),
                ascBadge = lCtx.getString(R.string.chart_asc_short),
                centerTitle = dive.divisionalChartCode,
                centerSubtitle = dive.title
            )
            y += chartSize + 12f
        }

        if (y < PAGE_HEIGHT - 165f) {
            y = drawSectionHeading(canvas, lCtx.getString(R.string.cat_what_to_do), MARGIN_X, y)
            dive.whatToDo.take(3).forEach { tip ->
                if (y < PAGE_HEIGHT - 125f) {
                    drawBulletItem(canvas, "✓", tip, COLOR_GREEN, bodyPaint, MARGIN_X + 4f, y, CONTENT_WIDTH - 8f)
                    y += measureTextHeight(tip, CONTENT_WIDTH - 26f, bodyPaint) + 4f
                }
            }
        }

        if (y < PAGE_HEIGHT - 120f) {
            y += 4f
            y = drawSectionHeading(canvas, lCtx.getString(R.string.cat_safety_advice), MARGIN_X, y)
            dive.safetyAdvice.take(3).forEach { tip ->
                if (y < PAGE_HEIGHT - 95f) {
                    drawBulletItem(canvas, "•", tip, COLOR_SAFFRON_DARK, bodyPaint, MARGIN_X + 4f, y, CONTENT_WIDTH - 8f)
                    y += measureTextHeight(tip, CONTENT_WIDTH - 26f, bodyPaint) + 4f
                }
            }
        }

        if (y < PAGE_HEIGHT - 95f) {
            y += 6f
            y = drawSectionHeading(canvas, lCtx.getString(R.string.cat_remedies_title), MARGIN_X, y)
            val remRows = listOf(
                lCtx.getString(R.string.cat_remedy_mantra) to dive.remedy.mantra,
                lCtx.getString(R.string.cat_remedy_charity) to dive.remedy.charity,
                lCtx.getString(R.string.cat_remedy_ritual) to dive.remedy.ritual
            )
            drawKeyValueTable(canvas, remRows, MARGIN_X, y, CONTENT_WIDTH)
        }
    }

    private fun drawPlanetaryTablePage(
        canvas: Canvas,
        lCtx: Context,
        person: PersonEntity,
        kundali: KundaliData,
        isHi: Boolean,
        pageNum: Int
    ) {
        drawPageHeaderFooter(
            canvas,
            lCtx,
            if (isHi) "संपूर्ण नवग्रह एवं भाव तालिका" else "Planetary Ephemeris & Placements",
            person.name,
            pageNum
        )

        var y = 84f
        y = drawSectionHeading(canvas, lCtx.getString(R.string.pdf_planets_title), MARGIN_X, y)

        val paint = Paint().apply { isAntiAlias = true; color = COLOR_TABLE_HEAD }
        canvas.drawRect(MARGIN_X, y, MARGIN_X + CONTENT_WIDTH, y + 24f, paint)

        val headPaint = TextPaint().apply {
            isAntiAlias = true
            color = COLOR_INDIGO
            textSize = 9.5f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }
        canvas.drawText(lCtx.getString(R.string.pdf_col_planet), MARGIN_X + 8f, y + 16f, headPaint)
        canvas.drawText(lCtx.getString(R.string.pdf_col_sign), MARGIN_X + 108f, y + 16f, headPaint)
        canvas.drawText(lCtx.getString(R.string.pdf_col_degree), MARGIN_X + 205f, y + 16f, headPaint)
        canvas.drawText(lCtx.getString(R.string.pdf_col_retro), MARGIN_X + 280f, y + 16f, headPaint)
        canvas.drawText(lCtx.getString(R.string.pdf_col_nakshatra), MARGIN_X + 332f, y + 16f, headPaint)
        canvas.drawText(lCtx.getString(R.string.pdf_col_house), MARGIN_X + 472f, y + 16f, headPaint)
        y += 26f

        val rowPaint = TextPaint().apply { isAntiAlias = true; color = COLOR_TEXT; textSize = 9.5f }
        // Lagna row
        canvas.drawText(lCtx.getString(R.string.pdf_lagna_row), MARGIN_X + 8f, y + 16f, rowPaint)
        canvas.drawText(kundali.lagna.rashiName, MARGIN_X + 108f, y + 16f, rowPaint)
        canvas.drawText(kundali.lagna.formattedDegree, MARGIN_X + 205f, y + 16f, rowPaint)
        canvas.drawText("-", MARGIN_X + 286f, y + 16f, rowPaint)
        canvas.drawText("${kundali.lagna.nakshatraName} (${kundali.lagna.pada})", MARGIN_X + 332f, y + 16f, rowPaint)
        canvas.drawText("1", MARGIN_X + 482f, y + 16f, rowPaint)
        y += 24f

        kundali.planets.forEachIndexed { idx, pl ->
            if (idx % 2 == 0) {
                paint.color = COLOR_ROW_ALT
                canvas.drawRect(MARGIN_X, y, MARGIN_X + CONTENT_WIDTH, y + 24f, paint)
            }
            canvas.drawText("${pl.name} (${pl.glyph})", MARGIN_X + 8f, y + 16f, rowPaint)
            canvas.drawText(pl.rashiName, MARGIN_X + 108f, y + 16f, rowPaint)
            canvas.drawText(pl.formattedDegree, MARGIN_X + 205f, y + 16f, rowPaint)
            canvas.drawText(if (pl.isRetrograde) "[R]" else "-", MARGIN_X + 284f, y + 16f, rowPaint)
            canvas.drawText("${pl.nakshatraName} (${pl.pada})", MARGIN_X + 332f, y + 16f, rowPaint)
            canvas.drawText("${pl.houseNumber}", MARGIN_X + 482f, y + 16f, rowPaint)
            y += 24f
        }

        y += 24f
        y = drawSectionHeading(
            canvas,
            if (isHi) "दशा एवं राशि सार" else "Current Dasha & Planetary Snapshot",
            MARGIN_X,
            y
        )
        val notePaint = TextPaint().apply { isAntiAlias = true; color = COLOR_TEXT; textSize = 10f }
        drawWrappedText(
            canvas,
            "${lCtx.getString(R.string.reading_active_dasha, kundali.currentDashaSummary)}\n${lCtx.getString(R.string.reading_dasha_note)}",
            MARGIN_X,
            y,
            CONTENT_WIDTH,
            notePaint
        )
    }

    private fun drawBhavaCuspsPage(
        canvas: Canvas,
        lCtx: Context,
        person: PersonEntity,
        kundali: KundaliData,
        isHi: Boolean,
        pageNum: Int
    ) {
        drawPageHeaderFooter(
            canvas,
            lCtx,
            lCtx.getString(R.string.detail_bhava_title),
            person.name,
            pageNum
        )

        var y = 84f
        y = drawSectionHeading(canvas, lCtx.getString(R.string.detail_bhava_title), MARGIN_X, y)

        val houseThemes = if (isHi) {
            listOf(
                "स्वभाव, व्यक्तित्व एवं प्राण शक्ति",
                "धन, वाणी एवं कुटुंब",
                "पराक्रम, भाई-बहन एवं कौशल",
                "सुख, माता एवं गृह-वाहन",
                "बुद्धि, शिक्षा एवं संतान",
                "सेवा, स्वास्थ्य अनुशासन एवं प्रतियोगिता",
                "विवाह, साझेदारी एवं संबंध",
                "रूपांतरण, शोध एवं दीर्घायु",
                "भाग्य, धर्म, गुरु एवं उच्च शिक्षा",
                "कर्म, करियर, पद एवं प्रतिष्ठा",
                "लाभ, आय एवं आकांक्षाएं",
                "विदेश, आध्यात्म एवं निवेश"
            )
        } else {
            listOf(
                "Self, Vitality, Appearance & Purpose",
                "Wealth, Family, Speech & Values",
                "Courage, Siblings, Communication & Effort",
                "Home, Mother, Property & Inner Peace",
                "Intelligence, Education, Creativity & Children",
                "Service, Wellness Routines & Resilience",
                "Marriage, Partnerships & Public Collaborations",
                "Transformation, Research & Longevity",
                "Fortune, Wisdom, Dharma & Higher Travel",
                "Career, Authority, Public Standing & Karma",
                "Gains, Income, Networks & Aspirations",
                "Global Horizons, Retreats & Liberation"
            )
        }

        val rows = kundali.houses.mapIndexed { idx, h ->
            val houseTitle = "${lCtx.getString(R.string.house_label, h.houseNumber)} — ${h.rashiName} (${h.formattedDegree})"
            val occupants = kundali.planets.filter { it.houseNumber == h.houseNumber }.joinToString(", ") { it.name }
            val occLabel = if (occupants.isNotEmpty()) " | $occupants" else ""
            houseTitle to "${houseThemes.getOrElse(idx) { "" }}$occLabel"
        }
        drawKeyValueTable(canvas, rows, MARGIN_X, y, CONTENT_WIDTH)
    }

    private fun drawDashaTimelinePage(
        canvas: Canvas,
        lCtx: Context,
        person: PersonEntity,
        kundali: KundaliData,
        fromIdx: Int,
        toIdx: Int,
        isHi: Boolean,
        pageNum: Int
    ) {
        drawPageHeaderFooter(
            canvas,
            lCtx,
            "${lCtx.getString(R.string.detail_dasha_title)} (${fromIdx + 1}–$toIdx)",
            person.name,
            pageNum
        )

        var y = 80f
        val mahaTitlePaint = TextPaint().apply {
            isAntiAlias = true
            color = COLOR_INDIGO
            textSize = 11f
            typeface = Typeface.create(Typeface.SERIF, Typeface.BOLD)
        }
        val datePaint = TextPaint().apply {
            isAntiAlias = true
            color = COLOR_SAFFRON_DARK
            textSize = 9.5f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }
        val antarPaint = TextPaint().apply {
            isAntiAlias = true
            color = COLOR_TEXT
            textSize = 8.5f
        }

        val slice = kundali.vimshottariDasha.subList(
            fromIdx.coerceAtLeast(0),
            toIdx.coerceAtMost(kundali.vimshottariDasha.size)
        )

        slice.forEach { maha ->
            val subCount = maha.subPeriods.size
            val rowsNeeded = (subCount + 2) / 3
            val cardH = 34f + (rowsNeeded * 16f) + 10f
            if (y + cardH < PAGE_HEIGHT - 48f) {
                drawCardBox(canvas, MARGIN_X, y, CONTENT_WIDTH, cardH)
                canvas.drawText(
                    lCtx.getString(R.string.maha_dasha_suffix, maha.planet),
                    MARGIN_X + 12f,
                    y + 18f,
                    mahaTitlePaint
                )
                val rangeStr = "${maha.startDate} – ${maha.endDate}"
                val rw = datePaint.measureText(rangeStr)
                canvas.drawText(rangeStr, MARGIN_X + CONTENT_WIDTH - 12f - rw, y + 18f, datePaint)

                val colW = (CONTENT_WIDTH - 24f) / 3f
                maha.subPeriods.forEachIndexed { idx, antar ->
                    val col = idx % 3
                    val row = idx / 3
                    val ax = MARGIN_X + 12f + col * colW
                    val ay = y + 36f + row * 16f
                    canvas.drawText(
                        "${maha.planet.take(3)}/${antar.planet.take(3)}: ${antar.startDate}–${antar.endDate.take(4)}",
                        ax,
                        ay,
                        antarPaint
                    )
                }
                y += cardH + 10f
            }
        }
    }

    private fun drawYogasAndDoshasPage(
        canvas: Canvas,
        lCtx: Context,
        person: PersonEntity,
        kundali: KundaliData,
        isHi: Boolean,
        pageNum: Int
    ) {
        drawPageHeaderFooter(
            canvas,
            lCtx,
            lCtx.getString(R.string.detail_yogas_doshas_title),
            person.name,
            pageNum
        )

        var y = 84f
        val titlePaint = TextPaint().apply {
            isAntiAlias = true
            color = COLOR_INDIGO
            textSize = 11.5f
            typeface = Typeface.create(Typeface.SERIF, Typeface.BOLD)
        }
        val statusPaint = TextPaint().apply {
            isAntiAlias = true
            textSize = 9.5f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }
        val bodyPaint = TextPaint().apply {
            isAntiAlias = true
            color = COLOR_TEXT
            textSize = 9.5f
        }
        val remPaint = TextPaint().apply {
            isAntiAlias = true
            color = COLOR_INDIGO
            textSize = 9f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }

        kundali.yogasAndDoshas.forEach { yd ->
            val descH = measureTextHeight(yd.description, CONTENT_WIDTH - 24f, bodyPaint)
            val remLine = lCtx.getString(R.string.remedy_prefix, yd.remedy)
            val remH = measureTextHeight(remLine, CONTENT_WIDTH - 24f, remPaint)
            val cardH = 32f + descH + remH + 18f

            if (y + cardH < PAGE_HEIGHT - 50f) {
                drawCardBox(canvas, MARGIN_X, y, CONTENT_WIDTH, cardH)
                canvas.drawText(yd.name, MARGIN_X + 12f, y + 20f, titlePaint)
                statusPaint.color = if (yd.isPresent) COLOR_SAFFRON_DARK else COLOR_GREEN
                val sw = statusPaint.measureText(yd.statusText)
                canvas.drawText(yd.statusText, MARGIN_X + CONTENT_WIDTH - 12f - sw, y + 20f, statusPaint)

                drawWrappedText(canvas, yd.description, MARGIN_X + 12f, y + 28f, CONTENT_WIDTH - 24f, bodyPaint)
                drawWrappedText(canvas, remLine, MARGIN_X + 12f, y + 34f + descH, CONTENT_WIDTH - 24f, remPaint)
                y += cardH + 10f
            }
        }
    }

    private fun drawDivisionalChartsPairPage(
        canvas: Canvas,
        lCtx: Context,
        person: PersonEntity,
        kundali: KundaliData,
        chartStyle: ChartStyle,
        isHi: Boolean,
        pageNum: Int
    ) {
        drawPageHeaderFooter(
            canvas,
            lCtx,
            if (isHi) "वर्ग कुंडलियां: नवमांश (D9) एवं दशमांश (D10)" else "Divisional Charts: Navamsha (D9) & Dashamsha (D10)",
            person.name,
            pageNum
        )

        var y = 80f
        val charts = listOf("D9", "D10").mapNotNull { code -> kundali.divisionalCharts.find { it.code == code } }
        charts.forEach { div ->
            y = drawSingleDivisionalBlock(canvas, lCtx, kundali, div, chartStyle, MARGIN_X, y)
            y += 18f
        }
    }

    private fun drawD7AndMatrixPage(
        canvas: Canvas,
        lCtx: Context,
        person: PersonEntity,
        kundali: KundaliData,
        chartStyle: ChartStyle,
        isHi: Boolean,
        pageNum: Int
    ) {
        drawPageHeaderFooter(
            canvas,
            lCtx,
            if (isHi) "सप्तमांश (D7) एवं तुलनात्मक ग्रह सारिणी" else "Saptamsha (D7) & Divisional Sign Matrix",
            person.name,
            pageNum
        )

        var y = 80f
        kundali.divisionalCharts.find { it.code == "D7" }?.let { d7 ->
            y = drawSingleDivisionalBlock(canvas, lCtx, kundali, d7, chartStyle, MARGIN_X, y)
            y += 22f
        }

        y = drawSectionHeading(
            canvas,
            if (isHi) "D1 / D9 / D10 / D7 राशि तुलना" else "Planetary Sign Matrix (D1 / D9 / D10 / D7)",
            MARGIN_X,
            y
        )

        val paint = Paint().apply { isAntiAlias = true; color = COLOR_TABLE_HEAD }
        canvas.drawRect(MARGIN_X, y, MARGIN_X + CONTENT_WIDTH, y + 22f, paint)
        val headPaint = TextPaint().apply {
            isAntiAlias = true
            color = COLOR_INDIGO
            textSize = 9.5f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }
        canvas.drawText(lCtx.getString(R.string.pdf_col_planet), MARGIN_X + 8f, y + 15f, headPaint)
        canvas.drawText("D1 (Lagna)", MARGIN_X + 115f, y + 15f, headPaint)
        canvas.drawText("D9 (Navamsha)", MARGIN_X + 220f, y + 15f, headPaint)
        canvas.drawText("D10 (Career)", MARGIN_X + 335f, y + 15f, headPaint)
        canvas.drawText("D7 (Saptamsha)", MARGIN_X + 435f, y + 15f, headPaint)
        y += 22f

        val d9Map = kundali.divisionalCharts.find { it.code == "D9" }?.placements?.associate { it.canonicalPlanet to it.signName } ?: emptyMap()
        val d10Map = kundali.divisionalCharts.find { it.code == "D10" }?.placements?.associate { it.canonicalPlanet to it.signName } ?: emptyMap()
        val d7Map = kundali.divisionalCharts.find { it.code == "D7" }?.placements?.associate { it.canonicalPlanet to it.signName } ?: emptyMap()

        val rowPaint = TextPaint().apply { isAntiAlias = true; color = COLOR_TEXT; textSize = 9f }

        // Lagna row + 9 planets
        val matrixRows = listOf(
            listOf(lCtx.getString(R.string.pdf_lagna_row), kundali.lagna.rashiName, d9Map["Lagna"] ?: "-", d10Map["Lagna"] ?: "-", d7Map["Lagna"] ?: "-")
        ) + kundali.planets.map { pl ->
            listOf(pl.name, pl.rashiName, d9Map[pl.canonicalName] ?: "-", d10Map[pl.canonicalName] ?: "-", d7Map[pl.canonicalName] ?: "-")
        }

        matrixRows.forEachIndexed { idx, cols ->
            if (idx % 2 == 1) {
                paint.color = COLOR_ROW_ALT
                canvas.drawRect(MARGIN_X, y, MARGIN_X + CONTENT_WIDTH, y + 20f, paint)
            }
            canvas.drawText(cols[0], MARGIN_X + 8f, y + 14f, rowPaint)
            canvas.drawText(cols[1], MARGIN_X + 115f, y + 14f, rowPaint)
            canvas.drawText(cols[2], MARGIN_X + 220f, y + 14f, rowPaint)
            canvas.drawText(cols[3], MARGIN_X + 335f, y + 14f, rowPaint)
            canvas.drawText(cols[4], MARGIN_X + 435f, y + 14f, rowPaint)
            y += 20f
        }
    }

    private fun drawSingleDivisionalBlock(
        canvas: Canvas,
        lCtx: Context,
        kundali: KundaliData,
        div: DivisionalChart,
        chartStyle: ChartStyle,
        x: Float,
        startY: Float
    ): Float {
        var y = drawSectionHeading(canvas, div.title, x, startY)
        val descPaint = TextPaint().apply { isAntiAlias = true; color = COLOR_MUTED; textSize = 9.5f }
        val dh = drawWrappedText(canvas, div.description, x, y, CONTENT_WIDTH, descPaint)
        y += dh + 8f

        val chartSize = 215f
        val spec = kundali.toChartSpec(div.code)
        drawChartToPdfCanvas(
            canvas = canvas,
            left = x,
            top = y,
            size = chartSize,
            spec = spec,
            chartStyle = chartStyle,
            lagnaBadge = lCtx.getString(R.string.chart_lagna_short),
            ascBadge = lCtx.getString(R.string.chart_asc_short),
            centerTitle = div.code,
            centerSubtitle = ""
        )

        // Placement table to the right of the chart
        val tableX = x + chartSize + 16f
        val tableW = CONTENT_WIDTH - chartSize - 16f
        val placePaint = TextPaint().apply { isAntiAlias = true; color = COLOR_TEXT; textSize = 9.5f }
        var py = y + 4f
        div.placements.forEachIndexed { idx, p ->
            if (idx % 2 == 0) {
                val bg = Paint().apply { color = COLOR_ROW_ALT }
                canvas.drawRect(tableX, py, tableX + tableW, py + 19f, bg)
            }
            canvas.drawText(p.planet, tableX + 6f, py + 13f, placePaint)
            canvas.drawText(p.signName, tableX + (tableW * 0.48f), py + 13f, placePaint)
            py += 19.5f
        }

        return maxOf(y + chartSize, py)
    }

    private fun drawMasterRemediesPage(
        canvas: Canvas,
        lCtx: Context,
        person: PersonEntity,
        kundali: KundaliData,
        isHi: Boolean,
        pageNum: Int
    ) {
        drawPageHeaderFooter(
            canvas,
            lCtx,
            if (isHi) "समेकित उपाय, मंत्र, रत्न एवं साप्ताहिक साधना" else "Master Consolidated Remedies & Practice",
            person.name,
            pageNum
        )

        var y = 82f
        val l = kundali.luckyElements
        y = drawSectionHeading(canvas, lCtx.getString(R.string.reading_lucky_elements), MARGIN_X, y)
        y = drawKeyValueTable(
            canvas,
            listOf(
                lCtx.getString(R.string.lucky_number) to "${l.number}",
                lCtx.getString(R.string.lucky_color) to l.color,
                lCtx.getString(R.string.lucky_day) to l.day,
                lCtx.getString(R.string.lucky_direction) to l.direction,
                lCtx.getString(R.string.lucky_gemstone) to l.gemstone
            ),
            MARGIN_X,
            y,
            CONTENT_WIDTH
        )

        y += 14f
        val r = kundali.remedies
        y = drawSectionHeading(canvas, lCtx.getString(R.string.reading_remedies), MARGIN_X, y)
        y = drawKeyValueTable(
            canvas,
            listOf(
                lCtx.getString(R.string.remedy_mantra) to r.mantra,
                lCtx.getString(R.string.remedy_charity) to r.charity,
                lCtx.getString(R.string.remedy_fasting) to r.fasting,
                lCtx.getString(R.string.remedy_behavioral) to r.behavioral
            ),
            MARGIN_X,
            y,
            CONTENT_WIDTH
        )

        y += 16f
        y = drawSectionHeading(
            canvas,
            if (isHi) "जीवन क्षेत्रों के विशेष मंत्र एवं साप्ताहिक अभ्यास" else "Category Mantras & Weekly Discipline Summary",
            MARGIN_X,
            y
        )

        val rows = ReadingCategory.entries.mapNotNull { cat ->
            kundali.categoryDeepDives[cat]?.let { dive ->
                dive.title to "${dive.remedy.mantra} • ${dive.remedy.ritual}"
            }
        }
        y = drawKeyValueTable(canvas, rows, MARGIN_X, y, CONTENT_WIDTH)

        y += 16f
        val discPaint = TextPaint().apply { isAntiAlias = true; color = COLOR_MUTED; textSize = 9.5f }
        drawWrappedText(
            canvas,
            lCtx.getString(R.string.pdf_footer),
            MARGIN_X,
            y,
            CONTENT_WIDTH,
            discPaint
        )
    }

    // ---- Pages 32..37: 2027 Annual Edition Appendix ----

    private fun draw2027OverviewPage(
        canvas: Canvas,
        lCtx: Context,
        person: PersonEntity,
        forecast: YearForecast,
        isHi: Boolean,
        pageNum: Int
    ) {
        drawPageHeaderFooter(
            canvas,
            lCtx,
            if (isHi) "2027 वार्षिक परिशिष्ट • मुख्य विषय एवं सूचकांक" else "2027 Annual Edition • Cosmic Overview & Meters",
            person.name,
            pageNum
        )

        var y = 76f
        val bodyPaint = TextPaint().apply { isAntiAlias = true; color = COLOR_TEXT; textSize = 9.2f }
        val boldIndigo = TextPaint().apply {
            isAntiAlias = true
            color = COLOR_INDIGO
            textSize = 14f
            typeface = Typeface.create(Typeface.SERIF, Typeface.BOLD)
        }
        val badgePaint = TextPaint().apply {
            isAntiAlias = true
            color = COLOR_SAFFRON_DARK
            textSize = 9.5f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }

        val heroSummaryH = measureTextHeight(forecast.heroSummary, CONTENT_WIDTH - 24f, bodyPaint)
        val dashaText = "${lCtx.getString(R.string.year_2027_dasha_badge, "${forecast.mahaLord} (${forecast.antarLords.joinToString(", ")})")}"
        val heroCardH = 52f + heroSummaryH + 22f
        drawCardBox(canvas, MARGIN_X, y, CONTENT_WIDTH, heroCardH)
        canvas.drawText("${forecast.heroGlyph}  ${forecast.signatureHeadline}", MARGIN_X + 12f, y + 22f, boldIndigo)
        val drawnHeroH = drawWrappedText(canvas, forecast.heroSummary, MARGIN_X + 12f, y + 32f, CONTENT_WIDTH - 24f, bodyPaint)
        canvas.drawText(dashaText, MARGIN_X + 12f, y + 40f + drawnHeroH, badgePaint)
        y += heroCardH + 12f

        y = drawSectionHeading(canvas, lCtx.getString(R.string.year_2027_at_a_glance), MARGIN_X, y)
        forecast.atAGlanceMeters.forEach { m ->
            y = drawProgressBarRow(
                canvas = canvas,
                lCtx = lCtx,
                label = "${m.glyph}  ${m.label}",
                score = m.score,
                summary = m.note,
                x = MARGIN_X,
                y = y,
                width = CONTENT_WIDTH
            )
            y += 4f
        }

        y += 6f
        y = drawSectionHeading(canvas, lCtx.getString(R.string.year_2027_golden_windows), MARGIN_X, y)
        forecast.goldenWindows.forEach { win ->
            val h = measureTextHeight(win.reason, CONTENT_WIDTH - 24f, bodyPaint)
            if (y + h + 26f < PAGE_HEIGHT - 55f) {
                drawCardBox(canvas, MARGIN_X, y, CONTENT_WIDTH, h + 22f)
                canvas.drawText("★ ${win.label}", MARGIN_X + 10f, y + 14f, badgePaint)
                drawWrappedText(canvas, win.reason, MARGIN_X + 10f, y + 18f, CONTENT_WIDTH - 20f, bodyPaint)
                y += h + 26f
            }
        }

        if (y + 50f < PAGE_HEIGHT - 55f) {
            y = drawSectionHeading(canvas, lCtx.getString(R.string.year_2027_effort_windows), MARGIN_X, y)
            forecast.effortWindows.forEach { win ->
                val h = measureTextHeight(win.reason, CONTENT_WIDTH - 24f, bodyPaint)
                if (y + h + 26f < PAGE_HEIGHT - 48f) {
                    drawCardBox(canvas, MARGIN_X, y, CONTENT_WIDTH, h + 22f)
                    canvas.drawText("✦ ${win.label}", MARGIN_X + 10f, y + 14f, badgePaint)
                    drawWrappedText(canvas, win.reason, MARGIN_X + 10f, y + 18f, CONTENT_WIDTH - 20f, bodyPaint)
                    y += h + 26f
                }
            }
        }
    }

    private fun draw2027DeepDivesPage(
        canvas: Canvas,
        lCtx: Context,
        person: PersonEntity,
        forecast: YearForecast,
        fromIdx: Int,
        toIdx: Int,
        isHi: Boolean,
        pageNum: Int
    ) {
        drawPageHeaderFooter(
            canvas,
            lCtx,
            if (isHi) "2027 गहन श्रेणी विश्लेषण" else "2027 Category Deep-Dives",
            person.name,
            pageNum
        )

        var y = 76f
        val titlePaint = TextPaint().apply {
            isAntiAlias = true
            color = COLOR_INDIGO
            textSize = 11f
            typeface = Typeface.create(Typeface.SERIF, Typeface.BOLD)
        }
        val winPaint = TextPaint().apply {
            isAntiAlias = true
            color = COLOR_SAFFRON_DARK
            textSize = 8.6f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }
        val bodyPaint = TextPaint().apply { isAntiAlias = true; color = COLOR_TEXT; textSize = 8.6f }

        val slice = forecast.categoryDeepDives.subList(
            fromIdx.coerceAtLeast(0),
            toIdx.coerceAtMost(forecast.categoryDeepDives.size)
        )
        slice.forEach { dd ->
            val meta = computeAstroMeterMeta(dd.score)
            val statusLabel = lCtx.getString(meta.statusResId)
            val gLabel = dd.goldenWindows.joinToString(", ") { it.label }
            val eLabel = dd.effortWindows.joinToString(", ") { it.label }
            val combinedBody = dd.paragraphs.joinToString("\n")
            val bodyH = measureTextHeight(combinedBody, CONTENT_WIDTH - 20f, bodyPaint)
            val remText = "${lCtx.getString(R.string.year_2027_remedy_note_label)}: ${dd.remedy2027}"
            val remH = measureTextHeight(remText, CONTENT_WIDTH - 20f, bodyPaint)
            val cardH = 38f + bodyH + remH + 14f

            if (y + cardH < PAGE_HEIGHT - 44f) {
                drawCardBox(canvas, MARGIN_X, y, CONTENT_WIDTH, cardH)
                canvas.drawText("${dd.categoryLabel}  [$statusLabel]", MARGIN_X + 10f, y + 16f, titlePaint)
                canvas.drawText(
                    "${lCtx.getString(R.string.year_2027_golden_chip, gLabel)}  •  ${lCtx.getString(R.string.year_2027_effort_chip, eLabel)}  (${dd.chartCode})",
                    MARGIN_X + 10f,
                    y + 30f,
                    winPaint
                )
                val drawnBody = drawWrappedText(canvas, combinedBody, MARGIN_X + 10f, y + 36f, CONTENT_WIDTH - 20f, bodyPaint)
                drawWrappedText(canvas, remText, MARGIN_X + 10f, y + 40f + drawnBody, CONTENT_WIDTH - 20f, bodyPaint)
                y += cardH + 10f
            }
        }
    }

    private fun draw2027DeepDivesWithDashaPage(
        canvas: Canvas,
        lCtx: Context,
        person: PersonEntity,
        forecast: YearForecast,
        isHi: Boolean,
        pageNum: Int
    ) {
        drawPageHeaderFooter(
            canvas,
            lCtx,
            if (isHi) "2027 धर्म विश्लेषण एवं सक्रिय दशा चक्र" else "2027 Dharma Deep-Dive & Active Dasha Timeline",
            person.name,
            pageNum
        )

        var y = 76f
        val titlePaint = TextPaint().apply {
            isAntiAlias = true
            color = COLOR_INDIGO
            textSize = 11f
            typeface = Typeface.create(Typeface.SERIF, Typeface.BOLD)
        }
        val winPaint = TextPaint().apply {
            isAntiAlias = true
            color = COLOR_SAFFRON_DARK
            textSize = 8.6f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }
        val bodyPaint = TextPaint().apply { isAntiAlias = true; color = COLOR_TEXT; textSize = 8.6f }

        forecast.categoryDeepDives.drop(4).forEach { dd ->
            val meta = computeAstroMeterMeta(dd.score)
            val statusLabel = lCtx.getString(meta.statusResId)
            val gLabel = dd.goldenWindows.joinToString(", ") { it.label }
            val eLabel = dd.effortWindows.joinToString(", ") { it.label }
            val combinedBody = dd.paragraphs.joinToString("\n")
            val bodyH = measureTextHeight(combinedBody, CONTENT_WIDTH - 20f, bodyPaint)
            val remText = "${lCtx.getString(R.string.year_2027_remedy_note_label)}: ${dd.remedy2027}"
            val remH = measureTextHeight(remText, CONTENT_WIDTH - 20f, bodyPaint)
            val cardH = 38f + bodyH + remH + 14f

            drawCardBox(canvas, MARGIN_X, y, CONTENT_WIDTH, cardH)
            canvas.drawText("${dd.categoryLabel}  [$statusLabel]", MARGIN_X + 10f, y + 16f, titlePaint)
            canvas.drawText(
                "${lCtx.getString(R.string.year_2027_golden_chip, gLabel)}  •  ${lCtx.getString(R.string.year_2027_effort_chip, eLabel)}  (${dd.chartCode})",
                MARGIN_X + 10f,
                y + 30f,
                winPaint
            )
            val drawnBody = drawWrappedText(canvas, combinedBody, MARGIN_X + 10f, y + 36f, CONTENT_WIDTH - 20f, bodyPaint)
            drawWrappedText(canvas, remText, MARGIN_X + 10f, y + 40f + drawnBody, CONTENT_WIDTH - 20f, bodyPaint)
            y += cardH + 14f
        }

        if (forecast.dashaTimeline2027.isNotEmpty() && y + 60f < PAGE_HEIGHT - 44f) {
            y = drawSectionHeading(canvas, lCtx.getString(R.string.year_2027_dasha_timeline), MARGIN_X, y)
            val rows = forecast.dashaTimeline2027.map { it.planet to "${it.startDate} – ${it.endDate}" }
            drawKeyValueTable(canvas, rows, MARGIN_X, y, CONTENT_WIDTH)
        }
    }

    private fun draw2027MonthlyTimelinePage(
        canvas: Canvas,
        lCtx: Context,
        person: PersonEntity,
        forecast: YearForecast,
        fromIdx: Int,
        toIdx: Int,
        isHi: Boolean,
        pageNum: Int
    ) {
        drawPageHeaderFooter(
            canvas,
            lCtx,
            if (isHi) "2027 मासिक भविष्यफल समयरेखा" else "2027 Month-by-Month Timeline",
            person.name,
            pageNum
        )

        var y = 76f
        val titlePaint = TextPaint().apply {
            isAntiAlias = true
            color = COLOR_INDIGO
            textSize = 11f
            typeface = Typeface.create(Typeface.SERIF, Typeface.BOLD)
        }
        val dashaPaint = TextPaint().apply {
            isAntiAlias = true
            color = COLOR_SAFFRON_DARK
            textSize = 8.6f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }
        val bodyPaint = TextPaint().apply { isAntiAlias = true; color = COLOR_TEXT; textSize = 8.6f }
        val focusPaint = TextPaint().apply { isAntiAlias = true; color = COLOR_GREEN; textSize = 8.4f }

        val slice = forecast.monthlyForecasts.subList(
            fromIdx.coerceAtLeast(0),
            toIdx.coerceAtMost(forecast.monthlyForecasts.size)
        )
        slice.forEach { mf ->
            val predH = measureTextHeight(mf.prediction, CONTENT_WIDTH - 20f, bodyPaint)
            val cardH = 38f + predH + 22f
            if (y + cardH < PAGE_HEIGHT - 44f) {
                drawCardBox(canvas, MARGIN_X, y, CONTENT_WIDTH, cardH)
                canvas.drawText(mf.monthName, MARGIN_X + 10f, y + 16f, titlePaint)
                val dw = dashaPaint.measureText(mf.dashaSubPeriod)
                canvas.drawText(mf.dashaSubPeriod, MARGIN_X + CONTENT_WIDTH - dw - 10f, y + 16f, dashaPaint)
                canvas.drawText(
                    lCtx.getString(R.string.year_2027_moon_transit_prefix, mf.moonTransitFlavor),
                    MARGIN_X + 10f,
                    y + 29f,
                    dashaPaint
                )
                val drawnPred = drawWrappedText(canvas, mf.prediction, MARGIN_X + 10f, y + 34f, CONTENT_WIDTH - 20f, bodyPaint)
                val focusLine = "${lCtx.getString(R.string.year_2027_lucky_focus)}: ${mf.luckyFocus}   |   ${lCtx.getString(R.string.year_2027_effort_focus)}: ${mf.effortFocus}"
                drawWrappedText(canvas, focusLine, MARGIN_X + 10f, y + 38f + drawnPred, CONTENT_WIDTH - 20f, focusPaint)
                y += cardH + 8f
            }
        }
    }

    private fun draw2027ChartsAppendixPage(
        canvas: Canvas,
        lCtx: Context,
        person: PersonEntity,
        kundali: KundaliData,
        chartStyle: ChartStyle,
        isHi: Boolean,
        pageNum: Int
    ) {
        drawPageHeaderFooter(
            canvas,
            lCtx,
            lCtx.getString(R.string.year_2027_charts_appendix_title),
            person.name,
            pageNum
        )

        var y = 76f
        val lagnaBadge = lCtx.getString(R.string.chart_lagna_short)
        val ascBadge = lCtx.getString(R.string.chart_asc_short)
        val headPaint = TextPaint().apply {
            isAntiAlias = true
            color = COLOR_INDIGO
            textSize = 10f
            typeface = Typeface.create(Typeface.SERIF, Typeface.BOLD)
        }

        val chartW = 195f
        val gap = (CONTENT_WIDTH - chartW * 2f)
        // Row 1: D1 (left) and D9 (right)
        canvas.drawText(if (isHi) "लग्न चक्र (D1)" else "Natal Birth Chart (D1)", MARGIN_X, y + 10f, headPaint)
        canvas.drawText(if (isHi) "नवांश चक्र (D9)" else "Navamsha Chart (D9)", MARGIN_X + chartW + gap, y + 10f, headPaint)
        y += 18f
        drawChartToPdfCanvas(
            canvas = canvas,
            left = MARGIN_X,
            top = y,
            size = chartW,
            spec = kundali.toChartSpec("D1"),
            chartStyle = chartStyle,
            lagnaBadge = lagnaBadge,
            ascBadge = ascBadge,
            centerTitle = "D1",
            centerSubtitle = kundali.lagna.rashiName
        )
        drawChartToPdfCanvas(
            canvas = canvas,
            left = MARGIN_X + chartW + gap,
            top = y,
            size = chartW,
            spec = kundali.toChartSpec("D9"),
            chartStyle = chartStyle,
            lagnaBadge = lagnaBadge,
            ascBadge = ascBadge,
            centerTitle = "D9",
            centerSubtitle = lCtx.getString(R.string.cat_marriage_tab)
        )
        y += chartW + 18f

        // Row 2: D10 (left) + Sign Matrix summary (right)
        canvas.drawText(if (isHi) "दशमांश चक्र (D10)" else "Dashamsha Chart (D10)", MARGIN_X, y + 10f, headPaint)
        canvas.drawText(lCtx.getString(R.string.year_2027_sign_matrix_title), MARGIN_X + chartW + 24f, y + 10f, headPaint)
        y += 18f
        drawChartToPdfCanvas(
            canvas = canvas,
            left = MARGIN_X,
            top = y,
            size = chartW,
            spec = kundali.toChartSpec("D10"),
            chartStyle = chartStyle,
            lagnaBadge = lagnaBadge,
            ascBadge = ascBadge,
            centerTitle = "D10",
            centerSubtitle = lCtx.getString(R.string.cat_career_tab)
        )

        val d9Map = kundali.divisionalCharts.find { it.code == "D9" }?.placements?.associate { it.canonicalPlanet to it.signName } ?: emptyMap()
        val d10Map = kundali.divisionalCharts.find { it.code == "D10" }?.placements?.associate { it.canonicalPlanet to it.signName } ?: emptyMap()
        val rows = kundali.planets.map { pl ->
            pl.name to "${pl.rashiName} | ${d9Map[pl.canonicalName] ?: "-"} | ${d10Map[pl.canonicalName] ?: "-"}"
        }
        drawKeyValueTable(canvas, rows, MARGIN_X + chartW + 24f, y, CONTENT_WIDTH - chartW - 24f)
    }

    private fun draw2027RemediesClosingPage(
        canvas: Canvas,
        lCtx: Context,
        person: PersonEntity,
        forecast: YearForecast,
        isHi: Boolean,
        pageNum: Int
    ) {
        drawPageHeaderFooter(
            canvas,
            lCtx,
            lCtx.getString(R.string.year_2027_remedies_title),
            person.name,
            pageNum
        )

        var y = 78f
        y = drawSectionHeading(canvas, lCtx.getString(R.string.year_2027_remedies_title), MARGIN_X, y)
        val remRows = forecast.remediesSummary.map { card ->
            card.categoryLabel to "${card.mantra}\n${card.why}\n${card.how}"
        }
        y = drawKeyValueTable(canvas, remRows, MARGIN_X, y, CONTENT_WIDTH)

        y += 16f
        val discPaint = TextPaint().apply { isAntiAlias = true; color = COLOR_MUTED; textSize = 9.2f }
        if (y + 36f < PAGE_HEIGHT - 44f) {
            drawWrappedText(canvas, forecast.disclaimer, MARGIN_X, y, CONTENT_WIDTH, discPaint)
        }
    }

    // ---- Drawing Primitives ----

    private fun drawSectionHeading(canvas: Canvas, title: String, x: Float, y: Float): Float {
        val paint = TextPaint().apply {
            isAntiAlias = true
            color = COLOR_INDIGO
            textSize = 12.5f
            typeface = Typeface.create(Typeface.SERIF, Typeface.BOLD)
        }
        canvas.drawText(title, x, y + 12f, paint)
        val line = Paint().apply {
            color = COLOR_SAFFRON
            strokeWidth = 1.5f
        }
        canvas.drawLine(x, y + 17f, x + CONTENT_WIDTH, y + 17f, line)
        return y + 26f
    }

    private fun drawCardBox(canvas: Canvas, x: Float, y: Float, width: Float, height: Float): Float {
        val fill = Paint().apply { isAntiAlias = true; color = COLOR_CREAM }
        val stroke = Paint().apply {
            isAntiAlias = true
            color = COLOR_CREAM_BORDER
            style = Paint.Style.STROKE
            strokeWidth = 1f
        }
        val rect = RectF(x, y, x + width, y + height)
        canvas.drawRoundRect(rect, 8f, 8f, fill)
        canvas.drawRoundRect(rect, 8f, 8f, stroke)
        return y + height
    }

    private fun drawMiniStatCard(
        canvas: Canvas,
        x: Float,
        y: Float,
        width: Float,
        label: String,
        value: String,
        sub: String
    ) {
        drawCardBox(canvas, x, y, width, 62f)
        val lblP = TextPaint().apply { isAntiAlias = true; color = COLOR_SAFFRON_DARK; textSize = 8.5f; typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD) }
        val valP = TextPaint().apply { isAntiAlias = true; color = COLOR_INDIGO; textSize = 12f; typeface = Typeface.create(Typeface.SERIF, Typeface.BOLD) }
        val subP = TextPaint().apply { isAntiAlias = true; color = COLOR_MUTED; textSize = 8.5f }
        canvas.drawText(label, x + 10f, y + 17f, lblP)
        canvas.drawText(value, x + 10f, y + 36f, valP)
        canvas.drawText(sub, x + 10f, y + 51f, subP)
    }

    private fun drawProgressBarRow(
        canvas: Canvas,
        lCtx: Context,
        label: String,
        score: Int,
        summary: String,
        x: Float,
        y: Float,
        width: Float
    ): Float {
        val meta: AstroMeterMeta = computeAstroMeterMeta(score)
        val labelPaint = TextPaint().apply {
            isAntiAlias = true
            color = COLOR_INDIGO
            textSize = 10.5f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }
        val badgeColor = when {
            score >= 80 -> COLOR_GREEN
            score >= 60 -> COLOR_SAFFRON_DARK
            score >= 40 -> COLOR_INDIGO
            else -> Color.rgb(0xA8, 0x38, 0x38)
        }
        val scorePaint = TextPaint().apply {
            isAntiAlias = true
            color = badgeColor
            textSize = 10f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }
        val sumPaint = TextPaint().apply {
            isAntiAlias = true
            color = COLOR_TEXT
            textSize = 8.8f
        }

        canvas.drawText(label, x, y + 11f, labelPaint)
        val stars = "★".repeat(meta.starCount) + "☆".repeat(5 - meta.starCount)
        val statusStr = "${lCtx.getString(meta.statusResId)}  $stars"
        val sw = scorePaint.measureText(statusStr)
        canvas.drawText(statusStr, x + width - sw, y + 11f, scorePaint)

        val trackY = y + 17f
        val trackPaint = Paint().apply { isAntiAlias = true; color = Color.rgb(0xEC, 0xE5, 0xD6) }
        canvas.drawRoundRect(x, trackY, x + width, trackY + 7.5f, 3.75f, 3.75f, trackPaint)

        val fillW = (width * (score.coerceIn(6, 100) / 100f)).coerceAtLeast(10f)
        val fillPaint = Paint().apply {
            isAntiAlias = true
            shader = LinearGradient(
                x, trackY, x + fillW, trackY,
                intArrayOf(
                    Color.rgb(0xF6, 0xCF, 0x86),
                    COLOR_SAFFRON,
                    COLOR_SAFFRON_DARK
                ),
                null,
                Shader.TileMode.CLAMP
            )
        }
        canvas.drawRoundRect(x, trackY, x + fillW, trackY + 7.5f, 3.75f, 3.75f, fillPaint)

        val sh = drawWrappedText(canvas, summary, x, trackY + 11f, width, sumPaint)
        return trackY + 13f + sh
    }

    private fun drawBulletItem(
        canvas: Canvas,
        bullet: String,
        text: String,
        bulletColor: Int,
        textPaint: TextPaint,
        x: Float,
        y: Float,
        width: Float
    ) {
        val bp = TextPaint().apply {
            isAntiAlias = true
            color = bulletColor
            textSize = textPaint.textSize
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }
        canvas.drawText(bullet, x, y + textPaint.textSize, bp)
        drawWrappedText(canvas, text, x + 15f, y, width - 15f, textPaint)
    }

    private fun drawKeyValueTable(
        canvas: Canvas,
        rows: List<Pair<String, String>>,
        x: Float,
        startY: Float,
        width: Float
    ): Float {
        var y = startY
        val keyW = width * 0.36f
        val valW = width - keyW - 20f
        val keyPaint = TextPaint().apply {
            isAntiAlias = true
            color = COLOR_INDIGO
            textSize = 9.5f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }
        val valPaint = TextPaint().apply {
            isAntiAlias = true
            color = COLOR_TEXT
            textSize = 9.5f
        }
        val bgPaint = Paint()
        val borderPaint = Paint().apply {
            color = COLOR_CREAM_BORDER
            style = Paint.Style.STROKE
            strokeWidth = 0.8f
        }

        rows.forEachIndexed { idx, (k, v) ->
            val kh = measureTextHeight(k, keyW, keyPaint)
            val vh = measureTextHeight(v, valW, valPaint)
            val rowH = maxOf(kh, vh) + 10f
            if (y + rowH < PAGE_HEIGHT - 45f) {
                bgPaint.color = if (idx % 2 == 0) COLOR_ROW_ALT else Color.WHITE
                canvas.drawRect(x, y, x + width, y + rowH, bgPaint)
                canvas.drawRect(x, y, x + width, y + rowH, borderPaint)
                drawWrappedText(canvas, k, x + 7f, y + 5f, keyW, keyPaint)
                drawWrappedText(canvas, v, x + keyW + 13f, y + 5f, valW, valPaint)
                y += rowH
            }
        }
        return y
    }

    private fun drawWrappedText(
        canvas: Canvas,
        text: String,
        x: Float,
        y: Float,
        maxWidth: Float,
        paint: TextPaint
    ): Float {
        val layout = StaticLayout.Builder
            .obtain(text, 0, text.length, paint, maxWidth.toInt().coerceAtLeast(10))
            .setAlignment(Layout.Alignment.ALIGN_NORMAL)
            .setLineSpacing(1.5f, 1.1f)
            .build()
        canvas.save()
        canvas.translate(x, y)
        layout.draw(canvas)
        canvas.restore()
        return layout.height.toFloat()
    }

    private fun measureTextHeight(text: String, maxWidth: Float, paint: TextPaint): Float {
        val layout = StaticLayout.Builder
            .obtain(text, 0, text.length, paint, maxWidth.toInt().coerceAtLeast(10))
            .setAlignment(Layout.Alignment.ALIGN_NORMAL)
            .setLineSpacing(1.5f, 1.1f)
            .build()
        return layout.height.toFloat()
    }

    private fun drawChartToPdfCanvas(
        canvas: Canvas,
        left: Float,
        top: Float,
        size: Float,
        spec: KundaliChartSpec,
        chartStyle: ChartStyle,
        lagnaBadge: String,
        ascBadge: String,
        centerTitle: String,
        centerSubtitle: String
    ) {
        val bgPaint = Paint().apply { isAntiAlias = true; color = COLOR_CREAM }
        canvas.drawRoundRect(left - 6f, top - 6f, left + size + 6f, top + size + 6f, 8f, 8f, bgPaint)

        canvas.save()
        canvas.translate(left, top)

        val strokePaint = Paint().apply {
            isAntiAlias = true
            color = COLOR_INDIGO
            style = Paint.Style.STROKE
            strokeWidth = 1.6f
        }

        if (chartStyle == ChartStyle.NORTH_INDIAN) {
            canvas.drawRect(0f, 0f, size, size, strokePaint)
            canvas.drawLine(0f, 0f, size, size, strokePaint)
            canvas.drawLine(size, 0f, 0f, size, strokePaint)

            val diamond = Path().apply {
                moveTo(size / 2f, 0f)
                lineTo(size, size / 2f)
                lineTo(size / 2f, size)
                lineTo(0f, size / 2f)
                close()
            }
            canvas.drawPath(diamond, strokePaint)

            val houseCenters = listOf(
                Pair(size * 0.50f, size * 0.25f),
                Pair(size * 0.25f, size * 0.12f),
                Pair(size * 0.12f, size * 0.25f),
                Pair(size * 0.25f, size * 0.50f),
                Pair(size * 0.12f, size * 0.75f),
                Pair(size * 0.25f, size * 0.88f),
                Pair(size * 0.50f, size * 0.75f),
                Pair(size * 0.75f, size * 0.88f),
                Pair(size * 0.88f, size * 0.75f),
                Pair(size * 0.75f, size * 0.50f),
                Pair(size * 0.88f, size * 0.25f),
                Pair(size * 0.75f, size * 0.12f)
            )

            val signP = Paint().apply {
                isAntiAlias = true
                color = COLOR_SAFFRON_DARK
                textSize = (size * 0.042f).coerceIn(7f, 13f)
                typeface = Typeface.DEFAULT_BOLD
                textAlign = Paint.Align.CENTER
            }
            val planetP = Paint().apply {
                isAntiAlias = true
                color = COLOR_TEXT
                textSize = (size * 0.036f).coerceIn(6.5f, 11f)
                textAlign = Paint.Align.CENTER
            }

            val lagnaRashiNum = spec.lagnaRashiIndex + 1
            for (houseNum in 1..12) {
                val (cx, cy) = houseCenters[houseNum - 1]
                val signNum = ((lagnaRashiNum - 1 + (houseNum - 1)) % 12) + 1
                canvas.drawText(signNum.toString(), cx, cy - size * 0.025f, signP)

                val planetsInHouse = spec.planets.filter { it.houseNumber == houseNum }
                val planetText = if (planetsInHouse.isEmpty()) {
                    if (houseNum == 1) lagnaBadge else ""
                } else {
                    val str = planetsInHouse.joinToString(" ") { p ->
                        if (p.isRetrograde) "${p.shortName}[R]" else p.shortName
                    }
                    if (houseNum == 1) "$lagnaBadge $str" else str
                }
                if (planetText.isNotEmpty()) {
                    canvas.drawText(planetText, cx, cy + size * 0.042f, planetP)
                }
            }
        } else {
            val cellW = size / 4f
            val cellH = size / 4f
            canvas.drawRect(0f, 0f, size, size, strokePaint)
            canvas.drawLine(cellW, 0f, cellW, size, strokePaint)
            canvas.drawLine(cellW * 2f, 0f, cellW * 2f, cellH, strokePaint)
            canvas.drawLine(cellW * 2f, cellH * 3f, cellW * 2f, size, strokePaint)
            canvas.drawLine(cellW * 3f, 0f, cellW * 3f, size, strokePaint)

            canvas.drawLine(0f, cellH, size, cellH, strokePaint)
            canvas.drawLine(0f, cellH * 2f, cellW, cellH * 2f, strokePaint)
            canvas.drawLine(cellW * 3f, cellH * 2f, size, cellH * 2f, strokePaint)
            canvas.drawLine(0f, cellH * 3f, size, cellH * 3f, strokePaint)

            val centerP = Paint().apply {
                isAntiAlias = true
                color = COLOR_INDIGO
                textSize = (cellW * 0.28f).coerceIn(10f, 18f)
                typeface = Typeface.DEFAULT_BOLD
                textAlign = Paint.Align.CENTER
            }
            val subP = Paint().apply {
                isAntiAlias = true
                color = COLOR_MUTED
                textSize = (cellW * 0.14f).coerceIn(6f, 10f)
                textAlign = Paint.Align.CENTER
            }
            canvas.drawText(centerTitle, size / 2f, size / 2f, centerP)
            if (centerSubtitle.isNotEmpty()) {
                canvas.drawText(centerSubtitle.take(28), size / 2f, size / 2f + 16f, subP)
            }

            val signCoords = mapOf(
                11 to Pair(0, 0), 0 to Pair(1, 0), 1 to Pair(2, 0), 2 to Pair(3, 0),
                10 to Pair(0, 1), 3 to Pair(3, 1),
                9 to Pair(0, 2), 4 to Pair(3, 2),
                8 to Pair(0, 3), 7 to Pair(1, 3), 6 to Pair(2, 3), 5 to Pair(3, 3)
            )
            val signP = Paint().apply {
                isAntiAlias = true
                color = COLOR_SAFFRON_DARK
                textSize = (cellW * 0.21f).coerceIn(7f, 12f)
                typeface = Typeface.DEFAULT_BOLD
                textAlign = Paint.Align.CENTER
            }
            val plP = Paint().apply {
                isAntiAlias = true
                color = COLOR_TEXT
                textSize = (cellW * 0.19f).coerceIn(6f, 10.5f)
                textAlign = Paint.Align.CENTER
            }

            for (rashiIdx in 0..11) {
                val (col, row) = signCoords[rashiIdx] ?: continue
                val cx = col * cellW + cellW / 2f
                val cy = row * cellH
                val signName = (spec.rashiNamesByIndex[rashiIdx] ?: "").take(3)
                val header = if (spec.lagnaRashiIndex == rashiIdx) "$signName [$ascBadge]" else signName
                canvas.drawText(header, cx, cy + cellH * 0.32f, signP)
                val planetsInSign = spec.planets.filter { it.rashiIndex == rashiIdx }
                if (planetsInSign.isNotEmpty()) {
                    val pText = planetsInSign.joinToString(" ") { p ->
                        if (p.isRetrograde) "${p.shortName}[R]" else p.shortName
                    }
                    canvas.drawText(pText, cx, cy + cellH * 0.68f, plP)
                }
            }
        }

        canvas.restore()
    }
}
