package com.github.arhor.spellbindr.ui.feature.characters.editor

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.github.arhor.spellbindr.MainDispatcherRule
import com.github.arhor.spellbindr.domain.model.AbilityIds
import com.github.arhor.spellbindr.domain.model.CharacterSheet
import com.github.arhor.spellbindr.domain.model.Skill
import com.github.arhor.spellbindr.domain.repository.FakeCharacterRepository
import com.github.arhor.spellbindr.domain.usecase.BuildCharacterSheetFromInputsUseCase
import com.github.arhor.spellbindr.domain.usecase.ComputeDerivedBonusesUseCase
import com.github.arhor.spellbindr.domain.usecase.LoadCharacterSheetUseCase
import com.github.arhor.spellbindr.domain.usecase.SaveCharacterSheetUseCase
import com.github.arhor.spellbindr.domain.usecase.ValidateCharacterSheetUseCase
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.async
import kotlinx.coroutines.cancel
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test

class CharacterEditorViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun `state should load character sheet when editing existing id`() = runTest(mainDispatcherRule.dispatcher) {
        // Given
        val sheet = CharacterSheet(id = "hero", name = "Existing", maxHitPoints = 12)
        val repository = FakeCharacterRepository(initialSheets = listOf(sheet))
        val viewModel = createViewModel(
            repository = repository,
            savedStateHandle = SavedStateHandle(mapOf("characterId" to sheet.id)),
        )

        // When
        val initialState = viewModel.uiState.value
        advanceUntilIdle()
        val loadedState = viewModel.uiState.value as CharacterEditorUiState.Content

        // Then
        assertThat(initialState).isEqualTo(CharacterEditorUiState.Loading)
        assertThat(loadedState.name).isEqualTo("Existing")
        assertThat(loadedState.mode).isEqualTo(EditorMode.Edit)
        viewModel.viewModelScope.cancel()
    }

    @Test
    fun `updating abilities and proficiencies should refresh derived bonuses`() = runTest(mainDispatcherRule.dispatcher) {
        // Given
        val viewModel = createViewModel()

        // When
        viewModel.onAbilityChanged(AbilityIds.STR, "14")
        val afterAbilityChange = viewModel.uiState.value as CharacterEditorUiState.Content

        viewModel.onSkillProficiencyChanged(Skill.ATHLETICS, true)
        val afterProficiencyChange = viewModel.uiState.value as CharacterEditorUiState.Content

        // Then
        val savingThrow = afterAbilityChange.savingThrows.first { it.abilityId == AbilityIds.STR }
        val athleticsWithoutProficiency = afterAbilityChange.skills.first { it.skill == Skill.ATHLETICS }
        val athleticsWithProficiency = afterProficiencyChange.skills.first { it.skill == Skill.ATHLETICS }

        assertThat(savingThrow.bonus).isEqualTo(2)
        assertThat(athleticsWithoutProficiency.bonus).isEqualTo(2)
        assertThat(athleticsWithProficiency.bonus).isEqualTo(4)
        viewModel.viewModelScope.cancel()
    }

    @Test
    fun `input handlers should clear validation errors`() = runTest(mainDispatcherRule.dispatcher) {
        // Given
        val viewModel = createViewModel()

        // When
        viewModel.onAbilityChanged(AbilityIds.STR, "")
        viewModel.onSaveClicked()
        val erroredState = viewModel.uiState.value as CharacterEditorUiState.Content

        viewModel.onNameChanged("Aria")
        viewModel.onAbilityChanged(AbilityIds.STR, "14")
        val correctedState = viewModel.uiState.value as CharacterEditorUiState.Content

        // Then
        val strengthField = erroredState.abilities.first { it.abilityId == AbilityIds.STR }
        val correctedStrengthField = correctedState.abilities.first { it.abilityId == AbilityIds.STR }

        assertThat(erroredState.nameError).isEqualTo("Required")
        assertThat(strengthField.error).isEqualTo("Required")
        assertThat(correctedState.nameError).isNull()
        assertThat(correctedStrengthField.error).isNull()
        viewModel.viewModelScope.cancel()
    }

    @Test
    fun `saving valid input should persist sheet and emit completion event`() = runTest(mainDispatcherRule.dispatcher) {
        // Given
        val repository = FakeCharacterRepository()
        val viewModel = createViewModel(repository = repository)
        viewModel.onNameChanged("Aria")

        // When
        val eventDeferred = async { viewModel.events.first() }
        viewModel.onSaveClicked()
        advanceUntilIdle()
        val event = eventDeferred.await()
        val state = viewModel.uiState.value as CharacterEditorUiState.Content

        // Then
        val savedSheet = repository.observeCharacterSheet(requireNotNull(state.characterId)).first()
        assertThat(event).isEqualTo(CharacterEditorEvent.Saved)
        assertThat(state.mode).isEqualTo(EditorMode.Edit)
        assertThat(savedSheet?.name).isEqualTo("Aria")
        viewModel.viewModelScope.cancel()
    }
}

private fun createViewModel(
    repository: FakeCharacterRepository = FakeCharacterRepository(),
    savedStateHandle: SavedStateHandle = SavedStateHandle(),
): CharacterEditorViewModel {
    val loadCharacterSheetUseCase = LoadCharacterSheetUseCase(repository)
    val saveCharacterSheetUseCase = SaveCharacterSheetUseCase(repository)
    val validateCharacterSheetUseCase = ValidateCharacterSheetUseCase()
    val computeDerivedBonusesUseCase = ComputeDerivedBonusesUseCase()
    val buildCharacterSheetFromInputsUseCase = BuildCharacterSheetFromInputsUseCase()

    return CharacterEditorViewModel(
        loadCharacterSheetUseCase = loadCharacterSheetUseCase,
        saveCharacterSheetUseCase = saveCharacterSheetUseCase,
        validateCharacterSheetUseCase = validateCharacterSheetUseCase,
        computeDerivedBonusesUseCase = computeDerivedBonusesUseCase,
        buildCharacterSheetFromInputsUseCase = buildCharacterSheetFromInputsUseCase,
        savedStateHandle = savedStateHandle,
    )
}
