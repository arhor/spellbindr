package com.github.arhor.spellbindr.data.mapper

import com.github.arhor.spellbindr.data.local.database.entity.CharacterSheetSnapshot
import com.github.arhor.spellbindr.domain.model.CharacterSheet
import com.github.arhor.spellbindr.domain.model.SavingThrowEntry
import com.github.arhor.spellbindr.domain.model.SkillEntry
import com.github.arhor.spellbindr.domain.model.SpellSlotState
import com.github.arhor.spellbindr.domain.model.defaultSavingThrows
import com.github.arhor.spellbindr.domain.model.defaultSkills
import com.github.arhor.spellbindr.domain.model.defaultSpellSlots

fun CharacterSheetSnapshot.toDomain(id: String): CharacterSheet = CharacterSheet(
    id = id,
    name = name,
    level = level,
    className = className,
    race = race,
    background = background,
    alignment = alignment,
    experiencePoints = experiencePoints,
    abilityScores = abilityScores,
    proficiencyBonus = proficiencyBonus,
    inspiration = inspiration,
    maxHitPoints = maxHitPoints,
    currentHitPoints = currentHitPoints,
    temporaryHitPoints = temporaryHitPoints,
    armorClass = armorClass,
    initiative = initiative,
    speed = speed,
    hitDice = hitDice,
    deathSaves = deathSaves,
    spellSlots = spellSlots,
    savingThrows = savingThrows,
    skills = skills,
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
    characterSpells = characterSpells,
    weapons = weapons,
)

fun CharacterSheet.toSnapshot(): CharacterSheetSnapshot = CharacterSheetSnapshot(
    name = name,
    level = level,
    className = className,
    race = race,
    background = background,
    alignment = alignment,
    experiencePoints = experiencePoints,
    abilityScores = abilityScores,
    proficiencyBonus = proficiencyBonus,
    inspiration = inspiration,
    maxHitPoints = maxHitPoints,
    currentHitPoints = currentHitPoints,
    temporaryHitPoints = temporaryHitPoints,
    armorClass = armorClass,
    initiative = initiative,
    speed = speed,
    hitDice = hitDice,
    deathSaves = deathSaves,
    spellSlots = spellSlots,
    savingThrows = savingThrows,
    skills = skills,
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
    characterSpells = characterSpells,
    weapons = weapons,
)

private fun defaultSavingThrows(): List<SavingThrowEntry> =
    defaultSavingThrows()

private fun defaultSkills(): List<SkillEntry> =
    defaultSkills()

private fun defaultSpellSlots(): List<SpellSlotState> =
    defaultSpellSlots()
