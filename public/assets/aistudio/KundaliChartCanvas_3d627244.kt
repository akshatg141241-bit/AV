package com.aryaleelastudios.kundaliguru.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aryaleelastudios.kundaliguru.data.model.ChartStyle
import com.aryaleelastudios.kundaliguru.data.model.KundaliData
import com.aryaleelastudios.kundaliguru.ui.theme.CreamBackground
import com.aryaleelastudios.kundaliguru.ui.theme.CreamBorder
import com.aryaleelastudios.kundaliguru.ui.theme.DeepIndigo
import com.aryaleelastudios.kundaliguru.ui.theme.SaffronAccent
import com.aryaleelastudios.kundaliguru.ui.theme.SaffronDark

@Composable
fun KundaliChartCanvas(
    kundali: KundaliData,
    chartStyle: ChartStyle,
    isHindi: Boolean,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .testTag("kundali_chart_canvas_box")
            .fillMaxWidth()
            .aspectRatio(1f)
            .background(CreamBackground, RoundedCornerShape(16.dp))
            .border(1.5.dp, CreamBorder, RoundedCornerShape(16.dp))
            .padding(8.dp),
        contentAlignment = Alignment.Center
    ) {
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .padding(4.dp)
                .testTag("kundali_chart_drawing")
        ) {
            val w = size.width
            val h = size.height

            if (chartStyle == ChartStyle.NORTH_INDIAN) {
                drawNorthIndianChart(w, h, kundali, isHindi)
            } else {
                drawSouthIndianChart(w, h, kundali, isHindi)
            }
        }
    }
}

private fun DrawScope.drawNorthIndianChart(
    w: Float,
    h: Float,
    kundali: KundaliData,
    isHindi: Boolean
) {
    val stroke = Stroke(width = 2.5f)
    val accentStroke = Stroke(width = 1.5f)
    val mainColor = DeepIndigo
    val accentColor = SaffronDark

    // Outer bounding square
    drawRect(color = mainColor, size = size, style = stroke)

    // Diagonals
    drawLine(mainColor, Offset(0f, 0f), Offset(w, h), strokeWidth = 2f)
    drawLine(mainColor, Offset(w, 0f), Offset(0f, h), strokeWidth = 2f)

    // Inner Diamond (connecting midpoints)
    val midPath = Path().apply {
        moveTo(w / 2f, 0f)
        lineTo(w, h / 2f)
        lineTo(w / 2f, h)
        lineTo(0f, h / 2f)
        close()
    }
    drawPath(midPath, color = mainColor, style = stroke)

    // House Centers for labeling sign numbers and planets
    // In North Indian charts, House 1 is top center diamond
    val houseCenters = listOf(
        Offset(w * 0.50f, h * 0.25f), // H1: Top Diamond
        Offset(w * 0.25f, h * 0.12f), // H2: Top Left Triangle
        Offset(w * 0.12f, h * 0.25f), // H3: Far Left Upper Triangle
        Offset(w * 0.25f, h * 0.50f), // H4: Left Diamond
        Offset(w * 0.12f, h * 0.75f), // H5: Far Left Lower Triangle
        Offset(w * 0.25f, h * 0.88f), // H6: Bottom Left Triangle
        Offset(w * 0.50f, h * 0.75f), // H7: Bottom Diamond
        Offset(w * 0.75f, h * 0.88f), // H8: Bottom Right Triangle
        Offset(w * 0.88f, h * 0.75f), // H9: Far Right Lower Triangle
        Offset(w * 0.75f, h * 0.50f), // H10: Right Diamond
        Offset(w * 0.88f, h * 0.25f), // H11: Far Right Upper Triangle
        Offset(w * 0.75f, h * 0.12f)  // H12: Top Right Triangle
    )

    val native = drawContext.canvas.nativeCanvas

    val signPaint = android.graphics.Paint().apply {
        color = android.graphics.Color.rgb(0xC7, 0x7C, 0x18) // Saffron dark
        textSize = (w * 0.038f).coerceIn(24f, 36f)
        isAntiAlias = true
        typeface = android.graphics.Typeface.DEFAULT_BOLD
        textAlign = android.graphics.Paint.Align.CENTER
    }

    val planetPaint = android.graphics.Paint().apply {
        color = android.graphics.Color.rgb(0x1C, 0x1D, 0x36) // Deep Indigo text
        textSize = (w * 0.034f).coerceIn(22f, 32f)
        isAntiAlias = true
        typeface = android.graphics.Typeface.DEFAULT
        textAlign = android.graphics.Paint.Align.CENTER
    }

    val lagnaRashiNum = kundali.lagna.rashiIndex + 1 // 1..12

    // Draw house numbers (Rashis) and planets in each house
    for (houseNum in 1..12) {
        val center = houseCenters[houseNum - 1]
        val signNum = ((lagnaRashiNum - 1 + (houseNum - 1)) % 12) + 1

        // Sign number
        native.drawText(
            signNum.toString(),
            center.x,
            center.y - (w * 0.04f),
            signPaint
        )

        // Find planets in this house
        val planetsInHouse = kundali.planets.filter { it.houseNumber == houseNum }
        val planetText = if (planetsInHouse.isEmpty()) {
            if (houseNum == 1) (if (isHindi) "लग्न" else "Lag") else ""
        } else {
            val listStr = planetsInHouse.joinToString(" ") { p ->
                val pShort = if (isHindi) p.nameHi.take(2) else p.nameEn.take(2)
                if (p.isRetrograde) "$pShort[R]" else pShort
            }
            if (houseNum == 1) "${if (isHindi) "लग्न" else "Lag"} $listStr" else listStr
        }

        if (planetText.isNotEmpty()) {
            native.drawText(
                planetText,
                center.x,
                center.y + (w * 0.035f),
                planetPaint
            )
        }
    }
}

