@file:OptIn(ExperimentalMaterial3Api::class)

package com.github.arhor.spellbindr.ui.feature.characters

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.github.arhor.spellbindr.data.model.AbilityScores
import com.github.arhor.spellbindr.data.model.CharacterSheet
import com.github.arhor.spellbindr.data.model.predefined.Ability
import com.github.arhor.spellbindr.ui.AppTopBarConfig
import com.github.arhor.spellbindr.ui.AppTopBarNavigation
import com.github.arhor.spellbindr.ui.WithAppTopBar
import com.github.arhor.spellbindr.ui.theme.AppTheme

@Composable
fun CharacterSheetRoute(
    onBack: () -> Unit,
    onEditCharacter: (String) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: CharacterSheetViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsState()
    CharacterSheetScreen(
        state = state,
        onBack = onBack,
        onEditCharacter = onEditCharacter,
        modifier = modifier,
    )
}

@Composable
fun CharacterSheetScreen(
    state: CharacterSheetUiState,
    onBack: () -> Unit,
    onEditCharacter: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    var selectedTab by rememberSaveable(state.character?.id) {
        mutableStateOf(CharacterSheetTab.Sheet)
    }
    var overflowExpanded by remember(state.character?.id) {
        mutableStateOf(false)
    }

    WithAppTopBar(
        AppTopBarConfig(
            visible = true,
            title = {
                Text(
                    text = state.character?.let(::titleFor) ?: "Character",
                    maxLines = 1,
                )
            },
            navigation = AppTopBarNavigation.Back(onBack),
            actions = {
                state.character?.let { sheet ->
                    IconButton(onClick = { overflowExpanded = true }) {
                        Icon(
                            imageVector = Icons.Filled.MoreVert,
                            contentDescription = "More actions",
                        )
                    }
                    DropdownMenu(
                        expanded = overflowExpanded,
                        onDismissRequest = { overflowExpanded = false },
                    ) {
                        DropdownMenuItem(
                            text = { Text("Edit") },
                            onClick = {
                                overflowExpanded = false
                                onEditCharacter(sheet.id)
                            },
                        )
                    }
                }
            },
        )
    ) {
        Box(
            modifier = modifier
                .fillMaxSize()
                .padding(vertical = 16.dp),
        ) {
            when {
                state.isLoading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center),
                    )
                }

                state.character != null -> {
                    CharacterSheetContent(
                        sheet = state.character,
                        selectedTab = selectedTab,
                        onTabSelected = { selectedTab = it },
                    )
                }

                else -> {
                    CharacterSheetError(
                        message = state.errorMessage ?: "Unable to load character.",
                        modifier = Modifier
                            .align(Alignment.Center)
                            .padding(horizontal = 32.dp),
                    )
                }
            }
        }
    }
}

@Composable
private fun CharacterSheetContent(
    sheet: CharacterSheet,
    selectedTab: CharacterSheetTab,
    onTabSelected: (CharacterSheetTab) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
            .verticalScroll(rememberScrollState()),
    ) {
        CharacterHeroSection(sheet = sheet)
        Spacer(modifier = Modifier.height(24.dp))
        PrimaryTabRow(
            selectedTabIndex = selectedTab.ordinal,
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
            contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
        ) {
            CharacterSheetTab.entries.forEach { tab ->
                Tab(
                    selected = tab == selectedTab,
                    onClick = { onTabSelected(tab) },
                    text = { Text(tab.label) },
                )
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
        when (selectedTab) {
            CharacterSheetTab.Sheet -> CharacterSheetOverviewTab(sheet)
            CharacterSheetTab.Spells -> CharacterSheetSpellsTab(sheet)
            CharacterSheetTab.Notes -> CharacterSheetNotesTab(sheet)
        }
        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
private fun CharacterHeroSection(sheet: CharacterSheet) {
    Surface(
        shape = MaterialTheme.shapes.large,
        tonalElevation = 4.dp,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(
                text = heroSubtitle(sheet),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.secondary,
            )
            Spacer(modifier = Modifier.height(12.dp))
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.horizontalScroll(rememberScrollState()),
            ) {
                HeroChip(label = "HP", value = hitPointsLabel(sheet))
                HeroChip(label = "AC", value = sheet.armorClass.toString())
                HeroChip(label = "Initiative", value = modifierText(sheet.initiative))
                sheet.speed.takeIf { it.isNotBlank() }?.let {
                    HeroChip(label = "Speed", value = it)
                }
                HeroChip(label = "Prof. Bonus", value = modifierText(sheet.proficiencyBonus))
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
private fun CharacterSheetOverviewTab(sheet: CharacterSheet) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        AbilityGrid(abilities = sheet.toAbilityDisplays())
        InfoCard(
            title = "Core details",
            body = buildString {
                append(heroSubtitle(sheet))
                sheet.alignment.takeIf { it.isNotBlank() }?.let {
                    append("\nAlignment: ")
                    append(it.trim())
                }
                sheet.experiencePoints?.let {
                    append("\nXP: ")
                    append(it)
                }
            },
        )
        InfoCard(
            title = "Senses & Languages",
            body = listOfNotBlank(
                sheet.senses.takeIf { it.isNotBlank() }?.let { "Senses: ${it.trim()}" },
                sheet.languages.takeIf { it.isNotBlank() }?.let { "Languages: ${it.trim()}" },
            ).joinToString(separator = "\n"),
        )
        InfoCard(
            title = "Proficiencies & Equipment",
            body = listOfNotBlank(
                sheet.proficiencies.takeIf { it.isNotBlank() }?.let { "Proficiencies:\n${it.trim()}" },
                sheet.equipment.takeIf { it.isNotBlank() }?.let { "Equipment:\n${it.trim()}" },
            ).joinToString(separator = "\n\n"),
        )
    }
}

@Composable
private fun CharacterSheetSpellsTab(sheet: CharacterSheet) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        InfoCard(
            title = "Attacks & Cantrips",
            body = sheet.attacksAndCantrips,
        )
        InfoCard(
            title = "Features & Traits",
            body = sheet.featuresAndTraits,
        )
    }
}

@Composable
private fun CharacterSheetNotesTab(sheet: CharacterSheet) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        InfoCard(
            title = "Personality Traits",
            body = sheet.personalityTraits,
        )
        InfoCard(
            title = "Ideals",
            body = sheet.ideals,
        )
        InfoCard(
            title = "Bonds",
            body = sheet.bonds,
        )
        InfoCard(
            title = "Flaws",
            body = sheet.flaws,
        )
        InfoCard(
            title = "Notes",
            body = sheet.notes,
        )
    }
}

