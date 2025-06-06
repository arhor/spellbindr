package com.github.arhor.spellbindr.data.repository

import androidx.compose.runtime.Stable
import com.github.arhor.spellbindr.data.datasource.local.RacesAssetDataStore
import com.github.arhor.spellbindr.data.model.Race
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Stable
@Singleton
class RacesRepository @Inject constructor(
    private val racesDataStore: RacesAssetDataStore,
) {
    val allRaces: Flow<List<Race>>
        get() = racesDataStore.data.map { it ?: emptyList() }

    suspend fun findBackgroundById(id: String): Race? =
        allRaces.firstOrNull()?.find { it.id == id }
} 
