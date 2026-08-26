package io.github.arhor.dnd.companion.data.repository

import io.github.arhor.dnd.companion.data.local.assets.AbilityAssetDataStore
import io.github.arhor.dnd.companion.domain.model.Ability
import io.github.arhor.dnd.companion.domain.model.Loadable
import io.github.arhor.dnd.companion.domain.repository.AbilityRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AbilityRepositoryImpl @Inject constructor(
    private val abilityAssetDataStore: AbilityAssetDataStore,
) : AbilityRepository {

    override val allAbilitiesState: Flow<Loadable<List<Ability>>>
        get() = abilityAssetDataStore.data
}
