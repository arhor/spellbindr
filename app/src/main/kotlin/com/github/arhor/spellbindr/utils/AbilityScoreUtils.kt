package com.github.arhor.spellbindr.utils

import kotlin.math.floor
import kotlin.random.Random

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

fun calculatePointBuyCost(scores: Map<String, Int>): Int {
    return scores.values.sumOf { score ->
        when (score) {
            8 -> 0
            9 -> 1
            10 -> 2
            11 -> 3
            12 -> 4
            13 -> 5
            14 -> 7
            15 -> 9
            else -> 0
        }
    }
}

fun standardArray(): List<Int> {
    return listOf(15, 14, 13, 12, 10, 8)
}

fun roll4d6DropLowest(): List<Int> {
    return List(6) {
        val rolls = List(4) { Random.nextInt(1, 7) }
        rolls.sortedDescending().take(3).sum()
    }
}

private val abilityNames = listOf("STR", "DEX", "CON", "INT", "WIS", "CHA")

fun calculateModifier(score: Int): Int {
    return floor((score - 10) / 2.0).toInt()
}

fun generate(
    method: GenerationMethod,
    assignedScores: Map<String, Int> = emptyMap()
): AbilityScores {
    val scores = when (method) {
        is GenerationMethod.Roll -> {
            val rolledScores = roll4d6DropLowest()
            if (method.autoAssign) {
                abilityNames.zip(rolledScores.sortedDescending()).toMap()
            } else {
                assignedScores
            }
        }

        is GenerationMethod.StandardArray -> assignedScores
        is GenerationMethod.PointBuy -> assignedScores
    }

    val modifiers: Map<String, Int> = scores.mapValues { (_, score) -> calculateModifier(score) }
    val pointBuyCost = if (method is GenerationMethod.PointBuy) {
        calculatePointBuyCost(scores)
    } else {
        null
    }

    return AbilityScores(
        scores = scores,
        modifiers = modifiers,
        pointBuyCost = pointBuyCost,
    )
}

sealed class GenerationMethod {
    data class Roll(val autoAssign: Boolean = false) : GenerationMethod()
    data object StandardArray : GenerationMethod()
    data object PointBuy : GenerationMethod()
}

data class AbilityScores(
    val scores: Map<String, Int>,
    val modifiers: Map<String, Int>,
    val pointBuyCost: Int?,
)
