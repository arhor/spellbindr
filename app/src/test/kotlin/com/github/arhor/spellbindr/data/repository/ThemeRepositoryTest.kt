package com.github.arhor.spellbindr.data.repository

import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import com.github.arhor.spellbindr.data.model.AppThemeMode
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.io.File
import kotlin.io.path.createTempFile

class ThemeRepositoryTest {

    @Test
    fun `themeMode returns null when unset`() = runTest {
        val (repository, file) = createRepository()
        try {
            val stored = repository.themeMode.first()
            assertNull(stored)
        } finally {
            file.delete()
        }
    }

    @Test
    fun `setThemeMode persists selection`() = runTest {
        val (repository, file) = createRepository()
        try {
            repository.setThemeMode(AppThemeMode.DARK)
            val stored = repository.themeMode.first()
            assertEquals(AppThemeMode.DARK, stored)
        } finally {
            file.delete()
        }
    }
}

private fun TestScope.createRepository(): Pair<ThemeRepository, File> {
    val file = createTempFile(prefix = "theme-repo", suffix = ".preferences_pb").toFile()
    val dataStore = PreferenceDataStoreFactory.create(scope = this) { file }
    return ThemeRepository(dataStore) to file
}
