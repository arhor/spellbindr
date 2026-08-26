package io.github.arhor.dnd.companion.domain.usecase

import io.github.arhor.dnd.companion.domain.model.Condition
import io.github.arhor.dnd.companion.domain.model.Loadable
import io.github.arhor.dnd.companion.domain.repository.ConditionsRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ObserveAllConditionsUseCase @Inject constructor(
    private val conditionsRepository: ConditionsRepository,
) {
    operator fun invoke(): Flow<Loadable<List<Condition>>> =
        conditionsRepository.allConditionsState
}
