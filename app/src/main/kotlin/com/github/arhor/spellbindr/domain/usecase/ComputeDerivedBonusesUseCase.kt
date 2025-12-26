package com.github.arhor.spellbindr.domain.usecase

import com.github.arhor.spellbindr.domain.model.AbilityId
import com.github.arhor.spellbindr.domain.model.AbilityIds
import com.github.arhor.spellbindr.domain.model.AbilityScores
import com.github.arhor.spellbindr.domain.model.CharacterEditorDerivedBonuses
import com.github.arhor.spellbindr.domain.model.CharacterEditorInput
import com.github.arhor.spellbindr.domain.model.SavingThrowBonus
import com.github.arhor.spellbindr.domain.model.SavingThrowInput
import com.github.arhor.spellbindr.domain.model.SkillBonus
import com.github.arhor.spellbindr.domain.model.SkillProficiencyInput
import javax.inject.Inject

class ComputeDerivedBonusesUseCase @Inject constructor() {
    operator fun invoke(input: CharacterEditorInput): CharacterEditorDerivedBonuses {
        val abilityScores = input.toAbilityScores()
        val proficiencyValue = input.proficiencyBonus.toIntOrNull() ?: 0

        return CharacterEditorDerivedBonuses(
            savingThrows = input.savingThrows.map { entry ->
                SavingThrowBonus(
                    abilityId = entry.abilityId,
                    bonus = abilityScores.modifierFor(entry.abilityId) + entry.proficiencyBonus(proficiencyValue),
                )
            },
            skills = input.skills.map { entry ->
                SkillBonus(
                    skill = entry.skill,
                    bonus = abilityScores.modifierFor(entry.skill.abilityId) + entry.proficiencyBonus(proficiencyValue),
                )
            },
        )
    }
}

private fun CharacterEditorInput.toAbilityScores(): AbilityScores = AbilityScores(
    strength = abilityScoreFor(AbilityIds.STR),
    dexterity = abilityScoreFor(AbilityIds.DEX),
    constitution = abilityScoreFor(AbilityIds.CON),
    intelligence = abilityScoreFor(AbilityIds.INT),
    wisdom = abilityScoreFor(AbilityIds.WIS),
    charisma = abilityScoreFor(AbilityIds.CHA),
)

private fun CharacterEditorInput.abilityScoreFor(abilityId: AbilityId): Int =
    abilities.firstOrNull { it.abilityId == abilityId }?.score?.toIntOrNull() ?: 10

private fun SavingThrowInput.proficiencyBonus(proficiencyValue: Int): Int =
    if (proficient) proficiencyValue else 0

private fun SkillProficiencyInput.proficiencyBonus(proficiencyValue: Int): Int = when {
    expertise -> proficiencyValue * 2
    proficient -> proficiencyValue
    else -> 0
}
