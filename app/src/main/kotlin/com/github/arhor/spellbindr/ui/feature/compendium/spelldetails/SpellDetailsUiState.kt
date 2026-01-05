package com.github.arhor.spellbindr.ui.feature.compendium.spelldetails

import androidx.compose.runtime.Immutable
import com.github.arhor.spellbindr.domain.model.Spell

sealed interface SpellDetailsUiState {
    @Immutable
    data object Loading : SpellDetailsUiState

    @Immutable
    data class Content(
        val spell: Spell,
        val isFavorite: Boolean,
    ) : SpellDetailsUiState

    @Immutable
    data class Error(
        val errorMessage: String,
    ) : SpellDetailsUiState
}
