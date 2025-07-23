package com.github.arhor.spellbindr.data.repository

import com.github.arhor.spellbindr.data.local.assets.ConditionAssetDataStore
import com.github.arhor.spellbindr.data.model.Condition
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ConditionRepository @Inject constructor(
    private val conditionsDataStore: ConditionAssetDataStore,
) {
    val allConditions: Flow<List<Condition>>
        get() = conditionsDataStore.data.map { it ?: emptyList() }
}
