package io.github.arhor.dnd.companion.domain.repository

import io.github.arhor.dnd.companion.domain.model.Ability
import io.github.arhor.dnd.companion.domain.model.Loadable
import kotlinx.coroutines.flow.Flow

interface AbilityRepository {
    val allAbilitiesState: Flow<Loadable<List<Ability>>>
}
