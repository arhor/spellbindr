package com.github.arhor.spellbindr.domain.repository

import com.github.arhor.spellbindr.domain.model.Ability
import kotlinx.coroutines.flow.Flow

interface AbilityRepository {
    val allAbilities: Flow<List<Ability>>
}
