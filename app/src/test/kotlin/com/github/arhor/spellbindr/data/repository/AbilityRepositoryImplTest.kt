package com.github.arhor.spellbindr.data.repository

import com.github.arhor.spellbindr.data.local.assets.AbilityAssetDataStore
import com.github.arhor.spellbindr.domain.model.Ability
import com.google.common.truth.Truth.assertThat
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class AbilityRepositoryImplTest {

    private val abilityFlow = MutableStateFlow<List<Ability>?>(null)
    private val abilityAssetDataStore = mockk<AbilityAssetDataStore> {
        every { data } returns abilityFlow
    }

    private val repository = AbilityRepositoryImpl(abilityAssetDataStore)

    @Test
    fun `allAbilities should emit empty list when asset is not loaded`() = runTest {
        // Given
        // No abilities loaded into the data store

        // When
        val result = repository.allAbilities.first()

        // Then
        assertThat(result).isEmpty()
    }

    @Test
    fun `allAbilities should map abilities from asset models when data is available`() = runTest {
        // Given
        val strength = Ability(
            id = "str",
            displayName = "Strength",
            description = listOf("Strength description"),
        )
        val dexterity = Ability(
            id = "dex",
            displayName = "Dexterity",
            description = listOf("Dexterity description"),
        )

        abilityFlow.value = listOf(strength, dexterity)

        // When
        val result = repository.allAbilities.first()

        // Then
        assertThat(result).containsExactly(
            Ability(
                id = "str",
                displayName = "Strength",
                description = listOf("Strength description"),
            ),
            Ability(
                id = "dex",
                displayName = "Dexterity",
                description = listOf("Dexterity description"),
            ),
        ).inOrder()
    }
}
