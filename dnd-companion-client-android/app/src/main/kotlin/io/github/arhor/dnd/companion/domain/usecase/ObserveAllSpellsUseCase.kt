package io.github.arhor.dnd.companion.domain.usecase

import io.github.arhor.dnd.companion.domain.model.Loadable
import io.github.arhor.dnd.companion.domain.model.Spell
import io.github.arhor.dnd.companion.domain.repository.SpellsRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ObserveAllSpellsUseCase @Inject constructor(
    private val spellsRepository: SpellsRepository,
) {
    operator fun invoke(): Flow<Loadable<List<Spell>>> = spellsRepository.allSpellsState
}
