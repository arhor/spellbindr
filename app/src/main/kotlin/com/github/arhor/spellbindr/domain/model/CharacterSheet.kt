package com.github.arhor.spellbindr.domain.model

import com.github.arhor.spellbindr.data.model.DamageType
import com.github.arhor.spellbindr.data.model.EquipmentCategory
import java.util.UUID

/**
 * Represents the full data captured by the manual character sheet editor.
 * This is the primary persistence model for user-created characters.
 *
 * @property id Unique identifier.
 * @property name Character name.
 * @property level Total character level.
 * @property className Free-text or selected class name(s).
 * @property race Free-text or selected race name.
 * @property background Free-text or selected background.
 * @property alignment Character alignment (e.g. "Chaotic Good").
 * @property experiencePoints Current XP amount.
 * @property abilityScores The six core ability scores.
 * @property proficiencyBonus Derived or manually set proficiency bonus.
 * @property inspiration Whether the character currently has inspiration.
 * @property maxHitPoints Maximum HP.
 * @property currentHitPoints Current HP.
 * @property temporaryHitPoints Temporary HP buffer.
 * @property armorClass Armor Class (AC).
 * @property initiative Initiative bonus.
 * @property speed Movement speed description (e.g. "30 ft").
 * @property hitDice Available hit dice (e.g. "1d8").
 * @property deathSaves Current death save successes/failures.
 * @property spellSlots Tracked spell slots per level.
 * @property savingThrows Configured saving throws with bonuses and proficiency.
 * @property skills Configured skills with bonuses and proficiency/expertise.
 * @property senses Passive perception or other senses.
 * @property languages Known languages.
 * @property proficiencies Armor, weapon, and tool proficiencies.
 * @property attacksAndCantrips Free-text attacks section.
 * @property featuresAndTraits Free-text features section.
 * @property equipment Free-text equipment list.
 * @property personalityTraits Roleplay personality traits.
 * @property ideals Roleplay ideals.
 * @property bonds Roleplay bonds.
 * @property flaws Roleplay flaws.
 * @property notes General notes.
 * @property characterSpells List of spells assigned to the character.
 * @property weapons List of weapons equipped or in inventory.
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
 * Container for the six standard D&D ability scores.
 *
 * @property strength Strength score.
 * @property dexterity Dexterity score.
 * @property constitution Constitution score.
 * @property intelligence Intelligence score.
 * @property wisdom Wisdom score.
 * @property charisma Charisma score.
 */
data class AbilityScores(
    val strength: Int = 10,
    val dexterity: Int = 10,
    val constitution: Int = 10,
    val intelligence: Int = 10,
    val wisdom: Int = 10,
    val charisma: Int = 10,
) {
    /**
     * Calculates the modifier for a given [ability] based on its score.
     * Formula: `(score - 10) / 2` (integer division).
     */
    fun modifierFor(ability: Ability): Int = when (ability) {
        Ability.STR -> (strength - 10) / 2
        Ability.DEX -> (dexterity - 10) / 2
        Ability.CON -> (constitution - 10) / 2
        Ability.INT -> (intelligence - 10) / 2
        Ability.WIS -> (wisdom - 10) / 2
        Ability.CHA -> (charisma - 10) / 2
    }
}

/**
 * Represents a saving throw configuration for a specific ability.
 *
 * @property ability The ability associated with the save.
 * @property bonus Total bonus to add to the d20 roll.
 * @property proficient Whether the character is proficient in this save.
 */
data class SavingThrowEntry(
    val ability: Ability,
    val bonus: Int = 0,
    val proficient: Boolean = false,
)

/**
 * Represents a skill configuration.
 *
 * @property skill The specific skill (e.g. Athletics, Stealth).
 * @property bonus Total bonus to add to the d20 roll.
 * @property proficient Whether the character is proficient in this skill.
 * @property expertise Whether the character has expertise (double proficiency) in this skill.
 */
data class SkillEntry(
    val skill: Skill,
    val bonus: Int = 0,
    val proficient: Boolean = false,
    val expertise: Boolean = false,
)

/**
 * Tracks success and failure counts for death saving throws.
 *
 * @property successes Number of successful saves (typically max 3).
 * @property failures Number of failed saves (typically max 3).
 */
data class DeathSaveState(
    val successes: Int = 0,
    val failures: Int = 0,
)

/**
 * Tracks usage of spell slots for a specific spell level.
 *
 * @property level The spell level (1-9).
 * @property total Total slots available at this level.
 * @property expended Number of slots used.
 */
data class SpellSlotState(
    val level: Int,
    val total: Int = 0,
    val expended: Int = 0,
)

/**
 * Represents a spell assigned to a character's spell list.
 *
 * @property spellId The unique identifier of the spell (e.g. "magic-missile").
 * @property sourceClass The class source for this spell (e.g. "Wizard"), if applicable.
 */
data class CharacterSpell(
    val spellId: String,
    val sourceClass: String = "",
)

/**
 * Represents a weapon carried by the character.
 *
 * @property id Unique instance ID for this weapon entry.
 * @property catalogId Optional ID referencing the standard weapon catalog.
 * @property name Display name of the weapon.
 * @property category Broad category (Simple, Martial, etc.).
 * @property categories Set of specific categories this weapon belongs to.
 * @property ability The ability used for attack and damage rolls.
 * @property proficient Whether the character is proficient with this weapon.
 * @property damageDiceCount Number of dice to roll for damage.
 * @property damageDieSize Size of the damage die (e.g. 6 for d6).
 * @property useAbilityForDamage Whether to add the ability modifier to damage.
 * @property damageType The type of damage dealt (Slashing, Piercing, etc.).
 */
data class Weapon(
    val id: String = UUID.randomUUID().toString(),
    val catalogId: String? = null,
    val name: String,
    val category: EquipmentCategory? = null,
    val categories: Set<EquipmentCategory> = emptySet(),
    val ability: Ability = Ability.STR,
    val proficient: Boolean = false,
    val damageDiceCount: Int = 1,
    val damageDieSize: Int = 6,
    val useAbilityForDamage: Boolean = true,
    val damageType: DamageType = DamageType.SLASHING,
)

/**
 * Returns a list of default [SpellSlotState] for levels 1 through 9.
 */
fun defaultSpellSlots(): List<SpellSlotState> =
    (1..9).map { level -> SpellSlotState(level = level) }

/**
 * Returns a list of default [SavingThrowEntry] for all abilities.
 */
fun defaultSavingThrows(): List<SavingThrowEntry> =
    Ability.entries.map { ability -> SavingThrowEntry(ability = ability) }

/**
 * Returns a list of default [SkillEntry] for all standard skills.
 */
fun defaultSkills(): List<SkillEntry> =
    Skill.entries.map { skill -> SkillEntry(skill = skill) }
