package com.github.arhor.spellbindr.domain.usecase

import com.github.arhor.spellbindr.domain.model.Spell
import com.github.arhor.spellbindr.domain.repository.SpellsRepository
import com.github.arhor.spellbindr.utils.unwrap
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ObserveAllSpellsUseCase @Inject constructor(
    private val spellsRepository: SpellsRepository,
) {
    operator fun invoke(): Flow<List<Spell>> =
        spellsRepository
            .allSpellsState
            .unwrap()
}
