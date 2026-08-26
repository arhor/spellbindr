package io.github.arhor.dnd.companion.domain.usecase

import io.github.arhor.dnd.companion.domain.model.Feat
import io.github.arhor.dnd.companion.domain.model.Loadable
import io.github.arhor.dnd.companion.domain.repository.FeatsRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ObserveAllFeatsUseCase @Inject constructor(
    private val featsRepository: FeatsRepository,
) {
    operator fun invoke(): Flow<Loadable<List<Feat>>> = featsRepository.allFeatsState
}
