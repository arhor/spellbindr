package com.github.arhor.spellbindr.data.repository

import com.github.arhor.spellbindr.data.local.assets.CharacterRaceAssetDataStore
import com.github.arhor.spellbindr.domain.model.Race
import com.google.common.truth.Truth.assertThat
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.withTimeout
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class RacesRepositoryImplTest {

    @Test
    fun `findRaceById should return null when asset load fails`() = runTest {
        val stateFlow = MutableStateFlow<AssetState<List<Race>>>(AssetState.Error(IllegalStateException("Boom")))
        val dataStore = mockk<CharacterRaceAssetDataStore> {
            every { data } returns stateFlow
        }
        val repository = RacesRepositoryImpl(dataStore)

        val result = withTimeout(1_000) { repository.findRaceById("missing") }

        assertThat(result).isNull()
    }
}
