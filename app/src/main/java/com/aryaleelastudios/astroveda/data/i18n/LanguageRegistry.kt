package com.aryaleelastudios.astroveda.data.i18n

import androidx.annotation.FontRes
import com.aryaleelastudios.astroveda.R

data class LanguageEntry(
    val code: String,
    val nativeName: String,
    val script: String,
    val fontFamily: String,
    val fontPath: String,
    @FontRes val fontResId: Int,
    val enabled: Boolean
)

object LanguageRegistry {

    val entries: List<LanguageEntry> = listOf(
        LanguageEntry(
            code = "en",
            nativeName = "English",
            script = "Latin",
            fontFamily = "Lora",
            fontPath = "res/font/lora.ttf",
            fontResId = R.font.lora,
            enabled = true
        ),
        LanguageEntry(
            code = "hi",
            nativeName = "हिन्दी",
            script = "Devanagari",
            fontFamily = "Noto Serif Devanagari",
            fontPath = "res/font/noto_serif_devanagari.ttf",
            fontResId = R.font.noto_serif_devanagari,
            enabled = true
        ),
        LanguageEntry(
            code = "gu",
            nativeName = "ગુજરાતી",
            script = "Gujarati",
            fontFamily = "Noto Serif Gujarati",
            fontPath = "res/font/noto_serif_gujarati.ttf",
            fontResId = R.font.noto_serif_gujarati,
            enabled = false
        )
    )

    fun enabledLanguages(): List<LanguageEntry> = entries.filter { it.enabled }

    fun findByCode(code: String): LanguageEntry? = entries.find { it.code == code }

    fun resolveOrDefault(code: String): LanguageEntry {
        val found = findByCode(code)
        return if (found != null && found.enabled) found else entries.first()
    }
}
