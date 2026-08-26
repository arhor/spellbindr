package com.github.arhor.spellbindr.data.repository

import com.github.arhor.spellbindr.data.local.assets.ConditionsDataStore
import com.github.arhor.spellbindr.domain.model.Condition
import com.github.arhor.spellbindr.domain.model.Loadable
import com.github.arhor.spellbindr.domain.repository.ConditionsRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ConditionsRepositoryImpl @Inject constructor(
    private val conditionsDataStore: ConditionsDataStore,
) : ConditionsRepository {

    override val allConditionsState: Flow<Loadable<List<Condition>>>
        get() = conditionsDataStore.data
}
