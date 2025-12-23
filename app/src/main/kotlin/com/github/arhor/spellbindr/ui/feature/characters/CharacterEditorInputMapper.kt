package com.github.arhor.spellbindr.ui.feature.characters

import com.github.arhor.spellbindr.domain.model.AbilityScoreInput
import com.github.arhor.spellbindr.domain.model.CharacterEditorDerivedBonuses
import com.github.arhor.spellbindr.domain.model.CharacterEditorInput
import com.github.arhor.spellbindr.domain.model.SavingThrowInput
import com.github.arhor.spellbindr.domain.model.SkillProficiencyInput

fun CharacterEditorUiState.toDomainInput(): CharacterEditorInput = CharacterEditorInput(
    characterId = characterId,
    name = name,
    level = level,
    className = className,
    race = race,
    background = background,
    alignment = alignment,
    experiencePoints = experiencePoints,
    abilities = abilities.map { AbilityScoreInput(abilityId = it.abilityId, score = it.score) },
    proficiencyBonus = proficiencyBonus,
    inspiration = inspiration,
    maxHitPoints = maxHitPoints,
    currentHitPoints = currentHitPoints,
    temporaryHitPoints = temporaryHitPoints,
    armorClass = armorClass,
    initiative = initiative,
    speed = speed,
    hitDice = hitDice,
    savingThrows = savingThrows.map { SavingThrowInput(abilityId = it.abilityId, proficient = it.proficient) },
    skills = skills.map {
        SkillProficiencyInput(skill = it.skill, proficient = it.proficient, expertise = it.expertise)
    },
    senses = senses,
    languages = languages,
    proficiencies = proficiencies,
    attacksAndCantrips = attacksAndCantrips,
    featuresAndTraits = featuresAndTraits,
    equipment = equipment,
    personalityTraits = personalityTraits,
    ideals = ideals,
    bonds = bonds,
    flaws = flaws,
    notes = notes,
)

fun CharacterEditorUiState.withDerivedBonuses(
    derived: CharacterEditorDerivedBonuses,
): CharacterEditorUiState {
    val savingThrowBonuses = derived.savingThrows.associateBy { it.abilityId }
    val skillBonuses = derived.skills.associateBy { it.skill }
    return copy(
        savingThrows = savingThrows.map { entry ->
            entry.copy(bonus = savingThrowBonuses[entry.abilityId]?.bonus ?: entry.bonus)
        },
        skills = skills.map { entry ->
            entry.copy(bonus = skillBonuses[entry.skill]?.bonus ?: entry.bonus)
        },
    )
}
