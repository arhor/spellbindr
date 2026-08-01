package com.github.arhor.spellbindr.ui.feature.character.guided

import com.github.arhor.spellbindr.MainDispatcherRule
import com.github.arhor.spellbindr.domain.model.AbilityIds
import com.github.arhor.spellbindr.domain.model.Background
import com.github.arhor.spellbindr.domain.model.CharacterCreationResult
import com.github.arhor.spellbindr.domain.model.CharacterClass
import com.github.arhor.spellbindr.domain.model.ClassLevel
import com.github.arhor.spellbindr.domain.model.GenericInfo
import com.github.arhor.spellbindr.domain.model.HitPointGain
import com.github.arhor.spellbindr.domain.model.Loadable
import com.github.arhor.spellbindr.domain.model.Race
import com.github.arhor.spellbindr.domain.usecase.ObserveAllBackgroundsUseCase
import com.github.arhor.spellbindr.domain.usecase.ObserveAllCharacterClassesUseCase
import com.github.arhor.spellbindr.domain.usecase.ObserveAllEquipmentUseCase
import com.github.arhor.spellbindr.domain.usecase.ObserveAllFeaturesUseCase
import com.github.arhor.spellbindr.domain.usecase.ObserveAllLanguagesUseCase
import com.github.arhor.spellbindr.domain.usecase.ObserveAllRacesUseCase
import com.github.arhor.spellbindr.domain.usecase.ObserveAllSpellsUseCase
import com.github.arhor.spellbindr.domain.usecase.ObserveAllTraitsUseCase
import com.github.arhor.spellbindr.domain.usecase.SaveGuidedCharacterUseCase
import com.github.arhor.spellbindr.ui.feature.character.guided.model.AbilityScoreMethod
import com.google.common.truth.Truth.assertThat
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import kotlinx.coroutines.async
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
    fun `dispatch should update name in content state when name changes`() =
        runTest(mainDispatcherRule.dispatcher) {
            // Given
            val vm = buildViewModel()
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

    @Test
    fun `dispatch should atomically save creation result once when create is valid`() =
        runTest(mainDispatcherRule.dispatcher) {
            // Given
            val fighter = CharacterClass(
                id = "fighter",
                name = "Fighter",
                hitDie = 10,
                proficiencies = emptyList(),
                proficiencyChoices = emptyList(),
                savingThrows = emptyList(),
                spellcasting = null,
                startingEquipment = null,
                subclasses = emptyList(),
                levels = listOf(ClassLevel(id = "fighter-1", level = 1, features = emptyList())),
            )
            val human = Race(
                id = "human",
                name = "Human",
                traits = emptyList(),
                subraces = emptyList(),
            )
            val acolyte = Background(
                id = "acolyte",
                name = "Acolyte",
                feature = GenericInfo(name = "Shelter of the Faithful", desc = emptyList()),
                effects = emptyList(),
            )
            val saveGuidedCharacter = mockk<SaveGuidedCharacterUseCase>()
            val savedResult = slot<CharacterCreationResult>()
            coEvery { saveGuidedCharacter(capture(savedResult)) } returns Unit
            val vm = buildViewModel(
                classes = listOf(fighter),
                races = listOf(human),
                backgrounds = listOf(acolyte),
                saveGuidedCharacter = saveGuidedCharacter,
            )
            vm.uiState.first { it is GuidedCharacterSetupUiState.Content }
            vm.dispatch(GuidedCharacterSetupIntent.NameChanged("Nova"))
            vm.dispatch(GuidedCharacterSetupIntent.ClassSelected(fighter.id))
            vm.dispatch(GuidedCharacterSetupIntent.RaceSelected(human.id))
            vm.dispatch(GuidedCharacterSetupIntent.BackgroundSelected(acolyte.id))
            vm.dispatch(GuidedCharacterSetupIntent.AbilityMethodSelected(AbilityScoreMethod.POINT_BUY))
            advanceUntilIdle()
            vm.uiState.first { state ->
                state is GuidedCharacterSetupUiState.Content &&
                    state.selection.classId == fighter.id &&
                    state.selection.raceId == human.id &&
                    state.selection.backgroundId == acolyte.id &&
                    state.selection.abilityMethod == AbilityScoreMethod.POINT_BUY
            }
            val effect = async { vm.effects.first() }

            // When
            vm.dispatch(GuidedCharacterSetupIntent.CreateClicked)
            advanceUntilIdle()

            // Then
            coVerify(exactly = 1) { saveGuidedCharacter(any()) }
            assertThat(effect.await()).isEqualTo(
                GuidedCharacterSetupEffect.CharacterCreated(savedResult.captured.sheet.id),
            )
            assertThat(savedResult.captured.progression.levels.single().classId).isEqualTo("fighter")
            assertThat(savedResult.captured.progression.levels.single().hitPointGain)
                .isEqualTo(HitPointGain.Fixed(10))
        }

    private fun buildViewModel(
        classes: List<CharacterClass> = emptyList(),
        races: List<Race> = emptyList(),
        backgrounds: List<Background> = emptyList(),
        saveGuidedCharacter: SaveGuidedCharacterUseCase = mockk(relaxed = true),
    ): GuidedCharacterSetupViewModel {
        val observeClasses = mockk<ObserveAllCharacterClassesUseCase>()
        val observeRaces = mockk<ObserveAllRacesUseCase>()
        val observeTraits = mockk<ObserveAllTraitsUseCase>()
        val observeBackgrounds = mockk<ObserveAllBackgroundsUseCase>()
        val observeLanguages = mockk<ObserveAllLanguagesUseCase>()
        val observeFeatures = mockk<ObserveAllFeaturesUseCase>()
        val observeEquipment = mockk<ObserveAllEquipmentUseCase>()
        val observeSpells = mockk<ObserveAllSpellsUseCase>()

        every { observeClasses() } returns flowOf(Loadable.Content(classes))
        every { observeRaces() } returns flowOf(Loadable.Content(races))
        every { observeTraits() } returns flowOf(Loadable.Content(emptyList()))
        every { observeBackgrounds() } returns flowOf(Loadable.Content(backgrounds))
        every { observeLanguages() } returns flowOf(Loadable.Content(emptyList()))
        every { observeFeatures() } returns flowOf(Loadable.Content(emptyList()))
        every { observeEquipment() } returns flowOf(Loadable.Content(emptyList()))
        every { observeSpells() } returns flowOf(Loadable.Content(emptyList()))

        return GuidedCharacterSetupViewModel(
            observeClasses = observeClasses,
            observeRaces = observeRaces,
            observeTraits = observeTraits,
            observeBackgrounds = observeBackgrounds,
            observeLanguages = observeLanguages,
            observeFeatures = observeFeatures,
            observeEquipment = observeEquipment,
            observeSpells = observeSpells,
            saveGuidedCharacter = saveGuidedCharacter,
        )
    }
}
