package com.aryaleelastudios.astroveda.ui.screens

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aryaleelastudios.astroveda.R
import com.aryaleelastudios.astroveda.astro.AshtaKootaEngine
import com.aryaleelastudios.astroveda.astro.AstroEngine
import com.aryaleelastudios.astroveda.data.local.PersonEntity
import com.aryaleelastudios.astroveda.data.model.AyanamsaType
import com.aryaleelastudios.astroveda.data.repository.ContentRepository
import com.aryaleelastudios.astroveda.ui.components.clearFocusOnTap
import com.aryaleelastudios.astroveda.ui.theme.AuspiciousGreen
import com.aryaleelastudios.astroveda.ui.theme.CreamBackground
import com.aryaleelastudios.astroveda.ui.theme.CreamBorder
import com.aryaleelastudios.astroveda.ui.theme.DeepIndigo
import com.aryaleelastudios.astroveda.ui.theme.MutedRed
import com.aryaleelastudios.astroveda.ui.theme.NeutralGold
import com.aryaleelastudios.astroveda.ui.theme.SaffronAccent
import com.aryaleelastudios.astroveda.ui.theme.SaffronDark
import com.aryaleelastudios.astroveda.ui.theme.TextMuted
import com.aryaleelastudios.astroveda.ui.theme.TextPrimary
import com.aryaleelastudios.astroveda.ui.theme.astroPrimaryButtonColors

