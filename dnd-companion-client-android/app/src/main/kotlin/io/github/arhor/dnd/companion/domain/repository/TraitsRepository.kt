package io.github.arhor.dnd.companion.domain.repository

import io.github.arhor.dnd.companion.domain.model.Loadable
import io.github.arhor.dnd.companion.domain.model.Trait
import kotlinx.coroutines.flow.Flow

interface TraitsRepository {
    val allTraitsState: Flow<Loadable<List<Trait>>>
}
