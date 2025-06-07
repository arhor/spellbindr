package com.github.arhor.spellbindr.data.model

import com.github.arhor.spellbindr.util.copy
import kotlinx.serialization.Serializable

/**
 * Represents an effect that can be applied to a character's state.
 *
 * Effects are used to modify a character's abilities, proficiencies, or other attributes.
 * They are applied sequentially to the character's initial state to determine the current state.
 *
 * @property givenBy The source that granted this effect, if any.
 */
@Serializable
sealed interface Effect {
    val givenBy: Source?
    fun applyTo(state: Character.State): Character.State

    //    val currentState: State
//        get() = effects.fold(initial = State()) { state, effect -> effect.applyTo(state) }

    /**
     * Represents the source of an effect.
     *
     * This class is used to track where an effect originated from, such as a feat, feature, or trait.
     *
     * @property type The type of the source.
     * @property link A reference to the specific entity that is the source of the effect.
     */
    @Serializable
    data class Source(
        val type: Type,
        val link: EntityRef
    ) {
        /**
         * Enumerates the different types of effects.
         *
         * - `FEAT`: Represents an effect granted by a feat.
         * - `FEATURE`: Represents an effect granted by a class feature or similar.
         * - `TRAIT`: Represents an effect granted by a racial trait or similar.
         */
        enum class Type {
            FEAT,
            FEATURE,
            TRAIT,
        }
    }

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
        override val givenBy: Source?,
    ) : Effect {
        override fun applyTo(state: Character.State) = state.copy(abilities = state.abilities.copy {
            merge(ability, value, Int::plus)
        })
    }

    /**
     * Represents an effect that grants proficiencies to a character.
     *
     * @property proficiencies A set of [EntityRef] objects representing the proficiencies granted by this effect.
     */
    @Serializable
    data class ProficienciesEffect(
        val proficiencies: Set<EntityRef>,
        override val givenBy: Source?,
    ) : Effect {
        override fun applyTo(state: Character.State) = state.copy(proficiencies = state.proficiencies + proficiencies)
    }
}
