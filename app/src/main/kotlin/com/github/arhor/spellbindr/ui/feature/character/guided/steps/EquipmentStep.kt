@file:OptIn(
    androidx.compose.foundation.layout.ExperimentalLayoutApi::class,
    androidx.compose.material3.ExperimentalMaterial3Api::class,
)

package com.github.arhor.spellbindr.ui.feature.character.guided

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.github.arhor.spellbindr.domain.model.EntityRef
import com.github.arhor.spellbindr.ui.feature.character.guided.components.ChoiceSection
import com.github.arhor.spellbindr.ui.feature.character.guided.internal.GuidedChoiceCategory

@Composable
internal fun EquipmentStep(
    state: GuidedCharacterSetupUiState.Content,
    onChoiceToggled: (key: String, optionId: String, maxSelected: Int) -> Unit,
    listState: LazyListState,
) {
    val clazz = state.selection.classId?.let { id -> state.classes.firstOrNull { it.id == id } }
    val bg = state.selection.backgroundId?.let { id -> state.backgrounds.firstOrNull { it.id == id } }
    val fixed = remember(clazz?.startingEquipment, bg?.effects, state.referenceDataVersion) {
        buildList<String> {
            clazz?.startingEquipment?.forEach { ref ->
                val name = state.equipmentById[ref.id]?.name ?: EntityRef(ref.id).prettyString()
                add(if (ref.quantity <= 1) name else "$name x${ref.quantity}")
            }
            bg?.effects?.forEach { effect ->
                if (effect is com.github.arhor.spellbindr.domain.model.Effect.AddEquipmentEffect) {
                    effect.equipment.forEach { counted ->
                        val name = state.equipmentById[counted.id]?.name ?: EntityRef(counted.id).prettyString()
                        add(if (counted.quantity <= 1) name else "$name x${counted.quantity}")
                    }
                }
            }
        }
    }

    LazyColumn(
        state = listState,
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item { Text(text = "Starting equipment", style = MaterialTheme.typography.titleMedium) }
        item {
            Text(
                text = "Review included gear, then complete each required background equipment choice.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }

        if (fixed.isNotEmpty()) {
            item {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(text = "Included", style = MaterialTheme.typography.titleSmall)
                        fixed.forEach { Text(text = "• $it", style = MaterialTheme.typography.bodyMedium) }
                    }
                }
            }
        }

        state.choiceRequirements
            .filter { it.category == GuidedChoiceCategory.EQUIPMENT }
            .forEachIndexed { index, requirement ->
                item(key = requirement.key) {
                    val selected = state.selection.choiceSelections[requirement.key].orEmpty()
                    val options = requirement.options.associate { it.id to it.displayName }
                    ChoiceSection(
                        title = if (state.choiceRequirements.count {
                            it.category == GuidedChoiceCategory.EQUIPMENT
                        } > 1
                        ) {
                            "Background equipment ${index + 1}"
                        } else {
                            "Background equipment"
                        },
                        description = "Choose ${requirement.choice.choose} · ${requirement.sourceLabel}",
                        choice = requirement.choice,
                        selected = selected,
                        options = options,
                        onToggle = { optionId ->
                            onChoiceToggled(
                                requirement.key,
                                optionId,
                                requirement.choice.choose,
                            )
                        },
                    )
                }
            }
    }
}
