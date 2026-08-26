package com.github.arhor.spellbindr.data.repository

import com.github.arhor.spellbindr.data.local.assets.CharacterRaceAssetDataStore
import com.github.arhor.spellbindr.domain.model.Loadable
import com.github.arhor.spellbindr.domain.model.Race
import com.google.common.truth.Truth.assertThat
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.withTimeout
import org.junit.Test

class RacesRepositoryImplTest {

    @Test
    fun `findRaceById should return null when asset load fails`() = runTest {
        // Given
        val stateFlow = MutableStateFlow<Loadable<List<Race>>>(Loadable.Failure(cause = IllegalStateException("Boom")))
        val dataStore = mockk<CharacterRaceAssetDataStore> {
            every { data } returns stateFlow
        }
        val repository = RacesRepositoryImpl(dataStore)

        // When
        val result = withTimeout(1_000) { repository.findRaceById("missing") }

        // Then
        assertThat(result).isNull()
    }
}
