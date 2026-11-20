package com.aryaleelastudios.kundaliguru.astro

import com.aryaleelastudios.kundaliguru.data.model.*
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.*

object AstroEngine {

    val RASHI_NAMES_EN = listOf(
        "Aries", "Taurus", "Gemini", "Cancer",
        "Leo", "Virgo", "Libra", "Scorpio",
        "Sagittarius", "Capricorn", "Aquarius", "Pisces"
    )

    val RASHI_NAMES_HI = listOf(
        "मेष", "वृषभ", "मिथुन", "कर्क",
        "सिंह", "कन्या", "तुला", "वृश्चिक",
        "धनु", "मकर", "कुंभ", "मीन"
    )

    val NAKSHATRA_NAMES_EN = listOf(
        "Ashwini", "Bharani", "Krittika", "Rohini", "Mrigashira", "Ardra",
        "Punarvasu", "Pushya", "Ashlesha", "Magha", "Purva Phalguni", "Uttara Phalguni",
        "Hasta", "Chitra", "Swati", "Vishakha", "Anuradha", "Jyeshtha",
        "Mula", "Purva Ashadha", "Uttara Ashadha", "Shravana", "Dhanishta", "Shatabhisha",
        "Purva Bhadrapada", "Uttara Bhadrapada", "Revati"
    )

    val NAKSHATRA_NAMES_HI = listOf(
        "अश्विनी", "भरणी", "कृत्तिका", "रोहिणी", "मृगशिरा", "आर्द्रा",
        "पुनर्वसु", "पुष्य", "आश्लेषा", "मघा", "पूर्वाफाल्गुनी", "उत्तराफाल्गुनी",
        "हस्त", "चित्रा", "स्वाती", "विशाखा", "अनुराधा", "ज्येष्ठा",
        "मूल", "पूर्वाषाढ़ा", "उत्तराषाढ़ा", "श्रवण", "धनिष्ठा", "शतभिषा",
        "पूर्वाभाद्रपद", "उत्तराभाद्रपद", "रेवती"
    )

    val DASHA_LORDS_EN = listOf(
        "Ketu", "Venus", "Sun", "Moon", "Mars", "Rahu", "Jupiter", "Saturn", "Mercury"
    )

    val DASHA_LORDS_HI = listOf(
        "केतु", "शुक्र", "सूर्य", "चंद्र", "मंगल", "राहु", "गुरु", "शनि", "बुध"
    )

    val DASHA_YEARS = listOf(7, 20, 6, 10, 7, 18, 16, 19, 17)

    val TITHI_NAMES_EN = listOf(
        "Pratipada", "Dwitiya", "Tritiya", "Chaturthi", "Panchami",
        "Shashti", "Saptami", "Ashtami", "Navami", "Dashami",
        "Ekadashi", "Dwadashi", "Trayodashi", "Chaturdashi", "Purnima",
        "Pratipada", "Dwitiya", "Tritiya", "Chaturthi", "Panchami",
        "Shashti", "Saptami", "Ashtami", "Navami", "Dashami",
        "Ekadashi", "Dwadashi", "Trayodashi", "Chaturdashi", "Amavasya"
    )

    val TITHI_NAMES_HI = listOf(
        "प्रतिपदा", "द्वितीया", "तृतीया", "चतुर्थी", "पंचमी",
        "षष्ठी", "सप्तमी", "अष्टमी", "नवमी", "दशमी",
        "एकादशी", "द्वादशी", "त्रयोदशी", "चतुर्दशी", "पूर्णिमा",
        "प्रतिपदा", "द्वितीया", "तृतीया", "चतुर्थी", "पंचमी",
        "षष्ठी", "सप्तमी", "अष्टमी", "नवमी", "दशमी",
        "एकादशी", "द्वादशी", "त्रयोदशी", "चतुर्दशी", "अमावस्या"
    )

    val VARA_NAMES_EN = listOf(
        "Sunday", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday"
    )

    val VARA_NAMES_HI = listOf(
        "रविवार", "सोमवार", "मंगलवार", "बुधवार", "गुरुवार", "शुक्रवार", "शनिवार"
    )

    val YOGA_NAMES_EN = listOf(
        "Vishkambha", "Priti", "Ayushman", "Saubhagya", "Shobhana", "Atiganda",
        "Sukarma", "Dhriti", "Shula", "Ganda", "Vriddhi", "Dhruva",
        "Vyaghata", "Harshana", "Vajra", "Siddhi", "Vyatipata", "Variyan",
        "Parigha", "Shiva", "Siddha", "Sadhya", "Shubha", "Shukla",
        "Brahma", "Indra", "Vaidhriti"
    )

    val YOGA_NAMES_HI = listOf(
        "विष्कम्भ", "प्रीति", "आयुष्मान्", "सौभाग्य", "शोभन", "अतिगण्ड",
        "सुकर्मा", "धृति", "शूल", "गण्ड", "वृद्धि", "ध्रुव",
        "व्याघात", "हर्षण", "वज्र", "सिद्धि", "व्यतीपात", "वरीयान्",
        "परिघ", "शिव", "सिद्ध", "साध्य", "शुभ", "शुक्ल",
        "ब्रह्म", "इन्द्र", "वैधृति"
    )

    val KARANA_NAMES_EN = listOf(
        "Bava", "Balava", "Kaulava", "Taitila", "Gara", "Vanija", "Vishti (Bhadra)",
        "Shakuni", "Chatushpada", "Naga", "Kinstughna"
    )

