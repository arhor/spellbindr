package io.github.arhor.dnd.companion.ui.feature.compendium.spells

import androidx.compose.runtime.Immutable
import io.github.arhor.dnd.companion.domain.model.EntityRef
import io.github.arhor.dnd.companion.domain.model.Spell

sealed interface SpellsUiState {
    @Immutable
    data object Loading : SpellsUiState

    @Immutable
    data class Content(
        val query: String,
        val spells: List<Spell>,
        val showFavoriteOnly: Boolean,
        val castingClasses: List<EntityRef>,
        val selectedClasses: Set<EntityRef>,
    ) : SpellsUiState

    @Immutable
    data class Failure(
        val errorMessage: String,
    ) : SpellsUiState
}
