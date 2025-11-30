package com.github.arhor.spellbindr.utils

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotSame
import org.junit.Test

class MapExtTest {

    @Test
    fun `copy creates new map with applied mutations`() {
        // Given
        val original = mapOf("a" to 1, "b" to 2)

        // When
        val result = original.copy { this["c"] = 3 }

        // Then
        assertNotSame(original, result)
        assertEquals(mapOf("a" to 1, "b" to 2, "c" to 3), result)
        assertEquals(mapOf("a" to 1, "b" to 2), original)
    }
}
