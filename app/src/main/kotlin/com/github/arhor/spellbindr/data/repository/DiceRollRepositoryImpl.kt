package com.github.arhor.spellbindr.data.repository

import com.github.arhor.spellbindr.data.model.RollHistoryEntry
import com.github.arhor.spellbindr.data.model.RollSet
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * In-memory implementation of DiceRollRepository
 * In a real app, this would use Room database for persistence
 */
@Singleton
class DiceRollRepositoryImpl @Inject constructor() : DiceRollRepository {

    private val _rollHistory = MutableStateFlow<List<RollHistoryEntry>>(emptyList())

    override suspend fun saveRoll(rollSet: RollSet) {
        val entry = RollHistoryEntry(rollSet = rollSet)
        _rollHistory.value = listOf(entry) + _rollHistory.value
    }

    override fun getRollHistory(): Flow<List<RollHistoryEntry>> = _rollHistory.asStateFlow()

    override suspend fun clearHistory() {
        _rollHistory.value = emptyList()
    }

    override suspend fun deleteRoll(rollId: String) {
        _rollHistory.value = _rollHistory.value.filter { it.id != rollId }
    }
}
