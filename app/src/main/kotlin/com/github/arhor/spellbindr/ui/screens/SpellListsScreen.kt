package com.github.arhor.spellbindr.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.github.arhor.spellbindr.viewmodel.SpellListViewModel
import com.github.arhor.spellbindr.data.repository.SpellRepository
import com.github.arhor.spellbindr.data.model.SpellList
import com.github.arhor.spellbindr.data.model.Spell

@Composable
fun SpellListsScreen(
    onCreateList: () -> Unit = {},
    onEditList: (String) -> Unit = {},
    onViewList: (String) -> Unit = {},
    spellListViewModel: SpellListViewModel = hiltViewModel()
) {
    val viewModel = spellListViewModel
    val spellLists = viewModel.spellLists.collectAsState().value

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
            Button(onClick = onCreateList) {
                Text("Create New List")
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
        LazyColumn {
            items(spellLists.size) { index ->
                val list = spellLists[index]
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    onClick = { onViewList(list.name) }
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = list.name,
                            style = MaterialTheme.typography.titleMedium,
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(text = "${list.spellNames.size} spells", style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SpellListDetailScreen(
    listName: String,
    spellList: SpellList?,
    spellListViewModel: SpellListViewModel,
    onEdit: () -> Unit = {},
    onBack: () -> Unit = {},
    onSpellClick: (String) -> Unit = {}
) {
    val spells: List<Spell> = spellList?.spellNames?.mapNotNull { name ->
        spellListViewModel.getAllSpells().find { it.name == name }
    } ?: emptyList()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(listName) },
                navigationIcon = {
                    IconButton(onClick = onBack) { Text("Back") }
                },
                actions = {
                    IconButton(onClick = onEdit) { Text("Edit") }
                }
            )
        }
    ) { innerPadding ->
        Column(modifier = Modifier.padding(innerPadding).padding(16.dp)) {
            Text("Spells in this list:", style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(8.dp))
            LazyColumn {
                items(spells.size) { index ->
                    val spell = spells[index]
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                            .clickable { onSpellClick(spell.name) }
                    ) {
                        Text(
                            text = "Lvl. ${spell.level} ${spell.name}",
                            style = MaterialTheme.typography.bodyLarge,
                            modifier = Modifier.padding(16.dp)
                        )
                    }
                }
            }
        }
    }
} 