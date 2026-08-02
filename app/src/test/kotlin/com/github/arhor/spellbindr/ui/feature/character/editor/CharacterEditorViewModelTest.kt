package com.github.arhor.spellbindr.ui.feature.character.editor

import androidx.lifecycle.SavedStateHandle
import com.github.arhor.spellbindr.MainDispatcherRule
import com.github.arhor.spellbindr.domain.model.AbilityIds
import com.github.arhor.spellbindr.domain.model.CharacterProgression
import com.github.arhor.spellbindr.domain.model.CharacterSheet
import com.github.arhor.spellbindr.domain.model.CharacterWithProgression
import com.github.arhor.spellbindr.domain.model.ProgressionOrigin
import com.github.arhor.spellbindr.domain.model.ProgressionState
import com.github.arhor.spellbindr.domain.model.Skill
import com.github.arhor.spellbindr.domain.usecase.BuildCharacterSheetFromInputsUseCase
import com.github.arhor.spellbindr.domain.usecase.ComputeDerivedBonusesUseCase
import com.github.arhor.spellbindr.domain.usecase.LoadCharacterWithProgressionUseCase
import com.github.arhor.spellbindr.domain.usecase.ValidateCharacterSheetUseCase
import com.google.common.truth.Truth.assertThat
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test

class CharacterEditorViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun `dispatch should update name in content state when name changes`() {
        // Given
        val vm = CharacterEditorViewModel(
            loadCharacterWithProgressionUseCase = mockk(relaxed = true),
            saveCharacterSheetUseCase = mockk(relaxed = true),
            validateCharacterSheetUseCase = mockk<ValidateCharacterSheetUseCase>(relaxed = true),
            computeDerivedBonusesUseCase = mockk<ComputeDerivedBonusesUseCase>(relaxed = true),
            buildCharacterSheetFromInputsUseCase = mockk<BuildCharacterSheetFromInputsUseCase>(relaxed = true),
            savedStateHandle = SavedStateHandle(),
        )

        // When
        vm.dispatch(CharacterEditorIntent.NameChanged("Astra"))

        // Then
        val state = vm.uiState.value as CharacterEditorUiState.Content
        assertThat(state.name).isEqualTo("Astra")
    }

    @Test
    fun `dispatch should retain progression-owned values when managed character is edited`() = runTest {
        // Given
        val loadCharacterWithProgressionUseCase = mockk<LoadCharacterWithProgressionUseCase>()
        every { loadCharacterWithProgressionUseCase("character-1") } returns flowOf(
            CharacterWithProgression(
                sheet = CharacterSheet(
                    id = "character-1",
                    className = "Wizard",
                    level = 3,
                    proficiencyBonus = 2,
                    maxHitPoints = 14,
                    hitDice = "3d6",
                ),
                progressionState = ProgressionState.Managed(
                    CharacterProgression(
                        referenceDataVersion = "test",
                        origin = ProgressionOrigin.Guided,
                        levels = emptyList(),
                    ),
                ),
            ),
        )
        val vm = CharacterEditorViewModel(
            loadCharacterWithProgressionUseCase = loadCharacterWithProgressionUseCase,
            saveCharacterSheetUseCase = mockk(relaxed = true),
            validateCharacterSheetUseCase = mockk(relaxed = true),
            computeDerivedBonusesUseCase = ComputeDerivedBonusesUseCase(),
            buildCharacterSheetFromInputsUseCase = mockk(relaxed = true),
            savedStateHandle = SavedStateHandle(mapOf("characterId" to "character-1")),
        )
        advanceUntilIdle()

        // When
        vm.dispatch(CharacterEditorIntent.ClassChanged("Fighter"))
        vm.dispatch(CharacterEditorIntent.LevelChanged("4"))
        vm.dispatch(CharacterEditorIntent.MaxHpChanged("99"))
        vm.dispatch(CharacterEditorIntent.HitDiceChanged("4d10"))
        vm.dispatch(CharacterEditorIntent.AbilityChanged(AbilityIds.STR, "20"))
        vm.dispatch(CharacterEditorIntent.ProficiencyBonusChanged("8"))
        vm.dispatch(CharacterEditorIntent.SavingThrowProficiencyChanged(AbilityIds.STR, true))
        vm.dispatch(CharacterEditorIntent.SkillProficiencyChanged(Skill.ATHLETICS, true))
        vm.dispatch(CharacterEditorIntent.NameChanged("Astra"))
        vm.dispatch(CharacterEditorIntent.NotesChanged("Still editable"))

        // Then
        val state = vm.uiState.value as CharacterEditorUiState.Content
        assertThat(state.isProgressionManaged).isTrue()
        assertThat(state.className).isEqualTo("Wizard")
        assertThat(state.level).isEqualTo("3")
        assertThat(state.maxHitPoints).isEqualTo("14")
        assertThat(state.hitDice).isEqualTo("3d6")
        assertThat(state.abilities.first { it.abilityId == AbilityIds.STR }.score).isEqualTo("10")
        assertThat(state.proficiencyBonus).isEqualTo("2")
        assertThat(state.savingThrows.first { it.abilityId == AbilityIds.STR }.proficient).isFalse()
        assertThat(state.skills.first { it.skill == Skill.ATHLETICS }.proficient).isFalse()
        assertThat(state.name).isEqualTo("Astra")
        assertThat(state.notes).isEqualTo("Still editable")
    }
}
