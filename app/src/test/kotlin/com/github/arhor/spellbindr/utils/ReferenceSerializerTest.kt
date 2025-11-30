package com.github.arhor.spellbindr.utils

import com.github.arhor.spellbindr.data.model.next.Reference
import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Test

class ReferenceSerializerTest {

    @Test
    fun `serialize writes reference id as json string`() {
        // When
        val encoded = Json.encodeToString(ReferenceSerializer, Reference("spell-123"))

        // Then
        assertEquals("\"spell-123\"", encoded)
    }

    @Test
    fun `deserialize creates reference from json string`() {
        // When
        val decoded = Json.decodeFromString(ReferenceSerializer, "\"spell-123\"")

        // Then
        assertEquals(Reference("spell-123"), decoded)
    }
}
