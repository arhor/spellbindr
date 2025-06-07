package com.github.arhor.spellbindr.data.model

import com.github.arhor.spellbindr.util.copy
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Represents an effect that can be applied to a character's state.
 *
 * Effects are used to modify a character's abilities, proficiencies, or other attributes.
 * They are applied sequentially to the character's initial state to determine the current state.
 */
@Serializable
sealed interface Effect {
    fun applyTo(character: Character): Character


    /**
     * Represents an effect that modifies a character's ability score.
     *
     * @property ability The reference to the ability being affected.
     * @property value The amount by which the ability score is modified.
     */
    @Serializable
    data class AbilityEffect(
        val ability: EntityRef,
        val value: Int,
    ) : Effect {
        override fun applyTo(character: Character) = character.copy(
            abilities = character.abilities.copy {
                merge(ability, value, Int::plus)
            },
        )
    }

    /**
     * Represents an effect that grants proficiencies to a character.
     *
     * @property proficiencies A set of [EntityRef] objects representing the proficiencies granted by this effect.
     */
    @Serializable
    @SerialName("proficiency_effect")
    data class ProficienciesEffect(
        val proficiencies: Set<EntityRef>,
    ) : Effect {
        override fun applyTo(character: Character) = character.copy(
            proficiencies = character.proficiencies + proficiencies,
        )
    }

    /**
     * Represents an effect that grants a character knowledge of specific spells.
     *
     * @property knownSpells A set of [EntityRef] objects representing the spells the character learns.
     */
    @Serializable
    @SerialName("known_spells_effect")
    data class KnownSpellsEffect(
        val knownSpells: Set<EntityRef>,
    ) : Effect {
        override fun applyTo(character: Character) = character.copy(
            knownSpells = character.knownSpells + knownSpells,
        )
    }

    /**
     * Represents an effect that grants a resistance to a character.
     *
     * @property resistance The reference to the resistance being granted.
     */
    @Serializable
    @SerialName("resistance_effect")
    data class ResistanceEffect(
        val resistance: EntityRef,
    ) : Effect {
        override fun applyTo(character: Character) = character.copy(
            resistances = character.resistances + resistance,
        )
    }
}
