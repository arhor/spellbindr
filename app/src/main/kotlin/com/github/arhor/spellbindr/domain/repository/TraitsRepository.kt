package com.github.arhor.spellbindr.domain.repository

import com.github.arhor.spellbindr.domain.model.AssetState
import com.github.arhor.spellbindr.domain.model.Trait
import kotlinx.coroutines.flow.Flow

interface TraitsRepository {
    val allTraitsState: Flow<AssetState<List<Trait>>>
}
