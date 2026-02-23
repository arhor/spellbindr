package com.github.arhor.spellbindr.domain.usecase

import com.github.arhor.spellbindr.domain.model.Loadable
import com.github.arhor.spellbindr.domain.model.Race
import com.github.arhor.spellbindr.domain.repository.RacesRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ObserveAllRacesUseCase @Inject constructor(
    private val racesRepository: RacesRepository,
) {
    operator fun invoke(): Flow<Loadable<List<Race>>> =
        racesRepository.allRacesState
}
