package com.github.arhor.spellbindr.utils

import com.github.arhor.spellbindr.domain.model.EntityRef
import com.google.common.truth.Truth.assertThat
import kotlinx.serialization.json.Json
import org.junit.Test

class ReferenceSerializerTest {

    @Test
    fun `serialize writes reference id as json string`() {
        // When
        val encoded = Json.encodeToString(EntityRefSerializer, EntityRef("spell-123"))

        // Then
        assertThat(encoded).isEqualTo("\"spell-123\"")
    }

    @Test
    fun `deserialize creates reference from json string`() {
        // When
        val decoded = Json.decodeFromString(EntityRefSerializer, "\"spell-123\"")

        // Then
        assertThat(decoded).isEqualTo(EntityRef("spell-123"))
    }
}
