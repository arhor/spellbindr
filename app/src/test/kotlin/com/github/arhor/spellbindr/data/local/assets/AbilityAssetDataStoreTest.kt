package com.github.arhor.spellbindr.data.local.assets

import android.content.Context
import android.content.res.AssetManager
import android.util.Log
import com.google.common.truth.Truth.assertThat
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import org.junit.Before
import org.junit.Test
import java.io.ByteArrayInputStream

class AbilityAssetDataStoreTest {

    private val context = mockk<Context>(relaxed = true)
    private val assetManager = mockk<AssetManager>(relaxed = true)
    private val json = Json { ignoreUnknownKeys = true }

    @Before
    fun setup() {
        mockkStatic(Log::class)
        every { Log.isLoggable(any(), any()) } returns false
    }

    @Test
    fun `should load abilities from json`() = runTest {
        // Given
        val jsonContent = """
            [
              {
                "id": "STR",
                "displayName": "Strength",
                "description": [
                  "Strength measures bodily power, athletic training, and the extent to which you can exert raw physical force.",
                  "A Strength check can model any attempt to lift, push, pull, or break something."
                ]
              },
              {
                "id": "DEX",
                "displayName": "Dexterity",
                "description": [
                  "Dexterity measures agility, reflexes, and balance."
                ]
              },
              {
                "id": "CON",
                "displayName": "Constitution",
                "description": [
                  "Constitution measures health, stamina, and vital force."
                ]
              },
              {
                "id": "INT",
                "displayName": "Intelligence",
                "description": [
                  "Intelligence measures mental acuity, accuracy of recall, and the ability to reason."
                ]
              },
              {
                "id": "WIS",
                "displayName": "Wisdom",
                "description": [
                  "Wisdom reflects how attuned you are to the world around you."
                ]
              },
              {
                "id": "CHA",
                "displayName": "Charisma",
                "description": [
                  "Charisma measures your ability to interact effectively with others."
                ]
              }
            ]
        """.trimIndent()
        val inputStream = ByteArrayInputStream(jsonContent.toByteArray())

        every { context.assets } returns assetManager
        every { assetManager.open("data/abilities.json") } returns inputStream

        val dataStore = AbilityAssetDataStore(context, json)

        // When
        dataStore.initialize()
        val abilities = dataStore.data.value

        // Then
        assertThat(abilities).isNotNull()
        assertThat(abilities).hasSize(6)

        val abilityIds = abilities?.map { it.id }
        assertThat(abilityIds).containsExactly("STR", "DEX", "CON", "INT", "WIS", "CHA")

        val str = abilities?.find { it.id == "STR" }
        assertThat(str?.displayName).isEqualTo("Strength")
        assertThat(str?.description).isNotEmpty()
    }
}
