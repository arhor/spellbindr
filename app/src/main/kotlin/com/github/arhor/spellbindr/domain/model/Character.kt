package com.github.arhor.spellbindr.domain.model

/**
 * Main domain model representing a D&D 5e character in the Spellbindr app.
 *
 * This model captures the static definition of a character (race, class, background, etc.)
 * as well as computed/derived state in [State].
 *
 * @property id Unique identifier for the character.
 * @property name Character's name.
 * @property race Reference to the character's race.
 * @property subrace Optional reference to a subrace.
 * @property classes Map of class references to level in that class.
 * @property background Reference to the character's background.
 * @property abilityScores Base ability scores map.
 * @property proficiencies Set of skill/tool proficiencies.
 * @property equipment Set of equipment references.
 * @property inventory Map of item references to quantity.
 * @property spells Set of known spell references.
 */
data class Character(
    val id: String,
    val name: String = "",
    val race: EntityRef,
    val subrace: EntityRef? = null,
    val classes: Map<EntityRef, Int>,
    val background: EntityRef,
    val abilityScores: Map<EntityRef, Int>,
    val proficiencies: Set<EntityRef>,
    val equipment: Set<EntityRef> = emptySet(),
    val inventory: Map<EntityRef, Int> = emptyMap(),
    val spells: Set<EntityRef> = emptySet(),
) {
    /**
     * Total character level (sum of all class levels).
     */
    val level: Int
        get() = classes.values.sum()

    /**
     * Computed runtime state of a character, including derived stats like max HP, AC, etc.
     * This state is typically derived from the base [Character] data plus rules/modifiers.
     */
    data class State(
        val size: String = "medium",
        val level: Int = 1,
        val speed: Int = 30,
        val actions: Set<com.github.arhor.spellbindr.data.model.Action> = emptySet(),
        val languages: Set<EntityRef> = emptySet(),
        val knownSpells: Set<EntityRef> = emptySet(),
        val resistances: Set<EntityRef> = emptySet(),
        val abilityScores: Map<EntityRef, Int> = emptyMap(),
        val proficiencies: Set<EntityRef> = emptySet(),
        val currentHitPoints: Int = 0,
        val maximumHitPoints: Int = 0,
        val conditionImmunities: Set<EntityRef> = emptySet(),
    )

    /**
     * Creates a default initial state for this character.
     */
    fun createState() = State(level = level)
}
