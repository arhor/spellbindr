package com.github.arhor.spellbindr.domain.usecase

import com.github.arhor.spellbindr.domain.model.Ability
import com.github.arhor.spellbindr.domain.model.AbilityScores
import com.github.arhor.spellbindr.domain.model.CharacterEditorInput
import com.github.arhor.spellbindr.domain.model.CharacterSheet
import com.github.arhor.spellbindr.domain.model.SavingThrowEntry
import com.github.arhor.spellbindr.domain.model.SavingThrowInput
import com.github.arhor.spellbindr.domain.model.SkillEntry
import com.github.arhor.spellbindr.domain.model.SkillProficiencyInput
import java.util.UUID
import javax.inject.Inject

class BuildCharacterSheetFromInputsUseCase @Inject constructor() {
    operator fun invoke(input: CharacterEditorInput, base: CharacterSheet?): CharacterSheet {
        val ensuredId = input.characterId ?: base?.id ?: UUID.randomUUID().toString()
        val abilityScores = input.toAbilityScores()
        val proficiencyValue = input.proficiencyBonus.toIntOrNull() ?: 2
        val baseline = base ?: CharacterSheet(id = ensuredId)

        return baseline.copy(
            id = ensuredId,
            name = input.name.trim(),
            level = input.level.toIntOrNull() ?: 1,
            className = input.className.trim(),
            race = input.race.trim(),
            background = input.background.trim(),
            alignment = input.alignment.trim(),
            experiencePoints = input.experiencePoints.toIntOrNull(),
            abilityScores = abilityScores,
            proficiencyBonus = proficiencyValue,
            inspiration = input.inspiration,
            maxHitPoints = input.maxHitPoints.toIntOrNull() ?: baseline.maxHitPoints,
            currentHitPoints = input.currentHitPoints.toIntOrNull() ?: baseline.currentHitPoints,
            temporaryHitPoints = input.temporaryHitPoints.toIntOrNull() ?: baseline.temporaryHitPoints,
            armorClass = input.armorClass.toIntOrNull() ?: 10,
            initiative = input.initiative.toIntOrNull() ?: 0,
            speed = input.speed.trim(),
            hitDice = input.hitDice.trim(),
            savingThrows = input.savingThrows.map { entry ->
                SavingThrowEntry(
                    ability = entry.ability,
                    bonus = abilityScores.modifierFor(entry.ability) + entry.proficiencyBonus(proficiencyValue),
                    proficient = entry.proficient,
                )
            },
            skills = input.skills.map { entry ->
                SkillEntry(
                    skill = entry.skill,
                    bonus = abilityScores.modifierFor(entry.skill.ability) + entry.proficiencyBonus(proficiencyValue),
                    proficient = entry.proficient,
                    expertise = entry.expertise,
                )
            },
            senses = input.senses,
            languages = input.languages,
            proficiencies = input.proficiencies,
            attacksAndCantrips = input.attacksAndCantrips,
            featuresAndTraits = input.featuresAndTraits,
            equipment = input.equipment,
            personalityTraits = input.personalityTraits,
            ideals = input.ideals,
            bonds = input.bonds,
            flaws = input.flaws,
            notes = input.notes,
        )
    }
}

private fun CharacterEditorInput.toAbilityScores(): AbilityScores = AbilityScores(
    strength = abilityScoreFor(Ability.STR),
    dexterity = abilityScoreFor(Ability.DEX),
    constitution = abilityScoreFor(Ability.CON),
    intelligence = abilityScoreFor(Ability.INT),
    wisdom = abilityScoreFor(Ability.WIS),
    charisma = abilityScoreFor(Ability.CHA),
)

private fun CharacterEditorInput.abilityScoreFor(ability: Ability): Int =
    abilities.firstOrNull { it.ability == ability }?.score?.toIntOrNull() ?: 10

private fun SavingThrowInput.proficiencyBonus(proficiencyValue: Int): Int =
    if (proficient) proficiencyValue else 0

private fun SkillProficiencyInput.proficiencyBonus(proficiencyValue: Int): Int = when {
    expertise -> proficiencyValue * 2
    proficient -> proficiencyValue
    else -> 0
}
