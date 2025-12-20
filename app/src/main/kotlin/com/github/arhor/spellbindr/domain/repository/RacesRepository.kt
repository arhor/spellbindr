package com.github.arhor.spellbindr.domain.repository

import com.github.arhor.spellbindr.domain.model.Race
import kotlinx.coroutines.flow.Flow

interface RacesRepository {
    val allRaces: Flow<List<Race>>

    suspend fun findRaceById(id: String): Race?
}
