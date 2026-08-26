package com.github.arhor.spellbindr.ui.feature.character.guided.internal

import com.github.arhor.spellbindr.domain.model.CharacterClass
import com.github.arhor.spellbindr.domain.model.Loadable
import com.github.arhor.spellbindr.domain.usecase.ObserveAllBackgroundsUseCase
import com.github.arhor.spellbindr.domain.usecase.ObserveAllCharacterClassesUseCase
import com.github.arhor.spellbindr.domain.usecase.ObserveAllEquipmentUseCase
import com.github.arhor.spellbindr.domain.usecase.ObserveAllFeaturesUseCase
import com.github.arhor.spellbindr.domain.usecase.ObserveAllLanguagesUseCase
import com.github.arhor.spellbindr.domain.usecase.ObserveAllRacesUseCase
import com.github.arhor.spellbindr.domain.usecase.ObserveAllTraitsUseCase
import com.google.common.truth.Truth.assertThat
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Test

class GuidedReferenceDataCoordinatorTest {

    @Test
    fun `observeGuidedReferenceDataState should retain bundled version when source data re-emits`() = runTest {
        // Given
        val classesFlow = MutableSharedFlow<Loadable<List<CharacterClass>>>(replay = 1)
        val observeClasses = mockk<ObserveAllCharacterClassesUseCase> {
            every { this@mockk() } returns classesFlow
        }
        val observeRaces = mockk<ObserveAllRacesUseCase> {
            every { this@mockk() } returns flowOf(Loadable.Content(emptyList()))
        }
        val observeTraits = mockk<ObserveAllTraitsUseCase> {
            every { this@mockk() } returns flowOf(Loadable.Content(emptyList()))
        }
        val observeBackgrounds = mockk<ObserveAllBackgroundsUseCase> {
            every { this@mockk() } returns flowOf(Loadable.Content(emptyList()))
        }
        val observeLanguages = mockk<ObserveAllLanguagesUseCase> {
            every { this@mockk() } returns flowOf(Loadable.Content(emptyList()))
        }
        val observeFeatures = mockk<ObserveAllFeaturesUseCase> {
            every { this@mockk() } returns flowOf(Loadable.Content(emptyList()))
        }
        val observeEquipment = mockk<ObserveAllEquipmentUseCase> {
            every { this@mockk() } returns flowOf(Loadable.Content(emptyList()))
        }
        val state = observeGuidedReferenceDataState(
            scope = backgroundScope,
            observeClasses = observeClasses,
            observeRaces = observeRaces,
            observeTraits = observeTraits,
            observeBackgrounds = observeBackgrounds,
            observeLanguages = observeLanguages,
            observeFeatures = observeFeatures,
            observeEquipment = observeEquipment,
        )
        state.launchIn(backgroundScope)
        runCurrent()

        // When
        classesFlow.emit(Loadable.Content(emptyList()))
        runCurrent()
        val firstVersion = (state.value as GuidedReferenceDataState.Content).data.version
        classesFlow.emit(Loadable.Content(listOf(characterClass("fighter"))))
        runCurrent()
        val secondVersion = (state.value as GuidedReferenceDataState.Content).data.version

        // Then
        assertThat(listOf(firstVersion, secondVersion)).containsExactly(
            "srd-5e-2014-data-v1",
            "srd-5e-2014-data-v1",
        ).inOrder()
    }

    private fun characterClass(id: String): CharacterClass = CharacterClass(
        id = id,
        name = "Fighter",
        hitDie = 10,
        proficiencies = emptyList(),
        proficiencyChoices = emptyList(),
        savingThrows = emptyList(),
        startingEquipment = emptyList(),
        subclasses = emptyList(),
        levels = emptyList(),
    )
}
