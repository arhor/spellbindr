package com.github.arhor.spellbindr.utils

import org.junit.Assert.assertEquals
import org.junit.Test

class StringExtTest {

    @Test
    fun `toTitleCase capitalizes words and trims separators`() {
        // When
        val result = "  elDRitCh  BlaST  ".toTitleCase()

        // Then
        assertEquals("Eldritch Blast", result)
    }
}
