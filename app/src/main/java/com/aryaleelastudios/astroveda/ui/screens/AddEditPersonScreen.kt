package com.aryaleelastudios.astroveda.ui.screens

import android.app.DatePickerDialog
import android.app.TimePickerDialog
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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aryaleelastudios.astroveda.R
import com.aryaleelastudios.astroveda.data.i18n.findActivity
import com.aryaleelastudios.astroveda.data.local.PersonEntity
import com.aryaleelastudios.astroveda.data.model.Gender
import com.aryaleelastudios.astroveda.ui.components.DEFAULT_PLACE_LAT
import com.aryaleelastudios.astroveda.ui.components.DEFAULT_PLACE_LON
import com.aryaleelastudios.astroveda.ui.components.DEFAULT_PLACE_NAME
import com.aryaleelastudios.astroveda.ui.components.DEFAULT_PLACE_TZ
import com.aryaleelastudios.astroveda.ui.components.PlacePickerDialog
import com.aryaleelastudios.astroveda.ui.components.clearFocusOnTap
import com.aryaleelastudios.astroveda.ui.theme.CreamBackground
import com.aryaleelastudios.astroveda.ui.theme.CreamBorder
import com.aryaleelastudios.astroveda.ui.theme.DeepIndigo
import com.aryaleelastudios.astroveda.ui.theme.SaffronAccent
import com.aryaleelastudios.astroveda.ui.theme.SaffronDark
import com.aryaleelastudios.astroveda.ui.theme.TextMuted
import com.aryaleelastudios.astroveda.ui.theme.astroFilterChipBorder
import com.aryaleelastudios.astroveda.ui.theme.astroFilterChipColors
import com.aryaleelastudios.astroveda.ui.theme.astroPrimaryButtonColors
import com.aryaleelastudios.astroveda.ui.theme.astroSecondaryButtonBorder
import com.aryaleelastudios.astroveda.ui.theme.astroSecondaryButtonColors
import java.util.Locale

private val MARITAL_OPTIONS = listOf(
    "Single" to R.string.marital_single,
    "Married" to R.string.marital_married,
    "Divorced" to R.string.marital_divorced,
    "Widowed" to R.string.marital_widowed
)

