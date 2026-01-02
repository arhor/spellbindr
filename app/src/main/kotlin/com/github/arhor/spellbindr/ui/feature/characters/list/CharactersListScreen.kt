@file:OptIn(ExperimentalMaterial3Api::class)

package com.github.arhor.spellbindr.ui.feature.characters.list

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
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.github.arhor.spellbindr.ui.components.AppTopBarConfig
import com.github.arhor.spellbindr.ui.components.ProvideTopBarState
import com.github.arhor.spellbindr.ui.components.TopBarState
import com.github.arhor.spellbindr.ui.theme.AppTheme

/**
 * Stateful entry point for the Characters List screen.
 *
 * Sets up the top bar and delegates UI rendering to [CharactersListScreen].
 */
@Composable
fun CharactersListRoute(
    vm: CharactersListViewModel,
    onCharacterSelected: (CharacterListItem) -> Unit,
    onCreateCharacter: () -> Unit,
) {
    val state by vm.state.collectAsStateWithLifecycle()

    ProvideTopBarState(
        topBarState = TopBarState(
            config = AppTopBarConfig(
                title = "Characters",
            ),
        ),
    ) {
        CharactersListScreen(
            uiState = state,
            onCharacterSelected = onCharacterSelected,
            onCreateCharacter = onCreateCharacter,
        )
    }
}

@Composable
fun CharactersListScreen(
    vm: CharactersListViewModel,
    onCharacterSelected: (CharacterListItem) -> Unit,
    onCreateCharacter: () -> Unit,
) {
    val state by vm.state.collectAsStateWithLifecycle()

    CharactersListScreen(
        uiState = state,
        onCharacterSelected = onCharacterSelected,
        onCreateCharacter = onCreateCharacter,
    )
}

/**
 * Pure UI composable for displaying the list of characters.
 */
@Composable
fun CharactersListScreen(
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
    val displayName by remember(item.name) {
        derivedStateOf { item.name.ifBlank { "Unnamed hero" } }
    }
    val headline by remember(item.level, item.className) {
        derivedStateOf {
            buildString {
                append("Level ${item.level.coerceAtLeast(1)}")
                if (item.className.isNotBlank()) {
                    append(' ')
                    append(item.className)
                }
            }
        }
    }
    val detail by remember(item.race, item.background) {
        derivedStateOf {
            listOfNotNull(
                item.race.takeIf { it.isNotBlank() },
                item.background.takeIf { it.isNotBlank() },
            ).joinToString(separator = " • ")
        }
    }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        tonalElevation = 2.dp,
        shape = MaterialTheme.shapes.large,
    ) {
        Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 16.dp)) {
            Text(
                text = displayName,
                style = MaterialTheme.typography.titleLarge,
            )
            if (headline.isNotBlank()) {
                Text(
                    text = headline,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 4.dp),
                )
            }
            if (detail.isNotBlank()) {
                Text(
                    text = detail,
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
        level = 7,
        className = "Wizard",
        race = "Half-elf",
        background = "Luna Conservatory",
    ),
    CharacterListItem(
        id = "2",
        name = "Bronn Blackbriar",
        level = 5,
        className = "Fighter",
        race = "Human",
        background = "Knight of the Autumn Guard",
    ),
)
