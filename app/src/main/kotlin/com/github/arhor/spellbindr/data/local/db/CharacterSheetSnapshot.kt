package com.github.arhor.spellbindr.data.local.db

import com.github.arhor.spellbindr.domain.model.AbilityScores
import com.github.arhor.spellbindr.domain.model.CharacterSheet
import com.github.arhor.spellbindr.domain.model.CharacterSpell
import com.github.arhor.spellbindr.domain.model.DeathSaveState
import com.github.arhor.spellbindr.domain.model.SavingThrowEntry
import com.github.arhor.spellbindr.domain.model.SkillEntry
import com.github.arhor.spellbindr.domain.model.SpellSlotState
import com.github.arhor.spellbindr.domain.model.Weapon
import com.github.arhor.spellbindr.domain.model.defaultSavingThrows
import com.github.arhor.spellbindr.domain.model.defaultSkills
import com.github.arhor.spellbindr.domain.model.defaultSpellSlots
import kotlinx.serialization.Serializable

/**
 * Snapshot stored in Room (id is stored on the [CharacterEntity]).
 */
@Serializable
data class CharacterSheetSnapshot(
    val name: String = "",
    val level: Int = 1,
    val className: String = "",
    val race: String = "",
    val background: String = "",
    val alignment: String = "",
    val experiencePoints: Int? = null,
    val abilityScores: AbilityScores = AbilityScores(),
    val proficiencyBonus: Int = 2,
    val inspiration: Boolean = false,
    val maxHitPoints: Int = 1,
    val currentHitPoints: Int = 1,
    val temporaryHitPoints: Int = 0,
    val armorClass: Int = 10,
    val initiative: Int = 0,
    val speed: String = "",
    val hitDice: String = "",
    val deathSaves: DeathSaveState = DeathSaveState(),
    val spellSlots: List<SpellSlotState> = defaultSpellSlots(),
    val savingThrows: List<SavingThrowEntry> = defaultSavingThrows(),
    val skills: List<SkillEntry> = defaultSkills(),
    val senses: String = "",
    val languages: String = "",
    val proficiencies: String = "",
    val attacksAndCantrips: String = "",
    val featuresAndTraits: String = "",
    val equipment: String = "",
    val personalityTraits: String = "",
    val ideals: String = "",
    val bonds: String = "",
    val flaws: String = "",
    val notes: String = "",
    val characterSpells: List<CharacterSpell> = emptyList(),
    val weapons: List<Weapon> = emptyList(),
)

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
