package com.github.arhor.spellbindr.ui.feature.settings

import com.github.arhor.spellbindr.MainDispatcherRule
import com.github.arhor.spellbindr.domain.model.ThemeMode
import com.github.arhor.spellbindr.domain.repository.FakeThemeRepository
import com.github.arhor.spellbindr.domain.usecase.ObserveThemeModeUseCase
import com.github.arhor.spellbindr.domain.usecase.SetThemeModeUseCase
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class SettingsViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun `state should load when repository emits empty preference`() = runTest(mainDispatcherRule.dispatcher) {
        // Given
        val viewModel = createViewModel()

        // When
        advanceUntilIdle()
        val state = viewModel.state.value

        // Then
        assertThat(state.loaded).isTrue()
        assertThat(state.themeMode).isNull()
    }

    @Test
    fun `onThemeModeSelected should update preference when mode changes`() = runTest(mainDispatcherRule.dispatcher) {
        // Given
        val viewModel = createViewModel()
        advanceUntilIdle()

        // When
        viewModel.onThemeModeSelected(ThemeMode.DARK)
        advanceUntilIdle()
        val darkSelection = viewModel.state.value.themeMode

        viewModel.onThemeModeSelected(ThemeMode.LIGHT)
        advanceUntilIdle()
        val lightSelection = viewModel.state.value.themeMode

        viewModel.onThemeModeSelected(null)
        advanceUntilIdle()

        // Then
        assertThat(darkSelection).isEqualTo(ThemeMode.DARK)
        assertThat(lightSelection).isEqualTo(ThemeMode.LIGHT)
        assertThat(viewModel.state.value.themeMode).isNull()
    }
}

private fun TestScope.createViewModel(): SettingsViewModel {
    val repository = FakeThemeRepository()
    val observeThemeModeUseCase = ObserveThemeModeUseCase(repository)
    val setThemeModeUseCase = SetThemeModeUseCase(repository)
    return SettingsViewModel(observeThemeModeUseCase, setThemeModeUseCase)
}
