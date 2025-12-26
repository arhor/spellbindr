package com.github.arhor.spellbindr.ui.feature.characters.sheet

import androidx.lifecycle.SavedStateHandle
import com.github.arhor.spellbindr.MainDispatcherRule
import com.github.arhor.spellbindr.domain.model.DamageType
import com.github.arhor.spellbindr.domain.model.EquipmentCategory
import com.github.arhor.spellbindr.domain.model.CharacterSheet
import com.github.arhor.spellbindr.domain.model.SpellSlotState
import com.github.arhor.spellbindr.domain.model.WeaponCatalogEntry
import com.github.arhor.spellbindr.domain.repository.FakeCharacterRepository
import com.github.arhor.spellbindr.domain.repository.FakeSpellsRepository
import com.github.arhor.spellbindr.domain.repository.FakeWeaponCatalogRepository
import com.github.arhor.spellbindr.domain.usecase.DeleteCharacterUseCase
import com.github.arhor.spellbindr.domain.usecase.LoadCharacterSheetUseCase
import com.github.arhor.spellbindr.domain.usecase.ObserveAllSpellsUseCase
import com.github.arhor.spellbindr.domain.usecase.ObserveWeaponCatalogUseCase
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

    @Test
    fun `selecting catalog weapon pre-fills editor fields`() = runTest(mainDispatcherRule.dispatcher) {
        val catalogEntry = WeaponCatalogEntry(
            id = "longsword",
            name = "Longsword",
            categories = setOf(EquipmentCategory.WEAPON, EquipmentCategory.MARTIAL),
            damageDiceCount = 1,
            damageDieSize = 8,
            damageType = DamageType.SLASHING,
        )
        val viewModel = createViewModel(
            sheet = CharacterSheet(id = "hero"),
            weaponCatalogEntries = listOf(catalogEntry),
        )
        advanceUntilIdle()

        viewModel.onAction(CharacterSheetViewModel.CharacterSheetUiAction.WeaponCatalogItemSelected("longsword"))
        val state = viewModel.awaitContentState {
            it.weaponEditorState?.catalogId == "longsword"
        }
        val editor = requireNotNull(state.weaponEditorState)
        assertThat(editor.name).isEqualTo("Longsword")
        assertThat(editor.category).isEqualTo(EquipmentCategory.MARTIAL)
        assertThat(editor.damageDiceCount).isEqualTo("1")
        assertThat(editor.damageDieSize).isEqualTo("8")
        assertThat(editor.damageType).isEqualTo(DamageType.SLASHING)
    }

    @Test
    fun `custom weapon still saves without catalogId`() = runTest(mainDispatcherRule.dispatcher) {
        val viewModel = createViewModel(CharacterSheet(id = "hero"))
        advanceUntilIdle()

        viewModel.onAction(CharacterSheetViewModel.CharacterSheetUiAction.AddWeaponClicked)
        viewModel.onAction(CharacterSheetViewModel.CharacterSheetUiAction.WeaponNameChanged("Custom Blade"))
        viewModel.onAction(CharacterSheetViewModel.CharacterSheetUiAction.WeaponSaved)

        val state = viewModel.awaitContentState { it.weapons.weapons.firstOrNull()?.name == "Custom Blade" }
        val savedWeaponId = state.weapons.weapons.first().id
        viewModel.onAction(CharacterSheetViewModel.CharacterSheetUiAction.WeaponSelected(savedWeaponId))
        val editorState = viewModel.awaitContentState { it.weaponEditorState?.id == savedWeaponId }.weaponEditorState
        assertThat(editorState?.catalogId).isNull()
    }

    @Test
    fun `editing after selection retains catalogId but updates fields`() = runTest(mainDispatcherRule.dispatcher) {
        val catalogEntry = WeaponCatalogEntry(
            id = "shortbow",
            name = "Shortbow",
            categories = setOf(EquipmentCategory.WEAPON, EquipmentCategory.SIMPLE, EquipmentCategory.RANGED),
            damageDiceCount = 1,
            damageDieSize = 6,
            damageType = DamageType.PIERCING,
        )
        val viewModel = createViewModel(
            sheet = CharacterSheet(id = "hero"),
            weaponCatalogEntries = listOf(catalogEntry),
        )
        advanceUntilIdle()

        viewModel.onAction(CharacterSheetViewModel.CharacterSheetUiAction.WeaponCatalogItemSelected("shortbow"))
        viewModel.onAction(CharacterSheetViewModel.CharacterSheetUiAction.WeaponNameChanged("Custom Shortbow"))
        viewModel.onAction(CharacterSheetViewModel.CharacterSheetUiAction.WeaponDieSizeChanged("8"))
        viewModel.onAction(CharacterSheetViewModel.CharacterSheetUiAction.WeaponSaved)

        val state = viewModel.awaitContentState { it.weapons.weapons.any { weapon -> weapon.name == "Custom Shortbow" } }
        val savedWeapon = state.weapons.weapons.first { it.name == "Custom Shortbow" }
        assertThat(savedWeapon.damageLabel).contains("1d8")
        viewModel.onAction(CharacterSheetViewModel.CharacterSheetUiAction.WeaponSelected(savedWeapon.id))
        val editorState = viewModel.awaitContentState { it.weaponEditorState?.id == savedWeapon.id }.weaponEditorState
        assertThat(editorState?.catalogId).isEqualTo("shortbow")
        assertThat(editorState?.name).isEqualTo("Custom Shortbow")
        assertThat(editorState?.damageDieSize).isEqualTo("8")
    }
}

private suspend fun CharacterSheetViewModel.awaitContentState(
    predicate: (CharacterSheetUiState.Content) -> Boolean = { true },
): CharacterSheetUiState.Content =
    uiState.first { state ->
        state is CharacterSheetUiState.Content && predicate(state)
    } as CharacterSheetUiState.Content

private fun TestScope.createViewModel(
    sheet: CharacterSheet,
    weaponCatalogEntries: List<WeaponCatalogEntry> = emptyList(),
): CharacterSheetViewModel {
    val characterRepository = FakeCharacterRepository(initialSheets = listOf(sheet))
    val spellsRepository = FakeSpellsRepository()
    val weaponCatalogRepository = FakeWeaponCatalogRepository(weaponCatalogEntries)
    return CharacterSheetViewModel(
        deleteCharacterUseCase = DeleteCharacterUseCase(characterRepository),
        loadCharacterSheetUseCase = LoadCharacterSheetUseCase(characterRepository),
        observeAllSpellsUseCase = ObserveAllSpellsUseCase(spellsRepository),
        observeWeaponCatalogUseCase = ObserveWeaponCatalogUseCase(weaponCatalogRepository),
        saveCharacterSheetUseCase = SaveCharacterSheetUseCase(characterRepository),
        updateHitPointsUseCase = UpdateHitPointsUseCase(),
        toggleSpellSlotUseCase = ToggleSpellSlotUseCase(),
        updateWeaponListUseCase = UpdateWeaponListUseCase(),
        savedStateHandle = SavedStateHandle(mapOf("characterId" to sheet.id)),
    )
}
