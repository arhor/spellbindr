package com.github.arhor.spellbindr.data.repository

import com.github.arhor.spellbindr.data.local.assets.SpellAssetDataStore
import com.github.arhor.spellbindr.domain.model.AssetState
import com.github.arhor.spellbindr.domain.model.Spell
import com.github.arhor.spellbindr.domain.repository.FavoritesRepository
import com.google.common.truth.Truth.assertThat
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.withTimeout
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class SpellsRepositoryImplTest {

    @Test
    fun `getSpellById should return null when asset load fails`() = runTest {
        val stateFlow = MutableStateFlow<AssetState<List<Spell>>>(AssetState.Error(IllegalStateException("Boom")))
        val dataStore = mockk<SpellAssetDataStore> {
            every { data } returns stateFlow
        }
        val favoritesRepository = mockk<FavoritesRepository>(relaxed = true)
        val repository = SpellsRepositoryImpl(dataStore, favoritesRepository)

        val result = withTimeout(1_000) { repository.getSpellById("missing") }

        assertThat(result).isNull()
    }
}
