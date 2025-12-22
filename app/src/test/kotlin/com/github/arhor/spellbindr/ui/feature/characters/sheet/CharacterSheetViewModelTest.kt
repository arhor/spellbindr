package com.github.arhor.spellbindr.ui.feature.characters.sheet

import androidx.lifecycle.SavedStateHandle
import com.github.arhor.spellbindr.MainDispatcherRule
import com.github.arhor.spellbindr.domain.model.CharacterSheet
import com.github.arhor.spellbindr.domain.model.SpellSlotState
import com.github.arhor.spellbindr.domain.repository.FakeCharacterRepository
import com.github.arhor.spellbindr.domain.repository.FakeSpellsRepository
import com.github.arhor.spellbindr.domain.usecase.DeleteCharacterUseCase
import com.github.arhor.spellbindr.domain.usecase.LoadCharacterSheetUseCase
import com.github.arhor.spellbindr.domain.usecase.ObserveAllSpellsUseCase
import com.github.arhor.spellbindr.domain.usecase.SaveCharacterSheetUseCase
import com.github.arhor.spellbindr.domain.usecase.ToggleSpellSlotUseCase
import com.github.arhor.spellbindr.domain.usecase.UpdateHitPointsUseCase
import com.github.arhor.spellbindr.domain.usecase.UpdateWeaponListUseCase
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class CharacterSheetViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun `adjust current hp updates sheet state`() = runTest(mainDispatcherRule.dispatcher) {
        val sheet = CharacterSheet(id = "hero", maxHitPoints = 12, currentHitPoints = 7)
        val viewModel = createViewModel(sheet)
        advanceUntilIdle()

        viewModel.onAction(CharacterSheetViewModel.CharacterSheetUiAction.AdjustCurrentHp(-4))
        val state = viewModel.awaitContentState { it.header.hitPoints.current == 3 }
        assertThat(state.header.hitPoints.current).isEqualTo(3)
    }

    @Test
    fun `toggle spell slot updates expended count`() = runTest(mainDispatcherRule.dispatcher) {
        val sheet = CharacterSheet(
            id = "hero",
            spellSlots = listOf(SpellSlotState(level = 1, total = 2, expended = 0)),
        )
        val viewModel = createViewModel(sheet)
        advanceUntilIdle()

        viewModel.onAction(CharacterSheetViewModel.CharacterSheetUiAction.SpellSlotToggled(level = 1, slotIndex = 0))
        val state = viewModel.awaitContentState {
            it.spells.spellLevels.first { level -> level.level == 1 }.spellSlot?.expended == 1
        }
        val levelOneSlot = state.spells.spellLevels.first { it.level == 1 }.spellSlot
        assertThat(levelOneSlot?.expended).isEqualTo(1)
    }

    @Test
    fun `weapon edits add weapon to sheet`() = runTest(mainDispatcherRule.dispatcher) {
        val sheet = CharacterSheet(id = "hero")
        val viewModel = createViewModel(sheet)
        advanceUntilIdle()

        viewModel.onAction(CharacterSheetViewModel.CharacterSheetUiAction.AddWeaponClicked)
        viewModel.onAction(CharacterSheetViewModel.CharacterSheetUiAction.WeaponNameChanged("Longsword"))
        viewModel.onAction(CharacterSheetViewModel.CharacterSheetUiAction.WeaponSaved)
        val state = viewModel.awaitContentState { it.weapons.weapons.firstOrNull()?.name == "Longsword" }
        assertThat(state.weapons.weapons).hasSize(1)
        assertThat(state.weapons.weapons.first().name).isEqualTo("Longsword")
    }
}

private suspend fun CharacterSheetViewModel.awaitContentState(
    predicate: (CharacterSheetUiState.Content) -> Boolean = { true },
): CharacterSheetUiState.Content =
    uiState.first { state ->
        state is CharacterSheetUiState.Content && predicate(state)
    } as CharacterSheetUiState.Content

private fun TestScope.createViewModel(sheet: CharacterSheet): CharacterSheetViewModel {
    val characterRepository = FakeCharacterRepository(initialSheets = listOf(sheet))
    val spellsRepository = FakeSpellsRepository()
    return CharacterSheetViewModel(
        deleteCharacterUseCase = DeleteCharacterUseCase(characterRepository),
        loadCharacterSheetUseCase = LoadCharacterSheetUseCase(characterRepository),
        observeAllSpellsUseCase = ObserveAllSpellsUseCase(spellsRepository),
        saveCharacterSheetUseCase = SaveCharacterSheetUseCase(characterRepository),
        updateHitPointsUseCase = UpdateHitPointsUseCase(),
        toggleSpellSlotUseCase = ToggleSpellSlotUseCase(),
        updateWeaponListUseCase = UpdateWeaponListUseCase(),
        savedStateHandle = SavedStateHandle(mapOf("characterId" to sheet.id)),
    )
}
