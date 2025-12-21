package com.github.arhor.spellbindr.domain.repository

import com.github.arhor.spellbindr.domain.model.FavoriteType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

class FakeFavoritesRepository(
    initialFavorites: Map<FavoriteType, List<String>> = emptyMap(),
) : FavoritesRepository {
    private val favoritesByType = FavoriteType.entries.associateWith { type ->
        MutableStateFlow(initialFavorites[type].orEmpty())
    }.toMutableMap()

    override fun observeFavoriteIds(type: FavoriteType): Flow<List<String>> =
        favoritesByType.getOrPut(type) { MutableStateFlow(emptyList()) }

    override suspend fun toggleFavorite(type: FavoriteType, entityId: String) {
        val state = favoritesByType.getOrPut(type) { MutableStateFlow(emptyList()) }
        val updated = state.value.toMutableSet()
        if (!updated.add(entityId)) {
            updated.remove(entityId)
        }
        state.value = updated.toList()
    }

    override suspend fun isFavorite(type: FavoriteType, entityId: String): Boolean =
        favoritesByType[type]?.value?.contains(entityId) ?: false
}
