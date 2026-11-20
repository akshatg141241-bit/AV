package com.aryaleelastudios.kundaliguru.astro

import com.aryaleelastudios.kundaliguru.data.model.AshtaKootaResult
import com.aryaleelastudios.kundaliguru.data.model.KootaItem
import kotlin.math.abs

object AshtaKootaEngine {

    // Nakshatra Yoni Animals (0 to 26)
    // 0:Ashwini(Horse), 1:Bharani(Elephant), 2:Krittika(Sheep), 3:Rohini(Serpent), 4:Mrigashira(Serpent),
    // 5:Ardra(Dog), 6:Punarvasu(Cat), 7:Pushya(Sheep), 8:Ashlesha(Cat), 9:Magha(Rat),
    // 10:Purva Phalguni(Rat), 11:Uttara Phalguni(Cow), 12:Hasta(Buffalo), 13:Chitra(Tiger),
    // 14:Swati(Buffalo), 15:Vishakha(Tiger), 16:Anuradha(Hare), 17:Jyeshtha(Hare), 18:Mula(Dog),
    // 19:Purva Ashadha(Monkey), 20:Uttara Ashadha(Mongoose), 21:Shravana(Monkey), 22:Dhanishta(Lion),
    // 23:Shatabhisha(Horse), 24:Purva Bhadrapada(Lion), 25:Uttara Bhadrapada(Cow), 26:Revati(Elephant)
    private val NAKSHATRA_YONI = listOf(
        0, 1, 2, 3, 3,
        4, 5, 2, 5, 6,
        6, 7, 8, 9,
        8, 9, 10, 10, 4,
        11, 12, 11, 13,
        0, 13, 7, 1
    )

    // Gana: 0: Deva, 1: Manushya, 2: Rakshasa
    private val NAKSHATRA_GANA = listOf(
        0, 1, 2, 1, 0, 1, // Ashwini(D), Bharani(M), Krittika(R), Rohini(M), Mrigashira(D), Ardra(M)
        0, 0, 2, 2, 1, 1, // Punarvasu(D), Pushya(D), Ashlesha(R), Magha(R), P.Phalguni(M), U.Phalguni(M)
        0, 2, 0, 2, 0, 2, // Hasta(D), Chitra(R), Swati(D), Vishakha(R), Anuradha(D), Jyeshtha(R)
        2, 1, 1, 0, 2, 2, // Mula(R), P.Ashadha(M), U.Ashadha(M), Shravana(D), Dhanishta(R), Shatabhisha(R)
        1, 1, 0           // P.Bhadrapada(M), U.Bhadrapada(M), Revati(D)
    )

    // Nadi: 0: Aadi, 1: Madhya, 2: Antya
    private val NAKSHATRA_NADI = listOf(
        0, 1, 2, 2, 1, 0,
        0, 1, 2, 2, 1, 0,
        0, 1, 2, 2, 1, 0,
        0, 1, 2, 2, 1, 0,
        0, 1, 2
    )

    // Rashi Lords: 0:Mars, 1:Venus, 2:Mercury, 3:Moon, 4:Sun, 5:Mercury, 6:Venus, 7:Mars, 8:Jupiter, 9:Saturn, 10:Saturn, 11:Jupiter
    private val RASHI_LORDS = listOf(0, 1, 2, 3, 4, 2, 1, 0, 5, 6, 6, 5)

    // Planetary Friendship matrix: 0:Sun, 1:Moon, 2:Mars, 3:Mercury, 4:Jupiter, 5:Venus, 6:Saturn
    // Returns 5 for friends, 4/3 for neutral, 1/0 for enemies
    private fun getGrahaMaitriScore(lordG: Int, lordB: Int): Double {
        if (lordG == lordB) return 5.0
        val friends = mapOf(
            4 to setOf(3, 2, 5), // Sun -> Moon, Mars, Jupiter
            3 to setOf(4, 2),    // Moon -> Sun, Mercury
            0 to setOf(4, 3, 5), // Mars -> Sun, Moon, Jupiter
            2 to setOf(4, 1),    // Mercury -> Sun, Venus
            5 to setOf(4, 3, 0), // Jupiter -> Sun, Moon, Mars
            1 to setOf(2, 6),    // Venus -> Mercury, Saturn
            6 to setOf(2, 1)     // Saturn -> Mercury, Venus
        )
        val isGFriend = friends[lordG]?.contains(lordB) == true
        val isBFriend = friends[lordB]?.contains(lordG) == true
        return when {
            isGFriend && isBFriend -> 5.0
            (isGFriend && !isBFriend) || (!isGFriend && isBFriend) -> 4.0
            else -> 1.0
        }
    }

