package com.github.arhor.spellbindr.ui.feature.compendium.spells

import androidx.compose.runtime.Immutable
import com.github.arhor.spellbindr.domain.model.Spell

sealed interface SpellsUiState {
    data object Loading : SpellsUiState

    @Immutable
    data class Loaded(
        val spells: List<Spell>,
        val spellsByLevel: Map<Int, List<Spell>>,
    ) : SpellsUiState

    @Immutable
    data class Error(val message: String) : SpellsUiState
}
