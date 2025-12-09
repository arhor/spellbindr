package com.github.arhor.spellbindr.ui.feature.compendium

import com.github.arhor.spellbindr.data.model.EntityRef
import com.github.arhor.spellbindr.data.model.Spell

interface SpellListState {
    val query: String
    val spells: List<Spell>
    val showFavorite: Boolean
    val showFilterDialog: Boolean
    val castingClasses: List<EntityRef>
    val currentClasses: Set<EntityRef>
    val isLoading: Boolean
    val error: String?
}
