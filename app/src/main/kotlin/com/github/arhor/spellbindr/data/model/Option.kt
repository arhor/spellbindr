package com.github.arhor.spellbindr.data.model

import com.github.arhor.spellbindr.domain.model.EntityRef as DomainEntityRef
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable


/**
 * Represents a generic option that can be one of several types.
 * This sealed class is used to model different kinds of choices or selections within the game data.
 *
 * Each subclass represents a specific type of option:
 * - [ReferenceOption]: An option that refers to another entity.
 * - [ActionOption]: An option that describes an action.
 * - [MultipleOption]: An option that groups multiple other options.
 * - [StringOption]: An option that is a simple string value.
 * - [IdealOption]: An option representing an ideal with a description and alignments.
 * - [CountedReferenceOption]: An option that refers to a counted number of entities, possibly with prerequisites.
 * - [ScorePrerequisiteOption]: An option that defines a prerequisite based on an ability score.
 * - [AbilityBonusOption]: An option that provides a bonus to an ability score.
 * - [BreathOption]: An option representing a breath weapon.
 * - [DamageOption]: An option describing damage details.
 * - [ChoiceOption]: An option that encapsulates a more complex choice structure.
 */
@Serializable
sealed class Option {

    /**
     * Represents an option that refers to another entity.
     *
     * This is commonly used to link to items, spells, features, etc.
     *
     * @property item A reference to the entity being linked.
     */
    @Serializable
    @SerialName("reference")
    data class ReferenceOption(
        val item: DomainEntityRef
    ) : Option()

    /**
     * Represents an action option within the game.
     *
     * This option defines a specific action a character or creature can take, along with its properties.
     *
     * @property actionName The name of the action.
     * @property count An optional integer indicating how many times this action can be used or occurs.
     * @property type The type of the action (e.g., "melee", "ranged", "ability").
     * @property notes Optional additional information or context about the action.
     */
    @Serializable
    @SerialName("action")
    data class ActionOption(
        val actionName: String,
        val count: Int? = null,
        val type: String,
        val notes: String? = null
    ) : Option()

    /**
     * Represents an option that itself contains a list of other options.
     * This is used when a feature or choice offers multiple distinct benefits or selections.
     *
     * @property items A list of [Option] objects that make up this multiple option.
     */
    @Serializable
    @SerialName("multiple")
    data class MultipleOption(
        val items: List<Option>
    ) : Option()

    /**
     * Represents an option that is a simple string.
     *
     * @property string The string value of the option.
     */
    @Serializable
    @SerialName("string")
    data class StringOption(
        val string: String
    ) : Option()

    /**
     * Represents an ideal option, often associated with character backgrounds or alignments.
     *
     * @property desc A description of the ideal.
     * @property alignments A list of alignments that this ideal is typically associated with.
     *                    Each alignment is represented by an [DomainEntityRef].
     */
    @Serializable
    @SerialName("ideal")
    data class IdealOption(
        val desc: String,
        val alignments: List<DomainEntityRef>
    ) : Option()

    /**
     * Represents an option that provides a certain number of references to an entity,
     * potentially with prerequisites.
     *
     * @property count The number of references provided by this option.
     * @property of An [DomainEntityRef] pointing to the entity being referenced.
     * @property prerequisites An optional list of [CountedReferencePrerequisite] objects
     * that must be met for this option to be available.
     */
    @Serializable
    @SerialName("counted_reference")
    data class CountedReferenceOption(
        val count: Int,
        val of: DomainEntityRef,
        val prerequisites: List<CountedReferencePrerequisite>? = null
    ) : Option()

    /**
     * Represents a prerequisite option that requires a minimum score in a specific ability.
     *
     * @property abilityScore A reference to the ability score (e.g., Strength, Dexterity).
     * @property minimumScore The minimum score required in the ability.
     */
    @Serializable
    @SerialName("score_prerequisite")
    data class ScorePrerequisiteOption(
        val abilityScore: DomainEntityRef,
        val minimumScore: Int
    ) : Option()

    /**
     * Represents an option that grants a bonus to a specific ability score.
     * This can be used for features, traits, or other game mechanics that modify ability scores.
     *
     * @property abilityScore A reference to the ability score that receives the bonus (e.g., Strength, Dexterity).
     * @property bonus The numerical value of the bonus to be applied to the ability score.
     */
    @Serializable
    @SerialName("ability_bonus")
    data class AbilityBonusOption(
        val abilityScore: DomainEntityRef,
        val bonus: Int
    ) : Option()

    /**
     * Represents a breath weapon option.
     *
     * @property name The name of the breath weapon.
     * @property dc The difficulty class (DC) for the saving throw against the breath weapon.
     * @property damage The damage dealt by the breath weapon. This is optional.
     */
    @Serializable
    @SerialName("breath")
    data class BreathOption(
        val name: String,
        val dc: DifficultyClass,
        val damage: List<Damage>? = null
    ) : Option()

    /**
     * Represents an option that deals damage.
     *
     * @property damageType The type of damage dealt.
     * @property damageDice The dice rolled to determine the amount of damage.
     * @property notes Optional notes about the damage.
     */
    @Serializable
    @SerialName("damage")
    data class DamageOption(
        val damageType: DomainEntityRef,
        val damageDice: String,
        val notes: String? = null
    ) : Option()

    /**
     * Represents an option that allows a user to make a choice from a predefined set of options.
     *
     * @property choice The [Choice] object detailing the options available.
     */
    @Serializable
    @SerialName("choice")
    data class ChoiceOption(
        val choice: Choice
    ) : Option()
}
