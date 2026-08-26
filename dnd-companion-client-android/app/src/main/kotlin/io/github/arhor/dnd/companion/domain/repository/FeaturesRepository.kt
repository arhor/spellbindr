package io.github.arhor.dnd.companion.domain.repository

import io.github.arhor.dnd.companion.domain.model.Feature
import io.github.arhor.dnd.companion.domain.model.Loadable
import kotlinx.coroutines.flow.Flow

interface FeaturesRepository {
    val allFeaturesState: Flow<Loadable<List<Feature>>>
}

