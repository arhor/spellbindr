package io.github.arhor.dnd.companion.domain.repository

import io.github.arhor.dnd.companion.domain.model.Loadable
import io.github.arhor.dnd.companion.domain.model.Race
import kotlinx.coroutines.flow.Flow

interface RacesRepository {
    val allRacesState: Flow<Loadable<List<Race>>>

    suspend fun findRaceById(id: String): Race?
}
