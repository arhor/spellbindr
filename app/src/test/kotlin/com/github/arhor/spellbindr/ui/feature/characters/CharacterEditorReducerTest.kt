package com.github.arhor.spellbindr.ui.feature.characters

import com.github.arhor.spellbindr.data.model.predefined.Ability
import com.github.arhor.spellbindr.data.model.predefined.Skill
import com.github.arhor.spellbindr.domain.usecase.ComputeDerivedBonusesUseCase
import com.google.common.truth.Truth.assertThat
import org.junit.Test

class CharacterEditorReducerTest {

    private val computeDerivedBonusesUseCase = ComputeDerivedBonusesUseCase()

    @Test
    fun `reduceCharacterEditorState updates fields and clears validation errors`() {
        val state = CharacterEditorUiState(
            name = "",
            nameError = "Required",
            level = "0",
            levelError = "Level must be ≥ 1",
            maxHitPoints = "0",
            maxHitPointsError = "Required",
            abilities = AbilityFieldState.defaults().map { field ->
                if (field.ability == Ability.STR) field.copy(error = "Required") else field
            },
        )

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
            CharacterEditorAction.AbilityChanged(Ability.STR, "14"),
            computeDerivedBonusesUseCase,
        )

        assertThat(updatedAbility.name).isEqualTo("Aria")
        assertThat(updatedAbility.nameError).isNull()
        assertThat(updatedAbility.level).isEqualTo("2")
        assertThat(updatedAbility.levelError).isNull()
        assertThat(updatedAbility.maxHitPoints).isEqualTo("12")
        assertThat(updatedAbility.maxHitPointsError).isNull()
        val strengthField = updatedAbility.abilities.first { it.ability == Ability.STR }
        assertThat(strengthField.score).isEqualTo("14")
        assertThat(strengthField.error).isNull()
    }

    @Test
    fun `reduceCharacterEditorState recomputes bonuses when ability changes`() {
        val state = CharacterEditorUiState()

        val updated = reduceCharacterEditorState(
            state,
            CharacterEditorAction.AbilityChanged(Ability.STR, "14"),
            computeDerivedBonusesUseCase,
        )

        val savingThrow = updated.savingThrows.first { it.ability == Ability.STR }
        val athletics = updated.skills.first { it.skill == Skill.ATHLETICS }
        assertThat(savingThrow.bonus).isEqualTo(2)
        assertThat(athletics.bonus).isEqualTo(2)
    }

    @Test
    fun `reduceCharacterEditorState recomputes bonuses when proficiency changes`() {
        val state = CharacterEditorUiState()

        val updated = reduceCharacterEditorState(
            state,
            CharacterEditorAction.SkillProficiencyChanged(Skill.ATHLETICS, true),
            computeDerivedBonusesUseCase,
        )

        val athletics = updated.skills.first { it.skill == Skill.ATHLETICS }
        assertThat(athletics.bonus).isEqualTo(2)
    }

    @Test
    fun `reduceCharacterEditorState recomputes bonuses when proficiency bonus changes`() {
        val state = CharacterEditorUiState(
            skills = SkillInputState.defaults().map { entry ->
                if (entry.skill == Skill.ATHLETICS) entry.copy(proficient = true) else entry
            },
        )

        val updated = reduceCharacterEditorState(
            state,
            CharacterEditorAction.ProficiencyBonusChanged("4"),
            computeDerivedBonusesUseCase,
        )

        val athletics = updated.skills.first { it.skill == Skill.ATHLETICS }
        assertThat(athletics.bonus).isEqualTo(4)
    }
}
