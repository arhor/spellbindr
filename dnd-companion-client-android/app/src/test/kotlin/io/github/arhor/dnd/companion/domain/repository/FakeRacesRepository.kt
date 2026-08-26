package io.github.arhor.dnd.companion.domain.repository

import io.github.arhor.dnd.companion.domain.model.Loadable
import io.github.arhor.dnd.companion.domain.model.Race
import kotlinx.coroutines.flow.MutableStateFlow

class FakeRacesRepository(
    initialRaces: List<Race> = emptyList(),
) : RacesRepository {
    override val allRacesState = MutableStateFlow<Loadable<List<Race>>>(
        Loadable.Content(initialRaces)
    )

    override suspend fun findRaceById(id: String): Race? =
        (allRacesState.value as? Loadable.Content)
            ?.data
            ?.firstOrNull { it.id == id }
}
