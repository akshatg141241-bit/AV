package com.aryaleelastudios.astroveda.astro

import com.aryaleelastudios.astroveda.data.local.PersonEntity
import com.aryaleelastudios.astroveda.data.model.AyanamsaType
import com.aryaleelastudios.astroveda.data.model.CosmicHighlight
import com.aryaleelastudios.astroveda.data.model.DashaPeriod
import com.aryaleelastudios.astroveda.data.model.DivisionalChart
import com.aryaleelastudios.astroveda.data.model.DivisionalPlacement
import com.aryaleelastudios.astroveda.data.model.HouseCusp
import com.aryaleelastudios.astroveda.data.model.KundaliData
import com.aryaleelastudios.astroveda.data.model.LagnaInfo
import com.aryaleelastudios.astroveda.data.model.MonthForecast
import com.aryaleelastudios.astroveda.data.model.MonthRange
import com.aryaleelastudios.astroveda.data.model.PanchangInfo
import com.aryaleelastudios.astroveda.data.model.PlanetPosition
import com.aryaleelastudios.astroveda.data.model.PowerBarMetric
import com.aryaleelastudios.astroveda.data.model.PredictionPeriod
import com.aryaleelastudios.astroveda.data.model.ReadingCategory
import com.aryaleelastudios.astroveda.data.model.YearCategoryDeepDive
import com.aryaleelastudios.astroveda.data.model.YearForecast
import com.aryaleelastudios.astroveda.data.model.YogaDoshaStatus
import com.aryaleelastudios.astroveda.data.repository.ContentRepository
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import kotlin.math.abs
import kotlin.math.acos
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.floor
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sin
import kotlin.math.tan

object AstroEngine {

    val CANONICAL_RASHIS = listOf(
        "Aries", "Taurus", "Gemini", "Cancer",
        "Leo", "Virgo", "Libra", "Scorpio",
        "Sagittarius", "Capricorn", "Aquarius", "Pisces"
    )

    val CANONICAL_DASHA_LORDS = listOf(
        "Ketu", "Venus", "Sun", "Moon", "Mars", "Rahu", "Jupiter", "Saturn", "Mercury"
    )

    val DASHA_YEARS = listOf(7, 20, 6, 10, 7, 18, 16, 19, 17)

    private fun normalizeDegrees(deg: Double): Double {
        var d = deg % 360.0
        if (d < 0.0) d += 360.0
        return d
    }

    private fun toRadians(deg: Double): Double = deg * Math.PI / 180.0
    private fun toDegrees(rad: Double): Double = rad * 180.0 / Math.PI

    fun calculateJulianDay(
        year: Int,
        month: Int,
        day: Int,
        hour: Int,
        minute: Int,
        second: Int,
        tzOffsetHours: Double
    ): Double {
        val decimalUtcHour = (hour + minute / 60.0 + second / 3600.0) - tzOffsetHours
        var y = year
        var m = month
        val d = day.toDouble() + decimalUtcHour / 24.0

        if (m <= 2) {
            y -= 1
            m += 12
        }

        val a = floor(y / 100.0)
        val b = 2.0 - a + floor(a / 4.0)
        return floor(365.25 * (y + 4716)) + floor(30.6001 * (m + 1)) + d + b - 1524.5
    }

    fun calculateAyanamsa(jd: Double, type: AyanamsaType): Double {
        val t = (jd - 2451545.0) / 36525.0
        val baseLahiri = 23.85694 + 1.396887 * t + 0.000308 * t * t
        return when (type) {
            AyanamsaType.LAHIRI -> baseLahiri
            AyanamsaType.RAMAN -> baseLahiri - 1.44
            AyanamsaType.KP -> baseLahiri - 0.11
        }
    }

    private fun calculateSunTropical(t: Double): Pair<Double, Double> {
        val l0 = normalizeDegrees(280.46646 + 36000.76983 * t + 0.0003032 * t * t)
        val m = normalizeDegrees(357.52911 + 35999.05029 * t - 0.0001537 * t * t)
        val mRad = toRadians(m)
        val c = (1.914602 - 0.004817 * t - 0.000014 * t * t) * sin(mRad) +
            (0.019993 - 0.000101 * t) * sin(2 * mRad) +
            0.000289 * sin(3 * mRad)
        val trueLong = normalizeDegrees(l0 + c)
        return Pair(trueLong, 0.9856)
    }

    private fun calculateMoonTropical(t: Double): Pair<Double, Double> {
        val lp = normalizeDegrees(218.3164477 + 481267.88125421 * t)
        val d = normalizeDegrees(297.8501921 + 445267.1114034 * t)
        val m = normalizeDegrees(357.5291092 + 35999.0502909 * t)
        val mp = normalizeDegrees(134.9633964 + 477198.8675055 * t)
        val f = normalizeDegrees(93.2720950 + 483202.0175233 * t)

        val dR = toRadians(d)
        val mR = toRadians(m)
        val mpR = toRadians(mp)
        val fR = toRadians(f)

        val l = lp +
            6.288774 * sin(mpR) +
            1.274027 * sin(2 * dR - mpR) +
            0.658314 * sin(2 * dR) +
            0.213618 * sin(2 * mpR) -
            0.185116 * sin(mR) -
            0.114332 * sin(2 * fR) +
            0.058793 * sin(2 * dR - 2 * mpR) +
            0.057066 * sin(2 * dR - mR - mpR) +
            0.053322 * sin(2 * dR + mpR) +
            0.046153 * sin(2 * dR - mR)

        return Pair(normalizeDegrees(l), 13.176)
    }

    private fun calculatePlanetHelio(
        t: Double,
        a0: Double, e0: Double, eDot: Double,
        i0: Double, iDot: Double,
        l0: Double, lDot: Double,
        p0: Double, pDot: Double,
        n0: Double, nDot: Double
    ): Triple<Double, Double, Double> {
        val a = a0
        val e = e0 + eDot * t
        val i = toRadians(i0 + iDot * t)
        val l = normalizeDegrees(l0 + lDot * t)
        val p = normalizeDegrees(p0 + pDot * t)
        val n = toRadians(normalizeDegrees(n0 + nDot * t))

        val m = normalizeDegrees(l - p)
        val mRad = toRadians(m)

        var eAnom = mRad
        for (step in 0..5) {
            val delta = (eAnom - e * sin(eAnom) - mRad) / (1.0 - e * cos(eAnom))
            eAnom -= delta
            if (abs(delta) < 1e-6) break
        }

        val xOrb = a * (cos(eAnom) - e)
        val yOrb = a * kotlin.math.sqrt(1.0 - e * e) * sin(eAnom)

        val omegaRad = toRadians(normalizeDegrees(p - (n0 + nDot * t)))
        val xNode = xOrb * cos(omegaRad) - yOrb * sin(omegaRad)
        val yNode = xOrb * sin(omegaRad) + yOrb * cos(omegaRad)

        val xEcl = xNode * cos(n) - yNode * cos(i) * sin(n)
        val yEcl = xNode * sin(n) + yNode * cos(i) * cos(n)
        val zEcl = yNode * sin(i)

        return Triple(xEcl, yEcl, zEcl)
    }

    private fun helioToGeocentric(
        planetCoords: Triple<Double, Double, Double>,
        sunCoords: Triple<Double, Double, Double>
    ): Double {
        val xGeo = planetCoords.first + sunCoords.first
        val yGeo = planetCoords.second + sunCoords.second
        return normalizeDegrees(toDegrees(atan2(yGeo, xGeo)))
    }

