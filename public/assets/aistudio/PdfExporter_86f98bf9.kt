package com.aryaleelastudios.kundaliguru.pdf

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.net.Uri
import com.aryaleelastudios.kundaliguru.data.local.PersonEntity
import com.aryaleelastudios.kundaliguru.data.model.KundaliData
import java.io.FileOutputStream

object PdfExporter {

    fun exportToUri(
        context: Context,
        uri: Uri,
        person: PersonEntity,
        kundali: KundaliData,
        isHindi: Boolean
    ): Boolean {
        return try {
            val document = PdfDocument()
            val pageWidth = 595 // A4 standard point size
            val pageHeight = 842
            val pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, 1).create()
            val page = document.startPage(pageInfo)
            val canvas: Canvas = page.canvas

            val paint = Paint().apply { isAntiAlias = true }

            // Header Background Accent
            paint.color = Color.rgb(0x2B, 0x2D, 0x5C) // Deep Indigo
            canvas.drawRect(0f, 0f, pageWidth.toFloat(), 90f, paint)

            // Header Title
            paint.color = Color.rgb(0xE8, 0xA3, 0x3D) // Saffron Gold
            paint.textSize = 22f
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            val appTitle = if (isHindi) "कुंडली गुरु • जन्म पत्रिका" else "Kundali Guru • Vedic Birth Chart"
            canvas.drawText(appTitle, 36f, 45f, paint)

            paint.color = Color.WHITE
            paint.textSize = 11f
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
            canvas.drawText("AryaLeela Studios • Vedic Astrology Engine", 36f, 70f, paint)

            var y = 120f

            // Personal Info Card
            paint.color = Color.rgb(0xFF, 0xF8, 0xEC) // Cream
            canvas.drawRoundRect(36f, y, pageWidth - 36f, y + 80f, 8f, 8f, paint)
            paint.color = Color.rgb(0xE8, 0xDE, 0xCA)
            paint.style = Paint.Style.STROKE
            paint.strokeWidth = 1f
            canvas.drawRoundRect(36f, y, pageWidth - 36f, y + 80f, 8f, 8f, paint)
            paint.style = Paint.Style.FILL

            paint.color = Color.rgb(0x1F, 0x20, 0x41)
            paint.textSize = 14f
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            canvas.drawText("${person.name} (${person.gender})", 50f, y + 25f, paint)

            paint.textSize = 10f
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
            val dateLabel = if (isHindi) "जन्म दिनांक:" else "Birth Date:"
            val timeLabel = if (isHindi) "जन्म समय:" else "Birth Time:"
            val placeLabel = if (isHindi) "जन्म स्थान:" else "Birth Place:"
            canvas.drawText("$dateLabel ${person.birthDate}  |  $timeLabel ${if (person.isTimeUnknown) "12:00 PM (Unknown)" else person.birthTime}", 50f, y + 45f, paint)
            canvas.drawText("$placeLabel ${person.birthPlace} (Lat: ${"%.2f".format(person.latitude)}, Lon: ${"%.2f".format(person.longitude)})", 50f, y + 65f, paint)

            y += 105f

            // Panchang Summary
            paint.textSize = 12f
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            paint.color = Color.rgb(0x2B, 0x2D, 0x5C)
            val panchangTitle = if (isHindi) "जन्म पंचांग विवरण" else "Birth Panchang"
            canvas.drawText(panchangTitle, 36f, y, paint)
            y += 16f

            paint.textSize = 9.5f
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
            paint.color = Color.rgb(0x33, 0x33, 0x44)
            val p = kundali.panchang
            val panchangLine1 = if (isHindi) {
                "तिथि: ${p.tithiHi} (${p.pakshaHi})  |  वार: ${p.varaHi}  |  सूर्योदय: ${p.sunrise}"
            } else {
                "Tithi: ${p.tithiEn} (${p.pakshaEn})  |  Vara: ${p.varaEn}  |  Sunrise: ${p.sunrise}"
            }
            val panchangLine2 = if (isHindi) {
                "नक्षत्र: ${p.nakshatraHi}  |  योग: ${p.yogaHi}  |  करण: ${p.karanaHi}"
            } else {
                "Nakshatra: ${p.nakshatraEn}  |  Yoga: ${p.yogaEn}  |  Karana: ${p.karanaEn}"
            }
            canvas.drawText(panchangLine1, 36f, y, paint)
            y += 14f
            canvas.drawText(panchangLine2, 36f, y, paint)

            y += 25f

            // Planetary Table Header
            paint.textSize = 12f
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            paint.color = Color.rgb(0x2B, 0x2D, 0x5C)
            val planetsTitle = if (isHindi) "ग्रह स्थिति (Planetary Positions)" else "Planetary Positions"
            canvas.drawText(planetsTitle, 36f, y, paint)
            y += 16f

            // Table header background
            paint.color = Color.rgb(0xEE, 0xE9, 0xDD)
            canvas.drawRect(36f, y, pageWidth - 36f, y + 20f, paint)

            paint.color = Color.rgb(0x2B, 0x2D, 0x5C)
            paint.textSize = 9f
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            canvas.drawText(if (isHindi) "ग्रह" else "Planet", 42f, y + 14f, paint)
            canvas.drawText(if (isHindi) "राशि" else "Sign", 120f, y + 14f, paint)
            canvas.drawText(if (isHindi) "अंश" else "Degree", 210f, y + 14f, paint)
            canvas.drawText(if (isHindi) "वक्री" else "Retro", 290f, y + 14f, paint)
            canvas.drawText(if (isHindi) "नक्षत्र (पद)" else "Nakshatra (Pada)", 340f, y + 14f, paint)
            canvas.drawText(if (isHindi) "भाव" else "House", 480f, y + 14f, paint)
            y += 20f

            // Lagna row
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
            paint.color = Color.rgb(0x1F, 0x20, 0x41)
            val lagnaName = if (isHindi) "लग्न (Ascendant)" else "Lagna (Ascendant)"
            val lagnaRashi = if (isHindi) kundali.lagna.rashiNameHi else kundali.lagna.rashiNameEn
            val lagnaNak = if (isHindi) "${kundali.lagna.nakshatraNameHi} (${kundali.lagna.pada})" else "${kundali.lagna.nakshatraNameEn} (${kundali.lagna.pada})"
            canvas.drawText(lagnaName, 42f, y + 13f, paint)
            canvas.drawText(lagnaRashi, 120f, y + 13f, paint)
            canvas.drawText(kundali.lagna.formattedDegree, 210f, y + 13f, paint)
            canvas.drawText("-", 290f, y + 13f, paint)
            canvas.drawText(lagnaNak, 340f, y + 13f, paint)
            canvas.drawText("1", 485f, y + 13f, paint)
            y += 16f

            // Planet rows
            kundali.planets.forEachIndexed { idx, pl ->
                if (idx % 2 == 1) {
                    paint.color = Color.rgb(0xFA, 0xF6, 0xEE)
                    canvas.drawRect(36f, y, pageWidth - 36f, y + 16f, paint)
                }
                paint.color = Color.rgb(0x1F, 0x20, 0x41)
                val pName = if (isHindi) "${pl.nameHi} (${pl.glyph})" else "${pl.nameEn} (${pl.glyph})"
                val pRashi = if (isHindi) pl.rashiNameHi else pl.rashiNameEn
                val pNak = if (isHindi) "${pl.nakshatraNameHi} (${pl.pada})" else "${pl.nakshatraNameEn} (${pl.pada})"
                val pRetro = if (pl.isRetrograde) "[R]" else "-"

                canvas.drawText(pName, 42f, y + 12f, paint)
                canvas.drawText(pRashi, 120f, y + 12f, paint)
                canvas.drawText(pl.formattedDegree, 210f, y + 12f, paint)
                canvas.drawText(pRetro, 290f, y + 12f, paint)
                canvas.drawText(pNak, 340f, y + 12f, paint)
                canvas.drawText("${pl.houseNumber}", 485f, y + 12f, paint)
                y += 16f
            }

            y += 15f

            // Yogas & Doshas Summary
            paint.textSize = 12f
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            paint.color = Color.rgb(0x2B, 0x2D, 0x5C)
            canvas.drawText(if (isHindi) "प्रमुख योग एवं दोष" else "Key Yogas & Doshas", 36f, y, paint)
            y += 15f

            paint.textSize = 9.5f
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
            kundali.yogasAndDoshas.take(4).forEach { yd ->
                val name = if (isHindi) yd.nameHi else yd.nameEn
                val status = if (isHindi) yd.statusTextHi else yd.statusTextEn
                paint.color = if (yd.isPresent) Color.rgb(0xC7, 0x7C, 0x18) else Color.rgb(0x2E, 0x7D, 0x32)
                canvas.drawText("• $name: $status", 42f, y, paint)
                y += 13f
            }

            y += 15f

            // Lucky Elements & Guidance
            paint.textSize = 12f
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            paint.color = Color.rgb(0x2B, 0x2D, 0x5C)
            canvas.drawText(if (isHindi) "शुभ तत्व एवं वैदिक उपाय" else "Lucky Elements & Remedies", 36f, y, paint)
            y += 15f

            paint.textSize = 9.5f
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
            paint.color = Color.rgb(0x33, 0x33, 0x44)
            val l = kundali.luckyElements
            val luckyLine = if (isHindi) {
                "शुभ अंक: ${l.number}  |  रंग: ${l.color}  |  दिन: ${l.day}  |  रत्न: ${l.gemstone}"
            } else {
                "Lucky No: ${l.number}  |  Color: ${l.color}  |  Day: ${l.day}  |  Gemstone: ${l.gemstone}"
            }
            canvas.drawText(luckyLine, 42f, y, paint)
            y += 14f

            val mantraText = if (isHindi) "वैदिक मंत्र: ${kundali.remedies.mantra}" else "Mantra: ${kundali.remedies.mantra}"
            canvas.drawText(mantraText, 42f, y, paint)

            // Footer Disclaimer
            paint.textSize = 8.5f
            paint.color = Color.GRAY
            val disclaimer = if (isHindi) {
                "केवल मार्गदर्शन के लिए। किसी पेशेवर सलाह का विकल्प नहीं है। • कुंडली गुरु (आर्यलीला स्टूडियोज़)"
            } else {
                "For guidance only. Not a substitute for professional advice. • Kundali Guru (AryaLeela Studios)"
            }
            canvas.drawText(disclaimer, 36f, pageHeight - 30f, paint)

            document.finishPage(page)

            context.contentResolver.openOutputStream(uri)?.use { out ->
                document.writeTo(out)
            }
            document.close()
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
}
