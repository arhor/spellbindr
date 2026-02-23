package com.github.arhor.spellbindr.ui.feature.character.sheet

import androidx.lifecycle.SavedStateHandle
import com.github.arhor.spellbindr.MainDispatcherRule
import com.github.arhor.spellbindr.domain.model.CharacterSheet
import com.github.arhor.spellbindr.domain.model.Loadable
import com.github.arhor.spellbindr.domain.usecase.DeleteCharacterUseCase
import com.github.arhor.spellbindr.domain.usecase.LoadCharacterSheetUseCase
import com.github.arhor.spellbindr.domain.usecase.ObserveAllSpellsUseCase
import com.github.arhor.spellbindr.domain.usecase.ObserveSpellcastingClassesUseCase
import com.github.arhor.spellbindr.domain.usecase.ObserveWeaponCatalogUseCase
import com.github.arhor.spellbindr.domain.usecase.SaveCharacterSheetUseCase
import com.github.arhor.spellbindr.domain.usecase.ToggleSpellSlotUseCase
import com.github.arhor.spellbindr.domain.usecase.UpdateHitPointsUseCase
import com.github.arhor.spellbindr.domain.usecase.UpdateWeaponListUseCase
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
    fun `persist should debounce consecutive updates and save latest sheet only`() = runTest {
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

        vm.adjustCurrentHp(-1)
        vm.adjustCurrentHp(-1)
        vm.adjustCurrentHp(+1)

        advanceTimeBy(149)
        runCurrent()
        coVerify(exactly = 0) { saveCharacterSheetUseCase(any()) }

        advanceTimeBy(1)
        advanceUntilIdle()

        val captured = mutableListOf<CharacterSheet>()
        coVerify(exactly = 1) { saveCharacterSheetUseCase(capture(captured)) }
        assertThat(captured.single().currentHitPoints).isEqualTo(4)
    }

    @Test
    fun `weapon catalog failure should be exposed in ui state error message`() = runTest {
        val vm = createViewModel(
            initialSheet = CharacterSheet(id = TEST_CHARACTER_ID),
            weaponCatalogState = Loadable.Failure(errorMessage = "boom"),
        )

        advanceUntilIdle()

        val state = vm.uiState.value as CharacterSheetUiState.Content
        assertThat(state.errorMessage).isEqualTo("Unable to load weapon catalog")
    }

    private fun createViewModel(
        initialSheet: CharacterSheet,
        saveCharacterSheetUseCase: SaveCharacterSheetUseCase = mockk(relaxed = true),
        weaponCatalogState: Loadable<List<com.github.arhor.spellbindr.domain.model.WeaponCatalogEntry>>,
    ): CharacterSheetViewModel {
        val deleteCharacterUseCase = mockk<DeleteCharacterUseCase>(relaxed = true)
        val loadCharacterSheetUseCase = mockk<LoadCharacterSheetUseCase>()
        val observeAllSpellsUseCase = mockk<ObserveAllSpellsUseCase>()
        val observeSpellcastingClassesUseCase = mockk<ObserveSpellcastingClassesUseCase>()
        val observeWeaponCatalogUseCase = mockk<ObserveWeaponCatalogUseCase>()

        every { loadCharacterSheetUseCase(TEST_CHARACTER_ID) } returns flowOf(initialSheet)
        every { observeAllSpellsUseCase() } returns flowOf(Loadable.Content(emptyList()))
        every { observeSpellcastingClassesUseCase() } returns flowOf(Loadable.Content(emptyList()))
        every { observeWeaponCatalogUseCase() } returns flowOf(weaponCatalogState)

        return CharacterSheetViewModel(
            deleteCharacterUseCase = deleteCharacterUseCase,
            loadCharacterSheetUseCase = loadCharacterSheetUseCase,
            observeAllSpellsUseCase = observeAllSpellsUseCase,
            observeSpellcastingClassesUseCase = observeSpellcastingClassesUseCase,
            observeWeaponCatalogUseCase = observeWeaponCatalogUseCase,
            saveCharacterSheetUseCase = saveCharacterSheetUseCase,
            updateHitPointsUseCase = UpdateHitPointsUseCase(),
            toggleSpellSlotUseCase = ToggleSpellSlotUseCase(),
            updateWeaponListUseCase = UpdateWeaponListUseCase(),
            savedStateHandle = SavedStateHandle(mapOf("characterId" to TEST_CHARACTER_ID)),
        )
    }

    private companion object {
        private const val TEST_CHARACTER_ID = "character-1"
    }
}
