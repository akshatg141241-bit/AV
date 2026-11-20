package com.aryaleelastudios.kundaliguru.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aryaleelastudios.kundaliguru.data.local.PersonEntity
import com.aryaleelastudios.kundaliguru.data.model.KundaliData
import com.aryaleelastudios.kundaliguru.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TodayPredictionScreen(
    person: PersonEntity,
    kundali: KundaliData,
    isHindi: Boolean,
    onNavigateBack: () -> Unit
) {
    val dateFmt = SimpleDateFormat("EEEE, dd MMMM yyyy", if (isHindi) Locale("hi", "IN") else Locale.ENGLISH)
    val todayDateFormatted = dateFmt.format(Date())

    val rashiName = if (isHindi) kundali.lagna.rashiNameHi else kundali.lagna.rashiNameEn
    val dailyPrediction = if (isHindi) kundali.dailyPredictionHi else kundali.dailyPredictionEn

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = if (isHindi) "आज का राशिफल" else "Today's Prediction",
                        fontWeight = FontWeight.Bold,
                        color = DeepIndigo
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack, modifier = Modifier.testTag("today_back_btn")) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = DeepIndigo)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = CreamBackground)
            )
        },
        containerColor = CreamBackground,
        modifier = Modifier.testTag("today_prediction_screen")
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Hero Card
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = DeepIndigo),
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            brush = Brush.verticalGradient(
                                colors = listOf(DeepIndigo, IndigoContainer)
                            )
                        )
                        .padding(20.dp)
                ) {
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = person.name,
                                    fontSize = 22.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = SaffronAccent
                                )
                                Text(
                                    text = if (isHindi) "राशि: $rashiName" else "Sign: $rashiName",
                                    fontSize = 14.sp,
                                    color = Color.White.copy(alpha = 0.85f)
                                )
                            }
                            Box(
                                modifier = Modifier
                                    .size(48.dp)
                                    .background(SaffronAccent.copy(alpha = 0.2f), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "☉",
                                    fontSize = 24.sp,
                                    color = SaffronAccent
                                )
                            }
                        }

                        Spacer(Modifier.height(14.dp))
                        HorizontalDivider(color = Color.White.copy(alpha = 0.15f))
                        Spacer(Modifier.height(14.dp))

                        Text(
                            text = todayDateFormatted,
                            fontSize = 13.sp,
                            color = SaffronLight,
                            fontWeight = FontWeight.Medium
                        )
                        Spacer(Modifier.height(6.dp))
                        Text(
                            text = if (isHindi) "दैनिक ब्रह्मांडीय ऊर्जा प्रभाव" else "Daily Cosmic Theme & Energy",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color.White
                        )
                    }
                }
            }

            // Daily Insight Body Card
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = androidx.compose.foundation.BorderStroke(1.dp, CreamBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = SaffronDark, modifier = Modifier.size(22.dp))
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text = if (isHindi) "आज का मार्गदर्शन" else "Today's Guidance",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = DeepIndigo
                        )
                    }
                    Spacer(Modifier.height(12.dp))
                    Text(
                        text = dailyPrediction,
                        fontSize = 15.sp,
                        lineHeight = 22.sp,
                        color = TextPrimary
                    )

                    Spacer(Modifier.height(14.dp))
                    HorizontalDivider(color = CreamBorder)
                    Spacer(Modifier.height(12.dp))

                    // 1-line actionable tip
                    Row(verticalAlignment = Alignment.Top) {
                        Icon(Icons.Default.Lightbulb, contentDescription = null, tint = SaffronDark, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text = if (isHindi) "सकारात्मक टिप: सुबह कुछ समय शांत बैठकर ईश्वर स्मरण करें और वाणी में मिठास बनाए रखें।"
                            else "Actionable Tip: Spend a few quiet morning moments in prayer or meditation, and maintain gentle warmth in all interactions.",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            color = DeepIndigo
                        )
                    }
                }
            }

            // Lucky Elements Card
            Text(
                text = if (isHindi) "आज के शुभ तत्व" else "Today's Lucky Elements",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = DeepIndigo
            )

            val l = kundali.luckyElements
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                LuckyElementBox(
                    label = if (isHindi) "शुभ अंक" else "Lucky No.",
                    value = "${l.number}",
                    icon = Icons.Default.Tag,
                    modifier = Modifier.weight(1f)
                )
                LuckyElementBox(
                    label = if (isHindi) "शुभ रंग" else "Lucky Color",
                    value = l.color.split(" ").firstOrNull() ?: l.color,
                    icon = Icons.Default.Palette,
                    modifier = Modifier.weight(1f)
                )
                LuckyElementBox(
                    label = if (isHindi) "शुभ दिन" else "Auspicious Day",
                    value = l.day.split(" ").firstOrNull() ?: l.day,
                    icon = Icons.Default.Today,
                    modifier = Modifier.weight(1f)
                )
            }

            // Disclaimer Card
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = CreamSurfaceVariant),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = if (isHindi) "केवल मार्गदर्शन के लिए। किसी पेशेवर सलाह का विकल्प नहीं है।"
                    else "For guidance only. Not a substitute for professional advice.",
                    fontSize = 12.sp,
                    color = TextMuted,
                    modifier = Modifier.padding(14.dp)
                )
            }
        }
    }
}

@Composable
private fun LuckyElementBox(
    label: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = androidx.compose.foundation.BorderStroke(1.dp, CreamBorder),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(icon, contentDescription = null, tint = SaffronDark, modifier = Modifier.size(20.dp))
            Spacer(Modifier.height(6.dp))
            Text(label, fontSize = 11.sp, color = TextMuted)
            Spacer(Modifier.height(2.dp))
            Text(value, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = DeepIndigo, maxLines = 1)
        }
    }
}
