package com.github.arhor.spellbindr.ui.feature.character.spellpicker

import androidx.lifecycle.SavedStateHandle
import com.github.arhor.spellbindr.MainDispatcherRule
import com.github.arhor.spellbindr.domain.model.CharacterSheet
import com.github.arhor.spellbindr.domain.model.Loadable
import com.github.arhor.spellbindr.domain.usecase.ObserveCharacterSheetUseCase
import com.github.arhor.spellbindr.domain.usecase.ObserveSpellcastingClassesUseCase
import com.github.arhor.spellbindr.domain.usecase.ObserveSpellsUseCase
import com.google.common.truth.Truth.assertThat
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test

class CharacterSpellPickerViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun `dispatch should update query and favorites filter`() =
        runTest(mainDispatcherRule.dispatcher) {
            // Given
            val observeCharacterSheet = mockk<ObserveCharacterSheetUseCase>()
            val observeSpells = mockk<ObserveSpellsUseCase>()
            val observeSpellcastingClasses = mockk<ObserveSpellcastingClassesUseCase>()
            every { observeCharacterSheet("character-1") } returns flowOf(
                Loadable.Content(CharacterSheet(id = "character-1", className = "Wizard")),
            )
            every { observeSpells(any(), any(), any()) } returns flowOf(Loadable.Content(emptyList()))
            every { observeSpellcastingClasses() } returns flowOf(Loadable.Content(emptyList()))

            val vm = CharacterSpellPickerViewModel(
                observeCharacterSheet = observeCharacterSheet,
                observeSpells = observeSpells,
                observeSpellcastingClasses = observeSpellcastingClasses,
                savedStateHandle = SavedStateHandle(mapOf("characterId" to "character-1")),
            )
            advanceUntilIdle()
            vm.uiState.first { it is CharacterSpellPickerUiState.Content }

            // When
            vm.dispatch(CharacterSpellPickerIntent.QueryChanged("Fire"))
            advanceTimeBy(350)
            advanceUntilIdle()
            vm.dispatch(CharacterSpellPickerIntent.FavoritesToggled)
            advanceUntilIdle()

            // Then
            val content = vm.uiState.first {
                it is CharacterSpellPickerUiState.Content && it.query == "Fire" && it.showFavoriteOnly
            } as CharacterSpellPickerUiState.Content
            assertThat(content.query).isEqualTo("Fire")
            assertThat(content.showFavoriteOnly).isTrue()
        }
}
