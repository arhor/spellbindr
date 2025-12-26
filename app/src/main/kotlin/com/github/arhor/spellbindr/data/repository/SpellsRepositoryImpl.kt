package com.github.arhor.spellbindr.data.repository

import com.github.arhor.spellbindr.data.local.assets.SpellAssetDataStore
import com.github.arhor.spellbindr.domain.model.FavoriteType
import com.github.arhor.spellbindr.domain.model.Spell
import com.github.arhor.spellbindr.domain.repository.FavoritesRepository
import com.github.arhor.spellbindr.domain.repository.SpellsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SpellsRepositoryImpl @Inject constructor(
    private val allSpellsDataStore: SpellAssetDataStore,
    private val favoritesRepository: FavoritesRepository,
) : SpellsRepository {
    override val allSpells: Flow<List<Spell>>
        get() = allSpellsDataStore.data.map { it.orEmpty() }

    override val favoriteSpellIds: Flow<List<String>>
        get() = favoritesRepository.observeFavoriteIds(FavoriteType.SPELL)

    override suspend fun getSpellById(id: String): Spell? =
        allSpellsDataStore.data.firstOrNull()?.firstOrNull { it.id == id }

    override suspend fun toggleFavorite(spellId: String) {
        favoritesRepository.toggleFavorite(FavoriteType.SPELL, spellId)
    }

    override suspend fun isFavorite(
        spellId: String?,
        favoriteSpellIds: List<String>?,
    ): Boolean {
        val targetSpellId = spellId ?: return false
        val ids = favoriteSpellIds ?: return favoritesRepository.isFavorite(FavoriteType.SPELL, targetSpellId)

        return targetSpellId in ids
    }

}
