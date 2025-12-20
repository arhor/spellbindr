package com.github.arhor.spellbindr.domain.repository

import com.github.arhor.spellbindr.domain.model.FavoriteType
import kotlinx.coroutines.flow.Flow

/**
 * Favorites are persisted in Room and exposed via [FavoritesRepositoryImpl].
 */
interface FavoritesRepository {
    fun observeFavoriteIds(type: FavoriteType): Flow<List<String>>

    suspend fun toggleFavorite(type: FavoriteType, entityId: String)

    suspend fun isFavorite(type: FavoriteType, entityId: String): Boolean
}
