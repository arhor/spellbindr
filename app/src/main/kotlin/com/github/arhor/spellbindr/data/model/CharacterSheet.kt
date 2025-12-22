package com.github.arhor.spellbindr.data.model

import com.github.arhor.spellbindr.data.model.predefined.Ability
import com.github.arhor.spellbindr.data.model.predefined.DamageType
import com.github.arhor.spellbindr.data.model.predefined.Skill
import java.util.UUID
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable


/**
 * Represents the data captured by the manual character sheet editor.
 */
data class CharacterSheet(
    val id: String,
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

@Serializable
data class AbilityScores(
    val strength: Int = 10,
    val dexterity: Int = 10,
    val constitution: Int = 10,
    val intelligence: Int = 10,
    val wisdom: Int = 10,
    val charisma: Int = 10,
) {
    fun modifierFor(ability: Ability): Int = when (ability) {
        Ability.STR -> (strength - 10) / 2
        Ability.DEX -> (dexterity - 10) / 2
        Ability.CON -> (constitution - 10) / 2
        Ability.INT -> (intelligence - 10) / 2
        Ability.WIS -> (wisdom - 10) / 2
        Ability.CHA -> (charisma - 10) / 2
    }
}

@Serializable
data class SavingThrowEntry(
    val ability: Ability,
    val bonus: Int = 0,
    val proficient: Boolean = false,
)

@Serializable
data class SkillEntry(
    val skill: Skill,
    val bonus: Int = 0,
    val proficient: Boolean = false,
    val expertise: Boolean = false,
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

fun defaultSavingThrows(): List<SavingThrowEntry> =
    Ability.entries.map { ability -> SavingThrowEntry(ability = ability) }

fun defaultSkills(): List<SkillEntry> =
    Skill.entries.map { skill -> SkillEntry(skill = skill) }

@Serializable
data class DeathSaveState(
    val successes: Int = 0,
    val failures: Int = 0,
)

@Serializable
data class SpellSlotState(
    val level: Int,
    val total: Int = 0,
    val expended: Int = 0,
)

@Serializable
data class CharacterSpell(
    val spellId: String,
    val sourceClass: String = "",
)

@Serializable
data class Weapon(
    val id: String = UUID.randomUUID().toString(),
    val catalogId: String? = null,
    val name: String,
    val category: EquipmentCategory? = null,
    val categories: Set<EquipmentCategory> = emptySet(),
    @SerialName("attackAbility")
    val ability: Ability = Ability.STR,
    val proficient: Boolean = false,
    val damageDiceCount: Int = 1,
    val damageDieSize: Int = 6,
    val useAbilityForDamage: Boolean = true,
    val damageType: DamageType = DamageType.SLASHING,
)

fun defaultSpellSlots(): List<SpellSlotState> =
    (1..9).map { level -> SpellSlotState(level = level) }
