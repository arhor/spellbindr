package com.github.arhor.spellbindr.data.model

import com.github.arhor.spellbindr.domain.model.Ability
import com.github.arhor.spellbindr.domain.model.AbilityIds
import com.github.arhor.spellbindr.domain.model.Skill
import com.github.arhor.spellbindr.domain.model.displayName
import com.google.common.truth.Truth.assertThat
import java.nio.file.Files
import java.nio.file.Paths
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.junit.Test

class AbilitySkillSerializationTest {

    private val json = Json {
        ignoreUnknownKeys = true
        classDiscriminator = "type"
    }

    private val abilitiesJsonPath by lazy {
        listOf(
            Paths.get("app", "src", "main", "assets", "data", "abilities.json"),
            Paths.get("src", "main", "assets", "data", "abilities.json"),
        ).firstOrNull(Files::exists) ?: error("Expected abilities asset under src/main/assets/data")
    }

    private val abilitiesFromAsset by lazy {
        json.decodeFromString<List<AbilityAssetModel>>(abilitiesJsonPath.toFile().readText())
    }

    @Test
    fun `abilities asset exposes ids names and descriptions`() {
        val abilitiesById = abilitiesFromAsset.associateBy(AbilityAssetModel::id)

        assertThat(abilitiesById.keys).containsExactlyElementsIn(AbilityIds.standardOrder)

        AbilityIds.standardOrder.forEach { abilityId ->
            val ability = abilitiesById.getValue(abilityId)
            assertThat(ability.name).isEqualTo(abilityId.displayName())
            assertThat(ability.description).hasSize(2)
            assertThat(ability.description.first()).contains(ability.name)
            assertThat(ability.description.last()).isNotEmpty()
        }

        assertThat(abilitiesById.getValue(AbilityIds.STR).description[1]).contains("Athletics skill reflects aptitude")
    }

    @Test
    fun `skills reference abilities defined in assets`() {
        val abilityIds = abilitiesFromAsset.map(AbilityAssetModel::id).toSet()

        Skill.values().forEach { skill ->
            assertThat(abilityIds).contains(skill.abilityId)
        }
    }

    @Test
    fun `ability serializes and deserializes as object`() {
        val ability = Ability(
            id = "str",
            displayName = "Strength",
            description = listOf("Test description"),
        )

        val encoded = json.encodeToString(Ability.serializer(), ability)
        val decoded = json.decodeFromString(Ability.serializer(), encoded)

        assertThat(decoded).isEqualTo(ability)
    }

    @Test
    fun `ability deserializes from json object`() {
        val decoded = json.decodeFromString(
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
        val encoded = json.encodeToString(Skill.serializer(), Skill.ANIMAL_HANDLING)

        // Then
        assertThat(encoded).isEqualTo("\"animal-handling\"")
    }

    @Test
    fun `skill deserializes case-insensitively`() {
        // When
        val decoded = json.decodeFromString(Skill.serializer(), "\"PeRcEpTiOn\"")

        // Then
        assertThat(decoded).isEqualTo(Skill.PERCEPTION)
    }
}
