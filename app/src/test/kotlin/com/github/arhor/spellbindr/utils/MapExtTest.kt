package com.github.arhor.spellbindr.utils

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class MapExtTest {

    @Test
    fun `copy creates new map with applied mutations`() {
        // Given
        val original = mapOf("a" to 1, "b" to 2)

        // When
        val result = original.copy { this["c"] = 3 }

        // Then
        assertThat(result).isNotSameInstanceAs(original)
        assertThat(result).containsExactlyEntriesIn(mapOf("a" to 1, "b" to 2, "c" to 3))
        assertThat(original).containsExactlyEntriesIn(mapOf("a" to 1, "b" to 2))
    }
}
