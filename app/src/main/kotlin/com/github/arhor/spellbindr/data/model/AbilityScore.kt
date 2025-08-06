package com.github.arhor.spellbindr.data.model

import kotlinx.serialization.Serializable

/**
 * Represents an ability score in Dungeons & Dragons.
 *
 * Each ability score describes a fundamental aspect of a character or monster,
 * influencing their capabilities in various situations.
 *
 * @property displayName The full, display-friendly name of the ability score.
 * @property description A detailed explanation of what the ability score measures and how it's used.
 * @property associatedSkills A list of skill names that are typically associated with this ability score.
 */
@Serializable
enum class AbilityScore(
    val displayName: String,
    val description: List<String>,
) {
    STR(
        displayName = "Strength",
        description = listOf(
            "Strength measures bodily power, athletic training, and the extent to which you can exert raw physical force.",
            "A Strength check can model any attempt to lift, push, pull, or break something, to force your body through a space, or to otherwise apply brute force to a situation. The Athletics skill reflects aptitude in certain kinds of Strength checks."
        )
    ),
    DEX(
        displayName = "Dexterity",
        description = listOf(
            "Dexterity measures agility, reflexes, and balance.",
            "A Dexterity check can model any attempt to move nimbly, quickly, or quietly, or to keep from falling on tricky footing. The Acrobatics, Sleight of Hand, and Stealth skills reflect aptitude in certain kinds of Dexterity checks."
        )
    ),
    CON(
        displayName = "Constitution",
        description = listOf(
            "Constitution measures health, stamina, and vital force.",
            "Constitution checks are uncommon, and no skills apply to Constitution checks, because the endurance this ability represents is largely passive rather than involving a specific effort on the part of a character or monster."
        )
    ),
    INT(
        displayName = "Intelligence",
        description = listOf(
            "Intelligence measures mental acuity, accuracy of recall, and the ability to reason.",
            "An Intelligence check comes into play when you need to draw on logic, education, memory, or deductive reasoning. The Arcana, History, Investigation, Nature, and Religion skills reflect aptitude in certain kinds of Intelligence checks.",
        )
    ),
    WIS(
        displayName = "Wisdom",
        description = listOf(
            "Wisdom reflects how attuned you are to the world around you and represents perceptiveness and intuition.",
            "A Wisdom check might reflect an effort to read body language, understand someone's feelings, notice things about the environment, or care for an injured person. The Animal Handling, Insight, Medicine, Perception, and Survival skills reflect aptitude in certain kinds of Wisdom checks.",
        )
    ),
    CHA(
        displayName = "Charisma",
        description = listOf(
            "Charisma measures your ability to interact effectively with others. It includes such factors as confidence and eloquence, and it can represent a charming or commanding personality.",
            "A Charisma check might arise when you try to influence or entertain others, when you try to make an impression or tell a convincing lie, or when you are navigating a tricky social situation. The Deception, Intimidation, Performance, and Persuasion skills reflect aptitude in certain kinds of Charisma checks.",
        )
    );

    val associatedSkills: List<Skill>
        get() = Skill.entries.filter { it.abilityScore == this }
}