private fun DrawScope.drawSouthIndianChart(
    w: Float,
    h: Float,
    kundali: KundaliData,
    isHindi: Boolean
) {
    val cellW = w / 4f
    val cellH = h / 4f
    val stroke = Stroke(width = 2.5f)
    val mainColor = DeepIndigo

    // Outer grid boundary
    drawRect(mainColor, size = size, style = stroke)

    // Vertical lines
    drawLine(mainColor, Offset(cellW, 0f), Offset(cellW, h), strokeWidth = 2f)
    drawLine(mainColor, Offset(cellW * 2f, 0f), Offset(cellW * 2f, cellH), strokeWidth = 2f)
    drawLine(mainColor, Offset(cellW * 2f, cellH * 3f), Offset(cellW * 2f, h), strokeWidth = 2f)
    drawLine(mainColor, Offset(cellW * 3f, 0f), Offset(cellW * 3f, h), strokeWidth = 2f)

    // Horizontal lines
    drawLine(mainColor, Offset(0f, cellH), Offset(w, cellH), strokeWidth = 2f)
    drawLine(mainColor, Offset(0f, cellH * 2f), Offset(cellW, cellH * 2f), strokeWidth = 2f)
    drawLine(mainColor, Offset(cellW * 3f, cellH * 2f), Offset(w, cellH * 2f), strokeWidth = 2f)
    drawLine(mainColor, Offset(0f, cellH * 3f), Offset(w, cellH * 3f), strokeWidth = 2f)

    // Center 2x2 box outline
    drawRect(
        color = SaffronDark,
        topLeft = Offset(cellW, cellH),
        size = androidx.compose.ui.geometry.Size(cellW * 2f, cellH * 2f),
        style = Stroke(width = 1.5f)
    )

    // Fixed South Indian sign coordinates:
    // Row 0: Pisces(11), Aries(0), Taurus(1), Gemini(2)
    // Row 1: Aquarius(10), Center, Center, Cancer(3)
    // Row 2: Capricorn(9), Center, Center, Leo(4)
    // Row 3: Sagittarius(8), Scorpio(7), Libra(6), Virgo(5)
    val signCoords = mapOf(
        11 to Pair(0, 0), 0 to Pair(1, 0), 1 to Pair(2, 0), 2 to Pair(3, 0),
        10 to Pair(0, 1), 3 to Pair(3, 1),
        9 to Pair(0, 2), 4 to Pair(3, 2),
        8 to Pair(0, 3), 7 to Pair(1, 3), 6 to Pair(2, 3), 5 to Pair(3, 3)
    )

    val native = drawContext.canvas.nativeCanvas

    val titlePaint = android.graphics.Paint().apply {
        color = android.graphics.Color.rgb(0xC7, 0x7C, 0x18)
        textSize = (cellW * 0.22f).coerceIn(20f, 28f)
        isAntiAlias = true
        typeface = android.graphics.Typeface.DEFAULT_BOLD
        textAlign = android.graphics.Paint.Align.CENTER
    }

    val planetPaint = android.graphics.Paint().apply {
        color = android.graphics.Color.rgb(0x1C, 0x1D, 0x36)
        textSize = (cellW * 0.20f).coerceIn(18f, 26f)
        isAntiAlias = true
        typeface = android.graphics.Typeface.DEFAULT
        textAlign = android.graphics.Paint.Align.CENTER
    }

    // Center watermark / title
    native.drawText(
        if (isHindi) "दक्षिण भारतीय चक्र" else "South Indian Chart",
        w / 2f,
        h / 2f - 10f,
        titlePaint
    )
    native.drawText(
        if (isHindi) "लग्न: ${kundali.lagna.rashiNameHi}" else "Asc: ${kundali.lagna.rashiNameEn}",
        w / 2f,
        h / 2f + 25f,
        planetPaint
    )

    // Draw for each of the 12 signs
    for (rashiIdx in 0..11) {
        val (col, row) = signCoords[rashiIdx] ?: continue
        val x = col * cellW + cellW / 2f
        val y = row * cellH

        val signName = if (isHindi) kundali.lagna.rashiNameHi.take(3) else com.aryaleelastudios.kundaliguru.astro.AstroEngine.RASHI_NAMES_EN[rashiIdx].take(3)
        val isLagnaSign = kundali.lagna.rashiIndex == rashiIdx

        // Header in cell
        val header = if (isLagnaSign) "$signName [ASC]" else signName
        native.drawText(header, x, y + cellH * 0.28f, titlePaint)

        // Planets in this sign
        val planetsInSign = kundali.planets.filter { it.rashiIndex == rashiIdx }
        if (planetsInSign.isNotEmpty()) {
            val pText = planetsInSign.joinToString(" ") { p ->
                val pShort = if (isHindi) p.nameHi.take(2) else p.nameEn.take(2)
                if (p.isRetrograde) "$pShort[R]" else pShort
            }
            native.drawText(pText, x, y + cellH * 0.65f, planetPaint)
        }
    }
}
