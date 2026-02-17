package com.github.arhor.spellbindr.ui.feature.settings

import com.github.arhor.spellbindr.MainDispatcherRule
import com.github.arhor.spellbindr.domain.model.ThemeMode
import com.github.arhor.spellbindr.domain.usecase.ObserveThemeModeUseCase
import com.github.arhor.spellbindr.domain.usecase.SetThemeModeUseCase
import com.google.common.truth.Truth.assertThat
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test

class SettingsViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val observeThemeMode = mockk<ObserveThemeModeUseCase>()
    private val setThemeModeUseCase = mockk<SetThemeModeUseCase>()

    @Test
    fun `uiState should emit loading then content when theme mode is available`() =
        runTest(mainDispatcherRule.dispatcher) {
            // Given
            val themeModes = MutableSharedFlow<ThemeMode?>()
            every { observeThemeMode() } returns themeModes
            coEvery { setThemeModeUseCase(any()) } returns Unit

            val viewModel = SettingsViewModel(observeThemeMode, setThemeModeUseCase)
            val states = mutableListOf<SettingsUiState>()

            // When
            val job = launch { viewModel.uiState.take(2).toList(states) }
            runCurrent()
            themeModes.emit(ThemeMode.DARK)
            advanceUntilIdle()
            job.join()

            // Then
            assertThat(states)
                .containsExactly(
                    SettingsUiState.Loading,
                    SettingsUiState.Content(ThemeMode.DARK),
                )
                .inOrder()
            verify(exactly = 1) { observeThemeMode() }
        }

    @Test
    fun `dispatch should update theme only when selected mode differs from current`() =
        runTest(mainDispatcherRule.dispatcher) {
            // Given
            every { observeThemeMode() } returns MutableStateFlow(ThemeMode.DARK)
            coEvery { setThemeModeUseCase(ThemeMode.LIGHT) } returns Unit
            coEvery { setThemeModeUseCase(ThemeMode.DARK) } returns Unit

            val viewModel = SettingsViewModel(observeThemeMode, setThemeModeUseCase)
            advanceUntilIdle()

            // When
            viewModel.dispatch(SettingsIntent.ThemeModeSelected(ThemeMode.DARK))
            viewModel.dispatch(SettingsIntent.ThemeModeSelected(ThemeMode.LIGHT))
            advanceUntilIdle()

            // Then
            coVerify(exactly = 0) { setThemeModeUseCase(ThemeMode.DARK) }
            coVerify(exactly = 1) { setThemeModeUseCase(ThemeMode.LIGHT) }
        }

    @Test
    fun `dispatch should emit show message effect when theme update fails`() =
        runTest(mainDispatcherRule.dispatcher) {
            // Given
            every { observeThemeMode() } returns MutableStateFlow(ThemeMode.DARK)
            coEvery { setThemeModeUseCase(ThemeMode.LIGHT) } throws IllegalStateException("Write failed")

            val viewModel = SettingsViewModel(observeThemeMode, setThemeModeUseCase)
            advanceUntilIdle()

            // When
            val effect = async { viewModel.effects.first() }
            viewModel.dispatch(SettingsIntent.ThemeModeSelected(ThemeMode.LIGHT))
            advanceUntilIdle()

            // Then
            assertThat(effect.await()).isEqualTo(SettingsEffect.ShowMessage("Write failed"))
        }

    @Test
    fun `uiState should become error when observing theme mode fails`() =
        runTest(mainDispatcherRule.dispatcher) {
            // Given
            every { observeThemeMode() } returns flow {
                throw IllegalStateException("Broken flow")
            }
            coEvery { setThemeModeUseCase(any()) } returns Unit

            // When
            val viewModel = SettingsViewModel(observeThemeMode, setThemeModeUseCase)
            advanceUntilIdle()

            // Then
            assertThat(viewModel.uiState.value).isEqualTo(SettingsUiState.Error("Broken flow"))
        }
}
