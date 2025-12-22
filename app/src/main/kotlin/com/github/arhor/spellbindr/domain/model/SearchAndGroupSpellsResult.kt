package com.github.arhor.spellbindr.domain.model

data class SearchAndGroupSpellsResult(
    val spells: List<Spell>,
    val spellsByLevel: Map<Int, List<Spell>>,
    val totalCount: Int,
    val query: String,
    val classes: Set<EntityRef>,
    val favoriteOnly: Boolean,
)
