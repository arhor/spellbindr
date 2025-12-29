package com.github.arhor.spellbindr.data.repository

import androidx.compose.runtime.Stable
import com.github.arhor.spellbindr.data.local.assets.CharacterRaceAssetDataStore
import com.github.arhor.spellbindr.domain.model.AssetState
import com.github.arhor.spellbindr.domain.model.Race
import com.github.arhor.spellbindr.domain.repository.RacesRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import javax.inject.Singleton

@Stable
@Singleton
class RacesRepositoryImpl @Inject constructor(
    private val racesDataStore: CharacterRaceAssetDataStore,
) : RacesRepository {

    override val allRacesState: Flow<AssetState<List<Race>>>
        get() = racesDataStore.data

    override suspend fun findRaceById(id: String): Race? =
        when (val state = racesDataStore.data.first { it !is AssetState.Loading }) {
            is AssetState.Ready -> state.data.find { it.id == id }
            is AssetState.Error -> null
            is AssetState.Loading -> null
        }
}
