package com.github.arhor.spellbindr.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.selection.toggleable
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.github.arhor.spellbindr.data.model.Spell
import com.github.arhor.spellbindr.data.model.SpellList
import com.github.arhor.spellbindr.viewmodel.SpellListViewModel

@Composable
fun EditSpellListScreen(
    initialList: SpellList? = null,
    onSave: () -> Unit,
    onCancel: () -> Unit,
    spellListViewModel: SpellListViewModel
) {
    val spellListsState by spellListViewModel.spellLists.collectAsState()

    val allSpells = remember { spellListViewModel.getAllSpells() }

    var name by remember { mutableStateOf(TextFieldValue(initialList?.name ?: "")) }
    var selectedSpellNames by remember {
        mutableStateOf(
            initialList?.spellNames?.toSet() ?: setOf<String>()
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            label = { Text("List Name") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text("Select Spells:", style = MaterialTheme.typography.titleMedium)
        Spacer(modifier = Modifier.height(8.dp))
        LazyColumn(modifier = Modifier.weight(1f)) {
            items(allSpells.size) { index ->
                val spell = allSpells[index]
                val isSelected = spell.name in selectedSpellNames
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .toggleable(
                            value = isSelected,
                            onValueChange = {
                                if (isSelected) {
                                    selectedSpellNames -= spell.name
                                } else {
                                    selectedSpellNames += spell.name
                                }
                                selectedSpellNames =
                                    selectedSpellNames.toMutableSet() // trigger recomposition
                            }
                        )
                        .padding(vertical = 4.dp),
                ) {
                    Checkbox(
                        checked = isSelected,
                        onCheckedChange = null // handled by Row
                    )
                    Text(
                        text = spell.name,
                        modifier = Modifier.padding(start = 8.dp)
                    )
                }
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
            TextButton(onClick = onCancel) { Text("Cancel") }
            Spacer(modifier = Modifier.width(8.dp))
            Button(
                onClick = {
                    if (
                        name.text.isNotBlank() &&
                        selectedSpellNames.isNotEmpty() &&
                        spellListsState.none { it.name == name.text }
                    ) {
                        val newList = SpellList(name.text, selectedSpellNames.toList())
                        if (initialList == null) {
                            spellListViewModel.addSpellList(newList)
                        } else {
                            spellListViewModel.updateSpellList(newList)
                        }
                        onSave()
                    }
                },
                enabled = name.text.isNotBlank() && selectedSpellNames.isNotEmpty()
            ) {
                Text("Save")
            }
        }
    }
} 