package io.github.arhor.dnd.companion.domain.usecase.internal

import io.github.arhor.dnd.companion.domain.model.AbilityId
import io.github.arhor.dnd.companion.domain.model.AbilityIds
import io.github.arhor.dnd.companion.domain.model.AbilityScores
import io.github.arhor.dnd.companion.domain.model.CharacterEditorInput
import io.github.arhor.dnd.companion.domain.model.SavingThrowInput
import io.github.arhor.dnd.companion.domain.model.SkillProficiencyInput

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