    fun matchHoroscopes(
        groomName: String,
        groomRashiIdx: Int,
        groomNakshatraIdx: Int,
        groomIsManglik: Boolean,
        brideName: String,
        brideRashiIdx: Int,
        brideNakshatraIdx: Int,
        brideIsManglik: Boolean
    ): AshtaKootaResult {

        // 1. Varna (1 pt)
        // 0: Brahmin (3, 7, 11), 1: Kshatriya (0, 4, 8), 2: Vaishya (1, 5, 9), 3: Shudra (2, 6, 10)
        fun getVarna(r: Int): Int = when (r) {
            3, 7, 11 -> 3 // Highest/Brahmin
            0, 4, 8 -> 2  // Kshatriya
            1, 5, 9 -> 1  // Vaishya
            else -> 0     // Shudra
        }
        val vG = getVarna(groomRashiIdx)
        val vB = getVarna(brideRashiIdx)
        val varnaScore = if (vG >= vB) 1.0 else 0.0
        val varnaItem = KootaItem(
            nameEn = "Varna Koota",
            nameHi = "वर्ण कूट",
            score = varnaScore,
            maxScore = 1.0,
            meaningEn = "Reflects spiritual alignment, ego harmony, and vocational compatibility.",
            meaningHi = "आध्यात्मिक दृष्टिकोण, अहंकार सामंजस्य और कार्य-संस्कृति की अनुकूलता।"
        )

        // 2. Vashya (2 pts)
        val diffRashi = (brideRashiIdx - groomRashiIdx + 12) % 12
        val vashyaScore = when (diffRashi) {
            0, 6 -> 2.0
            3, 4, 8, 9 -> 1.0
            else -> 0.5
        }
        val vashyaItem = KootaItem(
            nameEn = "Vashya Koota",
            nameHi = "वश्य कूट",
            score = vashyaScore,
            maxScore = 2.0,
            meaningEn = "Measures mutual attraction, influence, and cooperative power balance.",
            meaningHi = "परस्पर आकर्षण, नियंत्रण संतुलन और वैवाहिक सामंजस्य का सूचक।"
        )

        // 3. Tara (3 pts)
        val d1 = ((groomNakshatraIdx - brideNakshatraIdx + 27) % 9)
        val d2 = ((brideNakshatraIdx - groomNakshatraIdx + 27) % 9)
        val auspiciousD = setOf(1, 2, 4, 6, 8)
        val t1 = d1 in auspiciousD
        val t2 = d2 in auspiciousD
        val taraScore = when {
            t1 && t2 -> 3.0
            t1 || t2 -> 1.5
            else -> 0.0
        }
        val taraItem = KootaItem(
            nameEn = "Tara Koota",
            nameHi = "तारा कूट",
            score = taraScore,
            maxScore = 3.0,
            meaningEn = "Indicates destiny, luck, health, and mutual longevity of the couple.",
            meaningHi = "भाग्य, स्वास्थ्य, दीर्घायु और परस्पर शुभता का मूल्यांकन।"
        )

        // 4. Yoni (4 pts)
        val yG = NAKSHATRA_YONI[groomNakshatraIdx.coerceIn(0, 26)]
        val yB = NAKSHATRA_YONI[brideNakshatraIdx.coerceIn(0, 26)]
        val yoniScore = when {
            yG == yB -> 4.0
            abs(yG - yB) in listOf(1, 2, 7) -> 3.0
            abs(yG - yB) in listOf(3, 4, 5) -> 2.0
            else -> 1.0
        }
        val yoniItem = KootaItem(
            nameEn = "Yoni Koota",
            nameHi = "योनि कूट",
            score = yoniScore,
            maxScore = 4.0,
            meaningEn = "Analyzes biological affinity, physical compatibility, and mutual affection.",
            meaningHi = "शारीरिक सामंजस्य, सहज आत्मीयता और दांपत्य सुख का परिचायक।"
        )

        // 5. Graha Maitri (5 pts)
        val lordG = RASHI_LORDS[groomRashiIdx.coerceIn(0, 11)]
        val lordB = RASHI_LORDS[brideRashiIdx.coerceIn(0, 11)]
        val maitriScore = getGrahaMaitriScore(lordG, lordB)
        val maitriItem = KootaItem(
            nameEn = "Graha Maitri Koota",
            nameHi = "ग्रह मैत्री कूट",
            score = maitriScore,
            maxScore = 5.0,
            meaningEn = "Examines intellectual wavelength, psychological friendship, and communication.",
            meaningHi = "मानसिक मित्रता, बौद्धिक सामंजस्य और दृष्टिकोण की समानता।"
        )

        // 6. Gana (6 pts)
        val gG = NAKSHATRA_GANA[groomNakshatraIdx.coerceIn(0, 26)]
        val gB = NAKSHATRA_GANA[brideNakshatraIdx.coerceIn(0, 26)]
        val ganaScore = when {
            gG == gB -> 6.0
            (gG == 0 && gB == 1) || (gG == 1 && gB == 0) -> 5.0
            (gG == 0 && gB == 2) -> 1.0
            (gG == 2 && gB == 0) -> 0.0
            else -> 0.0
        }
        val ganaItem = KootaItem(
            nameEn = "Gana Koota",
            nameHi = "गण कूट",
            score = ganaScore,
            maxScore = 6.0,
            meaningEn = "Reflects natural temperament, emotional disposition, and behavioral habits.",
            meaningHi = "स्वभाव, आचरण, जीवनशैली और मानसिक प्रकृति का मिलान।"
        )

        // 7. Bhakoot (7 pts)
        val rDist = ((groomRashiIdx - brideRashiIdx + 12) % 12) + 1
        val badDistances = setOf(2, 6, 8, 12)
        val isSameLord = lordG == lordB
        val bhakootScore = if (rDist !in badDistances || isSameLord) 7.0 else 0.0
        val bhakootItem = KootaItem(
            nameEn = "Bhakoot Koota",
            nameHi = "भकूट कूट",
            score = bhakootScore,
            maxScore = 7.0,
            meaningEn = "Assesses family prosperity, emotional bliss, financial growth, and longevity.",
            meaningHi = "पारिवारिक समृद्धि, दांपत्य प्रेम और आर्थिक स्थायित्व का मुख्य आधार।"
        )

        // 8. Nadi (8 pts)
        val nG = NAKSHATRA_NADI[groomNakshatraIdx.coerceIn(0, 26)]
        val nB = NAKSHATRA_NADI[brideNakshatraIdx.coerceIn(0, 26)]
        val nadiScore = if (nG != nB) 8.0 else 0.0
        val nadiItem = KootaItem(
            nameEn = "Nadi Koota",
            nameHi = "नाड़ी कूट",
            score = nadiScore,
            maxScore = 8.0,
            meaningEn = "Most crucial koota: genetic health, progeny vitality, and nervous constitution.",
            meaningHi = "सर्वाधिक महत्वपूर्ण कूट: संतान सुख, आनुवंशिक स्वास्थ्य और जीवन शक्ति।"
        )

        val total = varnaScore + vashyaScore + taraScore + yoniScore + maitriScore + ganaScore + bhakootScore + nadiScore
        val (verdictEn, verdictHi, detailEn, detailHi) = when {
            total >= 28.0 -> Quadruple(
                "Utkrisht Milan (Excellent)",
                "उत्कृष्ट मिलान (शुभ एवं श्रेष्ठ)",
                "Exceptional celestial alignment. The horoscopes share strong mutual friendship, family prosperity, and harmonious life vision.",
                "ग्रह-नक्षत्रों का अत्यंत दुर्लभ एवं शुभ संयोग। दांपत्य जीवन में अपार स्नेह, सुख-समृद्धि और दीर्घायु की सुंदर संभावना है।"
            )
            total >= 18.0 -> Quadruple(
                "Madhyam Milan (Favorable / Good)",
                "मध्यम मिलान (संतोषजनक एवं शुभ)",
                "Good compatibility with strong foundation. Minor differences can easily be bridged with understanding, mutual respect, and patience.",
                "संतोषजनक और उत्तम मिलान। परस्पर समझ, सम्मान और सद्भाव से दांपत्य जीवन सुखमय और सफल रहेगा।"
            )
            else -> Quadruple(
                "Nyun Milan (Remedies Advised)",
                "न्यून मिलान (उपाय आवश्यक)",
                "Below traditional benchmark. Astrological remedies, pre-marriage dialogue, and elder blessings are strongly encouraged.",
                "गुण मिलान औसत से कम है। विद्वान ज्योतिषी के मार्गदर्शन में शांति उपाय और परस्पर गंभीर विचार-विमर्श अनुशंसित है।"
            )
        }

        val mangalAnalysisEn = when {
            groomIsManglik && brideIsManglik -> "Both partners have Manglik placement, which naturally neutralizes Mangal Dosha (Dosha Samyam)."
            !groomIsManglik && !brideIsManglik -> "Both partners are non-Manglik. No Kuja Dosha afflictions detected."
            groomIsManglik -> "Partner 1 has Manglik indications while Partner 2 does not. Mild astrological remedies or Hanuman worship recommended."
            else -> "Partner 2 has Manglik indications while Partner 1 does not. Gentle remedial fasts and mindful communication recommended."
        }

        val mangalAnalysisHi = when {
            groomIsManglik && brideIsManglik -> "दोनों कुंडलियों में मांगलिक योग होने से परस्पर दोष साम्य हो जाता है, जिससे दोष स्वतः निष्प्रभावी हो जाता है।"
            !groomIsManglik && !brideIsManglik -> "दोनों कुंडलियां मांगलिक दोष से सर्वथा मुक्त हैं।"
            groomIsManglik -> "प्रथम साथी मांगलिक हैं जबकि द्वितीय नहीं। मंगलवार को हनुमान आराधना अथवा शांति उपाय हितकर रहेंगे।"
            else -> "द्वितीय साथी मांगलिक हैं जबकि प्रथम नहीं। परस्पर आदर, सौहार्द और नियमित ईश-स्मरण से शांति रहेगी।"
        }

        return AshtaKootaResult(
            groomName = groomName,
            brideName = brideName,
            groomRashiEn = AstroEngine.RASHI_NAMES_EN[groomRashiIdx],
            groomRashiHi = AstroEngine.RASHI_NAMES_HI[groomRashiIdx],
            groomNakshatraEn = AstroEngine.NAKSHATRA_NAMES_EN[groomNakshatraIdx],
            groomNakshatraHi = AstroEngine.NAKSHATRA_NAMES_HI[groomNakshatraIdx],
            brideRashiEn = AstroEngine.RASHI_NAMES_EN[brideRashiIdx],
            brideRashiHi = AstroEngine.RASHI_NAMES_HI[brideRashiIdx],
            brideNakshatraEn = AstroEngine.NAKSHATRA_NAMES_EN[brideNakshatraIdx],
            brideNakshatraHi = AstroEngine.NAKSHATRA_NAMES_HI[brideNakshatraIdx],
            totalScore = total,
            verdictEn = verdictEn,
            verdictHi = verdictHi,
            verdictDetailEn = detailEn,
            verdictDetailHi = detailHi,
            kootas = listOf(varnaItem, vashyaItem, taraItem, yoniItem, maitriItem, ganaItem, bhakootItem, nadiItem),
            mangalDoshaAnalysisEn = mangalAnalysisEn,
            mangalDoshaAnalysisHi = mangalAnalysisHi
        )
    }

    private data class Quadruple<A, B, C, D>(val first: A, val second: B, val third: C, val fourth: D)
}
