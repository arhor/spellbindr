package com.github.arhor.spellbindr.utils

/**
 * Calculates the ability score modifier based on the provided score.
 *
 * The modifier is determined by subtracting 10 from the score and then dividing the result by 2,
 * rounding down.
 *
 * For example:
 * - A score of 10 or 11 results in a +0 modifier.
 * - A score of 12 or 13 results in a +1 modifier.
 * - A score of 8 or 9 results in a -1 modifier.
 *
 * The function returns the modifier as a string, prefixed with a "+" sign if the modifier is positive.
 *
 * @param score The ability score (e.g., Strength, Dexterity, Constitution, Intelligence, Wisdom, Charisma).
 * @return A string representation of the ability score modifier (e.g., "+2", "-1", "+0").
 */
fun calculateAbilityScoreModifier(score: Int): String {
    val result = (score - 10) / 2
    val prefix = if (result >= 0) "+" else ""

    return "$prefix$result"
}
