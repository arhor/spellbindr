package com.github.arhor.spellbindr.ui.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.github.arhor.spellbindr.data.model.SpellcastingClass
import com.github.arhor.spellbindr.viewmodel.SpellListViewModel
import com.github.arhor.spellbindr.viewmodel.SpellSearchViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SpellSearchScreen(
    onSpellClick: (String) -> Unit = {},
    spellListViewModel: SpellListViewModel = hiltViewModel()
) {
    val hiltViewModel = hiltViewModel<SpellSearchViewModel>()
    val state by hiltViewModel.state.collectAsState()
    var expanded by remember { mutableStateOf(false) }
    val selectedClass = state.selectedClass
    val spellLists = spellListViewModel.spellLists.collectAsState().value
    var showAddDialog by remember { mutableStateOf(false) }
    var selectedSpellName by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = !expanded }
        ) {
            OutlinedTextField(
                value = selectedClass?.toString() ?: "All Classes",
                onValueChange = {},
                readOnly = true,
                label = { Text("Filter by Class") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                modifier = Modifier
                    .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                    .fillMaxWidth(),
            )
            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                DropdownMenuItem(
                    text = { Text("All Classes") },
                    onClick = {
                        hiltViewModel.onClassFilterChanged(null)
                        expanded = false
                    }
                )
                SpellcastingClass.entries.forEach { spellClass ->
                    DropdownMenuItem(
                        text = { Text(spellClass.toString()) },
                        onClick = {
                            hiltViewModel.onClassFilterChanged(spellClass)
                            expanded = false
                        }
                    )
                }
            }
        }
        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = state.searchQuery,
            onValueChange = hiltViewModel::onSearchQueryChanged,
            label = { Text("Search spells") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        when {
            state.isLoading -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }

            state.error != null -> {
                Text(
                    text = "Error: ${state.error}",
                    color = MaterialTheme.colorScheme.error
                )
            }

            else -> {
                SpellList(
                    spells = state.spells,
                    onSpellClick = onSpellClick,
                    onSpellFavor = {
                        selectedSpellName = it
                        showAddDialog = true
                    },
                )
            }
        }
    }

    if (showAddDialog && selectedSpellName != null) {
        AlertDialog(
            onDismissRequest = { showAddDialog = false },
            title = { Text("Add '${selectedSpellName}' to which list?") },
            text = {
                Column {
                    spellLists.forEach { list ->
                        TextButton(onClick = {
                            val updated = list.copy(
                                spellNames = (list.spellNames + selectedSpellName!!).distinct()
                            )
                            spellListViewModel.updateSpellList(updated)
                            showAddDialog = false
                        }) {
                            Text(list.name)
                        }
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { showAddDialog = false }) { Text("Cancel") }
            }
        )
    }
}
