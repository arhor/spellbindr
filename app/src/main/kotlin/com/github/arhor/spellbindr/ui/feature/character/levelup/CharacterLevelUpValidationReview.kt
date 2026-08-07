package com.github.arhor.spellbindr.ui.feature.character.levelup

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.toggleable
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import com.github.arhor.spellbindr.domain.model.LevelUpValidationCode
import com.github.arhor.spellbindr.domain.model.LevelUpValidationIssue
import com.github.arhor.spellbindr.domain.model.LevelUpValidationSeverity

@Composable
internal fun CharacterLevelUpValidationReview(
    state: CharacterLevelUpUiState.Content,
    dispatch: CharacterLevelUpDispatch,
) {
    val findings = state.preview.validations.filter { issue ->
        issue.severity == LevelUpValidationSeverity.Informational ||
            issue.severity == LevelUpValidationSeverity.Overrideable
    }
    if (findings.isEmpty()) return

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        tonalElevation = 1.dp,
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text("Review findings", style = MaterialTheme.typography.titleSmall)
            findings.forEach { issue ->
                when (issue.severity) {
                    LevelUpValidationSeverity.Informational -> InformationalFinding(issue)
                    LevelUpValidationSeverity.Overrideable -> RuleExceptionFinding(state, issue, dispatch)
                    LevelUpValidationSeverity.Blocking -> Unit
                }
            }
        }
    }
}

@Composable
private fun InformationalFinding(issue: LevelUpValidationIssue) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surfaceVariant,
        shape = MaterialTheme.shapes.small,
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Text("Warning", style = MaterialTheme.typography.labelLarge)
            Text(issue.message)
            Text(
                "No action required.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun RuleExceptionFinding(
    state: CharacterLevelUpUiState.Content,
    issue: LevelUpValidationIssue,
    dispatch: CharacterLevelUpDispatch,
) {
    val accepted = issue.acknowledgementId in state.plan.selections.acknowledgedIssueCodes
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.secondaryContainer,
        shape = MaterialTheme.shapes.small,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .toggleable(
                    value = accepted,
                    enabled = !state.isSaving,
                    role = Role.Checkbox,
                    onValueChange = {
                        dispatch(CharacterLevelUpIntent.AcknowledgementChanged(issue.acknowledgementId, it))
                    },
                )
                .padding(12.dp),
            verticalAlignment = Alignment.Top,
        ) {
            Checkbox(
                checked = accepted,
                enabled = !state.isSaving,
                onCheckedChange = null,
            )
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Text(
                    if (accepted) "Accepted rule exception" else "Rule exception",
                    style = MaterialTheme.typography.labelLarge,
                )
                Text(issue.message)
                Text(
                    issue.reviewContext(state),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                )
            }
        }
    }
}

private fun LevelUpValidationIssue.reviewContext(state: CharacterLevelUpUiState.Content): String = when (code) {
    LevelUpValidationCode.MulticlassPrerequisite -> "Class choice: ${state.selectedClassLabel()}"
    LevelUpValidationCode.ExperienceThreshold -> "Target level: ${state.preview.after.totalLevel}"
    else -> "Level-up choice: ${state.selectedClassLabel()} · level ${state.preview.after.totalLevel}"
}

private fun CharacterLevelUpUiState.Content.selectedClassLabel(): String {
    val selectedClassId = plan.selectedClassId
    return classes.firstOrNull { it.id == selectedClassId }?.name
        ?: selectedClassId
        ?: preview.after.classDisplayName
}
