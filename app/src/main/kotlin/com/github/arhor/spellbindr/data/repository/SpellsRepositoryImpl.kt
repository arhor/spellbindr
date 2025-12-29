package com.github.arhor.spellbindr.data.repository

import com.github.arhor.spellbindr.data.local.assets.SpellAssetDataStore
import com.github.arhor.spellbindr.domain.model.AssetState
import com.github.arhor.spellbindr.domain.model.FavoriteType
import com.github.arhor.spellbindr.domain.model.Spell
import com.github.arhor.spellbindr.domain.repository.FavoritesRepository
import com.github.arhor.spellbindr.domain.repository.SpellsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SpellsRepositoryImpl @Inject constructor(
    private val allSpellsDataStore: SpellAssetDataStore,
    private val favoritesRepository: FavoritesRepository,
) : SpellsRepository {

    override val allSpellsState: Flow<AssetState<List<Spell>>>
        get() = allSpellsDataStore.data

    override val favoriteSpellIds: Flow<List<String>>
        get() = favoritesRepository.observeFavoriteIds(FavoriteType.SPELL)

    override suspend fun getSpellById(id: String): Spell? =
        when (val state = allSpellsDataStore.data.first { it !is AssetState.Loading }) {
            is AssetState.Ready -> state.data.firstOrNull { it.id == id }
            is AssetState.Error -> null
            is AssetState.Loading -> null
        }

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
