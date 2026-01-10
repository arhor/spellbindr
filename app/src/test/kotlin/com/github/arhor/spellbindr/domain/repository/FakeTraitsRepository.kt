package com.github.arhor.spellbindr.domain.repository

import com.github.arhor.spellbindr.domain.model.Loadable
import com.github.arhor.spellbindr.domain.model.Trait
import kotlinx.coroutines.flow.MutableStateFlow

class FakeTraitsRepository(
    initialTraits: List<Trait> = emptyList(),
) : TraitsRepository {
    override val allTraitsState = MutableStateFlow<Loadable<List<Trait>>>(
        Loadable.Content(initialTraits)
    )
}