private val OCCUPATION_OPTIONS = listOf(
    "Service" to R.string.occ_service,
    "Business" to R.string.occ_business,
    "Student" to R.string.occ_student,
    "Freelancer" to R.string.occ_freelancer,
    "Homemaker" to R.string.occ_homemaker,
    "Other" to R.string.occ_other
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditPersonScreen(
    personToEdit: PersonEntity?,
    onSavePerson: (PersonEntity) -> Unit,
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val dialogContext = remember(context) { context.findActivity() ?: context }
    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current
    var currentStep by remember { mutableIntStateOf(1) }

    fun dismissKeyboard() {
        focusManager.clearFocus(force = true)
        keyboardController?.hide()
    }

    // Step 1
    var name by remember { mutableStateOf(personToEdit?.name ?: "") }
    var selectedGenderKey by remember { mutableStateOf(personToEdit?.gender ?: Gender.PREFER_NOT_TO_SAY.storageKey) }
    var nameError by remember { mutableStateOf(false) }

    // Step 2
    var birthDate by remember { mutableStateOf(personToEdit?.birthDate ?: "1995-08-15") }
    var birthTime by remember { mutableStateOf(personToEdit?.birthTime ?: "14:30") }
    var isTimeUnknown by remember { mutableStateOf(personToEdit?.isTimeUnknown ?: false) }

    // Step 3 (defaults to New Delhi, Delhi)
    var birthPlace by remember { mutableStateOf(personToEdit?.birthPlace ?: DEFAULT_PLACE_NAME) }
    var latitude by remember { mutableDoubleStateOf(personToEdit?.latitude ?: DEFAULT_PLACE_LAT) }
    var longitude by remember { mutableDoubleStateOf(personToEdit?.longitude ?: DEFAULT_PLACE_LON) }
    var timezoneId by remember { mutableStateOf(personToEdit?.timezoneId ?: DEFAULT_PLACE_TZ) }
    var showPlacePicker by remember { mutableStateOf(false) }

    // Step 4
    var maritalStatus by remember { mutableStateOf(personToEdit?.maritalStatus ?: "Single") }
    var occupation by remember { mutableStateOf(personToEdit?.occupation ?: "Service") }

    val dateParts = birthDate.split("-").mapNotNull { it.toIntOrNull() }
    val initialYear = if (dateParts.size == 3) dateParts[0] else 1995
    val initialMonth = if (dateParts.size == 3) dateParts[1] - 1 else 7
    val initialDay = if (dateParts.size == 3) dateParts[2] else 15

    val datePickerDialog = remember(birthDate, dialogContext) {
        DatePickerDialog(
            dialogContext,
            { _, year, month, dayOfMonth ->
                birthDate = String.format(Locale.US, "%04d-%02d-%02d", year, month + 1, dayOfMonth)
            },
            initialYear,
            initialMonth,
            initialDay
        )
    }

    val timeParts = birthTime.split(":").mapNotNull { it.toIntOrNull() }
    val initialHour = if (timeParts.size == 2) timeParts[0] else 12
    val initialMinute = if (timeParts.size == 2) timeParts[1] else 0

    val timePickerDialog = remember(birthTime, dialogContext) {
        TimePickerDialog(
            dialogContext,
            { _, hourOfDay, minute ->
                birthTime = String.format(Locale.US, "%02d:%02d", hourOfDay, minute)
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
                        text = if (personToEdit == null) stringResource(R.string.title_add_person) else stringResource(R.string.title_edit_person),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = DeepIndigo
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack, modifier = Modifier.testTag("add_person_back_btn")) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.cd_back),
                            tint = DeepIndigo
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = CreamBackground)
            )
        },
        containerColor = CreamBackground,
        modifier = Modifier.testTag("add_edit_person_screen")
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clearFocusOnTap()
                .padding(padding),
            contentAlignment = Alignment.TopCenter
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 600.dp)
                    .imePadding()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp, vertical = 8.dp)
            ) {
                // Step Indicator (1..5)
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
                                color = when {
                                    isActive -> Color.White
                                    isPast -> DeepIndigo
                                    else -> TextMuted
                                },
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
                        Text(
                            text = stringResource(R.string.step1_title),
                            style = MaterialTheme.typography.headlineSmall,
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
                            label = { Text(stringResource(R.string.label_full_name)) },
                            isError = nameError,
                            supportingText = {
                                if (nameError) {
                                    Text(stringResource(R.string.error_enter_name), color = MaterialTheme.colorScheme.error)
                                }
                            },
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
                                .testTag("person_name_input")
                        )

                        Spacer(Modifier.height(16.dp))

                        Text(
                            text = stringResource(R.string.label_gender_optional),
                            style = MaterialTheme.typography.labelLarge,
                            color = DeepIndigo
                        )
                        Spacer(Modifier.height(8.dp))

                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Gender.entries.forEach { g ->
                                val isSelected = selectedGenderKey == g.storageKey
                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = if (isSelected) SaffronAccent.copy(alpha = 0.2f) else Color.White,
                                    border = BorderStroke(
                                        width = if (isSelected) 1.5.dp else 1.dp,
                                        color = if (isSelected) SaffronDark else CreamBorder
                                    ),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .heightIn(min = 48.dp)
                                        .clickable {
                                            dismissKeyboard()
                                            selectedGenderKey = g.storageKey
                                        }
                                        .testTag("gender_option_${g.name}")
                                ) {
                                    Row(
                                        modifier = Modifier.padding(12.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        RadioButton(
                                            selected = isSelected,
                                            onClick = {
                                                dismissKeyboard()
                                                selectedGenderKey = g.storageKey
                                            },
                                            colors = RadioButtonDefaults.colors(selectedColor = DeepIndigo)
                                        )
                                        Spacer(Modifier.width(8.dp))
                                        Text(
                                            text = stringResource(g.labelResId),
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                            color = DeepIndigo
                                        )
                                    }
                                }
                            }
                        }
                    }

                    2 -> {
                        Text(
                            text = stringResource(R.string.step2_title),
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = DeepIndigo
                        )
                        Spacer(Modifier.height(16.dp))

                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = Color.White,
                            border = BorderStroke(1.dp, CreamBorder),
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(min = 56.dp)
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
                                    Text(stringResource(R.string.label_birth_date), fontSize = 12.sp, color = TextMuted)
                                    Text(birthDate, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = DeepIndigo)
                                }
                            }
                        }

                        Spacer(Modifier.height(14.dp))

                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = if (isTimeUnknown) Color(0xFFF0F0F0) else Color.White,
                            border = BorderStroke(1.dp, CreamBorder),
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(min = 56.dp)
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
                                    Text(stringResource(R.string.label_birth_time), fontSize = 12.sp, color = TextMuted)
                                    Text(
                                        text = if (isTimeUnknown) stringResource(R.string.time_unknown_display) else birthTime,
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isTimeUnknown) TextMuted else DeepIndigo
                                    )
                                }
                            }
                        }

                        Spacer(Modifier.height(14.dp))

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
                                    text = stringResource(R.string.label_time_unknown),
                                    fontWeight = FontWeight.SemiBold,
                                    color = DeepIndigo,
                                    fontSize = 14.sp
                                )
                                Text(
                                    text = stringResource(R.string.desc_time_unknown),
                                    fontSize = 11.sp,
                                    color = TextMuted
                                )
                            }
                            Switch(
                                checked = isTimeUnknown,
                                onCheckedChange = {
                                    dismissKeyboard()
                                    isTimeUnknown = it
                                },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = SaffronDark,
                                    checkedTrackColor = SaffronAccent.copy(alpha = 0.5f)
                                ),
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
                                        text = stringResource(R.string.warning_time_unknown),
                                        fontSize = 12.sp,
                                        color = DeepIndigo
                                    )
                                }
                            }
                        }
                    }

                    3 -> {
                        Text(
                            text = stringResource(R.string.step3_title),
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = DeepIndigo
                        )
                        Spacer(Modifier.height(16.dp))

                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = Color.White,
                            border = BorderStroke(1.5.dp, DeepIndigo),
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(min = 64.dp)
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
                                    Text(stringResource(R.string.label_birth_place), fontSize = 12.sp, color = TextMuted)
                                    Text(birthPlace, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = DeepIndigo)
                                    Spacer(Modifier.height(4.dp))
                                    Text(
                                        text = stringResource(R.string.place_coords_summary, latitude, longitude, timezoneId),
                                        fontSize = 11.sp,
                                        color = TextMuted
                                    )
                                }
                                Icon(Icons.Default.Edit, contentDescription = stringResource(R.string.cd_edit_place), tint = DeepIndigo)
                            }
                        }

                        Spacer(Modifier.height(16.dp))

                        Text(
                            text = stringResource(R.string.place_step_hint),
                            fontSize = 13.sp,
                            color = TextMuted
                        )
                    }

                    4 -> {
                        Text(
                            text = stringResource(R.string.step4_title),
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = DeepIndigo
                        )
                        Spacer(Modifier.height(16.dp))

                        Text(
                            text = stringResource(R.string.label_marital_status),
                            style = MaterialTheme.typography.titleSmall,
                            color = DeepIndigo
                        )
                        Spacer(Modifier.height(6.dp))

                        MARITAL_OPTIONS.chunked(2).forEach { pairList ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                pairList.forEach { (key, labelRes) ->
                                    val isSelected = maritalStatus == key
                                    FilterChip(
                                        selected = isSelected,
                                        onClick = {
                                            dismissKeyboard()
                                            maritalStatus = key
                                        },
                                        label = {
                                            Text(
                                                text = stringResource(labelRes),
                                                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Medium
                                            )
                                        },
                                        colors = astroFilterChipColors(),
                                        border = astroFilterChipBorder(selected = isSelected),
                                        modifier = Modifier
                                            .weight(1f)
                                            .heightIn(min = 48.dp)
                                    )
                                }
                            }
                        }

                        Spacer(Modifier.height(20.dp))

                        Text(
                            text = stringResource(R.string.label_occupation),
                            style = MaterialTheme.typography.titleSmall,
                            color = DeepIndigo
                        )
                        Spacer(Modifier.height(6.dp))
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            OCCUPATION_OPTIONS.chunked(2).forEach { rowList ->
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    rowList.forEach { (key, labelRes) ->
                                        val isSelected = occupation == key
                                        FilterChip(
                                            selected = isSelected,
                                            onClick = {
                                                dismissKeyboard()
                                                occupation = key
                                            },
                                            label = {
                                                Text(
                                                    text = stringResource(labelRes),
                                                    fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Medium
                                                )
                                            },
                                            colors = astroFilterChipColors(),
                                            border = astroFilterChipBorder(selected = isSelected),
                                            modifier = Modifier
                                                .weight(1f)
                                                .heightIn(min = 48.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }

                    5 -> {
                        val genderLabel = stringResource(Gender.fromStorageKey(selectedGenderKey).labelResId)
                        val maritalLabelRes = MARITAL_OPTIONS.find { it.first == maritalStatus }?.second ?: R.string.marital_single
                        val occLabelRes = OCCUPATION_OPTIONS.find { it.first == occupation }?.second ?: R.string.occ_other

                        Text(
                            text = stringResource(R.string.step5_title),
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = DeepIndigo
                        )
                        Spacer(Modifier.height(16.dp))

                        Card(
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            shape = RoundedCornerShape(16.dp),
                            border = BorderStroke(1.dp, CreamBorder),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                ReviewRow(stringResource(R.string.review_name), name)
                                ReviewRow(stringResource(R.string.review_gender), genderLabel)
                                ReviewRow(stringResource(R.string.review_date), birthDate)
                                ReviewRow(
                                    stringResource(R.string.review_time),
                                    if (isTimeUnknown) stringResource(R.string.time_unknown_display) else birthTime
                                )
                                ReviewRow(stringResource(R.string.review_place), birthPlace)
                                ReviewRow(
                                    stringResource(R.string.review_coords),
                                    String.format(Locale.US, "%.2f°, %.2f° (%s)", latitude, longitude, timezoneId)
                                )
                                ReviewRow(stringResource(R.string.review_marital), stringResource(maritalLabelRes))
                                ReviewRow(stringResource(R.string.review_occupation), stringResource(occLabelRes))
                            }
                        }
                    }
                }

                Spacer(Modifier.height(28.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    if (currentStep > 1) {
                        OutlinedButton(
                            onClick = {
                                dismissKeyboard()
                                currentStep -= 1
                            },
                            colors = astroSecondaryButtonColors(),
                            border = astroSecondaryButtonBorder(),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .weight(1f)
                                .height(50.dp)
                                .testTag("add_person_prev_btn")
                        ) {
                            Text(
                                text = stringResource(R.string.btn_previous),
                                color = DeepIndigo,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }

                    Button(
                        onClick = {
                            dismissKeyboard()
                            if (currentStep == 1 && name.trim().isBlank()) {
                                nameError = true
                                return@Button
                            }
                            if (currentStep < 5) {
                                currentStep += 1
                            } else {
                                val person = PersonEntity(
                                    id = personToEdit?.id ?: 0,
                                    name = name.trim(),
                                    gender = selectedGenderKey,
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
                        colors = astroPrimaryButtonColors(),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(50.dp)
                            .testTag(if (currentStep == 5) "add_person_save_btn" else "add_person_next_btn")
                    ) {
                        Text(
                            text = if (currentStep == 5) stringResource(R.string.btn_save_horoscope) else stringResource(R.string.btn_next),
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )
                    }
                }

                Spacer(Modifier.height(20.dp))
            }
        }
    }

    if (showPlacePicker) {
        PlacePickerDialog(
            currentPlace = birthPlace,
            currentLat = latitude,
            currentLon = longitude,
            currentTz = timezoneId,
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
