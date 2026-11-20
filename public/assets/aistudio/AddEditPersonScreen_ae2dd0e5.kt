package com.aryaleelastudios.kundaliguru.ui.screens

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
import com.aryaleelastudios.kundaliguru.data.local.PersonEntity
import com.aryaleelastudios.kundaliguru.data.model.Gender
import com.aryaleelastudios.kundaliguru.ui.components.PlacePickerDialog
import com.aryaleelastudios.kundaliguru.ui.theme.CreamBackground
import com.aryaleelastudios.kundaliguru.ui.theme.CreamBorder
import com.aryaleelastudios.kundaliguru.ui.theme.DeepIndigo
import com.aryaleelastudios.kundaliguru.ui.theme.SaffronAccent
import com.aryaleelastudios.kundaliguru.ui.theme.SaffronDark
import com.aryaleelastudios.kundaliguru.ui.theme.TextMuted
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditPersonScreen(
    personToEdit: PersonEntity?,
    isHindi: Boolean,
    onSavePerson: (PersonEntity) -> Unit,
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    var currentStep by remember { mutableIntStateOf(1) }

    // Step 1: Name & Gender
    var name by remember { mutableStateOf(personToEdit?.name ?: "") }
    var selectedGender by remember { mutableStateOf(personToEdit?.gender ?: Gender.MALE.displayNameEn) }
    var nameError by remember { mutableStateOf(false) }

    // Step 2: Date & Time
    var birthDate by remember { mutableStateOf(personToEdit?.birthDate ?: "1995-08-15") }
    var birthTime by remember { mutableStateOf(personToEdit?.birthTime ?: "14:30") }
    var isTimeUnknown by remember { mutableStateOf(personToEdit?.isTimeUnknown ?: false) }

    // Step 3: Place
    var birthPlace by remember { mutableStateOf(personToEdit?.birthPlace ?: "Varanasi, Uttar Pradesh") }
    var latitude by remember { mutableDoubleStateOf(personToEdit?.latitude ?: 25.3176) }
    var longitude by remember { mutableDoubleStateOf(personToEdit?.longitude ?: 82.9739) }
    var timezoneId by remember { mutableStateOf(personToEdit?.timezoneId ?: "Asia/Kolkata") }
    var showPlacePicker by remember { mutableStateOf(false) }

    // Step 4: About You (Optional)
    var maritalStatus by remember { mutableStateOf(personToEdit?.maritalStatus ?: "Single") }
    var occupation by remember { mutableStateOf(personToEdit?.occupation ?: "Service") }

    val maritalOptions = listOf(
        Pair("Single", "अविवाहित"),
        Pair("Married", "विवाहित"),
        Pair("Divorced", "तलाकशुदा"),
        Pair("Widowed", "विधवा/विधुर")
    )

    val occupationOptions = listOf(
        Pair("Service", "नौकरी / सेवा"),
        Pair("Business", "व्यापार"),
        Pair("Student", "विद्यार्थी"),
        Pair("Freelancer", "स्वतंत्र कार्य"),
        Pair("Homemaker", "गृहणी"),
        Pair("Other", "अन्य")
    )

    // Date Picker launcher
    val dateParts = birthDate.split("-").mapNotNull { it.toIntOrNull() }
    val initialYear = if (dateParts.size == 3) dateParts[0] else 1995
    val initialMonth = if (dateParts.size == 3) dateParts[1] - 1 else 7
    val initialDay = if (dateParts.size == 3) dateParts[2] else 15

    val datePickerDialog = remember {
        DatePickerDialog(
            context,
            { _, year, month, dayOfMonth ->
                birthDate = "%04d-%02d-%02d".format(year, month + 1, dayOfMonth)
            },
            initialYear,
            initialMonth,
            initialDay
        )
    }

    // Time Picker launcher
    val timeParts = birthTime.split(":").mapNotNull { it.toIntOrNull() }
    val initialHour = if (timeParts.size == 2) timeParts[0] else 12
    val initialMinute = if (timeParts.size == 2) timeParts[1] else 0

    val timePickerDialog = remember {
        TimePickerDialog(
            context,
            { _, hourOfDay, minute ->
                birthTime = "%02d:%02d".format(hourOfDay, minute)
            },
            initialHour,
            initialMinute,
            false
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = if (personToEdit == null) {
                            if (isHindi) "नई कुंडली जोड़ें" else "Add New Person"
                        } else {
                            if (isHindi) "कुंडली संपादित करें" else "Edit Person"
                        },
                        fontWeight = FontWeight.Bold,
                        color = DeepIndigo
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack, modifier = Modifier.testTag("add_person_back_btn")) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = DeepIndigo)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = CreamBackground)
            )
        },
        containerColor = CreamBackground,
        modifier = Modifier.testTag("add_edit_person_screen")
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 8.dp)
        ) {
            // Step Progress Indicator (1 to 5)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                (1..5).forEach { stepIdx ->
                    val isActive = stepIdx == currentStep
                    val isPast = stepIdx < currentStep
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .background(
                                color = when {
                                    isActive -> DeepIndigo
                                    isPast -> SaffronAccent
                                    else -> CreamBorder
                                },
                                shape = RoundedCornerShape(10.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (isPast) "✓" else "$stepIdx",
                            color = if (isActive || isPast) Color.White else TextMuted,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                    }
                    if (stepIdx < 5) {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(3.dp)
                                .background(if (stepIdx < currentStep) SaffronAccent else CreamBorder)
                        )
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            when (currentStep) {
                1 -> {
                    // STEP 1: Name and Gender
                    Text(
                        text = if (isHindi) "चरण १: नाम एवं लिंग" else "Step 1: Name & Gender",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = DeepIndigo
                    )
                    Spacer(Modifier.height(14.dp))

                    OutlinedTextField(
                        value = name,
                        onValueChange = {
                            name = it
                            if (it.isNotBlank()) nameError = false
                        },
                        label = { Text(if (isHindi) "पूरा नाम (अनिवार्य)" else "Full Name (Required)") },
                        isError = nameError,
                        supportingText = {
                            if (nameError) Text(if (isHindi) "कृपया नाम दर्ज करें" else "Please enter a name", color = MaterialTheme.colorScheme.error)
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = DeepIndigo,
                            unfocusedBorderColor = CreamBorder
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("person_name_input")
                    )

                    Spacer(Modifier.height(16.dp))

                    Text(
                        text = if (isHindi) "लिंग (वैकल्पिक)" else "Gender (Optional)",
                        style = MaterialTheme.typography.labelLarge,
                        color = DeepIndigo
                    )
                    Spacer(Modifier.height(8.dp))

                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Gender.values().forEach { g ->
                            val isSelected = selectedGender == g.displayNameEn
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = if (isSelected) SaffronAccent.copy(alpha = 0.2f) else Color.White,
                                border = androidx.compose.foundation.BorderStroke(
                                    width = if (isSelected) 1.5.dp else 1.dp,
                                    color = if (isSelected) SaffronDark else CreamBorder
                                ),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { selectedGender = g.displayNameEn }
                                    .testTag("gender_option_${g.name}")
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    RadioButton(
                                        selected = isSelected,
                                        onClick = { selectedGender = g.displayNameEn },
                                        colors = RadioButtonDefaults.colors(selectedColor = DeepIndigo)
                                    )
                                    Spacer(Modifier.width(8.dp))
                                    Text(
                                        text = if (isHindi) g.displayNameHi else g.displayNameEn,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                        color = DeepIndigo
                                    )
                                }
                            }
                        }
                    }
                }

                2 -> {
                    // STEP 2: Birth Date & Time
                    Text(
                        text = if (isHindi) "चरण २: जन्म दिनांक एवं समय" else "Step 2: Birth Date & Time",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = DeepIndigo
                    )
                    Spacer(Modifier.height(16.dp))

                    // Date selector card
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = Color.White,
                        border = androidx.compose.foundation.BorderStroke(1.dp, CreamBorder),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { datePickerDialog.show() }
                            .testTag("birth_date_selector")
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.CalendarToday, contentDescription = null, tint = DeepIndigo)
                            Spacer(Modifier.width(14.dp))
                            Column {
                                Text(if (isHindi) "जन्म दिनांक" else "Birth Date", fontSize = 12.sp, color = TextMuted)
                                Text(birthDate, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = DeepIndigo)
                            }
                        }
                    }

                    Spacer(Modifier.height(14.dp))

                    // Time selector card
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = if (isTimeUnknown) Color(0xFFF0F0F0) else Color.White,
                        border = androidx.compose.foundation.BorderStroke(1.dp, CreamBorder),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable(enabled = !isTimeUnknown) { timePickerDialog.show() }
                            .testTag("birth_time_selector")
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.AccessTime, contentDescription = null, tint = if (isTimeUnknown) TextMuted else DeepIndigo)
                            Spacer(Modifier.width(14.dp))
                            Column {
                                Text(if (isHindi) "जन्म समय" else "Birth Time", fontSize = 12.sp, color = TextMuted)
                                Text(
                                    if (isTimeUnknown) (if (isHindi) "12:00 दोपहर (अज्ञात)" else "12:00 PM (Unknown)") else birthTime,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isTimeUnknown) TextMuted else DeepIndigo
                                )
                            }
                        }
                    }

                    Spacer(Modifier.height(14.dp))

                    // Time Unknown Switch
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color.White, RoundedCornerShape(12.dp))
                            .border(1.dp, CreamBorder, RoundedCornerShape(12.dp))
                            .padding(horizontal = 14.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = if (isHindi) "जन्म समय ज्ञात नहीं है" else "Time unknown",
                                fontWeight = FontWeight.SemiBold,
                                color = DeepIndigo,
                                fontSize = 14.sp
                            )
                            Text(
                                text = if (isHindi) "दोपहर 12:00 बजे का मानक समय माना जाएगा" else "Default 12:00 PM will be used",
                                fontSize = 11.sp,
                                color = TextMuted
                            )
                        }
                        Switch(
                            checked = isTimeUnknown,
                            onCheckedChange = { isTimeUnknown = it },
                            colors = SwitchDefaults.colors(checkedThumbColor = SaffronDark, checkedTrackColor = SaffronAccent.copy(alpha = 0.5f)),
                            modifier = Modifier.testTag("time_unknown_switch")
                        )
                    }

                    if (isTimeUnknown) {
                        Spacer(Modifier.height(10.dp))
                        Card(
                            colors = CardDefaults.cardColors(containerColor = SaffronAccent.copy(alpha = 0.15f)),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Warning, contentDescription = null, tint = SaffronDark, modifier = Modifier.size(20.dp))
                                Spacer(Modifier.width(10.dp))
                                Text(
                                    text = if (isHindi) "चेतावनी: समय अज्ञात होने पर लग्न और भाव कस्प अनुमानित हो सकते हैं। चंद्र राशि एवं नक्षत्र गणना सटीक रहेगी।"
                                    else "Notice: Lagna and house cusps may be approximate without exact time. Moon sign and Nakshatra remain reliable.",
                                    fontSize = 12.sp,
                                    color = DeepIndigo
                                )
                            }
                        }
                    }
                }

                3 -> {
                    // STEP 3: Birth Place
                    Text(
                        text = if (isHindi) "चरण ३: जन्म स्थान (स्थान चयन)" else "Step 3: Birth Place & Coordinates",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = DeepIndigo
                    )
                    Spacer(Modifier.height(16.dp))

                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = Color.White,
                        border = androidx.compose.foundation.BorderStroke(1.5.dp, DeepIndigo),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { showPlacePicker = true }
                            .testTag("open_place_picker_btn")
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.LocationOn, contentDescription = null, tint = SaffronDark, modifier = Modifier.size(28.dp))
                            Spacer(Modifier.width(14.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(if (isHindi) "जन्म स्थान" else "Birth Place", fontSize = 12.sp, color = TextMuted)
                                Text(birthPlace, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = DeepIndigo)
                                Spacer(Modifier.height(4.dp))
                                Text(
                                    "Lat: ${"%.4f".format(latitude)}, Lon: ${"%.4f".format(longitude)} | TZ: $timezoneId",
                                    fontSize = 11.sp,
                                    color = TextMuted
                                )
                            }
                            Icon(Icons.Default.Edit, contentDescription = "Edit Place", tint = DeepIndigo)
                        }
                    }

                    Spacer(Modifier.height(16.dp))

                    Text(
                        text = if (isHindi) "• 50+ प्रमुख शहरों से चयन करें अथवा सीधे मानचित्र पर पिन करें।"
                        else "• Pick from 50+ major cities or tap anywhere on the map to pin exact coordinates.",
                        fontSize = 13.sp,
                        color = TextMuted
                    )
                }

                4 -> {
                    // STEP 4: About You (Optional)
                    Text(
                        text = if (isHindi) "चरण ४: आपके बारे में (वैकल्पिक)" else "Step 4: About You (Optional)",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = DeepIndigo
                    )
                    Spacer(Modifier.height(16.dp))

                    Text(
                        text = if (isHindi) "वैवाहिक स्थिति" else "Marital Status",
                        style = MaterialTheme.typography.labelLarge,
                        color = DeepIndigo
                    )
                    Spacer(Modifier.height(6.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        maritalOptions.take(2).forEach { (en, hi) ->
                            FilterChip(
                                selected = maritalStatus == en,
                                onClick = { maritalStatus = en },
                                label = { Text(if (isHindi) hi else en) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = DeepIndigo,
                                    selectedLabelColor = SaffronAccent
                                ),
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        maritalOptions.drop(2).forEach { (en, hi) ->
                            FilterChip(
                                selected = maritalStatus == en,
                                onClick = { maritalStatus = en },
                                label = { Text(if (isHindi) hi else en) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = DeepIndigo,
                                    selectedLabelColor = SaffronAccent
                                ),
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }

                    Spacer(Modifier.height(20.dp))

                    Text(
                        text = if (isHindi) "व्यवसाय / कार्यक्षेत्र" else "Occupation / Domain",
                        style = MaterialTheme.typography.labelLarge,
                        color = DeepIndigo
                    )
                    Spacer(Modifier.height(6.dp))
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        occupationOptions.chunked(2).forEach { rowList ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                rowList.forEach { (en, hi) ->
                                    FilterChip(
                                        selected = occupation == en,
                                        onClick = { occupation = en },
                                        label = { Text(if (isHindi) hi else en) },
                                        colors = FilterChipDefaults.filterChipColors(
                                            selectedContainerColor = DeepIndigo,
                                            selectedLabelColor = SaffronAccent
                                        ),
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                            }
                        }
                    }
                }

                5 -> {
                    // STEP 5: Review & Save
                    Text(
                        text = if (isHindi) "चरण ५: विवरण समीक्षा एवं सहेजें" else "Step 5: Review & Save",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = DeepIndigo
                    )
                    Spacer(Modifier.height(16.dp))

                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        shape = RoundedCornerShape(16.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, CreamBorder),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            ReviewRow(label = if (isHindi) "नाम" else "Name", value = name)
                            ReviewRow(label = if (isHindi) "लिंग" else "Gender", value = selectedGender)
                            ReviewRow(label = if (isHindi) "जन्म दिनांक" else "Birth Date", value = birthDate)
                            ReviewRow(
                                label = if (isHindi) "जन्म समय" else "Birth Time",
                                value = if (isTimeUnknown) (if (isHindi) "12:00 दोपहर (अज्ञात)" else "12:00 PM (Unknown)") else birthTime
                            )
                            ReviewRow(label = if (isHindi) "जन्म स्थान" else "Place", value = birthPlace)
                            ReviewRow(label = if (isHindi) "अक्षांश / देशांतर" else "Coordinates", value = "${"%.2f".format(latitude)}°N, ${"%.2f".format(longitude)}°E")
                            ReviewRow(label = if (isHindi) "वैवाहिक स्थिति" else "Marital Status", value = maritalStatus)
                            ReviewRow(label = if (isHindi) "कार्यक्षेत्र" else "Occupation", value = occupation)
                        }
                    }
                }
            }

            Spacer(Modifier.height(28.dp))

            // Navigation Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (currentStep > 1) {
                    OutlinedButton(
                        onClick = { currentStep -= 1 },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(50.dp)
                            .testTag("add_person_prev_btn")
                    ) {
                        Text(if (isHindi) "पीछे" else "Previous", color = DeepIndigo)
                    }
                }

                Button(
                    onClick = {
                        if (currentStep == 1) {
                            if (name.trim().isBlank()) {
                                nameError = true
                                return@Button
                            }
                        }

                        if (currentStep < 5) {
                            currentStep += 1
                        } else {
                            // Save
                            val person = PersonEntity(
                                id = personToEdit?.id ?: 0,
                                name = name.trim(),
                                gender = selectedGender,
                                birthDate = birthDate,
                                birthTime = birthTime,
                                isTimeUnknown = isTimeUnknown,
                                birthPlace = birthPlace,
                                latitude = latitude,
                                longitude = longitude,
                                timezoneId = timezoneId,
                                maritalStatus = maritalStatus,
                                occupation = occupation
                            )
                            onSavePerson(person)
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = DeepIndigo,
                        contentColor = SaffronAccent
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .weight(1f)
                        .height(50.dp)
                        .testTag(if (currentStep == 5) "add_person_save_btn" else "add_person_next_btn")
                ) {
                    Text(
                        text = if (currentStep == 5) {
                            if (isHindi) "कुंडली सहेजें (Save)" else "Save Horoscope"
                        } else {
                            if (isHindi) "आगे बढ़ें (Next)" else "Next"
                        },
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                }
            }
        }
    }

    if (showPlacePicker) {
        PlacePickerDialog(
            currentPlace = birthPlace,
            currentLat = latitude,
            currentLon = longitude,
            currentTz = timezoneId,
            isHindi = isHindi,
            onPlaceSelected = { newName, newLat, newLon, newTz ->
                birthPlace = newName
                latitude = newLat
                longitude = newLon
                timezoneId = newTz
            },
            onDismiss = { showPlacePicker = false }
        )
    }
}

@Composable
private fun ReviewRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, color = TextMuted, fontSize = 13.sp)
        Text(value, fontWeight = FontWeight.SemiBold, color = DeepIndigo, fontSize = 13.sp)
    }
}
