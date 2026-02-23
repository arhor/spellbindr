package com.github.arhor.spellbindr.domain.repository

import com.github.arhor.spellbindr.domain.model.FavoriteType
import kotlinx.coroutines.flow.Flow

interface FavoritesRepository {
    fun observeFavoriteIds(type: FavoriteType): Flow<Set<String>>

    suspend fun toggleFavorite(type: FavoriteType, entityId: String)

    suspend fun isFavorite(type: FavoriteType, entityId: String): Boolean
}
