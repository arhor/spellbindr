@file:OptIn(ExperimentalMaterial3Api::class)

package com.github.arhor.spellbindr.characters

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.github.arhor.spellbindr.characters.model.AbilityScore
import com.github.arhor.spellbindr.characters.model.CharacterDetails
import com.github.arhor.spellbindr.characters.model.SampleCharacterRepository
import com.github.arhor.spellbindr.ui.AppTopBarConfig
import com.github.arhor.spellbindr.ui.AppTopBarNavigation
import com.github.arhor.spellbindr.ui.ProvideTopBar
import com.github.arhor.spellbindr.ui.theme.AppTheme

@Composable
fun CharacterSheetScreen(
    characterId: String,
    onBack: () -> Unit,
    onLevelUp: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val details = remember(characterId) {
        SampleCharacterRepository.details(characterId)
    }

    var selectedTab by rememberSaveable { mutableStateOf(CharacterSheetTab.Sheet) }
    var overflowExpanded by remember { mutableStateOf(false) }

    ProvideTopBar(
        AppTopBarConfig(
            visible = true,
            title = {
                Text(
                    text = "${details.summary.name} – Level ${details.summary.level}",
                    maxLines = 1,
                )
            },
            navigation = AppTopBarNavigation.Back(onBack),
            actions = {
                IconButton(onClick = { overflowExpanded = true }) {
                    Icon(
                        imageVector = Icons.Default.MoreVert,
                        contentDescription = "More actions",
                    )
                }
                DropdownMenu(
                    expanded = overflowExpanded,
                    onDismissRequest = { overflowExpanded = false },
                ) {
                    DropdownMenuItem(
                        text = { Text("Level up") },
                        onClick = {
                            overflowExpanded = false
                            onLevelUp(details.summary.id)
                        },
                    )
                }
            },
        )
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
            .verticalScroll(rememberScrollState()),
    ) {
        CharacterHeroSection(details = details)
        Spacer(modifier = Modifier.height(24.dp))
        PrimaryTabRow(
            selectedTabIndex = selectedTab.ordinal,
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
            contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
        ) {
            CharacterSheetTab.entries.forEach { tab ->
                Tab(
                    selected = tab == selectedTab,
                    onClick = { selectedTab = tab },
                    text = { Text(tab.label) },
                )
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
        when (selectedTab) {
            CharacterSheetTab.Sheet -> CharacterSheetTabContent(details = details)
            CharacterSheetTab.Spells -> CharacterSpellsTab(details = details)
            CharacterSheetTab.Notes -> CharacterNotesTab(details = details)
        }
    }
}

@Composable
private fun CharacterHeroSection(details: CharacterDetails) {
    Surface(
        shape = MaterialTheme.shapes.large,
        tonalElevation = 4.dp,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(
                text = details.summary.ancestry,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.secondary,
            )
            Spacer(modifier = Modifier.height(12.dp))
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.horizontalScroll(rememberScrollState()),
            ) {
                HeroChip(label = "HP", value = details.vitals.hitPoints)
                HeroChip(label = "AC", value = details.vitals.armorClass.toString())
                HeroChip(label = "Initiative", value = "+${details.vitals.initiative}")
            }
        }
    }
}

@Composable
private fun HeroChip(label: String, value: String) {
    Surface(
        shape = MaterialTheme.shapes.medium,
        tonalElevation = 3.dp,
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = value,
                style = MaterialTheme.typography.titleLarge,
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun CharacterSheetTabContent(details: CharacterDetails) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        AbilityGrid(abilities = details.abilityScores)
        Surface(
            shape = MaterialTheme.shapes.large,
            tonalElevation = 1.dp,
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Quick notes",
                    style = MaterialTheme.typography.titleMedium,
                )
                Text(
                    text = "Track class features, resources, or reminders. Everything syncs locally so it works offline.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 8.dp),
                )
            }
        }
    }
}

@Composable
private fun AbilityGrid(abilities: List<AbilityScore>) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        abilities.forEach { ability ->
            Surface(
                shape = MaterialTheme.shapes.large,
                tonalElevation = 2.dp,
            ) {
                Column(
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 14.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Text(
                        text = ability.label,
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.secondary,
                    )
                    Text(
                        text = ability.value.toString(),
                        style = MaterialTheme.typography.headlineSmall,
                    )
                    Text(
                        text = modifierText(ability.modifier),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
    }
}

@Composable
private fun CharacterSpellsTab(details: CharacterDetails) {
    if (details.preparedSpells.isEmpty()) {
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = MaterialTheme.shapes.large,
            tonalElevation = 1.dp,
        ) {
            Text(
                text = "No prepared spells. Use this space to mark known spells or resources once data is available.",
                modifier = Modifier.padding(16.dp),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    } else {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            details.preparedSpells.forEach { spell ->
                Surface(
                    shape = MaterialTheme.shapes.large,
                    tonalElevation = 1.dp,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text(
                        text = spell,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                        style = MaterialTheme.typography.bodyLarge,
                    )
                }
            }
        }
    }
}

@Composable
private fun CharacterNotesTab(details: CharacterDetails) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        details.notes.forEach { note ->
            Surface(
                shape = MaterialTheme.shapes.large,
                tonalElevation = 1.dp,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(
                    text = note,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                )
            }
        }
    }
}

private enum class CharacterSheetTab(val label: String) {
    Sheet("Sheet"),
    Spells("Spells"),
    Notes("Notes"),
}

private fun modifierText(value: Int): String = when {
    value > 0 -> "+$value"
    else -> value.toString()
}

@Preview
@Composable
private fun CharacterSheetPreview() {
    AppTheme {
        CharacterSheetScreen(
            characterId = "astra",
            onBack = {},
            onLevelUp = {},
        )
    }
}
