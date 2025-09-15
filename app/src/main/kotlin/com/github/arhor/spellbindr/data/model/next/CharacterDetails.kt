package com.github.arhor.spellbindr.data.model.next

data class CharacterDetails(
    val hp: Int,
    val name: String,
    val race: Reference,
    val subrace: Reference? = null,
    val classes: Map<Reference, Int>,
    val subclasses: Set<Reference> = emptySet(),
    val feats: Set<Reference> = emptySet(),
    val background: Reference,
    val abilityScores: Map<Reference, Int>,
    val proficiencies: Set<Reference>,
    val spells: Set<Reference> = emptySet(),

    ) {
    val level: Int
        get() = classes.values.sum()
}
