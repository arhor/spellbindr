package com.github.arhor.spellbindr.utils

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class StringExtTest {

    @Test
    fun `toTitleCase should capitalize words and trim separators when input contains mixed casing`() {
        // Given
        val input = "  elDRitCh  BlaST  "

        // When
        val result = input.toTitleCase()

        // Then
        assertThat(result).isEqualTo("Eldritch Blast")
    }
}
