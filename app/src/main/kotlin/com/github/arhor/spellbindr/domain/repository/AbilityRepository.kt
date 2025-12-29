package com.github.arhor.spellbindr.domain.repository

import com.github.arhor.spellbindr.domain.model.Ability
import com.github.arhor.spellbindr.domain.model.AssetState
import kotlinx.coroutines.flow.Flow

interface AbilityRepository {
    val allAbilitiesState: Flow<AssetState<List<Ability>>>
}
