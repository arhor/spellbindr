package com.github.arhor.spellbindr.ui.feature.character.sheet

import androidx.lifecycle.SavedStateHandle
import com.github.arhor.spellbindr.MainDispatcherRule
import com.github.arhor.spellbindr.domain.model.CharacterClass
import com.github.arhor.spellbindr.domain.model.CharacterLevelRecord
import com.github.arhor.spellbindr.domain.model.CharacterProgression
import com.github.arhor.spellbindr.domain.model.CharacterSheet
import com.github.arhor.spellbindr.domain.model.CharacterWithProgression
import com.github.arhor.spellbindr.domain.model.HitPointGain
import com.github.arhor.spellbindr.domain.model.Loadable
import com.github.arhor.spellbindr.domain.model.ProgressionOrigin
import com.github.arhor.spellbindr.domain.model.ProgressionState
import com.github.arhor.spellbindr.domain.usecase.DeleteCharacterUseCase
import com.github.arhor.spellbindr.domain.usecase.LoadCharacterWithProgressionUseCase
import com.github.arhor.spellbindr.domain.usecase.ObserveAllSpellsUseCase
import com.github.arhor.spellbindr.domain.usecase.ObserveAllCharacterClassesUseCase
import com.github.arhor.spellbindr.domain.usecase.ObserveWeaponCatalogUseCase
import com.github.arhor.spellbindr.domain.usecase.SaveCharacterSheetUseCase
import com.github.arhor.spellbindr.domain.usecase.ToggleSpellSlotUseCase
import com.github.arhor.spellbindr.domain.usecase.UpdateHitPointsUseCase
import com.github.arhor.spellbindr.domain.usecase.UpdateWeaponListUseCase
import com.github.arhor.spellbindr.ui.feature.character.sheet.model.ProgressionSummaryUiModel
import com.google.common.truth.Truth.assertThat
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test

class CharacterSheetViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun `adjustCurrentHp should save latest sheet once when updates occur within debounce window`() = runTest {
        // Given
        val saveCharacterSheetUseCase = mockk<SaveCharacterSheetUseCase>()
        coEvery { saveCharacterSheetUseCase(any()) } returns Unit
        val initialSheet = CharacterSheet(
            id = TEST_CHARACTER_ID,
            maxHitPoints = 10,
            currentHitPoints = 5,
        )

        val vm = createViewModel(
            initialSheet = initialSheet,
            saveCharacterSheetUseCase = saveCharacterSheetUseCase,
            weaponCatalogState = Loadable.Content(emptyList()),
        )

        advanceUntilIdle()

        // When
        vm.adjustCurrentHp(-1)
        vm.adjustCurrentHp(-1)
        vm.adjustCurrentHp(+1)

        advanceTimeBy(149)
        runCurrent()
        coVerify(exactly = 0) { saveCharacterSheetUseCase(any()) }

        advanceTimeBy(1)
        advanceUntilIdle()

