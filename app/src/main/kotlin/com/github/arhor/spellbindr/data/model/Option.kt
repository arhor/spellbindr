package com.github.arhor.spellbindr.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
sealed class Option {

    @Serializable
    @SerialName("reference")
    data class ReferenceOption(

        val item: EntityRef
    ) : Option()

    @Serializable
    @SerialName("action")
    data class ActionOption(
        @SerialName("action_name")
        val actionName: String,
        val count: Int? = null,
        val type: String,
        val notes: String? = null
    ) : Option()

    @Serializable
    @SerialName("multiple")
    data class MultipleOption(
        val items: List<Option>
    ) : Option()

    @Serializable
    @SerialName("string")
    data class StringOption(
        val string: String
    ) : Option()

    @Serializable
    @SerialName("ideal")
    data class IdealOption(
        val desc: String,
        val alignments: List<EntityRef>
    ) : Option()

    @Serializable
    @SerialName("counted_reference")
    data class CountedReferenceOption(
        val count: Int,
        val of: EntityRef,
        val prerequisites: List<CountedReferencePrerequisite>? = null
    ) : Option()

    @Serializable
    @SerialName("score_prerequisite")
    data class ScorePrerequisiteOption(
        val abilityScore: EntityRef,
        val minimumScore: Int
    ) : Option()

    @Serializable
    @SerialName("ability_bonus")
    data class AbilityBonusOption(
        val abilityScore: EntityRef,
        val bonus: Int
    ) : Option()

    @Serializable
    @SerialName("breath")
    data class BreathOption(
        val name: String,
        val dc: DifficultyClass,
        val damage: List<Damage>? = null
    ) : Option()

    @Serializable
    @SerialName("damage")
    data class DamageOption(
        val damageType: EntityRef,
        val damageDice: String,
        val notes: String? = null
    ) : Option()

    @Serializable
    @SerialName("choice")
    data class ChoiceOption(
        val choice: Choice
    ) : Option()
}
