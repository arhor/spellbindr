package com.github.arhor.spellbindr.ui.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FilterAlt
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
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
import com.github.arhor.spellbindr.ui.spells.search.SearchFilterDialog
import com.github.arhor.spellbindr.ui.spells.search.SpellSearchResultList
import com.github.arhor.spellbindr.viewmodel.SpellSearchViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SpellSearchScreen(
    onSpellClick: (String) -> Unit = {},
    spellSearchVM: SpellSearchViewModel = hiltViewModel(),
) {
    val spellSearchViewState by spellSearchVM.state.collectAsState()
    var showFilterDialog by remember { mutableStateOf(false) }

    fun handleFilterChanges(selectedClasses: Set<SpellcastingClass>) {
        spellSearchVM.onClassFilterChanged(selectedClasses)
        showFilterDialog = false
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Spell Book",
                style = MaterialTheme.typography.titleLarge,
            )
            Spacer(Modifier.weight(1f))
            Box {
                IconButton(onClick = { showFilterDialog = true }) {
                    Icon(Icons.Default.FilterAlt, contentDescription = "Filter by class")
                }
            }
        }
        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = spellSearchViewState.searchQuery,
            onValueChange = spellSearchVM::onSearchQueryChanged,
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
                )
            }
        }
    }

    SearchFilterDialog(
        showFilterDialog = showFilterDialog,
        currentClasses = spellSearchViewState.selectedClasses,
        onSubmit = ::handleFilterChanges,
        onCancel = ::handleFilterChanges,
    )
}
