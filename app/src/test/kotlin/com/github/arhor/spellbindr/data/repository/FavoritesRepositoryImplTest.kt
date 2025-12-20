package com.github.arhor.spellbindr.data.repository

import com.github.arhor.spellbindr.data.local.db.FavoriteEntity
import com.github.arhor.spellbindr.data.local.db.FavoritesDao
import com.github.arhor.spellbindr.domain.model.FavoriteType
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Test

class FavoritesRepositoryImplTest {

    private val dao = FakeFavoritesDao()
    private val repository = FavoritesRepositoryImpl(dao)

    @Test
    fun `observeFavoriteIds delegates to dao`() = runTest {
        // Given
        dao.favoriteIdsState.value = listOf("spell-1", "spell-2")

        // When
        val result = repository.observeFavoriteIds(FavoriteType.SPELL).first()

        // Then
        assertThat(result).containsExactly("spell-1", "spell-2").inOrder()
        assertThat(dao.lastObservedType).isEqualTo(FavoriteType.SPELL.name)
    }

    @Test
    fun `toggleFavorite inserts when missing`() = runTest {
        // Given
        dao.favoriteIdsState.value = emptyList()

        // When
        repository.toggleFavorite(FavoriteType.SPELL, "spell-1")

        // Then
        assertThat(dao.inserted).containsExactly(FavoriteEntity(type = FavoriteType.SPELL.name, entityId = "spell-1"))
        assertThat(dao.deleted).isEmpty()
    }

    @Test
    fun `toggleFavorite deletes when present`() = runTest {
        // Given
        dao.favoriteIdsState.value = listOf("spell-1")

        // When
        repository.toggleFavorite(FavoriteType.SPELL, "spell-1")

        // Then
        assertThat(dao.deleted).containsExactly(FavoriteEntity(type = FavoriteType.SPELL.name, entityId = "spell-1"))
        assertThat(dao.inserted).isEmpty()
    }

    @Test
    fun `isFavorite returns true when dao has record`() = runTest {
        // Given
        dao.favoriteIdsState.value = listOf("spell-1")

        // When
        val result = repository.isFavorite(FavoriteType.SPELL, "spell-1")

        // Then
        assertThat(result).isTrue()
    }

    private class FakeFavoritesDao : FavoritesDao {
        val favoriteIdsState = MutableStateFlow<List<String>>(emptyList())
        val inserted = mutableListOf<FavoriteEntity>()
        val deleted = mutableListOf<FavoriteEntity>()
        var lastObservedType: String? = null

        override fun observeFavoriteIds(type: String): Flow<List<String>> {
            lastObservedType = type
            return favoriteIdsState
        }

        override suspend fun isFavorite(type: String, entityId: String): Int =
            if (entityId in favoriteIdsState.value) 1 else 0

        override suspend fun insertFavorite(favorite: FavoriteEntity) {
            inserted += favorite
        }

        override suspend fun deleteFavorite(favorite: FavoriteEntity) {
            deleted += favorite
        }
    }
}