    val KARANA_NAMES_HI = listOf(
        "बव", "बालव", "कौलव", "तैतिल", "गर", "वणिज", "विष्टि (भद्रा)",
        "शकुनि", "चतुष्पाद", "नाग", "किंस्तुघ्न"
    )

    private fun normalizeDegrees(deg: Double): Double {
        var d = deg % 360.0
        if (d < 0.0) d += 360.0
        return d
    }

    private fun toRadians(deg: Double): Double = deg * Math.PI / 180.0
    private fun toDegrees(rad: Double): Double = rad * 180.0 / Math.PI

    fun calculateJulianDay(year: Int, month: Int, day: Int, hour: Int, minute: Int, second: Int, tzOffsetHours: Double): Double {
        val decimalUtcHour = (hour + minute / 60.0 + second / 3600.0) - tzOffsetHours
        var y = year
        var m = month
        var d = day.toDouble() + decimalUtcHour / 24.0

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
        return Pair(trueLong, 0.9856) // ~0.9856 deg/day
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

        var l = lp +
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

        return Pair(normalizeDegrees(l), 13.176) // ~13.2 deg/day
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

        // Solve Kepler
        var eAnom = mRad
        for (step in 0..5) {
            val delta = (eAnom - e * sin(eAnom) - mRad) / (1.0 - e * cos(eAnom))
            eAnom -= delta
            if (abs(delta) < 1e-6) break
        }

        val xOrb = a * (cos(eAnom) - e)
        val yOrb = a * sqrt(1.0 - e * e) * sin(eAnom)

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
        // Sun coords in heliocentric are (-Xsun_geo, -Ysun_geo, 0)
        // Vector from Earth to Planet = P_helio - Earth_helio = P_helio + Sun_geo
        val xGeo = planetCoords.first + sunCoords.first
        val yGeo = planetCoords.second + sunCoords.second
        var lambda = toDegrees(atan2(yGeo, xGeo))
        return normalizeDegrees(lambda)
    }

    private fun calculatePlanets(jd: Double, ayanamsa: Double): List<PlanetPosition> {
        val t = (jd - 2451545.0) / 36525.0
        val dt = 0.001 // ~36 minutes for retrograde velocity check

        val (sunTropical, _) = calculateSunTropical(t)
        val (sunTrop2, _) = calculateSunTropical(t + dt)

        val (moonTropical, _) = calculateMoonTropical(t)
        val (moonTrop2, _) = calculateMoonTropical(t + dt)

        // Earth-Sun geocentric vector
        val sunRad = toRadians(sunTropical)
        val sunCoords1 = Triple(cos(sunRad), sin(sunRad), 0.0)
        val sunRad2 = toRadians(sunTrop2)
        val sunCoords2 = Triple(cos(sunRad2), sin(sunRad2), 0.0)

        // Mars
        val mars1 = calculatePlanetHelio(t, 1.523662, 0.0934123, 0.000092, 1.85061, -0.000675, 355.45332, 19140.30268, 336.04084, 1.84104, 49.5574, -0.29257)
        val mars2 = calculatePlanetHelio(t + dt, 1.523662, 0.0934123, 0.000092, 1.85061, -0.000675, 355.45332, 19140.30268, 336.04084, 1.84104, 49.5574, -0.29257)
        val marsTrop1 = helioToGeocentric(mars1, sunCoords1)
        val marsTrop2 = helioToGeocentric(mars2, sunCoords2)

        // Mercury
        val merc1 = calculatePlanetHelio(t, 0.387098, 0.2056306, 0.000025, 7.00487, -0.005947, 252.25084, 149472.67411, 77.45645, 1.55648, 48.3313, -0.12534)
        val merc2 = calculatePlanetHelio(t + dt, 0.387098, 0.2056306, 0.000025, 7.00487, -0.005947, 252.25084, 149472.67411, 77.45645, 1.55648, 48.3313, -0.12534)
        val mercTrop1 = helioToGeocentric(merc1, sunCoords1)
        val mercTrop2 = helioToGeocentric(merc2, sunCoords2)

        // Jupiter
        val jup1 = calculatePlanetHelio(t, 5.203363, 0.04839266, -0.000128, 1.30530, -0.004156, 34.40438, 3034.74612, 14.75385, 1.61657, 100.55615, 0.21381)
        val jup2 = calculatePlanetHelio(t + dt, 5.203363, 0.04839266, -0.000128, 1.30530, -0.004156, 34.40438, 3034.74612, 14.75385, 1.61657, 100.55615, 0.21381)
        val jupTrop1 = helioToGeocentric(jup1, sunCoords1)
        val jupTrop2 = helioToGeocentric(jup2, sunCoords2)

        // Venus
        val ven1 = calculatePlanetHelio(t, 0.723332, 0.00677323, -0.000049, 3.39471, -0.000789, 181.97973, 58517.81538, 131.57294, 1.40222, 76.68069, -0.2777)
        val ven2 = calculatePlanetHelio(t + dt, 0.723332, 0.00677323, -0.000049, 3.39471, -0.000789, 181.97973, 58517.81538, 131.57294, 1.40222, 76.68069, -0.2777)
        val venTrop1 = helioToGeocentric(ven1, sunCoords1)
        val venTrop2 = helioToGeocentric(ven2, sunCoords2)

        // Saturn
        val sat1 = calculatePlanetHelio(t, 9.537070, 0.05415060, -0.000368, 2.48446, 0.001936, 49.94432, 1222.49362, 92.43194, 1.95842, 113.71504, -0.28867)
        val sat2 = calculatePlanetHelio(t + dt, 9.537070, 0.05415060, -0.000368, 2.48446, 0.001936, 49.94432, 1222.49362, 92.43194, 1.95842, 113.71504, -0.28867)
        val satTrop1 = helioToGeocentric(sat1, sunCoords1)
        val satTrop2 = helioToGeocentric(sat2, sunCoords2)

        // Rahu (Mean lunar ascending node)
        val rahuTrop1 = normalizeDegrees(125.04452 - 1934.136261 * t + 0.0020708 * t * t)
        val ketuTrop1 = normalizeDegrees(rahuTrop1 + 180.0)

        fun isRetrogradeMotion(pos1: Double, pos2: Double): Boolean {
            var diff = pos2 - pos1
            if (diff > 180.0) diff -= 360.0
            if (diff < -180.0) diff += 360.0
            return diff < 0.0
        }

        val rawPlanets = listOf(
            RawPlanet("Sun", "सूर्य", "☉", sunTropical, false),
            RawPlanet("Moon", "चंद्र", "☽", moonTropical, false),
            RawPlanet("Mars", "मंगल", "♂", marsTrop1, isRetrogradeMotion(marsTrop1, marsTrop2)),
            RawPlanet("Mercury", "बुध", "☿", mercTrop1, isRetrogradeMotion(mercTrop1, mercTrop2)),
            RawPlanet("Jupiter", "गुरु", "♃", jupTrop1, isRetrogradeMotion(jupTrop1, jupTrop2)),
            RawPlanet("Venus", "शुक्र", "♀", venTrop1, isRetrogradeMotion(venTrop1, venTrop2)),
            RawPlanet("Saturn", "शनि", "♄", satTrop1, isRetrogradeMotion(satTrop1, satTrop2)),
            RawPlanet("Rahu", "राहु", "☊", rahuTrop1, true),
            RawPlanet("Ketu", "केतु", "☋", ketuTrop1, true)
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
            val fmt = "%02d° %02d' %02d\"".format(d, m, s)

            PlanetPosition(
                nameEn = raw.nameEn,
                nameHi = raw.nameHi,
                glyph = raw.glyph,
                longitude = siderealLong,
                rashiIndex = rashiIdx,
                rashiNameEn = RASHI_NAMES_EN[rashiIdx],
                rashiNameHi = RASHI_NAMES_HI[rashiIdx],
                degreeInRashi = degInRashi,
                formattedDegree = fmt,
                isRetrograde = raw.isRetrograde,
                nakshatraIndex = nakshatraIdx,
                nakshatraNameEn = NAKSHATRA_NAMES_EN[nakshatraIdx],
                nakshatraNameHi = NAKSHATRA_NAMES_HI[nakshatraIdx],
                nakshatraLordEn = DASHA_LORDS_EN[lordIdx],
                nakshatraLordHi = DASHA_LORDS_HI[lordIdx],
                pada = pada
            )
        }
    }

