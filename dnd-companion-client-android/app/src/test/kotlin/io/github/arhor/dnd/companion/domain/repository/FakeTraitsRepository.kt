package io.github.arhor.dnd.companion.domain.repository

import io.github.arhor.dnd.companion.domain.model.Loadable
import io.github.arhor.dnd.companion.domain.model.Trait
import kotlinx.coroutines.flow.MutableStateFlow

class FakeTraitsRepository(
    initialTraits: List<Trait> = emptyList(),
) : TraitsRepository {
    override val allTraitsState = MutableStateFlow<Loadable<List<Trait>>>(
        Loadable.Content(initialTraits)
    )
}
