package com.github.arhor.spellbindr.ui.feature.settings

import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import com.github.arhor.spellbindr.MainDispatcherRule
import com.github.arhor.spellbindr.data.model.AppThemeMode
import com.github.arhor.spellbindr.data.repository.ThemeRepository
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.RegisterExtension
import java.io.File
import kotlin.io.path.createTempFile

@OptIn(ExperimentalCoroutinesApi::class)
class SettingsViewModelTest {

    @JvmField
    @RegisterExtension
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun `state is loaded when repository emits empty preference`() = runTest(mainDispatcherRule.dispatcher) {
        val (viewModel, file) = createViewModel()
        try {
            advanceUntilIdle()
            val state = viewModel.state.value
            assertThat(state.loaded).isTrue()
            assertThat(state.themeMode).isNull()
        } finally {
            file.delete()
        }
    }

    @Test
    fun `onThemeModeSelected updates preference`() = runTest(mainDispatcherRule.dispatcher) {
        val (viewModel, file) = createViewModel()
        try {
            advanceUntilIdle()

            viewModel.onThemeModeSelected(AppThemeMode.DARK)
            advanceUntilIdle()
            assertThat(viewModel.state.value.themeMode).isEqualTo(AppThemeMode.DARK)

            viewModel.onThemeModeSelected(AppThemeMode.LIGHT)
            advanceUntilIdle()
            assertThat(viewModel.state.value.themeMode).isEqualTo(AppThemeMode.LIGHT)

            viewModel.onThemeModeSelected(null)
            advanceUntilIdle()
            assertThat(viewModel.state.value.themeMode).isNull()
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
