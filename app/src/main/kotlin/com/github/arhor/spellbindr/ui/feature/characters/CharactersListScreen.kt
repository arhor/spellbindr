@file:OptIn(ExperimentalMaterial3Api::class)

package com.github.arhor.spellbindr.ui.feature.characters

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.github.arhor.spellbindr.ui.theme.AppTheme

@Composable
fun CharactersListScreen(
    vm: CharactersListViewModel,
    onCharacterSelected: (CharacterListItem) -> Unit,
    onCreateCharacter: () -> Unit,
) {
    val state by vm.state.collectAsState()

    CharactersListScreen(
        uiState = state,
        onCharacterSelected = onCharacterSelected,
        onCreateCharacter = onCreateCharacter,
    )
}

@Composable
private fun CharactersListScreen(
    uiState: CharactersListUiState,
    onCharacterSelected: (CharacterListItem) -> Unit,
    onCreateCharacter: () -> Unit,
) {
    Box(modifier = Modifier.fillMaxSize()) {
        when {
            uiState.isLoading -> {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center),
                )
            }

            uiState.isEmpty -> {
                EmptyCharacterState(
                    modifier = Modifier.fillMaxSize(),
                )
            }

            else -> {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 24.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                    items(uiState.characters, key = { it.id }) { character ->
                        CharacterCard(
                            item = character,
                            onClick = { onCharacterSelected(character) },
                        )
                    }
                }
            }
        }
        FloatingActionButton(
            onClick = onCreateCharacter,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp),
        ) {
            Icon(Icons.Default.Add, contentDescription = "Create character")
        }
    }
}

@Composable
private fun CharacterCard(
    item: CharacterListItem,
    onClick: () -> Unit,
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        tonalElevation = 2.dp,
        shape = MaterialTheme.shapes.large,
    ) {
        Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 16.dp)) {
            Text(
                text = item.name,
                style = MaterialTheme.typography.titleLarge,
            )
            if (item.headline.isNotBlank()) {
                Text(
                    text = item.headline,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 4.dp),
                )
            }
            if (item.detail.isNotBlank()) {
                Text(
                    text = item.detail,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.secondary,
                    modifier = Modifier.padding(top = 8.dp),
                )
            }
        }
    }
}

@Composable
private fun EmptyCharacterState(
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.padding(32.dp),
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
}

@Preview
@Composable
private fun CharactersListPreview() {
    AppTheme {
        CharactersListScreen(
            uiState = CharactersListUiState(
                characters = previewCharacters(),
                isLoading = false,
                isEmpty = false,
            ),
            onCharacterSelected = {},
            onCreateCharacter = {},
        )
    }
}

@Preview
@Composable
private fun CharactersListEmptyPreview() {
    AppTheme {
        CharactersListScreen(
            uiState = CharactersListUiState(
                characters = emptyList(),
                isLoading = false,
                isEmpty = true,
            ),
            onCharacterSelected = {},
            onCreateCharacter = {},
        )
    }
}

private fun previewCharacters(): List<CharacterListItem> = listOf(
    CharacterListItem(
        id = "1",
        name = "Astra Moonshadow",
        headline = "Level 7 Wizard",
        detail = "Half-elf • Luna Conservatory",
    ),
    CharacterListItem(
        id = "2",
        name = "Bronn Blackbriar",
        headline = "Level 5 Fighter",
        detail = "Human • Knight of the Autumn Guard",
    ),
)
