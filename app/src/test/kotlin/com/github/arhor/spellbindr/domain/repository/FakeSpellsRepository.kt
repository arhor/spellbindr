package com.github.arhor.spellbindr.domain.repository

import com.github.arhor.spellbindr.domain.model.Loadable
import com.github.arhor.spellbindr.domain.model.Spell
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

class FakeSpellsRepository(
    initialSpells: List<Spell> = emptyList(),
    initialFavoriteIds: List<String> = emptyList(),
) : SpellsRepository {
    override val allSpellsState = MutableStateFlow<Loadable<List<Spell>>>(
        Loadable.Success(initialSpells)
    )
    val favoriteSpellIdsState = MutableStateFlow(initialFavoriteIds)

    override val favoriteSpellIds: Flow<List<String>> = favoriteSpellIdsState

    override suspend fun getSpellById(id: String): Spell? =
        (allSpellsState.value as? Loadable.Success)
            ?.data
            ?.firstOrNull { it.id == id }

    override suspend fun toggleFavorite(spellId: String) {
        val updated = favoriteSpellIdsState.value.toMutableSet()
        if (!updated.add(spellId)) {
            updated.remove(spellId)
        }
        favoriteSpellIdsState.value = updated.toList()
    }

    override suspend fun isFavorite(spellId: String?, favoriteSpellIds: List<String>?): Boolean {
        if (spellId == null) return false
        val favorites = favoriteSpellIds?.toSet() ?: favoriteSpellIdsState.value.toSet()
        return spellId in favorites
    }
}
