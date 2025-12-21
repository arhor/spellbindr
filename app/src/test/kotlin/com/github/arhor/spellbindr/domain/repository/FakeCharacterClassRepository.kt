package com.github.arhor.spellbindr.domain.repository

import com.github.arhor.spellbindr.domain.model.EntityRef

class FakeCharacterClassRepository(
    var spellcastingClassesRefs: List<EntityRef> = emptyList(),
) : CharacterClassRepository {
    override suspend fun findSpellcastingClassesRefs(): List<EntityRef> = spellcastingClassesRefs
}
