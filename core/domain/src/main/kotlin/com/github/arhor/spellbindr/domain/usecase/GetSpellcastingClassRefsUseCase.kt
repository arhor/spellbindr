package com.github.arhor.spellbindr.domain.usecase

import com.github.arhor.spellbindr.domain.model.EntityRef
import com.github.arhor.spellbindr.domain.repository.CharacterClassRepository
import javax.inject.Inject

class GetSpellcastingClassRefsUseCase @Inject constructor(
    private val characterClassRepository: CharacterClassRepository,
) {
    suspend operator fun invoke(): List<EntityRef> = characterClassRepository.findSpellcastingClassesRefs()
}
