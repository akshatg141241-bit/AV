package com.aryaleelastudios.astroveda.astro

import com.aryaleelastudios.astroveda.data.model.AshtaKootaResult
import com.aryaleelastudios.astroveda.data.model.KootaItem
import com.aryaleelastudios.astroveda.data.repository.ContentRepository
import kotlin.math.abs

object AshtaKootaEngine {

    private val NAKSHATRA_YONI = listOf(
        0, 1, 2, 3, 3,
        4, 5, 2, 5, 6,
        6, 7, 8, 9,
        8, 9, 10, 10, 4,
        11, 12, 11, 13,
        0, 13, 7, 1
    )

    private val NAKSHATRA_GANA = listOf(
        0, 1, 2, 1, 0, 1,
        0, 0, 2, 2, 1, 1,
        0, 2, 0, 2, 0, 2,
        2, 1, 1, 0, 2, 2,
        1, 1, 0
    )

    private val NAKSHATRA_NADI = listOf(
        0, 1, 2, 2, 1, 0,
        0, 1, 2, 2, 1, 0,
        0, 1, 2, 2, 1, 0,
        0, 1, 2, 2, 1, 0,
        0, 1, 2
    )

    // Rashi Lords: 0:Mars, 1:Venus, 2:Mercury, 3:Moon, 4:Sun, 5:Mercury, 6:Venus, 7:Mars, 8:Jupiter, 9:Saturn, 10:Saturn, 11:Jupiter
    private val RASHI_LORDS = listOf(0, 1, 2, 3, 4, 2, 1, 0, 5, 6, 6, 5)

    private fun getGrahaMaitriScore(lordG: Int, lordB: Int): Double {
        if (lordG == lordB) return 5.0
        val friends = mapOf(
            4 to setOf(3, 2, 5),
            3 to setOf(4, 2),
            0 to setOf(4, 3, 5),
            2 to setOf(4, 1),
            5 to setOf(4, 3, 0),
            1 to setOf(2, 6),
            6 to setOf(2, 1)
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
        brideIsManglik: Boolean,
        langCode: String,
        contentRepo: ContentRepository
    ): AshtaKootaResult {
        fun lbl(key: String): String = contentRepo.resolveAstroLabel(langCode, "kootas", key)

        val rashis = contentRepo.resolveAstroStringList(langCode, "rashis").ifEmpty { AstroEngine.CANONICAL_RASHIS }
        val nakshatras = contentRepo.resolveAstroStringList(langCode, "nakshatras")

        // 1. Varna (1 pt)
        fun getVarna(r: Int): Int = when (r) {
            3, 7, 11 -> 3
            0, 4, 8 -> 2
            1, 5, 9 -> 1
            else -> 0
        }
        val varnaScore = if (getVarna(groomRashiIdx) >= getVarna(brideRashiIdx)) 1.0 else 0.0
        val varnaItem = KootaItem(lbl("varna_name"), varnaScore, 1.0, lbl("varna_meaning"))

        // 2. Vashya (2 pts)
        val diffRashi = (brideRashiIdx - groomRashiIdx + 12) % 12
        val vashyaScore = when (diffRashi) {
            0, 6 -> 2.0
            3, 4, 8, 9 -> 1.0
            else -> 0.5
        }
        val vashyaItem = KootaItem(lbl("vashya_name"), vashyaScore, 2.0, lbl("vashya_meaning"))

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
        val taraItem = KootaItem(lbl("tara_name"), taraScore, 3.0, lbl("tara_meaning"))

        // 4. Yoni (4 pts)
        val yG = NAKSHATRA_YONI[groomNakshatraIdx.coerceIn(0, 26)]
        val yB = NAKSHATRA_YONI[brideNakshatraIdx.coerceIn(0, 26)]
        val yoniScore = when {
            yG == yB -> 4.0
            abs(yG - yB) in listOf(1, 2, 7) -> 3.0
            abs(yG - yB) in listOf(3, 4, 5) -> 2.0
            else -> 1.0
        }
        val yoniItem = KootaItem(lbl("yoni_name"), yoniScore, 4.0, lbl("yoni_meaning"))

        // 5. Graha Maitri (5 pts)
        val lordG = RASHI_LORDS[groomRashiIdx.coerceIn(0, 11)]
        val lordB = RASHI_LORDS[brideRashiIdx.coerceIn(0, 11)]
        val maitriScore = getGrahaMaitriScore(lordG, lordB)
        val maitriItem = KootaItem(lbl("maitri_name"), maitriScore, 5.0, lbl("maitri_meaning"))

        // 6. Gana (6 pts)
        val gG = NAKSHATRA_GANA[groomNakshatraIdx.coerceIn(0, 26)]
        val gB = NAKSHATRA_GANA[brideNakshatraIdx.coerceIn(0, 26)]
        val ganaScore = when {
            gG == gB -> 6.0
            (gG == 0 && gB == 1) || (gG == 1 && gB == 0) -> 5.0
            gG == 0 && gB == 2 -> 1.0
            else -> 0.0
        }
        val ganaItem = KootaItem(lbl("gana_name"), ganaScore, 6.0, lbl("gana_meaning"))

        // 7. Bhakoot (7 pts)
        val rDist = ((groomRashiIdx - brideRashiIdx + 12) % 12) + 1
        val badDistances = setOf(2, 6, 8, 12)
        val bhakootScore = if (rDist !in badDistances || lordG == lordB) 7.0 else 0.0
        val bhakootItem = KootaItem(lbl("bhakoot_name"), bhakootScore, 7.0, lbl("bhakoot_meaning"))

        // 8. Nadi (8 pts)
        val nG = NAKSHATRA_NADI[groomNakshatraIdx.coerceIn(0, 26)]
        val nB = NAKSHATRA_NADI[brideNakshatraIdx.coerceIn(0, 26)]
        val nadiScore = if (nG != nB) 8.0 else 0.0
        val nadiItem = KootaItem(lbl("nadi_name"), nadiScore, 8.0, lbl("nadi_meaning"))

        val total = varnaScore + vashyaScore + taraScore + yoniScore + maitriScore + ganaScore + bhakootScore + nadiScore
        val (verdict, verdictDetail) = when {
            total >= 28.0 -> lbl("verdict_utkrisht") to lbl("verdict_utkrisht_detail")
            total >= 18.0 -> lbl("verdict_madhyam") to lbl("verdict_madhyam_detail")
            else -> lbl("verdict_nyun") to lbl("verdict_nyun_detail")
        }

        val mangalAnalysis = when {
            groomIsManglik && brideIsManglik -> lbl("mangal_both")
            !groomIsManglik && !brideIsManglik -> lbl("mangal_none")
            groomIsManglik -> lbl("mangal_one_p1")
            else -> lbl("mangal_one_p2")
        }

        return AshtaKootaResult(
            groomName = groomName,
            brideName = brideName,
            groomRashi = rashis.getOrElse(groomRashiIdx) { "" },
            groomNakshatra = nakshatras.getOrElse(groomNakshatraIdx) { "" },
            brideRashi = rashis.getOrElse(brideRashiIdx) { "" },
            brideNakshatra = nakshatras.getOrElse(brideNakshatraIdx) { "" },
            totalScore = total,
            verdict = verdict,
            verdictDetail = verdictDetail,
            kootas = listOf(varnaItem, vashyaItem, taraItem, yoniItem, maitriItem, ganaItem, bhakootItem, nadiItem),
            mangalDoshaAnalysis = mangalAnalysis
        )
    }
}
