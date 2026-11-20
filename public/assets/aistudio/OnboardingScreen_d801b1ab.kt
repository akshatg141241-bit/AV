package com.aryaleelastudios.kundaliguru.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Translate
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aryaleelastudios.kundaliguru.ui.theme.CreamBackground
import com.aryaleelastudios.kundaliguru.ui.theme.CreamBorder
import com.aryaleelastudios.kundaliguru.ui.theme.DeepIndigo
import com.aryaleelastudios.kundaliguru.ui.theme.SaffronAccent
import com.aryaleelastudios.kundaliguru.ui.theme.SaffronDark
import com.aryaleelastudios.kundaliguru.ui.theme.TextMuted

@Composable
fun OnboardingScreen(
    currentLanguage: String,
    onLanguageSelected: (String) -> Unit,
    onSaveNameAndFinish: (name: String) -> Unit
) {
    var step by remember { mutableIntStateOf(1) }
    var enteredName by remember { mutableStateOf("") }
    val isHindi = currentLanguage == "hi"

    Box(
        modifier = Modifier
            .fillMaxSize()
            .testTag("onboarding_screen_container")
            .background(CreamBackground)
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(24.dp)
    ) {
        if (step == 1) {
            // STEP 1 — Language Selection
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .background(SaffronAccent.copy(alpha = 0.2f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Translate,
                        contentDescription = null,
                        tint = DeepIndigo,
                        modifier = Modifier.size(32.dp)
                    )
                }

                Spacer(Modifier.height(24.dp))

                Text(
                    text = "Choose your language",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = DeepIndigo
                )
                Text(
                    text = "अपनी भाषा चुनें",
                    style = MaterialTheme.typography.titleMedium,
                    color = SaffronDark,
                    fontWeight = FontWeight.SemiBold
                )

                Spacer(Modifier.height(36.dp))

                // Two large cards: हिंदी | English
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Hindi Card
                    Card(
                        modifier = Modifier
                            .weight(1f)
                            .height(130.dp)
                            .clickable {
                                onLanguageSelected("hi")
                            }
                            .border(
                                width = if (currentLanguage == "hi") 2.5.dp else 1.dp,
                                color = if (currentLanguage == "hi") SaffronDark else CreamBorder,
                                shape = RoundedCornerShape(16.dp)
                            )
                            .testTag("language_card_hi"),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (currentLanguage == "hi") SaffronAccent.copy(alpha = 0.2f) else Color.White
                        )
                    ) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = "हिंदी",
                                    fontSize = 24.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = DeepIndigo
                                )
                                Spacer(Modifier.height(4.dp))
                                Text(
                                    text = "Devanagari",
                                    fontSize = 12.sp,
                                    color = TextMuted
                                )
                            }
                            if (currentLanguage == "hi") {
                                Icon(
                                    Icons.Default.Check,
                                    contentDescription = "Selected",
                                    tint = SaffronDark,
                                    modifier = Modifier
                                        .align(Alignment.TopEnd)
                                        .padding(10.dp)
                                )
                            }
                        }
                    }

                    // English Card
                    Card(
                        modifier = Modifier
                            .weight(1f)
                            .height(130.dp)
                            .clickable {
                                onLanguageSelected("en")
                            }
                            .border(
                                width = if (currentLanguage == "en") 2.5.dp else 1.dp,
                                color = if (currentLanguage == "en") SaffronDark else CreamBorder,
                                shape = RoundedCornerShape(16.dp)
                            )
                            .testTag("language_card_en"),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (currentLanguage == "en") SaffronAccent.copy(alpha = 0.2f) else Color.White
                        )
                    ) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = "English",
                                    fontSize = 22.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = DeepIndigo
                                )
                                Spacer(Modifier.height(4.dp))
                                Text(
                                    text = "Latin Script",
                                    fontSize = 12.sp,
                                    color = TextMuted
                                )
                            }
                            if (currentLanguage == "en") {
                                Icon(
                                    Icons.Default.Check,
                                    contentDescription = "Selected",
                                    tint = SaffronDark,
                                    modifier = Modifier
                                        .align(Alignment.TopEnd)
                                        .padding(10.dp)
                                )
                            }
                        }
                    }
                }

                Spacer(Modifier.height(48.dp))

                Button(
                    onClick = { step = 2 },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = DeepIndigo,
                        contentColor = SaffronAccent
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .testTag("onboarding_step1_next_btn")
                ) {
                    Text(
                        text = if (isHindi) "आगे बढ़ें (Continue)" else "Continue",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        } else {
            // STEP 2 — Name Input
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .background(SaffronAccent.copy(alpha = 0.2f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = null,
                        tint = DeepIndigo,
                        modifier = Modifier.size(32.dp)
                    )
                }

                Spacer(Modifier.height(24.dp))

                Text(
                    text = if (isHindi) "हम आपको क्या कहें?" else "What should we call you?",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = DeepIndigo
                )
                Spacer(Modifier.height(6.dp))
                Text(
                    text = if (isHindi) "आपका नाम केवल आपके डिवाइस पर सुरक्षित रहेगा" else "Your name stays completely private on this device",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextMuted
                )

                Spacer(Modifier.height(32.dp))

                OutlinedTextField(
                    value = enteredName,
                    onValueChange = { enteredName = it },
                    placeholder = { Text(if (isHindi) "अपना शुभ नाम लिखें" else "Enter your preferred name") },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = DeepIndigo,
                        unfocusedBorderColor = CreamBorder,
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("onboarding_user_name_input")
                )

                Spacer(Modifier.height(36.dp))

                Button(
                    onClick = {
                        val name = enteredName.trim().ifEmpty { if (isHindi) "मित्र" else "Seeker" }
                        onSaveNameAndFinish(name)
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = DeepIndigo,
                        contentColor = SaffronAccent
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .testTag("onboarding_finish_btn")
                ) {
                    Text(
                        text = if (isHindi) "शुरू करें (Get Started)" else "Get Started",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}