    private fun calculatePlanets(
        jd: Double,
        ayanamsa: Double,
        rashiLabels: List<String>,
        nakshatraLabels: List<String>,
        dashaLabels: List<String>,
        planetLabels: Map<String, String>
    ): List<PlanetPosition> {
        val t = (jd - 2451545.0) / 36525.0
        val dt = 0.001

        val (sunTropical, _) = calculateSunTropical(t)
        val (sunTrop2, _) = calculateSunTropical(t + dt)

        val (moonTropical, _) = calculateMoonTropical(t)

        val sunRad = toRadians(sunTropical)
        val sunCoords1 = Triple(cos(sunRad), sin(sunRad), 0.0)
        val sunRad2 = toRadians(sunTrop2)
        val sunCoords2 = Triple(cos(sunRad2), sin(sunRad2), 0.0)

        val mars1 = calculatePlanetHelio(t, 1.523662, 0.0934123, 0.000092, 1.85061, -0.000675, 355.45332, 19140.30268, 336.04084, 1.84104, 49.5574, -0.29257)
        val mars2 = calculatePlanetHelio(t + dt, 1.523662, 0.0934123, 0.000092, 1.85061, -0.000675, 355.45332, 19140.30268, 336.04084, 1.84104, 49.5574, -0.29257)
        val marsTrop1 = helioToGeocentric(mars1, sunCoords1)
        val marsTrop2 = helioToGeocentric(mars2, sunCoords2)

        val merc1 = calculatePlanetHelio(t, 0.387098, 0.2056306, 0.000025, 7.00487, -0.005947, 252.25084, 149472.67411, 77.45645, 1.55648, 48.3313, -0.12534)
        val merc2 = calculatePlanetHelio(t + dt, 0.387098, 0.2056306, 0.000025, 7.00487, -0.005947, 252.25084, 149472.67411, 77.45645, 1.55648, 48.3313, -0.12534)
        val mercTrop1 = helioToGeocentric(merc1, sunCoords1)
        val mercTrop2 = helioToGeocentric(merc2, sunCoords2)

        val jup1 = calculatePlanetHelio(t, 5.203363, 0.04839266, -0.000128, 1.30530, -0.004156, 34.40438, 3034.74612, 14.75385, 1.61657, 100.55615, 0.21381)
        val jup2 = calculatePlanetHelio(t + dt, 5.203363, 0.04839266, -0.000128, 1.30530, -0.004156, 34.40438, 3034.74612, 14.75385, 1.61657, 100.55615, 0.21381)
        val jupTrop1 = helioToGeocentric(jup1, sunCoords1)
        val jupTrop2 = helioToGeocentric(jup2, sunCoords2)

        val ven1 = calculatePlanetHelio(t, 0.723332, 0.00677323, -0.000049, 3.39471, -0.000789, 181.97973, 58517.81538, 131.57294, 1.40222, 76.68069, -0.2777)
        val ven2 = calculatePlanetHelio(t + dt, 0.723332, 0.00677323, -0.000049, 3.39471, -0.000789, 181.97973, 58517.81538, 131.57294, 1.40222, 76.68069, -0.2777)
        val venTrop1 = helioToGeocentric(ven1, sunCoords1)
        val venTrop2 = helioToGeocentric(ven2, sunCoords2)

        val sat1 = calculatePlanetHelio(t, 9.537070, 0.05415060, -0.000368, 2.48446, 0.001936, 49.94432, 1222.49362, 92.43194, 1.95842, 113.71504, -0.28867)
        val sat2 = calculatePlanetHelio(t + dt, 9.537070, 0.05415060, -0.000368, 2.48446, 0.001936, 49.94432, 1222.49362, 92.43194, 1.95842, 113.71504, -0.28867)
        val satTrop1 = helioToGeocentric(sat1, sunCoords1)
        val satTrop2 = helioToGeocentric(sat2, sunCoords2)

        val rahuTrop1 = normalizeDegrees(125.04452 - 1934.136261 * t + 0.0020708 * t * t)
        val ketuTrop1 = normalizeDegrees(rahuTrop1 + 180.0)

        fun isRetrogradeMotion(pos1: Double, pos2: Double): Boolean {
            var diff = pos2 - pos1
            if (diff > 180.0) diff -= 360.0
            if (diff < -180.0) diff += 360.0
            return diff < 0.0
        }

        val rawPlanets = listOf(
            RawPlanet("Sun", "☉", sunTropical, false),
            RawPlanet("Moon", "☽", moonTropical, false),
            RawPlanet("Mars", "♂", marsTrop1, isRetrogradeMotion(marsTrop1, marsTrop2)),
            RawPlanet("Mercury", "☿", mercTrop1, isRetrogradeMotion(mercTrop1, mercTrop2)),
            RawPlanet("Jupiter", "♃", jupTrop1, isRetrogradeMotion(jupTrop1, jupTrop2)),
            RawPlanet("Venus", "♀", venTrop1, isRetrogradeMotion(venTrop1, venTrop2)),
            RawPlanet("Saturn", "♄", satTrop1, isRetrogradeMotion(satTrop1, satTrop2)),
            RawPlanet("Rahu", "☊", rahuTrop1, true),
            RawPlanet("Ketu", "☋", ketuTrop1, true)
        )

        return rawPlanets.map { raw ->
            val siderealLong = normalizeDegrees(raw.tropicalLong - ayanamsa)
            val rashiIdx = (siderealLong / 30.0).toInt().coerceIn(0, 11)
            val degInRashi = siderealLong % 30.0

            val nakshatraIdx = (siderealLong / (360.0 / 27.0)).toInt().coerceIn(0, 26)
            val pada = ((siderealLong % (360.0 / 27.0)) / (360.0 / 108.0)).toInt().coerceIn(0, 3) + 1
            val lordIdx = nakshatraIdx % 9

            val d = degInRashi.toInt()
            val m = ((degInRashi - d) * 60).toInt()
            val s = ((((degInRashi - d) * 60) - m) * 60).toInt()
            val fmt = "%02d° %02d' %02d\"".format(Locale.US, d, m, s)

            PlanetPosition(
                canonicalName = raw.canonicalName,
                name = planetLabels[raw.canonicalName] ?: raw.canonicalName,
                glyph = raw.glyph,
                longitude = siderealLong,
                rashiIndex = rashiIdx,
                rashiName = rashiLabels.getOrElse(rashiIdx) { CANONICAL_RASHIS[rashiIdx] },
                degreeInRashi = degInRashi,
                formattedDegree = fmt,
                isRetrograde = raw.isRetrograde,
                nakshatraIndex = nakshatraIdx,
                nakshatraName = nakshatraLabels.getOrElse(nakshatraIdx) { "" },
                nakshatraLord = dashaLabels.getOrElse(lordIdx) { CANONICAL_DASHA_LORDS[lordIdx] },
                pada = pada
            )
        }
    }

    private data class RawPlanet(
        val canonicalName: String,
        val glyph: String,
        val tropicalLong: Double,
        val isRetrograde: Boolean
    )

    fun calculateLagna(
        jd: Double,
        lat: Double,
        lon: Double,
        ayanamsa: Double,
        rashiLabels: List<String> = CANONICAL_RASHIS,
        nakshatraLabels: List<String> = emptyList()
    ): LagnaInfo {
        val t = (jd - 2451545.0) / 36525.0
        val gmst0 = normalizeDegrees(280.46061837 + 360.98564736629 * (jd - 2451545.0) + 0.000387933 * t * t)
        val lst = normalizeDegrees(gmst0 + lon)
        val eps = toRadians(23.4392911 - 0.0130042 * t)

        val lstRad = toRadians(lst)
        val latRad = toRadians(lat)

        val num = -cos(lstRad)
        val den = sin(lstRad) * cos(eps) + tan(latRad) * sin(eps)
        var ascTrop = toDegrees(atan2(num, den)) + 90.0
        ascTrop = normalizeDegrees(ascTrop)

        val ascSidereal = normalizeDegrees(ascTrop - ayanamsa)
        val rashiIdx = (ascSidereal / 30.0).toInt().coerceIn(0, 11)
        val degInRashi = ascSidereal % 30.0

        val nakshatraIdx = (ascSidereal / (360.0 / 27.0)).toInt().coerceIn(0, 26)
        val pada = ((ascSidereal % (360.0 / 27.0)) / (360.0 / 108.0)).toInt().coerceIn(0, 3) + 1

        val d = degInRashi.toInt()
        val m = ((degInRashi - d) * 60).toInt()
        val s = ((((degInRashi - d) * 60) - m) * 60).toInt()
        val fmt = "%02d° %02d' %02d\"".format(Locale.US, d, m, s)

        return LagnaInfo(
            longitude = ascSidereal,
            rashiIndex = rashiIdx,
            rashiName = rashiLabels.getOrElse(rashiIdx) { CANONICAL_RASHIS[rashiIdx] },
            degreeInRashi = degInRashi,
            formattedDegree = fmt,
            nakshatraIndex = nakshatraIdx,
            nakshatraName = nakshatraLabels.getOrElse(nakshatraIdx) { "" },
            pada = pada
        )
    }

    private fun calculateHouseCusps(lagna: LagnaInfo, rashiLabels: List<String>): List<HouseCusp> {
        return (1..12).map { houseNum ->
            val signIdx = (lagna.rashiIndex + (houseNum - 1)) % 12
            val deg = lagna.degreeInRashi
            val d = deg.toInt()
            val m = ((deg - d) * 60).toInt()
            val s = ((((deg - d) * 60) - m) * 60).toInt()
            val fmt = "%02d° %02d' %02d\"".format(Locale.US, d, m, s)
            HouseCusp(
                houseNumber = houseNum,
                rashiIndex = signIdx,
                rashiName = rashiLabels.getOrElse(signIdx) { CANONICAL_RASHIS[signIdx] },
                degree = deg,
                formattedDegree = fmt
            )
        }
    }

