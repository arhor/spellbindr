package io.github.arhor.dnd.companion.ui.feature.compendium.conditions

import androidx.compose.runtime.Immutable
import io.github.arhor.dnd.companion.domain.model.Condition

sealed interface ConditionsUiState {
    @Immutable
    data object Loading : ConditionsUiState

    @Immutable
    data class Content(
        val conditions: List<Condition>,
        val selectedItemId: String? = null,
    ) : ConditionsUiState

    @Immutable
    data class Failure(
        val errorMessage: String,
    ) : ConditionsUiState
}