        // Then
        val captured = mutableListOf<CharacterSheet>()
        coVerify(exactly = 1) { saveCharacterSheetUseCase(capture(captured)) }
        assertThat(captured.single().currentHitPoints).isEqualTo(4)
    }

    @Test
    fun `uiState should expose error message when weapon catalog loading fails`() = runTest {
        // Given
        val vm = createViewModel(
            initialSheet = CharacterSheet(id = TEST_CHARACTER_ID),
            weaponCatalogState = Loadable.Failure(errorMessage = "boom"),
        )

        // When
        advanceUntilIdle()

        // Then
        val state = vm.uiState.value as CharacterSheetUiState.Content
        assertThat(state.errorMessage).isEqualTo("Unable to load weapon catalog")
    }

    @Test
    fun `uiState should expose setup message when progression is unmanaged`() = runTest {
        // Given
        val vm = createViewModel(
            initialSheet = CharacterSheet(id = TEST_CHARACTER_ID),
            progressionState = ProgressionState.Unmanaged,
            weaponCatalogState = Loadable.Content(emptyList()),
        )

        // When
        advanceUntilIdle()

        // Then
        val state = vm.uiState.value as CharacterSheetUiState.Content
        val progression = state.progression as ProgressionSummaryUiModel.Unmanaged
        assertThat(progression.message).isEqualTo("Set up level progression to enable guided level-up.")
    }

    @Test
    fun `uiState should expose ordered multiclass summary when progression is managed`() = runTest {
        // Given
        val progression = CharacterProgression(
            referenceDataVersion = "test",
            origin = ProgressionOrigin.Guided,
            levels = listOf(
                levelRecord(characterLevel = 1, classId = "fighter", classLevel = 1),
                levelRecord(characterLevel = 2, classId = "wizard", classLevel = 1),
                levelRecord(characterLevel = 3, classId = "fighter", classLevel = 2),
            ),
        )
        val vm = createViewModel(
            initialSheet = CharacterSheet(id = TEST_CHARACTER_ID),
            progressionState = ProgressionState.Managed(progression),
            characterClasses = listOf(
                characterClass(id = "fighter", name = "Fighter"),
                characterClass(id = "wizard", name = "Wizard"),
            ),
            weaponCatalogState = Loadable.Content(emptyList()),
        )

        // When
        advanceUntilIdle()

        // Then
        val state = vm.uiState.value as CharacterSheetUiState.Content
        val summary = state.progression as ProgressionSummaryUiModel.Managed
        assertThat(summary.totalLevel).isEqualTo(3)
        assertThat(summary.classes).isEqualTo("Fighter 2 / Wizard 1")
        assertThat(summary.levels).containsExactly(
            "1. Fighter 1",
            "2. Wizard 1",
            "3. Fighter 2",
        ).inOrder()
    }

    @Test
    fun `uiState should fall back to stable id label when managed class is absent from reference data`() = runTest {
        // Given
        val progression = CharacterProgression(
            referenceDataVersion = "test",
            origin = ProgressionOrigin.Guided,
            levels = listOf(
                levelRecord(characterLevel = 1, classId = "blood-hunter", classLevel = 1),
            ),
        )
        val vm = createViewModel(
            initialSheet = CharacterSheet(id = TEST_CHARACTER_ID),
            progressionState = ProgressionState.Managed(progression),
            characterClasses = emptyList(),
            weaponCatalogState = Loadable.Content(emptyList()),
        )

        // When
        advanceUntilIdle()

        // Then
        val state = vm.uiState.value as CharacterSheetUiState.Content
        val summary = state.progression as ProgressionSummaryUiModel.Managed
        assertThat(summary.classes).isEqualTo("Blood Hunter 1")
        assertThat(summary.levels).containsExactly("1. Blood Hunter 1")
    }

    @Test
    fun `uiState should expose character classes message when all-class reference loading fails`() = runTest {
        // Given
        val vm = createViewModel(
            initialSheet = CharacterSheet(id = TEST_CHARACTER_ID),
            characterClassesState = Loadable.Failure(errorMessage = "boom"),
            weaponCatalogState = Loadable.Content(emptyList()),
        )

        // When
        advanceUntilIdle()

        // Then
        val state = vm.uiState.value as CharacterSheetUiState.Content
        assertThat(state.errorMessage).isEqualTo("Unable to load character classes")
    }

    private fun createViewModel(
        initialSheet: CharacterSheet,
        progressionState: ProgressionState = ProgressionState.Unmanaged,
        characterClasses: List<CharacterClass> = emptyList(),
        characterClassesState: Loadable<List<CharacterClass>> = Loadable.Content(characterClasses),
        saveCharacterSheetUseCase: SaveCharacterSheetUseCase = mockk(relaxed = true),
        weaponCatalogState: Loadable<List<com.github.arhor.spellbindr.domain.model.WeaponCatalogEntry>>,
    ): CharacterSheetViewModel {
        val deleteCharacterUseCase = mockk<DeleteCharacterUseCase>(relaxed = true)
        val loadCharacterWithProgressionUseCase = mockk<LoadCharacterWithProgressionUseCase>()
        val observeAllSpellsUseCase = mockk<ObserveAllSpellsUseCase>()
        val observeAllCharacterClassesUseCase = mockk<ObserveAllCharacterClassesUseCase>()
        val observeWeaponCatalogUseCase = mockk<ObserveWeaponCatalogUseCase>()

        every { loadCharacterWithProgressionUseCase(TEST_CHARACTER_ID) } returns flowOf(
            CharacterWithProgression(
                sheet = initialSheet,
                progressionState = progressionState,
            )
        )
        every { observeAllSpellsUseCase() } returns flowOf(Loadable.Content(emptyList()))
        every { observeAllCharacterClassesUseCase() } returns flowOf(characterClassesState)
        every { observeWeaponCatalogUseCase() } returns flowOf(weaponCatalogState)

        return CharacterSheetViewModel(
            deleteCharacterUseCase = deleteCharacterUseCase,
            loadCharacterWithProgressionUseCase = loadCharacterWithProgressionUseCase,
            observeAllSpellsUseCase = observeAllSpellsUseCase,
            observeAllCharacterClassesUseCase = observeAllCharacterClassesUseCase,
            observeWeaponCatalogUseCase = observeWeaponCatalogUseCase,
            saveCharacterSheetUseCase = saveCharacterSheetUseCase,
            updateHitPointsUseCase = UpdateHitPointsUseCase(),
            toggleSpellSlotUseCase = ToggleSpellSlotUseCase(),
            updateWeaponListUseCase = UpdateWeaponListUseCase(),
            savedStateHandle = SavedStateHandle(mapOf("characterId" to TEST_CHARACTER_ID)),
        )
    }

    private fun levelRecord(
        characterLevel: Int,
        classId: String,
        classLevel: Int,
    ): CharacterLevelRecord = CharacterLevelRecord(
        characterLevel = characterLevel,
        classId = classId,
        classLevel = classLevel,
        hitPointGain = HitPointGain.Fixed(1),
    )

    private fun characterClass(
        id: String,
        name: String,
    ): CharacterClass = CharacterClass(
        id = id,
        name = name,
        hitDie = 8,
        proficiencies = emptyList(),
        proficiencyChoices = emptyList(),
        savingThrows = emptyList(),
        startingEquipment = emptyList(),
        subclasses = emptyList(),
        levels = emptyList(),
    )

    private companion object {
        private const val TEST_CHARACTER_ID = "character-1"
    }
}
