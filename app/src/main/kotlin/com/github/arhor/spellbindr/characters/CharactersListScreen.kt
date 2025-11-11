@file:OptIn(ExperimentalMaterial3Api::class)

package com.github.arhor.spellbindr.characters

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.github.arhor.spellbindr.characters.model.CharacterSummary
import com.github.arhor.spellbindr.characters.model.EmptyCharacterList
import com.github.arhor.spellbindr.characters.model.SampleCharacterRepository
import com.github.arhor.spellbindr.ui.theme.AppTheme

@Composable
fun CharactersListScreen(
    modifier: Modifier = Modifier,
    characters: List<CharacterSummary> = SampleCharacterRepository.summaries(),
    onCharacterSelected: (CharacterSummary) -> Unit,
    onCreateCharacter: () -> Unit,
) {
    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = { Text(text = "Characters") },
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onCreateCharacter) {
                Icon(Icons.Default.Add, contentDescription = "Create character")
            }
        },
    ) { innerPadding ->
        if (characters.isEmpty()) {
            EmptyCharacterState(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                onCreateCharacter = onCreateCharacter,
            )
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                items(characters, key = { it.id }) { character ->
                    CharacterCard(
                        summary = character,
                        onClick = { onCharacterSelected(character) },
                    )
                }
            }
        }
    }
}

@Composable
private fun CharacterCard(
    summary: CharacterSummary,
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
                text = summary.name,
                style = MaterialTheme.typography.titleLarge,
            )
            Text(
                text = "Level ${summary.level} ${summary.className} • ${summary.race}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 4.dp),
            )
            Text(
                text = summary.ancestry,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.secondary,
                modifier = Modifier.padding(top = 8.dp),
            )
        }
    }
}

@Composable
private fun EmptyCharacterState(
    modifier: Modifier = Modifier,
    onCreateCharacter: () -> Unit,
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
            characters = SampleCharacterRepository.summaries(),
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
            characters = EmptyCharacterList,
            onCharacterSelected = {},
            onCreateCharacter = {},
        )
    }
}
