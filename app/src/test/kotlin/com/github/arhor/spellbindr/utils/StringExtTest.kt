package com.github.arhor.spellbindr.utils

import com.google.common.truth.Truth.assertThat
import org.junit.jupiter.api.Test

class StringExtTest {

    @Test
    fun `toTitleCase capitalizes words and trims separators`() {
        // When
        val result = "  elDRitCh  BlaST  ".toTitleCase()

        // Then
        assertThat(result).isEqualTo("Eldritch Blast")
    }
}
