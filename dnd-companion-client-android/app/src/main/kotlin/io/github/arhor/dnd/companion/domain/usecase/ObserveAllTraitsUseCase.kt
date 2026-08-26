package io.github.arhor.dnd.companion.domain.usecase

import io.github.arhor.dnd.companion.domain.model.Loadable
import io.github.arhor.dnd.companion.domain.model.Trait
import io.github.arhor.dnd.companion.domain.repository.TraitsRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ObserveAllTraitsUseCase @Inject constructor(
    private val traitsRepository: TraitsRepository,
) {
    operator fun invoke(): Flow<Loadable<List<Trait>>> =
        traitsRepository.allTraitsState
}
