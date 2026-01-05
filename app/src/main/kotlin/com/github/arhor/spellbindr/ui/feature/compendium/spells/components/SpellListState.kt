package com.github.arhor.spellbindr.ui.feature.compendium.spells.components

import com.github.arhor.spellbindr.domain.model.EntityRef
import com.github.arhor.spellbindr.domain.model.Spell
import com.github.arhor.spellbindr.ui.feature.compendium.spells.SpellsUiState

interface SpellListState {
    val query: String
    val showFavorite: Boolean
    val showFilterDialog: Boolean
    val castingClasses: List<EntityRef>
    val currentClasses: List<EntityRef>
    val uiState: SpellsUiState
    val spellsByLevel: Map<Int, List<Spell>>
    val expandedSpellLevels: Map<Int, Boolean>
    val expandedAll: Boolean
}
