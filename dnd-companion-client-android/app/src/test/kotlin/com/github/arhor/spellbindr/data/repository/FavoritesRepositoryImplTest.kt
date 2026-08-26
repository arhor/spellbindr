package com.github.arhor.spellbindr.data.repository

import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import com.github.arhor.spellbindr.domain.model.FavoriteType
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import org.junit.Test
import java.io.File
import kotlin.io.path.createTempFile

class FavoritesRepositoryImplTest {

    @Test
    fun `observeFavoriteIds should emit stored favorites when toggles are applied`() = runTest {
        // Given
        val (repository, file) = createRepository()
        try {
            repository.toggleFavorite(FavoriteType.SPELL, "spell-1")
            repository.toggleFavorite(FavoriteType.SPELL, "spell-2")

            // When
            val result = repository.observeFavoriteIds(FavoriteType.SPELL).first()

            // Then
            assertThat(result).containsExactly("spell-1", "spell-2")
        } finally {
            file.delete()
        }
    }

    @Test
    fun `toggleFavorite should store id when it is missing`() = runTest {
        // Given
        val (repository, file) = createRepository()
        try {
            repository.toggleFavorite(FavoriteType.SPELL, "spell-1")

            // When
            val result = repository.isFavorite(FavoriteType.SPELL, "spell-1")

            // Then
            assertThat(result).isTrue()
        } finally {
            file.delete()
        }
    }

    @Test
    fun `toggleFavorite should remove id when it is already present`() = runTest {
        // Given
        val (repository, file) = createRepository()
        try {
            repository.toggleFavorite(FavoriteType.SPELL, "spell-1")
            repository.toggleFavorite(FavoriteType.SPELL, "spell-1")

            // When
            val result = repository.isFavorite(FavoriteType.SPELL, "spell-1")

            // Then
            assertThat(result).isFalse()
        } finally {
            file.delete()
        }
    }

    @Test
    fun `isFavorite should return false when id is not stored`() = runTest {
        // Given
        val (repository, file) = createRepository()
        try {
            // When
            val result = repository.isFavorite(FavoriteType.SPELL, "spell-1")

            // Then
            assertThat(result).isFalse()
        } finally {
            file.delete()
        }
    }
}

private fun TestScope.createRepository(): Pair<FavoritesRepositoryImpl, File> {
    val file = createTempFile(prefix = "favorites-repo", suffix = ".preferences_pb").toFile()
    val dataStore = PreferenceDataStoreFactory.create(scope = this) { file }
    return FavoritesRepositoryImpl(dataStore) to file
}
