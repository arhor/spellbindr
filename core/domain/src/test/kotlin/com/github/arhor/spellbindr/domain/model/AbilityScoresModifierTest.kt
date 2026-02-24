package com.github.arhor.spellbindr.domain.model

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class AbilityScoresModifierTest {

    @Test
    fun `modifierFor should floor negative modifiers`() {
        val cases = mapOf(
            8 to -1,
            9 to -1,
            10 to 0,
            11 to 0,
            12 to 1,
        )

        cases.forEach { (score, expected) ->
            val scores = AbilityScores(strength = score)
            assertThat(scores.modifierFor(AbilityIds.STR)).isEqualTo(expected)
        }
    }
}

