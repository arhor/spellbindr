package com.github.arhor.spellbindr.ui.screens.characters.creation

import kotlin.math.floor
import kotlin.random.Random

data class AbilityScores(
    val scores: Map<String, Int>,
    val modifiers: Map<String, Int>,
    val pointBuyCost: Int?,
)

object AbilityScoreGenerator {

    private val abilityNames = listOf("STR", "DEX", "CON", "INT", "WIS", "CHA")

    fun calculateModifier(score: Int): Int {
        return floor((score - 10) / 2.0).toInt()
    }

    fun roll4d6DropLowest(): List<Int> {
        return List(6) {
            val rolls = List(4) { Random.nextInt(1, 7) }
            rolls.sortedDescending().take(3).sum()
        }
    }

    fun standardArray(): List<Int> {
        return listOf(15, 14, 13, 12, 10, 8)
    }

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
}
