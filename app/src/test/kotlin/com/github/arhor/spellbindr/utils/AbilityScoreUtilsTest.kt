package com.github.arhor.spellbindr.utils

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class AbilityScoreUtilsTest {

    @Test
    fun `calculateAbilityScoreModifier should format signed modifier when score values are provided`() {
        // Given
        val scores = mapOf(15 to "+2", 10 to "+0", 9 to "-1")

        // When
        val results = scores.mapValues { (score, _) -> calculateAbilityScoreModifier(score) }

        // Then
        assertThat(results).isEqualTo(scores)
    }

    @Test
    fun `signed should add plus prefix for non negative values when values are positive or zero`() {
        // Given
        val inputs = listOf(5, 0, -3)

        // When
        val values = inputs.map(::signed)

        // Then
        assertThat(values).containsExactly("+5", "+0", "-3").inOrder()
    }

    @Test
    fun `asCommaSeparatedString should skip zero modifiers when building output string`() {
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
    fun `calculatePointBuyCost should sum predefined costs when ability scores are provided`() {
        // Given
        val scores = mapOf("STR" to 15, "DEX" to 12, "CON" to 8, "INT" to 10, "WIS" to 13)

        // When
        val cost = calculatePointBuyCost(scores)

        // Then
        assertThat(cost).isEqualTo(9 + 4 + 0 + 2 + 5)
    }

    @Test
    fun `calculateModifier should floor odd scores when computing modifier`() {
        // Given
        val score = 9

        // When
        val modifier = calculateModifier(score)

        // Then
        assertThat(modifier).isEqualTo(-1)
    }

    @Test
    fun `roll4d6DropLowest should produce six values within allowed range when generating scores`() {
        // Given
        // No additional setup required

        // When
        val scores = roll4d6DropLowest()

        // Then
        assertThat(scores).hasSize(6)
        assertThat(scores.all { it in 3..18 }).isTrue()
    }

    @Test
    fun `generate should build modifiers without point buy cost when method is standardArray`() {
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
    fun `generate should build modifiers and cost when method is pointBuy`() {
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
