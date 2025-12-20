package com.github.arhor.spellbindr.data.repository

import androidx.compose.runtime.Stable
import com.github.arhor.spellbindr.data.local.assets.CharacterRaceAssetDataStore
import com.github.arhor.spellbindr.data.mapper.toDomain
import com.github.arhor.spellbindr.domain.model.Race
import com.github.arhor.spellbindr.domain.repository.RacesRepository as RacesRepositoryContract
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Stable
@Singleton
class RacesRepository @Inject constructor(
    private val racesDataStore: CharacterRaceAssetDataStore,
) : RacesRepositoryContract {
    override val allRaces: Flow<List<Race>>
        get() = racesDataStore.data.map { races -> races.orEmpty().map { it.toDomain() } }

    override suspend fun findRaceById(id: String): Race? =
        allRaces.firstOrNull()?.find { it.id == id }
}
