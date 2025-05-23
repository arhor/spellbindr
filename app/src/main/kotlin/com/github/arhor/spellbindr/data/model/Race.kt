@file:Suppress("unused")

package com.github.arhor.spellbindr.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Represents a playable race, including all its traits and subraces.
 *
 * @property name The name of the race.
 * @property source The reference source (e.g. "PHB").
 * @property traits A list of unique racial features and bonuses.
 * @property subraces Optional subraces available for this race.
 */
@Serializable
data class Race(
    val name: String,
    val source: String,
    val traits: List<Trait>,
    val subraces: List<Subrace> = emptyList()
)

/**
 * Represents a subrace, including its unique features and bonuses.
 *
 * @property name The subrace name.
 * @property source The reference source.
 * @property traits A list of subrace-specific features and bonuses.
 */
@Serializable
data class Subrace(
    val name: String,
    val source: String,
    val traits: List<Trait>
)

/**
 * Base sealed class for all race and subrace traits.
 * Each trait has a name, an optional description, and may define a specific effect.
 */
@Serializable
sealed class Trait {
    /** Trait name (e.g., "Darkvision", "Tool Proficiency") */
    abstract val name: String

    /** Trait description in plain text */
    abstract val desc: String

    /**
     * Generic trait with only name and description.
     */
    @Serializable
    @SerialName("generic")
    data class GenericTrait(
        override val name: String,
        override val desc: String,
    ) : Trait()

    /**
     * Ability score bonus trait (e.g. +2 CON).
     */
    @Serializable
    @SerialName("ability_score")
    data class AbilityScoreTrait(
        override val name: String,
        override val desc: String,
        val effect: AbilityEffect
    ) : Trait()

    /**
     * Size trait (e.g. Medium).
     */
    @Serializable
    @SerialName("size")
    data class SizeTrait(
        override val name: String,
        override val desc: String,
        val effect: SizeEffect
    ) : Trait()

    /**
     * Speed trait (e.g. base walking speed).
     */
    @Serializable
    @SerialName("speed")
    data class SpeedTrait(
        override val name: String,
        override val desc: String,
        val effect: SpeedEffect
    ) : Trait()

    /**
     * Language proficiency trait.
     */
    @Serializable
    @SerialName("languages")
    data class LanguageTrait(
        override val name: String,
        override val desc: String,
        val effect: LanguageEffect
    ) : Trait()

    /**
     * Weapon proficiency trait (may be fixed or chosen).
     */
    @Serializable
    @SerialName("weapon_proficiency")
    data class WeaponProficiencyTrait(
        override val name: String,
        override val desc: String,
        val effect: WeaponProficiencyEffect
    ) : Trait()

    /**
     * Tool proficiency trait (may be fixed or chosen).
     */
    @Serializable
    @SerialName("tool_proficiency")
    data class ToolProficiencyTrait(
        override val name: String,
        override val desc: String,
        val effect: ToolProficiencyEffect
    ) : Trait()

    /**
     * Skill proficiency trait (may be fixed or chosen).
     */
    @Serializable
    @SerialName("skill_proficiency")
    data class SkillProficiencyTrait(
        override val name: String,
        override val desc: String,
        val effect: SkillProficiencyEffect
    ) : Trait()

    /**
     * Draconic ancestry feature (for dragonborn).
     */
    @Serializable
    @SerialName("draconic_ancestry")
    data class DraconicAncestryTrait(
        override val name: String,
        override val desc: String,
        val effect: DraconicAncestryEffect
    ) : Trait()

    /**
     * Trait granting innate spells, cantrips, or magical features.
     */
    @Serializable
    @SerialName("spellcasting")
    data class SpellcastingTrait(
        override val name: String,
        override val desc: String,
        val effect: SpellcastingEffect
    ) : Trait()
}

/**
 * Effect for spell features (see: Tiefling, High Elf, etc.).
 *
 * @property spells List of spells granted or choices to be made.
 * @property ability The spellcasting ability used for these spells (e.g., CHA, INT).
 */
@Serializable
data class SpellcastingEffect(
    val spells: List<SpellGrant>,
    val ability: Ability,
)

