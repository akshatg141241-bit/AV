package com.aryaleelastudios.astroveda.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.aryaleelastudios.astroveda.R
import com.aryaleelastudios.astroveda.data.model.ChartStyle
import com.aryaleelastudios.astroveda.data.model.KundaliData
import com.aryaleelastudios.astroveda.ui.theme.CreamBackground
import com.aryaleelastudios.astroveda.ui.theme.CreamBorder
import com.aryaleelastudios.astroveda.ui.theme.DeepIndigo
import com.aryaleelastudios.astroveda.ui.theme.SaffronDark
import java.util.Locale

data class KundaliChartPlanet(
    val shortName: String,
    val rashiIndex: Int,
    val houseNumber: Int,
    val isRetrograde: Boolean = false
)

data class KundaliChartSpec(
    val lagnaRashiIndex: Int,
    val planets: List<KundaliChartPlanet>,
    val rashiNamesByIndex: Map<Int, String> = emptyMap()
)

fun KundaliData.toChartSpec(code: String = "D1"): KundaliChartSpec {
    val rashiMap = houses.associate { it.rashiIndex to it.rashiName }
    if (code.equals("D1", ignoreCase = true)) {
        return KundaliChartSpec(
            lagnaRashiIndex = lagna.rashiIndex,
            planets = planets.map { p ->
                KundaliChartPlanet(
                    shortName = p.name.take(2),
                    rashiIndex = p.rashiIndex,
                    houseNumber = p.houseNumber,
                    isRetrograde = p.isRetrograde
                )
            },
            rashiNamesByIndex = rashiMap
        )
    }

    val div = divisionalCharts.firstOrNull { it.code.equals(code, ignoreCase = true) }
        ?: return toChartSpec("D1")

    val divLagnaIdx = div.placements.firstOrNull { it.canonicalPlanet == "Lagna" }?.signIndex
        ?: lagna.rashiIndex

    val retroByCanonical = planets.associate { it.canonicalName to it.isRetrograde }
    val divPlanets = div.placements
        .filter { it.canonicalPlanet != "Lagna" }
        .map { p ->
            KundaliChartPlanet(
                shortName = p.planet.take(2),
                rashiIndex = p.signIndex,
                houseNumber = ((p.signIndex - divLagnaIdx + 12) % 12) + 1,
                isRetrograde = retroByCanonical[p.canonicalPlanet] ?: false
            )
        }

    return KundaliChartSpec(
        lagnaRashiIndex = divLagnaIdx,
        planets = divPlanets,
        rashiNamesByIndex = rashiMap
    )
}

@Composable
fun KundaliChartCanvas(
    kundali: KundaliData,
    chartStyle: ChartStyle,
    modifier: Modifier = Modifier
) {
    val lagnaBadge = stringResource(R.string.chart_lagna_short)
    val ascBadge = stringResource(R.string.chart_asc_short)
    val southChartTitle = stringResource(R.string.chart_south_watermark)
    val lagnaHeader = stringResource(R.string.chart_lagna_label, kundali.lagna.rashiName, kundali.lagna.formattedDegree)
    val chartSpec = remember(kundali) { kundali.toChartSpec("D1") }

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
                drawNorthIndianChart(w, h, chartSpec, lagnaBadge, isMini = false)
            } else {
                drawSouthIndianChart(w, h, chartSpec, ascBadge, southChartTitle, lagnaHeader, isMini = false)
            }
        }
    }
}

