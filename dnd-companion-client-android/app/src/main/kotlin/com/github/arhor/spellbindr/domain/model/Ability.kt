package com.github.arhor.spellbindr.domain.model

import kotlinx.serialization.Serializable

typealias AbilityId = String

@Serializable
data class Ability(
    val id: AbilityId,
    val displayName: String,
    val description: List<String>,
) {
    val abbreviation: String
        get() = id.uppercase()

    val ref: EntityRef
        get() = EntityRef(id)
}

object AbilityIds {
    const val STR: AbilityId = "str"
    const val DEX: AbilityId = "dex"
    const val CON: AbilityId = "con"
    const val INT: AbilityId = "int"
    const val WIS: AbilityId = "wis"
    const val CHA: AbilityId = "cha"

    val standardOrder: List<AbilityId> = listOf(STR, DEX, CON, INT, WIS, CHA)
}

fun AbilityId.abbreviation(): String = uppercase()

fun AbilityId.displayName(): String = when (lowercase()) {
    AbilityIds.STR -> "Strength"
    AbilityIds.DEX -> "Dexterity"
    AbilityIds.CON -> "Constitution"
    AbilityIds.INT -> "Intelligence"
    AbilityIds.WIS -> "Wisdom"
    AbilityIds.CHA -> "Charisma"
    else -> abbreviation()
}
