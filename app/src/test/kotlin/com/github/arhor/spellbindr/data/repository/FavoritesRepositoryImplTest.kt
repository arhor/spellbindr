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
    fun `observeFavoriteIds emits stored favorites`() = runTest {
        val (repository, file) = createRepository()
        try {
            repository.toggleFavorite(FavoriteType.SPELL, "spell-1")
            repository.toggleFavorite(FavoriteType.SPELL, "spell-2")

            val result = repository.observeFavoriteIds(FavoriteType.SPELL).first()

            assertThat(result).containsExactly("spell-1", "spell-2")
        } finally {
            file.delete()
        }
    }

    @Test
    fun `toggleFavorite stores when missing`() = runTest {
        val (repository, file) = createRepository()
        try {
            repository.toggleFavorite(FavoriteType.SPELL, "spell-1")

            val result = repository.isFavorite(FavoriteType.SPELL, "spell-1")

            assertThat(result).isTrue()
        } finally {
            file.delete()
        }
    }

    @Test
    fun `toggleFavorite removes when present`() = runTest {
        val (repository, file) = createRepository()
        try {
            repository.toggleFavorite(FavoriteType.SPELL, "spell-1")
            repository.toggleFavorite(FavoriteType.SPELL, "spell-1")

            val result = repository.isFavorite(FavoriteType.SPELL, "spell-1")

            assertThat(result).isFalse()
        } finally {
            file.delete()
        }
    }

    @Test
    fun `isFavorite returns false when missing`() = runTest {
        val (repository, file) = createRepository()
        try {
            val result = repository.isFavorite(FavoriteType.SPELL, "spell-1")

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
