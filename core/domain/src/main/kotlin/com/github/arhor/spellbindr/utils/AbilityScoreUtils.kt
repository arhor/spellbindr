package com.github.arhor.spellbindr.utils

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
fun signed(value: Int): String = if (value >= 0) "+$value" else value.toString()

fun calculatePointBuyCost(scores: Map<*, Int>): Int = scores.values.sumOf(::pointBuyCost)

fun standardArray(): List<Int> = listOf(15, 14, 13, 12, 10, 8)

private fun pointBuyCost(score: Int): Int = when (score) {
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
