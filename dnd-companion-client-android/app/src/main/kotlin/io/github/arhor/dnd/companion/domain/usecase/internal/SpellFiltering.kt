package io.github.arhor.dnd.companion.domain.usecase.internal

import io.github.arhor.dnd.companion.domain.model.EntityRef
import io.github.arhor.dnd.companion.domain.model.Spell

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