    private data class RawPlanet(
        val nameEn: String,
        val nameHi: String,
        val glyph: String,
        val tropicalLong: Double,
        val isRetrograde: Boolean
    )

    fun calculateLagna(jd: Double, lat: Double, lon: Double, ayanamsa: Double): LagnaInfo {
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
        val fmt = "%02d° %02d' %02d\"".format(d, m, s)

        return LagnaInfo(
            longitude = ascSidereal,
            rashiIndex = rashiIdx,
            rashiNameEn = RASHI_NAMES_EN[rashiIdx],
            rashiNameHi = RASHI_NAMES_HI[rashiIdx],
            degreeInRashi = degInRashi,
            formattedDegree = fmt,
            nakshatraIndex = nakshatraIdx,
            nakshatraNameEn = NAKSHATRA_NAMES_EN[nakshatraIdx],
            nakshatraNameHi = NAKSHATRA_NAMES_HI[nakshatraIdx],
            pada = pada
        )
    }

    private fun calculateHouseCusps(lagna: LagnaInfo): List<HouseCusp> {
        return (1..12).map { houseNum ->
            val signIdx = (lagna.rashiIndex + (houseNum - 1)) % 12
            val deg = lagna.degreeInRashi
            val d = deg.toInt()
            val m = ((deg - d) * 60).toInt()
            val s = ((((deg - d) * 60) - m) * 60).toInt()
            val fmt = "%02d° %02d' %02d\"".format(d, m, s)
            HouseCusp(
                houseNumber = houseNum,
                rashiIndex = signIdx,
                rashiNameEn = RASHI_NAMES_EN[signIdx],
                rashiNameHi = RASHI_NAMES_HI[signIdx],
                degree = deg,
                formattedDegree = fmt
            )
        }
    }

