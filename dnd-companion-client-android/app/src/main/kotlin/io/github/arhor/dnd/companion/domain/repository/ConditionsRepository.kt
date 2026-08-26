package io.github.arhor.dnd.companion.domain.repository

import io.github.arhor.dnd.companion.domain.model.Condition
import io.github.arhor.dnd.companion.domain.model.Loadable
import kotlinx.coroutines.flow.Flow

interface ConditionsRepository {
    val allConditionsState: Flow<Loadable<List<Condition>>>
}
