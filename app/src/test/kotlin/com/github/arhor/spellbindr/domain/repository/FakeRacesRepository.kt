package com.github.arhor.spellbindr.domain.repository

import com.github.arhor.spellbindr.domain.model.AssetState
import com.github.arhor.spellbindr.domain.model.Race
import kotlinx.coroutines.flow.MutableStateFlow

class FakeRacesRepository(
    initialRaces: List<Race> = emptyList(),
) : RacesRepository {
    override val allRacesState = MutableStateFlow<AssetState<List<Race>>>(
        AssetState.Ready(initialRaces)
    )

    override suspend fun findRaceById(id: String): Race? =
        (allRacesState.value as? AssetState.Ready)
            ?.data
            ?.firstOrNull { it.id == id }
}
