package com.github.arhor.spellbindr.domain.usecase

import com.github.arhor.spellbindr.domain.model.Spell
import com.github.arhor.spellbindr.domain.repository.SpellsRepository
import javax.inject.Inject

class GetSpellByIdUseCase @Inject constructor(
    private val spellsRepository: SpellsRepository,
) {
    suspend operator fun invoke(id: String): Spell? = spellsRepository.getSpellById(id)
}
