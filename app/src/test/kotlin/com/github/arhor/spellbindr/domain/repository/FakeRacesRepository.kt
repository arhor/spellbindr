package com.github.arhor.spellbindr.domain.repository

import com.github.arhor.spellbindr.domain.model.Race
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class FakeRacesRepository(
    initialRaces: List<Race> = emptyList(),
) : RacesRepository {
    val allRacesState = MutableStateFlow(initialRaces)

    override val allRaces: StateFlow<List<Race>> = allRacesState

    override suspend fun findRaceById(id: String): Race? = allRacesState.value.firstOrNull { it.id == id }
}
