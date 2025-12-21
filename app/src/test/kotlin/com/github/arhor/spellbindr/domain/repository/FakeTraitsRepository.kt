package com.github.arhor.spellbindr.domain.repository

import com.github.arhor.spellbindr.domain.model.Trait
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class FakeTraitsRepository(
    initialTraits: List<Trait> = emptyList(),
) : TraitsRepository {
    val allTraitsState = MutableStateFlow(initialTraits)

    override val allTraits: StateFlow<List<Trait>> = allTraitsState
}
