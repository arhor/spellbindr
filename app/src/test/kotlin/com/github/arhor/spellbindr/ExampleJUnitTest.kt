package com.github.arhor.spellbindr

import org.junit.Assert.assertEquals
import org.junit.Test

class ExampleJUnitTest {

    @Test
    fun `addition should return sum when two integers are added`() {
        // Given
        val augend = 2
        val addend = 2

        // When
        val result = augend + addend

        // Then
        assertEquals(4, result)
    }
}
