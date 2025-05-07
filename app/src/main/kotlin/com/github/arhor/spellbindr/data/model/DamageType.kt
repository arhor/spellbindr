package com.github.arhor.spellbindr.data.model

@Suppress("unused")
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
    NONE,
    ;

    override fun toString(): String = name.lowercase().replaceFirstChar(Char::titlecaseChar)
}
