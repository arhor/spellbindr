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
    spellListViewModel: SpellListViewModel = hiltViewModel(),
    spellSearchViewModel: SpellSearchViewModel = hiltViewModel(),
) {
    val spellListViewState by spellListViewModel.state.collectAsState()
    val spellSearchViewState by spellSearchViewModel.state.collectAsState()

    var expandedClass by remember { mutableStateOf(false) }
    var showAddDialog by remember { mutableStateOf(false) }
    var selectedSpellName by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        ExposedDropdownMenuBox(
            expanded = expandedClass,
            onExpandedChange = { expandedClass = !expandedClass }
        ) {
            OutlinedTextField(
                value = spellSearchViewState.selectedClass?.toString() ?: "All Classes",
                onValueChange = {},
                readOnly = true,
                label = { Text("Filter by Class") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedClass) },
                modifier = Modifier
                    .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                    .fillMaxWidth(),
            )
            ExposedDropdownMenu(
                expanded = expandedClass,
                onDismissRequest = { expandedClass = false }
            ) {
                DropdownMenuItem(
                    text = { Text("All Classes") },
                    onClick = {
                        spellSearchViewModel.onClassFilterChanged(null)
                        expandedClass = false
                    }
                )
                SpellcastingClass.entries.forEach { spellClass ->
                    DropdownMenuItem(
                        text = { Text(spellClass.toString()) },
                        onClick = {
                            spellSearchViewModel.onClassFilterChanged(spellClass)
                            expandedClass = false
                        }
                    )
                }
            }
        }
        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = spellSearchViewState.searchQuery,
            onValueChange = spellSearchViewModel::onSearchQueryChanged,
            label = { Text("Search spells") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        when {
            spellSearchViewState.isLoading -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }

            spellSearchViewState.error != null -> {
                Text(
                    text = "Error: ${spellSearchViewState.error}",
                    color = MaterialTheme.colorScheme.error
                )
            }

            else -> {
                SpellSearchResultList(
                    spells = spellSearchViewState.spells,
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
                    spellListViewState.forEach { list ->
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
