package io.github.arhor.dnd.companion.domain.usecase

import io.github.arhor.dnd.companion.domain.model.Loadable
import io.github.arhor.dnd.companion.domain.model.Race
import io.github.arhor.dnd.companion.domain.repository.RacesRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ObserveAllRacesUseCase @Inject constructor(
    private val racesRepository: RacesRepository,
) {
    operator fun invoke(): Flow<Loadable<List<Race>>> =
        racesRepository.allRacesState
}
