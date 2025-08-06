package com.github.arhor.spellbindr.data.model.predefined

import kotlinx.serialization.Serializable

/**
 * Represents a type of damage.
 *
 * @property displayName The name of the damage type.
 * @property description A list of strings describing the damage type.
 */
@Serializable
enum class DamageType(
    val displayName: String,
    val description: List<String>
) {
    ACID(
        displayName = "Acid",
        description = listOf(
            "The corrosive spray of a black dragon's breath and the dissolving enzymes secreted by a black pudding deal acid damage."
        )
    ),
    BLUDGEONING(
        displayName = "Bludgeoning",
        description = listOf(
            "Blunt force attacks, falling, constriction, and the like deal bludgeoning damage."
        )
    ),
    COLD(
        displayName = "Cold",
        description = listOf(
            "The infernal chill radiating from an ice devil's spear and the frigid blast of a white dragon's breath deal cold damage."
        )
    ),
    FIRE(
        displayName = "Fire",
        description = listOf(
            "Red dragons breathe fire, and many spells conjure flames to deal fire damage."
        )
    ),
    FORCE(
        displayName = "Force",
        description = listOf(
            "Force is pure magical energy focused into a damaging form. Most effects that deal force damage are spells, including magic missile and spiritual weapon."
        )
    ),
    LIGHTNING(
        displayName = "Lightning",
        description = listOf(
            "A lightning bolt spell and a blue dragon's breath deal lightning damage."
        )
    ),
    NECROTIC(
        displayName = "Necrotic",
        description = listOf(
            "Necrotic damage, dealt by certain undead and a spell such as chill touch, withers matter and even the soul."
        )
    ),
    PIERCING(
        displayName = "Piercing",
        description = listOf(
            "Puncturing and impaling attacks, including spears and monsters' bites, deal piercing damage."
        )
    ),
    POISON(
        displayName = "Poison",
        description = listOf(
            "Venomous stings and the toxic gas of a green dragon's breath deal poison damage."
        )
    ),
    PSYCHIC(
        displayName = "Psychic",
        description = listOf(
            "Mental abilities such as a psionic blast deal psychic damage."
        )
    ),
    RADIANT(
        displayName = "Radiant",
        description = listOf(
            "Radiant damage, dealt by a cleric's flame strike spell or an angel's smiting weapon, sears the flesh like fire and overloads the spirit with power."
        )
    ),
    SLASHING(
        displayName = "Slashing",
        description = listOf(
            "Swords, axes, and monsters' claws deal slashing damage."
        )
    ),
    THUNDER(
        displayName = "Thunder",
        description = listOf(
            "A concussive burst of sound, such as the effect of the thunderwave spell, deals thunder damage."
        )
    );
}
