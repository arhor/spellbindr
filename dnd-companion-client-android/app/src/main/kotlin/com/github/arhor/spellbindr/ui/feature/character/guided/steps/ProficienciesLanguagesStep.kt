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
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.github.arhor.spellbindr.ui.feature.character.guided.components.ChoiceSection
import com.github.arhor.spellbindr.ui.feature.character.guided.internal.GuidedChoiceCategory
import com.github.arhor.spellbindr.ui.feature.character.guided.internal.GuidedChoiceRequirement
import com.github.arhor.spellbindr.ui.feature.character.guided.internal.GuidedChoiceSource
import com.github.arhor.spellbindr.ui.feature.character.guided.internal.GuidedFixedGrant

/**
 * Presents all active proficiency and language grants without deriving rules from screen state.
 *
 * Requirements retain their canonical keys and limits; this is important because two sources that offer
 * similar choices must not be collapsed into a single selection pool.
 */
@Composable
internal fun ProficienciesLanguagesStep(
    fixedGrants: List<GuidedFixedGrant>,
    requirements: List<GuidedChoiceRequirement>,
    onChoiceToggled: (key: String, optionId: String, maxSelected: Int) -> Unit,
    listState: LazyListState,
) {
    val relevantGrants = fixedGrants.filter {
        (it.category == GuidedChoiceCategory.PROFICIENCY || it.category == GuidedChoiceCategory.LANGUAGE) &&
            !it.optionId.startsWith("saving-throw-")
    }
    val relevantRequirements = requirements.filter {
        it.category == GuidedChoiceCategory.PROFICIENCY || it.category == GuidedChoiceCategory.LANGUAGE
    }
    val completeCount = relevantRequirements.count { it.isComplete }

    LazyColumn(
        state = listState,
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = "Proficiencies & languages",
                    style = MaterialTheme.typography.titleMedium,
                )
                Text(
                    text = "Review what your character already knows, then complete each required choice.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }

        item {
            AlreadyGrantedCard(grants = relevantGrants)
        }

        if (relevantRequirements.isEmpty()) {
            item {
                Text(
                    text = "All grants are automatic. No choices required.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        } else {
            choiceSourceOrder.forEach { sourceGroup ->
                val sourceRequirements = relevantRequirements.filter { it.source.toUiGroup() == sourceGroup }
                if (sourceRequirements.isNotEmpty()) {
                    item(key = "source/${sourceGroup.name}") {
                        Text(
                            text = sourceGroup.title,
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.primary,
                        )
                    }
                    sourceRequirements.forEach { requirement ->
                        requirementItem(
                            requirement = requirement,
                            onChoiceToggled = onChoiceToggled,
                        )
                    }
                }
            }

            item {
                CompletionSummary(
                    completeCount = completeCount,
                    totalCount = relevantRequirements.size,
                )
            }
        }
    }
}

@Composable
private fun AlreadyGrantedCard(
    grants: List<GuidedFixedGrant>,
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Text(
                text = "Already granted",
                style = MaterialTheme.typography.titleSmall,
            )

            val groupedGrants = grants
                .groupBy(GuidedFixedGrant::grantDisplayGroup)
                .toSortedMap(compareBy(FixedGrantDisplayGroup::sortOrder))

            if (groupedGrants.isEmpty()) {
                Text(
                    text = "No fixed proficiencies or languages.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            } else {
                groupedGrants.forEach { (group, groupGrants) ->
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(
                            text = group.title,
                            style = MaterialTheme.typography.labelLarge,
                        )
                        groupGrants
                            .groupBy { it.optionId }
                            .values
                            .sortedBy { grantsWithSameId -> grantsWithSameId.first().displayName }
                            .forEach { grantsWithSameId ->
                                val grant = grantsWithSameId.first()
                                val sources = grantsWithSameId
                                    .map(GuidedFixedGrant::sourceLabel)
                                    .distinct()
                                    .joinToString()
                                Text(
                                    text = grant.displayName,
                                    style = MaterialTheme.typography.bodyMedium,
                                )
                                Text(
                                    text = "Granted by $sources",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                    }
                }
            }
        }
    }
}

private fun LazyListScope.requirementItem(
    requirement: GuidedChoiceRequirement,
    onChoiceToggled: (key: String, optionId: String, maxSelected: Int) -> Unit,
) {
    item(key = requirement.key) {
        val remaining = (requirement.choice.choose - requirement.selectedOptionIds.size).coerceAtLeast(0)
        val completed = remaining == 0
        val status = if (completed) {
            "Complete"
        } else {
            "$remaining selection${if (remaining == 1) "" else "s"} remaining"
        }
        val instruction = (requirement.choice as? com.github.arhor.spellbindr.domain.model.Choice.OptionsArrayChoice)
            ?.desc
            ?.takeIf(String::isNotBlank)
            ?: "Choose ${requirement.choice.choose}."

        ChoiceSection(
            title = if (completed) "✓ ${requirement.sourceLabel}" else requirement.sourceLabel,
            description = "$instruction $status.",
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

@Composable
private fun CompletionSummary(
    completeCount: Int,
    totalCount: Int,
) {
    val allComplete = completeCount == totalCount
    Card(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = if (allComplete) {
                "All $totalCount choices complete"
            } else {
                "$completeCount of $totalCount choices complete"
            },
            modifier = Modifier.padding(12.dp),
            style = MaterialTheme.typography.labelLarge,
            color = if (allComplete) {
                MaterialTheme.colorScheme.primary
            } else {
                MaterialTheme.colorScheme.onSurfaceVariant
            },
        )
    }
}

private val choiceSourceOrder = listOf(
    ChoiceSourceGroup.CLASS,
    ChoiceSourceGroup.RACE,
    ChoiceSourceGroup.BACKGROUND,
)

private enum class ChoiceSourceGroup(
    val title: String,
) {
    CLASS("Class"),
    RACE("Race & subrace"),
    BACKGROUND("Background"),
}

private fun GuidedChoiceSource.toUiGroup(): ChoiceSourceGroup = when (this) {
    GuidedChoiceSource.CLASS -> ChoiceSourceGroup.CLASS
    GuidedChoiceSource.RACE_TRAIT,
    GuidedChoiceSource.SUBRACE_TRAIT,
    -> ChoiceSourceGroup.RACE

    GuidedChoiceSource.BACKGROUND -> ChoiceSourceGroup.BACKGROUND
}

private enum class FixedGrantDisplayGroup(
    val title: String,
    val sortOrder: Int,
) {
    SKILLS("Skills", 0),
    TOOLS("Tools", 1),
    ARMOR_WEAPONS("Armor & weapons", 2),
    LANGUAGES("Languages", 3),
}

private fun GuidedFixedGrant.grantDisplayGroup(): FixedGrantDisplayGroup = when {
    category == GuidedChoiceCategory.LANGUAGE -> FixedGrantDisplayGroup.LANGUAGES
    optionId.startsWith("skill-") -> FixedGrantDisplayGroup.SKILLS
    optionId.isToolProficiencyId() -> FixedGrantDisplayGroup.TOOLS
    else -> FixedGrantDisplayGroup.ARMOR_WEAPONS
}

private fun String.isToolProficiencyId(): Boolean =
    toolIdMarkers.any { marker -> marker in lowercase() }

private val toolIdMarkers = listOf(
    "tool",
    "kit",
    "supplies",
    "instrument",
    "vehicle",
    "gaming-set",
)

private val GuidedChoiceRequirement.isComplete: Boolean
    get() = selectedOptionIds.size >= choice.choose
