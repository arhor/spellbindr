package io.github.arhor.dnd.companion.domain.repository

import io.github.arhor.dnd.companion.domain.model.Feat
import io.github.arhor.dnd.companion.domain.model.Loadable
import kotlinx.coroutines.flow.Flow

interface FeatsRepository {
    val allFeatsState: Flow<Loadable<List<Feat>>>
}
