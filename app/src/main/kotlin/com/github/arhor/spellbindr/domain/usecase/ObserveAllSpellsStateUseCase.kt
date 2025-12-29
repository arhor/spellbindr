package com.github.arhor.spellbindr.domain.usecase

import com.github.arhor.spellbindr.domain.model.AssetState
import com.github.arhor.spellbindr.domain.model.Spell
import com.github.arhor.spellbindr.domain.repository.SpellsRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ObserveAllSpellsStateUseCase @Inject constructor(
    private val spellsRepository: SpellsRepository,
) {
    operator fun invoke(): Flow<AssetState<List<Spell>>> = spellsRepository.allSpellsState
}