@Composable
fun ShaadiMilanScreen(
    persons: List<PersonEntity>,
    ayanamsa: AyanamsaType,
    languageCode: String,
    contentRepo: ContentRepository,
    onNavigateToAddPerson: () -> Unit
) {
    if (persons.size < 2) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clearFocusOnTap()
                .padding(24.dp)
                .testTag("milan_empty_state"),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier.widthIn(max = 460.dp)
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
                    text = stringResource(R.string.milan_empty_title),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = DeepIndigo,
                    textAlign = TextAlign.Center
                )

                Spacer(Modifier.height(8.dp))

                Text(
                    text = stringResource(R.string.milan_empty_subtitle),
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextMuted,
                    textAlign = TextAlign.Center
                )

                Spacer(Modifier.height(24.dp))

                Button(
                    onClick = onNavigateToAddPerson,
                    colors = astroPrimaryButtonColors(),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .heightIn(min = 48.dp)
                        .testTag("milan_add_person_cta")
                ) {
                    Icon(Icons.Default.PersonAdd, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text(stringResource(R.string.milan_add_person_cta), fontWeight = FontWeight.Bold)
                }
            }
        }
        return
    }

    var selectedPersonAId by remember { mutableLongStateOf(persons[0].id) }
    var selectedPersonBId by remember { mutableLongStateOf(persons[1].id) }

    val personA = remember(persons, selectedPersonAId) { persons.find { it.id == selectedPersonAId } ?: persons[0] }
    val personB = remember(persons, selectedPersonBId) { persons.find { it.id == selectedPersonBId } ?: persons[1] }

    val matchResult = remember(personA, personB, ayanamsa, languageCode) {
        val kundaliA = AstroEngine.computeKundali(
            birthDateStr = personA.birthDate,
            birthTimeStr = personA.birthTime,
            isTimeUnknown = personA.isTimeUnknown,
            lat = personA.latitude,
            lon = personA.longitude,
            tzId = personA.timezoneId,
            ayanamsaType = ayanamsa,
            langCode = languageCode,
            contentRepo = contentRepo,
            person = personA
        )
        val kundaliB = AstroEngine.computeKundali(
            birthDateStr = personB.birthDate,
            birthTimeStr = personB.birthTime,
            isTimeUnknown = personB.isTimeUnknown,
            lat = personB.latitude,
            lon = personB.longitude,
            tzId = personB.timezoneId,
            ayanamsaType = ayanamsa,
            langCode = languageCode,
            contentRepo = contentRepo,
            person = personB
        )

        val moonA = kundaliA.planets.first { it.canonicalName == "Moon" }
        val moonB = kundaliB.planets.first { it.canonicalName == "Moon" }
        val isManglikA = kundaliA.yogasAndDoshas.find { it.canonicalKey == "Mangal" }?.isPresent ?: false
        val isManglikB = kundaliB.yogasAndDoshas.find { it.canonicalKey == "Mangal" }?.isPresent ?: false

        AshtaKootaEngine.matchHoroscopes(
            groomName = personA.name,
            groomRashiIdx = moonA.rashiIndex,
            groomNakshatraIdx = moonA.nakshatraIndex,
            groomIsManglik = isManglikA,
            brideName = personB.name,
            brideRashiIdx = moonB.rashiIndex,
            brideNakshatraIdx = moonB.nakshatraIndex,
            brideIsManglik = isManglikB,
            langCode = languageCode,
            contentRepo = contentRepo
        )
    }

    var showPersonASelector by remember { mutableStateOf(false) }
    var showPersonBSelector by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .clearFocusOnTap(),
        contentAlignment = Alignment.TopCenter
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = 600.dp)
                .verticalScroll(rememberScrollState())
                .padding(20.dp)
                .testTag("shaadi_milan_screen"),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = stringResource(R.string.milan_select_profiles),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = DeepIndigo
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Card(
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = BorderStroke(1.5.dp, DeepIndigo),
                    modifier = Modifier
                        .weight(1f)
                        .heightIn(min = 64.dp)
                        .clickable { showPersonASelector = true }
                        .testTag("milan_select_partner_1")
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(stringResource(R.string.milan_partner_1), fontSize = 11.sp, color = TextMuted)
                        Spacer(Modifier.height(4.dp))
                        Text(personA.name, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = DeepIndigo, maxLines = 1)
                        Text("${matchResult.groomRashi} • ${matchResult.groomNakshatra}", fontSize = 11.sp, color = SaffronDark, maxLines = 1)
                    }
                }

                Card(
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = BorderStroke(1.5.dp, DeepIndigo),
                    modifier = Modifier
                        .weight(1f)
                        .heightIn(min = 64.dp)
                        .clickable { showPersonBSelector = true }
                        .testTag("milan_select_partner_2")
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(stringResource(R.string.milan_partner_2), fontSize = 11.sp, color = TextMuted)
                        Spacer(Modifier.height(4.dp))
                        Text(personB.name, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = DeepIndigo, maxLines = 1)
                        Text("${matchResult.brideRashi} • ${matchResult.brideNakshatra}", fontSize = 11.sp, color = SaffronDark, maxLines = 1)
                    }
                }
            }

            // 0–36 Circular Dial
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = BorderStroke(1.dp, CreamBorder),
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

                    val scoreColor = when {
                        matchResult.totalScore >= 28 -> AuspiciousGreen
                        matchResult.totalScore >= 18 -> NeutralGold
                        else -> MutedRed
                    }

                    Box(
                        modifier = Modifier.size(170.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Canvas(modifier = Modifier.fillMaxSize().padding(10.dp)) {
                            val strokeW = 14f
                            drawArc(
                                color = Color(0xFFEFE8DB),
                                startAngle = 150f,
                                sweepAngle = 240f,
                                useCenter = false,
                                style = Stroke(width = strokeW, cap = StrokeCap.Round)
                            )
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
                                style = MaterialTheme.typography.displayLarge,
                                fontSize = 38.sp,
                                fontWeight = FontWeight.Bold,
                                color = scoreColor
                            )
                            Text(
                                text = stringResource(R.string.milan_gunas_unit),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = TextMuted
                            )
                        }
                    }

                    Spacer(Modifier.height(10.dp))

                    Text(
                        text = matchResult.verdict,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        color = scoreColor,
                        textAlign = TextAlign.Center
                    )

                    Spacer(Modifier.height(6.dp))

                    Text(
                        text = matchResult.verdictDetail,
                        fontSize = 13.sp,
                        color = TextPrimary,
                        textAlign = TextAlign.Center,
                        lineHeight = 18.sp
                    )
                }
            }

            // Mangal Dosha Compatibility
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
                            text = stringResource(R.string.milan_mangal_title),
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = DeepIndigo
                        )
                    }
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = matchResult.mangalDoshaAnalysis,
                        fontSize = 13.sp,
                        color = TextPrimary,
                        lineHeight = 19.sp
                    )
                }
            }

            // 8-Koota Breakdown
            Text(
                text = stringResource(R.string.milan_breakdown_title),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = DeepIndigo
            )

            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = BorderStroke(1.dp, CreamBorder),
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
                                    text = koota.name,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    color = DeepIndigo
                                )
                                Text(
                                    text = "${koota.score.toInt()} / ${koota.maxScore.toInt()}",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    color = when {
                                        koota.score == koota.maxScore -> AuspiciousGreen
                                        koota.score > 0 -> NeutralGold
                                        else -> MutedRed
                                    }
                                )
                            }
                            Spacer(Modifier.height(3.dp))
                            Text(
                                text = koota.meaning,
                                fontSize = 12.sp,
                                color = TextMuted,
                                lineHeight = 16.sp
                            )
                            Spacer(Modifier.height(6.dp))
                            LinearProgressIndicator(
                                progress = { (koota.score / koota.maxScore).toFloat().coerceIn(0f, 1f) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(6.dp),
                                color = if (koota.score == koota.maxScore) AuspiciousGreen else SaffronDark,
                                trackColor = Color(0xFFF1EAE0)
                            )
                        }
                    }
                }
            }
        }
    }

    if (showPersonASelector) {
        SelectPersonDialog(
            title = stringResource(R.string.milan_select_partner_1),
            persons = persons,
            selectedId = selectedPersonAId,
            onSelect = { selectedPersonAId = it },
            onDismiss = { showPersonASelector = false }
        )
    }

    if (showPersonBSelector) {
        SelectPersonDialog(
            title = stringResource(R.string.milan_select_partner_2),
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
        title = {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = DeepIndigo
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                persons.forEach { p ->
                    val isSelected = p.id == selectedId
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = if (isSelected) SaffronAccent.copy(alpha = 0.2f) else Color.White,
                        border = BorderStroke(1.dp, if (isSelected) SaffronDark else CreamBorder),
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 48.dp)
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
                Text(stringResource(R.string.close_btn), color = DeepIndigo)
            }
        },
        containerColor = CreamBackground,
        shape = RoundedCornerShape(16.dp)
    )
}
