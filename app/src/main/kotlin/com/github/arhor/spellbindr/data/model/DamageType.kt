package com.github.arhor.spellbindr.data.model

enum class DamageType {
    ACID,
    BLUDGEONING,
    COLD,
    FIRE,
    FORCE,
    LIGHTNING,
    NECROTIC,
    PIERCING,
    POISON,
    PSYCHIC,
    RADIANT,
    SLASHING,
    THUNDER,
    NONE;

    override fun toString(): String =
        name.lowercase()
            .replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
}
