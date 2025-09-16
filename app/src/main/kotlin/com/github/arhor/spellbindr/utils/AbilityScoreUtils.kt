package com.github.arhor.spellbindr.utils

const val ABILITY_SCORE_DEFAULT_VALUE = 10
const val ABILITY_SCORE_DEFAULT_BONUS = 0

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
fun calculateAbilityScoreModifier(score: Int): String =
    signed((score - ABILITY_SCORE_DEFAULT_VALUE) / 2)

/**
 * Converts an integer value to a string, prefixing it with a "+" sign if the value is non-negative.
 *
 * For example:
 * - `signed(5)` returns `"+5"`
 * - `signed(0)` returns `"+0"`
 * - `signed(-3)` returns `"-3"`
 *
 * @param value The integer value to be converted.
 * @return A string representation of the value, with a leading "+" sign if the value is non-negative.
 */
fun signed(value: Int): String =
    if (value >= 0) "+$value" else value.toString()
