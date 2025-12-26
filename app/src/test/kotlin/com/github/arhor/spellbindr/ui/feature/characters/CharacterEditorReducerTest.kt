package com.github.arhor.spellbindr.ui.feature.characters

import com.github.arhor.spellbindr.domain.model.AbilityIds
import com.github.arhor.spellbindr.domain.model.Skill
import com.github.arhor.spellbindr.domain.usecase.ComputeDerivedBonusesUseCase
import com.google.common.truth.Truth.assertThat
import org.junit.Test

class CharacterEditorReducerTest {

    private val computeDerivedBonusesUseCase = ComputeDerivedBonusesUseCase()

    @Test
    fun `reduceCharacterEditorState should update fields and clear validation errors when inputs change`() {
        // Given
        val state = CharacterEditorUiState(
            name = "",
            nameError = "Required",
            level = "0",
            levelError = "Level must be ≥ 1",
            maxHitPoints = "0",
            maxHitPointsError = "Required",
            abilities = AbilityFieldState.defaults().map { field ->
                if (field.abilityId == AbilityIds.STR) field.copy(error = "Required") else field
            },
        )

        // When
        val updatedName = reduceCharacterEditorState(
            state,
            CharacterEditorAction.NameChanged("Aria"),
            computeDerivedBonusesUseCase,
        )
        val updatedLevel = reduceCharacterEditorState(
            updatedName,
            CharacterEditorAction.LevelChanged("2"),
            computeDerivedBonusesUseCase,
        )
        val updatedHp = reduceCharacterEditorState(
            updatedLevel,
            CharacterEditorAction.MaxHpChanged("12"),
            computeDerivedBonusesUseCase,
        )
        val updatedAbility = reduceCharacterEditorState(
            updatedHp,
            CharacterEditorAction.AbilityChanged(AbilityIds.STR, "14"),
            computeDerivedBonusesUseCase,
        )

        // Then
        assertThat(updatedAbility.name).isEqualTo("Aria")
        assertThat(updatedAbility.nameError).isNull()
        assertThat(updatedAbility.level).isEqualTo("2")
        assertThat(updatedAbility.levelError).isNull()
        assertThat(updatedAbility.maxHitPoints).isEqualTo("12")
        assertThat(updatedAbility.maxHitPointsError).isNull()
        val strengthField = updatedAbility.abilities.first { it.abilityId == AbilityIds.STR }
        assertThat(strengthField.score).isEqualTo("14")
        assertThat(strengthField.error).isNull()
    }

    @Test
    fun `reduceCharacterEditorState should recompute bonuses when ability changes`() {
        // Given
        val state = CharacterEditorUiState()

        // When
        val updated = reduceCharacterEditorState(
            state,
            CharacterEditorAction.AbilityChanged(AbilityIds.STR, "14"),
            computeDerivedBonusesUseCase,
        )

        // Then
        val savingThrow = updated.savingThrows.first { it.abilityId == AbilityIds.STR }
        val athletics = updated.skills.first { it.skill == Skill.ATHLETICS }
        assertThat(savingThrow.bonus).isEqualTo(2)
        assertThat(athletics.bonus).isEqualTo(2)
    }

    @Test
    fun `reduceCharacterEditorState should recompute bonuses when skill proficiency changes`() {
        // Given
        val state = CharacterEditorUiState()

        // When
        val updated = reduceCharacterEditorState(
            state,
            CharacterEditorAction.SkillProficiencyChanged(Skill.ATHLETICS, true),
            computeDerivedBonusesUseCase,
        )

        // Then
        val athletics = updated.skills.first { it.skill == Skill.ATHLETICS }
        assertThat(athletics.bonus).isEqualTo(2)
    }

    @Test
    fun `reduceCharacterEditorState should recompute bonuses when proficiency bonus changes`() {
        // Given
        val state = CharacterEditorUiState(
            skills = SkillInputState.defaults().map { entry ->
                if (entry.skill == Skill.ATHLETICS) entry.copy(proficient = true) else entry
            },
        )

        // When
        val updated = reduceCharacterEditorState(
            state,
            CharacterEditorAction.ProficiencyBonusChanged("4"),
            computeDerivedBonusesUseCase,
        )

        // Then
        val athletics = updated.skills.first { it.skill == Skill.ATHLETICS }
        assertThat(athletics.bonus).isEqualTo(4)
    }
}
