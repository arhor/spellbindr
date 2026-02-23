package com.github.arhor.spellbindr.ui.feature.character.guided.model

import androidx.compose.runtime.Immutable

@Immutable
data class GuidedValidationIssue(
    val severity: Severity,
    val message: String,
) {
    enum class Severity { ERROR, WARNING }
}
