package com.github.arhor.spellbindr.data.repository

import com.github.arhor.spellbindr.data.local.assets.SpellAssetDataStore
import com.github.arhor.spellbindr.domain.model.Loadable
import com.github.arhor.spellbindr.domain.model.Spell
import com.github.arhor.spellbindr.domain.repository.FavoritesRepository
import com.google.common.truth.Truth.assertThat
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.withTimeout
import org.junit.Test

class SpellsRepositoryImplTest {

    @Test
    fun `getSpellById should return null when asset load fails`() = runTest {
        val stateFlow = MutableStateFlow<Loadable<List<Spell>>>(Loadable.Error(IllegalStateException("Boom")))
        val dataStore = mockk<SpellAssetDataStore> {
            every { data } returns stateFlow
        }
        val favoritesRepository = mockk<FavoritesRepository>(relaxed = true)
        val repository = SpellsRepositoryImpl(dataStore, favoritesRepository)

        val result = withTimeout(1_000) { repository.getSpellById("missing") }

        assertThat(result).isNull()
    }
}
