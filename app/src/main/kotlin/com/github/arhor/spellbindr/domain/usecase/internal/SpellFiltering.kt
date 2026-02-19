package com.github.arhor.spellbindr.domain.usecase.internal

import com.github.arhor.spellbindr.domain.model.EntityRef
import com.github.arhor.spellbindr.domain.model.Spell

internal fun Spell.matchesFilters(
    query: String,
    classes: Collection<EntityRef>,
    favoriteSpellIds: Set<String>,
): Boolean {
    val normalizedQuery = query.trim()
    val matchesQuery = normalizedQuery.isEmpty() || name.contains(normalizedQuery, ignoreCase = true)
    val matchesClasses = classes.isEmpty() || classes.any { it in this.classes }
    val matchesFavorite = favoriteSpellIds.isEmpty() || id in favoriteSpellIds

    return matchesQuery && matchesClasses && matchesFavorite
}
