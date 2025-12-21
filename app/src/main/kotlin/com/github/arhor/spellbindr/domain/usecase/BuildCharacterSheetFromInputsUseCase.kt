package com.github.arhor.spellbindr.domain.usecase

import com.github.arhor.spellbindr.data.model.predefined.Ability
import com.github.arhor.spellbindr.domain.model.AbilityScores
import com.github.arhor.spellbindr.domain.model.CharacterSheet
import com.github.arhor.spellbindr.domain.model.SavingThrowEntry
import com.github.arhor.spellbindr.domain.model.SkillEntry
import com.github.arhor.spellbindr.ui.feature.characters.CharacterEditorUiState
import com.github.arhor.spellbindr.ui.feature.characters.SavingThrowInputState
import com.github.arhor.spellbindr.ui.feature.characters.SkillInputState
import java.util.UUID

class BuildCharacterSheetFromInputsUseCase {
    operator fun invoke(state: CharacterEditorUiState, base: CharacterSheet?): CharacterSheet {
        val ensuredId = state.characterId ?: base?.id ?: UUID.randomUUID().toString()
        val abilityScores = state.toAbilityScores()
        val proficiencyValue = state.proficiencyBonus.toIntOrNull() ?: 2
        val baseline = base ?: CharacterSheet(id = ensuredId)

        return baseline.copy(
            id = ensuredId,
            name = state.name.trim(),
            level = state.level.toIntOrNull() ?: 1,
            className = state.className.trim(),
            race = state.race.trim(),
            background = state.background.trim(),
            alignment = state.alignment.trim(),
            experiencePoints = state.experiencePoints.toIntOrNull(),
            abilityScores = abilityScores,
            proficiencyBonus = proficiencyValue,
            inspiration = state.inspiration,
            maxHitPoints = state.maxHitPoints.toIntOrNull() ?: baseline.maxHitPoints,
            currentHitPoints = state.currentHitPoints.toIntOrNull() ?: baseline.currentHitPoints,
            temporaryHitPoints = state.temporaryHitPoints.toIntOrNull() ?: baseline.temporaryHitPoints,
            armorClass = state.armorClass.toIntOrNull() ?: 10,
            initiative = state.initiative.toIntOrNull() ?: 0,
            speed = state.speed.trim(),
            hitDice = state.hitDice.trim(),
            savingThrows = state.savingThrows.map { entry ->
                SavingThrowEntry(
                    ability = entry.ability,
                    bonus = abilityScores.modifierFor(entry.ability) + entry.proficiencyBonus(proficiencyValue),
                    proficient = entry.proficient,
                )
            },
            skills = state.skills.map { entry ->
                SkillEntry(
                    skill = entry.skill,
                    bonus = abilityScores.modifierFor(entry.skill.ability) + entry.proficiencyBonus(proficiencyValue),
                    proficient = entry.proficient,
                    expertise = entry.expertise,
                )
            },
            senses = state.senses,
            languages = state.languages,
            proficiencies = state.proficiencies,
            attacksAndCantrips = state.attacksAndCantrips,
            featuresAndTraits = state.featuresAndTraits,
            equipment = state.equipment,
            personalityTraits = state.personalityTraits,
            ideals = state.ideals,
            bonds = state.bonds,
            flaws = state.flaws,
            notes = state.notes,
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
