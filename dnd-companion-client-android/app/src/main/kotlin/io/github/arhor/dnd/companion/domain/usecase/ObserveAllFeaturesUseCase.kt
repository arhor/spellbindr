package io.github.arhor.dnd.companion.domain.usecase

import io.github.arhor.dnd.companion.domain.model.Feature
import io.github.arhor.dnd.companion.domain.model.Loadable
import io.github.arhor.dnd.companion.domain.repository.FeaturesRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ObserveAllFeaturesUseCase @Inject constructor(
    private val featuresRepository: FeaturesRepository,
) {
    operator fun invoke(): Flow<Loadable<List<Feature>>> =
        featuresRepository.allFeaturesState
}

