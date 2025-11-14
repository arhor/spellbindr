@file:OptIn(ExperimentalMaterial3Api::class)

package com.github.arhor.spellbindr.ui.feature.characters

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.github.arhor.spellbindr.ui.AppTopBarConfig
import com.github.arhor.spellbindr.ui.AppTopBarNavigation
import com.github.arhor.spellbindr.ui.WithAppTopBar
import com.github.arhor.spellbindr.ui.feature.compendium.spells.search.SpellSearchScreen
import com.github.arhor.spellbindr.ui.theme.AppTheme

@Composable
fun CharacterSpellPickerRoute(
    onBack: () -> Unit,
    onSpellAdded: (CharacterSpellAssignment) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: CharacterSpellPickerViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsState()

    CharacterSpellPickerScreen(
        state = state,
        onBack = onBack,
        onSourceChanged = viewModel::onSourceClassChanged,
        onSpellSelected = { spellId ->
            viewModel.buildAssignment(spellId)?.let(onSpellAdded)
        },
        modifier = modifier,
    )
}

@Composable
private fun CharacterSpellPickerScreen(
    state: CharacterSpellPickerUiState,
    onBack: () -> Unit,
    onSourceChanged: (String) -> Unit,
    onSpellSelected: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    WithAppTopBar(
        AppTopBarConfig(
            visible = true,
            title = { Text("Add Spells") },
            navigation = AppTopBarNavigation.Back(onBack),
        )
    ) {
        when {
            state.isLoading -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center,
                ) {
                    CircularProgressIndicator()
                }
            }

            state.errorMessage != null -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = state.errorMessage,
                        textAlign = TextAlign.Center,
                    )
                }
            }

            else -> {
                Column(
                    modifier = modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                    OutlinedTextField(
                        value = state.sourceClass,
                        onValueChange = onSourceChanged,
                        label = { Text("Spellcasting class") },
                        placeholder = { Text(text = state.defaultSourceClass.ifBlank { "Spellbook" }) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                    )
                    Text(
                        text = "Tap a spell below to add it to the character.",
                        style = MaterialTheme.typography.bodyMedium,
                    )
                    Box(modifier = Modifier.weight(1f)) {
                        SpellSearchScreen(
                            onSpellClick = onSpellSelected,
                        )
                    }
                }
            }
        }
    }
}

@Preview
@Composable
private fun CharacterSpellPickerPreview() {
    AppTheme {
        CharacterSpellPickerScreen(
            state = CharacterSpellPickerUiState(
                sourceClass = "Wizard",
                defaultSourceClass = "Wizard",
            ),
            onBack = {},
            onSourceChanged = {},
            onSpellSelected = {},
        )
    }
}
