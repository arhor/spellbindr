package com.github.arhor.spellbindr.domain.model

import com.google.common.truth.Truth.assertThat
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.junit.Test

class AbilitySkillSerializationTest {

    @Test
    fun `ability serializes to lowercase string`() {
        // When
        val encoded = Json.encodeToString(Ability.serializer(), Ability.STR)

        // Then
        assertThat(encoded).isEqualTo("\"str\"")
    }

    @Test
    fun `ability deserializes from lowercase string`() {
        // When
        val decoded = Json.decodeFromString(Ability.serializer(), "\"wis\"")

        // Then
        assertThat(decoded).isEqualTo(Ability.WIS)
    }

    @Test
    fun `skill serializes to kebab case`() {
        // When
        val encoded = Json.encodeToString(Skill.serializer(), Skill.ANIMAL_HANDLING)

        // Then
        assertThat(encoded).isEqualTo("\"animal-handling\"")
    }

    @Test
    fun `skill deserializes case-insensitively`() {
        // When
        val decoded = Json.decodeFromString(Skill.serializer(), "\"PeRcEpTiOn\"")

        // Then
        assertThat(decoded).isEqualTo(Skill.PERCEPTION)
    }
}
