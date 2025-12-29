package com.github.arhor.spellbindr.domain.usecase

import com.github.arhor.spellbindr.domain.model.Race
import com.github.arhor.spellbindr.domain.repository.RacesRepository
import com.github.arhor.spellbindr.utils.unwrap
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ObserveRacesUseCase @Inject constructor(
    private val racesRepository: RacesRepository,
) {
    operator fun invoke(): Flow<List<Race>> =
        racesRepository
            .allRacesState
            .unwrap()
}
