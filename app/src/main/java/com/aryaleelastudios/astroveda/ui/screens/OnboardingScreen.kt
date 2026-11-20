package com.aryaleelastudios.astroveda.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Translate
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aryaleelastudios.astroveda.R
import com.aryaleelastudios.astroveda.data.i18n.LanguageRegistry
import com.aryaleelastudios.astroveda.ui.components.clearFocusOnTap
import com.aryaleelastudios.astroveda.ui.theme.CreamBackground
import com.aryaleelastudios.astroveda.ui.theme.CreamBorder
import com.aryaleelastudios.astroveda.ui.theme.DeepIndigo
import com.aryaleelastudios.astroveda.ui.theme.SaffronAccent
import com.aryaleelastudios.astroveda.ui.theme.SaffronDark
import com.aryaleelastudios.astroveda.ui.theme.TextMuted
import com.aryaleelastudios.astroveda.ui.theme.astroPrimaryButtonColors
import com.aryaleelastudios.astroveda.ui.theme.astroSecondaryButtonBorder
import com.aryaleelastudios.astroveda.ui.theme.astroSecondaryButtonColors

@Composable
fun OnboardingScreen(
    currentLanguage: String,
    onLanguageSelected: (String) -> Unit,
    onCompleteAndAddPerson: (name: String) -> Unit,
    onCompleteToKundaliTab: (name: String) -> Unit
) {
    var step by remember { mutableIntStateOf(1) }
    var enteredName by remember { mutableStateOf("") }
    val enabledLanguages = remember { LanguageRegistry.enabledLanguages() }
    val guestFallback = stringResource(R.string.onboarding_default_guest)
    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current

    fun dismissKeyboard() {
        focusManager.clearFocus(force = true)
        keyboardController?.hide()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .testTag("onboarding_screen_container")
            .background(CreamBackground)
            .clearFocusOnTap()
            .statusBarsPadding()
            .navigationBarsPadding()
            .imePadding()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = 560.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            when (step) {
                1 -> {
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
                        text = stringResource(R.string.onboarding_choose_language),
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = DeepIndigo,
                        textAlign = TextAlign.Center
                    )
                    Spacer(Modifier.height(6.dp))
                    Text(
                        text = stringResource(R.string.onboarding_choose_language_sub),
                        style = MaterialTheme.typography.bodyMedium,
                        color = SaffronDark,
                        fontWeight = FontWeight.SemiBold,
                        textAlign = TextAlign.Center
                    )

                    Spacer(Modifier.height(32.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        enabledLanguages.forEach { entry ->
                            val isSelected = currentLanguage == entry.code
                            Card(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(132.dp)
                                    .clickable {
                                        dismissKeyboard()
                                        onLanguageSelected(entry.code)
                                    }
                                    .border(
                                        width = if (isSelected) 2.5.dp else 1.dp,
                                        color = if (isSelected) SaffronDark else CreamBorder,
                                        shape = RoundedCornerShape(16.dp)
                                    )
                                    .testTag("language_card_${entry.code}"),
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = if (isSelected) SaffronAccent.copy(alpha = 0.2f) else Color.White
                                )
                            ) {
                                Box(
                                    modifier = Modifier.fillMaxSize(),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text(
                                            text = entry.nativeName,
                                            style = MaterialTheme.typography.titleLarge,
                                            fontSize = 22.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = DeepIndigo
                                        )
                                        Spacer(Modifier.height(4.dp))
                                        Text(
                                            text = entry.script,
                                            fontSize = 12.sp,
                                            color = TextMuted
                                        )
                                    }
                                    if (isSelected) {
                                        Icon(
                                            imageVector = Icons.Default.Check,
                                            contentDescription = null,
                                            tint = SaffronDark,
                                            modifier = Modifier
                                                .align(Alignment.TopEnd)
                                                .padding(10.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }

                    Spacer(Modifier.height(44.dp))

                    Button(
                        onClick = {
                            dismissKeyboard()
                            step = 2
                        },
                        colors = astroPrimaryButtonColors(),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                            .testTag("onboarding_step1_next_btn")
                    ) {
                        Text(
                            text = stringResource(R.string.onboarding_continue),
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                2 -> {
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
                        text = stringResource(R.string.onboarding_name_title),
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = DeepIndigo,
                        textAlign = TextAlign.Center
                    )
                    Spacer(Modifier.height(6.dp))
                    Text(
                        text = stringResource(R.string.onboarding_name_subtitle),
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextMuted,
                        textAlign = TextAlign.Center
                    )

                    Spacer(Modifier.height(32.dp))

                    OutlinedTextField(
                        value = enteredName,
                        onValueChange = { enteredName = it },
                        placeholder = { Text(stringResource(R.string.onboarding_name_placeholder)) },
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
                            dismissKeyboard()
                            step = 3
                        },
                        colors = astroPrimaryButtonColors(),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                            .testTag("onboarding_step2_next_btn")
                    ) {
                        Text(
                            text = stringResource(R.string.onboarding_get_started),
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                else -> {
                    val resolvedName = enteredName.trim().ifEmpty { guestFallback }

                    Box(
                        modifier = Modifier
                            .size(88.dp)
                            .background(SaffronAccent.copy(alpha = 0.2f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = null,
                            tint = DeepIndigo,
                            modifier = Modifier.size(44.dp)
                        )
                    }

                    Spacer(Modifier.height(24.dp))

                    Text(
                        text = stringResource(R.string.onboarding_step3_title, resolvedName),
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = DeepIndigo,
                        textAlign = TextAlign.Center
                    )
                    Spacer(Modifier.height(10.dp))
                    Text(
                        text = stringResource(R.string.onboarding_step3_subtitle),
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextMuted,
                        textAlign = TextAlign.Center,
                        lineHeight = 21.sp
                    )

                    Spacer(Modifier.height(36.dp))

                    Button(
                        onClick = {
                            dismissKeyboard()
                            onCompleteAndAddPerson(resolvedName)
                        },
                        colors = astroPrimaryButtonColors(),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                            .testTag("onboarding_add_first_person_cta")
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(20.dp))
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text = stringResource(R.string.cta_add_first_person),
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(Modifier.height(12.dp))

                    OutlinedButton(
                        onClick = {
                            dismissKeyboard()
                            onCompleteToKundaliTab(resolvedName)
                        },
                        colors = astroSecondaryButtonColors(),
                        border = astroSecondaryButtonBorder(),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .testTag("onboarding_explore_tab_btn")
                    ) {
                        Text(
                            text = stringResource(R.string.onboarding_explore_tab),
                            fontSize = 15.sp,
                            color = DeepIndigo,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }
    }
}
