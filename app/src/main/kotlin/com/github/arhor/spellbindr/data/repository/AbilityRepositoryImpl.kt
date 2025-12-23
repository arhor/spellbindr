package com.github.arhor.spellbindr.data.repository

import com.github.arhor.spellbindr.data.local.assets.AbilityAssetDataStore
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

    override fun getAbilities(): Flow<List<Ability>> {
        return abilityAssetDataStore.data.map { abilities ->
            abilities?.map { ability ->
                Ability(
                    id = ability.id,
                    displayName = ability.displayName,
                    description = ability.description,
                )
            } ?: emptyList()
        }
    }
}

