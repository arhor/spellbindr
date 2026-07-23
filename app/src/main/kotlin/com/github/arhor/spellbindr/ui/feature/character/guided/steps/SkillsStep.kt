@file:OptIn(
    androidx.compose.foundation.layout.ExperimentalLayoutApi::class,
    androidx.compose.material3.ExperimentalMaterial3Api::class,
)

package com.github.arhor.spellbindr.ui.feature.character.guided

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.github.arhor.spellbindr.domain.model.Choice
import com.github.arhor.spellbindr.ui.feature.character.guided.components.ChoiceSection

@Composable
internal fun SkillsStep(
    state: GuidedCharacterSetupUiState.Content,
    onChoiceToggled: (key: String, optionId: String, maxSelected: Int) -> Unit,
    listState: LazyListState,
) {
    val clazz = state.selection.classId?.let { id -> state.classes.firstOrNull { it.id == id } }
    LazyColumn(
        state = listState,
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item { Text(text = "Skills & proficiencies", style = MaterialTheme.typography.titleMedium) }

        if (clazz == null) {
            item { Text("Choose a class first.") }
            return@LazyColumn
        }

        if (clazz.proficiencyChoices.isEmpty()) {
            item {
                Text(
                    text = "No additional choices for this class.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            return@LazyColumn
        }

        clazz.proficiencyChoices.forEachIndexed { index, choice ->
            item(key = "class/proficiency/$index") {
                val choiceKey = GuidedCharacterSetupViewModel.classProficiencyChoiceKey(index)
                val selected = state.selection.choiceSelections[choiceKey].orEmpty()
                val options = remember(choiceKey, choice, state.referenceDataVersion) { resolveOptions(choice, state) }
                ChoiceSection(
                    title = "Class choice ${index + 1}",
                    description = (choice as? Choice.OptionsArrayChoice)?.desc
                        ?: "Choose ${choice.choose}",
                    choice = choice,
                    selected = selected,
                    options = options,
                    disabledOptions = computeAlreadySelectedProficiencyReasons(state, choiceKey),
                    onToggle = { optionId -> onChoiceToggled(choiceKey, optionId, choice.choose) },
                )
            }
        }
    }
}

