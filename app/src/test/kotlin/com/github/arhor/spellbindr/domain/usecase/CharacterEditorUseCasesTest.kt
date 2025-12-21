package com.github.arhor.spellbindr.domain.usecase

import com.github.arhor.spellbindr.domain.model.Ability
import com.github.arhor.spellbindr.domain.model.AbilityScoreInput
import com.github.arhor.spellbindr.domain.model.CharacterEditorInput
import com.github.arhor.spellbindr.domain.model.CharacterSheet
import com.github.arhor.spellbindr.domain.model.SavingThrowInput
import com.github.arhor.spellbindr.domain.model.Skill
import com.github.arhor.spellbindr.domain.model.SkillProficiencyInput
import com.google.common.truth.Truth.assertThat
import org.junit.Test

class CharacterEditorUseCasesTest {

    private val validateUseCase = ValidateCharacterSheetUseCase()
    private val computeDerivedBonusesUseCase = ComputeDerivedBonusesUseCase()
    private val buildCharacterSheetFromInputsUseCase = BuildCharacterSheetFromInputsUseCase()

    @Test
    fun `validate returns errors for required fields and invalid abilities`() {
        val input = CharacterEditorInput(
            name = "",
            level = "0",
            maxHitPoints = "0",
            abilities = AbilityScoreInput.defaults().map { ability ->
                if (ability.ability == Ability.STR) ability.copy(score = "") else ability
            },
        )

        val result = validateUseCase(input)

        assertThat(result.nameError).isEqualTo(CharacterSheetInputError.Required)
        assertThat(result.levelError).isEqualTo(CharacterSheetInputError.MinValue(1))
        assertThat(result.maxHpError).isEqualTo(CharacterSheetInputError.Required)
        assertThat(result.abilityErrors[Ability.STR]).isEqualTo(CharacterSheetInputError.Required)
        assertThat(result.hasErrors).isTrue()
    }

    @Test
    fun `validate returns no errors for valid inputs`() {
        val input = CharacterEditorInput(
            name = "Ayla",
            level = "2",
            maxHitPoints = "8",
        )

        val result = validateUseCase(input)

        assertThat(result.nameError).isNull()
        assertThat(result.levelError).isNull()
        assertThat(result.maxHpError).isNull()
        assertThat(result.abilityErrors).isEmpty()
        assertThat(result.hasErrors).isFalse()
    }

    @Test
    fun `computeDerivedBonusesUseCase updates saving throws and skills`() {
        val input = CharacterEditorInput(
            proficiencyBonus = "2",
        )
            .withAbilityScore(Ability.STR, "14")
            .withAbilityScore(Ability.DEX, "8")
            .copy(
                savingThrows = SavingThrowInput.defaults().map { entry ->
                    if (entry.ability == Ability.STR) entry.copy(proficient = true) else entry
                },
                skills = SkillProficiencyInput.defaults().map { entry ->
                    if (entry.skill == Skill.ACROBATICS) entry.copy(expertise = true) else entry
                },
            )

        val result = computeDerivedBonusesUseCase(input)

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
        val input = CharacterEditorInput(
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
                savingThrows = SavingThrowInput.defaults().map { entry ->
                    if (entry.ability == Ability.STR) entry.copy(proficient = true) else entry
                },
                skills = SkillProficiencyInput.defaults().map { entry ->
                    if (entry.skill == Skill.ACROBATICS) entry.copy(expertise = true) else entry
                },
            )

        val result = buildCharacterSheetFromInputsUseCase(input, baseSheet)

        assertThat(result.id).isEqualTo("base-id")
        assertThat(result.name).isEqualTo("Mira")
        assertThat(result.level).isEqualTo(3)
        assertThat(result.experiencePoints).isEqualTo(1200)
        assertThat(result.abilityScores.strength).isEqualTo(14)
        assertThat(result.maxHitPoints).isEqualTo(14)
        assertThat(result.savingThrows.first { it.ability == Ability.STR }.bonus).isEqualTo(4)
        assertThat(result.skills.first { it.skill == Skill.ACROBATICS }.bonus).isEqualTo(3)
    }

    @Test
    fun `validate handles blank and invalid numeric values`() {
        val input = CharacterEditorInput(
            name = "Ayla",
            level = "not-a-number",
            maxHitPoints = "",
            abilities = AbilityScoreInput.defaults().map { ability ->
                if (ability.ability == Ability.INT) ability.copy(score = " ") else ability
            },
        )

        val result = validateUseCase(input)

        assertThat(result.nameError).isNull()
        assertThat(result.levelError).isEqualTo(CharacterSheetInputError.MinValue(1))
        assertThat(result.maxHpError).isEqualTo(CharacterSheetInputError.Required)
        assertThat(result.abilityErrors[Ability.INT]).isEqualTo(CharacterSheetInputError.Required)
    }

    private fun CharacterEditorInput.withAbilityScore(ability: Ability, score: String): CharacterEditorInput =
        copy(
            abilities = abilities.map { field ->
                if (field.ability == ability) field.copy(score = score) else field
            }
        )
}
