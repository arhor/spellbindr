package com.github.arhor.spellbindr.utils

import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.Assert.assertThrows

class CaseInsensitiveEnumSerializerTest {

    @Test
    fun `serialize converts enum name to kebab-case lowercase`() {
        // Given
        val serializer = CaseInsensitiveEnumSerializer<Example>()

        // When
        val encoded = Json.encodeToString(serializer, Example.LONG_NAME)

        // Then
        assertEquals("\"long-name\"", encoded)
    }

    @Test
    fun `deserialize accepts case-insensitive kebab values`() {
        // Given
        val serializer = CaseInsensitiveEnumSerializer<Example>()

        // When
        val decoded = Json.decodeFromString(serializer, "\"LoNg-NaMe\"")

        // Then
        assertEquals(Example.LONG_NAME, decoded)
    }

    @Test
    fun `deserialize fails for unknown enum value`() {
        // Given
        val serializer = CaseInsensitiveEnumSerializer<Example>()

        // When
        val result = assertThrows(IllegalArgumentException::class.java) {
            Json.decodeFromString(serializer, "\"missing\"")
        }

        // Then
        assertTrue(result.message!!.contains("missing"))
    }

    private enum class Example {
        LONG_NAME,
        SIMPLE,
    }
}
