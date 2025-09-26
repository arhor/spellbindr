package com.github.arhor.spellbindr.data.repository

import androidx.compose.runtime.Stable
import com.github.arhor.spellbindr.data.local.assets.CharacterRaceAssetDataStore
import com.github.arhor.spellbindr.data.model.next.CharacterRace
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Stable
@Singleton
class RacesRepository @Inject constructor(
    private val racesDataStore: CharacterRaceAssetDataStore,
) {
    val allRaces: Flow<List<CharacterRace>>
        get() = racesDataStore.data.map { it ?: emptyList() }

    suspend fun findRaceById(id: String): CharacterRace? =
        allRaces.firstOrNull()?.find { it.id == id }
} 
