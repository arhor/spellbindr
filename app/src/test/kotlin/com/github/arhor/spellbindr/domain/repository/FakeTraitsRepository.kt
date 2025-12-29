package com.github.arhor.spellbindr.domain.repository

import com.github.arhor.spellbindr.domain.model.AssetState
import com.github.arhor.spellbindr.domain.model.Trait
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map

class FakeTraitsRepository(
    initialTraits: List<Trait> = emptyList(),
) : TraitsRepository {
    override val allTraitsState = MutableStateFlow<AssetState<List<Trait>>>(
        AssetState.Ready(initialTraits)
    )

    override val allTraits: Flow<List<Trait>> =
        allTraitsState.map { state ->
            when (state) {
                is AssetState.Ready -> state.data
                else -> emptyList()
            }
        }
}
