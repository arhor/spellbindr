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

@Composable
internal fun CharacterLevelUpClassProgressionReview(state: CharacterLevelUpUiState.Content) {
    val before = state.preview.before
    val after = state.preview.after
    val selectedClassId = state.plan.selectedClassId
    val selectedClassName = selectedClassId?.let { classId ->
        state.classes.firstOrNull { it.id == classId }?.name ?: classId
    }

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
        }
    }
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

private fun Int?.orZero(): Int = this ?: 0
