package com.github.arhor.spellbindr.ui.feature.compendium.conditions

import androidx.compose.runtime.Immutable
import com.github.arhor.spellbindr.domain.model.Condition

sealed interface ConditionsUiState {
    @Immutable
    data object Loading : ConditionsUiState

    @Immutable
    data class Content(
        val conditions: List<Condition>,
        val selectedItemId: String? = null,
    ) : ConditionsUiState

    @Immutable
    data class Error(
        val message: String,
    ) : ConditionsUiState
}
