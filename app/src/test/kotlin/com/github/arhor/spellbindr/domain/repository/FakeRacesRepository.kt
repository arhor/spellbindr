package com.github.arhor.spellbindr.domain.repository

import com.github.arhor.spellbindr.domain.model.Loadable
import com.github.arhor.spellbindr.domain.model.Race
import kotlinx.coroutines.flow.MutableStateFlow

class FakeRacesRepository(
    initialRaces: List<Race> = emptyList(),
) : RacesRepository {
    override val allRacesState = MutableStateFlow<Loadable<List<Race>>>(
        Loadable.Ready(initialRaces)
    )

    override suspend fun findRaceById(id: String): Race? =
        (allRacesState.value as? Loadable.Ready)
            ?.data
            ?.firstOrNull { it.id == id }
}
