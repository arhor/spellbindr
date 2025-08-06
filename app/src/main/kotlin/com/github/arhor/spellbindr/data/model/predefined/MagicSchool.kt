package com.github.arhor.spellbindr.data.model.predefined

import com.github.arhor.spellbindr.utils.CaseInsensitiveEnumSerializer
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable

/**
 * Represents the different schools of magic.
 * Each school of magic has a unique focus and set of associated spells.
 *
 * @property displayName The user-friendly name of the magic school.
 * @property description A list of strings describing the characteristics and typical effects of spells from this school.
 */
@Serializable(with = MagicSchool.Companion::class)
enum class MagicSchool(
    val displayName: String,
    val description: List<String>
) {
    ABJURATION(
        displayName = "Abjuration",
        description = listOf(
            "Abjuration spells are protective in nature, though some of them have aggressive uses. They create magical barriers, negate harmful effects, harm trespassers, or banish creatures to other planes of existence."
        )
    ),
    CONJURATION(
        displayName = "Conjuration",
        description = listOf(
            "Conjuration spells involve the transportation of objects and creatures from one location to another. Some spells summon creatures or objects to the caster's side, whereas others allow the caster to teleport to another location. Some conjurations create objects or effects out of nothing."
        )
    ),
    DIVINATION(
        displayName = "Divination",
        description = listOf(
            "Divination spells reveal information, whether in the form of secrets long forgotten, glimpses of the future, the locations of hidden things, the truth behind illusions, or visions of distant people or places."
        )
    ),
    ENCHANTMENT(
        displayName = "Enchantment",
        description = listOf(
            "Enchantment spells affect the minds of others, influencing or controlling their behavior. Such spells can make enemies see the caster as a friend, force creatures to take a course of action, or even control another creature like a puppet."
        )
    ),
    EVOCATION(
        displayName = "Evocation",
        description = listOf(
            "Evocation spells manipulate magical energy to produce a desired effect. Some call up blasts of fire or lightning. Others channel positive energy to heal wounds."
        )
    ),
    ILLUSION(
        displayName = "Illusion",
        description = listOf(
            "Illusion spells deceive the senses or minds of others. They cause people to see things that are not there, to miss things that are there, to hear phantom noises, or to remember things that never happened. Some illusions create phantom images that any creature can see, but the most insidious illusions plant an image directly in the mind of a creature."
        )
    ),
    NECROMANCY(
        displayName = "Necromancy",
        description = listOf(
            "Necromancy spells manipulate the energies of life and death. Such spells can grant an extra reserve of life force, drain the life energy from another creature, create the undead, or even bring the dead back to life."
        )
    ),
    TRANSMUTATION(
        displayName = "Transmutation",
        description = listOf(
            "Transmutation spells change the properties of a creature, object, or environment. They might turn an enemy into a harmless creature, bolster the strength of an ally, make an object move at the caster's command, or enhance a creature's innate healing abilities to rapidly recover from injury."
        )
    );

    companion object : KSerializer<MagicSchool> by CaseInsensitiveEnumSerializer()
}
