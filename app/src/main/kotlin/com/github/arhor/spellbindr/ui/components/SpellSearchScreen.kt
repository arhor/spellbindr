package com.github.arhor.spellbindr.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.github.arhor.spellbindr.data.model.Spell
import com.github.arhor.spellbindr.viewmodel.SpellSearchViewModel
import org.koin.androidx.compose.koinViewModel

@Composable
fun SpellSearchScreen(
    viewModel: SpellSearchViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        OutlinedTextField(
            value = uiState.searchQuery,
            onValueChange = viewModel::onSearchQueryChanged,
            label = { Text("Search spells") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        when {
            uiState.isLoading -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }

            uiState.error != null -> {
                Text(
                    text = "Error: ${uiState.error}",
                    color = MaterialTheme.colorScheme.error
                )
            }

            else -> {
                SpellList(spells = uiState.spells)
            }
        }
    }
}

@Composable
private fun SpellList(spells: List<Spell>) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(spells) { spell ->
            SpellCard(spell = spell)
        }
    }
}

@Composable
private fun SpellCard(spell: Spell) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = spell.name,
                style = MaterialTheme.typography.titleLarge
            )
            Text(
                text = "Level ${spell.level} ${spell.school}",
                style = MaterialTheme.typography.bodyMedium
            )
            Text(
                text = spell.desc,
                style = MaterialTheme.typography.bodySmall,
                maxLines = 3
            )
        }
    }
} 