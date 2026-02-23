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
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.github.arhor.spellbindr.ui.feature.character.guided.components.ChoiceSection
import com.github.arhor.spellbindr.ui.feature.character.guided.components.SelectRow

@Composable
internal fun ClassChoicesStep(
    state: GuidedCharacterSetupUiState.Content,
    onSubclassSelected: (String) -> Unit,
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
        item {
            Text(
                text = "Level 1 choices",
                style = MaterialTheme.typography.titleMedium,
            )
        }

        if (clazz == null) {
            item { Text("Choose a class first.") }
            return@LazyColumn
        }

        if (clazz.requiresLevelOneSubclass()) {
            item {
                Text(
                    text = "Subclass",
                    style = MaterialTheme.typography.titleSmall,
                )
            }
            items(clazz.subclasses, key = { it.id }) { subclass ->
                SelectRow(
                    title = subclass.name,
                    selected = state.selection.subclassId == subclass.id,
                    onClick = { onSubclassSelected(subclass.id) },
                )
            }
        }

        val level1FeatureIds = clazz.levels.firstOrNull { it.level == 1 }?.features.orEmpty()
        val featureChoices = level1FeatureIds.mapNotNull { featureId ->
            val feature = state.featuresById[featureId] ?: return@mapNotNull null
            val choice = feature.choice ?: return@mapNotNull null
            Triple(featureId, feature, choice)
        }

        featureChoices.forEach { (featureId, feature, choice) ->
            item(key = "feature/$featureId") {
                val selected =
                    state.selection.choiceSelections[GuidedCharacterSetupViewModel.featureChoiceKey(featureId)].orEmpty()
                val options = remember(featureId, choice, state.referenceDataVersion) {
                    resolveOptions(choice, state)
                }
                ChoiceSection(
                    title = feature.name,
                    description = null,
                    choice = choice,
                    selected = selected,
                    options = options,
                    onToggle = { optionId ->
                        onChoiceToggled(
                            GuidedCharacterSetupViewModel.featureChoiceKey(featureId),
                            optionId,
                            choice.choose,
                        )
                    },
                )
            }
        }
    }
}

