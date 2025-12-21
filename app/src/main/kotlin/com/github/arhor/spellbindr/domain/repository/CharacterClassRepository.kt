package com.github.arhor.spellbindr.domain.repository

import com.github.arhor.spellbindr.domain.model.EntityRef

interface CharacterClassRepository {
    suspend fun findSpellcastingClassesRefs(): List<EntityRef>
}
