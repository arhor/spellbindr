package com.github.arhor.spellbindr.utils

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class AbilityScoreUtilsTest {

    @Test
    fun `calculateAbilityScoreModifier formats signed modifier`() {
        // Given
        val scores = mapOf(15 to "+2", 10 to "+0", 9 to "-1")

        // When
        val results = scores.mapValues { (score, _) -> calculateAbilityScoreModifier(score) }

        // Then
        assertThat(results).isEqualTo(scores)
    }

    @Test
    fun `signed adds plus prefix for non negative values`() {
        // When
        val values = listOf(5, 0, -3).map(::signed)

        // Then
        assertThat(values).containsExactly("+5", "+0", "-3").inOrder()
    }

    @Test
    fun `asCommaSeparatedString skips zero modifiers`() {
        // Given
        val modifiers = linkedMapOf(
            "STR" to 2,
            "DEX" to 0,
            "CON" to -1,
        )

        // When
        val result = modifiers.asCommaSeparatedString()

        // Then
        assertThat(result).isEqualTo("STR: +2, CON: -1")
    }

    @Test
    fun `calculatePointBuyCost sums predefined costs`() {
        // Given
        val scores = mapOf("STR" to 15, "DEX" to 12, "CON" to 8, "INT" to 10, "WIS" to 13)

        // When
        val cost = calculatePointBuyCost(scores)

        // Then
        assertThat(cost).isEqualTo(9 + 4 + 0 + 2 + 5)
    }

    @Test
    fun `calculateModifier floors odd scores`() {
        // When
        val modifier = calculateModifier(9)

        // Then
        assertThat(modifier).isEqualTo(-1)
    }

    @Test
    fun `roll4d6DropLowest produces six values within allowed range`() {
        // When
        val scores = roll4d6DropLowest()

        // Then
        assertThat(scores).hasSize(6)
        assertThat(scores.all { it in 3..18 }).isTrue()
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
        assertThat(abilityScores.scores).isEqualTo(assignedScores)
        assertThat(abilityScores.modifiers).isEqualTo(mapOf("STR" to 2, "DEX" to 2, "CON" to 1, "INT" to 1, "WIS" to 0, "CHA" to -1))
        assertThat(abilityScores.pointBuyCost).isNull()
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
        assertThat(abilityScores.scores).isEqualTo(assignedScores)
        assertThat(abilityScores.modifiers).isEqualTo(mapOf("STR" to 2, "DEX" to 1, "CON" to 0, "INT" to 1, "WIS" to 2, "CHA" to -1))
        assertThat(abilityScores.pointBuyCost).isEqualTo(9 + 4 + 2 + 5 + 7 + 0)
}
}