@Composable
fun DivisionalChartCanvas(
    chartCode: String,
    kundali: KundaliData,
    chartStyle: ChartStyle,
    modifier: Modifier = Modifier
) {
    val lagnaBadge = stringResource(R.string.chart_lagna_short)
    val ascBadge = stringResource(R.string.chart_asc_short)
    val chartSpec = remember(kundali, chartCode) { kundali.toChartSpec(chartCode) }
    val lagnaRashiName = kundali.houses.firstOrNull { it.rashiIndex == chartSpec.lagnaRashiIndex }?.rashiName
        ?: kundali.lagna.rashiName
    val southSubtitle = "$chartCode • $lagnaBadge: $lagnaRashiName"

    Box(
        modifier = modifier
            .testTag("divisional_chart_${chartCode.lowercase(Locale.US)}")
            .fillMaxWidth()
            .aspectRatio(1f)
            .background(CreamBackground, RoundedCornerShape(14.dp))
            .border(1.25.dp, CreamBorder, RoundedCornerShape(14.dp))
            .padding(8.dp),
        contentAlignment = Alignment.Center
    ) {
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .padding(4.dp)
        ) {
            val w = size.width
            val h = size.height

            if (chartStyle == ChartStyle.NORTH_INDIAN) {
                drawNorthIndianChart(w, h, chartSpec, lagnaBadge, isMini = false)
            } else {
                drawSouthIndianChart(
                    w = w,
                    h = h,
                    spec = chartSpec,
                    ascBadge = ascBadge,
                    southChartTitle = chartCode,
                    lagnaSubtitle = southSubtitle,
                    isMini = false
                )
            }
        }
    }
}

@Composable
fun MiniKundaliChartCanvas(
    chartCode: String,
    chartSpec: KundaliChartSpec,
    chartStyle: ChartStyle,
    modifier: Modifier = Modifier
) {
    val lagnaBadge = stringResource(R.string.chart_lagna_short)
    val ascBadge = stringResource(R.string.chart_asc_short)

    Box(
        modifier = modifier
            .size(100.dp)
            .aspectRatio(1f)
            .background(CreamBackground, RoundedCornerShape(12.dp))
            .border(1.25.dp, CreamBorder, RoundedCornerShape(12.dp))
            .padding(4.dp)
            .testTag("mini_chart_${chartCode.lowercase(Locale.US)}"),
        contentAlignment = Alignment.Center
    ) {
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .padding(2.dp)
        ) {
            val w = size.width
            val h = size.height

            if (chartStyle == ChartStyle.NORTH_INDIAN) {
                drawNorthIndianChart(w, h, chartSpec, lagnaBadge, isMini = true)
            } else {
                drawSouthIndianChart(
                    w = w,
                    h = h,
                    spec = chartSpec,
                    ascBadge = ascBadge,
                    southChartTitle = chartCode,
                    lagnaSubtitle = "",
                    isMini = true
                )
            }
        }
    }
}

