package com.github.arhor.spellbindr.ui.feature.compendium.spells.search

import com.github.arhor.spellbindr.domain.model.EntityRef
import com.github.arhor.spellbindr.domain.model.Spell
import com.github.arhor.spellbindr.ui.feature.compendium.CompendiumViewModel

interface SpellListState {
    val query: String
    val showFavorite: Boolean
    val showFilterDialog: Boolean
    val castingClasses: List<EntityRef>
    val currentClasses: Set<EntityRef>
    val uiState: CompendiumViewModel.SpellsUiState
    val spellsByLevel: Map<Int, List<Spell>>
    val expandedSpellLevels: Map<Int, Boolean>
    val expandedAll: Boolean
}
