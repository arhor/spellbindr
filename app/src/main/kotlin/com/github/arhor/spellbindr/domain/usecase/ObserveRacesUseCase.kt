package com.github.arhor.spellbindr.domain.usecase

import com.github.arhor.spellbindr.domain.model.AssetState
import com.github.arhor.spellbindr.domain.model.Race
import com.github.arhor.spellbindr.domain.repository.RacesRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class ObserveRacesUseCase @Inject constructor(
    private val racesRepository: RacesRepository,
) {
    operator fun invoke(): Flow<List<Race>> = racesRepository.allRacesState.map {
        when (it) {
            is AssetState.Loading -> emptyList()
            is AssetState.Ready -> it.data
            is AssetState.Error -> emptyList()
        }
    }
}
