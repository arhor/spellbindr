package com.github.arhor.spellbindr.domain.serialization

import com.github.arhor.spellbindr.domain.model.EntityRef
import com.google.common.truth.Truth.assertThat
import kotlinx.serialization.json.Json
import org.junit.Test

class ReferenceSerializerTest {

    @Test
    fun `serialize should write reference id as json string when encoding entity ref`() {
        // Given
        val reference = EntityRef("spell-123")

        // When
        val encoded = Json.encodeToString(EntityRefSerializer, reference)

        // Then
        assertThat(encoded).isEqualTo("\"spell-123\"")
    }

    @Test
    fun `deserialize should create reference from json string when decoding entity ref`() {
        // Given
        val encoded = "\"spell-123\""

        // When
        val decoded = Json.decodeFromString(EntityRefSerializer, encoded)

        // Then
        assertThat(decoded).isEqualTo(EntityRef("spell-123"))
    }
}
