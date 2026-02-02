package com.github.arhor.spellbindr.domain.usecase

import com.github.arhor.spellbindr.domain.model.Feature
import com.github.arhor.spellbindr.domain.model.Loadable
import com.github.arhor.spellbindr.domain.repository.FeaturesRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ObserveAllFeaturesUseCase @Inject constructor(
    private val featuresRepository: FeaturesRepository,
) {
    operator fun invoke(): Flow<Loadable<List<Feature>>> =
        featuresRepository.allFeaturesState
}

