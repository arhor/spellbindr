package com.github.arhor.spellbindr.domain.repository

import com.github.arhor.spellbindr.domain.model.Feature
import com.github.arhor.spellbindr.domain.model.Loadable
import kotlinx.coroutines.flow.Flow

interface FeaturesRepository {
    val allFeaturesState: Flow<Loadable<List<Feature>>>
}

