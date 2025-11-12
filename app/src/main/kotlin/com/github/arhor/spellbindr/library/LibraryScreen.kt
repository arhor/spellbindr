@file:OptIn(ExperimentalMaterial3Api::class)

package com.github.arhor.spellbindr.library

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.github.arhor.spellbindr.library.model.MonsterSummary
import com.github.arhor.spellbindr.library.model.RuleSummary
import com.github.arhor.spellbindr.library.model.SampleLibraryContent
import com.github.arhor.spellbindr.library.model.SpellSummary
import com.github.arhor.spellbindr.ui.AppTopBarConfig
import com.github.arhor.spellbindr.ui.WithAppTopBar
import com.github.arhor.spellbindr.ui.theme.AppTheme

@Composable
fun LibraryScreen(
    modifier: Modifier = Modifier,
    onSpellSelected: (String) -> Unit,
    onMonsterSelected: (String) -> Unit,
    onRuleSelected: (String) -> Unit,
) {
    var selectedSegment by rememberSaveable { mutableStateOf(LibrarySegment.Spells) }
    var searchQuery by rememberSaveable { mutableStateOf("") }

    val filteredSpells = SampleLibraryContent.spells.filter { it.matches(searchQuery) }
    val filteredMonsters = SampleLibraryContent.monsters.filter { it.matches(searchQuery) }
    val filteredRules = SampleLibraryContent.rules.filter { it.matches(searchQuery) }

    WithAppTopBar(
        AppTopBarConfig(
            visible = true,
            title = { Text("Library") },
        )
    ) {
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(16.dp),
        ) {
            PrimaryTabRow(selectedTabIndex = selectedSegment.ordinal) {
                LibrarySegment.entries.forEach { segment ->
                    Tab(
                        selected = segment == selectedSegment,
                        onClick = { selectedSegment = segment },
                        text = { Text(segment.label) },
                    )
                }
            }
            OutlinedTextField(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp),
                value = searchQuery,
                onValueChange = { searchQuery = it },
                label = { Text("Search ${selectedSegment.label.lowercase()}") },
                singleLine = true,
            )
            when (selectedSegment) {
                LibrarySegment.Spells -> LibraryList(
                    items = filteredSpells,
                    emptyLabel = "No spells found.",
                    itemContent = { spell ->
                        LibraryCard(
                            title = spell.name,
                            primaryMeta = "Level ${spell.level} • ${spell.school}",
                            secondaryMeta = spell.classes.joinToString(),
                            onClick = { onSpellSelected(spell.id) }
                        )
                    }
                )

                LibrarySegment.Monsters -> LibraryList(
                    items = filteredMonsters,
                    emptyLabel = "No monsters found.",
                    itemContent = { monster ->
                        LibraryCard(
                            title = monster.name,
                            primaryMeta = monster.creatureType,
                            secondaryMeta = "${monster.challengeRating} • ${monster.disposition}",
                            onClick = { onMonsterSelected(monster.id) }
                        )
                    }
                )

                LibrarySegment.Rules -> LibraryList(
                    items = filteredRules,
                    emptyLabel = "No rules found.",
                    itemContent = { rule ->
                        LibraryCard(
                            title = rule.name,
                            primaryMeta = rule.snippet,
                            secondaryMeta = "Tap to open details",
                            onClick = { onRuleSelected(rule.id) }
                        )
                    }
                )
            }
        }
    }

}

@Composable
private fun <T> LibraryList(
    items: List<T>,
    emptyLabel: String,
    itemContent: @Composable (T) -> Unit,
) {
    if (items.isEmpty()) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 32.dp),
            shape = MaterialTheme.shapes.large,
            tonalElevation = 1.dp,
        ) {
            Text(
                text = emptyLabel,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(16.dp),
            )
        }
    } else {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(bottom = 32.dp),
        ) {
            items(items) { item ->
                itemContent(item)
            }
        }
    }
}

@Composable
private fun LibraryCard(
    title: String,
    primaryMeta: String,
    secondaryMeta: String,
    onClick: () -> Unit,
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        onClick = onClick,
        shape = MaterialTheme.shapes.large,
        tonalElevation = 2.dp,
    ) {
        Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 16.dp)) {
            Text(text = title, style = MaterialTheme.typography.titleMedium)
            Text(
                text = primaryMeta,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 4.dp),
            )
            Text(
                text = secondaryMeta,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.secondary,
                modifier = Modifier.padding(top = 8.dp),
            )
        }
    }
}

private enum class LibrarySegment(val label: String) {
    Spells("Spells"),
    Monsters("Monsters"),
    Rules("Rules"),
}

private fun SpellSummary.matches(query: String): Boolean =
    query.isBlank() ||
        name.contains(query, ignoreCase = true) ||
        school.contains(query, ignoreCase = true)

private fun MonsterSummary.matches(query: String): Boolean =
    query.isBlank() ||
        name.contains(query, ignoreCase = true) ||
        creatureType.contains(query, ignoreCase = true) ||
        challengeRating.contains(query, ignoreCase = true)

private fun RuleSummary.matches(query: String): Boolean =
    query.isBlank() ||
        name.contains(query, ignoreCase = true) ||
        snippet.contains(query, ignoreCase = true)

@Preview
@Composable
private fun LibraryScreenPreview() {
    AppTheme {
        LibraryScreen(
            onSpellSelected = {},
            onMonsterSelected = {},
            onRuleSelected = {},
        )
    }
}
