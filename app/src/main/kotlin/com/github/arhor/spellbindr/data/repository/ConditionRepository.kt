package com.github.arhor.spellbindr.data.repository

import com.github.arhor.spellbindr.data.local.assets.ConditionAssetDataStore
import com.github.arhor.spellbindr.data.model.Condition
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ConditionRepository @Inject constructor(
    private val conditionsDataStore: ConditionAssetDataStore,
) {
    val allConditions: Flow<List<Condition>>
        get() = conditionsDataStore.data.map { it ?: emptyList() }

    suspend fun findConditionByName(name: String): Condition? =
        allConditions.firstOrNull()?.find { it.name == name }

    suspend fun findConditions(query: String): List<Condition> =
        allConditions.first().filter {
            it.name.contains(query, ignoreCase = true)
        }
}
