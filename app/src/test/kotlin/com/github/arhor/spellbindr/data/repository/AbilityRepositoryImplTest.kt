package com.github.arhor.spellbindr.data.repository

import com.github.arhor.spellbindr.data.local.assets.AbilityAssetDataStore
import com.github.arhor.spellbindr.data.model.AbilityAssetModel
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

    private val abilityFlow = MutableStateFlow<List<AbilityAssetModel>?>(null)
    private val abilityAssetDataStore = mockk<AbilityAssetDataStore> {
        every { data } returns abilityFlow
    }

    private val repository = AbilityRepositoryImpl(abilityAssetDataStore)

    @Test
    fun `allAbilities emits empty list when asset is not loaded`() = runTest {
        val result = repository.allAbilities.first()

        assertThat(result).isEmpty()
    }

    @Test
    fun `allAbilities maps abilities from asset models`() = runTest {
        val strength = AbilityAssetModel(
            id = "str",
            name = "Strength",
            description = listOf("Strength description"),
        )
        val dexterity = AbilityAssetModel(
            id = "dex",
            name = "Dexterity",
            description = listOf("Dexterity description"),
        )

        abilityFlow.value = listOf(strength, dexterity)

        val result = repository.allAbilities.first()

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
