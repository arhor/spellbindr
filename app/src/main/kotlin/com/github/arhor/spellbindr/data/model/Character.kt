package com.github.arhor.spellbindr.data.model

import kotlinx.serialization.Serializable

/**
 * Main data class representing a D&D 5e character in the Spellbindr app.
 *
 * Stores all core properties needed to fully describe a character,
 * their current state, and progression throughout the campaign.
 *
 * ```
 * val character = Character(
 *     id = "7d47f30b-3e21-4b19-8a71-ef4b642e3170",
 *     name = "Aelar Brightblade",
 *     race = EntityRef("elf"),
 *     subrace = EntityRef("high_elf"),
 *     alignment = EntityRef("chaotic_good"),
 *     background = EntityRef("acolyte"),
 *     classes = mapOf(EntityRef("wizard") to 5),
 *     experience = 6500,
 *     currentHitPoints = 28,
 *     maximumHitPoints = 28,
 *     abilities = mapOf(
 *         EntityRef("STR") to 10,
 *         EntityRef("DEX") to 16,
 *         EntityRef("CON") to 13,
 *         EntityRef("INT") to 18,
 *         EntityRef("WIS") to 12,
 *         EntityRef("CHA") to 14,
 *     ),
 *     proficiencies = setOf(EntityRef("arcana"), EntityRef("history")),
 *     feats = setOf(EntityRef("war_caster")),
 *     knownSpells = setOf(EntityRef("fireball"), EntityRef("mage_armor")),
 *     preparedSpells = setOf(EntityRef("fireball"))
 * )
 * ```
 *
 * @property id Unique identifier of the character (UUID or other).
 * @property name Character's name.
 * @property race Reference to the character's main race.
 * @property subrace Reference to the character's subrace, if any.
 * @property alignment Reference to the character's alignment (lawful good, chaotic neutral, etc).
 * @property background Reference to the character's chosen background.
 * @property classes Map of character class references to levels.
 *                   Supports multiclassing. Example: { Paladin: 2, Warlock: 1 }.
 * @property experience Current amount of XP points earned by the character.
 * @property currentHitPoints Character's current HP (after damage, healing, etc).
 * @property maximumHitPoints Character's maximum HP (as calculated from class, CON, features).
 * @property abilities Map of ability score references (e.g., STR, DEX) to values.
 *                     Example: { STR: 16, DEX: 14, CON: 13, INT: 10, WIS: 12, CHA: 18 }.
 * @property proficiencies Set of references to all proficiencies the character possesses
 *                         (skills, tools, saving throws, armor, weapons, etc).
 * @property feats Set of references to all feats selected or granted to the character.
 * @property knownSpells Set of references to all spells known (learned) by the character.
 * @property preparedSpells Set of references to all spells currently prepared (for classes like Cleric, Druid, Wizard).
 *
 * @constructor Creates a new Character with all key D&D attributes.
 *
 *
 * @property level Computed property returning the sum of all class levels (total character level).
 */
@Serializable
data class Character(
    val id: String,
    val name: String,
    val race: EntityRef,
    val subrace: EntityRef?,
    val alignment: EntityRef,
    val background: EntityRef,
    val classes: Map<EntityRef, Int>,
    val experience: Int,
    val currentHitPoints: Int,
    val maximumHitPoints: Int,
    val abilities: Map<EntityRef, Int>,
    val proficiencies: Set<EntityRef>,
    val feats: Set<EntityRef>,
    val knownSpells: Set<EntityRef>,
    val preparedSpells: Set<EntityRef>,
    val resistances: Set<EntityRef>,
) {
    val level: Int
        get() = classes.values.sum()
}
