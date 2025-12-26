package com.github.arhor.spellbindr.domain.model

import com.google.common.truth.Truth.assertThat
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.junit.Test

class AbilitySkillSerializationTest {

    @Test
    fun `ability serializes and deserializes as object`() {
        val ability = Ability(
            id = "str",
            displayName = "Strength",
            description = listOf("Test description"),
        )

        val encoded = Json.encodeToString(Ability.serializer(), ability)
        val decoded = Json.decodeFromString(Ability.serializer(), encoded)

        assertThat(decoded).isEqualTo(ability)
    }

    @Test
    fun `ability deserializes from json object`() {
        val decoded = Json.decodeFromString(
            Ability.serializer(),
            """
            {
              "id": "wis",
              "displayName": "Wisdom",
              "description": ["desc"]
            }
            """.trimIndent(),
        )

        assertThat(decoded).isEqualTo(
            Ability(
                id = "wis",
                displayName = "Wisdom",
                description = listOf("desc"),
            )
        )
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