    private fun calculatePanchang(
        birthCalendar: Calendar,
        sunLong: Double,
        moonLong: Double,
        lat: Double
    ): PanchangInfo {
        // Tithi: (Moon - Sun) mod 360 / 12
        var tithiDiff = moonLong - sunLong
        if (tithiDiff < 0) tithiDiff += 360.0
        val tithiIndex = (tithiDiff / 12.0).toInt().coerceIn(0, 29)
        val isShukla = tithiIndex < 15

        // Vara
        val dayOfWeek = birthCalendar.get(Calendar.DAY_OF_WEEK) - 1 // 0=Sunday
        val varaEn = VARA_NAMES_EN[dayOfWeek]
        val varaHi = VARA_NAMES_HI[dayOfWeek]

        // Yoga
        val yogaSum = normalizeDegrees(sunLong + moonLong)
        val yogaIndex = (yogaSum / (360.0 / 27.0)).toInt().coerceIn(0, 26)

        // Karana
        val karanaTotal = (tithiDiff / 6.0).toInt().coerceIn(0, 59)
        val karanaIndex = when {
            karanaTotal == 0 -> 10 // Kinstughna
            karanaTotal >= 57 -> when (karanaTotal) {
                57 -> 7 // Shakuni
                58 -> 8 // Chatushpada
                else -> 9 // Naga
            }
            else -> (karanaTotal - 1) % 7
        }

        // Nakshatra of Moon
        val nakshatraIdx = (moonLong / (360.0 / 27.0)).toInt().coerceIn(0, 26)

        // Approximate sunrise / sunset based on declination of sun
        val dayOfYear = birthCalendar.get(Calendar.DAY_OF_YEAR)
        val declination = 23.45 * sin(toRadians(360.0 / 365.0 * (dayOfYear - 81)))
        val hourAngleArg = -tan(toRadians(lat)) * tan(toRadians(declination))
        val hourAngle = if (hourAngleArg in -1.0..1.0) toDegrees(acos(hourAngleArg)) else 90.0
        val halfDayHours = hourAngle / 15.0

        val riseHour = (12.0 - halfDayHours).toInt()
        val riseMin = (((12.0 - halfDayHours) - riseHour) * 60).toInt().coerceIn(0, 59)
        val setHour = (12.0 + halfDayHours).toInt()
        val setMin = (((12.0 + halfDayHours) - setHour) * 60).toInt().coerceIn(0, 59)

        val sunriseStr = "%02d:%02d AM".format(riseHour, riseMin)
        val sunsetStr = "%02d:%02d PM".format(if (setHour > 12) setHour - 12 else setHour, setMin)

        return PanchangInfo(
            tithiEn = TITHI_NAMES_EN[tithiIndex],
            tithiHi = TITHI_NAMES_HI[tithiIndex],
            pakshaEn = if (isShukla) "Shukla Paksha" else "Krishna Paksha",
            pakshaHi = if (isShukla) "शुक्ल पक्ष" else "कृष्ण पक्ष",
            varaEn = varaEn,
            varaHi = varaHi,
            nakshatraEn = NAKSHATRA_NAMES_EN[nakshatraIdx],
            nakshatraHi = NAKSHATRA_NAMES_HI[nakshatraIdx],
            yogaEn = YOGA_NAMES_EN[yogaIndex],
            yogaHi = YOGA_NAMES_HI[yogaIndex],
            karanaEn = KARANA_NAMES_EN[karanaIndex],
            karanaHi = KARANA_NAMES_HI[karanaIndex],
            sunrise = sunriseStr,
            sunset = sunsetStr
        )
    }

    private fun calculateVimshottariDasha(
        moonPosition: PlanetPosition,
        birthDate: Date
    ): Pair<List<DashaPeriod>, Pair<String, String>> {
        val nakSpan = 360.0 / 27.0
        val degInNak = moonPosition.longitude % nakSpan
        val nakshatraIdx = moonPosition.nakshatraIndex
        val initialLordIdx = nakshatraIdx % 9

        // Fraction elapsed in nakshatra
        val fractionRemaining = (1.0 - (degInNak / nakSpan)).coerceIn(0.01, 0.99)
        val totalYears = DASHA_YEARS[initialLordIdx]
        val remainingYears = totalYears * fractionRemaining

        val periods = mutableListOf<DashaPeriod>()
        val cal = Calendar.getInstance().apply { time = birthDate }
        val sdf = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())

        val nowCal = Calendar.getInstance()
        var currentDashaEn = "Jupiter-Saturn"
        var currentDashaHi = "गुरु-शनि"

        var currLordIdx = initialLordIdx
        for (i in 0 until 9) {
            val lordYears = if (i == 0) remainingYears else DASHA_YEARS[currLordIdx].toDouble()
            val startCal = cal.clone() as Calendar
            val startStr = sdf.format(startCal.time)

            val daysToAdd = (lordYears * 365.25).toInt()
            cal.add(Calendar.DAY_OF_YEAR, daysToAdd)
            val endCal = cal.clone() as Calendar
            val endStr = sdf.format(endCal.time)

            // Sub-periods (Antar Dashas)
            val subPeriods = mutableListOf<DashaPeriod>()
            val subCal = startCal.clone() as Calendar
            var subLordIdx = currLordIdx
            for (j in 0 until 9) {
                val subStart = sdf.format(subCal.time)
                val fraction = (DASHA_YEARS[subLordIdx].toDouble() / 120.0)
                val subDays = (lordYears * fraction * 365.25).toInt()
                val subStartCal = subCal.clone() as Calendar
                subCal.add(Calendar.DAY_OF_YEAR, subDays)
                val subEnd = sdf.format(subCal.time)

                if (nowCal.time >= subStartCal.time && nowCal.time <= subCal.time) {
                    currentDashaEn = "${DASHA_LORDS_EN[currLordIdx]} - ${DASHA_LORDS_EN[subLordIdx]}"
                    currentDashaHi = "${DASHA_LORDS_HI[currLordIdx]} - ${DASHA_LORDS_HI[subLordIdx]}"
                }

                subPeriods.add(
                    DashaPeriod(
                        planetEn = DASHA_LORDS_EN[subLordIdx],
                        planetHi = DASHA_LORDS_HI[subLordIdx],
                        startDate = subStart,
                        endDate = subEnd
                    )
                )
                subLordIdx = (subLordIdx + 1) % 9
            }

            periods.add(
                DashaPeriod(
                    planetEn = DASHA_LORDS_EN[currLordIdx],
                    planetHi = DASHA_LORDS_HI[currLordIdx],
                    startDate = startStr,
                    endDate = endStr,
                    subPeriods = subPeriods
                )
            )

            currLordIdx = (currLordIdx + 1) % 9
        }

