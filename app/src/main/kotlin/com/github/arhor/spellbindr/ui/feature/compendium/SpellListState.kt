package com.github.arhor.spellbindr.ui.feature.compendium

import com.github.arhor.spellbindr.domain.model.EntityRef
interface SpellListState {
    val query: String
    val showFavorite: Boolean
    val showFilterDialog: Boolean
    val castingClasses: List<EntityRef>
    val currentClasses: Set<EntityRef>
    val uiState: CompendiumViewModel.SpellsUiState
}
