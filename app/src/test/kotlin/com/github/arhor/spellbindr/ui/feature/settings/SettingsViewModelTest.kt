package com.github.arhor.spellbindr.ui.feature.settings

import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import com.github.arhor.spellbindr.MainDispatcherRule
import com.github.arhor.spellbindr.data.model.AppThemeMode
import com.github.arhor.spellbindr.data.repository.ThemeRepository
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Rule
import org.junit.Test
import java.io.File
import kotlin.io.path.createTempFile

class SettingsViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun `ensureThemeInitialized persists system default`() = runTest(mainDispatcherRule.dispatcher) {
        val (viewModel, file) = createViewModel()
        try {
            advanceUntilIdle()
            assertNull(viewModel.state.value.themeMode)

            viewModel.ensureThemeInitialized(defaultIsDark = true)
            advanceUntilIdle()

            assertEquals(AppThemeMode.DARK, viewModel.state.value.themeMode)
        } finally {
            file.delete()
        }
    }

    @Test
    fun `onThemeToggle updates preference`() = runTest(mainDispatcherRule.dispatcher) {
        val (viewModel, file) = createViewModel()
        try {
            advanceUntilIdle()

            viewModel.onThemeToggle(isDark = true)
            advanceUntilIdle()
            assertEquals(AppThemeMode.DARK, viewModel.state.value.themeMode)

            viewModel.onThemeToggle(isDark = false)
            advanceUntilIdle()
            assertEquals(AppThemeMode.LIGHT, viewModel.state.value.themeMode)
        } finally {
            file.delete()
        }
    }
}

private fun TestScope.createViewModel(): Pair<SettingsViewModel, File> {
    val file = createTempFile(prefix = "settings-vm", suffix = ".preferences_pb").toFile()
    val dataStore = PreferenceDataStoreFactory.create(scope = this) { file }
    val repository = ThemeRepository(dataStore)
    return SettingsViewModel(repository) to file
}
