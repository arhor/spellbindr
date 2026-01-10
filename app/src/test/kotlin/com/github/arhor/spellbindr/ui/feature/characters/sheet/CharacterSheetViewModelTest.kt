package com.github.arhor.spellbindr.ui.feature.characters.sheet

import androidx.lifecycle.SavedStateHandle
import com.github.arhor.spellbindr.MainDispatcherRule
import com.github.arhor.spellbindr.domain.model.CharacterSheet
import com.github.arhor.spellbindr.domain.model.Cost
import com.github.arhor.spellbindr.domain.model.Damage
import com.github.arhor.spellbindr.domain.model.DamageType
import com.github.arhor.spellbindr.domain.model.EntityRef
import com.github.arhor.spellbindr.domain.model.Equipment
import com.github.arhor.spellbindr.domain.model.EquipmentCategory
import com.github.arhor.spellbindr.domain.model.Loadable
import com.github.arhor.spellbindr.domain.model.SpellSlotState
import com.github.arhor.spellbindr.domain.repository.EquipmentRepository
import com.github.arhor.spellbindr.domain.repository.FakeCharacterRepository
import com.github.arhor.spellbindr.domain.repository.FakeSpellsRepository
import com.github.arhor.spellbindr.domain.usecase.DeleteCharacterUseCase
import com.github.arhor.spellbindr.domain.usecase.LoadCharacterSheetUseCase
import com.github.arhor.spellbindr.domain.usecase.ObserveAllSpellsUseCase
import com.github.arhor.spellbindr.domain.usecase.ObserveWeaponCatalogUseCase
import com.github.arhor.spellbindr.domain.usecase.SaveCharacterSheetUseCase
import com.github.arhor.spellbindr.domain.usecase.ToggleSpellSlotUseCase
import com.github.arhor.spellbindr.domain.usecase.UpdateHitPointsUseCase
import com.github.arhor.spellbindr.domain.usecase.UpdateWeaponListUseCase
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test

class CharacterSheetViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun `adjustCurrentHp should update header hit points`() = runTest(mainDispatcherRule.dispatcher) {
        val sheet = CharacterSheet(id = "hero", maxHitPoints = 12, currentHitPoints = 7)
        val viewModel = createViewModel(sheet)
        advanceUntilIdle()

        viewModel.adjustCurrentHp(-4)
        val state = viewModel.awaitContentState { it.header.hitPoints.current == 3 }

        assertThat(state.header.hitPoints.current).isEqualTo(3)
    }

    @Test
    fun `toggleSpellSlot should update expended slot count`() = runTest(mainDispatcherRule.dispatcher) {
        val sheet = CharacterSheet(
            id = "hero",
            spellSlots = listOf(SpellSlotState(level = 1, total = 2, expended = 0)),
        )
        val viewModel = createViewModel(sheet)
        advanceUntilIdle()

        viewModel.toggleSpellSlot(level = 1, slotIndex = 0)
        val state = viewModel.awaitContentState {
            it.spells.spellLevels.first { level -> level.level == 1 }.spellSlot?.expended == 1
        }
        val levelOneSlot = state.spells.spellLevels.first { it.level == 1 }.spellSlot

        assertThat(levelOneSlot?.expended).isEqualTo(1)
    }

    @Test
    fun `saveWeapon should add new weapon to sheet`() = runTest(mainDispatcherRule.dispatcher) {
        val viewModel = createViewModel(CharacterSheet(id = "hero"))
        advanceUntilIdle()

        viewModel.startNewWeapon()
        viewModel.setWeaponName("Longsword")
        viewModel.saveWeapon()
        val state = viewModel.awaitContentState { it.weapons.weapons.firstOrNull()?.name == "Longsword" }

        assertThat(state.weapons.weapons).hasSize(1)
        assertThat(state.weapons.weapons.first().name).isEqualTo("Longsword")
    }

    @Test
    fun `selectWeaponFromCatalog should prefill editor`() = runTest(mainDispatcherRule.dispatcher) {
        val catalogEntry = Equipment(
            id = "longsword",
            name = "Longsword",
            cost = Cost(quantity = 0, unit = "gp"),
            damage = Damage(damageDice = "1d8", damageType = EntityRef("slashing")),
            categories = setOf(EquipmentCategory.WEAPON, EquipmentCategory.MARTIAL),
        )
        val viewModel = createViewModel(
            sheet = CharacterSheet(id = "hero"),
            weaponCatalogEntries = listOf(catalogEntry),
        )
        advanceUntilIdle()

        viewModel.selectWeaponFromCatalog("longsword")
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
    fun `saveWeapon should retain catalog id when editing existing weapon`() = runTest(mainDispatcherRule.dispatcher) {
        val catalogEntry = Equipment(
            id = "shortbow",
            name = "Shortbow",
            cost = Cost(quantity = 0, unit = "gp"),
            damage = Damage(damageDice = "1d6", damageType = EntityRef("piercing")),
            categories = setOf(EquipmentCategory.WEAPON, EquipmentCategory.SIMPLE, EquipmentCategory.RANGED),
        )
        val viewModel = createViewModel(
            sheet = CharacterSheet(id = "hero"),
            weaponCatalogEntries = listOf(catalogEntry),
        )
        advanceUntilIdle()

        viewModel.selectWeaponFromCatalog("shortbow")
        viewModel.setWeaponName("Custom Shortbow")
        viewModel.setWeaponDieSize("8")
        viewModel.saveWeapon()

        val state = viewModel.awaitContentState { it.weapons.weapons.any { weapon -> weapon.name == "Custom Shortbow" } }
        val savedWeapon = state.weapons.weapons.first { it.name == "Custom Shortbow" }
        viewModel.selectWeapon(savedWeapon.id)
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
    weaponCatalogEntries: List<Equipment> = emptyList(),
): CharacterSheetViewModel {
    val characterRepository = FakeCharacterRepository(initialSheets = listOf(sheet))
    val spellsRepository = FakeSpellsRepository()
    val equipmentRepository = FakeEquipmentRepository(weaponCatalogEntries)
    return CharacterSheetViewModel(
        deleteCharacterUseCase = DeleteCharacterUseCase(characterRepository),
        loadCharacterSheetUseCase = LoadCharacterSheetUseCase(characterRepository),
        observeAllSpellsUseCase = ObserveAllSpellsUseCase(spellsRepository),
        observeWeaponCatalogUseCase = ObserveWeaponCatalogUseCase(equipmentRepository),
        saveCharacterSheetUseCase = SaveCharacterSheetUseCase(characterRepository),
        updateHitPointsUseCase = UpdateHitPointsUseCase(),
        toggleSpellSlotUseCase = ToggleSpellSlotUseCase(),
        updateWeaponListUseCase = UpdateWeaponListUseCase(),
        savedStateHandle = SavedStateHandle(mapOf("characterId" to sheet.id)),
    )
}

private class FakeEquipmentRepository(
    initialEquipment: List<Equipment>,
) : EquipmentRepository {
    override val allEquipmentState: Flow<Loadable<List<Equipment>>> =
        MutableStateFlow(Loadable.Content(initialEquipment))

    override suspend fun findEquipmentById(id: String): Equipment? = null
}
