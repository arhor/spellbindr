package com.github.arhor.spellbindr.settings.data.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.github.arhor.spellbindr.domain.model.AppSettings
import com.github.arhor.spellbindr.domain.model.ThemeMode
import com.github.arhor.spellbindr.logging.NoOpLoggerFactory
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import org.junit.Test
import java.io.File
import kotlin.io.path.createTempFile

class SettingsRepositoryImplTest {

    @Test
    fun `settings should emit default app settings when value is not stored`() = runTest {
        // Given
        val (repository, _, file) = createRepository()
        try {
            // When
            val result = repository.settings.first()

            // Then
            assertThat(result).isEqualTo(AppSettings(themeMode = null))
        } finally {
            file.delete()
        }
    }

    @Test
    fun `setThemeMode should persist selected mode`() = runTest {
        // Given
        val (repository, _, file) = createRepository()
        try {
            // When
            repository.setThemeMode(ThemeMode.DARK)
            val result = repository.settings.first()

            // Then
            assertThat(result.themeMode).isEqualTo(ThemeMode.DARK)
        } finally {
            file.delete()
        }
    }

    @Test
    fun `setThemeMode with null should clear stored mode`() = runTest {
        // Given
        val (repository, _, file) = createRepository()
        try {
            repository.setThemeMode(ThemeMode.DARK)

            // When
            repository.setThemeMode(null)
            val result = repository.settings.first()

            // Then
            assertThat(result).isEqualTo(AppSettings(themeMode = null))
        } finally {
            file.delete()
        }
    }

    @Test
    fun `settings should map invalid stored mode to null`() = runTest {
        // Given
        val (repository, dataStore, file) = createRepository()
        try {
            dataStore.edit { preferences ->
                preferences[THEME_MODE_KEY] = "INVALID_MODE"
            }

            // When
            val result = repository.settings.first()

            // Then
            assertThat(result).isEqualTo(AppSettings(themeMode = null))
        } finally {
            file.delete()
        }
    }

    companion object {
        private val THEME_MODE_KEY = stringPreferencesKey("theme_mode")
    }
}

private fun TestScope.createRepository(): Triple<SettingsRepositoryImpl, DataStore<Preferences>, File> {
    val file = createTempFile(prefix = "settings-repo", suffix = ".preferences_pb").toFile()
    val dataStore = PreferenceDataStoreFactory.create(scope = this) { file }

    return Triple(
        first = SettingsRepositoryImpl(
            dataStore = dataStore,
            loggerFactory = NoOpLoggerFactory,
        ),
        second = dataStore,
        third = file,
    )
}
