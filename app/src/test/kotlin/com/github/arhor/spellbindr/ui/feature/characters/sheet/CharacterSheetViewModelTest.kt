//package com.github.arhor.spellbindr.ui.feature.characters.sheet
//
//import androidx.lifecycle.SavedStateHandle
//import com.github.arhor.spellbindr.MainDispatcherRule
//import com.github.arhor.spellbindr.domain.model.CharacterSheet
//import com.github.arhor.spellbindr.domain.model.DamageType
//import com.github.arhor.spellbindr.domain.model.EquipmentCategory
//import com.github.arhor.spellbindr.domain.model.SpellSlotState
//import com.github.arhor.spellbindr.domain.model.WeaponCatalogEntry
//import com.github.arhor.spellbindr.domain.repository.FakeCharacterRepository
//import com.github.arhor.spellbindr.domain.repository.FakeSpellsRepository
//import com.github.arhor.spellbindr.domain.usecase.DeleteCharacterUseCase
//import com.github.arhor.spellbindr.domain.usecase.LoadCharacterSheetUseCase
//import com.github.arhor.spellbindr.domain.usecase.ObserveAllSpellsUseCase
//import com.github.arhor.spellbindr.domain.usecase.ObserveWeaponCatalogUseCase
//import com.github.arhor.spellbindr.domain.usecase.SaveCharacterSheetUseCase
//import com.github.arhor.spellbindr.domain.usecase.ToggleSpellSlotUseCase
//import com.github.arhor.spellbindr.domain.usecase.UpdateHitPointsUseCase
//import com.github.arhor.spellbindr.domain.usecase.UpdateWeaponListUseCase
//import com.google.common.truth.Truth.assertThat
//import kotlinx.coroutines.flow.first
//import kotlinx.coroutines.test.TestScope
//import kotlinx.coroutines.test.advanceUntilIdle
//import kotlinx.coroutines.test.runTest
//import org.junit.Rule
//import org.junit.Test
//
//class CharacterSheetViewModelTest {
//
//    @get:Rule
//    val mainDispatcherRule = MainDispatcherRule()
//
//    @Test
//    fun `onAction should adjust current hit points when value changes`() = runTest(mainDispatcherRule.dispatcher) {
//        // Given
//        val sheet = CharacterSheet(id = "hero", maxHitPoints = 12, currentHitPoints = 7)
//        val viewModel = createViewModel(sheet)
//        advanceUntilIdle()
//
//        // When
//        viewModel.onAction(CharacterSheetViewModel.CharacterSheetUiAction.AdjustCurrentHp(-4))
//        val state = viewModel.awaitContentState { it.header.hitPoints.current == 3 }
//
//        // Then
//        assertThat(state.header.hitPoints.current).isEqualTo(3)
//    }
//
//    @Test
//    fun `onAction should update expended spell slot count when toggled`() = runTest(mainDispatcherRule.dispatcher) {
//        // Given
//        val sheet = CharacterSheet(
//            id = "hero",
//            spellSlots = listOf(SpellSlotState(level = 1, total = 2, expended = 0)),
//        )
//        val viewModel = createViewModel(sheet)
//        advanceUntilIdle()
//
//        // When
//        viewModel.onAction(CharacterSheetViewModel.CharacterSheetUiAction.SpellSlotToggled(level = 1, slotIndex = 0))
//        val state = viewModel.awaitContentState {
//            it.spells.spellLevels.first { level -> level.level == 1 }.spellSlot?.expended == 1
//        }
//        val levelOneSlot = state.spells.spellLevels.first { it.level == 1 }.spellSlot
//
//        // Then
//        assertThat(levelOneSlot?.expended).isEqualTo(1)
//    }
//
//    @Test
//    fun `onAction should add weapon to sheet when editor is saved`() = runTest(mainDispatcherRule.dispatcher) {
//        // Given
//        val sheet = CharacterSheet(id = "hero")
//        val viewModel = createViewModel(sheet)
//        advanceUntilIdle()
//
//        // When
//        viewModel.onAction(CharacterSheetViewModel.CharacterSheetUiAction.AddWeaponClicked)
//        viewModel.onAction(CharacterSheetViewModel.CharacterSheetUiAction.WeaponNameChanged("Longsword"))
//        viewModel.onAction(CharacterSheetViewModel.CharacterSheetUiAction.WeaponSaved)
//        val state = viewModel.awaitContentState { it.weapons.weapons.firstOrNull()?.name == "Longsword" }
//
//        // Then
//        assertThat(state.weapons.weapons).hasSize(1)
//        assertThat(state.weapons.weapons.first().name).isEqualTo("Longsword")
//    }
//
//    @Test
//    fun `onAction should prefill editor when catalog weapon is selected`() = runTest(mainDispatcherRule.dispatcher) {
//        // Given
//        val catalogEntry = WeaponCatalogEntry(
//            id = "longsword",
//            name = "Longsword",
//            categories = setOf(EquipmentCategory.WEAPON, EquipmentCategory.MARTIAL),
//            damageDiceNum = 1,
//            damageDieSize = 8,
//            damageType = DamageType.SLASHING,
//        )
//        val viewModel = createViewModel(
//            sheet = CharacterSheet(id = "hero"),
//            weaponCatalogEntries = listOf(catalogEntry),
//        )
//        advanceUntilIdle()
//
//        // When
//        viewModel.onAction(CharacterSheetViewModel.CharacterSheetUiAction.WeaponCatalogItemSelected("longsword"))
//        val state = viewModel.awaitContentState {
//            it.weaponEditorState?.catalogId == "longsword"
//        }
//        val editor = requireNotNull(state.weaponEditorState)
//
//        // Then
//        assertThat(editor.name).isEqualTo("Longsword")
//        assertThat(editor.category).isEqualTo(EquipmentCategory.MARTIAL)
//        assertThat(editor.damageDiceCount).isEqualTo("1")
//        assertThat(editor.damageDieSize).isEqualTo("8")
//        assertThat(editor.damageType).isEqualTo(DamageType.SLASHING)
//    }
//
//    @Test
//    fun `onAction should save custom weapon when catalog id is absent`() = runTest(mainDispatcherRule.dispatcher) {
//        // Given
//        val viewModel = createViewModel(CharacterSheet(id = "hero"))
//        advanceUntilIdle()
//
//        // When
//        viewModel.onAction(CharacterSheetViewModel.CharacterSheetUiAction.AddWeaponClicked)
//        viewModel.onAction(CharacterSheetViewModel.CharacterSheetUiAction.WeaponNameChanged("Custom Blade"))
//        viewModel.onAction(CharacterSheetViewModel.CharacterSheetUiAction.WeaponSaved)
//
//        val state = viewModel.awaitContentState { it.weapons.weapons.firstOrNull()?.name == "Custom Blade" }
//        val savedWeaponId = state.weapons.weapons.first().id
//        viewModel.onAction(CharacterSheetViewModel.CharacterSheetUiAction.WeaponSelected(savedWeaponId))
//        val editorState = viewModel.awaitContentState { it.weaponEditorState?.id == savedWeaponId }.weaponEditorState
//
//        // Then
//        assertThat(editorState?.catalogId).isNull()
//    }
//
//    @Test
//    fun `onAction should retain catalog id but update fields when editing selected weapon`() = runTest(mainDispatcherRule.dispatcher) {
//        // Given
//        val catalogEntry = WeaponCatalogEntry(
//            id = "shortbow",
//            name = "Shortbow",
//            categories = setOf(EquipmentCategory.WEAPON, EquipmentCategory.SIMPLE, EquipmentCategory.RANGED),
//            damageDiceNum = 1,
//            damageDieSize = 6,
//            damageType = DamageType.PIERCING,
//        )
//        val viewModel = createViewModel(
//            sheet = CharacterSheet(id = "hero"),
//            weaponCatalogEntries = listOf(catalogEntry),
//        )
//        advanceUntilIdle()
//
//        // When
//        viewModel.onAction(CharacterSheetViewModel.CharacterSheetUiAction.WeaponCatalogItemSelected("shortbow"))
//        viewModel.onAction(CharacterSheetViewModel.CharacterSheetUiAction.WeaponNameChanged("Custom Shortbow"))
//        viewModel.onAction(CharacterSheetViewModel.CharacterSheetUiAction.WeaponDieSizeChanged("8"))
//        viewModel.onAction(CharacterSheetViewModel.CharacterSheetUiAction.WeaponSaved)
//
//        val state = viewModel.awaitContentState { it.weapons.weapons.any { weapon -> weapon.name == "Custom Shortbow" } }
//        val savedWeapon = state.weapons.weapons.first { it.name == "Custom Shortbow" }
//        assertThat(savedWeapon.damageLabel).contains("1d8")
//        viewModel.onAction(CharacterSheetViewModel.CharacterSheetUiAction.WeaponSelected(savedWeapon.id))
//        val editorState = viewModel.awaitContentState { it.weaponEditorState?.id == savedWeapon.id }.weaponEditorState
//
//        // Then
//        assertThat(editorState?.catalogId).isEqualTo("shortbow")
//        assertThat(editorState?.name).isEqualTo("Custom Shortbow")
//        assertThat(editorState?.damageDieSize).isEqualTo("8")
//    }
//}
//
//private suspend fun CharacterSheetViewModel.awaitContentState(
//    predicate: (CharacterSheetUiState.Content) -> Boolean = { true },
//): CharacterSheetUiState.Content =
//    uiState.first { state ->
//        state is CharacterSheetUiState.Content && predicate(state)
//    } as CharacterSheetUiState.Content
//
//private fun TestScope.createViewModel(
//    sheet: CharacterSheet,
//    weaponCatalogEntries: List<WeaponCatalogEntry> = emptyList(),
//): CharacterSheetViewModel {
//    val characterRepository = FakeCharacterRepository(initialSheets = listOf(sheet))
//    val spellsRepository = FakeSpellsRepository()
//    return CharacterSheetViewModel(
//        deleteCharacterUseCase = DeleteCharacterUseCase(characterRepository),
//        loadCharacterSheetUseCase = LoadCharacterSheetUseCase(characterRepository),
//        observeAllSpellsUseCase = ObserveAllSpellsUseCase(spellsRepository),
//        observeWeaponCatalogUseCase = ObserveWeaponCatalogUseCase(weaponCatalogRepository),
//        saveCharacterSheetUseCase = SaveCharacterSheetUseCase(characterRepository),
//        updateHitPointsUseCase = UpdateHitPointsUseCase(),
//        toggleSpellSlotUseCase = ToggleSpellSlotUseCase(),
//        updateWeaponListUseCase = UpdateWeaponListUseCase(),
//        savedStateHandle = SavedStateHandle(mapOf("characterId" to sheet.id)),
//    )
//}
