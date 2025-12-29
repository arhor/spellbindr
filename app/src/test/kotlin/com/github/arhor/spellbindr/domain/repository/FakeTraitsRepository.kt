package com.github.arhor.spellbindr.domain.repository

import com.github.arhor.spellbindr.domain.model.AssetState
import com.github.arhor.spellbindr.domain.model.Trait
import kotlinx.coroutines.flow.MutableStateFlow

class FakeTraitsRepository(
    initialTraits: List<Trait> = emptyList(),
) : TraitsRepository {
    override val allTraitsState = MutableStateFlow<AssetState<List<Trait>>>(
        AssetState.Ready(initialTraits)
    )
}
