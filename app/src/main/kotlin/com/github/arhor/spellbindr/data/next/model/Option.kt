package com.github.arhor.spellbindr.data.next.model

import com.github.arhor.spellbindr.data.newModel.Damage
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
sealed class Option {
    abstract val optionType: String

    @Serializable
    open class BaseOption(
        @SerialName("option_type")
        override val optionType: String
    ) : Option()

    @Serializable
    @SerialName("reference")
    data class ReferenceOption(
        @SerialName("option_type")
        override val optionType: String = "reference",
        val item: EntityRef
    ) : Option()

    @Serializable
    @SerialName("action")
    data class ActionOption(
        @SerialName("option_type")
        override val optionType: String = "action",
        @SerialName("action_name")
        val actionName: String,
        val count: Int? = null,
        val type: String,
        val notes: String? = null
    ) : Option()

    @Serializable
    @SerialName("multiple")
    data class MultipleOption(
        @SerialName("option_type")
        override val optionType: String = "multiple",
        val items: List<Option>
    ) : Option()

    @Serializable
    @SerialName("string")
    data class StringOption(
        @SerialName("option_type")
        override val optionType: String = "string",
        val string: String
    ) : Option()

    @Serializable
    @SerialName("ideal")
    data class IdealOption(
        @SerialName("option_type")
        override val optionType: String = "ideal",
        val desc: String,
        val alignments: List<EntityRef>
    ) : Option()

    @Serializable
    @SerialName("counted_reference")
    data class CountedReferenceOption(
        @SerialName("option_type")
        override val optionType: String = "counted_reference",
        val count: Int,
        val of: EntityRef,
        val prerequisites: List<CountedReferencePrerequisite>? = null
    ) : Option()

    @Serializable
    @SerialName("score_prerequisite")
    data class ScorePrerequisiteOption(
        @SerialName("option_type")
        override val optionType: String = "score_prerequisite",
        val abilityScore: EntityRef,
        val minimumScore: Int
    ) : Option()

    @Serializable
    @SerialName("ability_bonus")
    data class AbilityBonusOption(
        @SerialName("option_type")
        override val optionType: String = "ability_bonus",
        val abilityScore: EntityRef,
        val bonus: Int
    ) : Option()

    @Serializable
    @SerialName("breath")
    data class BreathOption(
        @SerialName("option_type")
        override val optionType: String = "breath",
        val name: String,
        val dc: DifficultyClass,
        val damage: List<Damage>? = null
    ) : Option()

    @Serializable
    @SerialName("damage")
    data class DamageOption(
        @SerialName("option_type")
        override val optionType: String = "damage",
        val damageType: EntityRef,
        @SerialName("damage_dice")
        val damageDice: String,
        val notes: String? = null
    ) : Option()

    @Serializable
    @SerialName("choice")
    data class ChoiceOption(
        @SerialName("option_type")
        override val optionType: String = "choice",
        val choice: Choice
    ) : Option()
}
