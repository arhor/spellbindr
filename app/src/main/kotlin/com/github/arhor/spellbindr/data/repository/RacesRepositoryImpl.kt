package com.github.arhor.spellbindr.data.repository

import androidx.compose.runtime.Stable
import com.github.arhor.spellbindr.data.local.assets.CharacterRaceAssetDataStore
import com.github.arhor.spellbindr.domain.model.Race
import com.github.arhor.spellbindr.domain.repository.RacesRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Stable
@Singleton
class RacesRepositoryImpl @Inject constructor(
    private val racesDataStore: CharacterRaceAssetDataStore,
) : RacesRepository {
    override val allRaces: Flow<List<Race>>
        get() = racesDataStore.data.map { it.orEmpty() }

    override suspend fun findRaceById(id: String): Race? =
        allRaces.firstOrNull()?.find { it.id == id }
}
