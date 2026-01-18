package com.github.arhor.spellbindr.ui.feature.characters.spellpicker

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.github.arhor.spellbindr.domain.model.CharacterSpellAssignment
import com.github.arhor.spellbindr.ui.components.ErrorMessage
import com.github.arhor.spellbindr.ui.components.LoadingIndicator
import com.github.arhor.spellbindr.ui.feature.compendium.spells.components.SpellList
import com.github.arhor.spellbindr.ui.feature.compendium.spells.components.SpellSearchInput
import com.github.arhor.spellbindr.ui.theme.AppTheme

@Composable
fun CharacterSpellPickerScreen(
    state: CharacterSpellPickerUiState,
    onSourceClassChanged: (String) -> Unit = {},
    onQueryChanged: (String) -> Unit = {},
    onFavoriteClick: () -> Unit = {},
    onSpellClick: (CharacterSpellAssignment) -> Unit = {},
) {
    when (state) {
        is CharacterSpellPickerUiState.Loading -> LoadingIndicator()

        is CharacterSpellPickerUiState.Content -> CharacterSpellPickerContent(
            state = state,
            onSourceClassChanged = onSourceClassChanged,
            onQueryChanged = onQueryChanged,
            onFavoriteClick = onFavoriteClick,
            onSpellClick = onSpellClick,
        )

        is CharacterSpellPickerUiState.Failure -> ErrorMessage(state.errorMessage)
    }
}

@Composable
private fun CharacterSpellPickerContent(
    state: CharacterSpellPickerUiState.Content,
    onSourceClassChanged: (String) -> Unit,
    onQueryChanged: (String) -> Unit,
    onFavoriteClick: () -> Unit,
    onSpellClick: (CharacterSpellAssignment) -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        OutlinedTextField(
            value = state.sourceClass,
            onValueChange = { value -> onSourceClassChanged(value) },
            label = { Text("Spellcasting class") },
            placeholder = { Text(text = state.defaultSourceClass.ifBlank { "Spellbook" }) },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
        )
        Text(
            text = "Tap a spell below to add it to the character.",
            style = MaterialTheme.typography.bodyMedium,
        )
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                SpellSearchInput(
                    query = state.query,
                    onQueryChanged = onQueryChanged,
                    showFavorite = state.showFavoriteOnly,
                    onFavoriteClick = onFavoriteClick,
                )
                Spacer(modifier = Modifier.height(16.dp))
                SpellList(
                    spells = state.spells,
                    onSpellClick = {
                        onSpellClick(
                            CharacterSpellAssignment(
                                it.id,
                                state.sourceClass.ifBlank { state.defaultSourceClass },
                            )
                        )
                    },
                )
            }
        }
    }
}

@Preview
@Composable
private fun CharacterSpellPickerPreview() {
    AppTheme {
        CharacterSpellPickerScreen(
            state = CharacterSpellPickerUiState.Content(
                sourceClass = "Wizard",
                defaultSourceClass = "Wizard",
                query = "",
                spells = emptyList(),
                showFavoriteOnly = false,
                castingClasses = emptyList(),
                currentClasses = emptySet(),
            ),
        )
    }
}
