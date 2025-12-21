package com.github.arhor.spellbindr.domain.usecase

import com.github.arhor.spellbindr.data.model.predefined.Ability
import com.github.arhor.spellbindr.domain.model.AbilityScores
import com.github.arhor.spellbindr.ui.feature.characters.CharacterEditorUiState
import com.github.arhor.spellbindr.ui.feature.characters.SavingThrowInputState
import com.github.arhor.spellbindr.ui.feature.characters.SkillInputState

class ComputeDerivedBonusesUseCase {
    operator fun invoke(state: CharacterEditorUiState): CharacterEditorUiState {
        val abilityScores = state.toAbilityScores()
        val proficiencyValue = state.proficiencyBonus.toIntOrNull() ?: 0

        return state.copy(
            savingThrows = state.savingThrows.map { entry ->
                entry.copy(
                    bonus = abilityScores.modifierFor(entry.ability) + entry.proficiencyBonus(proficiencyValue),
                )
            },
            skills = state.skills.map { entry ->
                entry.copy(
                    bonus = abilityScores.modifierFor(entry.skill.ability) + entry.proficiencyBonus(proficiencyValue),
                )
            },
        )
    }
}

private fun CharacterEditorUiState.toAbilityScores(): AbilityScores = AbilityScores(
    strength = abilityScoreFor(Ability.STR),
    dexterity = abilityScoreFor(Ability.DEX),
    constitution = abilityScoreFor(Ability.CON),
    intelligence = abilityScoreFor(Ability.INT),
    wisdom = abilityScoreFor(Ability.WIS),
    charisma = abilityScoreFor(Ability.CHA),
)

private fun CharacterEditorUiState.abilityScoreFor(ability: Ability): Int =
    abilities.firstOrNull { it.ability == ability }?.score?.toIntOrNull() ?: 10

private fun SavingThrowInputState.proficiencyBonus(proficiencyValue: Int): Int =
    if (proficient) proficiencyValue else 0

private fun SkillInputState.proficiencyBonus(proficiencyValue: Int): Int = when {
    expertise -> proficiencyValue * 2
    proficient -> proficiencyValue
    else -> 0
}