        return Pair(periods, Pair(currentDashaEn, currentDashaHi))
    }

    private fun calculateYogasAndDoshas(
        lagna: LagnaInfo,
        planets: List<PlanetPosition>
    ): List<YogaDoshaStatus> {
        val pMap = planets.associateBy { it.nameEn }
        val mars = pMap["Mars"]
        val moon = pMap["Moon"]
        val jupiter = pMap["Jupiter"]
        val saturn = pMap["Saturn"]
        val rahu = pMap["Rahu"]
        val ketu = pMap["Ketu"]

        // 1. Mangal Dosha: Mars in 1, 2, 4, 7, 8, 12 from Lagna or Moon
        val marsHouseLagna = mars?.houseNumber ?: 1
        val marsHouseMoon = if (mars != null && moon != null) {
            ((mars.rashiIndex - moon.rashiIndex + 12) % 12) + 1
        } else 1
        val mangalDoshaHouses = setOf(1, 2, 4, 7, 8, 12)
        val hasMangalLagna = marsHouseLagna in mangalDoshaHouses
        val hasMangalMoon = marsHouseMoon in mangalDoshaHouses
        val isManglik = hasMangalLagna || hasMangalMoon
        val isCancelled = mars?.rashiIndex in listOf(0, 7, 9) // Aries, Scorpio, Capricorn (exalted/own sign)

        val mangalStatus = YogaDoshaStatus(
            nameEn = "Mangal (Kuja) Dosha",
            nameHi = "मांगलिक दोष",
            isPresent = isManglik && !isCancelled,
            statusTextEn = when {
                !isManglik -> "Not Present (Auspicious)"
                isCancelled -> "Cancelled / Neutralized (Bhanga)"
                else -> "Present (Mild to Moderate)"
            },
            statusTextHi = when {
                !isManglik -> "दोष नहीं है (शुभ)"
                isCancelled -> "दोष भंग (प्रभावहीन)"
                else -> "मांगलिक दोष उपस्थित"
            },
            descriptionEn = if (isManglik) {
                if (isCancelled) "Mars resides in House $marsHouseLagna, but its placement in ${mars?.rashiNameEn} neutralizes its severe afflictions through auspicious dignity."
                else "Mars occupies House $marsHouseLagna from Lagna, indicating strong assertiveness, dynamic passion, and energy requiring mindful patience in matrimony."
            } else "Mars is placed in House $marsHouseLagna, completely free from traditional Manglik afflictions.",
            descriptionHi = if (isManglik) {
                if (isCancelled) "मंगल लग्न से $marsHouseLagna भाव में है, किंतु ${mars?.rashiNameHi} राशि में स्थित होने के कारण दोष का स्वतः परिहार हो जाता है।"
                else "मंगल लग्न से $marsHouseLagna भाव में स्थित है, जो ऊर्जा और उत्साह का सूचक है। वैवाहिक जीवन में सौहार्द और परस्पर समझ आवश्यक है।"
            } else "लग्न अथवा चंद्र से मांगलिक दोष के भावों में मंगल स्थित नहीं है, कुंडली सर्वथा दोषमुक्त है।",
            remedyEn = "Chant Hanuman Chalisa, respect marital partners, and perform Tuesdays charity of red lentils or jaggery.",
            remedyHi = "नित्य हनुमान चालीसा का पाठ करें, मंगलवार को लाल मसूर अथवा गुड़ का दान करें और परस्पर संवाद में मधुरता रखें।"
        )

        // 2. Kaal Sarp Dosha: All 7 physical planets hemmed between Rahu and Ketu
        var isKaalSarp = false
        if (rahu != null && ketu != null) {
            val rLong = rahu.longitude
            val kLong = ketu.longitude
            val minR = min(rLong, kLong)
            val maxR = max(rLong, kLong)

            val physicalPlanets = planets.filter { it.nameEn !in listOf("Rahu", "Ketu") }
            val countBetween = physicalPlanets.count { it.longitude in minR..maxR }
            isKaalSarp = countBetween == physicalPlanets.size || countBetween == 0
        }

        val kaalSarpStatus = YogaDoshaStatus(
            nameEn = "Kaal Sarp Yoga",
            nameHi = "कालसर्प योग",
            isPresent = isKaalSarp,
            statusTextEn = if (isKaalSarp) "Present (Formative Axis)" else "Absent (Open Cosmic Flow)",
            statusTextHi = if (isKaalSarp) "उपस्थित (संघर्ष के बाद विजय)" else "अनुपस्थित (मुक्त ब्रह्मांडीय प्रवाह)",
            descriptionEn = if (isKaalSarp) "All classical planets are positioned along the karmic nodal axis of Rahu-Ketu, bestowing deep spiritual transformation and eventual triumph after early perseverance."
            else "Planets are distributed freely across the zodiac without nodal confinement, ensuring smooth organic progress.",
            descriptionHi = if (isKaalSarp) "सभी प्रमुख ग्रह राहु-केतु अक्ष के भीतर स्थित हैं, जो आरंभिक परिश्रम के पश्चात उच्च आध्यात्मिक विकास और अप्रत्याशित सफलता का योग बनाते हैं।"
            else "ग्रह राहु-केतु के बंधन से मुक्त हैं, जिससे जीवन में स्वावलंबन और सहज उन्नति के मार्ग प्रशस्त रहते हैं।",
            remedyEn = "Worship Lord Shiva with Mahamrityunjaya Japa, chant Om Namah Shivaya, and visit Jyotirlinga temples when possible.",
            remedyHi = "भगवान शिव का जलाभिषेक करें, महामृत्युंजय मंत्र का जप करें और पक्षियों तथा मछलियों को दाना दें।"
        )

        // 3. Sade Sati: Saturn transit relative to natal Moon sign
        // Approximate current transit Saturn (in Aquarius / Pisces)
        val currentSaturnRashi = 10 // Aquarius
        val moonRashi = moon?.rashiIndex ?: 0
        val diffRashi = (currentSaturnRashi - moonRashi + 12) % 12
        val sadeSatiPhase = when (diffRashi) {
            11 -> "First Phase (Rising - Dhaiya)"
            0 -> "Second Phase (Peak - Core)"
            1 -> "Third Phase (Setting - Relief)"
            else -> "Not Active"
        }
        val isSadeSatiActive = diffRashi in listOf(11, 0, 1)

        val sadeSatiStatus = YogaDoshaStatus(
            nameEn = "Shani Sade Sati",
            nameHi = "शनि साढ़े साती",
            isPresent = isSadeSatiActive,
            statusTextEn = if (isSadeSatiActive) sadeSatiPhase else "Inactive (Free Period)",
            statusTextHi = when (diffRashi) {
                11 -> "प्रथम चरण (प्रवेश काल)"
                0 -> "द्वितीय चरण (शिखर काल)"
                1 -> "तृतीय चरण (उतरती साढ़े साती)"
                else -> "वर्तमान में साढ़े साती नहीं है"
            },
            descriptionEn = if (isSadeSatiActive) "Saturn's 7.5-year transit currently touches your lunar sphere, cultivating unmatched discipline, humility, and purifying resilience."
            else "Your natal Moon is currently outside Saturn's transit proximity, supporting steady mental ease and regular progress.",
            descriptionHi = if (isSadeSatiActive) "शनिदेव का गोचर चंद्र राशि के समीप है, जो आत्म-अनुशासन, धैर्य और कर्म की शुद्धि द्वारा स्थायी सफलता का निर्माण करता है।"
            else "वर्तमान में शनि साढ़े साती का प्रभाव नहीं है, मानसिक शांति और सहज प्रगति का उत्तम समय है।",
            remedyEn = "Light a sesame or mustard oil lamp under a Peepal tree on Saturday evenings; serve elders and humble workers.",
            remedyHi = "शनिवार सायंकाल पीपल वृक्ष के नीचे सरसों के तेल का दीपक प्रज्वलित करें और असहायों की सेवा करें।"
        )

        // 4. Gajakesari Yoga: Jupiter in Kendra (1, 4, 7, 10) from Moon
        val jupFromMoon = if (jupiter != null && moon != null) {
            ((jupiter.rashiIndex - moon.rashiIndex + 12) % 12) + 1
        } else 1
        val isGajakesari = jupFromMoon in listOf(1, 4, 7, 10)

        val gajakesariStatus = YogaDoshaStatus(
            nameEn = "Gajakesari Yoga",
            nameHi = "गजकेसरी योग",
            isPresent = isGajakesari,
            statusTextEn = if (isGajakesari) "Formed (Highly Auspicious)" else "Not Formed",
            statusTextHi = if (isGajakesari) "निर्मित (अत्यंत शुभ एवं राजयोग)" else "निर्मित नहीं है",
            descriptionEn = if (isGajakesari) "Jupiter occupies Kendra House $jupFromMoon from Moon, conferring wisdom, royal nobility, eloquence, and enduring reputation."
            else "Jupiter is not positioned in an angular Kendra from Moon, but contributes grace through other benign aspects.",
            descriptionHi = if (isGajakesari) "चंद्रमा से केंद्र (भाव $jupFromMoon) में देवगुरु बृहस्पति स्थित हैं, जो मान-सम्मान, विवेक, समृद्धि और अपार प्रतिष्ठा का महान राजयोग बनाते हैं।"
            else "चंद्रमा से केंद्र में गुरु की स्थिति नहीं है, तथापि गुरु का शुभ प्रभाव अन्य भावों से प्राप्त होता है।",
            remedyEn = "Wear yellow clothes on Thursdays, respect teachers and mentors, and meditate on Brihaspati mantra.",
            remedyHi = "गुरुवार को पीले वस्त्र धारण करें, गुरुजनों का आशीर्वाद लें और ॐ बृहस्पतये नमः का जप करें।"
        )

        // 5. Kemadruma Yoga: No planets in 2nd and 12th from Moon
        val planetsIn2or12 = planets.filter { it.nameEn !in listOf("Sun", "Moon", "Rahu", "Ketu") }.any { p ->
            val diff = ((p.rashiIndex - (moon?.rashiIndex ?: 0) + 12) % 12) + 1
            diff == 2 || diff == 12
        }
        val isKemadruma = !planetsIn2or12
        // Cancelled if Jupiter or Venus is in Kendra from Lagna or Moon
        val kemadrumaCancelled = isKemadruma && isGajakesari

        val kemadrumaStatus = YogaDoshaStatus(
            nameEn = "Kemadruma Yoga",
            nameHi = "केमद्रुम योग",
            isPresent = isKemadruma && !kemadrumaCancelled,
            statusTextEn = when {
                !isKemadruma -> "Absent (Moon Well Supported)"
                kemadrumaCancelled -> "Cancelled (Bhanga by Benefics)"
                else -> "Present (Inner Self-Reliance)"
            },
            statusTextHi = when {
                !isKemadruma -> "अनुपस्थित (चंद्रमा सुरक्षित)"
                kemadrumaCancelled -> "दोष भंग (शुभ ग्रहों द्वारा निरस्त)"
                else -> "उपस्थित (आत्म-निर्भरता का योग)"
            },
            descriptionEn = if (isKemadruma) {
                if (kemadrumaCancelled) "Flanked houses from Moon are empty, but powerful Kendra benefics cancel its isolating tendencies into creative autonomy."
                else "Moon stands independent without adjacent planetary companions, calling for cultivating steady emotional self-anchoring."
            } else "Benefic planetary energies flank your Moon in the 2nd or 12th houses, supporting emotional contentment and financial flow.",
            descriptionHi = if (isKemadruma) {
                if (kemadrumaCancelled) "चंद्रमा के आगे-पीछे ग्रह न होने पर भी केंद्र में शुभ ग्रहों के कारण केमद्रुम योग भंग होकर स्वावलंबन देता है।"
                else "चंद्रमा के द्वितीय और द्वादश भाव रिक्त हैं, जो व्यक्ति को स्वयं के बल पर आगे बढ़ने की अद्वितीय क्षमता प्रदान करते हैं।"
            } else "चंद्रमा के दोनों ओर ग्रह स्थित हैं, जिससे मानसिक संतोष और पारिवारिक सहयोग प्रचुर मात्रा में मिलता है।",
            remedyEn = "Chant Shiva mantra on Mondays, worship Goddess Lakshmi with white sweets, and keep silver items.",
            remedyHi = "सोमवार को शिवलिंग पर कच्चा दूध अर्पित करें, माता महालक्ष्मी की उपासना करें और माता का आदर करें।"
        )

        return listOf(gajakesariStatus, mangalStatus, kaalSarpStatus, sadeSatiStatus, kemadrumaStatus)
    }

    private fun calculateDivisionalCharts(
        lagna: LagnaInfo,
        planets: List<PlanetPosition>
    ): List<DivisionalChart> {
        // D9 Navamsa: 9 parts of 3°20' each
        fun getD9Sign(rashiIdx: Int, degInRashi: Double): Int {
            val part = (degInRashi / (30.0 / 9.0)).toInt().coerceIn(0, 8)
            val startSign = when (rashiIdx % 4) {
                0 -> 0 // Fire: Aries
                1 -> 9 // Earth: Capricorn
                2 -> 6 // Air: Libra
                else -> 3 // Water: Cancer
            }
            return (startSign + part) % 12
        }

        // D10 Dashamsa: 10 parts of 3° each
        fun getD10Sign(rashiIdx: Int, degInRashi: Double): Int {
            val part = (degInRashi / 3.0).toInt().coerceIn(0, 9)
            val isOdd = (rashiIdx % 2 == 0) // Aries is 0 (odd 1st sign in 1-based)
            val startSign = if (isOdd) rashiIdx else (rashiIdx + 8) % 12
            return (startSign + part) % 12
        }

        // D7 Saptamsa: 7 parts of 4°17'8.57" each
        fun getD7Sign(rashiIdx: Int, degInRashi: Double): Int {
            val part = (degInRashi / (30.0 / 7.0)).toInt().coerceIn(0, 6)
            val isOdd = (rashiIdx % 2 == 0)
            val startSign = if (isOdd) rashiIdx else (rashiIdx + 6) % 12
            return (startSign + part) % 12
        }

        val allItems = mutableListOf<Pair<String, Pair<String, Pair<Int, Double>>>>()
        allItems.add(Pair("Lagna", Pair("लग्न", Pair(lagna.rashiIndex, lagna.degreeInRashi))))
        planets.forEach {
            allItems.add(Pair(it.nameEn, Pair(it.nameHi, Pair(it.rashiIndex, it.degreeInRashi))))
        }

        val d9Placements = allItems.map { (nameEn, pair) ->
            val (nameHi, pos) = pair
            val signIdx = getD9Sign(pos.first, pos.second)
            DivisionalPlacement(nameEn, nameHi, signIdx, RASHI_NAMES_EN[signIdx], RASHI_NAMES_HI[signIdx])
        }

        val d10Placements = allItems.map { (nameEn, pair) ->
            val (nameHi, pos) = pair
            val signIdx = getD10Sign(pos.first, pos.second)
            DivisionalPlacement(nameEn, nameHi, signIdx, RASHI_NAMES_EN[signIdx], RASHI_NAMES_HI[signIdx])
        }

        val d7Placements = allItems.map { (nameEn, pair) ->
            val (nameHi, pos) = pair
            val signIdx = getD7Sign(pos.first, pos.second)
            DivisionalPlacement(nameEn, nameHi, signIdx, RASHI_NAMES_EN[signIdx], RASHI_NAMES_HI[signIdx])
        }

        return listOf(
            DivisionalChart(
                code = "D9",
                titleEn = "Navamsha Chart (D9)",
                titleHi = "नवांश कुंडली (डी-९)",
                descriptionEn = "Reveals the inner soul purpose, marriage dynamics, spouse characteristics, and overall spiritual potential.",
                descriptionHi = "आत्मा के सूक्ष्म स्वभाव, वैवाहिक सुख, जीवनसाथी के गुण और आध्यात्मिक क्षमता का प्रामाणिक दर्पण।",
                placements = d9Placements
            ),
            DivisionalChart(
                code = "D10",
                titleEn = "Dashamsha Chart (D10)",
                titleHi = "दशांश कुंडली (डी-१०)",
                descriptionEn = "Highlights profession, public prominence, career milestones, authority, and accomplishments in society.",
                descriptionHi = "कार्यक्षेत्र, व्यवसाय, आजीविका, सामाजिक प्रतिष्ठा और जीवन में अर्जित उपलब्धियों का विश्लेषण।",
                placements = d10Placements
            ),
            DivisionalChart(
                code = "D7",
                titleEn = "Saptamsha Chart (D7)",
                titleHi = "सप्तांश कुंडली (डी-७)",
                descriptionEn = "Reflects children, progeny happiness, creative lineage, and compounding creative prosperity.",
                descriptionHi = "संतान सुख, वंश वृद्धि, रचनात्मकता और रचनात्मक समृद्धि का विस्तृत विवेचन।",
                placements = d7Placements
            )
        )
    }

    fun computeKundali(
        birthDateStr: String, // YYYY-MM-DD
        birthTimeStr: String, // HH:mm
        isTimeUnknown: Boolean,
        lat: Double,
        lon: Double,
        tzId: String,
        ayanamsaType: AyanamsaType,
        predictionsRepo: PredictionsRepository
    ): KundaliData {
        val dateParts = birthDateStr.split("-").map { it.toInt() }
        val year = dateParts[0]
        val month = dateParts[1]
        val day = dateParts[2]

        val timeParts = if (isTimeUnknown) listOf(12, 0) else birthTimeStr.split(":").map { it.toInt() }
        val hour = timeParts[0]
        val minute = timeParts[1]

        val tz = TimeZone.getTimeZone(tzId)
        val cal = Calendar.getInstance(tz).apply {
            set(year, month - 1, day, hour, minute, 0)
        }
        val tzOffsetHours = tz.getOffset(cal.timeInMillis) / 3600000.0

        val jd = calculateJulianDay(year, month, day, hour, minute, 0, tzOffsetHours)
        val ayanamsa = calculateAyanamsa(jd, ayanamsaType)

        val lagna = calculateLagna(jd, lat, lon, ayanamsa)
        val planets = calculatePlanets(jd, ayanamsa)

        // Assign house numbers relative to Lagna
        planets.forEach { p ->
            val h = ((p.rashiIndex - lagna.rashiIndex + 12) % 12) + 1
            p.houseNumber = h
        }

        val houses = calculateHouseCusps(lagna)
        val sun = planets.first { it.nameEn == "Sun" }
        val moon = planets.first { it.nameEn == "Moon" }

        val panchang = calculatePanchang(cal, sun.longitude, moon.longitude, lat)
        val (vimshottari, currentDashaPair) = calculateVimshottariDasha(moon, cal.time)
        val yogasAndDoshas = calculateYogasAndDoshas(lagna, planets)
        val divisionalCharts = calculateDivisionalCharts(lagna, planets)

        val moonSignEn = moon.rashiNameEn
        val readings = predictionsRepo.getReadings(moonSignEn, lagna.rashiNameEn, cal.time)

        return KundaliData(
            lagna = lagna,
            planets = planets,
            houses = houses,
            panchang = panchang,
            vimshottariDasha = vimshottari,
            currentDashaSummaryEn = currentDashaPair.first,
            currentDashaSummaryHi = currentDashaPair.second,
            yogasAndDoshas = yogasAndDoshas,
            divisionalCharts = divisionalCharts,
            personalityReadingEn = readings.personalityEn,
            personalityReadingHi = readings.personalityHi,
            careerReadingEn = readings.careerEn,
            careerReadingHi = readings.careerHi,
            relationshipReadingEn = readings.relationshipsEn,
            relationshipReadingHi = readings.relationshipsHi,
            healthReadingEn = readings.healthEn,
            healthReadingHi = readings.healthHi,
            luckyElements = readings.luckyElements,
            remedies = readings.remedies,
            dailyPredictionEn = readings.dailyEn,
            dailyPredictionHi = readings.dailyHi,
            monthlyPredictionEn = readings.monthlyEn,
            monthlyPredictionHi = readings.monthlyHi,
            yearlyPredictionEn = readings.yearlyEn,
            yearlyPredictionHi = readings.yearlyHi,
            lifePredictionEn = readings.lifeEn,
            lifePredictionHi = readings.lifeHi
        )
    }
}
