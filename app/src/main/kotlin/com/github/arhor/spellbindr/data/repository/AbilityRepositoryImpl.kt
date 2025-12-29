package com.github.arhor.spellbindr.data.repository

import com.github.arhor.spellbindr.data.local.assets.AbilityAssetDataStore
import com.github.arhor.spellbindr.data.local.assets.dataOrNull
import com.github.arhor.spellbindr.domain.model.Ability
import com.github.arhor.spellbindr.domain.repository.AbilityRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AbilityRepositoryImpl @Inject constructor(
    private val abilityAssetDataStore: AbilityAssetDataStore,
) : AbilityRepository {

    override val allAbilities: Flow<List<Ability>>
        get() = abilityAssetDataStore.data.map { it.dataOrNull().orEmpty() }
}
