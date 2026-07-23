package com.github.arhor.spellbindr.utils

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class AbilityScoreUtilsTest {

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
    fun `standardArray should return canonical six values`() {
        // Given
        val expected = listOf(15, 14, 13, 12, 10, 8)

        // When
        val values = standardArray()

        // Then
        assertThat(values).isEqualTo(expected)
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
}