    private fun calculatePanchang(
        birthCalendar: Calendar,
        sunLong: Double,
        moonLong: Double,
        lat: Double,
        langCode: String,
        contentRepo: ContentRepository
    ): PanchangInfo {
        var tithiDiff = moonLong - sunLong
        if (tithiDiff < 0) tithiDiff += 360.0
        val tithiIndex = (tithiDiff / 12.0).toInt().coerceIn(0, 29)
        val isShukla = tithiIndex < 15

        val dayOfWeek = birthCalendar.get(Calendar.DAY_OF_WEEK) - 1
        val yogaSum = normalizeDegrees(sunLong + moonLong)
        val yogaIndex = (yogaSum / (360.0 / 27.0)).toInt().coerceIn(0, 26)

        val karanaTotal = (tithiDiff / 6.0).toInt().coerceIn(0, 59)
        val karanaIndex = when {
            karanaTotal == 0 -> 10
            karanaTotal >= 57 -> when (karanaTotal) {
                57 -> 7
                58 -> 8
                else -> 9
            }
            else -> (karanaTotal - 1) % 7
        }

        val nakshatraIdx = (moonLong / (360.0 / 27.0)).toInt().coerceIn(0, 26)

        val dayOfYear = birthCalendar.get(Calendar.DAY_OF_YEAR)
        val declination = 23.45 * sin(toRadians(360.0 / 365.0 * (dayOfYear - 81)))
        val hourAngleArg = -tan(toRadians(lat)) * tan(toRadians(declination))
        val hourAngle = if (hourAngleArg in -1.0..1.0) toDegrees(acos(hourAngleArg)) else 90.0
        val halfDayHours = hourAngle / 15.0

        val riseHour = (12.0 - halfDayHours).toInt().coerceIn(4, 9)
        val riseMin = (((12.0 - halfDayHours) - riseHour) * 60).toInt().coerceIn(0, 59)
        val setHour = (12.0 + halfDayHours).toInt().coerceIn(15, 21)
        val setMin = (((12.0 + halfDayHours) - setHour) * 60).toInt().coerceIn(0, 59)

        val sunriseStr = "%02d:%02d AM".format(Locale.US, riseHour, riseMin)
        val sunsetStr = "%02d:%02d PM".format(Locale.US, if (setHour > 12) setHour - 12 else setHour, setMin)

        val tithis = contentRepo.resolveAstroStringList(langCode, "tithis")
        val varas = contentRepo.resolveAstroStringList(langCode, "varas")
        val nakshatras = contentRepo.resolveAstroStringList(langCode, "nakshatras")
        val yogas = contentRepo.resolveAstroStringList(langCode, "yogas")
        val karanas = contentRepo.resolveAstroStringList(langCode, "karanas")
        val paksha = contentRepo.resolveAstroLabel(
            langCode,
            if (isShukla) "paksha_shukla" else "paksha_krishna"
        )

        return PanchangInfo(
            tithi = tithis.getOrElse(tithiIndex) { "" },
            paksha = paksha,
            vara = varas.getOrElse(dayOfWeek) { "" },
            nakshatra = nakshatras.getOrElse(nakshatraIdx) { "" },
            yoga = yogas.getOrElse(yogaIndex) { "" },
            karana = karanas.getOrElse(karanaIndex) { "" },
            sunrise = sunriseStr,
            sunset = sunsetStr
        )
    }

    private data class DashaComputation(
        val periods: List<DashaPeriod>,
        val summary: String,
        val mahaCanonical: String,
        val antarCanonical: String
    )

    private fun calculateVimshottariDasha(
        moonPosition: PlanetPosition,
        birthDate: Date,
        referenceDate: LocalDate,
        dashaLabels: List<String>
    ): DashaComputation {
        val nakSpan = 360.0 / 27.0
        val degInNak = moonPosition.longitude % nakSpan
        val nakshatraIdx = moonPosition.nakshatraIndex
        val initialLordIdx = nakshatraIdx % 9

        val fractionRemaining = (1.0 - (degInNak / nakSpan)).coerceIn(0.01, 0.99)
        val totalYears = DASHA_YEARS[initialLordIdx]
        val remainingYears = totalYears * fractionRemaining

        val periods = mutableListOf<DashaPeriod>()
        val cal = Calendar.getInstance().apply { time = birthDate }
        val sdf = SimpleDateFormat("dd MMM yyyy", Locale.ENGLISH)

        val refCal = Calendar.getInstance().apply {
            set(referenceDate.year, referenceDate.monthValue - 1, referenceDate.dayOfMonth, 12, 0, 0)
        }
        val refTime = refCal.time

        var currentMahaCanon = CANONICAL_DASHA_LORDS[initialLordIdx]
        var currentAntarCanon = CANONICAL_DASHA_LORDS[initialLordIdx]
        var currentSummary = "${dashaLabels.getOrElse(initialLordIdx) { currentMahaCanon }} - ${dashaLabels.getOrElse(initialLordIdx) { currentAntarCanon }}"

        var currLordIdx = initialLordIdx
        for (i in 0 until 9) {
            val lordYears = if (i == 0) remainingYears else DASHA_YEARS[currLordIdx].toDouble()
            val startCal = cal.clone() as Calendar
            val startStr = sdf.format(startCal.time)
            val mahaStartYear = startCal.get(Calendar.YEAR)

            val daysToAdd = (lordYears * 365.25).toInt()
            cal.add(Calendar.DAY_OF_YEAR, daysToAdd)
            val endCal = cal.clone() as Calendar
            val endStr = sdf.format(endCal.time)
            val mahaEndYear = endCal.get(Calendar.YEAR)

            val subPeriods = mutableListOf<DashaPeriod>()
            val subCal = startCal.clone() as Calendar
            var subLordIdx = currLordIdx
            for (j in 0 until 9) {
                val subStartCal = subCal.clone() as Calendar
                val subStart = sdf.format(subStartCal.time)
                val subStartYear = subStartCal.get(Calendar.YEAR)
                val fraction = (DASHA_YEARS[subLordIdx].toDouble() / 120.0)
                val subDays = (lordYears * fraction * 365.25).toInt()
                subCal.add(Calendar.DAY_OF_YEAR, subDays)
                val subEnd = sdf.format(subCal.time)
                val subEndYear = subCal.get(Calendar.YEAR)

                if (refTime >= subStartCal.time && refTime <= subCal.time) {
                    currentMahaCanon = CANONICAL_DASHA_LORDS[currLordIdx]
                    currentAntarCanon = CANONICAL_DASHA_LORDS[subLordIdx]
                    val mahaDisp = dashaLabels.getOrElse(currLordIdx) { currentMahaCanon }
                    val antarDisp = dashaLabels.getOrElse(subLordIdx) { currentAntarCanon }
                    currentSummary = "$mahaDisp - $antarDisp"
                }

                val subCanon = CANONICAL_DASHA_LORDS[subLordIdx]
                subPeriods.add(
                    DashaPeriod(
                        canonicalLord = subCanon,
                        planet = dashaLabels.getOrElse(subLordIdx) { subCanon },
                        startDate = subStart,
                        endDate = subEnd,
                        startYear = subStartYear,
                        endYear = subEndYear
                    )
                )
                subLordIdx = (subLordIdx + 1) % 9
            }

            val mahaCanon = CANONICAL_DASHA_LORDS[currLordIdx]
            periods.add(
                DashaPeriod(
                    canonicalLord = mahaCanon,
                    planet = dashaLabels.getOrElse(currLordIdx) { mahaCanon },
                    startDate = startStr,
                    endDate = endStr,
                    startYear = mahaStartYear,
                    endYear = mahaEndYear,
                    subPeriods = subPeriods
                )
            )

            currLordIdx = (currLordIdx + 1) % 9
        }

        return DashaComputation(periods, currentSummary, currentMahaCanon, currentAntarCanon)
    }

    private fun currentTransitSaturnRashi(referenceDate: LocalDate, ayanamsaType: AyanamsaType): Int {
        val jdNow = calculateJulianDay(referenceDate.year, referenceDate.monthValue, referenceDate.dayOfMonth, 12, 0, 0, 0.0)
        val ayanNow = calculateAyanamsa(jdNow, ayanamsaType)
        val t = (jdNow - 2451545.0) / 36525.0
        val (sunTrop, _) = calculateSunTropical(t)
        val sunRad = toRadians(sunTrop)
        val sunCoords = Triple(cos(sunRad), sin(sunRad), 0.0)
        val sat = calculatePlanetHelio(t, 9.537070, 0.05415060, -0.000368, 2.48446, 0.001936, 49.94432, 1222.49362, 92.43194, 1.95842, 113.71504, -0.28867)
        val satTrop = helioToGeocentric(sat, sunCoords)
        val satSidereal = normalizeDegrees(satTrop - ayanNow)
        return (satSidereal / 30.0).toInt().coerceIn(0, 11)
    }

