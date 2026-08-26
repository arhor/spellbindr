package io.github.arhor.dnd.companion.data.repository

import io.github.arhor.dnd.companion.data.local.assets.SpellAssetDataStore
import io.github.arhor.dnd.companion.domain.model.FavoriteType
import io.github.arhor.dnd.companion.domain.model.Loadable
import io.github.arhor.dnd.companion.domain.model.Spell
import io.github.arhor.dnd.companion.domain.repository.FavoritesRepository
import io.github.arhor.dnd.companion.domain.repository.SpellsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SpellsRepositoryImpl @Inject constructor(
    private val allSpellsDataStore: SpellAssetDataStore,
    private val favoritesRepository: FavoritesRepository,
) : SpellsRepository {

    override val allSpellsState: Flow<Loadable<List<Spell>>>
        get() = allSpellsDataStore.data

    override val favoriteSpellIds: Flow<Set<String>>
        get() = favoritesRepository.observeFavoriteIds(FavoriteType.SPELL)

    override suspend fun getSpellById(id: String): Spell? =
        when (val state = allSpellsDataStore.data.first { it !is Loadable.Loading }) {
            is Loadable.Content -> state.data.firstOrNull { it.id == id }
            is Loadable.Failure -> null
            is Loadable.Loading -> null
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
