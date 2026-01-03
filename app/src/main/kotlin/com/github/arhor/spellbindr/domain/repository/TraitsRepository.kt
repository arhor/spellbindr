package com.github.arhor.spellbindr.domain.repository

import com.github.arhor.spellbindr.domain.model.Loadable
import com.github.arhor.spellbindr.domain.model.Trait
import kotlinx.coroutines.flow.Flow

interface TraitsRepository {
    val allTraitsState: Flow<Loadable<List<Trait>>>
}