    private fun calculateYogasAndDoshas(
        planets: List<PlanetPosition>,
        currentSaturnRashi: Int,
        langCode: String,
        contentRepo: ContentRepository
    ): List<YogaDoshaStatus> {
        fun lbl(key: String): String = contentRepo.resolveAstroLabel(langCode, "yogas_doshas", key)

        val pMap = planets.associateBy { it.canonicalName }
        val mars = pMap["Mars"]
        val moon = pMap["Moon"]
        val jupiter = pMap["Jupiter"]
        val rahu = pMap["Rahu"]
        val ketu = pMap["Ketu"]

        // 1. Mangal Dosha
        val marsHouseLagna = mars?.houseNumber ?: 1
        val marsHouseMoon = if (mars != null && moon != null) {
            ((mars.rashiIndex - moon.rashiIndex + 12) % 12) + 1
        } else 1
        val mangalDoshaHouses = setOf(1, 2, 4, 7, 8, 12)
        val hasMangal = marsHouseLagna in mangalDoshaHouses || marsHouseMoon in mangalDoshaHouses
        val isMangalCancelled = mars?.rashiIndex in listOf(0, 7, 9)

        val mangalStatus = YogaDoshaStatus(
            canonicalKey = "Mangal",
            name = lbl("mangal_name"),
            isPresent = hasMangal && !isMangalCancelled,
            isCancelled = hasMangal && isMangalCancelled,
            statusText = when {
                !hasMangal -> lbl("mangal_none")
                isMangalCancelled -> lbl("mangal_cancelled")
                else -> lbl("mangal_present")
            },
            description = when {
                !hasMangal -> lbl("mangal_desc_none").replace("{house}", marsHouseLagna.toString())
                isMangalCancelled -> lbl("mangal_desc_cancelled")
                    .replace("{house}", marsHouseLagna.toString())
                    .replace("{rashi}", mars?.rashiName.orEmpty())
                else -> lbl("mangal_desc_present").replace("{house}", marsHouseLagna.toString())
            },
            remedy = lbl("mangal_remedy")
        )

        // 2. Kaal Sarp Yoga
        var isKaalSarp = false
        if (rahu != null && ketu != null) {
            val minR = min(rahu.longitude, ketu.longitude)
            val maxR = max(rahu.longitude, ketu.longitude)
            val physicalPlanets = planets.filter { it.canonicalName !in listOf("Rahu", "Ketu") }
            val countBetween = physicalPlanets.count { it.longitude in minR..maxR }
            isKaalSarp = countBetween == physicalPlanets.size || countBetween == 0
        }

        val kaalSarpStatus = YogaDoshaStatus(
            canonicalKey = "KaalSarp",
            name = lbl("kaalsarp_name"),
            isPresent = isKaalSarp,
            statusText = if (isKaalSarp) lbl("kaalsarp_present") else lbl("kaalsarp_absent"),
            description = if (isKaalSarp) lbl("kaalsarp_desc_present") else lbl("kaalsarp_desc_absent"),
            remedy = lbl("kaalsarp_remedy")
        )

        // 3. Shani Sade Sati
        val moonRashi = moon?.rashiIndex ?: 0
        val diffRashi = (currentSaturnRashi - moonRashi + 12) % 12
        val isSadeSatiActive = diffRashi in listOf(11, 0, 1)
        val sadeSatiStatus = YogaDoshaStatus(
            canonicalKey = "SadeSati",
            name = lbl("sadesati_name"),
            isPresent = isSadeSatiActive,
            statusText = when (diffRashi) {
                11 -> lbl("sadesati_phase1")
                0 -> lbl("sadesati_phase2")
                1 -> lbl("sadesati_phase3")
                else -> lbl("sadesati_inactive")
            },
            description = if (isSadeSatiActive) lbl("sadesati_desc_active") else lbl("sadesati_desc_inactive"),
            remedy = lbl("sadesati_remedy")
        )

        // 4. Gajakesari Yoga
        val jupFromMoon = if (jupiter != null && moon != null) {
            ((jupiter.rashiIndex - moon.rashiIndex + 12) % 12) + 1
        } else 1
        val isGajakesari = jupFromMoon in listOf(1, 4, 7, 10)
        val gajakesariStatus = YogaDoshaStatus(
            canonicalKey = "Gajakesari",
            name = lbl("gajakesari_name"),
            isPresent = isGajakesari,
            statusText = if (isGajakesari) lbl("gajakesari_present") else lbl("gajakesari_absent"),
            description = if (isGajakesari) {
                lbl("gajakesari_desc_present").replace("{house}", jupFromMoon.toString())
            } else {
                lbl("gajakesari_desc_absent")
            },
            remedy = lbl("gajakesari_remedy")
        )

        // 5. Kemadruma Yoga
        val planetsIn2or12 = planets.filter { it.canonicalName !in listOf("Sun", "Moon", "Rahu", "Ketu") }.any { p ->
            val diff = ((p.rashiIndex - (moon?.rashiIndex ?: 0) + 12) % 12) + 1
            diff == 2 || diff == 12
        }
        val isKemadruma = !planetsIn2or12
        val kemadrumaCancelled = isKemadruma && isGajakesari
        val kemadrumaStatus = YogaDoshaStatus(
            canonicalKey = "Kemadruma",
            name = lbl("kemadruma_name"),
            isPresent = isKemadruma && !kemadrumaCancelled,
            isCancelled = kemadrumaCancelled,
            statusText = when {
                !isKemadruma -> lbl("kemadruma_absent")
                kemadrumaCancelled -> lbl("kemadruma_cancelled")
                else -> lbl("kemadruma_present")
            },
            description = when {
                !isKemadruma -> lbl("kemadruma_desc_absent")
                kemadrumaCancelled -> lbl("kemadruma_desc_cancelled")
                else -> lbl("kemadruma_desc_present")
            },
            remedy = lbl("kemadruma_remedy")
        )

        return listOf(gajakesariStatus, mangalStatus, kaalSarpStatus, sadeSatiStatus, kemadrumaStatus)
    }

    private fun calculateDivisionalCharts(
        lagna: LagnaInfo,
        planets: List<PlanetPosition>,
        rashiLabels: List<String>,
        lagnaDisplayName: String,
        langCode: String,
        contentRepo: ContentRepository
    ): List<DivisionalChart> {
        fun getD9Sign(rashiIdx: Int, degInRashi: Double): Int {
            val part = (degInRashi / (30.0 / 9.0)).toInt().coerceIn(0, 8)
            val startSign = when (rashiIdx % 4) {
                0 -> 0
                1 -> 9
                2 -> 6
                else -> 3
            }
            return (startSign + part) % 12
        }

        fun getD10Sign(rashiIdx: Int, degInRashi: Double): Int {
            val part = (degInRashi / 3.0).toInt().coerceIn(0, 9)
            val isOdd = (rashiIdx % 2 == 0)
            val startSign = if (isOdd) rashiIdx else (rashiIdx + 8) % 12
            return (startSign + part) % 12
        }

        fun getD7Sign(rashiIdx: Int, degInRashi: Double): Int {
            val part = (degInRashi / (30.0 / 7.0)).toInt().coerceIn(0, 6)
            val isOdd = (rashiIdx % 2 == 0)
            val startSign = if (isOdd) rashiIdx else (rashiIdx + 6) % 12
            return (startSign + part) % 12
        }

        val allItems = mutableListOf<Triple<String, String, Pair<Int, Double>>>()
        allItems.add(Triple("Lagna", lagnaDisplayName, Pair(lagna.rashiIndex, lagna.degreeInRashi)))
        planets.forEach {
            allItems.add(Triple(it.canonicalName, it.name, Pair(it.rashiIndex, it.degreeInRashi)))
        }

        val d9Placements = allItems.map { (canon, disp, pos) ->
            val signIdx = getD9Sign(pos.first, pos.second)
            DivisionalPlacement(canon, disp, signIdx, rashiLabels.getOrElse(signIdx) { CANONICAL_RASHIS[signIdx] })
        }

        val d10Placements = allItems.map { (canon, disp, pos) ->
            val signIdx = getD10Sign(pos.first, pos.second)
            DivisionalPlacement(canon, disp, signIdx, rashiLabels.getOrElse(signIdx) { CANONICAL_RASHIS[signIdx] })
        }

        val d7Placements = allItems.map { (canon, disp, pos) ->
            val signIdx = getD7Sign(pos.first, pos.second)
            DivisionalPlacement(canon, disp, signIdx, rashiLabels.getOrElse(signIdx) { CANONICAL_RASHIS[signIdx] })
        }

        return listOf(
            DivisionalChart(
                code = "D9",
                title = contentRepo.resolveAstroLabel(langCode, "divisional_charts", "D9_title"),
                description = contentRepo.resolveAstroLabel(langCode, "divisional_charts", "D9_desc"),
                placements = d9Placements
            ),
            DivisionalChart(
                code = "D10",
                title = contentRepo.resolveAstroLabel(langCode, "divisional_charts", "D10_title"),
                description = contentRepo.resolveAstroLabel(langCode, "divisional_charts", "D10_desc"),
                placements = d10Placements
            ),
            DivisionalChart(
                code = "D7",
                title = contentRepo.resolveAstroLabel(langCode, "divisional_charts", "D7_title"),
                description = contentRepo.resolveAstroLabel(langCode, "divisional_charts", "D7_desc"),
                placements = d7Placements
            )
        )
    }

