package com.github.arhor.spellbindr.ui.feature.settings

import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import com.github.arhor.spellbindr.MainDispatcherRule
import com.github.arhor.spellbindr.data.repository.ThemeRepositoryImpl
import com.github.arhor.spellbindr.domain.model.ThemeMode
import com.github.arhor.spellbindr.domain.usecase.ObserveThemeModeUseCase
import com.github.arhor.spellbindr.domain.usecase.SetThemeModeUseCase
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.ExperimentalCoroutinesApi
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

            viewModel.onThemeModeSelected(ThemeMode.DARK)
            advanceUntilIdle()
            assertThat(viewModel.state.value.themeMode).isEqualTo(ThemeMode.DARK)

            viewModel.onThemeModeSelected(ThemeMode.LIGHT)
            advanceUntilIdle()
            assertThat(viewModel.state.value.themeMode).isEqualTo(ThemeMode.LIGHT)

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
    val repository = ThemeRepositoryImpl(dataStore)
    val observeThemeModeUseCase = ObserveThemeModeUseCase(repository)
    val setThemeModeUseCase = SetThemeModeUseCase(repository)
    return SettingsViewModel(observeThemeModeUseCase, setThemeModeUseCase) to file
}
