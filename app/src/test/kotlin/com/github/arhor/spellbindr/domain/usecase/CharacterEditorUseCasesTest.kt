package com.github.arhor.spellbindr.domain.usecase

import com.github.arhor.spellbindr.data.model.predefined.Ability
import com.github.arhor.spellbindr.data.model.predefined.Skill
import com.github.arhor.spellbindr.domain.model.CharacterSheet
import com.github.arhor.spellbindr.ui.feature.characters.AbilityFieldState
import com.github.arhor.spellbindr.ui.feature.characters.CharacterEditorUiState
import com.google.common.truth.Truth.assertThat
import org.junit.Test

class CharacterEditorUseCasesTest {

    private val validateUseCase = ValidateCharacterSheetUseCase()
    private val computeDerivedBonusesUseCase = ComputeDerivedBonusesUseCase()
    private val buildCharacterSheetFromInputsUseCase = BuildCharacterSheetFromInputsUseCase()

    @Test
    fun `validate returns errors for required fields and invalid abilities`() {
        val state = CharacterEditorUiState(
            name = "",
            level = "0",
            maxHitPoints = "0",
            abilities = AbilityFieldState.defaults().map { field ->
                if (field.ability == Ability.STR) field.copy(score = "") else field
            },
        )

        val result = validateUseCase(state)

        assertThat(result.nameError).isEqualTo("Required")
        assertThat(result.levelError).isEqualTo("Level must be ≥ 1")
        assertThat(result.maxHpError).isEqualTo("Required")
        assertThat(result.abilityStates.first { it.ability == Ability.STR }.error).isEqualTo("Required")
        assertThat(result.hasErrors).isTrue()
    }

    @Test
    fun `validate returns no errors for valid inputs`() {
        val state = CharacterEditorUiState(
            name = "Ayla",
            level = "2",
            maxHitPoints = "8",
        )

        val result = validateUseCase(state)

        assertThat(result.nameError).isNull()
        assertThat(result.levelError).isNull()
        assertThat(result.maxHpError).isNull()
        assertThat(result.abilityStates.all { it.error == null }).isTrue()
        assertThat(result.hasErrors).isFalse()
    }

    @Test
    fun `computeDerivedBonusesUseCase updates saving throws and skills`() {
        val state = CharacterEditorUiState(
            proficiencyBonus = "2",
        )
            .withAbilityScore(Ability.STR, "14")
            .withAbilityScore(Ability.DEX, "8")
            .copy(
                savingThrows = CharacterEditorUiState().savingThrows.map { entry ->
                    if (entry.ability == Ability.STR) entry.copy(proficient = true) else entry
                },
                skills = CharacterEditorUiState().skills.map { entry ->
                    if (entry.skill == Skill.ACROBATICS) entry.copy(expertise = true) else entry
                },
            )

        val result = computeDerivedBonusesUseCase(state)

        val strengthSave = result.savingThrows.first { it.ability == Ability.STR }
        val dexSave = result.savingThrows.first { it.ability == Ability.DEX }
        val acrobatics = result.skills.first { it.skill == Skill.ACROBATICS }

        assertThat(strengthSave.bonus).isEqualTo(4)
        assertThat(dexSave.bonus).isEqualTo(-1)
        assertThat(acrobatics.bonus).isEqualTo(3)
    }

    @Test
    fun `buildCharacterSheetFromInputsUseCase builds sheet from inputs`() {
        val baseSheet = CharacterSheet(id = "base-id", maxHitPoints = 10)
        val state = CharacterEditorUiState(
            name = "  Mira  ",
            level = "3",
            className = "Ranger",
            race = "Elf",
            background = "Outlander",
            alignment = "Chaotic Good",
            experiencePoints = "1200",
            proficiencyBonus = "2",
            maxHitPoints = "14",
        )
            .withAbilityScore(Ability.STR, "14")
            .withAbilityScore(Ability.DEX, "8")
            .copy(
                savingThrows = CharacterEditorUiState().savingThrows.map { entry ->
                    if (entry.ability == Ability.STR) entry.copy(proficient = true) else entry
                },
                skills = CharacterEditorUiState().skills.map { entry ->
                    if (entry.skill == Skill.ACROBATICS) entry.copy(expertise = true) else entry
                },
            )

        val result = buildCharacterSheetFromInputsUseCase(state, baseSheet)

        assertThat(result.id).isEqualTo("base-id")
        assertThat(result.name).isEqualTo("Mira")
        assertThat(result.level).isEqualTo(3)
        assertThat(result.experiencePoints).isEqualTo(1200)
        assertThat(result.abilityScores.strength).isEqualTo(14)
        assertThat(result.maxHitPoints).isEqualTo(14)
        assertThat(result.savingThrows.first { it.ability == Ability.STR }.bonus).isEqualTo(4)
        assertThat(result.skills.first { it.skill == Skill.ACROBATICS }.bonus).isEqualTo(3)
    }

    private fun CharacterEditorUiState.withAbilityScore(ability: Ability, score: String): CharacterEditorUiState =
        copy(
            abilities = abilities.map { field ->
                if (field.ability == ability) field.copy(score = score) else field
            }
        )
}
