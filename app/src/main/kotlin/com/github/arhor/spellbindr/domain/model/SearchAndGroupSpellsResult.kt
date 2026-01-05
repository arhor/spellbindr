package com.github.arhor.spellbindr.domain.model

data class SearchAndGroupSpellsResult(
    val spells: List<Spell>,
    val spellsByLevel: Map<Int, List<Spell>>,
    val totalCount: Int,
    val query: String,
    val classes: List<EntityRef>,
    val favoriteOnly: Boolean,
)
