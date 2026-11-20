package com.aryaleelastudios.astroveda.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SearchOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aryaleelastudios.astroveda.R
import com.aryaleelastudios.astroveda.data.local.PersonEntity
import com.aryaleelastudios.astroveda.ui.theme.CreamBackground
import com.aryaleelastudios.astroveda.ui.theme.CreamBorder
import com.aryaleelastudios.astroveda.ui.theme.DeepIndigo
import com.aryaleelastudios.astroveda.ui.theme.SaffronAccent
import com.aryaleelastudios.astroveda.ui.theme.SaffronDark
import com.aryaleelastudios.astroveda.ui.theme.TextMuted
import com.aryaleelastudios.astroveda.ui.theme.astroPrimaryButtonColors

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun PersonSelectorBottomSheet(
    persons: List<PersonEntity>,
    personRashis: Map<Long, String>,
    selectedPersonId: Long?,
    onSelectPerson: (PersonEntity) -> Unit,
    onAddNewPerson: () -> Unit,
    onEditPerson: (PersonEntity) -> Unit,
    onDuplicatePerson: (PersonEntity) -> Unit,
    onDeletePerson: (PersonEntity) -> Unit,
    onDismiss: () -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    var contextMenuPerson by remember { mutableStateOf<PersonEntity?>(null) }
    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current

    fun dismissKeyboard() {
        focusManager.clearFocus(force = true)
        keyboardController?.hide()
    }

    val filteredPersons = remember(persons, searchQuery, personRashis) {
        if (searchQuery.isBlank()) {
            persons
        } else {
            persons.filter {
                it.name.contains(searchQuery, ignoreCase = true) ||
                    it.birthPlace.contains(searchQuery, ignoreCase = true) ||
                    (personRashis[it.id]?.contains(searchQuery, ignoreCase = true) == true)
            }
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = CreamBackground,
        dragHandle = { BottomSheetDefaults.DragHandle(color = CreamBorder) },
        modifier = Modifier.testTag("person_selector_bottom_sheet")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clearFocusOnTap()
                .padding(horizontal = 20.dp, vertical = 8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = stringResource(R.string.sheet_select_horoscope),
                    style = MaterialTheme.typography.titleLarge,
                    color = DeepIndigo,
                    fontWeight = FontWeight.Bold
                )
                Button(
                    onClick = {
                        dismissKeyboard()
                        onDismiss()
                        onAddNewPerson()
                    },
                    colors = astroPrimaryButtonColors(),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .heightIn(min = 48.dp)
                        .testTag("bottom_sheet_add_new_person_btn")
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(6.dp))
                    Text(stringResource(R.string.cta_add_new), fontWeight = FontWeight.SemiBold)
                }
            }

            Spacer(Modifier.height(14.dp))

            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text(stringResource(R.string.sheet_search_hint)) },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = DeepIndigo) },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(Icons.Default.Close, contentDescription = stringResource(R.string.cd_clear_search), tint = TextMuted)
                        }
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
                    .testTag("person_search_input")
            )

            Spacer(Modifier.height(14.dp))

            if (filteredPersons.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.SearchOff,
                            contentDescription = null,
                            tint = TextMuted,
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(Modifier.height(8.dp))
                        Text(
                            text = stringResource(R.string.sheet_no_results),
                            color = TextMuted,
                            fontSize = 14.sp
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 380.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(filteredPersons, key = { it.id }) { person ->
                        val isSelected = person.id == selectedPersonId
                        val rashiName = personRashis[person.id].orEmpty()
                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = if (isSelected) SaffronAccent.copy(alpha = 0.15f) else Color.White,
                            border = BorderStroke(
                                width = if (isSelected) 1.5.dp else 1.dp,
                                color = if (isSelected) SaffronAccent else CreamBorder
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .combinedClickable(
                                    onClick = {
                                        dismissKeyboard()
                                        onSelectPerson(person)
                                        onDismiss()
                                    },
                                    onLongClick = {
                                        dismissKeyboard()
                                        contextMenuPerson = person
                                    }
                                )
                                .testTag("person_item_${person.id}")
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(44.dp)
                                        .background(
                                            if (isSelected) SaffronAccent else CreamBackground,
                                            CircleShape
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = person.name.take(1).uppercase(),
                                        color = DeepIndigo,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 18.sp
                                    )
                                }

                                Spacer(Modifier.width(14.dp))

                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = person.name,
                                        style = MaterialTheme.typography.titleMedium,
                                        color = DeepIndigo,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                    Spacer(Modifier.height(4.dp))
                                    Row(
                                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        if (rashiName.isNotBlank()) {
                                            AssistChip(
                                                onClick = { },
                                                label = {
                                                    Text(
                                                        text = rashiName,
                                                        fontSize = 11.sp,
                                                        fontWeight = FontWeight.SemiBold,
                                                        color = SaffronDark
                                                    )
                                                },
                                                colors = AssistChipDefaults.assistChipColors(
                                                    containerColor = SaffronAccent.copy(alpha = 0.16f)
                                                ),
                                                border = null,
                                                modifier = Modifier
                                                    .height(24.dp)
                                                    .testTag("person_rashi_chip_${person.id}")
                                            )
                                        }
                                        AssistChip(
                                            onClick = { },
                                            label = { Text(person.birthPlace, fontSize = 11.sp, maxLines = 1) },
                                            colors = AssistChipDefaults.assistChipColors(containerColor = CreamBackground),
                                            border = null,
                                            modifier = Modifier.height(24.dp)
                                        )
                                    }
                                }

                                if (isSelected) {
                                    Icon(
                                        Icons.Default.CheckCircle,
                                        contentDescription = null,
                                        tint = SaffronDark,
                                        modifier = Modifier.size(24.dp)
                                    )
                                } else {
                                    IconButton(
                                        onClick = { contextMenuPerson = person },
                                        modifier = Modifier.testTag("person_more_opts_${person.id}")
                                    ) {
                                        Icon(
                                            Icons.Default.MoreVert,
                                            contentDescription = stringResource(R.string.context_choose_action),
                                            tint = TextMuted
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            Spacer(Modifier.height(16.dp))
        }
    }

    if (contextMenuPerson != null) {
        val p = contextMenuPerson!!
        AlertDialog(
            onDismissRequest = { contextMenuPerson = null },
            title = {
                Text(
                    text = p.name,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = DeepIndigo
                )
            },
            text = {
                Column {
                    Text(
                        text = stringResource(R.string.context_choose_action),
                        fontSize = 14.sp,
                        color = TextMuted
                    )
                    Spacer(Modifier.height(12.dp))

                    TextButton(
                        onClick = {
                            contextMenuPerson = null
                            onDismiss()
                            onEditPerson(p)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 48.dp)
                            .testTag("context_edit_btn")
                    ) {
                        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Edit, contentDescription = null, tint = DeepIndigo)
                            Spacer(Modifier.width(12.dp))
                            Text(stringResource(R.string.context_edit), color = DeepIndigo)
                        }
                    }

                    TextButton(
                        onClick = {
                            contextMenuPerson = null
                            onDuplicatePerson(p)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 48.dp)
                            .testTag("context_duplicate_btn")
                    ) {
                        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.ContentCopy, contentDescription = null, tint = DeepIndigo)
                            Spacer(Modifier.width(12.dp))
                            Text(stringResource(R.string.context_duplicate), color = DeepIndigo)
                        }
                    }

                    TextButton(
                        onClick = {
                            contextMenuPerson = null
                            onDeletePerson(p)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 48.dp)
                            .testTag("context_delete_btn")
                    ) {
                        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Delete, contentDescription = null, tint = MaterialTheme.colorScheme.error)
                            Spacer(Modifier.width(12.dp))
                            Text(stringResource(R.string.context_delete), color = MaterialTheme.colorScheme.error)
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { contextMenuPerson = null }) {
                    Text(stringResource(R.string.close_btn), color = DeepIndigo)
                }
            },
            containerColor = CreamBackground,
            shape = RoundedCornerShape(16.dp)
        )
    }
}
