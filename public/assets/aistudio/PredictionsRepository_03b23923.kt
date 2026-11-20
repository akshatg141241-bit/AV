package com.aryaleelastudios.kundaliguru.astro

import android.content.Context
import com.aryaleelastudios.kundaliguru.data.model.LuckyElements
import com.aryaleelastudios.kundaliguru.data.model.Remedies
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.abs

class PredictionsRepository(private val context: Context) {

    private val enJson: JSONObject by lazy {
        loadJsonFromAsset("predictions/predictions_en.json")
    }

    private val hiJson: JSONObject by lazy {
        loadJsonFromAsset("predictions/predictions_hi.json")
    }

    private fun loadJsonFromAsset(fileName: String): JSONObject {
        return try {
            val content = context.assets.open(fileName).bufferedReader().use { it.readText() }
            JSONObject(content)
        } catch (e: Exception) {
            JSONObject()
        }
    }

    data class AstrologicalReadings(
        val personalityEn: String,
        val personalityHi: String,
        val careerEn: String,
        val careerHi: String,
        val relationshipsEn: String,
        val relationshipsHi: String,
        val healthEn: String,
        val healthHi: String,
        val luckyElements: LuckyElements,
        val remedies: Remedies,
        val dailyEn: String,
        val dailyHi: String,
        val monthlyEn: String,
        val monthlyHi: String,
        val yearlyEn: String,
        val yearlyHi: String,
        val lifeEn: String,
        val lifeHi: String
    )

    fun getReadings(moonSignEn: String, lagnaSignEn: String, birthDate: Date): AstrologicalReadings {
        val sign = if (enJson.optJSONObject("personality")?.has(moonSignEn) == true) moonSignEn else "Aries"

        val persEn = enJson.optJSONObject("personality")?.optString(sign) ?: ""
        val persHi = hiJson.optJSONObject("personality")?.optString(sign) ?: ""

        val careerEn = enJson.optJSONObject("career")?.optString(sign) ?: ""
        val careerHi = hiJson.optJSONObject("career")?.optString(sign) ?: ""

        val relEn = enJson.optJSONObject("relationships")?.optString(sign) ?: ""
        val relHi = hiJson.optJSONObject("relationships")?.optString(sign) ?: ""

        val healthEn = enJson.optJSONObject("health")?.optString(sign) ?: ""
        val healthHi = hiJson.optJSONObject("health")?.optString(sign) ?: ""

        // Lucky elements
        val luckyObjEn = enJson.optJSONObject("lucky_elements")?.optJSONObject(sign)
        val luckyObjHi = hiJson.optJSONObject("lucky_elements")?.optJSONObject(sign)
        val lucky = LuckyElements(
            number = luckyObjEn?.optInt("number", 7) ?: 7,
            color = luckyObjEn?.optString("color", "Golden Saffron") ?: "Golden Saffron",
            day = luckyObjEn?.optString("day", "Thursday") ?: "Thursday",
            direction = luckyObjEn?.optString("direction", "East") ?: "East",
            gemstone = luckyObjEn?.optString("gemstone", "Ruby") ?: "Ruby"
        )

        // Remedies
        val remObjEn = enJson.optJSONObject("remedies")?.optJSONObject(sign)
        val remObjHi = hiJson.optJSONObject("remedies")?.optJSONObject(sign)
        val remedies = Remedies(
            mantra = remObjEn?.optString("mantra", "Om Namah Shivaya") ?: "Om Namah Shivaya",
            charity = remObjEn?.optString("charity", "Donate yellow pulses or grain") ?: "",
            fasting = remObjEn?.optString("fasting", "Observe Thursday fast") ?: "",
            behavioral = remObjEn?.optString("behavioral", "Practice patience and morning meditation") ?: ""
        )

        // Deterministic date-based predictions
        val todayStr = SimpleDateFormat("yyyyMMdd", Locale.getDefault()).format(Date())
        val monthStr = SimpleDateFormat("yyyyMM", Locale.getDefault()).format(Date())
        val yearStr = SimpleDateFormat("yyyy", Locale.getDefault()).format(Date())

        val dailyArrayEn = enJson.optJSONArray("daily_insights")
        val dailyArrayHi = hiJson.optJSONArray("daily_insights")
        val dailyIdx = (abs(todayStr.hashCode() + sign.hashCode()) % (dailyArrayEn?.length() ?: 1))
        val dailyEn = dailyArrayEn?.optString(dailyIdx) ?: "Planetary configurations favor your endeavors today."
        val dailyHi = dailyArrayHi?.optString(dailyIdx) ?: "आज का ग्रह गोचर आपके कार्यों में अनुकूलता प्रदान करेगा।"

        val monthlyArrayEn = enJson.optJSONArray("monthly_insights")
        val monthlyArrayHi = hiJson.optJSONArray("monthly_insights")
        val monthIdx = (abs(monthStr.hashCode() + sign.hashCode()) % (monthlyArrayEn?.length() ?: 1))
        val monthlyEn = monthlyArrayEn?.optString(monthIdx) ?: "A month of steady consolidation and learning."
        val monthlyHi = monthlyArrayHi?.optString(monthIdx) ?: "यह मास स्थिरता और नवीन ज्ञान अर्जन के लिए उत्तम है।"

        val yearlyArrayEn = enJson.optJSONArray("yearly_insights")
        val yearlyArrayHi = hiJson.optJSONArray("yearly_insights")
        val yearIdx = (abs(yearStr.hashCode() + sign.hashCode()) % (yearlyArrayEn?.length() ?: 1))
        val yearlyEn = yearlyArrayEn?.optString(yearIdx) ?: "An auspicious year marking personal wisdom and strength."
        val yearlyHi = yearlyArrayHi?.optString(yearIdx) ?: "यह वर्ष आत्मबल, ज्ञान और प्रतिष्ठा में वृद्धि कराने वाला रहेगा।"

        val lifeArrayEn = enJson.optJSONArray("life_insights")
        val lifeArrayHi = hiJson.optJSONArray("life_insights")
        val lifeEn = lifeArrayEn?.optString(0) ?: "Your chart reflects steady resilience, noble vision, and dharmic peace."
        val lifeHi = lifeArrayHi?.optString(0) ?: "आपकी कुंडली में कर्मठता, विवेक और धर्म के मार्ग पर चलने से परम शांति का योग है।"

        return AstrologicalReadings(
            personalityEn = persEn,
            personalityHi = persHi,
            careerEn = careerEn,
            careerHi = careerHi,
            relationshipsEn = relEn,
            relationshipsHi = relHi,
            healthEn = healthEn,
            healthHi = healthHi,
            luckyElements = lucky,
            remedies = remedies,
            dailyEn = dailyEn,
            dailyHi = dailyHi,
            monthlyEn = monthlyEn,
            monthlyHi = monthlyHi,
            yearlyEn = yearlyEn,
            yearlyHi = yearlyHi,
            lifeEn = lifeEn,
            lifeHi = lifeHi
        )
    }
}