    private fun calculateCosmicHighlights(
        lagna: LagnaInfo,
        planets: List<PlanetPosition>,
        yogasAndDoshas: List<YogaDoshaStatus>,
        divisionalCharts: List<DivisionalChart>,
        currentMahaLordCanonical: String,
        langCode: String,
        contentRepo: ContentRepository
    ): List<CosmicHighlight> {
        val rashiLords = listOf(
            "Mars", "Venus", "Mercury", "Moon",
            "Sun", "Mercury", "Venus", "Mars",
            "Jupiter", "Saturn", "Saturn", "Jupiter"
        )
        val exaltedSigns = mapOf(
            "Sun" to 0, "Moon" to 1, "Mars" to 9, "Mercury" to 5,
            "Jupiter" to 3, "Venus" to 11, "Saturn" to 6, "Rahu" to 1, "Ketu" to 7
        )
        val debilitatedSigns = mapOf(
            "Sun" to 6, "Moon" to 7, "Mars" to 3, "Mercury" to 11,
            "Jupiter" to 9, "Venus" to 5, "Saturn" to 0, "Rahu" to 7, "Ketu" to 1
        )
        val ownSigns = mapOf(
            "Sun" to setOf(4),
            "Moon" to setOf(3),
            "Mars" to setOf(0, 7),
            "Mercury" to setOf(2, 5),
            "Jupiter" to setOf(8, 11),
            "Venus" to setOf(1, 6),
            "Saturn" to setOf(9, 10)
        )
        val benefics = setOf("Jupiter", "Venus", "Moon", "Mercury")
        val favorableHouses = setOf(1, 2, 4, 5, 7, 9, 10, 11)
        val challengingHouses = setOf(6, 8, 12)

        val pMap = planets.associateBy { it.canonicalName }

        fun houseLord(houseNum: Int): String {
            val signIdx = (lagna.rashiIndex + (houseNum - 1)) % 12
            return rashiLords[signIdx]
        }

        fun planetDignityScore(planetCanon: String): Int {
            val p = pMap[planetCanon] ?: return 12
            var s = 14
            if (exaltedSigns[planetCanon] == p.rashiIndex || ownSigns[planetCanon]?.contains(p.rashiIndex) == true) s += 12
            if (debilitatedSigns[planetCanon] == p.rashiIndex) s -= 9
            if (p.houseNumber in favorableHouses) s += 6
            if (p.houseNumber in challengingHouses) s -= 7
            if (planetCanon == currentMahaLordCanonical) s += 5
            return s
        }

        fun divisionalBonus(code: String, keyPlanet: String): Int {
            val div = divisionalCharts.find { it.code == code } ?: return 0
            val divLagnaSign = div.placements.find { it.canonicalPlanet == "Lagna" }?.signIndex ?: return 0
            val p = div.placements.find { it.canonicalPlanet == keyPlanet } ?: return 0
            val h = ((p.signIndex - divLagnaSign + 12) % 12) + 1
            return when {
                exaltedSigns[keyPlanet] == p.signIndex || ownSigns[keyPlanet]?.contains(p.signIndex) == true -> 10
                h in setOf(1, 4, 5, 7, 9, 10, 11) -> 6
                h in challengingHouses -> -6
                else -> 1
            }
        }

        val gajakesariBonus = if (yogasAndDoshas.find { it.canonicalKey == "Gajakesari" }?.isPresent == true) 8 else 0
        val mangalActive = yogasAndDoshas.find { it.canonicalKey == "Mangal" }?.isPresent == true
        val sadeSatiActive = yogasAndDoshas.find { it.canonicalKey == "SadeSati" }?.isPresent == true
        val kaalSarpActive = yogasAndDoshas.find { it.canonicalKey == "KaalSarp" }?.isPresent == true

        val careerScore = planetDignityScore(houseLord(10)) +
            planetDignityScore("Sun") / 2 +
            divisionalBonus("D10", houseLord(10)) +
            (planets.count { it.houseNumber == 10 && it.canonicalName in benefics } * 7) -
            (if (kaalSarpActive) 4 else 0)

        val wealthScore = planetDignityScore(houseLord(2)) / 2 +
            planetDignityScore(houseLord(11)) / 2 +
            planetDignityScore("Jupiter") / 2 +
            gajakesariBonus +
            (planets.count { it.houseNumber in setOf(2, 11) && it.canonicalName in benefics } * 7) -
            (planets.count { it.houseNumber in setOf(2, 12) && it.canonicalName !in benefics } * 4)

        val marriageScore = planetDignityScore(houseLord(7)) +
            planetDignityScore("Venus") / 2 +
            divisionalBonus("D9", "Venus") +
            (planets.count { it.houseNumber == 7 && it.canonicalName in benefics } * 7) -
            (if (mangalActive) 10 else 0)

        val healthScore = planetDignityScore(houseLord(1)) +
            planetDignityScore("Moon") / 2 -
            (planets.count { it.houseNumber in setOf(6, 8) && it.canonicalName !in benefics } * 6) -
            (if (sadeSatiActive) 8 else 0)

        val educationScore = planetDignityScore(houseLord(5)) +
            planetDignityScore("Mercury") / 2 +
            gajakesariBonus / 2 +
            (planets.count { it.houseNumber == 5 && it.canonicalName in benefics } * 7)

        data class DomainEval(val domainKey: String, val score: Int, val seedOffset: Int)

        val evaluated = listOf(
            DomainEval("domain_career", careerScore, 0),
            DomainEval("domain_wealth", wealthScore, 1),
            DomainEval("domain_marriage", marriageScore, 2),
            DomainEval("domain_health", healthScore, 3),
            DomainEval("domain_education", educationScore, 4)
        ).sortedByDescending { it.score }

        val strongest = evaluated.take(2)
        val challenging = evaluated.takeLast(2).reversed()

        val strongHighlights = strongest.mapIndexed { idx, item ->
            val tplIdx = (lagna.rashiIndex + item.seedOffset + idx) % 7
            val tpl = contentRepo.resolveAstroLabel(langCode, "cosmic_highlights", "strong_$tplIdx")
            val domainLabel = contentRepo.resolveAstroLabel(langCode, "cosmic_highlights", item.domainKey)
            CosmicHighlight(
                text = tpl.replace("{domain}", domainLabel),
                isStrong = true
            )
        }

        val challengeHighlights = challenging.mapIndexed { idx, item ->
            val tplIdx = (lagna.rashiIndex + item.seedOffset + idx) % 7
            val tpl = contentRepo.resolveAstroLabel(langCode, "cosmic_highlights", "challenge_$tplIdx")
            val domainLabel = contentRepo.resolveAstroLabel(langCode, "cosmic_highlights", item.domainKey)
            CosmicHighlight(
                text = tpl.replace("{domain}", domainLabel),
                isStrong = false
            )
        }

        return strongHighlights + challengeHighlights
    }

