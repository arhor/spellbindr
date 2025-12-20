package com.github.arhor.spellbindr.data.repository

import com.github.arhor.spellbindr.data.local.db.FavoriteEntity
import com.github.arhor.spellbindr.data.local.db.FavoritesDao
import com.github.arhor.spellbindr.domain.model.FavoriteType
import com.github.arhor.spellbindr.domain.repository.FavoritesRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FavoritesRepositoryImpl @Inject constructor(
    private val favoritesDao: FavoritesDao,
) : FavoritesRepository {
    override fun observeFavoriteIds(type: FavoriteType): Flow<List<String>> =
        favoritesDao.observeFavoriteIds(type.name)

    override suspend fun toggleFavorite(type: FavoriteType, entityId: String) {
        val entity = FavoriteEntity(type = type.name, entityId = entityId)
        val isFavorite = favoritesDao.isFavorite(type.name, entityId) > 0

        if (isFavorite) {
            favoritesDao.deleteFavorite(entity)
        } else {
            favoritesDao.insertFavorite(entity)
        }
    }

    override suspend fun isFavorite(type: FavoriteType, entityId: String): Boolean =
        favoritesDao.isFavorite(type.name, entityId) > 0
}
