@file:OptIn(
    androidx.compose.foundation.layout.ExperimentalLayoutApi::class,
    androidx.compose.material3.ExperimentalMaterial3Api::class,
)

package com.github.arhor.spellbindr.ui.feature.character.guided

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.github.arhor.spellbindr.domain.model.Choice
import com.github.arhor.spellbindr.domain.model.displayName
import com.github.arhor.spellbindr.ui.feature.character.guided.components.ChoiceSection
import com.github.arhor.spellbindr.ui.feature.character.guided.components.SelectRow

@Composable
internal fun RaceStep(
    state: GuidedCharacterSetupUiState.Content,
    onRaceSelected: (String) -> Unit,
    onSubraceSelected: (String) -> Unit,
    onChoiceToggled: (key: String, optionId: String, maxSelected: Int) -> Unit,
    listState: LazyListState,
) {
    var query by remember { mutableStateOf("") }
    val races = remember(state.races, query) {
        val trimmed = query.trim()
        if (trimmed.isBlank()) {
            state.races
        } else {
            state.races.filter { it.name.contains(trimmed, ignoreCase = true) }
        }
    }

    val selectedRace = state.selection.raceId?.let { id -> state.races.firstOrNull { it.id == id } }

    val traitIds = selectedRace?.let { race ->
        buildList {
            addAll(race.traits.map { it.id })
            val subrace = state.selection.subraceId?.let { sid -> race.subraces.firstOrNull { it.id == sid } }
            if (subrace != null) addAll(subrace.traits.map { it.id })
        }
    }.orEmpty()
    val traits = traitIds.mapNotNull { state.traitsById[it] }

    LazyColumn(
        state = listState,
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item {
            Text(
                text = "Choose a race",
                style = MaterialTheme.typography.titleMedium,
            )
        }
        if (state.races.size >= 8) {
            item {
                OutlinedTextField(
                    value = query,
                    onValueChange = { query = it },
                    label = { Text("Search races") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                )
            }
        }

        items(races, key = { it.id }) { race ->
            val selected = state.selection.raceId == race.id
            Card(
                onClick = { onRaceSelected(race.id) },
                modifier = Modifier.fillMaxWidth(),
            ) {
                Column(
                    modifier = Modifier.padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        RadioButton(selected = selected, onClick = null)
                        Text(text = race.name, style = MaterialTheme.typography.bodyLarge)
                    }
                    if (selected) {
                        Text(
                            text = "Traits: ${race.traits.size} • Subraces: ${race.subraces.size}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }
        }

        if (state.races.isNotEmpty() && races.isEmpty()) {
            item {
                Text(
                    text = "No matches.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }

        if (selectedRace == null) return@LazyColumn

        if (selectedRace.subraces.isNotEmpty()) {
            item { Text(text = "Subrace", style = MaterialTheme.typography.titleSmall) }
            items(selectedRace.subraces, key = { it.id }) { subrace ->
                SelectRow(
                    title = subrace.name,
                    selected = state.selection.subraceId == subrace.id,
                    onClick = { onSubraceSelected(subrace.id) },
                )
            }
        }

        if (traits.isNotEmpty()) {
            item { Text(text = "Traits", style = MaterialTheme.typography.titleSmall) }
        }

        items(traits, key = { it.id }) { trait ->
            var expanded by remember(trait.id) { mutableStateOf(false) }
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier.padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Text(text = trait.name, style = MaterialTheme.typography.titleSmall)
                    val visibleDesc = if (expanded) trait.desc else trait.desc.take(1)
                    visibleDesc.forEach { paragraph ->
                        Text(
                            text = paragraph,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    if (trait.desc.size > 1) {
                        OutlinedButton(onClick = { expanded = !expanded }) {
                            Text(if (expanded) "Hide details" else "Show details")
                        }
                    }

                    (trait.abilityBonusChoice as? Choice.AbilityBonusChoice)?.let { choice ->
                        val choiceKey = GuidedCharacterSetupViewModel.raceTraitAbilityBonusChoiceKey(trait.id)
                        val selected = state.selection.choiceSelections[choiceKey].orEmpty()
                        val options = remember(trait.id, state.referenceDataVersion) {
                            choice.from.flatMap { it.keys }.distinct().associateWith { id -> "${id.displayName()} +1" }
                        }
                        ChoiceSection(
                            title = "Ability score increase",
                            description = "Choose ${choice.choose}",
                            choice = choice,
                            selected = selected,
                            options = options,
                            onToggle = { optionId ->
                                onChoiceToggled(
                                    choiceKey,
                                    optionId,
                                    choice.choose
                                )
                            },
                        )
                    }

                    trait.languageChoice?.let { choice ->
                        val choiceKey = GuidedCharacterSetupViewModel.raceTraitLanguageChoiceKey(trait.id)
                        val selected = state.selection.choiceSelections[choiceKey].orEmpty()
                        val options =
                            remember(trait.id, choice, state.referenceDataVersion) { resolveOptions(choice, state) }
                        ChoiceSection(
                            title = "Languages",
                            description = "Choose ${choice.choose}",
                            choice = choice,
                            selected = selected,
                            options = options,
                            disabledOptions = computeAlreadySelectedLanguageReasons(
                                state,
                                choiceKey
                            ),
                            onToggle = { optionId ->
                                onChoiceToggled(
                                    choiceKey,
                                    optionId,
                                    choice.choose
                                )
                            },
                        )
                    }

                    trait.proficiencyChoice?.let { choice ->
                        val choiceKey = GuidedCharacterSetupViewModel.raceTraitProficiencyChoiceKey(trait.id)
                        val selected = state.selection.choiceSelections[choiceKey].orEmpty()
                        val options =
                            remember(trait.id, choice, state.referenceDataVersion) { resolveOptions(choice, state) }
                        ChoiceSection(
                            title = "Proficiencies",
                            description = "Choose ${choice.choose}",
                            choice = choice,
                            selected = selected,
                            options = options,
                            disabledOptions = computeAlreadySelectedProficiencyReasons(
                                state,
                                choiceKey
                            ),
                            onToggle = { optionId ->
                                onChoiceToggled(
                                    choiceKey,
                                    optionId,
                                    choice.choose
                                )
                            },
                        )
                    }

                    trait.draconicAncestryChoice?.let { choice ->
                        val choiceKey = GuidedCharacterSetupViewModel.raceTraitDraconicAncestryChoiceKey(trait.id)
                        val selected = state.selection.choiceSelections[choiceKey].orEmpty()
                        val options =
                            remember(trait.id, choice, state.referenceDataVersion) { resolveOptions(choice, state) }
                        ChoiceSection(
                            title = "Draconic ancestry",
                            description = "Choose ${choice.choose}",
                            choice = choice,
                            selected = selected,
                            options = options,
                            onToggle = { optionId ->
                                onChoiceToggled(
                                    choiceKey,
                                    optionId,
                                    choice.choose
                                )
                            },
                        )
                    }

                    trait.spellChoice?.let { choice ->
                        val choiceKey = GuidedCharacterSetupViewModel.raceTraitSpellChoiceKey(trait.id)
                        val selected = state.selection.choiceSelections[choiceKey].orEmpty()
                        val options = remember(trait.id, choice, state.spells) { resolveOptions(choice, state) }
                        ChoiceSection(
                            title = "Spell",
                            description = "Choose ${choice.choose}",
                            choice = choice,
                            selected = selected,
                            options = options,
                            onToggle = { optionId ->
                                onChoiceToggled(
                                    choiceKey,
                                    optionId,
                                    choice.choose
                                )
                            },
                        )
                    }
                }
            }
        }
    }
}