@Composable
private fun AbilityGrid(abilities: List<AbilityDisplay>) {
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
                        text = ability.score.toString(),
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
private fun InfoCard(
    title: String,
    body: String,
    placeholder: String = "No information yet",
) {
    Surface(
        shape = MaterialTheme.shapes.large,
        tonalElevation = 1.dp,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
            )
            Text(
                text = body.ifBlank { placeholder },
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 8.dp),
            )
        }
    }
}

@Composable
private fun CharacterSheetError(
    message: String,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(
            text = "Something went wrong",
            style = MaterialTheme.typography.titleLarge,
        )
        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

private enum class CharacterSheetTab(val label: String) {
    Sheet("Sheet"),
    Spells("Spells"),
    Notes("Notes"),
}

private data class AbilityDisplay(
    val label: String,
    val score: Int,
    val modifier: Int,
)

private fun CharacterSheet.toAbilityDisplays(): List<AbilityDisplay> = listOf(
    AbilityDisplay("STR", abilityScores.strength, abilityScores.modifierFor(Ability.STR)),
    AbilityDisplay("DEX", abilityScores.dexterity, abilityScores.modifierFor(Ability.DEX)),
    AbilityDisplay("CON", abilityScores.constitution, abilityScores.modifierFor(Ability.CON)),
    AbilityDisplay("INT", abilityScores.intelligence, abilityScores.modifierFor(Ability.INT)),
    AbilityDisplay("WIS", abilityScores.wisdom, abilityScores.modifierFor(Ability.WIS)),
    AbilityDisplay("CHA", abilityScores.charisma, abilityScores.modifierFor(Ability.CHA)),
)

private fun hitPointsLabel(sheet: CharacterSheet): String {
    val base = "${sheet.currentHitPoints} / ${sheet.maxHitPoints}"
    return if (sheet.temporaryHitPoints > 0) {
        "$base (+${sheet.temporaryHitPoints})"
    } else {
        base
    }
}

private fun heroSubtitle(sheet: CharacterSheet): String {
    val levelPart = "Level ${sheet.level.coerceAtLeast(1)}"
    val classPart = sheet.className.takeIf { it.isNotBlank() }?.let { "$levelPart ${it.trim()}" } ?: levelPart
    return listOfNotBlank(
        classPart,
        sheet.race.takeIf { it.isNotBlank() }?.trim(),
        sheet.background.takeIf { it.isNotBlank() }?.trim(),
    ).joinToString(separator = " • ")
}

private fun titleFor(sheet: CharacterSheet): String =
    if (sheet.name.isNotBlank()) "${sheet.name} – Level ${sheet.level}" else "Level ${sheet.level} hero"

private fun modifierText(value: Int): String = when {
    value > 0 -> "+$value"
    else -> value.toString()
}

private fun listOfNotBlank(vararg items: String?): List<String> =
    items.mapNotNull { value ->
        value?.takeIf { it.isNotBlank() }?.trim()
    }

@Preview
@Composable
private fun CharacterSheetPreview() {
    AppTheme {
        CharacterSheetScreen(
            state = CharacterSheetUiState(
                character = CharacterSheet(
                    id = "preview",
                    name = "Astra Moonshadow",
                    level = 7,
                    className = "Wizard",
                    race = "Half-elf",
                    background = "Luna Conservatory",
                    alignment = "Chaotic Good",
                    abilityScores = AbilityScores(10, 14, 12, 18, 13, 11),
                    proficiencyBonus = 3,
                    inspiration = true,
                    maxHitPoints = 38,
                    currentHitPoints = 34,
                    temporaryHitPoints = 4,
                    armorClass = 16,
                    initiative = 2,
                    speed = "30 ft",
                    hitDice = "7d6",
                    senses = "Darkvision 60 ft",
                    languages = "Common, Elvish, Celestial",
                    proficiencies = "Arcana, History, Insight",
                    attacksAndCantrips = "Fire Bolt, Ray of Frost, Shocking Grasp",
                    featuresAndTraits = "Arcane Recovery, Sculpt Spells",
                    equipment = "Quarterstaff, Spellbook, Scholar's pack",
                    personalityTraits = "Inquisitive, quietly confident.",
                    ideals = "Knowledge is the true power.",
                    bonds = "The Conservatory that raised me.",
                    flaws = "Trusts star omens a bit too much.",
                    notes = "Keep an eye on spell slots.",
                ),
            ),
            onBack = {},
            onEditCharacter = {},
        )
    }
}
