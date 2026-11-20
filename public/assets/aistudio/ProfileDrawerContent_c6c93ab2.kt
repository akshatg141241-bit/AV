package com.aryaleelastudios.kundaliguru.ui.screens

import android.app.TimePickerDialog
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aryaleelastudios.kundaliguru.data.model.AyanamsaType
import com.aryaleelastudios.kundaliguru.data.model.ChartStyle
import com.aryaleelastudios.kundaliguru.ui.theme.*

@Composable
fun ProfileDrawerContent(
    userName: String,
    currentLanguage: String,
    chartStyle: ChartStyle,
    ayanamsa: AyanamsaType,
    notificationsEnabled: Boolean,
    notificationHour: Int,
    notificationMinute: Int,
    onUpdateUserName: (String) -> Unit,
    onUpdateLanguage: (String) -> Unit,
    onUpdateChartStyle: (ChartStyle) -> Unit,
    onUpdateAyanamsa: (AyanamsaType) -> Unit,
    onUpdateNotifications: (Boolean, Int, Int) -> Unit,
    onExportJson: () -> Unit,
    onImportJson: () -> Unit,
    onClearAllData: () -> Unit,
    onCloseDrawer: () -> Unit
) {
    val context = LocalContext.current
    val isHindi = currentLanguage == "hi"

    var isEditingName by remember { mutableStateOf(false) }
    var tempName by remember(userName) { mutableStateOf(userName) }
    var showClearConfirmDialog by remember { mutableStateOf(false) }

    val timePickerDialog = remember(notificationHour, notificationMinute) {
        TimePickerDialog(
            context,
            { _, hourOfDay, minute ->
                onUpdateNotifications(notificationsEnabled, hourOfDay, minute)
            },
            notificationHour,
            notificationMinute,
            false
        )
    }

    ModalDrawerSheet(
        drawerContainerColor = CreamBackground,
        drawerShape = RoundedCornerShape(topStart = 24.dp, bottomStart = 24.dp),
        modifier = Modifier
            .fillMaxHeight()
            .widthIn(max = 330.dp)
            .testTag("profile_drawer_sheet")
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .background(DeepIndigo, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Person, contentDescription = null, tint = SaffronAccent)
                    }
                    Spacer(Modifier.width(12.dp))
                    Text(
                        text = if (isHindi) "प्रोफ़ाइल एवं सेटिंग्स" else "Profile & Settings",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = DeepIndigo
                    )
                }
                IconButton(onClick = onCloseDrawer, modifier = Modifier.testTag("close_drawer_btn")) {
                    Icon(Icons.Default.Close, contentDescription = "Close", tint = DeepIndigo)
                }
            }

            HorizontalDivider(color = CreamBorder)

            // Section 1: User Name (Editable)
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = androidx.compose.foundation.BorderStroke(1.dp, CreamBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (isHindi) "आपका नाम" else "Your Name",
                            fontSize = 12.sp,
                            color = TextMuted
                        )
                        IconButton(
                            onClick = {
                                if (isEditingName) {
                                    onUpdateUserName(tempName.trim().ifEmpty { userName })
                                    isEditingName = false
                                } else {
                                    isEditingName = true
                                }
                            },
                            modifier = Modifier.size(28.dp).testTag("edit_user_name_toggle")
                        ) {
                            Icon(
                                if (isEditingName) Icons.Default.Check else Icons.Default.Edit,
                                contentDescription = "Edit Name",
                                tint = DeepIndigo,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }

                    if (isEditingName) {
                        OutlinedTextField(
                            value = tempName,
                            onValueChange = { tempName = it },
                            singleLine = true,
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth().testTag("drawer_user_name_input")
                        )
                    } else {
                        Text(
                            text = userName.ifBlank { if (isHindi) "अतिथि साधक" else "Guest Seeker" },
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = DeepIndigo
                        )
                    }
                }
            }

            // Section 2: Language Switcher
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = androidx.compose.foundation.BorderStroke(1.dp, CreamBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = if (isHindi) "भाषा (Language)" else "App Language",
                        fontSize = 12.sp,
                        color = TextMuted
                    )
                    Spacer(Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FilterChip(
                            selected = currentLanguage == "hi",
                            onClick = { onUpdateLanguage("hi") },
                            label = { Text("हिंदी") },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = DeepIndigo,
                                selectedLabelColor = SaffronAccent
                            ),
                            modifier = Modifier.weight(1f).testTag("drawer_lang_hi")
                        )
                        FilterChip(
                            selected = currentLanguage == "en",
                            onClick = { onUpdateLanguage("en") },
                            label = { Text("English") },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = DeepIndigo,
                                selectedLabelColor = SaffronAccent
                            ),
                            modifier = Modifier.weight(1f).testTag("drawer_lang_en")
                        )
                    }
                }
            }

            // Section 3: Daily Notifications
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = androidx.compose.foundation.BorderStroke(1.dp, CreamBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = if (isHindi) "दैनिक राशिफल सूचना" else "Daily Horoscope Notification",
                                fontWeight = FontWeight.SemiBold,
                                color = DeepIndigo,
                                fontSize = 14.sp
                            )
                            Text(
                                text = if (isHindi) "प्रातःकाल मार्गदर्शन" else "Morning cosmic guidance",
                                fontSize = 11.sp,
                                color = TextMuted
                            )
                        }
                        Switch(
                            checked = notificationsEnabled,
                            onCheckedChange = { onUpdateNotifications(it, notificationHour, notificationMinute) },
                            colors = SwitchDefaults.colors(checkedThumbColor = SaffronDark),
                            modifier = Modifier.testTag("drawer_notif_switch")
                        )
                    }

                    if (notificationsEnabled) {
                        Spacer(Modifier.height(10.dp))
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(CreamBackground, RoundedCornerShape(8.dp))
                                .clickable { timePickerDialog.show() }
                                .padding(10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(if (isHindi) "सूचना समय" else "Alert Time", fontSize = 13.sp, color = DeepIndigo)
                            Text(
                                text = "%02d:%02d".format(notificationHour, notificationMinute),
                                fontWeight = FontWeight.Bold,
                                color = SaffronDark,
                                fontSize = 14.sp
                            )
                        }
                    }
                }
            }

            // Section 4: Chart Defaults & Ayanamsa
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = androidx.compose.foundation.BorderStroke(1.dp, CreamBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = if (isHindi) "ज्योतिषीय सेटिंग्स" else "Astrology Settings",
                        fontSize = 12.sp,
                        color = TextMuted
                    )

                    // Default Chart Style
                    Column {
                        Text(if (isHindi) "डिफ़ॉल्ट चक्र शैली" else "Default Chart Style", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = DeepIndigo)
                        Spacer(Modifier.height(4.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            ChartStyle.values().forEach { style ->
                                FilterChip(
                                    selected = chartStyle == style,
                                    onClick = { onUpdateChartStyle(style) },
                                    label = { Text(if (isHindi) style.displayNameHi else style.displayNameEn, fontSize = 11.sp) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = DeepIndigo,
                                        selectedLabelColor = SaffronAccent
                                    ),
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    }

                    // Default Ayanamsa
                    Column {
                        Text(if (isHindi) "अयनांश (Ayanamsa)" else "Ayanamsa System", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = DeepIndigo)
                        Spacer(Modifier.height(4.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            AyanamsaType.values().forEach { a ->
                                FilterChip(
                                    selected = ayanamsa == a,
                                    onClick = { onUpdateAyanamsa(a) },
                                    label = { Text(a.name, fontSize = 11.sp) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = DeepIndigo,
                                        selectedLabelColor = SaffronAccent
                                    )
                                )
                            }
                        }
                    }
                }
            }

            // Section 5: Data Management (SAF Export / Import / Clear)
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = androidx.compose.foundation.BorderStroke(1.dp, CreamBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = if (isHindi) "डेटा बैकअप एवं प्रबंधन" else "Data Management",
                        fontSize = 12.sp,
                        color = TextMuted
                    )

                    TextButton(
                        onClick = onExportJson,
                        modifier = Modifier.fillMaxWidth().testTag("drawer_export_json_btn")
                    ) {
                        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Download, contentDescription = null, tint = DeepIndigo)
                            Spacer(Modifier.width(10.dp))
                            Text(if (isHindi) "डेटा एक्सपोर्ट करें (JSON)" else "Export all data (JSON via SAF)", color = DeepIndigo)
                        }
                    }

                    TextButton(
                        onClick = onImportJson,
                        modifier = Modifier.fillMaxWidth().testTag("drawer_import_json_btn")
                    ) {
                        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Upload, contentDescription = null, tint = DeepIndigo)
                            Spacer(Modifier.width(10.dp))
                            Text(if (isHindi) "डेटा इम्पोर्ट करें (JSON)" else "Import data (JSON via SAF)", color = DeepIndigo)
                        }
                    }

                    TextButton(
                        onClick = { showClearConfirmDialog = true },
                        modifier = Modifier.fillMaxWidth().testTag("drawer_clear_all_btn")
                    ) {
                        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.DeleteForever, contentDescription = null, tint = MaterialTheme.colorScheme.error)
                            Spacer(Modifier.width(10.dp))
                            Text(if (isHindi) "सभी डेटा मिटाएं" else "Clear all data", color = MaterialTheme.colorScheme.error)
                        }
                    }
                }
            }

            // Section 6: About & Disclaimer
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = CreamSurfaceVariant),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "Kundali Guru v1.0",
                        fontWeight = FontWeight.Bold,
                        color = DeepIndigo,
                        fontSize = 14.sp
                    )
                    Text(
                        text = "AryaLeela Studios",
                        fontWeight = FontWeight.SemiBold,
                        color = SaffronDark,
                        fontSize = 12.sp
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = if (isHindi) "पूर्णतः ऑफलाइन वैदिक ज्योतिष गणना एवं राशिफल।\n\nअस्वीकरण: केवल मार्गदर्शन के लिए। किसी पेशेवर सलाह का विकल्प नहीं है।"
                        else "Pure offline Vedic astrology calculation & horoscope engine.\n\nDisclaimer: For guidance only. Not a substitute for professional advice.",
                        fontSize = 11.sp,
                        color = TextMuted,
                        lineHeight = 15.sp
                    )
                }
            }
        }
    }

    if (showClearConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showClearConfirmDialog = false },
            title = { Text(if (isHindi) "डेटा मिटाने की पुष्टि" else "Confirm Clear Data", color = DeepIndigo) },
            text = {
                Text(
                    text = if (isHindi) "क्या आप सभी सहेजी गई कुंडलियां और सेटिंग्स हटाना चाहते हैं? यह क्रिया पूर्ववत नहीं की जा सकती।"
                    else "Are you sure you want to clear all saved horoscopes and preferences? This action cannot be undone.",
                    color = TextPrimary
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showClearConfirmDialog = false
                        onClearAllData()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text(if (isHindi) "हां, सब मिटाएं" else "Yes, Clear All")
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearConfirmDialog = false }) {
                    Text(if (isHindi) "रद्द करें" else "Cancel", color = DeepIndigo)
                }
            },
            containerColor = CreamBackground,
            shape = RoundedCornerShape(16.dp)
        )
    }
}