/**
 * Base sealed class for spell-granting effects. Grants a spell to a character, either as a fixed spell or as a choice from a list.
 * - [Fixed]: Grants a specific spell.
 * - [Choice]: Allows to choose from a spell list, with possible restrictions.
 */
@Serializable
sealed class SpellGrant {
    /** Minimum character level required to obtain this spell. */
    abstract val minCharacterLevel: Int

    /** Usage restriction: at-will, per long rest, etc. */
    abstract val usage: SpellUsage

    /**
     * Grants a specific spell.
     * @property spell The spell's unique name (must match your spell DB).
     */
    @Serializable
    @SerialName("fixed")
    data class Fixed(
        val spell: String,
        override val minCharacterLevel: Int = 1,
        override val usage: SpellUsage = SpellUsage.AT_WILL,
    ) : SpellGrant()

    /**
     * Grants a choice of spells from a specific class's spell list and level.
     *
     * @property count Number of spells to choose.
     * @property level Spell level to choose (0 = cantrip, 1 = first level, etc.).
     * @property spellcastingClass Name of the spellcasting class (e.g., "wizard").
     */
    @Serializable
    @SerialName("choice")
    data class Choice(
        val count: Int,
        val level: Int,
        val spellcastingClass: String,
        override val minCharacterLevel: Int = 1,
        override val usage: SpellUsage = SpellUsage.AT_WILL,
    ) : SpellGrant()
}

/**
 * Describes how often a granted spell can be cast.
 */
@Serializable
enum class SpellUsage {
    AT_WILL,
    LONG_REST,
    SHORT_REST
}

/**
 * Machine-readable ability score bonus effect.
 *
 * Example: { "abilities": { "CON": 2 } }
 */
@Serializable
data class AbilityEffect(
    val abilities: List<Map<AbilityOption, Int>>
)

/**
 * Machine-readable size effect.
 */
@Serializable
data class SizeEffect(
    val size: Size
)

/** Represents D&D creature sizes. */
@Serializable
enum class Size {
    SMALL,
    MEDIUM,
    LARGE
}

/** Machine-readable speed effect. */
@Serializable
data class SpeedEffect(
    val speed: Int
)

/**
 * Grants language proficiencies.
 *
 * [Choice] allows for "pick any N", "pick from options", or "all listed" (see docs below).
 */
@Serializable
data class LanguageEffect(
    val languages: List<Choice<String>>
)

/**
 * Grants weapon proficiencies.
 *
 * [Choice] allows for "pick any N", "pick from options", or "all listed".
 */
@Serializable
data class WeaponProficiencyEffect(
    val weapons: List<Choice<String>>
)

/**
 * Grants tool proficiencies.
 *
 * [Choice] allows for "pick any N", "pick from options", or "all listed".
 */
@Serializable
data class ToolProficiencyEffect(
    val tools: List<Choice<String>>
)

/**
 * Grants skill proficiencies.
 *
 * [Choice] allows for "pick any N", "pick from options", or "all listed".
 */
@Serializable
data class SkillProficiencyEffect(
    val skills: List<Choice<String>>,
)

/**
 * Describes draconic ancestry options for Dragonborn.
 */
@Serializable
data class DraconicAncestryEffect(
    val ancestry: Choice<DraconicAncestryOption>,
)

/**
 * Describes a specific draconic ancestry.
 */
@Serializable
data class DraconicAncestryOption(
    val name: String,
    val type: String,
    val breathWeapon: String,
)

/**
 * Represents a choice from a group (languages, tools, weapons, skills, etc.).
 *
 * If [choose] is null and [options] is non-null: all listed are granted, no choice.
 * If [choose] is non-null and [options] is null: choose from all available in that category (e.g., "choose any language").
 * If [choose] and [options] are both set: choose N from options.
 */
@Serializable
data class Choice<T>(
    val choose: Int? = null,
    val options: List<T>? = null,
)

@Serializable
enum class AbilityOption {
    STR,
    DEX,
    CON,
    INT,
    WIS,
    CHA,
    ANY,
    ALL,
}
