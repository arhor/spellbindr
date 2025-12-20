package com.github.arhor.spellbindr.domain.model

/**
 * Main domain model representing a D&D 5e character in the Spellbindr app.
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
    val level: Int
        get() = classes.values.sum()

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

    fun createState() = State(level = level)
}
