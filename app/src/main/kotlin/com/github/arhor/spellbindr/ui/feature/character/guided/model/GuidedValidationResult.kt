package com.github.arhor.spellbindr.ui.feature.character.guided.model

import androidx.compose.runtime.Immutable

@Immutable
data class GuidedValidationResult(
    val issues: List<GuidedValidationIssue>,
) {
    val hasErrors: Boolean = issues.any { it.severity == GuidedValidationIssue.Severity.ERROR }
}
