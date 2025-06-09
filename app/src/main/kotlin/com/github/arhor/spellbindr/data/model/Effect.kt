@file:Suppress("unused")

package com.github.arhor.spellbindr.data.model

import com.github.arhor.spellbindr.util.copy
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Represents an effect that can be applied to a state's state.
 *
 * Effects are used to modify a state's abilities, proficiencies, or other attributes.
 * They are applied sequentially to the state's initial state to determine the current state.
 */
@Serializable
sealed interface Effect {
    fun applyTo(state: Character.State): Character.State

    /**
     * Represents an effect that modifies a state's ability scores.
     *
     * @property abilities A map where each key is an [EntityRef] to the ability being affected,
     *                     and each value is the amount by which the ability score is modified.
     */
    @Serializable
    @SerialName("modify-abilities")
    data class ModifyAbilityEffect(
        val abilities: Map<EntityRef, Int>,
    ) : Effect {
        override fun applyTo(state: Character.State) = state.copy(
            abilityScores = state.abilityScores.copy {
                abilities.forEach { (ability, value) ->
                    merge(ability, value, Int::plus)
                }
            },
        )
    }

    /**
     * Represents an effect that grants proficiencies to a state.
     *
     * @property proficiencies A set of IDs representing the proficiencies granted by this effect.
     */
    @Serializable
    @SerialName("add-proficiencies")
    data class AddProficienciesEffect(
        val proficiencies: Set<String>,
    ) : Effect {
        override fun applyTo(state: Character.State) = state.copy(
            proficiencies = state.proficiencies + proficiencies.map(::EntityRef),
        )
    }

    /**
     * Represents an effect that grants a state knowledge of specific spells.
     *
     * @property spells A set of [EntityRef] objects representing the spells the state learns.
     */
    @Serializable
    @SerialName("add-known-spells")
    data class AddKnownSpellsEffect(
        val spells: Set<EntityRef>,
    ) : Effect {
        override fun applyTo(state: Character.State) = state.copy(
            knownSpells = state.knownSpells + spells,
        )
    }

    /**
     * Represents an effect that grants a resistance to a state.
     *
     * @property damageType The reference to the damage type for which resistance being granted.
     */
    @Serializable
    @SerialName("add-resistance")
    data class AddResistanceEffect(
        val damageType: EntityRef,
    ) : Effect {
        override fun applyTo(state: Character.State) = state.copy(
            resistances = state.resistances + damageType,
        )
    }

    @Serializable
    @SerialName("add-hp")
    data class AddHpEffect(
        val value: Int,
        val perLevel: Boolean = false,
    ) : Effect {
        override fun applyTo(state: Character.State) = state.copy(
            maximumHitPoints = state.maximumHitPoints + state.level * value,
        )
    }

    @Serializable
    @SerialName("add-condition-immunity")
    data class AddConditionImmunityEffect(
        val condition: EntityRef,
    ) : Effect {
        override fun applyTo(state: Character.State) = state.copy(
            conditionImmunities = state.conditionImmunities + condition,
        )
    }

    @Serializable
    @SerialName("add-action")
    data class AddActionEffect(
        val actions: List<Action>,
    ) : Effect {
        override fun applyTo(state: Character.State) = state.copy(
            actions = state.actions + actions,
        )
    }

    @Serializable
    @SerialName("add-languages")
    data class AddLanguagesEffect(
        val languages: Set<String>,
    ) : Effect {
        override fun applyTo(state: Character.State) = state.copy(
            languages = state.languages + languages.map(::EntityRef),
        )
    }

    @Serializable
    @SerialName("modify-size")
    data class ModifySizeEffect(
        val size: String,
    ) : Effect {
        override fun applyTo(state: Character.State) = state.copy(
            size = size,
        )
    }

    @Serializable
    @SerialName("modify-speed")
    data class ModifySpeedEffect(
        val speed: Int,
    ) : Effect {
        override fun applyTo(state: Character.State) = state.copy(
            speed = speed,
        )
    }
}