    fun computeKundali(
        birthDateStr: String,
        birthTimeStr: String,
        isTimeUnknown: Boolean,
        lat: Double,
        lon: Double,
        tzId: String,
        ayanamsaType: AyanamsaType,
        langCode: String,
        contentRepo: ContentRepository,
        person: PersonEntity? = null,
        referenceDate: LocalDate = LocalDate.now()
    ): KundaliData {
        val dateParts = birthDateStr.split("-").mapNotNull { it.toIntOrNull() }
        val year = dateParts.getOrElse(0) { 1995 }
        val month = dateParts.getOrElse(1) { 1 }
        val day = dateParts.getOrElse(2) { 1 }

        val timeParts = if (isTimeUnknown) listOf(12, 0) else birthTimeStr.split(":").mapNotNull { it.toIntOrNull() }
        val hour = timeParts.getOrElse(0) { 12 }
        val minute = timeParts.getOrElse(1) { 0 }

        val tz = TimeZone.getTimeZone(tzId)
        val cal = Calendar.getInstance(tz).apply {
            set(year, month - 1, day, hour, minute, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val tzOffsetHours = tz.getOffset(cal.timeInMillis) / 3600000.0

        val jd = calculateJulianDay(year, month, day, hour, minute, 0, tzOffsetHours)
        val ayanamsa = calculateAyanamsa(jd, ayanamsaType)

        val rashiLabels = contentRepo.resolveAstroStringList(langCode, "rashis").ifEmpty { CANONICAL_RASHIS }
        val nakshatraLabels = contentRepo.resolveAstroStringList(langCode, "nakshatras")
        val dashaLabels = contentRepo.resolveAstroStringList(langCode, "dasha_lords").ifEmpty { CANONICAL_DASHA_LORDS }
        val planetLabels = mapOf(
            "Sun" to contentRepo.getPlanetDisplayName(langCode, "Sun"),
            "Moon" to contentRepo.getPlanetDisplayName(langCode, "Moon"),
            "Mars" to contentRepo.getPlanetDisplayName(langCode, "Mars"),
            "Mercury" to contentRepo.getPlanetDisplayName(langCode, "Mercury"),
            "Jupiter" to contentRepo.getPlanetDisplayName(langCode, "Jupiter"),
            "Venus" to contentRepo.getPlanetDisplayName(langCode, "Venus"),
            "Saturn" to contentRepo.getPlanetDisplayName(langCode, "Saturn"),
            "Rahu" to contentRepo.getPlanetDisplayName(langCode, "Rahu"),
            "Ketu" to contentRepo.getPlanetDisplayName(langCode, "Ketu")
        )
        val lagnaDisplayName = contentRepo.getPlanetDisplayName(langCode, "Lagna")

        val lagna = calculateLagna(jd, lat, lon, ayanamsa, rashiLabels, nakshatraLabels)
        val planets = calculatePlanets(jd, ayanamsa, rashiLabels, nakshatraLabels, dashaLabels, planetLabels)

        planets.forEach { p ->
            val h = ((p.rashiIndex - lagna.rashiIndex + 12) % 12) + 1
            p.houseNumber = h
        }

        val houses = calculateHouseCusps(lagna, rashiLabels)
        val sun = planets.first { it.canonicalName == "Sun" }
        val moon = planets.first { it.canonicalName == "Moon" }

        val panchang = calculatePanchang(cal, sun.longitude, moon.longitude, lat, langCode, contentRepo)
        val dashaComp = calculateVimshottariDasha(moon, cal.time, referenceDate, dashaLabels)
        val saturnTransitRashi = currentTransitSaturnRashi(referenceDate, ayanamsaType)
        val yogasAndDoshas = calculateYogasAndDoshas(planets, saturnTransitRashi, langCode, contentRepo)
        val divisionalCharts = calculateDivisionalCharts(lagna, planets, rashiLabels, lagnaDisplayName, langCode, contentRepo)
        val cosmicHighlights = calculateCosmicHighlights(
            lagna = lagna,
            planets = planets,
            yogasAndDoshas = yogasAndDoshas,
            divisionalCharts = divisionalCharts,
            currentMahaLordCanonical = dashaComp.mahaCanonical,
            langCode = langCode,
            contentRepo = contentRepo
        )

        val moonSignCanonical = CANONICAL_RASHIS[moon.rashiIndex]
        val readings = contentRepo.getSignReadings(langCode, moonSignCanonical)

        val ageYears = person?.let { contentRepo.computeAgeYears(it.birthDate, referenceDate) } ?: 28
        val categoryDeepDives = CategoryReadingEngine.computeAllCategories(
            lagna = lagna,
            planets = planets,
            houses = houses,
            yogasAndDoshas = yogasAndDoshas,
            divisionalCharts = divisionalCharts,
            mahaLordCanonical = dashaComp.mahaCanonical,
            antarLordCanonical = dashaComp.antarCanonical,
            langCode = langCode,
            signCareerReading = readings.career,
            signRelationshipReading = readings.relationships,
            signHealthReading = readings.health,
            contentRepo = contentRepo,
            vimshottariDasha = dashaComp.periods,
            referenceYear = referenceDate.year,
            ageYears = ageYears
        )

        val dailyRemedies = CategoryReadingEngine.computeDailyRemedies(
            lagna = lagna,
            planets = planets,
            houses = houses,
            yogasAndDoshas = yogasAndDoshas,
            divisionalCharts = divisionalCharts,
            mahaLordCanonical = dashaComp.mahaCanonical,
            antarLordCanonical = dashaComp.antarCanonical,
            langCode = langCode,
            categoryDeepDives = categoryDeepDives,
            contentRepo = contentRepo
        )

        val cosmicHighlightBars = CategoryReadingEngine.computeCosmicHighlightBars(
            lagna = lagna,
            planets = planets,
            houses = houses,
            yogasAndDoshas = yogasAndDoshas,
            divisionalCharts = divisionalCharts,
            mahaLordCanonical = dashaComp.mahaCanonical,
            antarLordCanonical = dashaComp.antarCanonical,
            langCode = langCode,
            categoryDeepDives = categoryDeepDives
        )

        val cosmicBlueprint = CategoryReadingEngine.computeCosmicBlueprint(
            lagna = lagna,
            planets = planets,
            mahaLordCanonical = dashaComp.mahaCanonical,
            categoryDeepDives = categoryDeepDives,
            langCode = langCode,
            contentRepo = contentRepo
        )

        val mahaLordLocalized = contentRepo.getPlanetDisplayName(langCode, dashaComp.mahaCanonical)
        val dailyPred = contentRepo.selectPrediction(
            langCode = langCode,
            period = PredictionPeriod.DAILY,
            person = person,
            dashaLordLocalized = mahaLordLocalized,
            dashaLordCanonical = dashaComp.mahaCanonical,
            moonSignLocalized = moon.rashiName,
            moonSignCanonical = moonSignCanonical,
            referenceDate = referenceDate
        )
        val monthlyPred = contentRepo.selectPrediction(
            langCode = langCode,
            period = PredictionPeriod.MONTHLY,
            person = person,
            dashaLordLocalized = mahaLordLocalized,
            dashaLordCanonical = dashaComp.mahaCanonical,
            moonSignLocalized = moon.rashiName,
            moonSignCanonical = moonSignCanonical,
            referenceDate = referenceDate
        )
        val yearlyPred = contentRepo.selectPrediction(
            langCode = langCode,
            period = PredictionPeriod.YEARLY,
            person = person,
            dashaLordLocalized = mahaLordLocalized,
            dashaLordCanonical = dashaComp.mahaCanonical,
            moonSignLocalized = moon.rashiName,
            moonSignCanonical = moonSignCanonical,
            referenceDate = referenceDate
        )
        val lifePred = contentRepo.selectPrediction(
            langCode = langCode,
            period = PredictionPeriod.LIFE,
            person = person,
            dashaLordLocalized = mahaLordLocalized,
            dashaLordCanonical = dashaComp.mahaCanonical,
            moonSignLocalized = moon.rashiName,
            moonSignCanonical = moonSignCanonical,
            referenceDate = referenceDate
        )

        return KundaliData(
            lagna = lagna,
            planets = planets,
            houses = houses,
            panchang = panchang,
            vimshottariDasha = dashaComp.periods,
            currentDashaSummary = dashaComp.summary,
            currentMahaLordCanonical = dashaComp.mahaCanonical,
            currentAntarLordCanonical = dashaComp.antarCanonical,
            yogasAndDoshas = yogasAndDoshas,
            divisionalCharts = divisionalCharts,
            cosmicHighlights = cosmicHighlights,
            cosmicHighlightBars = cosmicHighlightBars,
            cosmicBlueprint = cosmicBlueprint,
            categoryDeepDives = categoryDeepDives,
            dailyRemedies = dailyRemedies,
            personalityReading = readings.personality,
            careerReading = readings.career,
            relationshipReading = readings.relationships,
            healthReading = readings.health,
            luckyElements = readings.luckyElements,
            remedies = readings.remedies,
            dailyPrediction = dailyPred,
            monthlyPrediction = monthlyPred,
            yearlyPrediction = yearlyPred,
            lifePrediction = lifePred
        )
    }

    private data class YearDashaSegment(
        val mahaCanon: String,
        val mahaDisplay: String,
        val antarCanon: String,
        val antarDisplay: String,
        val start: LocalDate,
        val end: LocalDate,
        val rawSub: DashaPeriod
    )

    fun computeYearForecast(
        person: PersonEntity,
        year: Int,
        ayanamsaType: AyanamsaType = AyanamsaType.LAHIRI,
        langCode: String = "en",
        contentRepo: ContentRepository,
        kundali: KundaliData? = null
    ): YearForecast {
        // TODO(v2): true Varshphal annual chart
        val refMidYear = LocalDate.of(year, 7, 1)
        val refKundali = kundali ?: computeKundali(
            birthDateStr = person.birthDate,
            birthTimeStr = person.birthTime,
            isTimeUnknown = person.isTimeUnknown,
            lat = person.latitude,
            lon = person.longitude,
            tzId = person.timezoneId,
            ayanamsaType = ayanamsaType,
            langCode = langCode,
            contentRepo = contentRepo,
            person = person,
            referenceDate = refMidYear
        )
        val isHi = langCode == "hi"
        val yearStart = LocalDate.of(year, 1, 1)
        val yearEnd = LocalDate.of(year, 12, 31)
        val dashaFmt = DateTimeFormatter.ofPattern("dd MMM yyyy", Locale.ENGLISH)

        fun parseDashaDate(text: String, fallbackYear: Int, isEnd: Boolean): LocalDate {
            return try {
                LocalDate.parse(text.trim(), dashaFmt)
            } catch (_: Exception) {
                if (isEnd) LocalDate.of(fallbackYear, 12, 31) else LocalDate.of(fallbackYear, 1, 1)
            }
        }

        val overlappingSegments = mutableListOf<YearDashaSegment>()
        for (maha in refKundali.vimshottariDasha) {
            val mahaCanon = maha.canonicalLord.ifBlank { maha.planet }
            for (sub in maha.subPeriods) {
                val antarCanon = sub.canonicalLord.ifBlank { sub.planet }
                val sDate = parseDashaDate(sub.startDate, sub.startYear, isEnd = false)
                val eDate = parseDashaDate(sub.endDate, sub.endYear, isEnd = true)
                if (!eDate.isBefore(yearStart) && !sDate.isAfter(yearEnd)) {
                    overlappingSegments += YearDashaSegment(
                        mahaCanon = mahaCanon,
                        mahaDisplay = maha.planet.ifBlank { contentRepo.getPlanetDisplayName(langCode, mahaCanon) },
                        antarCanon = antarCanon,
                        antarDisplay = sub.planet.ifBlank { contentRepo.getPlanetDisplayName(langCode, antarCanon) },
                        start = sDate,
                        end = eDate,
                        rawSub = sub
                    )
                }
            }
        }

        if (overlappingSegments.isEmpty()) {
            val fallbackMaha = refKundali.currentMahaLordCanonical
            val fallbackAntar = refKundali.currentAntarLordCanonical
            val fallbackMahaDisp = contentRepo.getPlanetDisplayName(langCode, fallbackMaha)
            val fallbackAntarDisp = contentRepo.getPlanetDisplayName(langCode, fallbackAntar)
            overlappingSegments += YearDashaSegment(
                mahaCanon = fallbackMaha,
                mahaDisplay = fallbackMahaDisp,
                antarCanon = fallbackAntar,
                antarDisplay = fallbackAntarDisp,
                start = yearStart,
                end = yearEnd,
                rawSub = DashaPeriod(
                    canonicalLord = fallbackAntar,
                    planet = fallbackAntarDisp,
                    startDate = "01 Jan $year",
                    endDate = "31 Dec $year",
                    startYear = year,
                    endYear = year
                )
            )
        }

        val midSegment = overlappingSegments.firstOrNull {
            !refMidYear.isBefore(it.start) && !refMidYear.isAfter(it.end)
        } ?: overlappingSegments.first()

        val primaryMahaCanon = midSegment.mahaCanon
        val primaryMahaDisp = midSegment.mahaDisplay
        val antarLordsList = overlappingSegments.map { it.antarDisplay }.distinct()
        val dashaTimeline = overlappingSegments.map { seg ->
            seg.rawSub.copy(planet = "${seg.mahaDisplay} – ${seg.antarDisplay}")
        }

        val monthNames = contentRepo.resolveYear2027StringList(langCode, "months").let {
            if (it.size == 12) it else listOf(
                "January", "February", "March", "April", "May", "June",
                "July", "August", "September", "October", "November", "December"
            )
        }
        val rashiLabels = contentRepo.resolveAstroStringList(langCode, "rashis").let {
            if (it.size == 12) it else CANONICAL_RASHIS
        }
        val natalMoon = refKundali.planets.firstOrNull { it.canonicalName == "Moon" }
        val natalMoonRashi = natalMoon?.rashiIndex ?: 0
        val saturnTransit = currentTransitSaturnRashi(refMidYear, ayanamsaType)
        val saturnRelMoon = ((saturnTransit - natalMoonRashi + 12) % 12) + 1
        val saturnTransitBonus = when (saturnRelMoon) {
            3, 6, 11 -> 5
            12, 1, 2 -> -3
            else -> 1
        }

        fun dashaCategoryAffinity(antarCanon: String, cat: ReadingCategory): Int = when (cat) {
            ReadingCategory.CAREER -> when (antarCanon) {
                "Sun", "Jupiter", "Mercury" -> 6
                "Saturn", "Mars" -> 4
                "Venus" -> 3
                "Rahu" -> 2
                else -> 0
            }
            ReadingCategory.WEALTH -> when (antarCanon) {
                "Jupiter", "Venus", "Mercury" -> 6
                "Moon", "Sun" -> 4
                "Rahu" -> 3
                "Saturn" -> 1
                else -> 0
            }
            ReadingCategory.HEALTH -> when (antarCanon) {
                "Sun", "Jupiter", "Moon" -> 6
                "Venus", "Mercury" -> 4
                "Mars" -> 2
                "Saturn", "Ketu" -> -2
                else -> 0
            }
            ReadingCategory.MARRIAGE -> when (antarCanon) {
                "Venus", "Jupiter", "Moon" -> 6
                "Mercury" -> 4
                "Sun" -> 1
                "Saturn", "Mars", "Ketu" -> -2
                else -> 0
            }
            ReadingCategory.DHARMA -> when (antarCanon) {
                "Jupiter", "Ketu", "Sun" -> 6
                "Moon", "Mercury" -> 4
                "Saturn" -> 3
                "Venus" -> 2
                else -> 1
            }
        }

        fun segmentForMonth(month: Int): YearDashaSegment {
            val midMonth = LocalDate.of(year, month, 15)
            return overlappingSegments.firstOrNull {
                !midMonth.isBefore(it.start) && !midMonth.isAfter(it.end)
            } ?: overlappingSegments.last()
        }

        fun transitFlavorScore(moonTransitIdx: Int, cat: ReadingCategory): Int {
            val fromLagna = ((moonTransitIdx - refKundali.lagna.rashiIndex + 12) % 12) + 1
            return when (cat) {
                ReadingCategory.CAREER -> when (fromLagna) { 10, 11, 1, 2, 6 -> 8; 4, 9 -> 4; 8, 12 -> -5; else -> 1 }
                ReadingCategory.WEALTH -> when (fromLagna) { 2, 11, 5, 9, 4 -> 8; 1, 10 -> 4; 8, 12 -> -4; else -> 1 }
                ReadingCategory.HEALTH -> when (fromLagna) { 1, 5, 9, 11 -> 8; 3, 4 -> 3; 6, 8, 12 -> -4; else -> 2 }
                ReadingCategory.MARRIAGE -> when (fromLagna) { 7, 5, 11, 2, 1 -> 8; 4, 9 -> 4; 6, 8, 12 -> -4; else -> 1 }
                ReadingCategory.DHARMA -> when (fromLagna) { 9, 5, 12, 1 -> 8; 4, 11 -> 4; 6, 8 -> -3; else -> 2 }
            }
        }

        val categoryOrder = listOf(
            ReadingCategory.CAREER,
            ReadingCategory.WEALTH,
            ReadingCategory.HEALTH,
            ReadingCategory.MARRIAGE,
            ReadingCategory.DHARMA
        )

        val monthScoresByCategory = mutableMapOf<ReadingCategory, List<Int>>()
        val yearScoresByCategory = mutableMapOf<ReadingCategory, Int>()
        val categoryGoldenWindow = mutableMapOf<ReadingCategory, MonthRange>()
        val categoryEffortWindow = mutableMapOf<ReadingCategory, MonthRange>()

        fun formatRangeLabel(startM: Int, endM: Int): String {
            val sName = monthNames.getOrElse(startM - 1) { "Jan" }.take(3)
            val eName = monthNames.getOrElse(endM - 1) { "Dec" }.take(3)
            return if (startM == endM) "$sName $year" else "$sName–$eName $year"
        }

        for (cat in categoryOrder) {
            val baseCatScore = refKundali.categoryDeepDives[cat]?.overallScore ?: 70
            val monthlyScores = (1..12).map { m ->
                val seg = segmentForMonth(m)
                val antarDelta = dashaCategoryAffinity(seg.antarCanon, cat)
                val moonIdx = (natalMoonRashi + (year % 12) + (m - 1) * 5) % 12
                val transitDelta = transitFlavorScore(moonIdx, cat)
                (baseCatScore + antarDelta + transitDelta + saturnTransitBonus / 2).coerceIn(32, 96)
            }
            monthScoresByCategory[cat] = monthlyScores
            val avgAntarDelta = overlappingSegments
                .map { dashaCategoryAffinity(it.antarCanon, cat) }
                .average()
                .toInt()
            val windowScore = (baseCatScore + avgAntarDelta + saturnTransitBonus / 2).coerceIn(36, 95)
            yearScoresByCategory[cat] = windowScore

            // Best 2-month corridor for golden window
            var bestStart = 1
            var bestSum = Int.MIN_VALUE
            for (s in 1..11) {
                val pairSum = monthlyScores[s - 1] + monthlyScores[s]
                if (pairSum > bestSum) {
                    bestSum = pairSum
                    bestStart = s
                }
            }
            // Lowest 2-month corridor not overlapping golden window
            var effortStart = if (bestStart <= 6) 9 else 2
            var lowSum = Int.MAX_VALUE
            for (s in 1..11) {
                val overlaps = (s <= bestStart + 1) && (s + 1 >= bestStart)
                if (!overlaps) {
                    val pairSum = monthlyScores[s - 1] + monthlyScores[s]
                    if (pairSum < lowSum) {
                        lowSum = pairSum
                        effortStart = s
                    }
                }
            }

            val catKey = when (cat) {
                ReadingCategory.CAREER -> "CAREER"
                ReadingCategory.WEALTH -> "WEALTH"
                ReadingCategory.HEALTH -> "HEALTH"
                ReadingCategory.MARRIAGE -> "MARRIAGE"
                ReadingCategory.DHARMA -> "DHARMA"
            }
            categoryGoldenWindow[cat] = MonthRange(
                startMonth = bestStart,
                endMonth = bestStart + 1,
                label = formatRangeLabel(bestStart, bestStart + 1),
                reason = contentRepo.resolveYear2027String(langCode, "golden_reasons", catKey)
            )
            categoryEffortWindow[cat] = MonthRange(
                startMonth = effortStart,
                endMonth = effortStart + 1,
                label = formatRangeLabel(effortStart, effortStart + 1),
                reason = contentRepo.resolveYear2027String(langCode, "effort_reasons", catKey)
            )
        }

        // Determine primary cosmic tone planet for 2027 Hero
        val heroPlanetCanon = listOf(
            primaryMahaCanon,
            midSegment.antarCanon,
            "Jupiter", "Sun", "Mercury", "Venus", "Mars", "Saturn", "Moon"
        ).firstOrNull { it in CANONICAL_DASHA_LORDS } ?: "Jupiter"

        val heroHeadline = contentRepo.resolveYear2027String(langCode, "hero_by_planet", heroPlanetCanon, "headline")
            .ifBlank { if (isHi) "उज्ज्वल वर्ष $year" else "Year Ahead $year" }
        val heroGlyph = contentRepo.resolveYear2027String(langCode, "hero_by_planet", heroPlanetCanon, "glyph")
            .ifBlank { CategoryReadingEngine.planetGlyph(heroPlanetCanon) }
        val heroSummary = contentRepo.resolveYear2027String(langCode, "hero_by_planet", heroPlanetCanon, "summary")
            .replace("{maha}", primaryMahaDisp)

        fun meterBucket(score: Int): String = when {
            score >= 80 -> "excellent"
            score >= 60 -> "favorable"
            score >= 40 -> "growing"
            else -> "extra_effort"
        }

        val atAGlanceSpecs = listOf(
            Triple(ReadingCategory.CAREER, "CAREER", "♃" to (if (isHi) "करियर व कर्म" else "Career")),
            Triple(ReadingCategory.WEALTH, "WEALTH", "♀" to (if (isHi) "धन व समृद्धि" else "Wealth")),
            Triple(ReadingCategory.HEALTH, "HEALTH", "☉" to (if (isHi) "स्वास्थ्य व ऊर्जा" else "Health")),
            Triple(ReadingCategory.MARRIAGE, "MARRIAGE", "☽" to (if (isHi) "संबंध व परिवार" else "Relationships")),
            Triple(ReadingCategory.DHARMA, "GROWTH", "♄" to (if (isHi) "अंतर्ज्ञान व विकास" else "Growth"))
        )
        val atAGlanceMeters = atAGlanceSpecs.map { (cat, jsonKey, glyphLabel) ->
            val sc = yearScoresByCategory[cat] ?: 70
            PowerBarMetric(
                label = glyphLabel.second,
                score = sc,
                note = contentRepo.resolveYear2027String(langCode, "at_a_glance_notes", jsonKey, meterBucket(sc)),
                glyph = glyphLabel.first
            )
        }
        val overallScore = yearScoresByCategory.values.average().toInt().coerceIn(38, 95)

        val topGoldenWindows = categoryOrder
            .sortedByDescending { yearScoresByCategory[it] ?: 70 }
            .take(3)
            .mapNotNull { categoryGoldenWindow[it] }
        val topEffortWindows = listOf(
            categoryEffortWindow[ReadingCategory.CAREER],
            categoryEffortWindow[ReadingCategory.HEALTH]
        ).filterNotNull()

        val yearCategoryDeepDives = listOf(
            ReadingCategory.CAREER,
            ReadingCategory.WEALTH,
            ReadingCategory.HEALTH,
            ReadingCategory.MARRIAGE,
            ReadingCategory.DHARMA
        ).map { cat ->
            val sc = yearScoresByCategory[cat] ?: 70
            val catKey = when (cat) {
                ReadingCategory.CAREER -> "CAREER"
                ReadingCategory.WEALTH -> "WEALTH"
                ReadingCategory.MARRIAGE -> "MARRIAGE"
                ReadingCategory.HEALTH -> "HEALTH"
                ReadingCategory.DHARMA -> "DHARMA"
            }
            val bucket = when {
                sc >= 76 -> "high"
                sc >= 58 -> "balanced"
                else -> "effort"
            }
            val chartCode = when (cat) {
                ReadingCategory.CAREER -> "D10"
                ReadingCategory.MARRIAGE -> "D9"
                ReadingCategory.WEALTH, ReadingCategory.HEALTH, ReadingCategory.DHARMA -> "D1"
            }
            val catLabel = when (cat) {
                ReadingCategory.CAREER -> if (isHi) "करियर एवं कार्यक्षेत्र" else "Career & Vocation"
                ReadingCategory.WEALTH -> if (isHi) "धन एवं संपत्ति" else "Wealth & Prosperity"
                ReadingCategory.MARRIAGE -> if (isHi) "विवाह एवं संबंध" else "Relationships & Marriage"
                ReadingCategory.HEALTH -> if (isHi) "स्वास्थ्य एवं प्राण ऊर्जा" else "Health & Vitality"
                ReadingCategory.DHARMA -> if (isHi) "धर्म एवं आंतरिक विकास" else "Dharma & Growth"
            }
            val leadPlanetCanon = when (cat) {
                ReadingCategory.CAREER -> refKundali.planets.firstOrNull { it.canonicalName == "Mercury" || it.canonicalName == "Sun" }?.canonicalName ?: "Sun"
                ReadingCategory.WEALTH -> "Jupiter"
                ReadingCategory.MARRIAGE -> "Venus"
                ReadingCategory.HEALTH -> "Sun"
                ReadingCategory.DHARMA -> "Jupiter"
            }
            val leadPlanetDisp = contentRepo.getPlanetDisplayName(langCode, leadPlanetCanon)
            val gWin = categoryGoldenWindow[cat]!!
            val eWin = categoryEffortWindow[cat]!!
            val rawParas = contentRepo.resolveYear2027StringList(langCode, "category_narratives", catKey, bucket, "paragraphs")
            val formattedParas = rawParas.map { raw ->
                raw.replace("{planet}", leadPlanetDisp)
                    .replace("{maha}", primaryMahaDisp)
                    .replace("{golden_window}", gWin.label)
                    .replace("{effort_window}", eWin.label)
            }
            val remedy2027 = contentRepo.resolveYear2027String(langCode, "category_narratives", catKey, bucket, "remedy")

            YearCategoryDeepDive(
                category = cat,
                categoryLabel = catLabel,
                score = sc,
                chartCode = chartCode,
                goldenWindows = listOf(gWin),
                effortWindows = listOf(eWin),
                paragraphs = formattedParas,
                remedy2027 = remedy2027
            )
        }

        val personSalt = person.birthDate.fold(0) { acc, ch -> (acc * 31 + ch.code) and 0x7FFFFFFF }
        val monthlyForecasts = (1..12).map { monthIdx ->
            val seg = segmentForMonth(monthIdx)
            val mName = monthNames.getOrElse(monthIdx - 1) { "Month $monthIdx" }
            val moonIdx = (natalMoonRashi + (year % 12) + (monthIdx - 1) * 5) % 12
            val moonSignName = rashiLabels.getOrElse(moonIdx) { CANONICAL_RASHIS[moonIdx] }
            val variantIdx = (personSalt + monthIdx + year) % 3

            val flavors = contentRepo.resolveYear2027StringList(langCode, "monthly_templates", seg.antarCanon, "moon_flavors")
            val bodies = contentRepo.resolveYear2027StringList(langCode, "monthly_templates", seg.antarCanon, "body_variants")
            val luckyList = contentRepo.resolveYear2027StringList(langCode, "monthly_templates", seg.antarCanon, "lucky_tags")
            val effortList = contentRepo.resolveYear2027StringList(langCode, "monthly_templates", seg.antarCanon, "effort_tags")

            val flavorBase = flavors.getOrElse(variantIdx) { flavors.firstOrNull().orEmpty() }
            val moonTransitFlavor = if (flavorBase.isNotBlank()) "$moonSignName • $flavorBase" else moonSignName
            val bodyTemplate = bodies.getOrElse(variantIdx) { bodies.firstOrNull().orEmpty() }
            val predictionText = bodyTemplate
                .replace("{month}", mName)
                .replace("{maha}", seg.mahaDisplay)
                .replace("{antar}", seg.antarDisplay)
                .replace("{moon_sign}", moonSignName)
            val luckyFocus = luckyList.getOrElse(variantIdx) { luckyList.firstOrNull().orEmpty() }
            val effortFocus = effortList.getOrElse(variantIdx) { effortList.firstOrNull().orEmpty() }

            MonthForecast(
                monthIndex = monthIdx,
                monthName = mName,
                dashaSubPeriod = "${seg.mahaDisplay} – ${seg.antarDisplay}",
                moonTransitFlavor = moonTransitFlavor,
                prediction = predictionText,
                luckyFocus = luckyFocus,
                effortFocus = effortFocus
            )
        }

        val disclaimerText = contentRepo.resolveYear2027String(langCode, "disclaimer").ifBlank {
            if (isHi) "2027 मार्गदर्शन आपकी जन्म कुंडली और सक्रिय दशा गणना पर आधारित है।"
            else "2027 insights blend your natal D1/D9/D10 structure with Vimshottari Dasha periods active in 2027."
        }

        return YearForecast(
            year = year,
            personName = person.name,
            signatureHeadline = heroHeadline,
            heroGlyph = heroGlyph,
            heroSummary = heroSummary,
            mahaLord = primaryMahaDisp,
            antarLords = antarLordsList,
            dashaTimeline2027 = dashaTimeline,
            overallScore = overallScore,
            atAGlanceMeters = atAGlanceMeters,
            goldenWindows = topGoldenWindows,
            effortWindows = topEffortWindows,
            categoryDeepDives = yearCategoryDeepDives,
            monthlyForecasts = monthlyForecasts,
            remediesSummary = refKundali.dailyRemedies,
            disclaimer = disclaimerText
        )
    }
}
