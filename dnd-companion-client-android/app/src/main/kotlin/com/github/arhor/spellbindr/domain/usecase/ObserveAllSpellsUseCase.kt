package com.github.arhor.spellbindr.domain.usecase

import com.github.arhor.spellbindr.domain.model.Loadable
import com.github.arhor.spellbindr.domain.model.Spell
import com.github.arhor.spellbindr.domain.repository.SpellsRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ObserveAllSpellsUseCase @Inject constructor(
    private val spellsRepository: SpellsRepository,
) {
    operator fun invoke(): Flow<Loadable<List<Spell>>> = spellsRepository.allSpellsState
}
