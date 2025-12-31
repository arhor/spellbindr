package com.github.arhor.spellbindr.ui.feature.compendium.alignments

import androidx.compose.runtime.Immutable
import com.github.arhor.spellbindr.domain.model.Alignment

sealed interface AlignmentsUiState {
    @Immutable
    data object Loading : AlignmentsUiState

    @Immutable
    data class Content(
        val alignments: List<Alignment> = emptyList(),
        val expandedItemName: String? = null,
    ) : AlignmentsUiState

    @Immutable
    data class Error(
        val message: String,
    ) : AlignmentsUiState
}
