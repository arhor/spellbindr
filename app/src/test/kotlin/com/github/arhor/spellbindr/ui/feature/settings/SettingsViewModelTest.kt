package com.github.arhor.spellbindr.ui.feature.settings

import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import com.github.arhor.spellbindr.MainDispatcherRule
import com.github.arhor.spellbindr.data.model.AppThemeMode
import com.github.arhor.spellbindr.data.repository.ThemeRepository
import com.google.common.truth.Truth.assertThat
import io.mockk.coAnswers
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test
import java.io.File
import kotlin.io.path.createTempFile

@OptIn(ExperimentalCoroutinesApi::class)
class SettingsViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun `ensureThemeInitialized persists system default`() = runTest(mainDispatcherRule.dispatcher) {
        val (viewModel, file) = createViewModel()
        try {
            advanceUntilIdle()
            assertThat(viewModel.state.value.themeMode).isNull()

            viewModel.ensureThemeInitialized(defaultIsDark = true)
            advanceUntilIdle()

            assertThat(viewModel.state.value.themeMode).isEqualTo(AppThemeMode.DARK)
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
            assertThat(viewModel.state.value.themeMode).isEqualTo(AppThemeMode.DARK)

            viewModel.onThemeToggle(isDark = false)
            advanceUntilIdle()
            assertThat(viewModel.state.value.themeMode).isEqualTo(AppThemeMode.LIGHT)
        } finally {
            file.delete()
        }
    }

    @Test
    fun `ensureThemeInitialized applies default when repository is empty`() = runTest(mainDispatcherRule.dispatcher) {
        val themeFlow = MutableStateFlow<AppThemeMode?>(null)
        val repository = mockk<ThemeRepository> {
            every { themeMode } returns themeFlow
        }
        coEvery { repository.setThemeMode(AppThemeMode.DARK) } coAnswers {
            themeFlow.value = AppThemeMode.DARK
        }

        val viewModel = SettingsViewModel(repository)

        viewModel.ensureThemeInitialized(defaultIsDark = true)
        advanceUntilIdle()

        assertThat(viewModel.state.value.themeMode).isEqualTo(AppThemeMode.DARK)
        coVerify(exactly = 1) { repository.setThemeMode(AppThemeMode.DARK) }
    }
}

private fun TestScope.createViewModel(): Pair<SettingsViewModel, File> {
    val file = createTempFile(prefix = "settings-vm", suffix = ".preferences_pb").toFile()
    val dataStore = PreferenceDataStoreFactory.create(scope = this) { file }
    val repository = ThemeRepository(dataStore)
    return SettingsViewModel(repository) to file
}
