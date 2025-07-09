@file:Suppress("unused")

package com.github.arhor.spellbindr.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Represents a requirement that a character must satisfy.
 * This is a sealed interface, meaning all possible implementations are known at compile time.
 * This is useful for things like serialization and deserialization where all possible types
 * need to be known.
 *
 * Each implementation of this interface should define its own criteria for satisfaction.
 *
 * @see LevelRequirement for an example implementation.
 */
@Serializable
sealed interface Requirement {
    fun isSatisfiedBy(character: Character): Boolean

    /**
     * Represents a requirement that a character must have a certain level.
     *
     * @property level The minimum level required.
     */
    @Serializable
    @SerialName("level_requirement")
    data class LevelRequirement(
        val level: Int,
    ) : Requirement {
        override fun isSatisfiedBy(character: Character) = character.level >= level
    }
}
