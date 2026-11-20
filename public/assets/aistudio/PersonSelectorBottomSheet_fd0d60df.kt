package com.aryaleelastudios.kundaliguru.ui.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aryaleelastudios.kundaliguru.data.local.PersonEntity
import com.aryaleelastudios.kundaliguru.ui.theme.CreamBackground
import com.aryaleelastudios.kundaliguru.ui.theme.CreamBorder
import com.aryaleelastudios.kundaliguru.ui.theme.DeepIndigo
import com.aryaleelastudios.kundaliguru.ui.theme.SaffronAccent
import com.aryaleelastudios.kundaliguru.ui.theme.TextMuted

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun PersonSelectorBottomSheet(
    persons: List<PersonEntity>,
    selectedPersonId: Long?,
    isHindi: Boolean,
    onSelectPerson: (PersonEntity) -> Unit,
    onAddNewPerson: () -> Unit,
    onEditPerson: (PersonEntity) -> Unit,
    onDuplicatePerson: (PersonEntity) -> Unit,
    onDeletePerson: (PersonEntity) -> Unit,
    onDismiss: () -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    var contextMenuPerson by remember { mutableStateOf<PersonEntity?>(null) }

    val filteredPersons = remember(persons, searchQuery) {
        if (searchQuery.isBlank()) persons
        else persons.filter { it.name.contains(searchQuery, ignoreCase = true) || it.birthPlace.contains(searchQuery, ignoreCase = true) }
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
                .padding(horizontal = 20.dp, vertical = 8.dp)
        ) {
            // Header Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = if (isHindi) "कुंडली चयन करें" else "Select Horoscope",
                    style = MaterialTheme.typography.titleLarge,
                    color = DeepIndigo,
                    fontWeight = FontWeight.Bold
                )
                Button(
                    onClick = {
                        onDismiss()
                        onAddNewPerson()
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = DeepIndigo,
                        contentColor = SaffronAccent
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.testTag("bottom_sheet_add_new_person_btn")
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(6.dp))
                    Text(if (isHindi) "+ नई कुंडली" else "+ Add New", fontWeight = FontWeight.SemiBold)
                }
            }

            Spacer(Modifier.height(14.dp))

            // Search Bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text(if (isHindi) "नाम या स्थान से खोजें..." else "Search by name or place...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = DeepIndigo) },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(Icons.Default.Close, contentDescription = null, tint = TextMuted)
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
                            text = if (isHindi) "कोई कुंडली नहीं मिली" else "No matching profile found",
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
                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = if (isSelected) SaffronAccent.copy(alpha = 0.15f) else Color.White,
                            border = androidx.compose.foundation.BorderStroke(
                                width = if (isSelected) 1.5.dp else 1.dp,
                                color = if (isSelected) SaffronAccent else CreamBorder
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .combinedClickable(
                                    onClick = {
                                        onSelectPerson(person)
                                        onDismiss()
                                    },
                                    onLongClick = {
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
                                            if (isSelected) DeepIndigo else CreamBackground,
                                            CircleShape
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = person.name.take(1).uppercase(),
                                        color = if (isSelected) SaffronAccent else DeepIndigo,
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
                                    Spacer(Modifier.height(3.dp))
                                    Row(
                                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        AssistChip(
                                            onClick = { },
                                            label = { Text(person.birthDate, fontSize = 11.sp) },
                                            colors = AssistChipDefaults.assistChipColors(containerColor = CreamBackground),
                                            border = null,
                                            modifier = Modifier.height(24.dp)
                                        )
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
                                        contentDescription = "Selected",
                                        tint = SaffronAccent,
                                        modifier = Modifier.size(24.dp)
                                    )
                                } else {
                                    IconButton(
                                        onClick = { contextMenuPerson = person },
                                        modifier = Modifier.testTag("person_more_opts_${person.id}")
                                    ) {
                                        Icon(Icons.Default.MoreVert, contentDescription = "Options", tint = TextMuted)
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

    // Context Menu Dialog (Edit / Duplicate / Delete)
    if (contextMenuPerson != null) {
        val p = contextMenuPerson!!
        AlertDialog(
            onDismissRequest = { contextMenuPerson = null },
            title = { Text(p.name, fontWeight = FontWeight.Bold, color = DeepIndigo) },
            text = {
                Column {
                    Text(
                        text = if (isHindi) "इस कुंडली के लिए कार्य चुनें:" else "Choose an action for this profile:",
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
                        modifier = Modifier.fillMaxWidth().testTag("context_edit_btn")
                    ) {
                        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Edit, contentDescription = null, tint = DeepIndigo)
                            Spacer(Modifier.width(12.dp))
                            Text(if (isHindi) "संपादित करें (Edit)" else "Edit Profile", color = DeepIndigo)
                        }
                    }

                    TextButton(
                        onClick = {
                            contextMenuPerson = null
                            onDuplicatePerson(p)
                        },
                        modifier = Modifier.fillMaxWidth().testTag("context_duplicate_btn")
                    ) {
                        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.ContentCopy, contentDescription = null, tint = DeepIndigo)
                            Spacer(Modifier.width(12.dp))
                            Text(if (isHindi) "प्रतिलिपि बनाएं (Duplicate)" else "Duplicate Profile", color = DeepIndigo)
                        }
                    }

                    TextButton(
                        onClick = {
                            contextMenuPerson = null
                            onDeletePerson(p)
                        },
                        modifier = Modifier.fillMaxWidth().testTag("context_delete_btn")
                    ) {
                        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Delete, contentDescription = null, tint = MaterialTheme.colorScheme.error)
                            Spacer(Modifier.width(12.dp))
                            Text(if (isHindi) "हटाएं (Delete)" else "Delete Profile", color = MaterialTheme.colorScheme.error)
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { contextMenuPerson = null }) {
                    Text(if (isHindi) "बंद करें" else "Close", color = DeepIndigo)
                }
            },
            containerColor = CreamBackground,
            shape = RoundedCornerShape(16.dp)
        )
    }
}
