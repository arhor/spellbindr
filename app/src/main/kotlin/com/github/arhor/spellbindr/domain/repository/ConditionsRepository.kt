package com.github.arhor.spellbindr.domain.repository

import com.github.arhor.spellbindr.domain.model.Condition
import com.github.arhor.spellbindr.domain.model.Loadable
import kotlinx.coroutines.flow.Flow

interface ConditionsRepository {
    val allConditionsState: Flow<Loadable<List<Condition>>>
}
