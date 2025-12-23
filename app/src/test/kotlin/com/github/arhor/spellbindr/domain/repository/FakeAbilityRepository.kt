package com.github.arhor.spellbindr.domain.repository

import com.github.arhor.spellbindr.domain.model.Ability
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

class FakeAbilityRepository(
    initialAbilities: List<Ability> = emptyList(),
) : AbilityRepository {
    val abilitiesState = MutableStateFlow(initialAbilities)

    override fun getAbilities(): Flow<List<Ability>> = abilitiesState
}

