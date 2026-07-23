package com.github.arhor.spellbindr.ui.feature.character.guided

import com.github.arhor.spellbindr.MainDispatcherRule
import com.github.arhor.spellbindr.domain.model.Loadable
import com.github.arhor.spellbindr.domain.usecase.ObserveAllBackgroundsUseCase
import com.github.arhor.spellbindr.domain.usecase.ObserveAllCharacterClassesUseCase
import com.github.arhor.spellbindr.domain.usecase.ObserveAllEquipmentUseCase
import com.github.arhor.spellbindr.domain.usecase.ObserveAllFeaturesUseCase
import com.github.arhor.spellbindr.domain.usecase.ObserveAllLanguagesUseCase
import com.github.arhor.spellbindr.domain.usecase.ObserveAllRacesUseCase
import com.github.arhor.spellbindr.domain.usecase.ObserveAllSpellsUseCase
import com.github.arhor.spellbindr.domain.usecase.ObserveAllTraitsUseCase
import com.github.arhor.spellbindr.domain.usecase.SaveCharacterSheetUseCase
import com.google.common.truth.Truth.assertThat
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test

class GuidedCharacterSetupViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun `dispatch should update name in content state`() =
        runTest(mainDispatcherRule.dispatcher) {
            // Given
            val observeClasses = mockk<ObserveAllCharacterClassesUseCase>()
            val observeRaces = mockk<ObserveAllRacesUseCase>()
            val observeTraits = mockk<ObserveAllTraitsUseCase>()
            val observeBackgrounds = mockk<ObserveAllBackgroundsUseCase>()
            val observeLanguages = mockk<ObserveAllLanguagesUseCase>()
            val observeFeatures = mockk<ObserveAllFeaturesUseCase>()
            val observeEquipment = mockk<ObserveAllEquipmentUseCase>()
            val observeSpells = mockk<ObserveAllSpellsUseCase>()
            val saveCharacterSheet = mockk<SaveCharacterSheetUseCase>(relaxed = true)

            every { observeClasses() } returns flowOf(Loadable.Content(emptyList()))
            every { observeRaces() } returns flowOf(Loadable.Content(emptyList()))
            every { observeTraits() } returns flowOf(Loadable.Content(emptyList()))
            every { observeBackgrounds() } returns flowOf(Loadable.Content(emptyList()))
            every { observeLanguages() } returns flowOf(Loadable.Content(emptyList()))
            every { observeFeatures() } returns flowOf(Loadable.Content(emptyList()))
            every { observeEquipment() } returns flowOf(Loadable.Content(emptyList()))
            every { observeSpells() } returns flowOf(Loadable.Content(emptyList()))

            val vm = GuidedCharacterSetupViewModel(
                observeClasses = observeClasses,
                observeRaces = observeRaces,
                observeTraits = observeTraits,
                observeBackgrounds = observeBackgrounds,
                observeLanguages = observeLanguages,
                observeFeatures = observeFeatures,
                observeEquipment = observeEquipment,
                observeSpells = observeSpells,
                saveCharacterSheet = saveCharacterSheet,
            )
            advanceUntilIdle()
            vm.uiState.first { it is GuidedCharacterSetupUiState.Content }

            // When
            vm.dispatch(GuidedCharacterSetupIntent.NameChanged("Nova"))
            advanceUntilIdle()

            // Then
            val state = vm.uiState.first {
                it is GuidedCharacterSetupUiState.Content && it.name == "Nova"
            } as GuidedCharacterSetupUiState.Content
            assertThat(state.name).isEqualTo("Nova")
        }
}
