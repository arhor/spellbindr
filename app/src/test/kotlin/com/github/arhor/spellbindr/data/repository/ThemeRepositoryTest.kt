package com.github.arhor.spellbindr.data.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.github.arhor.spellbindr.data.model.AppThemeMode
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test
import java.io.File
import kotlin.io.path.createTempFile

class ThemeRepositoryTest {

    @Test
    fun `themeMode returns null when unset`() = runTest {
        val context = createRepository()
        try {
            val stored = context.repository.themeMode.first()
            assertThat(stored).isNull()
        } finally {
            context.file.delete()
        }
    }

    @Test
    fun `setThemeMode persists selection`() = runTest {
        val context = createRepository()
        try {
            context.repository.setThemeMode(AppThemeMode.DARK)
            val stored = context.repository.themeMode.first()
            assertThat(stored).isEqualTo(AppThemeMode.DARK)
        } finally {
            context.file.delete()
        }
    }

    @Test
    fun `themeMode returns null for invalid stored value`() = runTest {
        val context = createRepository()
        try {
            context.dataStore.edit { preferences ->
                preferences[stringPreferencesKey("theme_mode")] = "INVALID"
            }

            val stored = context.repository.themeMode.first()

            assertThat(stored).isNull()
        } finally {
            context.file.delete()
        }
    }
}

private data class ThemeRepositoryTestContext(
    val repository: ThemeRepository,
    val dataStore: DataStore<Preferences>,
    val file: File,
)

private fun TestScope.createRepository(): ThemeRepositoryTestContext {
    val file = createTempFile(prefix = "theme-repo", suffix = ".preferences_pb").toFile()
    val dataStore = PreferenceDataStoreFactory.create(scope = this) { file }
    val repository = ThemeRepository(dataStore)
    return ThemeRepositoryTestContext(repository, dataStore, file)
}
