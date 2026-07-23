package com.github.arhor.spellbindr.data.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringSetPreferencesKey
import com.github.arhor.spellbindr.di.FavoritesDataStore
import com.github.arhor.spellbindr.domain.model.FavoriteType
import com.github.arhor.spellbindr.domain.repository.FavoritesRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FavoritesRepositoryImpl @Inject constructor(
    @FavoritesDataStore private val dataStore: DataStore<Preferences>,
) : FavoritesRepository {
    override fun observeFavoriteIds(type: FavoriteType): Flow<Set<String>> =
        dataStore.data
            .map { it[favoritesKey(type)] }
            .map { it.orEmpty() }

    override suspend fun toggleFavorite(type: FavoriteType, entityId: String) {
        dataStore.edit { preferences ->
            val key = favoritesKey(type)
            val current = preferences[key].orEmpty().toMutableSet()
            if (entityId in current) {
                current.remove(entityId)
            } else {
                current.add(entityId)
            }
            if (current.isEmpty()) {
                preferences.remove(key)
            } else {
                preferences[key] = current
            }
        }
    }

    override suspend fun isFavorite(type: FavoriteType, entityId: String): Boolean =
        dataStore.data.first()[favoritesKey(type)]?.contains(entityId) == true

    private fun favoritesKey(type: FavoriteType) =
        stringSetPreferencesKey("favorites_${type.name.lowercase()}")
}
