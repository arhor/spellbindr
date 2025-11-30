package com.github.arhor.spellbindr.utils

import com.github.arhor.spellbindr.data.model.next.Reference
import com.google.common.truth.Truth.assertThat
import kotlinx.serialization.json.Json
import org.junit.Test

class ReferenceSerializerTest {

    @Test
    fun `serialize writes reference id as json string`() {
        // When
        val encoded = Json.encodeToString(ReferenceSerializer, Reference("spell-123"))

        // Then
        assertThat(encoded).isEqualTo("\"spell-123\"")
    }

    @Test
    fun `deserialize creates reference from json string`() {
        // When
        val decoded = Json.decodeFromString(ReferenceSerializer, "\"spell-123\"")

        // Then
        assertThat(decoded).isEqualTo(Reference("spell-123"))
    }
}