private fun DrawScope.drawNorthIndianChart(
    w: Float,
    h: Float,
    spec: KundaliChartSpec,
    lagnaBadge: String,
    isMini: Boolean
) {
    val outerStrokeWidth = if (isMini) 2.0f else 2.5f
    val innerStrokeWidth = if (isMini) 1.5f else 2.0f
    val stroke = Stroke(width = outerStrokeWidth)
    val mainColor = DeepIndigo

    drawRect(color = mainColor, size = size, style = stroke)
    drawLine(mainColor, Offset(0f, 0f), Offset(w, h), strokeWidth = innerStrokeWidth)
    drawLine(mainColor, Offset(w, 0f), Offset(0f, h), strokeWidth = innerStrokeWidth)

    val midPath = Path().apply {
        moveTo(w / 2f, 0f)
        lineTo(w, h / 2f)
        lineTo(w / 2f, h)
        lineTo(0f, h / 2f)
        close()
    }
    drawPath(midPath, color = mainColor, style = stroke)

    val houseCenters = listOf(
        Offset(w * 0.50f, h * 0.25f),
        Offset(w * 0.25f, h * 0.12f),
        Offset(w * 0.12f, h * 0.25f),
        Offset(w * 0.25f, h * 0.50f),
        Offset(w * 0.12f, h * 0.75f),
        Offset(w * 0.25f, h * 0.88f),
        Offset(w * 0.50f, h * 0.75f),
        Offset(w * 0.75f, h * 0.88f),
        Offset(w * 0.88f, h * 0.75f),
        Offset(w * 0.75f, h * 0.50f),
        Offset(w * 0.88f, h * 0.25f),
        Offset(w * 0.75f, h * 0.12f)
    )

    val native = drawContext.canvas.nativeCanvas

    val signPaint = android.graphics.Paint().apply {
        color = android.graphics.Color.rgb(0xC7, 0x7C, 0x18)
        textSize = if (isMini) (w * 0.068f).coerceIn(13f, 22f) else (w * 0.038f).coerceIn(22f, 36f)
        isAntiAlias = true
        typeface = android.graphics.Typeface.DEFAULT_BOLD
        textAlign = android.graphics.Paint.Align.CENTER
    }

    val planetPaint = android.graphics.Paint().apply {
        color = android.graphics.Color.rgb(0x1C, 0x1D, 0x36)
        textSize = if (isMini) (w * 0.054f).coerceIn(10f, 17f) else (w * 0.032f).coerceIn(20f, 30f)
        isAntiAlias = true
        typeface = android.graphics.Typeface.DEFAULT
        textAlign = android.graphics.Paint.Align.CENTER
    }

    val lagnaRashiNum = spec.lagnaRashiIndex + 1

    for (houseNum in 1..12) {
        val center = houseCenters[houseNum - 1]
        val signNum = ((lagnaRashiNum - 1 + (houseNum - 1)) % 12) + 1

        val signYOffset = if (isMini) w * 0.015f else w * 0.04f
        native.drawText(
            signNum.toString(),
            center.x,
            center.y - signYOffset,
            signPaint
        )

        val planetsInHouse = spec.planets.filter { it.houseNumber == houseNum }
        if (isMini) {
            val tokens = planetsInHouse.take(2).map { it.shortName }
            val miniText = when {
                tokens.isNotEmpty() -> tokens.joinToString(" ")
                houseNum == 1 -> lagnaBadge.take(2)
                else -> ""
            }
            if (miniText.isNotEmpty()) {
                native.drawText(
                    miniText,
                    center.x,
                    center.y + (w * 0.052f),
                    planetPaint
                )
            }
        } else {
            val planetText = if (planetsInHouse.isEmpty()) {
                if (houseNum == 1) lagnaBadge else ""
            } else {
                val listStr = planetsInHouse.joinToString(" ") { p ->
                    if (p.isRetrograde) "${p.shortName}[R]" else p.shortName
                }
                if (houseNum == 1) "$lagnaBadge $listStr" else listStr
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
}

private fun DrawScope.drawSouthIndianChart(
    w: Float,
    h: Float,
    spec: KundaliChartSpec,
    ascBadge: String,
    southChartTitle: String,
    lagnaSubtitle: String,
    isMini: Boolean
) {
    val cellW = w / 4f
    val cellH = h / 4f
    val outerStrokeWidth = if (isMini) 1.8f else 2.5f
    val lineStrokeWidth = if (isMini) 1.3f else 2.0f
    val stroke = Stroke(width = outerStrokeWidth)
    val mainColor = DeepIndigo

    drawRect(mainColor, size = size, style = stroke)

    drawLine(mainColor, Offset(cellW, 0f), Offset(cellW, h), strokeWidth = lineStrokeWidth)
    drawLine(mainColor, Offset(cellW * 2f, 0f), Offset(cellW * 2f, cellH), strokeWidth = lineStrokeWidth)
    drawLine(mainColor, Offset(cellW * 2f, cellH * 3f), Offset(cellW * 2f, h), strokeWidth = lineStrokeWidth)
    drawLine(mainColor, Offset(cellW * 3f, 0f), Offset(cellW * 3f, h), strokeWidth = lineStrokeWidth)

    drawLine(mainColor, Offset(0f, cellH), Offset(w, cellH), strokeWidth = lineStrokeWidth)
    drawLine(mainColor, Offset(0f, cellH * 2f), Offset(cellW, cellH * 2f), strokeWidth = lineStrokeWidth)
    drawLine(mainColor, Offset(cellW * 3f, cellH * 2f), Offset(w, cellH * 2f), strokeWidth = lineStrokeWidth)
    drawLine(mainColor, Offset(0f, cellH * 3f), Offset(w, cellH * 3f), strokeWidth = lineStrokeWidth)

    drawRect(
        color = SaffronDark,
        topLeft = Offset(cellW, cellH),
        size = Size(cellW * 2f, cellH * 2f),
        style = Stroke(width = if (isMini) 1.1f else 1.5f)
    )

    val signCoords = mapOf(
        11 to Pair(0, 0), 0 to Pair(1, 0), 1 to Pair(2, 0), 2 to Pair(3, 0),
        10 to Pair(0, 1), 3 to Pair(3, 1),
        9 to Pair(0, 2), 4 to Pair(3, 2),
        8 to Pair(0, 3), 7 to Pair(1, 3), 6 to Pair(2, 3), 5 to Pair(3, 3)
    )

    val native = drawContext.canvas.nativeCanvas

    val titlePaint = android.graphics.Paint().apply {
        color = android.graphics.Color.rgb(0xC7, 0x7C, 0x18)
        textSize = if (isMini) (cellW * 0.25f).coerceIn(11f, 18f) else (cellW * 0.20f).coerceIn(18f, 26f)
        isAntiAlias = true
        typeface = android.graphics.Typeface.DEFAULT_BOLD
        textAlign = android.graphics.Paint.Align.CENTER
    }

    val planetPaint = android.graphics.Paint().apply {
        color = android.graphics.Color.rgb(0x1C, 0x1D, 0x36)
        textSize = if (isMini) (cellW * 0.22f).coerceIn(10f, 16f) else (cellW * 0.19f).coerceIn(17f, 24f)
        isAntiAlias = true
        typeface = android.graphics.Typeface.DEFAULT
        textAlign = android.graphics.Paint.Align.CENTER
    }

    if (isMini) {
        val centerPaint = android.graphics.Paint().apply {
            color = android.graphics.Color.rgb(0x2B, 0x2D, 0x5C)
            textSize = (cellW * 0.34f).coerceIn(14f, 24f)
            isAntiAlias = true
            typeface = android.graphics.Typeface.DEFAULT_BOLD
            textAlign = android.graphics.Paint.Align.CENTER
        }
        native.drawText(
            southChartTitle,
            w / 2f,
            h / 2f + (centerPaint.textSize * 0.35f),
            centerPaint
        )
    } else {
        native.drawText(
            southChartTitle,
            w / 2f,
            h / 2f - 10f,
            titlePaint
        )
        if (lagnaSubtitle.isNotEmpty()) {
            native.drawText(
                lagnaSubtitle,
                w / 2f,
                h / 2f + 24f,
                planetPaint
            )
        }
    }

    for (rashiIdx in 0..11) {
        val (col, row) = signCoords[rashiIdx] ?: continue
        val x = col * cellW + cellW / 2f
        val y = row * cellH

        val signName = (spec.rashiNamesByIndex[rashiIdx] ?: "").take(if (isMini) 2 else 3)
        val isLagnaSign = spec.lagnaRashiIndex == rashiIdx

        if (isMini) {
            val header = if (isLagnaSign) "L·${rashiIdx + 1}" else "${rashiIdx + 1}"
            native.drawText(header, x, y + cellH * 0.34f, titlePaint)
            val planetsInSign = spec.planets.filter { it.rashiIndex == rashiIdx }
            if (planetsInSign.isNotEmpty()) {
                val pText = planetsInSign.take(2).joinToString(" ") { it.shortName }
                native.drawText(pText, x, y + cellH * 0.74f, planetPaint)
            }
        } else {
            val header = if (isLagnaSign) "$signName [$ascBadge]" else signName
            native.drawText(header, x, y + cellH * 0.28f, titlePaint)

            val planetsInSign = spec.planets.filter { it.rashiIndex == rashiIdx }
            if (planetsInSign.isNotEmpty()) {
                val pText = planetsInSign.joinToString(" ") { p ->
                    if (p.isRetrograde) "${p.shortName}[R]" else p.shortName
                }
                native.drawText(pText, x, y + cellH * 0.65f, planetPaint)
            }
        }
    }
}
