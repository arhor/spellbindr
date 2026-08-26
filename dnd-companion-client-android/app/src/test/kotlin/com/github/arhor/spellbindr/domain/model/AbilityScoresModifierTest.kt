package com.github.arhor.spellbindr.domain.model

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class AbilityScoresModifierTest {

    @Test
    fun `modifierFor should floor negative modifiers when score is below ten`() {
        // Given
        val cases = mapOf(
            8 to -1,
            9 to -1,
            10 to 0,
            11 to 0,
            12 to 1,
        )

        // When
        val actual = cases.mapValues { (score, _) ->
            val scores = AbilityScores(strength = score)
            scores.modifierFor(AbilityIds.STR)
        }

        // Then
        assertThat(actual).containsExactlyEntriesIn(cases)
    }
}
