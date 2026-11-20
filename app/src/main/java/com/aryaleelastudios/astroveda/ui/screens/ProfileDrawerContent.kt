package com.aryaleelastudios.astroveda.ui.screens

import android.app.TimePickerDialog
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
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
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Upload
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
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
import com.aryaleelastudios.astroveda.data.i18n.LanguageRegistry
import com.aryaleelastudios.astroveda.data.i18n.findActivity
import com.aryaleelastudios.astroveda.data.local.PersonEntity
import com.aryaleelastudios.astroveda.data.model.AyanamsaType
import com.aryaleelastudios.astroveda.data.model.ChartStyle
import com.aryaleelastudios.astroveda.ui.components.clearFocusOnTap
import com.aryaleelastudios.astroveda.ui.theme.CreamBackground
import com.aryaleelastudios.astroveda.ui.theme.CreamBorder
import com.aryaleelastudios.astroveda.ui.theme.CreamSurfaceVariant
import com.aryaleelastudios.astroveda.ui.theme.DeepIndigo
import com.aryaleelastudios.astroveda.ui.theme.SaffronAccent
import com.aryaleelastudios.astroveda.ui.theme.SaffronDark
import com.aryaleelastudios.astroveda.ui.theme.TextMuted
import com.aryaleelastudios.astroveda.ui.theme.TextPrimary
import com.aryaleelastudios.astroveda.ui.theme.astroFilterChipBorder
import com.aryaleelastudios.astroveda.ui.theme.astroFilterChipColors
import java.util.Locale

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ProfileDrawerContent(
    userName: String,
    currentLanguage: String,
    chartStyle: ChartStyle,
    ayanamsa: AyanamsaType,
    notificationsEnabled: Boolean,
    notificationHour: Int,
    notificationMinute: Int,
    persons: List<PersonEntity>,
    notificationPersonId: Long?,
    onUpdateUserName: (String) -> Unit,
    onUpdateLanguage: (String) -> Unit,
    onUpdateChartStyle: (ChartStyle) -> Unit,
    onUpdateAyanamsa: (AyanamsaType) -> Unit,
    onUpdateNotifications: (Boolean, Int, Int) -> Unit,
    onUpdateNotificationPerson: (Long) -> Unit,
    onExportJson: () -> Unit,
    onImportJson: () -> Unit,
    onClearAllData: () -> Unit,
    onCloseDrawer: () -> Unit
) {
    val context = LocalContext.current
    val dialogContext = remember(context) { context.findActivity() ?: context }
    val enabledLanguages = remember { LanguageRegistry.enabledLanguages() }
    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current

    fun dismissKeyboard() {
        focusManager.clearFocus(force = true)
        keyboardController?.hide()
    }

    var isEditingName by remember { mutableStateOf(false) }
    var tempName by remember(userName) { mutableStateOf(userName) }
    var showClearConfirmDialog by remember { mutableStateOf(false) }

    val effectiveNotifPersonId = remember(persons, notificationPersonId) {
        persons.find { it.id == notificationPersonId }?.id ?: persons.firstOrNull()?.id
    }

    val timePickerDialog = remember(notificationHour, notificationMinute, notificationsEnabled, dialogContext) {
        TimePickerDialog(
            dialogContext,
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
                .clearFocusOnTap()
                .verticalScroll(rememberScrollState())
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .background(SaffronAccent, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Person, contentDescription = null, tint = DeepIndigo)
                    }
                    Spacer(Modifier.width(12.dp))
                    Text(
                        text = stringResource(R.string.drawer_title),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = DeepIndigo
                    )
                }
                IconButton(
                    onClick = onCloseDrawer,
                    modifier = Modifier.testTag("close_drawer_btn")
                ) {
                    Icon(Icons.Default.Close, contentDescription = stringResource(R.string.cd_close), tint = DeepIndigo)
                }
            }

            HorizontalDivider(color = CreamBorder)

            // User Name
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = BorderStroke(1.dp, CreamBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = stringResource(R.string.drawer_your_name),
                            style = MaterialTheme.typography.titleSmall,
                            color = DeepIndigo
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
                            modifier = Modifier.testTag("edit_user_name_toggle")
                        ) {
                            Icon(
                                imageVector = if (isEditingName) Icons.Default.Check else Icons.Default.Edit,
                                contentDescription = stringResource(
                                    if (isEditingName) R.string.cd_save_user_name else R.string.cd_edit_user_name
                                ),
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
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("drawer_user_name_input")
                        )
                    } else {
                        Text(
                            text = userName.ifBlank { stringResource(R.string.drawer_guest) },
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = DeepIndigo
                        )
                    }
                }
            }

            // Language Switcher (populated dynamically from LanguageRegistry)
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = BorderStroke(1.dp, CreamBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = stringResource(R.string.drawer_language),
                        style = MaterialTheme.typography.titleSmall,
                        color = DeepIndigo
                    )
                    Spacer(Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        enabledLanguages.forEach { entry ->
                            val isSelected = currentLanguage == entry.code
                            FilterChip(
                                selected = isSelected,
                                onClick = {
                                    dismissKeyboard()
                                    onUpdateLanguage(entry.code)
                                },
                                label = {
                                    Text(
                                        text = entry.nativeName,
                                        fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Medium
                                    )
                                },
                                colors = astroFilterChipColors(),
                                border = astroFilterChipBorder(selected = isSelected),
                                modifier = Modifier
                                    .weight(1f)
                                    .heightIn(min = 48.dp)
                                    .testTag("drawer_lang_${entry.code}")
                            )
                        }
                    }
                }
            }

            // Notifications Toggle + Time Picker + Notification Person Selector (if >1 person)
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = BorderStroke(1.dp, CreamBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = stringResource(R.string.drawer_notif_title),
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.SemiBold,
                                color = DeepIndigo
                            )
                            Text(
                                text = stringResource(R.string.drawer_notif_subtitle),
                                fontSize = 11.sp,
                                color = TextMuted
                            )
                        }
                        Switch(
                            checked = notificationsEnabled,
                            onCheckedChange = {
                                dismissKeyboard()
                                onUpdateNotifications(it, notificationHour, notificationMinute)
                            },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = SaffronDark,
                                checkedTrackColor = SaffronAccent.copy(alpha = 0.45f)
                            ),
                            modifier = Modifier.testTag("drawer_notif_switch")
                        )
                    }

                    if (notificationsEnabled) {
                        Spacer(Modifier.height(10.dp))
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(min = 48.dp)
                                .background(CreamBackground, RoundedCornerShape(8.dp))
                                .clickable { timePickerDialog.show() }
                                .padding(horizontal = 12.dp, vertical = 10.dp)
                                .testTag("drawer_notif_time_row"),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(stringResource(R.string.drawer_notif_time), fontSize = 13.sp, color = DeepIndigo)
                            Text(
                                text = String.format(Locale.US, "%02d:%02d", notificationHour, notificationMinute),
                                fontWeight = FontWeight.Bold,
                                color = SaffronDark,
                                fontSize = 14.sp
                            )
                        }

                        if (persons.isEmpty()) {
                            Spacer(Modifier.height(8.dp))
                            Text(
                                text = stringResource(R.string.drawer_notif_disabled_hint),
                                fontSize = 11.sp,
                                color = TextMuted
                            )
                        } else if (persons.size > 1) {
                            Spacer(Modifier.height(10.dp))
                            Text(
                                text = stringResource(R.string.drawer_notif_person),
                                fontSize = 12.sp,
                                color = TextMuted
                            )
                            Spacer(Modifier.height(4.dp))
                            FlowRow(
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                persons.forEach { p ->
                                    val isSelected = p.id == effectiveNotifPersonId
                                    FilterChip(
                                        selected = isSelected,
                                        onClick = {
                                            dismissKeyboard()
                                            onUpdateNotificationPerson(p.id)
                                        },
                                        label = {
                                            Text(
                                                text = p.name,
                                                fontSize = 12.sp,
                                                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Medium
                                            )
                                        },
                                        colors = astroFilterChipColors(),
                                        border = astroFilterChipBorder(selected = isSelected),
                                        modifier = Modifier.testTag("drawer_notif_person_${p.id}")
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Chart Defaults & Ayanamsa
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = BorderStroke(1.dp, CreamBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = stringResource(R.string.drawer_settings_title),
                        style = MaterialTheme.typography.titleSmall,
                        color = DeepIndigo
                    )

                    Column {
                        Text(
                            text = stringResource(R.string.drawer_default_chart),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = DeepIndigo
                        )
                        Spacer(Modifier.height(4.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            ChartStyle.entries.forEach { style ->
                                val isSelected = chartStyle == style
                                FilterChip(
                                    selected = isSelected,
                                    onClick = {
                                        dismissKeyboard()
                                        onUpdateChartStyle(style)
                                    },
                                    label = {
                                        Text(
                                            text = stringResource(style.labelResId),
                                            fontSize = 11.sp,
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

                    Column {
                        Text(
                            text = stringResource(R.string.drawer_ayanamsa),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = DeepIndigo
                        )
                        Spacer(Modifier.height(4.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            AyanamsaType.entries.forEach { a ->
                                val isSelected = ayanamsa == a
                                FilterChip(
                                    selected = isSelected,
                                    onClick = {
                                        dismissKeyboard()
                                        onUpdateAyanamsa(a)
                                    },
                                    label = {
                                        Text(
                                            text = stringResource(a.labelResId),
                                            fontSize = 11.sp,
                                            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Medium
                                        )
                                    },
                                    colors = astroFilterChipColors(),
                                    border = astroFilterChipBorder(selected = isSelected),
                                    modifier = Modifier.heightIn(min = 48.dp)
                                )
                            }
                        }
                    }
                }
            }

            // Data Management (Export / Import / Clear)
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = BorderStroke(1.dp, CreamBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = stringResource(R.string.drawer_data_title),
                        style = MaterialTheme.typography.titleSmall,
                        color = DeepIndigo
                    )

                    TextButton(
                        onClick = onExportJson,
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 48.dp)
                            .testTag("drawer_export_json_btn")
                    ) {
                        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Download, contentDescription = null, tint = DeepIndigo)
                            Spacer(Modifier.width(10.dp))
                            Text(stringResource(R.string.drawer_export_json), color = DeepIndigo)
                        }
                    }

                    TextButton(
                        onClick = onImportJson,
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 48.dp)
                            .testTag("drawer_import_json_btn")
                    ) {
                        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Upload, contentDescription = null, tint = DeepIndigo)
                            Spacer(Modifier.width(10.dp))
                            Text(stringResource(R.string.drawer_import_json), color = DeepIndigo)
                        }
                    }

                    TextButton(
                        onClick = { showClearConfirmDialog = true },
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 48.dp)
                            .testTag("drawer_clear_all_btn")
                    ) {
                        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.DeleteForever, contentDescription = null, tint = MaterialTheme.colorScheme.error)
                            Spacer(Modifier.width(10.dp))
                            Text(stringResource(R.string.drawer_clear_all), color = MaterialTheme.colorScheme.error)
                        }
                    }
                }
            }

            // About + Disclaimer
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = CreamSurfaceVariant),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = stringResource(R.string.drawer_about_title),
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = DeepIndigo
                    )
                    Text(
                        text = stringResource(R.string.studio_name),
                        fontWeight = FontWeight.SemiBold,
                        color = SaffronDark,
                        fontSize = 12.sp
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = stringResource(R.string.drawer_about_body),
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
            title = {
                Text(
                    text = stringResource(R.string.clear_dialog_title),
                    style = MaterialTheme.typography.titleLarge,
                    color = DeepIndigo
                )
            },
            text = { Text(stringResource(R.string.clear_dialog_body), color = TextPrimary) },
            confirmButton = {
                Button(
                    onClick = {
                        showClearConfirmDialog = false
                        onClearAllData()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text(stringResource(R.string.clear_dialog_confirm))
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearConfirmDialog = false }) {
                    Text(stringResource(R.string.cancel_btn), color = DeepIndigo)
                }
            },
            containerColor = CreamBackground,
            shape = RoundedCornerShape(16.dp)
        )
    }
}
