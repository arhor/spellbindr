package com.github.arhor.spellbindr.ui.feature.character.list

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.github.arhor.spellbindr.ui.components.ErrorMessage
import com.github.arhor.spellbindr.ui.components.LoadingIndicator
import com.github.arhor.spellbindr.ui.feature.character.list.components.CharacterCard
import com.github.arhor.spellbindr.ui.feature.character.list.components.CreateCharacterDialog

@Composable
fun CharactersListScreen(
    state: CharactersListUiState,
    dispatch: CharactersListDispatch = {},
) {
    when (state) {
        is CharactersListUiState.Loading -> LoadingIndicator()
        is CharactersListUiState.Content -> CharactersListContent(state, dispatch)
        is CharactersListUiState.Failure -> ErrorMessage(state.errorMessage)
    }
}

@Composable
private fun CharactersListContent(
    state: CharactersListUiState.Content,
    dispatch: CharactersListDispatch,
) {
    Box(modifier = Modifier.fillMaxSize()) {
        if (state.characters.isEmpty()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(
                    text = "No characters yet",
                    style = MaterialTheme.typography.titleLarge,
                )
                Text(
                    text = "Create a hero to track hit points, spell slots, and more.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 8.dp),
                )
                Text(
                    text = "Tap the + button to get started.",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(top = 16.dp),
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                items(items = state.characters, key = { it.id }) { character ->
                    CharacterCard(
                        item = character,
                        onClick = { dispatch(CharactersListIntent.SelectCharacterClicked(character)) },
                    )
                }
            }
        }
        CreateCharacterDialog(
            onCreateCharacter = { dispatch(CharactersListIntent.CreateCharacterClicked(it)) },
        )
    }
}
