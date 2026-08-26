package io.github.arhor.dnd.companion.domain.repository

import io.github.arhor.dnd.companion.domain.model.FavoriteType
import kotlinx.coroutines.flow.Flow

interface FavoritesRepository {
    fun observeFavoriteIds(type: FavoriteType): Flow<Set<String>>

    suspend fun toggleFavorite(type: FavoriteType, entityId: String)

    suspend fun isFavorite(type: FavoriteType, entityId: String): Boolean
}
