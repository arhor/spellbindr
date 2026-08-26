package io.github.arhor.dnd.companion.ui.feature.compendium.alignments

import androidx.compose.runtime.Immutable
import io.github.arhor.dnd.companion.domain.model.Alignment

sealed interface AlignmentsUiState {
    @Immutable
    data object Loading : AlignmentsUiState

    @Immutable
    data class Content(
        val alignments: List<Alignment> = emptyList(),
        val selectedItemId: String? = null,
    ) : AlignmentsUiState

    @Immutable
    data class Failure(
        val errorMessage: String,
    ) : AlignmentsUiState
}
