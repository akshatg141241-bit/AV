package com.aryaleelastudios.kundaliguru.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aryaleelastudios.kundaliguru.data.local.PersonEntity
import com.aryaleelastudios.kundaliguru.data.model.*
import com.aryaleelastudios.kundaliguru.ui.components.KundaliChartCanvas
import com.aryaleelastudios.kundaliguru.ui.theme.*

@Composable
fun KundaliScreen(
    person: PersonEntity?,
    kundali: KundaliData?,
    chartStyle: ChartStyle,
    isHindi: Boolean,
    onChartStyleChanged: (ChartStyle) -> Unit,
    onNavigateToTodayPrediction: () -> Unit,
    onNavigateToAddPerson: () -> Unit,
    onEditPerson: (PersonEntity) -> Unit,
    onDeletePerson: (PersonEntity) -> Unit,
    onExportPdf: () -> Unit,
    onShareJson: () -> Unit
) {
    if (person == null || kundali == null) {
        // Empty State: illustration + "No Kundali yet" + "+ Add your first person"
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp)
                .testTag("kundali_empty_state"),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(100.dp)
                        .background(SaffronAccent.copy(alpha = 0.2f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = "🕉", fontSize = 48.sp)
                }

                Spacer(Modifier.height(24.dp))

                Text(
                    text = if (isHindi) "अभी कोई कुंडली नहीं है" else "No Kundali yet",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = DeepIndigo
                )

                Spacer(Modifier.height(8.dp))

                Text(
                    text = if (isHindi) "वैदिक ज्योतिष गणना, ग्रह चक्र, पंचांग एवं विस्तृत भविष्यवाणी देखने के लिए पहली कुंडली जोड़ें।"
                    else "Add your first person to calculate authentic Vedic chart, panchang, dashas, and predictions.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextMuted,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                    lineHeight = 20.sp
                )

                Spacer(Modifier.height(28.dp))

                Button(
                    onClick = onNavigateToAddPerson,
                    colors = ButtonDefaults.buttonColors(containerColor = DeepIndigo, contentColor = SaffronAccent),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .height(50.dp)
                        .testTag("add_first_person_cta")
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(20.dp))
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = if (isHindi) "+ पहली कुंडली जोड़ें" else "+ Add your first person",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                }
            }
        }
        return
    }

    // Main Single Scrollable Page when person is selected
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("kundali_content_list")
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(top = 12.dp, bottom = 32.dp)
    ) {
        // SECTION 1: Today's Prediction Card (2-line teaser + "Read more")
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = androidx.compose.foundation.BorderStroke(1.dp, CreamBorder),
                modifier = Modifier.fillMaxWidth().testTag("section_today_prediction_card")
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.WbSunny, contentDescription = null, tint = SaffronDark, modifier = Modifier.size(20.dp))
                            Spacer(Modifier.width(8.dp))
                            Text(
                                text = if (isHindi) "आज का राशिफल" else "Today's Prediction",
                                fontWeight = FontWeight.Bold,
                                color = DeepIndigo,
                                fontSize = 16.sp
                            )
                        }
                        Text(
                            text = if (isHindi) kundali.lagna.rashiNameHi else kundali.lagna.rashiNameEn,
                            fontWeight = FontWeight.SemiBold,
                            color = SaffronDark,
                            fontSize = 13.sp
                        )
                    }

                    Spacer(Modifier.height(10.dp))

                    Text(
                        text = if (isHindi) kundali.dailyPredictionHi else kundali.dailyPredictionEn,
                        fontSize = 14.sp,
                        maxLines = 2,
                        color = TextPrimary,
                        lineHeight = 20.sp
                    )

                    Spacer(Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(
                            onClick = onNavigateToTodayPrediction,
                            modifier = Modifier.testTag("today_read_more_btn")
                        ) {
                            Text(
                                text = if (isHindi) "विस्तार से पढ़ें →" else "Read more →",
                                color = DeepIndigo,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }

        // SECTION 2: Kundali Chart (North / South Indian toggle + Canvas)
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = androidx.compose.foundation.BorderStroke(1.dp, CreamBorder),
                modifier = Modifier.fillMaxWidth().testTag("section_chart_card")
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = if (isHindi) "लग्न कुंडली चक्र" else "Kundali Birth Chart",
                                fontWeight = FontWeight.Bold,
                                color = DeepIndigo,
                                fontSize = 16.sp
                            )
                            Text(
                                text = if (isHindi) "लग्न: ${kundali.lagna.rashiNameHi} (${kundali.lagna.formattedDegree})"
                                else "Lagna: ${kundali.lagna.rashiNameEn} (${kundali.lagna.formattedDegree})",
                                fontSize = 12.sp,
                                color = TextMuted
                            )
                        }

                        // Segmented control toggle
                        Row(
                            modifier = Modifier
                                .background(CreamBackground, RoundedCornerShape(10.dp))
                                .border(1.dp, CreamBorder, RoundedCornerShape(10.dp))
                                .padding(2.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .background(
                                        if (chartStyle == ChartStyle.NORTH_INDIAN) DeepIndigo else Color.Transparent,
                                        RoundedCornerShape(8.dp)
                                    )
                                    .clickable { onChartStyleChanged(ChartStyle.NORTH_INDIAN) }
                                    .padding(horizontal = 8.dp, vertical = 6.dp)
                                    .testTag("chart_toggle_north")
                            ) {
                                Text(
                                    text = if (isHindi) "उत्तर" else "North",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (chartStyle == ChartStyle.NORTH_INDIAN) SaffronAccent else DeepIndigo
                                )
                            }
                            Box(
                                modifier = Modifier
                                    .background(
                                        if (chartStyle == ChartStyle.SOUTH_INDIAN) DeepIndigo else Color.Transparent,
                                        RoundedCornerShape(8.dp)
                                    )
                                    .clickable { onChartStyleChanged(ChartStyle.SOUTH_INDIAN) }
                                    .padding(horizontal = 8.dp, vertical = 6.dp)
                                    .testTag("chart_toggle_south")
                            ) {
                                Text(
                                    text = if (isHindi) "दक्षिण" else "South",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (chartStyle == ChartStyle.SOUTH_INDIAN) SaffronAccent else DeepIndigo
                                )
                            }
                        }
                    }

                    Spacer(Modifier.height(14.dp))

                    KundaliChartCanvas(
                        kundali = kundali,
                        chartStyle = chartStyle,
                        isHindi = isHindi
                    )
                }
            }
        }

        // SECTION 3: Your Reading (Collapsible cards)
        item {
            Text(
                text = if (isHindi) "आपकी जन्म पत्रिका विवेचना" else "Your Reading",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = DeepIndigo
            )
        }

        item {
            CollapsibleCard(
                title = if (isHindi) "व्यक्तित्व एवं स्वभाव" else "Personality & Demeanor",
                icon = Icons.Default.SelfImprovement,
                initiallyExpanded = true
            ) {
                Text(
                    text = if (isHindi) kundali.personalityReadingHi else kundali.personalityReadingEn,
                    fontSize = 14.sp,
                    lineHeight = 21.sp,
                    color = TextPrimary
                )
            }
        }

        item {
            CollapsibleCard(
                title = if (isHindi) "करियर एवं धन समृद्धि" else "Career & Wealth",
                icon = Icons.Default.WorkOutline
            ) {
                Text(
                    text = if (isHindi) kundali.careerReadingHi else kundali.careerReadingEn,
                    fontSize = 14.sp,
                    lineHeight = 21.sp,
                    color = TextPrimary
                )
            }
        }

        item {
            CollapsibleCard(
                title = if (isHindi) "संबंध एवं दांपत्य जीवन" else "Relationships & Marriage",
                icon = Icons.Default.FavoriteBorder
            ) {
                Text(
                    text = if (isHindi) kundali.relationshipReadingHi else kundali.relationshipReadingEn,
                    fontSize = 14.sp,
                    lineHeight = 21.sp,
                    color = TextPrimary
                )
            }
        }

        item {
            CollapsibleCard(
                title = if (isHindi) "स्वास्थ्य एवं जीवन शक्ति" else "Health & Vitality",
                icon = Icons.Default.HealthAndSafety
            ) {
                Text(
                    text = if (isHindi) kundali.healthReadingHi else kundali.healthReadingEn,
                    fontSize = 14.sp,
                    lineHeight = 21.sp,
                    color = TextPrimary
                )
            }
        }

        item {
            CollapsibleCard(
                title = if (isHindi) "वर्तमान दशा काल (Current Phase)" else "Current Phase (Dasha)",
                icon = Icons.Default.Timelapse
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = if (isHindi) "सक्रिय दशा: ${kundali.currentDashaSummaryHi}" else "Active Dasha: ${kundali.currentDashaSummaryEn}",
                        fontWeight = FontWeight.Bold,
                        color = SaffronDark,
                        fontSize = 15.sp
                    )
                    Text(
                        text = if (isHindi) "विंशोत्तरी दशा चक्र के अनुसार यह काल ज्ञान अर्जन, कार्यक्षेत्र में स्थिरता और व्यावहारिक विवेक को सुदृढ़ करने का है।"
                        else "According to Vimshottari Dasha system, this planetary period brings focus to long-term consolidation and dharmic growth.",
                        fontSize = 13.sp,
                        color = TextPrimary,
                        lineHeight = 19.sp
                    )
                }
            }
        }

        item {
            CollapsibleCard(
                title = if (isHindi) "शुभ तत्व (Lucky Elements)" else "Lucky Elements",
                icon = Icons.Default.Stars
            ) {
                val l = kundali.luckyElements
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    DetailRow(if (isHindi) "शुभ अंक" else "Lucky Number", "${l.number}")
                    DetailRow(if (isHindi) "शुभ रंग" else "Lucky Color", l.color)
                    DetailRow(if (isHindi) "शुभ वार" else "Lucky Day", l.day)
                    DetailRow(if (isHindi) "शुभ दिशा" else "Auspicious Direction", l.direction)
                    DetailRow(if (isHindi) "अनुकूल रत्न" else "Benefic Gemstone", l.gemstone)
                }
            }
        }

        item {
            CollapsibleCard(
                title = if (isHindi) "वैदिक उपाय एवं मार्गदर्शन" else "Remedies & Upay",
                icon = Icons.Default.Spa
            ) {
                val rem = kundali.remedies
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    RemedyItem(if (isHindi) "मंत्र जप" else "Mantra", rem.mantra)
                    RemedyItem(if (isHindi) "दान पुण्य" else "Charity", rem.charity)
                    RemedyItem(if (isHindi) "व्रत / उपवास" else "Fasting", rem.fasting)
                    RemedyItem(if (isHindi) "सदाचार" else "Behavioral", rem.behavioral)
                }
            }
        }

        // SECTION 4: Predictions
        item {
            Text(
                text = if (isHindi) "भविष्यवाणी खंड (Predictions)" else "Predictions",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = DeepIndigo
            )
        }

        item {
            CollapsibleCard(
                title = if (isHindi) "दैनिक राशिफल (Daily)" else "Daily Prediction",
                icon = Icons.Default.Today
            ) {
                Text(
                    text = if (isHindi) kundali.dailyPredictionHi else kundali.dailyPredictionEn,
                    fontSize = 14.sp,
                    lineHeight = 20.sp,
                    color = TextPrimary
                )
            }
        }

        item {
            CollapsibleCard(
                title = if (isHindi) "मासिक राशिफल (Monthly)" else "Monthly Prediction",
                icon = Icons.Default.CalendarMonth
            ) {
                Text(
                    text = if (isHindi) kundali.monthlyPredictionHi else kundali.monthlyPredictionEn,
                    fontSize = 14.sp,
                    lineHeight = 20.sp,
                    color = TextPrimary
                )
            }
        }

        item {
            CollapsibleCard(
                title = if (isHindi) "वार्षिक राशिफल (Yearly)" else "Yearly Prediction",
                icon = Icons.Default.DateRange
            ) {
                Text(
                    text = if (isHindi) kundali.yearlyPredictionHi else kundali.yearlyPredictionEn,
                    fontSize = 14.sp,
                    lineHeight = 20.sp,
                    color = TextPrimary
                )
            }
        }

        item {
            CollapsibleCard(
                title = if (isHindi) "जीवन मार्गदर्शन (Life Path)" else "Life Prediction",
                icon = Icons.Default.Explore
            ) {
                Text(
                    text = if (isHindi) kundali.lifePredictionHi else kundali.lifePredictionEn,
                    fontSize = 14.sp,
                    lineHeight = 20.sp,
                    color = TextPrimary
                )
            }
        }

        // SECTION 5: Detailed Charts (Planets, Bhava, Dasha, Panchang, Yogas, Divisionals)
        item {
            Text(
                text = if (isHindi) "विस्तृत ज्योतिषीय सारणियां" else "Detailed Astrological Charts",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = DeepIndigo
            )
        }

        // Planets Table
        item {
            CollapsibleCard(
                title = if (isHindi) "ग्रह स्थिति सारणी" else "Planets (Sign, Degree, Retro, Nakshatra)",
                icon = Icons.Default.Public
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    // Header row
                    Row(
                        modifier = Modifier.fillMaxWidth().background(CreamBackground).padding(6.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(if (isHindi) "ग्रह" else "Planet", fontWeight = FontWeight.Bold, fontSize = 11.sp, modifier = Modifier.weight(1.2f))
                        Text(if (isHindi) "राशि" else "Sign", fontWeight = FontWeight.Bold, fontSize = 11.sp, modifier = Modifier.weight(1.2f))
                        Text(if (isHindi) "अंश" else "Deg", fontWeight = FontWeight.Bold, fontSize = 11.sp, modifier = Modifier.weight(1.2f))
                        Text(if (isHindi) "नक्षत्र" else "Nakshatra", fontWeight = FontWeight.Bold, fontSize = 11.sp, modifier = Modifier.weight(1.4f))
                    }

                    kundali.planets.forEach { p ->
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(horizontal = 6.dp, vertical = 3.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "${p.glyph} ${if (isHindi) p.nameHi else p.nameEn}${if (p.isRetrograde) "[R]" else ""}",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium,
                                color = DeepIndigo,
                                modifier = Modifier.weight(1.2f)
                            )
                            Text(
                                text = if (isHindi) p.rashiNameHi else p.rashiNameEn,
                                fontSize = 12.sp,
                                color = TextPrimary,
                                modifier = Modifier.weight(1.2f)
                            )
                            Text(
                                text = p.formattedDegree,
                                fontSize = 11.sp,
                                color = TextMuted,
                                modifier = Modifier.weight(1.2f)
                            )
                            Text(
                                text = "${if (isHindi) p.nakshatraNameHi else p.nakshatraNameEn} (${p.pada})",
                                fontSize = 11.sp,
                                color = SaffronDark,
                                modifier = Modifier.weight(1.4f)
                            )
                        }
                    }
                }
            }
        }

        // Bhava House Cusps
        item {
            CollapsibleCard(
                title = if (isHindi) "भाव कस्प (House Cusps 1 to 12)" else "Bhava (House Cusps)",
                icon = Icons.Default.HomeWork
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    kundali.houses.forEach { h ->
                        DetailRow(
                            label = "${if (isHindi) "भाव" else "House"} ${h.houseNumber}",
                            value = "${if (isHindi) h.rashiNameHi else h.rashiNameEn} (${h.formattedDegree})"
                        )
                    }
                }
            }
        }

        // Vimshottari Dasha
        item {
            CollapsibleCard(
                title = if (isHindi) "विंशोत्तरी महादशा एवं अंतर्दशा" else "Vimshottari Dasha (Maha -> Antar)",
                icon = Icons.Default.Timeline
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    kundali.vimshottariDasha.forEach { dasha ->
                        Card(
                            shape = RoundedCornerShape(10.dp),
                            colors = CardDefaults.cardColors(containerColor = CreamBackground),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = "${if (isHindi) dasha.planetHi else dasha.planetEn} ${if (isHindi) "महादशा" else "Maha Dasha"}",
                                        fontWeight = FontWeight.Bold,
                                        color = DeepIndigo,
                                        fontSize = 13.sp
                                    )
                                    Text(
                                        text = "${dasha.startDate} - ${dasha.endDate}",
                                        fontSize = 11.sp,
                                        color = TextMuted
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Panchang at Birth
        item {
            CollapsibleCard(
                title = if (isHindi) "जन्म पंचांग (Tithi, Vara, Yoga, Karana)" else "Panchang at Birth",
                icon = Icons.Default.CalendarToday
            ) {
                val p = kundali.panchang
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    DetailRow(if (isHindi) "तिथि" else "Tithi", "${if (isHindi) p.tithiHi else p.tithiEn} (${if (isHindi) p.pakshaHi else p.pakshaEn})")
                    DetailRow(if (isHindi) "वार" else "Vara (Day)", if (isHindi) p.varaHi else p.varaEn)
                    DetailRow(if (isHindi) "नक्षत्र" else "Nakshatra", if (isHindi) p.nakshatraHi else p.nakshatraEn)
                    DetailRow(if (isHindi) "योग" else "Yoga", if (isHindi) p.yogaHi else p.yogaEn)
                    DetailRow(if (isHindi) "करण" else "Karana", if (isHindi) p.karanaHi else p.karanaEn)
                    DetailRow(if (isHindi) "सूर्योदय / सूर्यास्त" else "Sunrise / Sunset", "${p.sunrise} / ${p.sunset}")
                }
            }
        }

        // Yogas & Doshas
        item {
            CollapsibleCard(
                title = if (isHindi) "योग एवं दोष (Mangal, Kaal Sarp, Sade Sati, etc.)" else "Yogas & Doshas",
                icon = Icons.Default.Shield
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    kundali.yogasAndDoshas.forEach { yd ->
                        Card(
                            shape = RoundedCornerShape(10.dp),
                            colors = CardDefaults.cardColors(containerColor = CreamBackground),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = if (isHindi) yd.nameHi else yd.nameEn,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp,
                                        color = DeepIndigo
                                    )
                                    Text(
                                        text = if (isHindi) yd.statusTextHi else yd.statusTextEn,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 11.sp,
                                        color = if (yd.isPresent) SaffronDark else AuspiciousGreen
                                    )
                                }
                                Spacer(Modifier.height(4.dp))
                                Text(
                                    text = if (isHindi) yd.descriptionHi else yd.descriptionEn,
                                    fontSize = 12.sp,
                                    color = TextPrimary,
                                    lineHeight = 17.sp
                                )
                                Spacer(Modifier.height(4.dp))
                                Text(
                                    text = "${if (isHindi) "उपाय:" else "Remedy:"} ${if (isHindi) yd.remedyHi else yd.remedyEn}",
                                    fontSize = 11.sp,
                                    color = DeepIndigo,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }
                }
            }
        }

        // Divisional charts: D9 Navamsa, D10 Dashamsa, D7 Saptamsa
        item {
            CollapsibleCard(
                title = if (isHindi) "वर्ग कुंडलियां (D9, D10, D7 Divisional Charts)" else "Divisional Charts (D9, D10, D7)",
                icon = Icons.Default.GridView
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    kundali.divisionalCharts.forEach { div ->
                        Card(
                            shape = RoundedCornerShape(10.dp),
                            colors = CardDefaults.cardColors(containerColor = CreamBackground),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text(
                                    text = if (isHindi) div.titleHi else div.titleEn,
                                    fontWeight = FontWeight.Bold,
                                    color = DeepIndigo,
                                    fontSize = 13.sp
                                )
                                Text(
                                    text = if (isHindi) div.descriptionHi else div.descriptionEn,
                                    fontSize = 11.sp,
                                    color = TextMuted,
                                    lineHeight = 15.sp
                                )
                                Spacer(Modifier.height(8.dp))
                                Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                                    div.placements.chunked(2).forEach { chunk ->
                                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                            chunk.forEach { p ->
                                                Text(
                                                    text = "${if (isHindi) p.planetHi else p.planetEn}: ${if (isHindi) p.signNameHi else p.signNameEn}",
                                                    fontSize = 11.sp,
                                                    color = DeepIndigo,
                                                    modifier = Modifier.weight(1f)
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // SECTION 6: Actions (PDF export, Share JSON, Edit, Delete)
        item {
            Text(
                text = if (isHindi) "कार्य एवं शेयर (Actions)" else "Actions & Export",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = DeepIndigo
            )
        }

        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = androidx.compose.foundation.BorderStroke(1.dp, CreamBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(
                        onClick = onExportPdf,
                        colors = ButtonDefaults.buttonColors(containerColor = DeepIndigo, contentColor = SaffronAccent),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth().height(48.dp).testTag("action_export_pdf_btn")
                    ) {
                        Icon(Icons.Default.PictureAsPdf, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        Text(if (isHindi) "पीडीएफ रिपोर्ट एक्सपोर्ट करें (PDF via SAF)" else "Export PDF Chart (via SAF)", fontWeight = FontWeight.Bold)
                    }

                    OutlinedButton(
                        onClick = onShareJson,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth().height(48.dp).testTag("action_share_json_btn")
                    ) {
                        Icon(Icons.Default.Share, contentDescription = null, tint = DeepIndigo, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        Text(if (isHindi) "कुंडली डेटा शेयर करें (Share JSON)" else "Share Kundali Data (JSON)", color = DeepIndigo)
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = { onEditPerson(person) },
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.weight(1f).height(44.dp).testTag("action_edit_person_btn")
                        ) {
                            Icon(Icons.Default.Edit, contentDescription = null, tint = DeepIndigo, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(6.dp))
                            Text(if (isHindi) "संपादित करें" else "Edit", color = DeepIndigo)
                        }

                        OutlinedButton(
                            onClick = { onDeletePerson(person) },
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error),
                            modifier = Modifier.weight(1f).height(44.dp).testTag("action_delete_person_btn")
                        ) {
                            Icon(Icons.Default.Delete, contentDescription = null, tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(6.dp))
                            Text(if (isHindi) "हटाएं" else "Delete", color = MaterialTheme.colorScheme.error)
                        }
                    }
                }
            }
        }

        // Disclaimer at bottom
        item {
            Text(
                text = if (isHindi) "केवल मार्गदर्शन के लिए। किसी पेशेवर सलाह का विकल्प नहीं है।"
                else "For guidance only. Not a substitute for professional advice.",
                fontSize = 11.sp,
                color = TextMuted,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
            )
        }
    }
}

@Composable
fun CollapsibleCard(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    initiallyExpanded: Boolean = false,
    content: @Composable () -> Unit
) {
    var isExpanded by remember { mutableStateOf(initiallyExpanded) }

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = androidx.compose.foundation.BorderStroke(1.dp, CreamBorder),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { isExpanded = !isExpanded },
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(icon, contentDescription = null, tint = SaffronDark, modifier = Modifier.size(20.dp))
                    Spacer(Modifier.width(10.dp))
                    Text(
                        text = title,
                        fontWeight = FontWeight.Bold,
                        color = DeepIndigo,
                        fontSize = 14.sp
                    )
                }
                Icon(
                    imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = if (isExpanded) "Collapse" else "Expand",
                    tint = DeepIndigo
                )
            }

            AnimatedVisibility(visible = isExpanded) {
                Column(modifier = Modifier.padding(top = 12.dp)) {
                    content()
                }
            }
        }
    }
}

@Composable
private fun DetailRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, fontSize = 12.sp, color = TextMuted)
        Text(value, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = DeepIndigo)
    }
}

@Composable
private fun RemedyItem(title: String, desc: String) {
    Column {
        Text(title, fontWeight = FontWeight.Bold, fontSize = 12.sp, color = SaffronDark)
        Text(desc, fontSize = 12.sp, color = TextPrimary, lineHeight = 17.sp)
    }
}
