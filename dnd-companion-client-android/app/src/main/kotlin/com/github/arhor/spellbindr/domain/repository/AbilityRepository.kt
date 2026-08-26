package com.github.arhor.spellbindr.domain.repository

import com.github.arhor.spellbindr.domain.model.Ability
import com.github.arhor.spellbindr.domain.model.Loadable
import kotlinx.coroutines.flow.Flow

interface AbilityRepository {
    val allAbilitiesState: Flow<Loadable<List<Ability>>>
}
