package com.github.arhor.spellbindr.domain.serialization

import com.google.common.truth.Truth.assertThat
import kotlinx.serialization.json.Json
import org.junit.Assert.assertThrows
import org.junit.Test

class CaseInsensitiveEnumSerializerTest {

    @Test
    fun `serialize should convert enum name to kebab-case lowercase when value is provided`() {
        // Given
        val serializer = CaseInsensitiveEnumSerializer<Example>()

        // When
        val encoded = Json.encodeToString(serializer, Example.LONG_NAME)

        // Then
        assertThat(encoded).isEqualTo("\"long-name\"")
    }

    @Test
    fun `deserialize should accept case-insensitive kebab values when parsing enum`() {
        // Given
        val serializer = CaseInsensitiveEnumSerializer<Example>()

        // When
        val decoded = Json.decodeFromString(serializer, "\"LoNg-NaMe\"")

        // Then
        assertThat(decoded).isEqualTo(Example.LONG_NAME)
    }

    @Test
    fun `deserialize should throw IllegalArgumentException when value is unknown`() {
        // Given
        val serializer = CaseInsensitiveEnumSerializer<Example>()

        // When
        val result = assertThrows(IllegalArgumentException::class.java) {
            Json.decodeFromString(serializer, "\"missing\"")
        }

        // Then
        assertThat(result).hasMessageThat().contains("missing")
    }

    private enum class Example {
        LONG_NAME,
        SIMPLE,
    }
}
