package com.github.arhor.spellbindr.domain.usecase

import com.github.arhor.spellbindr.domain.model.AbilityId
import com.github.arhor.spellbindr.domain.model.AbilityIds
import com.github.arhor.spellbindr.domain.model.AbilityScoreInput
import com.github.arhor.spellbindr.domain.model.CharacterEditorInput
import com.github.arhor.spellbindr.domain.model.CharacterSheet
import com.github.arhor.spellbindr.domain.model.CharacterSheetInputError
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
    fun `ValidateCharacterSheetUseCase should return errors when required fields or abilities are invalid`() {
        // Given
        val input = CharacterEditorInput(
            name = "",
            level = "0",
            maxHitPoints = "0",
            abilities = AbilityScoreInput.defaults().map { ability ->
                if (ability.abilityId == AbilityIds.STR) ability.copy(score = "") else ability
            },
        )

        // When
        val result = validateUseCase(input)

        // Then
        assertThat(result.nameError).isEqualTo(CharacterSheetInputError.Required)
        assertThat(result.levelError).isEqualTo(CharacterSheetInputError.MinValue(1))
        assertThat(result.maxHpError).isEqualTo(CharacterSheetInputError.Required)
        assertThat(result.abilityErrors[AbilityIds.STR]).isEqualTo(CharacterSheetInputError.Required)
        assertThat(result.hasErrors).isTrue()
    }

    @Test
    fun `ValidateCharacterSheetUseCase should return no errors when inputs are valid`() {
        // Given
        val input = CharacterEditorInput(
            name = "Ayla",
            level = "2",
            maxHitPoints = "8",
        )

        // When
        val result = validateUseCase(input)

        // Then
        assertThat(result.nameError).isNull()
        assertThat(result.levelError).isNull()
        assertThat(result.maxHpError).isNull()
        assertThat(result.abilityErrors).isEmpty()
        assertThat(result.hasErrors).isFalse()
    }

    @Test
    fun `ComputeDerivedBonusesUseCase should update saving throws and skills when proficiency applies`() {
        // Given
        val input = CharacterEditorInput(
            proficiencyBonus = "2",
        )
            .withAbilityScore(AbilityIds.STR, "14")
            .withAbilityScore(AbilityIds.DEX, "8")
            .copy(
                savingThrows = SavingThrowInput.defaults().map { entry ->
                    if (entry.abilityId == AbilityIds.STR) entry.copy(proficient = true) else entry
                },
                skills = SkillProficiencyInput.defaults().map { entry ->
                    if (entry.skill == Skill.ACROBATICS) entry.copy(expertise = true) else entry
                },
            )

        // When
        val result = computeDerivedBonusesUseCase(input)

        // Then
        val strengthSave = result.savingThrows.first { it.abilityId == AbilityIds.STR }
        val dexSave = result.savingThrows.first { it.abilityId == AbilityIds.DEX }
        val acrobatics = result.skills.first { it.skill == Skill.ACROBATICS }

        assertThat(strengthSave.bonus).isEqualTo(4)
        assertThat(dexSave.bonus).isEqualTo(-1)
        assertThat(acrobatics.bonus).isEqualTo(3)
    }

    @Test
    fun `BuildCharacterSheetFromInputsUseCase should build sheet when inputs are provided`() {
        // Given
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
            .withAbilityScore(AbilityIds.STR, "14")
            .withAbilityScore(AbilityIds.DEX, "8")
            .copy(
                savingThrows = SavingThrowInput.defaults().map { entry ->
                    if (entry.abilityId == AbilityIds.STR) entry.copy(proficient = true) else entry
                },
                skills = SkillProficiencyInput.defaults().map { entry ->
                    if (entry.skill == Skill.ACROBATICS) entry.copy(expertise = true) else entry
                },
            )

        // When
        val result = buildCharacterSheetFromInputsUseCase(input, baseSheet)

        // Then
        assertThat(result.id).isEqualTo("base-id")
        assertThat(result.name).isEqualTo("Mira")
        assertThat(result.level).isEqualTo(3)
        assertThat(result.experiencePoints).isEqualTo(1200)
        assertThat(result.abilityScores.strength).isEqualTo(14)
        assertThat(result.maxHitPoints).isEqualTo(14)
        assertThat(result.savingThrows.first { it.abilityId == AbilityIds.STR }.bonus).isEqualTo(4)
        assertThat(result.skills.first { it.skill == Skill.ACROBATICS }.bonus).isEqualTo(3)
    }

    @Test
    fun `ValidateCharacterSheetUseCase should handle blank and invalid numeric values when validating`() {
        // Given
        val input = CharacterEditorInput(
            name = "Ayla",
            level = "not-a-number",
            maxHitPoints = "",
            abilities = AbilityScoreInput.defaults().map { ability ->
                if (ability.abilityId == AbilityIds.INT) ability.copy(score = " ") else ability
            },
        )

        // When
        val result = validateUseCase(input)

        // Then
        assertThat(result.nameError).isNull()
        assertThat(result.levelError).isEqualTo(CharacterSheetInputError.MinValue(1))
        assertThat(result.maxHpError).isEqualTo(CharacterSheetInputError.Required)
        assertThat(result.abilityErrors[AbilityIds.INT]).isEqualTo(CharacterSheetInputError.Required)
    }

    private fun CharacterEditorInput.withAbilityScore(abilityId: AbilityId, score: String): CharacterEditorInput =
        copy(
            abilities = abilities.map { field ->
                if (field.abilityId == abilityId) field.copy(score = score) else field
            }
        )
}
