package com.github.arhor.spellbindr.data.model.next

class CharacterCreationModel(
    val hp: Int = 0,
    val race: String? = null,
    val subrace: String? = null,
    val clazz: String? = null,
    val subclass: String? = null,
    val background: String? = null,
    val abilityScores: Map<String, Int> = emptyMap(),
    val proficiencies: Set<String> = emptySet(),
)
