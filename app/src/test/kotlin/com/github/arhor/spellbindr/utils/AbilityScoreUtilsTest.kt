package com.github.arhor.spellbindr.utils

import com.github.arhor.spellbindr.data.model.predefined.Ability
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class AbilityScoreUtilsTest {

    @Test
    fun `calculateAbilityScoreModifier formats signed modifier`() {
        // Given
        val scores = mapOf(15 to "+2", 10 to "+0", 9 to "-1")

        // When
        val results = scores.mapValues { (score, _) -> calculateAbilityScoreModifier(score) }

        // Then
        assertEquals(scores, results)
    }

    @Test
    fun `signed adds plus prefix for non negative values`() {
        // When
        val values = listOf(5, 0, -3).map(::signed)

        // Then
        assertEquals(listOf("+5", "+0", "-3"), values)
    }

    @Test
    fun `asCommaSeparatedString skips zero modifiers`() {
        // Given
        val modifiers = linkedMapOf(
            Ability.STR to 2,
            Ability.DEX to 0,
            Ability.CON to -1,
        )

        // When
        val result = modifiers.asCommaSeparatedString()

        // Then
        assertEquals("STR: +2, CON: -1", result)
    }

    @Test
    fun `calculatePointBuyCost sums predefined costs`() {
        // Given
        val scores = mapOf("STR" to 15, "DEX" to 12, "CON" to 8, "INT" to 10, "WIS" to 13)

        // When
        val cost = calculatePointBuyCost(scores)

        // Then
        assertEquals(9 + 4 + 0 + 2 + 5, cost)
    }

    @Test
    fun `calculateModifier floors odd scores`() {
        // When
        val modifier = calculateModifier(9)

        // Then
        assertEquals(-1, modifier)
    }

    @Test
    fun `roll4d6DropLowest produces six values within allowed range`() {
        // When
        val scores = roll4d6DropLowest()

        // Then
        assertEquals(6, scores.size)
        assertTrue(scores.all { it in 3..18 })
    }

    @Test
    fun `generate standardArray builds modifiers without point buy cost`() {
        // Given
        val assignedScores = mapOf(
            "STR" to 15,
            "DEX" to 14,
            "CON" to 13,
            "INT" to 12,
            "WIS" to 10,
            "CHA" to 8,
        )

        // When
        val abilityScores = generate(
            method = GenerationMethod.StandardArray,
            assignedScores = assignedScores,
        )

        // Then
        assertEquals(assignedScores, abilityScores.scores)
        assertEquals(mapOf("STR" to 2, "DEX" to 2, "CON" to 1, "INT" to 1, "WIS" to 0, "CHA" to -1), abilityScores.modifiers)
        assertEquals(null, abilityScores.pointBuyCost)
    }

    @Test
    fun `generate pointBuy builds modifiers and cost`() {
        // Given
        val assignedScores = mapOf(
            "STR" to 15,
            "DEX" to 12,
            "CON" to 10,
            "INT" to 13,
            "WIS" to 14,
            "CHA" to 8,
        )

        // When
        val abilityScores = generate(
            method = GenerationMethod.PointBuy,
            assignedScores = assignedScores,
        )

        // Then
        assertEquals(assignedScores, abilityScores.scores)
        assertEquals(mapOf("STR" to 2, "DEX" to 1, "CON" to 0, "INT" to 1, "WIS" to 2, "CHA" to -1), abilityScores.modifiers)
        assertEquals(9 + 4 + 2 + 5 + 7 + 0, abilityScores.pointBuyCost)
    }
}
