package com.github.arhor.spellbindr.ui.feature.character.sheet.model

import androidx.compose.runtime.Immutable

@Immutable
sealed interface ProgressionSummaryUiModel {

    @Immutable
    data object Unmanaged : ProgressionSummaryUiModel {
        const val message: String = "Set up level progression to enable guided level-up."
    }

    @Immutable
    data class Managed(
        val totalLevel: Int,
        val classes: String,
        val levels: List<String>,
    ) : ProgressionSummaryUiModel
}
