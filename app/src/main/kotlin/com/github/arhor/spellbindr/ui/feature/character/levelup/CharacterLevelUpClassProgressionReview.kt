package com.github.arhor.spellbindr.ui.feature.character.levelup

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.github.arhor.spellbindr.domain.model.LevelUpRequirement
import com.github.arhor.spellbindr.domain.model.SpellcastingClassStats
import com.github.arhor.spellbindr.domain.model.calculateSpellcastingClassStats

@Composable
internal fun CharacterLevelUpClassProgressionReview(state: CharacterLevelUpUiState.Content) {
    val before = state.preview.before
    val after = state.preview.after
    val selectedClassId = state.plan.selectedClassId
    val selectedClassName = selectedClassId?.let { classId ->
        state.classes.firstOrNull { it.id == classId }?.name ?: classId
    }
    val beforeSpellcasting = before.calculateSpellcastingClassStats(state.classes)
    val afterSpellcasting = after.calculateSpellcastingClassStats(state.classes)

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        tonalElevation = 1.dp,
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text("Class progression", style = MaterialTheme.typography.titleSmall)
            ClassProgressionReviewRow(
                label = "Total level",
                before = before.totalLevel.toString(),
                after = after.totalLevel.toString(),
            )
            if (selectedClassId != null && selectedClassName != null) {
                ClassProgressionReviewRow(
                    label = "$selectedClassName level",
                    before = before.classLevels[selectedClassId].orZero().toString(),
                    after = after.classLevels[selectedClassId].orZero().toString(),
                )
            }
            ClassProgressionReviewRow(
                label = "Class levels",
                before = before.classDisplayName,
                after = after.classDisplayName,
            )
            selectedSubclassName(state, selectedClassId)?.let { subclassName ->
                Text("New subclass: $subclassName", style = MaterialTheme.typography.bodyMedium)
            }
            val spellcastingClassIds = (beforeSpellcasting.keys + afterSpellcasting.keys).toSortedSet()
            if (spellcastingClassIds.isNotEmpty()) {
                Text("Spellcasting", style = MaterialTheme.typography.titleSmall)
                spellcastingClassIds.forEach { classId ->
                    val className = state.classes.firstOrNull { it.id == classId }?.name ?: classId
                    val beforeStats = beforeSpellcasting[classId]
                    val afterStats = afterSpellcasting[classId]
                    val ability = (afterStats ?: beforeStats)?.abilityId?.uppercase()
                    val labelPrefix = ability?.let { "$className ($it)" } ?: className
                    ClassProgressionReviewRow(
                        label = "$labelPrefix spell save DC",
                        before = beforeStats?.spellSaveDc?.toString().orMissing(),
                        after = afterStats?.spellSaveDc?.toString().orMissing(),
                    )
                    ClassProgressionReviewRow(
                        label = "$labelPrefix spell attack",
                        before = beforeStats.formatAttackBonus(),
                        after = afterStats.formatAttackBonus(),
                    )
                }
            }
            MagicInitiateReview(state)
        }
    }
}

@Composable
private fun MagicInitiateReview(state: CharacterLevelUpUiState.Content) {
    val requirements = state.preview.requirements
        .filterIsInstance<LevelUpRequirement.ChoiceSelection>()
        .filter { it.sourceId == "magic-initiate" }
    if (requirements.isEmpty()) return

    fun selectedLabels(id: String): List<String> {
        val requirement = requirements.firstOrNull { it.id == id } ?: return emptyList()
        return requirement.options.filter { it.id in requirement.selectedOptionIds }.map { it.label }
    }

    Text("Magic Initiate", style = MaterialTheme.typography.titleSmall)
    selectedLabels("magic-initiate:class-list").singleOrNull()?.let { Text("Spell list: $it") }
    selectedLabels("magic-initiate:cantrips").takeIf { it.isNotEmpty() }
        ?.let { Text("Cantrips: ${it.joinToString()}") }
    selectedLabels("magic-initiate:first-level-spell").singleOrNull()?.let { Text("1st-level spell: $it") }
}

@Composable
private fun ClassProgressionReviewRow(label: String, before: String, after: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(label)
        Text("$before → $after")
    }
}

private fun selectedSubclassName(
    state: CharacterLevelUpUiState.Content,
    selectedClassId: String?,
): String? {
    val requirement = state.preview.requirements
        .filterIsInstance<LevelUpRequirement.SubclassSelection>()
        .firstOrNull { selectedClassId == null || it.classId == selectedClassId }
        ?: return null
    val selectedSubclassId = requirement.selectedSubclassId ?: return null
    return requirement.options.firstOrNull { it.id == selectedSubclassId }?.label ?: selectedSubclassId
}

private fun SpellcastingClassStats?.formatAttackBonus(): String = this?.spellAttackBonus?.let { bonus ->
    if (bonus >= 0) "+$bonus" else bonus.toString()
} ?: "N/A"

private fun String?.orMissing(): String = this ?: "N/A"

private fun Int?.orZero(): Int = this ?: 0
