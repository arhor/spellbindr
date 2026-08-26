package com.github.arhor.spellbindr.domain.repository

import com.github.arhor.spellbindr.domain.model.Feat
import com.github.arhor.spellbindr.domain.model.Loadable
import kotlinx.coroutines.flow.Flow

interface FeatsRepository {
    val allFeatsState: Flow<Loadable<List<Feat>>>
}
