package com.aryaleelastudios.kundaliguru.ui.screens

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aryaleelastudios.kundaliguru.astro.AshtaKootaEngine
import com.aryaleelastudios.kundaliguru.astro.AstroEngine
import com.aryaleelastudios.kundaliguru.astro.PredictionsRepository
import com.aryaleelastudios.kundaliguru.data.local.PersonEntity
import com.aryaleelastudios.kundaliguru.data.model.AshtaKootaResult
import com.aryaleelastudios.kundaliguru.data.model.AyanamsaType
import com.aryaleelastudios.kundaliguru.ui.theme.*

@Composable
fun ShaadiMilanScreen(
    persons: List<PersonEntity>,
    isHindi: Boolean,
    predictionsRepo: PredictionsRepository,
    onNavigateToAddPerson: () -> Unit
) {
    if (persons.size < 2) {
        // Empty state if <2 persons
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp)
                .testTag("milan_empty_state"),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(88.dp)
                        .background(SaffronAccent.copy(alpha = 0.2f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Favorite,
                        contentDescription = null,
                        tint = DeepIndigo,
                        modifier = Modifier.size(44.dp)
                    )
                }

                Spacer(Modifier.height(20.dp))

                Text(
                    text = if (isHindi) "मिलान के लिए कम से कम 2 व्यक्तियों को जोड़ें" else "Add at least 2 people to match",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = DeepIndigo
                )

                Spacer(Modifier.height(8.dp))

                Text(
                    text = if (isHindi) "विवाह गुण मिलान (३६ गुण एवं मांगलिक विचार) हेतु दो व्यक्तियों की जन्म कुंडली आवश्यक है।"
                    else "Kundali matching evaluates 36 Gunas and Mangal Dosha compatibility between two saved profiles.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextMuted,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )

                Spacer(Modifier.height(24.dp))

                Button(
                    onClick = onNavigateToAddPerson,
                    colors = ButtonDefaults.buttonColors(containerColor = DeepIndigo, contentColor = SaffronAccent),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.testTag("milan_add_person_cta")
                ) {
                    Icon(Icons.Default.PersonAdd, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text(if (isHindi) "+ नई कुंडली जोड़ें" else "+ Add Person", fontWeight = FontWeight.Bold)
                }
            }
        }
        return
    }

    var selectedPersonAId by remember { mutableLongStateOf(persons[0].id) }
    var selectedPersonBId by remember { mutableLongStateOf(persons[1].id) }

    val personA = remember(persons, selectedPersonAId) { persons.find { it.id == selectedPersonAId } ?: persons[0] }
    val personB = remember(persons, selectedPersonBId) { persons.find { it.id == selectedPersonBId } ?: persons[1] }

    val matchResult = remember(personA, personB) {
        val kundaliA = AstroEngine.computeKundali(
            personA.birthDate, personA.birthTime, personA.isTimeUnknown,
            personA.latitude, personA.longitude, personA.timezoneId,
            AyanamsaType.LAHIRI, predictionsRepo
        )
        val kundaliB = AstroEngine.computeKundali(
            personB.birthDate, personB.birthTime, personB.isTimeUnknown,
            personB.latitude, personB.longitude, personB.timezoneId,
            AyanamsaType.LAHIRI, predictionsRepo
        )

        val moonA = kundaliA.planets.first { it.nameEn == "Moon" }
        val moonB = kundaliB.planets.first { it.nameEn == "Moon" }
        val isManglikA = kundaliA.yogasAndDoshas.find { it.nameEn.contains("Mangal") }?.isPresent ?: false
        val isManglikB = kundaliB.yogasAndDoshas.find { it.nameEn.contains("Mangal") }?.isPresent ?: false

        AshtaKootaEngine.matchHoroscopes(
            groomName = personA.name,
            groomRashiIdx = moonA.rashiIndex,
            groomNakshatraIdx = moonA.nakshatraIndex,
            groomIsManglik = isManglikA,
            brideName = personB.name,
            brideRashiIdx = moonB.rashiIndex,
            brideNakshatraIdx = moonB.nakshatraIndex,
            brideIsManglik = isManglikB
        )
    }

    var showPersonASelector by remember { mutableStateOf(false) }
    var showPersonBSelector by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(20.dp)
            .testTag("shaadi_milan_screen"),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Person Selectors A & B
        Text(
            text = if (isHindi) "वर एवं कन्या की कुंडली चुनें" else "Select Profiles to Match",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = DeepIndigo
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Partner 1 Card
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = androidx.compose.foundation.BorderStroke(1.5.dp, DeepIndigo),
                modifier = Modifier
                    .weight(1f)
                    .clickable { showPersonASelector = true }
                    .testTag("milan_select_partner_1")
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(if (isHindi) "प्रथम साथी" else "Partner 1", fontSize = 11.sp, color = TextMuted)
                    Spacer(Modifier.height(4.dp))
                    Text(personA.name, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = DeepIndigo, maxLines = 1)
                    Text("${matchResult.groomRashiEn} • ${matchResult.groomNakshatraEn}", fontSize = 11.sp, color = SaffronDark, maxLines = 1)
                }
            }

            // Partner 2 Card
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = androidx.compose.foundation.BorderStroke(1.5.dp, DeepIndigo),
                modifier = Modifier
                    .weight(1f)
                    .clickable { showPersonBSelector = true }
                    .testTag("milan_select_partner_2")
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(if (isHindi) "द्वितीय साथी" else "Partner 2", fontSize = 11.sp, color = TextMuted)
                    Spacer(Modifier.height(4.dp))
                    Text(personB.name, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = DeepIndigo, maxLines = 1)
                    Text("${matchResult.brideRashiEn} • ${matchResult.brideNakshatraEn}", fontSize = 11.sp, color = SaffronDark, maxLines = 1)
                }
            }
        }

        // Circular Dial 0–36
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            border = androidx.compose.foundation.BorderStroke(1.dp, CreamBorder),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                val animatedProgress by animateFloatAsState(
                    targetValue = (matchResult.totalScore / 36.0).toFloat().coerceIn(0f, 1f),
                    animationSpec = tween(durationMillis = 1000),
                    label = "gauge_progress"
                )

                Box(
                    modifier = Modifier.size(170.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Canvas(modifier = Modifier.fillMaxSize().padding(10.dp)) {
                        val strokeW = 14f
                        // Track Arc (240 degrees from 150 to 390)
                        drawArc(
                            color = Color(0xFFEFE8DB),
                            startAngle = 150f,
                            sweepAngle = 240f,
                            useCenter = false,
                            style = Stroke(width = strokeW, cap = StrokeCap.Round)
                        )
                        // Progress Arc
                        val scoreColor = when {
                            matchResult.totalScore >= 28 -> AuspiciousGreen
                            matchResult.totalScore >= 18 -> NeutralGold
                            else -> InauspiciousRed
                        }
                        drawArc(
                            color = scoreColor,
                            startAngle = 150f,
                            sweepAngle = 240f * animatedProgress,
                            useCenter = false,
                            style = Stroke(width = strokeW, cap = StrokeCap.Round)
                        )
                    }

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "${matchResult.totalScore.toInt()}",
                            fontSize = 38.sp,
                            fontWeight = FontWeight.Bold,
                            color = DeepIndigo
                        )
                        Text(
                            text = "/ 36 Gunas",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = TextMuted
                        )
                    }
                }

                Spacer(Modifier.height(10.dp))

                Text(
                    text = if (isHindi) matchResult.verdictHi else matchResult.verdictEn,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    color = when {
                        matchResult.totalScore >= 28 -> AuspiciousGreen
                        matchResult.totalScore >= 18 -> NeutralGold
                        else -> InauspiciousRed
                    }
                )

                Spacer(Modifier.height(6.dp))

                Text(
                    text = if (isHindi) matchResult.verdictDetailHi else matchResult.verdictDetailEn,
                    fontSize = 13.sp,
                    color = TextPrimary,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                    lineHeight = 18.sp
                )
            }
        }

        // Mangal Dosha Compatibility Note
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = SaffronAccent.copy(alpha = 0.15f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Shield, contentDescription = null, tint = SaffronDark, modifier = Modifier.size(20.dp))
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = if (isHindi) "मांगलिक दोष विचार" else "Mangal Dosha Compatibility",
                        fontWeight = FontWeight.Bold,
                        color = DeepIndigo,
                        fontSize = 15.sp
                    )
                }
                Spacer(Modifier.height(8.dp))
                Text(
                    text = if (isHindi) matchResult.mangalDoshaAnalysisHi else matchResult.mangalDoshaAnalysisEn,
                    fontSize = 13.sp,
                    color = TextPrimary,
                    lineHeight = 19.sp
                )
            }
        }

        // 8-Koota Breakdown
        Text(
            text = if (isHindi) "अष्टकूट मिलान विवरण (८ कूट)" else "8-Koota Detailed Breakdown",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = DeepIndigo
        )

        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            border = androidx.compose.foundation.BorderStroke(1.dp, CreamBorder),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                matchResult.kootas.forEach { koota ->
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = if (isHindi) koota.nameHi else koota.nameEn,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = DeepIndigo
                            )
                            Text(
                                text = "${koota.score.toInt()} / ${koota.maxScore.toInt()}",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = if (koota.score == koota.maxScore) AuspiciousGreen else if (koota.score > 0) NeutralGold else InauspiciousRed
                            )
                        }
                        Spacer(Modifier.height(3.dp))
                        Text(
                            text = if (isHindi) koota.meaningHi else koota.meaningEn,
                            fontSize = 12.sp,
                            color = TextMuted,
                            lineHeight = 16.sp
                        )
                        Spacer(Modifier.height(6.dp))
                        LinearProgressIndicator(
                            progress = { (koota.score / koota.maxScore).toFloat() },
                            modifier = Modifier.fillMaxWidth().height(6.dp),
                            color = if (koota.score == koota.maxScore) AuspiciousGreen else SaffronDark,
                            trackColor = Color(0xFFF1EAE0)
                        )
                    }
                }
            }
        }
    }

    // Dropdown Dialog for Partner 1
    if (showPersonASelector) {
        SelectPersonDialog(
            title = if (isHindi) "प्रथम साथी चुनें" else "Select Partner 1",
            persons = persons,
            selectedId = selectedPersonAId,
            onSelect = { selectedPersonAId = it },
            onDismiss = { showPersonASelector = false }
        )
    }

    // Dropdown Dialog for Partner 2
    if (showPersonBSelector) {
        SelectPersonDialog(
            title = if (isHindi) "द्वितीय साथी चुनें" else "Select Partner 2",
            persons = persons,
            selectedId = selectedPersonBId,
            onSelect = { selectedPersonBId = it },
            onDismiss = { showPersonBSelector = false }
        )
    }
}

@Composable
private fun SelectPersonDialog(
    title: String,
    persons: List<PersonEntity>,
    selectedId: Long,
    onSelect: (Long) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title, fontWeight = FontWeight.Bold, color = DeepIndigo) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                persons.forEach { p ->
                    val isSelected = p.id == selectedId
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = if (isSelected) SaffronAccent.copy(alpha = 0.2f) else Color.White,
                        border = androidx.compose.foundation.BorderStroke(1.dp, if (isSelected) SaffronDark else CreamBorder),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                onSelect(p.id)
                                onDismiss()
                            }
                    ) {
                        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                            RadioButton(selected = isSelected, onClick = { onSelect(p.id); onDismiss() })
                            Spacer(Modifier.width(8.dp))
                            Text(p.name, fontWeight = FontWeight.SemiBold, color = DeepIndigo)
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Close", color = DeepIndigo)
            }
        },
        containerColor = CreamBackground,
        shape = RoundedCornerShape(16.dp)
    )
}
