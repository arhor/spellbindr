package com.github.arhor.spellbindr.domain.usecase.internal

import com.github.arhor.spellbindr.domain.model.AbilityId
import com.github.arhor.spellbindr.domain.model.AbilityIds
import com.github.arhor.spellbindr.domain.model.AbilityScores
import com.github.arhor.spellbindr.domain.model.CharacterEditorInput
import com.github.arhor.spellbindr.domain.model.SavingThrowInput
import com.github.arhor.spellbindr.domain.model.SkillProficiencyInput

internal fun CharacterEditorInput.resolveAbilityScores(): AbilityScores = AbilityScores(
    strength = abilityScoreFor(AbilityIds.STR),
    dexterity = abilityScoreFor(AbilityIds.DEX),
    constitution = abilityScoreFor(AbilityIds.CON),
    intelligence = abilityScoreFor(AbilityIds.INT),
    wisdom = abilityScoreFor(AbilityIds.WIS),
    charisma = abilityScoreFor(AbilityIds.CHA),
)

internal fun CharacterEditorInput.resolveProficiency(defaultValue: Int): Int =
    proficiencyBonus.toIntOrNull() ?: defaultValue

internal fun CharacterEditorInput.abilityScoreFor(abilityId: AbilityId): Int =
    abilities.firstOrNull { it.abilityId == abilityId }?.score?.toIntOrNull() ?: 10

internal fun SavingThrowInput.proficiencyBonusFor(proficiencyValue: Int): Int =
    if (proficient) proficiencyValue else 0

internal fun SkillProficiencyInput.proficiencyBonusFor(proficiencyValue: Int): Int = when {
    expertise -> proficiencyValue * 2
    proficient -> proficiencyValue
    else -> 0
}
