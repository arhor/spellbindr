package com.github.arhor.spellbindr.data.model

import com.github.arhor.spellbindr.domain.model.Ability
import com.github.arhor.spellbindr.domain.model.AbilityIds
import com.github.arhor.spellbindr.domain.model.Skill
import com.github.arhor.spellbindr.domain.model.displayName
import com.google.common.truth.Truth.assertThat
import kotlinx.serialization.json.Json
import org.junit.Test
import java.nio.file.Files
import java.nio.file.Paths

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
    fun `abilities asset should expose ids names and descriptions when parsed from json`() {
        // Given
        val abilitiesById = abilitiesFromAsset.associateBy(AbilityAssetModel::id)

        // When
        val orderedAbilities = AbilityIds.standardOrder.map(abilitiesById::getValue)

        // Then
        assertThat(abilitiesById.keys).containsExactlyElementsIn(AbilityIds.standardOrder)
        assertThat(orderedAbilities).hasSize(AbilityIds.standardOrder.size)

        AbilityIds.standardOrder.forEach { abilityId ->
            val ability = abilitiesById.getValue(abilityId)
            assertThat(ability.displayName).isEqualTo(abilityId.displayName())
            assertThat(ability.description).hasSize(2)
            assertThat(ability.description.first()).contains(ability.displayName)
            assertThat(ability.description.last()).isNotEmpty()
        }

        assertThat(abilitiesById.getValue(AbilityIds.STR).description[1]).contains("Athletics skill reflects aptitude")
    }

    @Test
    fun `skills should reference abilities defined in assets when mapping ability ids`() {
        // Given
        val abilityIds = abilitiesFromAsset.map(AbilityAssetModel::id).toSet()

        // When
        val referencedAbilityIds = Skill.values().map(Skill::abilityId).toSet()

        // Then
        assertThat(abilityIds).containsAtLeastElementsIn(referencedAbilityIds)
    }

    @Test
    fun `ability serializer should round trip when encoding and decoding object`() {
        // Given
        val ability = Ability(
            id = "str",
            displayName = "Strength",
            description = listOf("Test description"),
        )

        // When
        val encoded = json.encodeToString(Ability.serializer(), ability)
        val decoded = json.decodeFromString(Ability.serializer(), encoded)

        // Then
        assertThat(decoded).isEqualTo(ability)
    }

    @Test
    fun `ability serializer should decode from json object when attributes are provided`() {
        // Given
        val encoded = """
            {
              "id": "wis",
              "displayName": "Wisdom",
              "description": ["desc"]
            }
        """.trimIndent()

        // When
        val decoded = json.decodeFromString(
            Ability.serializer(),
            encoded,
        )

        // Then
        val expected = Ability(
            id = "wis",
            displayName = "Wisdom",
            description = listOf("desc"),
        )
        assertThat(decoded).isEqualTo(expected)
    }

    @Test
    fun `skill serializer should serialize to kebab case when encoding value`() {
        // Given
        val skill = Skill.ANIMAL_HANDLING

        // When
        val encoded = json.encodeToString(Skill.serializer(), skill)

        // Then
        assertThat(encoded).isEqualTo("\"animal-handling\"")
    }

    @Test
    fun `skill serializer should deserialize case-insensitively when decoding value`() {
        // Given
        val encoded = "\"PeRcEpTiOn\""

        // When
        val decoded = json.decodeFromString(Skill.serializer(), encoded)

        // Then
        assertThat(decoded).isEqualTo(Skill.PERCEPTION)
    }
}
