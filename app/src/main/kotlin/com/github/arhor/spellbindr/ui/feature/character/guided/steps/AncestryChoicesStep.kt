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
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.github.arhor.spellbindr.domain.model.Choice
import com.github.arhor.spellbindr.ui.feature.character.guided.components.ChoiceSection
import com.github.arhor.spellbindr.ui.feature.character.guided.internal.GuidedChoiceCategory
import com.github.arhor.spellbindr.ui.feature.character.guided.internal.GuidedChoiceRequirement

/**
 * Renders race-trait decisions that do not belong to the race identity, proficiency, or equipment steps.
 *
 * Trait descriptions and resolved display options come from the canonical requirement model, so this component
 * remains independent of the full reference-data state.
 */
@Composable
internal fun AncestryChoicesStep(
    requirements: List<GuidedChoiceRequirement>,
    onChoiceToggled: (key: String, optionId: String, maxSelected: Int) -> Unit,
    listState: LazyListState,
) {
    val ancestryRequirements = requirements.filter { it.category == GuidedChoiceCategory.ANCESTRY }
    val completeCount = ancestryRequirements.count { it.selectedOptionIds.size >= it.choice.choose }

    LazyColumn(
        state = listState,
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = "Ancestry choices",
                    style = MaterialTheme.typography.titleMedium,
                )
                Text(
                    text = "Complete the special choices granted by your race and subrace.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }

        if (ancestryRequirements.isEmpty()) {
            item {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "This ancestry has no additional choices.",
                        modifier = Modifier.padding(12.dp),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        } else {
            ancestryRequirements.forEach { requirement ->
                item(key = requirement.key) {
                    val remaining =
                        (requirement.choice.choose - requirement.selectedOptionIds.size).coerceAtLeast(0)
                    val completed = remaining == 0
                    val instruction = requirement.choice.instruction()
                    val description = buildList {
                        requirement.sourceDescription
                            ?.takeIf(String::isNotBlank)
                            ?.let(::add)
                        add(instruction)
                        add(
                            if (completed) {
                                "Complete."
                            } else {
                                "$remaining selection${if (remaining == 1) "" else "s"} remaining."
                            },
                        )
                    }.joinToString(separator = " ")

                    ChoiceSection(
                        title = if (completed) "✓ ${requirement.sourceLabel}" else requirement.sourceLabel,
                        description = description,
                        choice = requirement.choice,
                        selected = requirement.selectedOptionIds,
                        options = requirement.options.associate { it.id to it.displayName },
                        disabledOptions = requirement.disabledOptions,
                        onToggle = { optionId ->
                            onChoiceToggled(requirement.key, optionId, requirement.choice.choose)
                        },
                    )
                }
            }

            item {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = if (completeCount == ancestryRequirements.size) {
                            "All ${ancestryRequirements.size} ancestry choices complete"
                        } else {
                            "$completeCount of ${ancestryRequirements.size} ancestry choices complete"
                        },
                        modifier = Modifier.padding(12.dp),
                        style = MaterialTheme.typography.labelLarge,
                        color = if (completeCount == ancestryRequirements.size) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        },
                    )
                }
            }
        }
    }
}

private fun Choice.instruction(): String = when (this) {
    is Choice.OptionsArrayChoice -> desc?.takeIf(String::isNotBlank) ?: "Choose $choose."
    is Choice.AbilityBonusChoice -> "Choose $choose ability score bonus."
    is Choice.ResourceListChoice ->
        if (from.equals("spells", ignoreCase = true)) {
            "Choose $choose racial spell or cantrip."
        } else {
            "Choose $choose option."
        }

    else -> "Choose $choose option${if (choose == 1) "" else "s"}."
}
