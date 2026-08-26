package io.github.arhor.dnd.companion.data.repository

import io.github.arhor.dnd.companion.data.local.assets.CharacterRaceAssetDataStore
import io.github.arhor.dnd.companion.domain.model.Loadable
import io.github.arhor.dnd.companion.domain.model.Race
import io.github.arhor.dnd.companion.domain.repository.RacesRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RacesRepositoryImpl @Inject constructor(
    private val racesDataStore: CharacterRaceAssetDataStore,
) : RacesRepository {

    override val allRacesState: Flow<Loadable<List<Race>>>
        get() = racesDataStore.data

    override suspend fun findRaceById(id: String): Race? =
        when (val state = racesDataStore.data.first { it !is Loadable.Loading }) {
            is Loadable.Content -> state.data.find { it.id == id }
            is Loadable.Failure -> null
            is Loadable.Loading -> null
        }
}
